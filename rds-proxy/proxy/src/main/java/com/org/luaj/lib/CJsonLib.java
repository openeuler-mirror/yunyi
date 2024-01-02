package com.org.luaj.lib;

import com.org.luaj.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;


public class CJsonLib extends OneArgFunction {
//    private static final byte[][] char2escape = {
//            "\\u0000".getBytes(), "\\u0001".getBytes(), "\\u0002".getBytes(), "\\u0003".getBytes(),
//            "\\u0004".getBytes(), "\\u0005".getBytes(), "\\u0006".getBytes(), "\\u0007".getBytes(),
//            "\\b".getBytes(), "\\t".getBytes(), "\\n".getBytes(), "\\u000b.getBytes()".getBytes(),
//            "\\f".getBytes(), "\\r".getBytes(), "\\u000e".getBytes(), "\\u000f".getBytes(),
//            "\\u0010".getBytes(), "\\u0011".getBytes(), "\\u0012".getBytes(), "\\u0013".getBytes(),
//            "\\u0014".getBytes(), "\\u0015".getBytes(), "\\u0016".getBytes(), "\\u0017".getBytes(),
//            "\\u0018".getBytes(), "\\u0019".getBytes(), "\\u001a".getBytes(), "\\u001b".getBytes(),
//            "\\u001c".getBytes(), "\\u001d".getBytes(), "\\u001e".getBytes(), "\\u001f".getBytes(),
//            null, null, "\\\"".getBytes(), null, null, null, null, null,
//            null, null, null, null, null, null, null, "\\/".getBytes(),
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, "\\\\".getBytes(), null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, "\\u007f".getBytes(),
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//            null, null, null, null, null, null, null, null,
//    };


    private static final boolean DECODE_INVALID_NUMBERS = true;
    private static final int ENCODE_MAX_DEPTH = 100;
    private static final int DECODE_MAX_DEPTH = 100;

    private static final byte[][] char2escape = new byte[256][];

    private enum DecodeToken {
        T_OBJ_BEGIN,
        T_OBJ_END,
        T_ARR_BEGIN,
        T_ARR_END,
        T_STRING,
        T_NUMBER,
        T_BOOLEAN,
        T_NULL,
        T_COLON,
        T_COMMA,
        T_END,
        T_WHITESPACE,
        T_ERROR,
        T_UNKNOWN
    }

    private static final DecodeToken[] ch2token = new DecodeToken[256];

    private static final char[] escape2char = new char[256];

