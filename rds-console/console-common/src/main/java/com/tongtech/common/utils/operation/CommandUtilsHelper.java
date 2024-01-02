package com.tongtech.common.utils.operation;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


/**
 * 本地命令行执行工具,用于执行如DOS command和Linux shell命令
 *
 * @author Charlie Zhange
 *
 */
public class CommandUtilsHelper {

	public static volatile long MAX_WAITING_MILLISECOND = 1000;

	private static Logger log = LoggerFactory.getLogger(CommandUtilsHelper.class);

	private Runtime runtime = null;

	private OSType ostype = null;

	private String lastOut;

	private String lastErr;

	private volatile int grabberCount = -1;


	public synchronized OSType getOSType() throws IOException {

		if(ostype == null) {
			String osname = System.getProperty("os.name").toLowerCase();
			if(osname.indexOf("windows") >= 0) {
				ostype = OSType.WINDOWS;
			}
			else if(osname.indexOf("linux") >= 0) {
				ostype = OSType.LINUX;
			}
			else if(osname.indexOf("unix") >= 0) {
				ostype = OSType.UNIX;
			}
			else if(osname.indexOf("mac") >= 0) {
				ostype = OSType.MAC;
			}
			else {
				throw new IOException("Failed to get match OSType! os.name=" + osname);
			}
		}

		return ostype;
	}

	/**
	 * 直接执行一个系统命令行，命令执行完成后该函数才返回
	 * 如果发生错误自动记录到日志中
	 *
	 * @param commandLine
	 * @return Process.waitFor() code. The exit value of the process. By convention,
     *             <code>0</code> indicates normal termination.
	 */
	public synchronized int execute(String commandLine, String outputCharset) throws IOException, InterruptedException{
		if(runtime == null) {
			runtime = Runtime.getRuntime();
		}

		if (log.isDebugEnabled()) {
			log.debug("[" + getOSType() + "]COMMAND>" + commandLine);
		}

		String[] cmds = new String[3];
		switch(getOSType()) {
		case UNIX:
		case LINUX:
		case MAC:
			cmds[0] = "/bin/sh";
			cmds[1] = "-c"; //this options means: Carries out My_Command and then terminates
			break;
		case WINDOWS:
			cmds[0] = "cmd.exe";
			cmds[1] = "/C"; //this options means: Carries out My_Command and then terminates
			break;
		default:
			log.error("CommandUtils Not support OS System: " + System.getProperty("os.name"));
			assert false; //should never reach here
			break;
		}

		cmds[2] = commandLine;

		Process proc = runtime.exec(cmds);

		InputStream stderr = proc.getErrorStream();
		InputStream stdout = proc.getInputStream();
		grabberCount = 2;
		StreamGrabber errGrabber = new StreamGrabber(stderr, outputCharset, true);
		StreamGrabber outGrabber = new StreamGrabber(stdout, outputCharset, false);

		//需要等待 sgErr 和 sgOut 线程停止。最长等待 MAX_WAITING_MILLISECOND设定的毫秒数
		errGrabber.join(MAX_WAITING_MILLISECOND);
		outGrabber.join(MAX_WAITING_MILLISECOND);

		return proc.waitFor();
	}

	public synchronized String getLastOut() {
		return lastOut;
	}


	public synchronized String getLastErr() {
		return lastErr;
	}


	class StreamGrabber extends Thread {

		private InputStream input;

		private boolean isError;

		private String outputCharset;

		public StreamGrabber(InputStream input, String outputCharset, boolean isError) {
			this.input = input;
			this.isError = isError;
			this.outputCharset = outputCharset;
			this.setDaemon(true);
			this.start();
		}

		public void run() {
			try {
				String outString = IOUtils.toString(input, getCharset());
				if(isError) {
					lastErr = outString;
					if(outString != null && outString.trim().length() > 0 && log.isDebugEnabled()) {
						log.debug("COMMAND ERR[" + getCharset() + "]>" + outString);
					}
				}
				else {
					lastOut = outString;
					if(outString != null && outString.trim().length() > 0 && log.isDebugEnabled()) {
						log.debug("COMMAND OUT[" + getCharset() + "]>" + outString);
					}
				}

				grabberCount --;
			} catch (Throwable ioe) {
				log.error(ioe.getMessage(), ioe);
			}
		}

		private String getCharset() {
			if(outputCharset != null) {
				return outputCharset;
			}
			else {
				return Charset.defaultCharset().name();
			}
		}
	}



}


