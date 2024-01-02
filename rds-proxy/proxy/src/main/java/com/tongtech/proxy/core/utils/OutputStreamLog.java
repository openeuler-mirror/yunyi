package com.tongtech.proxy.core.utils;

import java.io.*;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>
 * Title: FuWuBao
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
 * @author LiGang
 * @version 1.0
 */

public class OutputStreamLog extends OutputStream implements Runnable {
    private final static int BUFFER_SIZE = 2048;

    private File fLogFile;

    private File fBaseLogFile;

    private final int iBuffSize;

    private final SimpleDateFormat sdfLog = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ");

    private long lNextDayBegin = 0; // 下次切换日志的时间

    private int iTimeZoneOffset = TimeZone.getDefault().getRawOffset(); // 程序运行环境的时区偏差，单位毫秒

    private FieldPosition fpNumberPosition = new FieldPosition(0); //

    private final long lMaxFileSize;

    private Vector<byte[]> vCurMsgs = null; // 当前日志内容列表

    private Vector<byte[]> vBackMsgs = null; // 备份日志内容列表

    private final int iMaxLogList = 50000; // 未实际写入文件的日志的最大条数

    private final int iLogListEmptyWarn = iMaxLogList / 5; // 未实际写入文件的日志的最小条数

    private final int iLogListFullWarn = iMaxLogList - iLogListEmptyWarn; // 未实际写入文件的日志的最小条数

    private final ReentrantLock synchronizedPutLog = new ReentrantLock(); // 写文件同步锁

    private boolean bClosed = false; // 日志关闭标志

    // 日志写文件的最大间隔时间的毫秒数
    private static final int MAX_WAITING = 1000;

    // 日志写文件的最小间隔时间的毫秒数
    private static final int MIN_WAITING = 50;

    // 日志写文件的实际间隔时间,初始为MAX_WAITING
    private int iLogWaitingPerWrite = MAX_WAITING;

    // 日志缓冲buf大小
    private final byte[] Buffer = new byte[BUFFER_SIZE];

    private int BufferOffset = 0;

    public OutputStreamLog(File file) {
        this(file, (long) 1024 * 1024 * 1024);
    }

    public OutputStreamLog(File file, long maxsize) {
        this(file, (int) 8192, maxsize);
    }

    public OutputStreamLog(File file, int bufsize, long maxsize) {

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
//

        // 新建文件的buffer大小
        if (bufsize < 1024) {
            bufsize = 1024;
        }
        this.iBuffSize = bufsize;

        // 不带日期的日志文件
        this.fBaseLogFile = file;

        this.lMaxFileSize = maxsize;

        // 计算第2天凌晨
        this.lNextDayBegin = getNextDayBegin();
        // System.out.println(this.sdfDayFormat.format(new
        // Date(this.lNextCvtTime-1)));

        // 创建日志缓冲列表类
        int i = this.iMaxLogList / 50 > 100 ? this.iMaxLogList / 50 : 100;
        this.vBackMsgs = new Vector<byte[]>(i, i);
        this.vCurMsgs = new Vector<byte[]>(i, i);

        // 启动写日志后台线程
        Thread th = new Thread(this);
        th.setDaemon(true);
        th.start();
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
        return this.fBaseLogFile.getAbsolutePath();
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
    public synchronized void close() {
        if (!this.bClosed) {
            try {
                this.bClosed = true;
                notifyAll();
            } catch (Exception e) {
                // e.printStackTrace();
            }
            System.out.println("OutputStreamLog::close() File '"
                    + this.fBaseLogFile.getName() + "' closed.");
        }
    }

    /**
     * 标准日志输出，该函数会在输出日志前加时间和“INFO”字样
     * <p>
     * <p>
     * /** 日志输出基础函数。该函数只将日志写到buffer列表里， 并不实际写文件（实际写文件需要调用writeLogsToFile方法）。
     *
     * @return boolean 日志输出成功返回true，否则返回false。
     */
    public synchronized void write(int b) {
        byte c = (byte) b;
        if (BufferOffset == BUFFER_SIZE || c == '\n') {
            if (BufferOffset > 0) {
                log(Buffer, 0, BufferOffset);
                BufferOffset = 0;
            }
        } else {
            Buffer[BufferOffset++] = c;
        }
    }

    public void write(byte[] log) {
        write(log, 0, log.length);
    }

    public synchronized void write(byte[] b, int offset, int len) {
        for (int i = 0; i < len; i++) {
            write(b[i + offset]);
        }
    }

    private void log(byte[] log, int offset, int len) {
        synchronizedPutLog.lock();
        try {
            if (this.vCurMsgs.size() < this.iMaxLogList && !this.bClosed
                    && len > 0) {
                StringBuffer msg = new StringBuffer(40);
                this.sdfLog.format(new Date(), msg, this.fpNumberPosition);
                byte[] title = msg.toString().getBytes();
                byte[] msgs = new byte[title.length + len];
                System.arraycopy(title, 0, msgs, 0, title.length);
                System.arraycopy(log, offset, msgs, title.length, len);
                this.vCurMsgs.addElement(msgs);
            }
        } catch (Exception e) {
        } finally {
            synchronizedPutLog.unlock();
        }
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
     * 刷新缓冲，将buffer中的日志实际写到文件里。为了减少写文件时对其他线程写日志的影响，
     * 程序创建了2个bugger，开始写文件前将空闲buffer和当前buffer互换，实际写文件操作后台并行执行。
     *
     * @return boolean 是否成功
     */
    private boolean writeLogsToFile() {
        boolean is_ok = true;
        long currenttime = System.currentTimeMillis();

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
            }
        } catch (Exception e) {
        }

        // 为了减少同步文件对写日志线程的影响，需要先更换日志buffer
        Vector<byte[]> v = null;
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
            try {
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(this.fLogFile, true),
                        this.iBuffSize);
                for (int k = v.size(), i = 0; i < k; i++) {
                    byte[] s = v.set(i, null);
                    out.write(s);
                    out.write('\n');
                }
                out.flush();
                out.close();
            } catch (IOException ioe) {
                this.fLogFile = null;
                is_ok = false;
                // System.out.println("Write '" + this.getLogfile() + "' error:
                // "
                // + ioe.getMessage());
                // ioe.printStackTrace(System.out);
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
                // System.err.println("Log's backgroud thread exception at '"
                // + this.toString() + "' " + e.getMessage());
                // e.printStackTrace(System.out);
            }
            this.writeLogsToFile();
        }
    }

    public synchronized void flush() {
        notifyAll();
    }

    /**
     * Called by the garbage collector
     */
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }

    /**
     * @return String 返回日志文件名
     */
    public String toString() {
        return this.fBaseLogFile.getName();
    }
}
