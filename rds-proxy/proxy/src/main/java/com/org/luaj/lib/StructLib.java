package com.org.luaj.lib;

import com.org.luaj.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/*
** Valid formats:
** > - big endian
** < - little endian
** ![num] - alignment
** x - pading
** b/B - signed/unsigned byte
** h/H - signed/unsigned short
** l/L - signed/unsigned long
** T   - size_t
** i/In - signed/unsigned integer with size 'n' (default is size of int)
** cn - sequence of 'n' chars (from/to a string); when packing, n==0 means
        the whole string; when unpacking, n==0 means use the previous
        read number as the string length
** s - zero-terminated string
** f - float
** d - double
** ' ' - ignored
*/
public class StructLib extends OneArgFunction {
//    private static final Log LOGGER = Configuration.getServerLog();

    private static final int MAXINTSIZE = 32;
    private static final int MAXALIGN = 4;
    private static final int BIG = 0;
    private static final int LITTLE = 1;

    public StructLib() {
    }

    public LuaValue call(LuaValue arg) {
        LuaTable t = new LuaTable();
        bind(t, StructLibV.class, new String[]{
                "pack", "unpack", "size"});
        env.set("struct", t);
        PackageLib.instance.LOADED.set("struct", t);
        return t;
    }

    static final class StructLibV extends VarArgFunction {
        public Varargs invoke(Varargs args) {
            switch (opcode) {
                case 0:
                    // pack
                    try {
                        return pack(args);
                    } catch (Throwable e) {
                        throw new LuaError(e);
                    }
                case 1:
                    // unpack
                    return unpack(args);
//                try {
//                    return unpack(args);
//                } catch (Throwable e) {
//                    throw new LuaError(e.getMessage());
//                }
                case 2:
                    return LuaLong.valueOf(size(args));
            }
            return NONE;
        }
    }

