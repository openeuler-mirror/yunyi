package com.tongtech.proxy.core.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * Title: Background Server
 * </p>
 *
 * <p>
 * Description:
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 *
 * <p>
 * Company:
 * </p>
 *
 * @author AGK
 * @version 1.0
 */

public class Log extends Thread {

    private File fLogFile;

    private final File fBaseLogFile;

    private final int iBuffSize;

    // private SimpleDateFormat sdfLog = new SimpleDateFormat(
    // "yyyy-MM-dd HH:mm:ss ");
    private final SimpleDateFormat sdfLog = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");

    private long lNextDayBegin = 0; // 下次切换日志的时间

    private int iTimeZoneOffset = TimeZone.getDefault().getRawOffset(); // 程序运行环境的时区偏差，单位毫秒

    private FieldPosition fpNumberPosition = new FieldPosition(0); //

    // 定义日志级别
    public static final int iLogLevelNothing = 0;

    public static final int iLogLevelError = 1;

    public static final int iLogLevelWarning = 2;

    public static final int iLogLevelInfo = 3;

    public static final int iLogLevelDebug = 4;

    public static final int iLogLevelDump = 5;

    private final long lMaxFileSize;

    private final int iLogBackupDays;

    private volatile int iCurLogLevel;

    private Vector<String> vCurMsgs = null; // 当前日志内容列表

    private Vector<String> vBackMsgs = null; // 备份日志内容列表

    private final int iMaxDebugLogList = 20000; // Debug级别的日志未实际写入文件的日志的最大条数
    private final int iMaxInfoLogList = 23000; // Info级别的日志未实际写入文件的日志的最大条数
    private final int iMaxWarnLogList = 25000; // Warn级别的日志未实际写入文件的日志的最大条数

    private final int iLogListEmptyWarn = iMaxDebugLogList / 5; // 未实际写入文件的日志的最小条数

    private final int iLogListFullWarn = iMaxDebugLogList - iLogListEmptyWarn; // 未实际写入文件的日志的最小条数

    private final ReentrantLock synchronizedPutLog = new ReentrantLock(); // 写文件同步锁

    private volatile boolean bClosed = false; // 日志关闭标志

    // 日志写文件的最大间隔时间的毫秒数
    private static final int MAX_WAITING = 1000;

    // 日志写文件的最小间隔时间的毫秒数
    private static final int MIN_WAITING = 50;

    // 日志写文件的实际间隔时间,初始为MAX_WAITING
    private int iLogWaitingPerWrite = MAX_WAITING;

    public Log(File file) {
        this(file, Log.iLogLevelDebug, 0);
    }

    public Log(File file, int bacupdates) {
        this(file, Log.iLogLevelDebug, bacupdates);
    }

    public Log(File file, int loglevel, int backupdays) {
        this(file, loglevel, (long) 1024 * 1024 * 1024, backupdays);
    }

    public Log(File file, int loglevel, long maxsize, int backupdays) {
        this(file, loglevel, (int) 8192, maxsize, backupdays);
    }

    /**
     * 构造函数，如果缺省，则使用 Log.iLogLevelInfo，BufferSize = 8192。
     *
     * @param file     file: 日志文件名描述类
     * @param loglevel int: 日志级别，缺省为Info
     * @param buflen   int: 写文件时使用的cache大小，缺省是8192。
     * @param maxsize  long: 日志文件大小的限制,当日志文件达到该值时会自动改名为同目录下带时间标签的文件
     */
    public Log(File file, int loglevel, int buflen, long maxsize, int backupdays) {

//        if (VENDER_ID < 2) {
//            sdfLog = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");
//        } else if (VENDER_ID < 20) {
//            if (((VENDER_ID - 2) % 4) == 0) {
//                sdfLog = new SimpleDateFormat("yyyyMMdd HHmmss:SSS ");
//            } else if (((VENDER_ID - 2) % 4) == 1) {
//                sdfLog = new SimpleDateFormat("yy.MM.dd HH:mm:ss[SSS] ");
//            } else if (((VENDER_ID - 2) % 4) == 2) {
//                sdfLog = new SimpleDateFormat("MM-dd-yyyy HH.mm.ss:SSS ");
//            } else {
//                sdfLog = new SimpleDateFormat("MM:dd HH.mm.ss-SSS ");
//            }
//        } else {
//            sdfLog = new SimpleDateFormat("HH-mm-ss ");
//        }

        // 新建文件的buffer大小
        if (buflen < 1024) {
            buflen = 1024;
        }
        this.iBuffSize = buflen;

        // 不带日期的日志文件
        this.fBaseLogFile = file;

        // 当前日志级别
        this.iCurLogLevel = loglevel;

        this.lMaxFileSize = maxsize;

        this.iLogBackupDays = backupdays;

        // 计算第2天凌晨
        this.lNextDayBegin = getNextDayBegin();
        // System.out.println(this.sdfDayFormat.format(new
        // Date(this.lNextCvtTime-1)));

        // 创建日志缓冲列表类
        int i = this.iMaxWarnLogList / 15;
        this.vBackMsgs = new Vector<String>(i, i);
        this.vCurMsgs = new Vector<String>(i, i);

        // 启动写日志后台线程
        this.setDaemon(true);
        this.start();
    }

