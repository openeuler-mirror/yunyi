package com.tongtech.proxy.core.cli;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
    private final static byte HEAD_ARRAY = '*';
    private final static byte HEAD_STRINGBLOCK = '$';
    private final static byte HEAD_INTEGER = ':';
    private final static byte HEAD_FLOAT = '#';
    private final static byte HEAD_BYTES = '[';
    private final static byte HEAD_SIMPLESTRING = '+';
    private final static byte HEAD_ERROR = '-';
    private final static String ENDOFLINE = "\r\n";

    private final static String NULLARRAY = "*-1\r\n";
    private final static String NULLSTRING = "$-1\r\n";

    public static byte[] encode(Object data) {
        StringBuilder buf = new StringBuilder(128);
        // 空包
        if (data == null) {
            buf.append(NULLSTRING);
        } else {
            if (data instanceof List) {
                List v = (List) data;
                buf.append((char) HEAD_ARRAY);
                buf.append(v.size());
                buf.append(ENDOFLINE);
                for (Object o : v) {
                    writeObject(buf, o);
                }
            } else if (data instanceof String) {
                String message = data.toString();
                if (message.charAt(0) == HEAD_ERROR
                        || message.charAt(0) == HEAD_SIMPLESTRING) {
                    buf.append(message);
                } else {
                    buf.append((char) HEAD_SIMPLESTRING);
                    buf.append(message);
                }
                buf.append(ENDOFLINE);
            } else {
                writeObject(buf, data);
            }
        }
        return buf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void writeObject(StringBuilder buf, Object o) {
        if (o == null) {
            buf.append(NULLSTRING);
        } else if (o instanceof Integer) {
            buf.append((char) HEAD_INTEGER);
            buf.append((Integer) o);
            buf.append(ENDOFLINE);
        } else if (o instanceof Long) {
            buf.append((char) HEAD_INTEGER);
            buf.append((Long) o);
            buf.append(ENDOFLINE);
        } else if (o instanceof Float) {
            buf.append((char) HEAD_FLOAT);
            buf.append((Float) o);
            buf.append(ENDOFLINE);
        } else if (o instanceof Double) {
            buf.append((char) HEAD_FLOAT);
            buf.append((Double) o);
            buf.append(ENDOFLINE);
        } else {
            writeString(buf, o.toString());
        }
    }

    private static void writeString(StringBuilder buf, String msg) {
        if (msg == null) {
            buf.append(NULLSTRING);
        } else {
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            buf.append((char) HEAD_STRINGBLOCK);
            buf.append(data.length);
            buf.append(ENDOFLINE);
            if (data.length > 0) {
                buf.append(msg);
            }
            buf.append(ENDOFLINE);
        }
    }

    public static Object read(PackageInputStream is) throws ConnectException {
        return process(is);
    }

    private static Object process(PackageInputStream is) throws ConnectException {
        final byte b = is.readByte();
        switch (b) {
            case HEAD_SIMPLESTRING:
                return processStatusCodeReply(is);
            case HEAD_STRINGBLOCK:
                return processBulkReply(is);
            case HEAD_ARRAY:
                return processMultiBulkReply(is);
            case HEAD_INTEGER:
                return processInteger(is);
            case HEAD_FLOAT:
                return processFloat(is);
            case HEAD_BYTES:
                return processBytes(is);
            case HEAD_ERROR:
                return processError(is);
            default:
                throw new ConnectException("Unknown reply: " + (char) b);
        }
    }

    private static SimpleString processStatusCodeReply(final PackageInputStream is) throws ConnectException {
        return new SimpleString(new String(is.readLineBytes(), StandardCharsets.UTF_8));
    }

    private static BulkString processBulkReply(final PackageInputStream is) throws ConnectException {
        byte[] data = processBytes(is);
        if (data != null) {
            return new BulkString(data);
        }
        return null;
    }

    private static byte[] processBytes(final PackageInputStream is) throws ConnectException {
        final int len = is.readIntCrLf();
        if (len == -1) {
            return null;
        }

        final byte[] read = new byte[len];
        int offset = 0;
        while (offset < len) {
            final int size = is.read(read, offset, (len - offset));
            if (size == -1) throw new ConnectException(
                    "It seems like server has closed the connection.");
            offset += size;
        }

        // read 2 more bytes for the command delimiter
        is.readByte();
        is.readByte();

        return read;
    }

    private static Long processInteger(PackageInputStream is) throws ConnectException {
        return is.readLongCrLf();
    }

    private static Float processFloat(PackageInputStream is) throws ConnectException {
        Float f = null;
        try {
            f = Float.intBitsToFloat((int) is.readLongCrLf());
        } catch (Exception e) {
        }
        return f;
    }

    private static ArrayList<Object> processMultiBulkReply(PackageInputStream is) throws ConnectException {
        final int num = is.readIntCrLf();
        if (num == -1) {
            return null;
        }
        final ArrayList<Object> ret = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            try {
                ret.add(process(is));
            } catch (ConnectException e) {
                ret.add(e);
            }
        }
        return ret;
    }

    private static ErrorString processError(final PackageInputStream is) throws ConnectException {
        String message = is.readLine();
        return new ErrorString(message);
        // TODO: I'm not sure if this is the best way to do this.
        // Maybe Read only first 5 bytes instead?
//        if (message.startsWith(MOVED_PREFIX)) {
//            String[] movedInfo = parseTargetHostAndSlot(message);
//            throw new ConnectException(message, new HostAndPort(movedInfo[1],
//                    Integer.parseInt(movedInfo[2])), Integer.parseInt(movedInfo[0]));
//        } else if (message.startsWith(ASK_PREFIX)) {
//            String[] askInfo = parseTargetHostAndSlot(message);
//            throw new ConnectException(message, new HostAndPort(askInfo[1],
//                    Integer.parseInt(askInfo[2])), Integer.parseInt(askInfo[0]));
//        } else if (message.startsWith(CLUSTERDOWN_PREFIX)) {
//            throw new ConnectException(message);
//        } else if (message.startsWith(BUSY_PREFIX)) {
//            throw new ConnectException(message);
//        } else if (message.startsWith(NOSCRIPT_PREFIX)) {
//            throw new ConnectException(message);
//        } else if (message.startsWith(WRONGPASS_PREFIX)) {
//            throw new ConnectException(message);
//        } else if (message.startsWith(NOPERM_PREFIX)) {
//            throw new ConnectException(message);
//        }
//        throw new ConnectException(message);
    }

}