    private static LuaString pack(Varargs vars) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        LuaString fmt = vars.checkstring(1);
        int fmt_opt = 0;
        Header h = new Header();
        int arg = 2;
        int totalsize = 0;
        while (fmt_opt < fmt.length()) {
            int opt = fmt.charAt(fmt_opt++);
            if (isdigit(opt)) {
                continue;
            }
            int size = optsize(opt, fmt, fmt_opt);
            int toalign = gettoalign(totalsize, h, opt, size);
            totalsize += toalign;
//            while (toalign-- > 0) luaL_addchar( & b, '\0');
            switch (opt) {
                case 'b':
                case 'B':
                case 'h':
                case 'H':
                case 'l':
                case 'L':
                case 'T':
                case 'i':
                case 'I': {  /* integer types */
                    putinteger(vars.checkint(arg++), b, h.endian, size);
                    break;
                }
                case 'x': {
                    b.write(0);
//                    luaL_addchar( & b, '\0');
                    break;
                }
                case 'f': {
//                    float f = (float) luaL_checknumber(L, arg++);
                    float f = (float) vars.checkdouble(arg++);
                    int fi = Float.floatToIntBits(f);
                    putinteger(fi, b, h.endian, size);
//                    correctbytes(( char *)&f, size, h.endian);
//                    luaL_addlstring( & b, ( char *)&f, size);
                    break;
                }
                case 'd': {
//                    double d = luaL_checknumber(L, arg++);
                    double d = vars.checkdouble(arg++);
                    long dl = Double.doubleToLongBits(d);
                    putinteger(dl, b, h.endian, size);
//                    correctbytes(( char *)&d, size, h.endian);
//                    luaL_addlstring( & b, ( char *)&d, size);
                    break;
                }
                case 'c':
                case 's': {
                    LuaString s = vars.checkstring(arg++);
                    int l = s.length();
                    if (size == 0) {
                        size = l;
                    }
                    if (l >= size) {
                        b.write(Arrays.copyOf(s.m_bytes, size));
//                         luaL_addlstring( & b, s, size);
                        if (opt == 's') {
                            b.write(0); /* add zero at the end */
//                            luaL_addchar( & b, '\0');  /* add zero at the end */
                            size++;
                        }
                    } else {
//                        LOGGER.warnLog("StrtuctLib::pack() string {} too short", s);
                        throw new LuaError("pack: string too short");
                    }
                    break;
                }
                default:
                    controloptions(opt, fmt, fmt_opt, h);
            }
            totalsize += size;
        }
        return LuaString.valueOf(b.toByteArray());
    }

    private static Varargs unpack(Varargs vars) {
        ArrayList<LuaValue> ret = new ArrayList<>();
        Header h = new Header();
        LuaString fmt = vars.checkstring(1);
        int fmt_opt = 0;
        LuaString data = vars.checkstring(2);
        int ld = data.length();
        int pos = vars.optint(3, 1);
        if (pos <= 0) {
//            LOGGER.warnLog("StructLib::unpack() offset '{}' must be 1 or greater", vars.checkstring(3));
            throw new LuaError("unpack: offset must be 1 or greater");
        }
//        size_t pos = luaL_optinteger(L, 3, 1);
//        luaL_argcheck(L, pos > 0, 3, "offset must be 1 or greater");
        pos--; /* Lua indexes are 1-based, but here we want 0-based for C
         * pointer math. */
        int n = 0;  /* number of results */
//        defaultoptions( & h);
        while (fmt_opt < fmt.length()) {
            int opt = fmt.charAt(fmt_opt++);
            if (isdigit(opt)) {
                continue;
            }
            int size = optsize(opt, fmt, fmt_opt);
            pos += gettoalign(pos, h, opt, size);
            if (size > ld || pos > (ld - size)) {
                throw new LuaError("unpack: data string too short");
            }
//            luaL_argcheck(L, size <= ld && pos <= ld - size,
//                    2, "data string too short");
            /* stack space for item + next position */
//            luaL_checkstack(L, 2, "too many results");
            switch (opt) {
                case 'b':
                case 'B':
                case 'h':
                case 'H':
                case 'l':
                case 'L':
                case 'T':
                case 'i':
                case 'I': {  /* integer types */
                    int issigned = islower(opt) ? 1 : 0;
                    long res = getinteger(data, pos, h.endian, issigned, size);
                    ret.add(LuaNumber.valueOf(res));
                    n++;
                    break;
                }
                case 'x': {
                    break;
                }
                case 'f': {
                    int i = (int) getinteger(data, pos, h.endian, 1, size);
                    float f = Float.intBitsToFloat(i);
                    ret.add(LuaDouble.valueOf(f));
                    n++;
                    break;
                }
                case 'd': {
                    long l = getinteger(data, pos, h.endian, 1, size);
                    double d = Double.longBitsToDouble(l);
                    ret.add(LuaDouble.valueOf(d));
                    n++;
                    break;
                }
                case 'c': {
                    if (size == 0) {
                        if (n == 0 || !ret.get(n - 1).isnumber()) {
                            throw new LuaError("unpack: format 'c0' needs a previous size");
                        }
                        size = ret.remove(n - 1).checkint();
                        n--;
                        if (size > ld || pos > (ld - size)) {
                            throw new LuaError("unpack: data string too short");
                        }
                    }
                    if (size == 0) {
                        ret.add(LuaString.valueOf(""));
                    } else {
                        byte[] d = new byte[size];
                        System.arraycopy(data.m_bytes, pos, d, 0, size);
                        ret.add(LuaString.valueOf(d));
                    }
                    n++;
                    break;
                }
                case 's': {
                    byte[] ds = data.m_bytes;
//        const char *e = (const char *)memchr(data + pos, '\0', ld - pos);
                    int end = -1;
                    for (int i = pos; i < ld; ++i) {
                        if (ds[i] == 0) {
                            end = i;
                            break;
                        }
                    }
                    if (end < pos) {
                        throw new LuaError("unpack: unfinished string in data");
                    }
                    size = end - pos + 1;
//                    lua_pushlstring(L, data + pos, size - 1);
                    if (end == pos) {
                        ret.add(LuaString.valueOf(""));
                    } else {
                        byte[] d = new byte[end - pos];
                        System.arraycopy(data.m_bytes, pos, d, 0, end - pos);
                        ret.add(LuaString.valueOf(d));
                    }
                    n++;
                    break;
                }
                default:
                    controloptions(opt, fmt, fmt_opt, h);
            }
            pos += size;
        }
        return varargsOf(ret.toArray(new LuaValue[0]));
    }

    private static void putinteger(long value, OutputStream b, int endian, int size) throws IOException {
        if (endian == LITTLE) {
            for (int i = 0; i < size; i++) {
                b.write((int) (value >> (i * 8)) & 0xff);
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                b.write((int) (value >> (i * 8)) & 0xff);
            }
        }
    }

    private static long getinteger(LuaString data, int pos, int endian, int issigned, int size) {
        long l = 0;
        int i;
        if (endian == BIG) {
            for (i = 0; i < size; i++) {
                l <<= 8;
                l |= data.charAt(i + pos);
            }
        } else {
            for (i = size - 1; i >= 0; i--) {
                l <<= 8;
                l |= data.charAt(i + pos);
            }
        }
        if (issigned > 0) {  /* signed format */
            long mask = (-1l) << (size * 8 - 1);
            if ((l & mask) != 0) { /* negative value? */
                l |= mask;  /* signal extension */
            }
        }
        return l;
    }

    private static int size(Varargs vars) {
        Header h = new Header();
        LuaString fmt = vars.checkstring(1);
        int pos = 0;
        int fmt_pos = 0;
        while (fmt_pos < fmt.length()) {
//            int opt = *fmt++;
            int opt = fmt.charAt(fmt_pos++);
            if (isdigit(opt)) {
                continue;
            }
            int size = optsize(opt, fmt, fmt_pos);
            pos += gettoalign(pos, h, opt, size);
            if (opt == 's') {
//                LOGGER.warnLog("StructLib::size() option 's' in {}({}) has no fixed size", fmt, fmt_pos);
                throw new LuaError("option 's' has no fixed size");
            } else if (opt == 'c' && size == 0) {
//                LOGGER.warnLog("StructLib::size() option 'c0' in {}({}) has no fixed size", fmt, fmt_pos);
                throw new LuaError("option 'c0' has no fixed size");
            }
            if (!isalnum(opt)) {
                controloptions(opt, fmt, fmt_pos, h);
            }
            pos += size;
        }
//        lua_pushinteger(L, pos);
        return pos;
    }

    private static int optsize(int opt, LuaString fmt, int fmt_pos) {
        switch (opt) {
            case 'B':
            case 'b':
                return 1;
            case 'H':
            case 'h':
                return 2;
            case 'L':
            case 'l':
                return 8;
            case 'T':
                return 4;
            case 'f':
                return 4;
            case 'd':
                return 8;
            case 'x':
                return 1;
            case 'c':
                return getnum(fmt, fmt_pos, 1);
            case 'i':
            case 'I': {
                int sz = getnum(fmt, fmt_pos, 4);
                if (sz > MAXINTSIZE) {
//                    LOGGER.warnLog("StructLib::optsize() integral size {} is larger than limit of {}", sz, MAXINTSIZE);
                    throw new LuaError("size is larger than limit of " + MAXINTSIZE);
                }
                return sz;
            }
            default:
                return 0;  /* other cases do not need alignment */
        }
    }

    private static int getnum(LuaString fmt, int fmt_pos, int df) {
        int c = 0;
        if (fmt_pos < fmt.length()) {
            c = fmt.charAt(fmt_pos++);
        }
        if (!isdigit(c))  /* no number? */ {
            return df;  /* return default value */
        } else {
            int a = 0;
            do {
                if (a > (Integer.MAX_VALUE / 10) || a * 10 > (Integer.MAX_VALUE - (c - '0'))) {
//                    LOGGER.warnLog("StructLib::getnum() integral size overflow");
                    throw new LuaError("integral size overflow");
                }
                a = a * 10 + c - '0';
                if (fmt_pos < fmt.length()) {
                    c = fmt.charAt(fmt_pos++);
                } else {
                    break;
                }
            } while (isdigit(c));
            return a;
        }
    }

    private static void controloptions(int opt, LuaString fmt, int fmt_opt, Header h) {
        switch (opt) {
            case ' ':
                return;  /* ignore white spaces */
            case '>':
                h.endian = BIG;
                return;
            case '<':
                h.endian = LITTLE;
                return;
            case '!': {
                int a = getnum(fmt, fmt_opt, MAXALIGN);
                if (!((a) > 0 && ((a) & ((a) - 1)) == 0)) {
//                    LOGGER.warnLog("StructLib::controloptions() alignment {} is not a power of 2", a);
                    throw new LuaError("alignment is not a power of 2");
                }
                h.align = a;
                return;
            }
            default: {
//                LOGGER.debugLog("StructLib::controloptions() invalid format option '{}'", (char) opt);
                throw new LuaError("invalid format option " + (char) opt);
            }
        }
    }

    private static int gettoalign(int len, Header h, int opt, int size) {
        if (size == 0 || opt == 'c') return 0;
        if (size > h.align)
            size = h.align;  /* respect max. alignment */
        return (size - (len & (size - 1))) & (size - 1);
    }

    private static boolean islower(int i) {
        return i >= 'a' && i <= 'z';
    }

    private static boolean isdigit(int i) {
        return i >= '0' && i <= '9';
    }

    private static boolean isalnum(int i) {
        return i >= '0' && i <= '9' || i >= 'a' && i <= 'z' || i >= 'A' && i <= 'Z';
    }

    private static class Header {
        private int endian = LITTLE;
        private int align = 1;
    }
}
