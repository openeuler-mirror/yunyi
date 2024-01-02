package com.tongtech.proxy.core.protocol;

import java.nio.charset.StandardCharsets;

import static com.tongtech.proxy.core.StaticContent.hashBytes;

public final class RdsString {
    private final static RdsString NULLSTRING = new RdsString(null);
    private final static byte[] ZEROLENBYTES = new byte[0];
    private final static RdsString ZEROLENSTRING = new RdsString(ZEROLENBYTES);

    private final byte[] message;

    private final int hash;

    public static RdsString getString(byte[] b) {
        if (b == null) {
            return NULLSTRING;
        } else if (b.length == 0) {
            return ZEROLENSTRING;
        } else {
            return new RdsString(b);
        }
    }

    private RdsString(byte[] b) {
        this.message = b;
        this.hash = hashBytes(b);
    }

    public byte[] message() {
        return this.message;
    }

    public boolean matches(RdsString parten, boolean nocase) {
        return matches(parten.message, nocase);
    }

    public boolean matches(byte[] parten, boolean nocase) {
        if (parten != null && this.message != null) {
            return bytesMatchlen(parten, 0, this.message, 0, false);
        }
        return false;
    }

    @Override
    public String toString() {
        if (this.message == null) {
            return "null";
        } else if (this.message.length == 0) {
            return "";
        } else {
            return new String(message, StandardCharsets.UTF_8);
        }
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RdsString) {
            RdsString str = (RdsString) o;
            if (message == null && str.message == null) {
                return true;
            } else if (message == null || str.message == null) {
                return false;
            } else {
                int n = message.length;
                if (n == str.message.length) {
                    byte[] v1 = message;
                    byte[] v2 = str.message;
                    int i = 0;
                    while (n-- != 0) {
                        if (v1[i] != v2[i])
                            return false;
                        i++;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static int tolower(int n) {
        if (n >= 'A' && n <= 'Z') {
            n = 'a' + n - 'A';
        }
        return n;
    }

    /* Glob-style pattern matching. */
    public static boolean bytesMatchlen(byte[] pattern, int patOffset,
                                        byte[] string, int strOffset, boolean nocase) {
        int patternLen = pattern.length - patOffset;
        int stringLen = string.length - strOffset;

        try {
            while (patternLen > 0 && stringLen > 0) {
                switch (pattern[patOffset]) {
                    case '*':
                        while (patternLen > 1 && pattern[patOffset + 1] == '*') {
                            patOffset++;
                            patternLen--;
                        }
                        if (patternLen == 1) {
                            return true; /* match */
                        }
                        while (stringLen > 0) {
                            if (bytesMatchlen(pattern, patOffset + 1,
                                    string, strOffset, nocase)) {
                                return true; /* match */
                            }
                            strOffset++;
                            stringLen--;
                        }
                        return false; /* no match */
//                break;
                    case '?':
                        if (stringLen == 0) {
                            return false; /* no match */
                        }
                        strOffset++;
                        stringLen--;
                        break;
                    case '[': {
                        boolean not, match;

                        patOffset++;
                        patternLen--;
                        not = pattern[patOffset] == '^';
                        if (not) {
                            patOffset++;
                            patternLen--;
                        }
                        match = false;
                        while (true) {
                            if (pattern[patOffset] == '\\' && patternLen >= 2) {
                                patOffset++;
                                patternLen--;
                                if (pattern[patOffset] == string[strOffset])
                                    match = true;
                            } else if (pattern[patOffset] == ']') {
                                break;
                            } else if (patternLen == 0) {
                                patOffset--;
                                patternLen++;
                                break;
                            } else if (pattern[patOffset + 1] == '-' && patternLen >= 3) {
                                int start = pattern[patOffset];
                                int end = pattern[patOffset + 2];
                                int c = string[0];
                                if (start > end) {
                                    int t = start;
                                    start = end;
                                    end = t;
                                }
                                if (nocase) {
                                    start = tolower(start);
                                    end = tolower(end);
                                    c = tolower(c);
                                }
                                patOffset += 2;
                                patternLen -= 2;
                                if (c >= start && c <= end)
                                    match = true;
                            } else {
                                if (!nocase) {
                                    if (pattern[0] == string[strOffset])
                                        match = true;
                                } else {
                                    if (tolower((int) pattern[patOffset]) == tolower((int) string[strOffset]))
                                        match = true;
                                }
                            }
                            patOffset++;
                            patternLen--;
                        }
                        if (not)
                            match = !match;
                        if (!match)
                            return false; /* no match */
                        strOffset++;
                        stringLen--;
                        break;
                    }
                    case '\\':
                        if (patternLen >= 2) {
                            patOffset++;
                            patternLen--;
                        }
                        /* fall through */
                    default:
                        if (!nocase) {
                            if (pattern[patOffset] != string[strOffset])
                                return false; /* no match */
                        } else {
                            if (tolower((int) pattern[patOffset]) != tolower((int) string[strOffset]))
                                return false; /* no match */
                        }
                        strOffset++;
                        stringLen--;
                        break;
                }
                patOffset++;
                patternLen--;
                if (stringLen == 0) {
                    while (patternLen > 0 && pattern[patOffset] == '*') {
                        patOffset++;
                        patternLen--;
                    }
                    break;
                }
            }
            if (patternLen == 0 && stringLen == 0) {
                return true;
            }
        } catch (Throwable t) {
        }
        return false;
    }

    public static byte[] substr(byte[] src, int begin, int end) {
        if (src == null) {
            throw new NullPointerException("src is null");
        }

        if (begin < 0 || begin >= src.length) {
            throw new IndexOutOfBoundsException("begin = " + begin);
        }
        if (end <= 0 || end > src.length || begin > end) {
            throw new IndexOutOfBoundsException("end = " + end);
        }

        if (begin == end) {
            return new byte[0];
        }

        byte[] data = new byte[end - begin];
        System.arraycopy(src, begin, data, 0, end - begin);

        return data;
    }

    public static byte[] append(byte[] head, byte[] tail) {
        if (tail == null) {
            return head;
        }
        if (head == null) {
            return tail;
        }
        if (tail.length == 0) {
            return head;
        }
        if (head.length == 0) {
            return tail;
        }

        byte[] data = new byte[head.length + tail.length];
        System.arraycopy(head, 0, data, 0, head.length);
        System.arraycopy(tail, 0, data, head.length, tail.length);

        return data;
    }

//    public byte[] getMessage() {
//        return message;
//    }
//
//    public int length() {
//        return this.message != null ? this.message.length : 0;
//    }

//    public long getLong() {
//        return getLong(message);
//    }

//    public double getDouble() {
//        return getDouble(message);
//    }

    public static int parseInt(byte[] bs) {
        return (int) parseLong(bs);
    }

    public static long parseLong(byte[] bs) {
        if (bs == null || bs.length == 0) {
            throw new NumberFormatException("input is null");
        }

        long l = 0;
        try {
            boolean is_negative = false;
            for (int i = 0; i < bs.length; ++i) {
                int b = bs[i];
                if (b == '_' || b == ' ') {
                    // do nothing
                } else if (i == 0 && b == '-') {
                    is_negative = true;
                } else if (i == 0 && b == '+') {
                    is_negative = false;
                } else if (b >= '0' && b <= '9') {
                    l = l * 10 + b - '0';
                } else {
                    throw new NumberFormatException("");
                }
            }
            return is_negative ? -l : l;
        } catch (Throwable t) {
        }
        throw new NumberFormatException(new String(bs, StandardCharsets.UTF_8));
    }

    public static double parseDouble(byte[] b) {
        if (b == null || b.length == 0) {
            return 0d;
        }
        String str = new String(b).trim();
        if (str.equalsIgnoreCase("-INF")) {
            return -Double.MAX_VALUE;
        } else if (str.equalsIgnoreCase("+INF")) {
            return Double.MAX_VALUE;
        } else {
            return Double.parseDouble(str);
        }
    }

    public static String toString(byte[] b) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < b.length; ++i) {
            char c = (char) ((b[i]) & 0xff);
            if (c > 31 && c < 128) {
                buf.append(c);
            } else {
                buf.append(String.format("\\x%02x", c));
            }
        }
        return buf.toString();
    }

    public static byte[] toLowerCase(byte[] b) {
        if (b != null && b.length > 0) {
            for (int i = 0; i < b.length; ++i) {
                if (b[i] >= 'A' && b[i] <= 'Z') {
                    b[i] = (byte) (b[i] + 'a' - 'A');
                }
            }
        }
        return b;
    }
}