    /**
     * 获得第2天的开始时间
     *
     * @return long 第2天凌晨0点的毫秒数
     */
    private long getNextDayBegin() {
        long cur = System.currentTimeMillis() + this.iTimeZoneOffset;
        return (cur / 86400000 + 1) * 86400000 - this.iTimeZoneOffset;
    }

    /**
     * 返回当前日志文件名（包括路径）
     */
    public String getLogfile() {
        return this.fBaseLogFile != null ? this.fBaseLogFile.getAbsolutePath()
                : null;
    }

    /**
     * @return boolean 日志文件已经关闭，返回真。日志关闭后不能再恢复为打开状态。
     */
    public boolean isClosed() {
        return this.bClosed;
    }

    /**
     * 关闭日志文件，在程序退出前，要调用该函数，否则可能会丢数据。
     */
    public void close() {
        if (!this.bClosed) {
            try {
                this.bClosed = true;
                writeLogsToFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 调试日志输出，该函数会在输出日志前加时间和“DEBUG”字样
     *
     * @return boolean 日志输出成功返回true，否则返回false。
     */
    public boolean debugLog(String msg, Object... vars) {
        if (Log.iLogLevelDebug <= this.iCurLogLevel
                && this.iCurLogLevel != Log.iLogLevelNothing) {
            try {
                return log(Log.iLogLevelDebug, "DEBUG ", merge(msg, vars), iMaxDebugLogList);
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean debugLog(Throwable t, String msg, Object... vars) {
        debugLog(msg, vars);
        if (t != null) {
            StackTraceElement[] elements = t.getStackTrace();
            StringBuilder trace = new StringBuilder(32);
            trace.append("\t\t");
            for (StackTraceElement element : elements) {
                if (element != null) {
                    trace.setLength(2);
                    trace.append(element.toString());
                    debugLog(trace.toString());
                }
            }
        }
        return true;
    }


    /**
     * 标准日志输出，该函数会在输出日志前加时间和“INFO”字样
     *
     * @return boolean 日志输出成功返回true，否则返回false。
     */
    public boolean infoLog(String msg, Object... vars) {
        if (Log.iLogLevelInfo <= this.iCurLogLevel
                && this.iCurLogLevel != Log.iLogLevelNothing) {
            try {
                return log(Log.iLogLevelInfo, "INFO  ", merge(msg, vars), iMaxInfoLogList);
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean infoLog(Throwable t, String msg, Object... vars) {
        infoLog(msg, vars);
        if (t != null) {
            StackTraceElement[] elements = t.getStackTrace();
            StringBuilder trace = new StringBuilder(32);
            trace.append("\t\t");
            for (StackTraceElement element : elements) {
                if (element != null) {
                    trace.setLength(2);
                    trace.append(element.toString());
                    infoLog(trace.toString());
                }
            }
        }
        return true;
    }

    /**
     * 错误日志输出，该函数会在输出日志前加时间和“WARNING”字样
     *
     * @return boolean 日志输出成功返回true，否则返回false。
     */
    public boolean warnLog(String msg, Object... vars) {
        if (Log.iLogLevelWarning <= this.iCurLogLevel
                && this.iCurLogLevel != Log.iLogLevelNothing) {
            try {
                return log(Log.iLogLevelWarning, "WARN  ", merge(msg, vars), iMaxWarnLogList);
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean warnLog(Throwable t, String msg, Object... vars) {
        warnLog(msg, vars);
        if (t != null) {
            StackTraceElement[] elements = t.getStackTrace();
            StringBuilder trace = new StringBuilder(32);
            trace.append("\t\t");
            for (StackTraceElement element : elements) {
                if (element != null) {
                    trace.setLength(2);
                    trace.append(element.toString());
                    warnLog(trace.toString());
                }
            }
        }
        return true;
    }

    /**
     * 错误日志输出，该函数会在输出日志前加时间和“ERROR”字样
     *
     * @return boolean 日志输出成功返回true，否则返回false。
     */
    public boolean errorLog(String msg, Object... vars) {
        if (Log.iLogLevelError <= this.iCurLogLevel
                && this.iCurLogLevel != Log.iLogLevelNothing) {
            try {
                return log(Log.iLogLevelError, "ERROR ", merge(msg, vars), iMaxWarnLogList);
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean errorLog(Throwable t, String msg, Object... vars) {
        errorLog(msg, vars);
        if (t != null) {
            StackTraceElement[] elements = t.getStackTrace();
            StringBuilder trace = new StringBuilder(32);
            trace.append("\t\t");
            for (StackTraceElement element : elements) {
                if (element != null) {
                    trace.setLength(2);
                    trace.append(element.toString());
                    errorLog(trace.toString());
                }
            }
        }
        return true;
    }

    /**
     * 错误日志输出，该函数会在输出日志前加时间和“ERROR”字样
     *
     * @return boolean 日志输出成功返回true，否则返回false。
     */
    public boolean coreLog(String msg, Object... vars) {
        try {
            return log(Log.iLogLevelNothing, "CORE  ", merge(msg, vars), iMaxWarnLogList);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean coreLog(Throwable t, String msg, Object... vars) {
        coreLog(msg, vars);
        if (t != null) {
            StackTraceElement[] elements = t.getStackTrace();
            StringBuilder trace = new StringBuilder(32);
            trace.append("\t\t");
            for (StackTraceElement element : elements) {
                if (element != null) {
                    trace.setLength(2);
                    trace.append(element.toString());
                    coreLog(trace.toString());
                }
            }
        }
        return true;
    }

    private void appendString(StringBuilder buf, Object o) {
        if (o instanceof List) {
            List list = (List) o;
            buf.append('[');
            for (int i = 0; i < list.size(); ++i) {
                if (i != 0) {
                    buf.append(',');
                    buf.append(' ');
                }
                appendString(buf, list.get(i));
            }
            buf.append(']');
        } else if (o instanceof byte[]) {
            byte[] data = (byte[]) o;
            for (byte b : data) {
                if (b >= 32 && b < 127) {
                    buf.append((char) b);
                } else {
                    buf.append(String.format("\\x%02x", b));
                }
            }
//            buf.append(StaticContent.escape(data, 0, data.length));
        } else if (o instanceof Throwable) {
            buf.append(((Throwable) o).getMessage());
        } else {
            buf.append(o);
        }
    }

    private String merge(String msg, Object... vars) {
        String ret = msg;
        if (vars != null && vars.length > 0) {
            int varIdx = 0;
            StringBuilder buf = new StringBuilder();
            int start = 0;
            int end = 0;
            int len = msg.length();
            while (end < len - 1 && varIdx < vars.length) {
                char c = msg.charAt(end);
                if (c == '\\') {
                    if (end > start) {
                        buf.append(msg.substring(start, end));
                    }
                    buf.append(msg.charAt(end + 1));
                    end += 2;
                    start = end;
                    continue;
                } else if (c == '{' && msg.charAt(end + 1) == '}') {
                    if (end > start) {
                        buf.append(msg.substring(start, end));
                    }
//                    buf.append(vars[varIdx] != null ? vars[varIdx].toString() : "null");
                    appendString(buf, vars[varIdx]);
                    varIdx++;
                    end += 2;
                    start = end;
                    continue;
                }
                end++;
            }
            if (start < len) {
                buf.append(msg.substring(start));
            }
            // 转换\t，否则日志会很奇怪
            for (int i = buf.length() - 1; i >= 0; --i) {
                if (buf.charAt(i) == '\r') {
                    buf.setCharAt(i, ' ');
                }
            }
            ret = buf.toString();
        } else {
            // 转换\t
            ret = ret.replace('\r', ' ');
        }
        return ret;
    }

    /**
     * 日志输出基础函数。该函数只将日志写到buffer列表里， 并不实际写文件（实际写文件需要调用writeLogsToFile方法）。
     *
     * @param log 日志内容
     * @return boolean 日志输出成功返回true，否则返回false。
     */
    private boolean log(int loglevel, String head, String log, int max) {
        boolean ret = false;

        // 检查日志级别是否正确
        if (loglevel <= this.iCurLogLevel
                && this.iCurLogLevel != Log.iLogLevelNothing) {
            // 日志级别符合要求，检查是否可以写日志
//            synchronizedPutLog.lock();
            try {
                if (this.vCurMsgs.size() < max && !this.bClosed && log != null) {
                    StringBuffer msg = new StringBuffer(64);
                    this.sdfLog.format(new Date(), msg, this.fpNumberPosition);
                    if (head != null) {
                        msg.append(head);
                    }
                    // debug以上级别的日志中增加线程名
                    if (this.iCurLogLevel >= Log.iLogLevelDebug) {
                        msg.append("[")
                                .append(Thread.currentThread().getName())
                                .append("] ");
                    }
                    msg.append(log);
                    String logstr = msg.toString();
                    synchronizedPutLog.lock();
                    try {
//                        if (this.vCurMsgs.size() < max && !this.bClosed) {
//                            this.vCurMsgs.addElement(logstr);
//                            ret = true;
//                        }
                        if (!this.bClosed) {
                            if (this.vCurMsgs.size() < max) {
                                this.vCurMsgs.addElement(logstr);
                                ret = true;
                            } else if (this.vCurMsgs.size() >= max) {
                                this.vCurMsgs.setSize(max - 100);
                                this.vCurMsgs.addElement("... some messages have been lost ...");
                            }
                        }
                    } finally {
                        synchronizedPutLog.unlock();
                    }
                } else {
//                    ret = false;
                    // if (!this.bClosed) {
                    // System.err
                    // .println("Buffer list too small, log is losted by "
                    // + this.toString());
                    // }
                }
            } catch (Exception e) {
//                ret = false;
//            } finally {
//                synchronizedPutLog.unlock();
            }
        }
        return ret;
    }

    /**
     * 减小写文件间隔时间，当写文件时发现缓存的日志量过多时调用该函数
     */
    private void decreaseWaitTime() {
        if (iLogWaitingPerWrite > MIN_WAITING) {
            iLogWaitingPerWrite = Math.max(iLogWaitingPerWrite >>> 1,
                    MIN_WAITING);
        }
    }

    /**
     * 增加写文件时间间隔，当写文件时发现缓存的日志条数过少时调用该函数
     */
    private void increaseWaitTime() {
        if (iLogWaitingPerWrite < MAX_WAITING) {
            iLogWaitingPerWrite = Math.min(iLogWaitingPerWrite + 10,
                    MAX_WAITING);
        }
    }

    /**
     * 删除过期日志文件
     */
    private void removeExpiredLogs() {
        if (this.iLogBackupDays > 0 && this.fBaseLogFile != null) {
            try {
                File path = this.fBaseLogFile.getParentFile();
                if (path != null) {
                    String baselog = this.fBaseLogFile.getName();
                    StringBuffer sb = new StringBuffer(256);
                    sb.append(baselog);
                    sb.append(".");
                    Date date = new Date(System.currentTimeMillis() - this.iLogBackupDays * 1000 * 86400);
                    SimpleDateFormat sdfDayFormat = new SimpleDateFormat("yyyyMMdd");
                    sdfDayFormat.format(date, sb, this.fpNumberPosition);
                    sb.append("235959");
                    String logwithdate = sb.toString();
                    for (File f : path.listFiles()) {
                        String logname = f.getName();
                        if (logname.startsWith(baselog)
                                && logname.compareTo(logwithdate) <= 0) {
                            try {
                                f.delete();
                            } catch (Exception e) {
                            }
                        }
                    }
                }
            } catch (Throwable t) {
            }
        }
    }

    /**
     * 刷新缓冲，将buffer中的日志实际写到文件里。为了减少写文件时对其他线程写日志的影响，
     * 程序创建了2个bugger，开始写文件前将空闲buffer和当前buffer互换，实际写文件操作后台并行执行。
     *
     * @return boolean 是否成功
     */
    private boolean writeLogsToFile() {
        boolean is_ok = true;
        long currenttime = System.currentTimeMillis();
        boolean isChangeFile = false;

        synchronizedPutLog.lock();
        try {
            int size = this.vCurMsgs.size();

            // 调整写日志间隔时间
            if (size < iLogListEmptyWarn) {
                increaseWaitTime();
            } else if (size > iLogListFullWarn) {
                decreaseWaitTime();
            }

            // 日志列表为空，不需要写文件
            if (size == 0) {
                return true;
            }
        } finally {
            synchronizedPutLog.unlock();
        }

        // 检查日志是否换名
        if (this.fLogFile == null || currenttime > this.lNextDayBegin) {
            if (this.fLogFile != null) {
                // 每次重启时都是null
                isChangeFile = true;
            }
            try {
                // 检查并尝试创建日志目录
                File parent = this.fBaseLogFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                // 生成新文件名
                StringBuffer sb = new StringBuffer(256);
                sb.append(getLogfile());
                sb.append(".");
                SimpleDateFormat sdfDayFormat = new SimpleDateFormat("yyyyMMdd");
                sdfDayFormat.format(new Date(), sb, this.fpNumberPosition);
                this.fLogFile = new File(sb.toString());
                this.lNextDayBegin = getNextDayBegin(); // 设置下次改名的时间
                // 尝试删除过期日志文件
                removeExpiredLogs();
            } catch (Exception e) {
                e.printStackTrace();
                this.fLogFile = null;
                is_ok = false;
            }
        }

        // 检查文件大小是否超过设定值
        try {
            if (this.fLogFile.length() >= this.lMaxFileSize - this.iBuffSize) {
                // new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
                StringBuffer sb = new StringBuffer(256);
                sb.append(getLogfile());
                sb.append(".");
                SimpleDateFormat sdfDayTimeFormat = new SimpleDateFormat(
                        "yyyyMMddHHmmss");
                sdfDayTimeFormat.format(new Date(), sb, this.fpNumberPosition);
                this.fLogFile.renameTo(new File(sb.toString()));
                isChangeFile = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 为了减少同步文件对写日志线程的影响，需要先更换日志buffer
        Vector<String> v = null;
        // 转换buffer
        synchronizedPutLog.lock();
        try {
            v = this.vCurMsgs;
            this.vCurMsgs = this.vBackMsgs;
            this.vBackMsgs = v;
        } finally {
            synchronizedPutLog.unlock();
        }

        // 写文件
        if (this.fLogFile != null) {
            // 判断文件是否存在，不存在则写日志
            try {
                if (!this.fLogFile.isFile()) {
                    isChangeFile = true;
                }
            } catch (Throwable t) {
            }

            try {
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(this.fLogFile, true),
                        this.iBuffSize);

                for (int k = v.size(), i = 0; i < k; i++) {
                    String s = v.set(i, null);
                    out.write(s.getBytes(StandardCharsets.UTF_8));
                    out.write('\n');
                }
                out.flush();
                out.close();
            } catch (IOException ioe) {
                this.fLogFile = null;
                is_ok = false;
//                System.out.println("Write '" + this.getLogfile() + "' error: "
//                        + ioe.getMessage());
//                ioe.printStackTrace(System.out);
            } catch (Exception e) {
                is_ok = false;
            }
        }
        try {
            v.clear(); // 清理日志缓存，释放内存
        } catch (Exception e) {
        }
        return is_ok;
    }

    /**
     * 实现线程接口
     */
    public synchronized void run() {
        while (!this.bClosed) {
            try {
                wait(iLogWaitingPerWrite);
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
                break;
            } catch (IllegalMonitorStateException ime) {
                ime.printStackTrace();
                break;
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                break;
            } catch (Exception e) {
//                System.err.println("Log's background thread exception at '"
//                        + this.toString() + "' " + e.getMessage());
//                e.printStackTrace(System.out);
            }
            this.writeLogsToFile();
        }
        // 关闭日志类
        this.close();
    }

    public boolean isInfo() {
        return this.iCurLogLevel >= iLogLevelInfo;
    }

    public boolean isDebug() {
        return this.iCurLogLevel >= iLogLevelDebug;
    }

    public int getLogLevel() {
        return this.iCurLogLevel;
    }

    public void setLogLevel(int level) {
        if (level >= 0) {
            this.iCurLogLevel = level;
        }
    }

    public void setLogLevel(String loglevel) {
        int level = -1;

        if ("Dump".equalsIgnoreCase(loglevel)) {
            level = iLogLevelDump;
        } else if ("Debug".equalsIgnoreCase(loglevel)) {
            level = iLogLevelDebug;
        } else if ("Info".equalsIgnoreCase(loglevel) || "Information".equalsIgnoreCase(loglevel)) {
            level = iLogLevelInfo;
        } else if ("Warn".equalsIgnoreCase(loglevel) || "Warning".equalsIgnoreCase(loglevel)) {
            level = iLogLevelWarning;
        } else if ("Error".equalsIgnoreCase(loglevel)) {
            level = iLogLevelError;
        } else if ("Nothing".equalsIgnoreCase(loglevel)) {
            level = iLogLevelNothing;
        }
        if (level >= iLogLevelNothing && level <= iLogLevelDump) {
            this.iCurLogLevel = level;
        }
    }

    /**
     * @return String 返回日志文件名
     */
    public String toString() {
        return this.fBaseLogFile != null ? this.fBaseLogFile.getName() : null;
    }
}