    static {

        for (int i = 0; i < char2escape.length; ++i) {
            if (i == '\b') {
                char2escape[i] = "\\b".getBytes();
            } else if (i == '\t') {
                char2escape[i] = "\\t".getBytes();
            } else if (i == '\n') {
                char2escape[i] = "\\n".getBytes();
            } else if (i == '\f') {
                char2escape[i] = "\\f".getBytes();
            } else if (i == '\r') {
                char2escape[i] = "\\r".getBytes();
            } else if (i == '/') {
                char2escape[i] = "\\/".getBytes();
            } else if (i == '\\') {
                char2escape[i] = "\\\\".getBytes();
            } else if (i <= 0x1f || i == 0x7f) {
                char2escape[i] = String.format("\\u00%02x", i).getBytes();
            } else {
                char2escape[i] = null;
            }
        }

        for (int i = 0; i < ch2token.length; ++i) {
            ch2token[i] = DecodeToken.T_ERROR;
        }
        /* Set tokens that require no further processing */
        ch2token['{'] = DecodeToken.T_OBJ_BEGIN;
        ch2token['}'] = DecodeToken.T_OBJ_END;
        ch2token['['] = DecodeToken.T_ARR_BEGIN;
        ch2token[']'] = DecodeToken.T_ARR_END;
        ch2token[','] = DecodeToken.T_COMMA;
        ch2token[':'] = DecodeToken.T_COLON;
        ch2token['\0'] = DecodeToken.T_END;
        ch2token[' '] = DecodeToken.T_WHITESPACE;
        ch2token['\t'] = DecodeToken.T_WHITESPACE;
        ch2token['\n'] = DecodeToken.T_WHITESPACE;
        ch2token['\r'] = DecodeToken.T_WHITESPACE;
        /* Update characters that require further processing */
        ch2token['f'] = DecodeToken.T_UNKNOWN;     /* false? */
        ch2token['i'] = DecodeToken.T_UNKNOWN;     /* inf, ininity? */
        ch2token['I'] = DecodeToken.T_UNKNOWN;
        ch2token['n'] = DecodeToken.T_UNKNOWN;     /* null, nan? */
        ch2token['N'] = DecodeToken.T_UNKNOWN;
        ch2token['t'] = DecodeToken.T_UNKNOWN;     /* true? */
        ch2token['"'] = DecodeToken.T_UNKNOWN;     /* string? */
        ch2token['+'] = DecodeToken.T_UNKNOWN;     /* number? */
        ch2token['-'] = DecodeToken.T_UNKNOWN;
        ch2token['0'] = DecodeToken.T_UNKNOWN;
        ch2token['1'] = DecodeToken.T_UNKNOWN;
        ch2token['2'] = DecodeToken.T_UNKNOWN;
        ch2token['3'] = DecodeToken.T_UNKNOWN;
        ch2token['4'] = DecodeToken.T_UNKNOWN;
        ch2token['5'] = DecodeToken.T_UNKNOWN;
        ch2token['6'] = DecodeToken.T_UNKNOWN;
        ch2token['7'] = DecodeToken.T_UNKNOWN;
        ch2token['8'] = DecodeToken.T_UNKNOWN;
        ch2token['9'] = DecodeToken.T_UNKNOWN;

        for (int i = 0; i < 256; i++) {
            escape2char[i] = 0;          /* String error */
        }
        escape2char['"'] = '"';
        escape2char['\\'] = '\\';
        escape2char['/'] = '/';
        escape2char['b'] = '\b';
        escape2char['t'] = '\t';
        escape2char['n'] = '\n';
        escape2char['f'] = '\f';
        escape2char['r'] = '\r';
        escape2char['u'] = 'u';          /* Unicode parsing required */
    }

    @Override
    public LuaValue call(LuaValue arg) {
        LuaTable t = new LuaTable();
        bind(t, CJsonLib0.class, new String[]{
                "new"});
        bind(t, CJsonLib1.class, new String[]{
                "encode", "decode"});
        env.set("cjson", t);
        PackageLib.instance.LOADED.set("cjson", t);
        return t;
    }

