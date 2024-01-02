package com.tongtech.proxy.core.server.io;

public class CodecContext {
    public static final Object NULL_ARRAY_OBJECT = new Object();
    public static final Object NULL_STRING_OBJECT = new Object();
    public static final Object ZERO_STRING_OBJECT = new Object();

    public final static byte HEAD_LIST = '*';
    public final static byte HEAD_STRINGBLOB = '$';
    public final static byte HEAD_INTEGER = ':';
    public final static byte HEAD_FLOAT = ',';
    public final static byte HEAD_BYTES = '[';
    public final static byte HEAD_SIMPLESTRING = '+';
    public final static byte HEAD_ERROR = '-';
    public final static byte HEAD_BOOLEAN = '#';
    public final static byte HEAD_ERRORBLOB = '!';
    public final static byte HEAD_VERBATIMSTRING = '=';
    public final static byte HEAD_BIGINTEGER = '(';
    public final static byte HEAD_MAP = '%';
    public final static byte HEAD_SET = '~';
    public final static byte HEAD_PROPERTIES = '|';
    public final static byte HEAD_PUBSUB = '>';

    public final static byte[] ENDOFLINE = "\r\n".getBytes();

    public final static byte[] NULLARRAY = "*-1\r\n".getBytes();
    public final static byte[] ZEROARRAY = "*0\r\n".getBytes();
    public final static byte[] NULLSTRING = "$-1\r\n".getBytes();
    public final static byte[] ZEROSTRING = "$0\r\n\r\n".getBytes();
}
