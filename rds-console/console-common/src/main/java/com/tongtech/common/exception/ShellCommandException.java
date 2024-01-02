package com.tongtech.common.exception;

/**
 * 在执行本地命令行时的异常
 *
 * @author Zhang Chenlong
 */
public class ShellCommandException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public ShellCommandException() {
        super();
    }

    public ShellCommandException(String message) {
        super(message);
    }

    public ShellCommandException(Throwable cause) {
        super(cause);
    }

    public ShellCommandException(String message, Throwable e)
    {
        super(message, e);
    }

}