    static final class CJsonLib0 extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            switch (opcode) {
                case 0:
                    LuaTable t = new LuaTable();
                    bind(t, CJsonLib0.class, new String[]{
                            "new"});
                    bind(t, CJsonLib1.class, new String[]{
                            "encode", "decode"});
                    return t;
            }
            return LuaValue.NIL;
        }
    }

    static final class CJsonLib1 extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg1) {
            switch (opcode) {
                case 0: // encode
                    return encode(arg1);
                case 1: // decode
                    return decode(arg1.checkstring());
            }
            return LuaValue.NIL;
        }
    }

    private static LuaString encode(LuaValue arg) {
        StringBuilder buf = new StringBuilder(128);
        encode(0, arg, buf);
        return LuaString.valueOf(buf.toString());
    }

    private static void encode(int current_depth, LuaValue arg, StringBuilder buf) {
        current_depth++;
        if (current_depth > ENCODE_MAX_DEPTH) {
            throw new LuaError("Cannot serialise, excessive nesting (" + current_depth + ")");
        }

        if (arg instanceof LuaNil) {
            buf.append("null");
        } else if (arg instanceof LuaBoolean) {
            if (arg.toboolean()) {
                buf.append("true");
            } else {
                buf.append("false");
            }
        } else if (arg instanceof LuaString) {
            LuaString str = (LuaString) arg;
            buf.append("\"");
            buf.append(escape(str.m_bytes, str.m_offset, str.m_length));
            buf.append("\"");
        } else if (arg instanceof LuaLong) {
            buf.append(arg.checklong());
        } else if (arg instanceof LuaDouble) {
            buf.append(arg.checkdouble());
        } else if (arg instanceof LuaTable) {
            LuaTable table = (LuaTable) arg;
            boolean is_map = table.isMap();
            if (is_map) {
                buf.append('{');
            } else {
                buf.append('[');
            }
            ForeachTable foreach = new ForeachTable(current_depth, buf, is_map);
            table.foreach(foreach);
            if (is_map) {
                buf.append('}');
            } else {
                buf.append(']');
            }
        } else {
            throw new LuaError("type not supported");
        }
    }

    /**
     * 编码,模拟js的escape函数.<br>
     * escape不编码字符有69个：*+-./@_0-9a-zA-Z
     *
     * @param data 字符串
     * @return 转义后的字符串或者null
     */
    private static final String escape(byte[] data, int offset, int len) {
        if (data == null) {
            return null;
        }

        // 判断是否需要转换
        boolean need = false;
        for (int i = 0; i < data.length; ++i) {
            int ch = data[i] & 0xff;
            if (char2escape[ch] != null) {
                need = true;
                break;
            }
        }
        if (!need) {
            return new String(data, StandardCharsets.UTF_8);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 2);
        int end = offset + len;
        for (int i = offset; i < end; i++) {
            int ch = data[i] & 0xff;
            if (char2escape[ch] == null) {
                bos.write(ch);
            } else {
                bos.write(char2escape[ch], 0, char2escape[ch].length);
            }
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    private static class ForeachTable extends LuaFunction {
        private final int current_depth;
        private final StringBuilder buf;
        private final boolean isMap;
        private boolean first = true;

        protected ForeachTable(int current_depth, StringBuilder buf, boolean isMap) {
            this.current_depth = current_depth;
            this.buf = buf;
            this.isMap = isMap;
        }

        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            if (!first) {
                buf.append(',');
            }
            first = false;

            if (isMap) {
                buf.append('\"');
                if (arg1 instanceof LuaNumber) {
                    buf.append(arg1);
                } else if (arg1 instanceof LuaString) {
                    LuaString str = (LuaString) arg1;
                    buf.append(escape(str.m_bytes, str.m_offset, str.m_length));
                } else {
                    throw new LuaError("table key must be a number or string");
                }
                buf.append('\"');
//                encode(arg1, this, buf);
                buf.append(':');
            }
            encode(current_depth, arg2, buf);

            return LuaValue.NIL;
        }
    }

    //
    //
    //  for decode
    //
    //


    private static class Token {
        private DecodeToken type = DecodeToken.T_ERROR;
        private LuaValue value;
    }

    private static class Parser {
        private final String data;
        private int ptr = 0;
        private int current_depth = 0;

        public Parser(String data) {
            this.data = data;
        }

        public int charAt(int pos) {
            return data != null && pos >= 0 && pos < data.length() ? data.charAt(pos) & 0xffff : 0;
        }

        public int currentChar() {
            return charAt(ptr);
        }

        public int ptrIncrease() {
            this.ptr += 1;
            return this.ptr;
        }

        public boolean strcmp(String str) {
            for (int i = 0; i < str.length(); ++i) {
                if ((ptr + i >= data.length()) || data.charAt(ptr + i) != str.charAt(i)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static void json_next_string_token(Parser json, Token token) {

        /* Caller must ensure a string is next */
//        assert (json.currentChar() == '"');
        if (json.currentChar() != '"') {
            throw new LuaError("invalid string token at " + json.ptr + " in '" + json.data + "'");
        }

        /* Skip " */
        int start = json.ptrIncrease();

        /* json->tmp is the temporary strbuf used to accumulate the
         * decoded string value.
         * json->tmp is sized to handle JSON containing only a string value.
         */
//            strbuf_reset(json -> tmp);

        int ch;
        while ((ch = json.currentChar()) != '"') {
            if (ch == 0) {
                /* Premature end of the string */
                throw new LuaError("unexpected end of string");
            }

            /* Handle escapes */
            if (ch == '\\') {
                /* Fetch escape character */
//                    ch = *(json -> ptr + 1);
                ch = json.charAt(json.ptr + 1);

                /* Translate escape code and append to tmp string */
                ch = escape2char[ch];
//                    if (ch == 'u') {
//                        if (json_append_unicode_escape(json) == 0)
//                            continue;
//
//                        json_set_token_error(token, json,
//                                "invalid unicode escape code");
//                        return;
//                    }
                if (ch == 0) {
                    throw new LuaError("invalid escape code");
                }

                /* Skip '\' */
//                    json -> ptr++;
                json.ptrIncrease();
            }
            /* Append normal character or translated single character
             * Unicode escapes are handled above */
//                strbuf_append_char_unsafe(json -> tmp, ch);
//                json -> ptr++;
            json.ptrIncrease();
        }

        byte[] str = accept(json.data, start, json.ptr);

        // Eat final quote (")
        json.ptrIncrease();

        token.type = DecodeToken.T_STRING;
        token.value = LuaString.valueOf(str);
    }

    /* JSON numbers should take the following form:
     *      -?(0|[1-9]|[1-9][0-9]+)(.[0-9]+)?([eE][-+]?[0-9]+)?
     *
     * json_next_number_token() uses strtod() which allows other forms:
     * - numbers starting with '+'
     * - NaN, -NaN, infinity, -infinity
     * - hexadecimal numbers
     * - numbers with leading zeros
     *
     * json_is_invalid_number() detects "numbers" which may pass strtod()'s
     * error checking, but should not be allowed with strict JSON.
     *
     * json_is_invalid_number() may pass numbers which cause strtod()
     * to generate an error.
     */
    private static boolean json_is_invalid_number(Parser json) {
        int p = json.ptr;

        /* Reject numbers starting with + */
        if (json.charAt(p) == '+') {
            return true;
        }

        /* Skip minus sign if it exists */
        if (json.charAt(p) == '-') {
            p++;
        }

        int ch = json.charAt(p);
        /* Reject numbers starting with 0x, or leading zeros */
        if (ch == '0') {
            int ch2 = json.charAt(p + 1);

            if ((ch2 | 0x20) == 'x' ||          /* Hex */
                    ('0' <= ch2 && ch2 <= '9')) {    /* Leading zero */
                return true;
            }

            return false;
        } else if (ch > '0' && ch <= '9') {
            return false;                           /* Ordinary number */
        }

        /* Reject inf/nan */
//        if (!strncasecmp(p, "inf", 3))
        if (ch == 'i' && json.charAt(p + 1) == 'n' && json.charAt(p + 2) == 'f') {
            return true;
        }
//        if (!strncasecmp(p, "nan", 3))
        if (ch == 'n' && json.charAt(p + 1) == 'a' && json.charAt(p + 2) == 'n') {
            return true;
        }

        /* Pass all other numbers which may still be invalid, but
         * strtod() will catch them. */
        return false;
    }

    /**
     * 判断输入参数是否可能是数字的1部分，数字格式的正则表示：
     * -?(0|[1-9]|[1-9][0-9]+)(.[0-9]+)?([eE][-+]?[0-9]+)?
     *
     * @param ch
     * @return
     */
    private static boolean maybe_in_number(int ch) {
        if (ch >= '0' && ch <= '9') {
            return true;
        } else if (ch == '+' || ch == '-' || ch == '.' || ch == 'E' || ch == 'e') {
            return true;
        } else {
            return false;
        }
    }

    private static void json_next_number_token(Parser json, Token token) {

        token.type = DecodeToken.T_NUMBER;

        int start = json.ptr;
        while (true) {
            int ch = json.currentChar();
            if (!maybe_in_number(ch)) {
                break;
            }
            json.ptrIncrease();
        }
        String number = null;
        if (start < json.ptr) {
            try {
                number = json.data.substring(start, json.ptr);
                double d = Double.parseDouble(number);
                if ((long) d == d) {
                    token.value = LuaLong.valueOf((long) d);
                } else {
                    token.value = LuaNumber.valueOf(d);
                }
                return;
            } catch (Throwable t) {
                throw new LuaError("invalid number '" + number + "': " + t);
            }
        }

        throw new LuaError("invalid null number");

//        token -> value.number = fpconv_strtod(json -> ptr, & endptr);
//        if (json -> ptr == endptr)
//            json_set_token_error(token, json, "invalid number");
//        else
//            json -> ptr = endptr;     /* Skip the processed number */
//
//        return;
    }

    /* Fills in the token struct.
     * T_STRING will return a pointer to the json_parse_t temporary string
     * T_ERROR will leave the json->ptr pointer at the error.
     */
    private static void json_next_token(Parser json, Token token) {
        int ch;

        /* Eat whitespace. */
        while (true) {
            ch = json.currentChar();
            token.type = ch2token[ch];
            if (token.type != DecodeToken.T_WHITESPACE) {
                break;
            }
            json.ptrIncrease();
        }

        /* Store location of new token. Required when throwing errors
         * for unexpected tokens (syntax errors). */
//        token.index = json.ptr;

        /* Don't advance the pointer for an error or the end */
        if (token.type == DecodeToken.T_ERROR) {
            throw new LuaError(String.format("invalue token '%c' at %d in '%s'", ch, json.ptr, json.data));
        }

        if (token.type == DecodeToken.T_END) {
            return;
        }

        /* Found a known single character token, advance index and return */
        if (token.type != DecodeToken.T_UNKNOWN) {
            json.ptr++;
            return;
        }

        /* Process characters which triggered T_UNKNOWN
         *
         * Must use strncmp() to match the front of the JSON string.
         * JSON identifier must be lowercase.
         * When strict_numbers if disabled, either case is allowed for
         * Infinity/NaN (since we are no longer following the spec..) */
        if (ch == '"') {
            json_next_string_token(json, token);
            return;
        } else if (ch == '-' || ('0' <= ch && ch <= '9')) {
            if (!DECODE_INVALID_NUMBERS && json_is_invalid_number(json)) {
                throw new LuaError(String.format("invalue token '%c' at %d in '%s'", ch, json.ptr, json.data));
            }
            json_next_number_token(json, token);
            return;
        } else if (json.strcmp("true")) {
            token.type = DecodeToken.T_BOOLEAN;
            token.value = LuaBoolean.valueOf(true);
            json.ptr += 4;
            return;
        } else if (json.strcmp("false")) {
            token.type = DecodeToken.T_BOOLEAN;
            token.value = LuaBoolean.valueOf(false);
            json.ptr += 5;
            return;
        } else if (json.strcmp("null")) {
            token.type = DecodeToken.T_NULL;
            token.value = LuaValue.NIL;
            json.ptr += 4;
            return;
        } else if (DECODE_INVALID_NUMBERS && json_is_invalid_number(json)) {
            /* When decode_invalid_numbers is enabled, only attempt to process
             * numbers we know are invalid JSON (Inf, NaN, hex)
             * This is required to generate an appropriate token error,
             * otherwise all bad tokens will register as "invalid number"
             */
            json_next_number_token(json, token);
            return;
        }

        /* Token starts with t/f/n but isn't recognised above. */
        throw new LuaError(String.format("invalue token '%c' at %d in '%s'", ch, json.ptr, json.data));
    }

    /* Handle the "value" context */
    private static LuaValue json_process_value(Parser json, Token token) {
        switch (token.type) {
            case T_STRING:
            case T_NUMBER:
            case T_BOOLEAN:
            case T_NULL:
                return token.value;
            case T_OBJ_BEGIN:
                return json_parse_object_context(json);
            case T_ARR_BEGIN:
                return json_parse_array_context(json);
        }
        throw new LuaError("value type " + token.type + " error.");
    }

    private static void json_decode_ascend(Parser json) {
        json.current_depth--;
    }

    static void json_decode_descend(Parser json) {
        json.current_depth++;

        if (json.current_depth <= DECODE_MAX_DEPTH) {
            return;
        }

        throw new LuaError("Found too many nested data structures (" + json.current_depth + ") at character '"
                + (char) json.currentChar() + "'(" + json.ptr + ")");
    }

    private static LuaValue json_parse_object_context(Parser json) {
        Token token = new Token();

        /* 3 slots required:
         * .., table, key, value */
//        json_decode_descend(l, json, 3);
        json_decode_descend(json);

//        lua_newtable(l);

        LuaTable table = new LuaTable();

        json_next_token(json, token);

        /* Handle empty objects */
        if (token.type == DecodeToken.T_OBJ_END) {
//            json_decode_ascend(json);
            json_decode_ascend(json);
            return LuaValue.NIL;
        }

        while (true) {
            if (token.type != DecodeToken.T_STRING) {
                throw new LuaError("object key must a string but got " + token.type);
            }

            /* Push key */
//            lua_pushlstring(l, token.value.string, token.string_len);
            LuaValue key = token.value;

            json_next_token(json, token);
            if (token.type != DecodeToken.T_COLON) {
                throw new LuaError("expect token T_COLON but get " + token.type);
            }

            /* Fetch value */
            json_next_token(json, token);
            LuaValue value = json_process_value(json, token);

            /* Set key = value */
//            lua_rawset(l, -3);
            table.set(key, value);

            json_next_token(json, token);

            if (token.type == DecodeToken.T_OBJ_END) {
                json_decode_ascend(json);
                return table;
            }

            if (token.type != DecodeToken.T_COMMA) {
                throw new LuaError("expect token T_COMMA but get " + token.type);
            }

            json_next_token(json, token);
        }
    }

    /* Handle the array context */
    private static LuaValue json_parse_array_context(Parser json) {
        Token token = new Token();
        int i;

        /* 2 slots required:
         * .., table, value */
//        json_decode_descend(l, json, 2);
        json_decode_descend(json);

//        lua_newtable(l);
        LuaTable table = new LuaTable();

        json_next_token(json, token);

        /* Handle empty arrays */
        if (token.type == DecodeToken.T_ARR_END) {
            json_decode_ascend(json);
            return table;
        }

        for (i = 1; ; i++) {
            LuaValue value = json_process_value(json, token);
//            lua_rawseti(l, -2, i);            /* arr[i] = value */
            table.set(i, value);

            json_next_token(json, token);

            if (token.type == DecodeToken.T_ARR_END) {
                json_decode_ascend(json);
                return table;
            }

            if (token.type != DecodeToken.T_COMMA) {
                throw new LuaError("expect token T_COMMA but get " + token.type);
            }

            json_next_token(json, token);
        }
    }


    /**
     * escape 方法的逆方法
     *
     * @param escaped 编码后的字符串
     * @return
     */
    private static final byte[] accept(String escaped, int start, int end) {
        if (escaped == null) {
            return null;
        }

        int idx = escaped.indexOf('\\', start);
        if (idx < start || idx >= end) {
            return escaped.substring(start, end).getBytes(StandardCharsets.UTF_8);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(escaped.length());
//        int len = escaped.length();
        for (int i = start; i < end; ++i) {
            int ch = escaped.charAt(i);
            if (ch == '\\') {
                // char 1
                int ch1 = escaped.charAt(++i);
                if (ch1 == 'r') {
                    ch = '\r';
                } else if (ch1 == 'n') {
                    ch = '\n';
                } else if (ch1 == 'b') {
                    ch = '\b';
                } else if (ch1 == 't') {
                    ch = '\t';
                } else if (ch1 == 'f') {
                    ch = '\f';
                } else if (ch1 == 'u') { // \\u
                    ch = 0;
                    for (int j = 0; j < 4; j++) {
                        ch1 = escaped.charAt(++i);
                        if (ch1 > '9') {
                            ch1 |= 0x20;/* Force lowercase */
                        }
                        if (ch1 >= 'a' && ch1 <= 'z') {
                            ch1 = ch1 - 'a' + 10;
                        } else if (ch1 >= '0' && ch1 <= '9') {
                            ch1 = ch1 - '0';
                        } else {
                            throw new LuaError("error char at " + i + " in '" + escaped + "'");
                        }
                        ch = (ch << 4) + ch1;
                    }

                    /* UTF-16 surrogate pairs take the following 2 byte form:
                     *      11011 x yyyyyyyyyy
                     * When x = 0: y is the high 10 bits of the ch
                     *      x = 1: y is the low 10 bits of the ch
                     *
                     * Check for a surrogate pair (high or low) */
                    if ((ch & 0xF800) == 0xD800) {
                        /* Error if the 1st surrogate is not high */
                        if ((ch & 0x400) != 0) {
                            throw new LuaError(String.format("error unicode format, unexpect 'x' in 11011 x yyyyyyyyyy: %x", ch));
                        }

                        /* Ensure the next code is a unicode escape */
                        if (escaped.charAt(++i) != '\\' || escaped.charAt(++i) != 'u') {
                            throw new LuaError("error char at " + i + " in '" + escaped + "'");
                        }

                        /* Fetch the next codepoint */

                        int ch2 = 0;
                        for (int j = 0; j < 4; j++) {
                            ch1 = escaped.charAt(++i);
                            if (ch1 > '9') {
                                ch1 |= 0x20;/* Force lowercase */
                            }
                            if (ch1 >= 'a' && ch1 <= 'z') {
                                ch1 = ch1 - 'a' + 10;
                            } else if (ch1 >= '0' && ch1 <= '9') {
                                ch1 = ch1 - '0';
                            } else {
                                throw new LuaError("error char at " + i + " in '" + escaped + "'");
                            }
                            ch2 = (ch2 << 4) + ch1;
                        }

                        if (ch2 < 0) {
                            throw new LuaError(String.format("error negative number: %x", ch2));
                        }

                        /* Error if the 2nd code is not a low surrogate */
                        if ((ch2 & 0xFC00) != 0xDC00) {
                            throw new LuaError(String.format("error unicode format: %x", ch));
                        }

                        /* Calculate Unicode codepoint */
                        ch = (ch & 0x3FF) << 10;
                        ch2 &= 0x3FF;
                        ch = (ch | ch2) + 0x10000;
                    }
                } else {
                    ch = ch1;
                }
            }
            if (ch <= 0x7F) {
                bos.write(ch);
            } else if (ch <= 0x7FF) {
                /* 110xxxxx 10xxxxxx */
                bos.write((ch >> 6) | 0xC0);
                bos.write((ch & 0x3F) | 0x80);
            } else if (ch <= 0xFFFF) {
                /* 1110xxxx 10xxxxxx 10xxxxxx */
                bos.write((ch >> 12) | 0xE0);
                bos.write(((ch >> 6) & 0x3F) | 0x80);
                bos.write((ch & 0x3F) | 0x80);
            } else if (ch <= 0x1FFFFF) {
                /* 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx */
                bos.write((ch >> 18) | 0xF0);
                bos.write(((ch >> 12) & 0x3F) | 0x80);
                bos.write(((ch >> 6) & 0x3F) | 0x80);
                bos.write((ch & 0x3F) | 0x80);
            }
        }
        return bos.toByteArray();
    }

    private static LuaValue decode(LuaString arg) {

        LuaValue value = LuaValue.NIL;

        String data = new String(arg.m_bytes, arg.m_offset, arg.m_length, StandardCharsets.UTF_8);

        Parser json = new Parser(data);

        Token token = new Token();
        int json_len;

//        luaL_argcheck(l, lua_gettop(l) == 1, 1, "expected 1 argument");
//
//        json.cfg = json_fetch_config(l);
//        json.data = luaL_checklstring(l, 1, & json_len);
//        json.current_depth = 0;
//        json.ptr = json.data;

        /* Detect Unicode other than UTF-8 (see RFC 4627, Sec 3)
         *
         * CJSON can support any simple data type, hence only the first
         * character is guaranteed to be ASCII (at worst: '"'). This is
         * still enough to detect whether the wrong encoding is in use. */
        if (data.length() >= 2 && (data.charAt(0) == 0 || data.charAt(1) == 0)) {
            throw new LuaError("JSON parser does not support UTF-16 or UTF-32");
        }

        /* Ensure the temporary buffer can hold the entire string.
         * This means we no longer need to do length checks since the decoded
         * string must be smaller than the entire json string */
//        json.tmp = strbuf_new(json_len);

        json_next_token(json, token);

        value = json_process_value(json, token);

        /* Ensure there is no more input left */
        json_next_token(json, token);

        if (token.type != DecodeToken.T_END) {
            throw new LuaError("the end expect, but got " + token.type);
        }

        return value;
    }
}
