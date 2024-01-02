/*******************************************************************************
 * Copyright (c) 2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.org.luaj.lib.jse;

import com.org.luaj.*;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LuaValue that represents a Java method.
 * <p>
 * Can be invoked via call(LuaValue...) and related methods.
 * <p>
 * This class is not used directly.
 * It is returned by calls to calls to {@link JavaInstance#get(LuaValue key)}
 * when a method is named.
 *
 * @see CoerceJavaToLua
 * @see CoerceLuaToJava
 */
class JavaMethod extends JavaMember {

    static final Map methods = Collections.synchronizedMap(new HashMap());

    static JavaMethod forMethod(Method m) {
        JavaMethod j = (JavaMethod) methods.get(m);
        if (j == null)
            methods.put(m, j = new JavaMethod(m));
        return j;
    }

    static LuaFunction forMethods(JavaMethod[] m) {
        return new Overload(m);
    }

    final Method method;

    private JavaMethod(Method m) {
        super(m.getParameterTypes(), m.getModifiers());
        this.method = m;
        try {
            if (!m.isAccessible())
                m.setAccessible(true);
        } catch (SecurityException s) {
        }
    }

    public LuaValue call() {
        return error("method cannot be called without instance");
    }

    public LuaValue call(LuaValue arg) {
        return invokeMethod(arg.checkuserdata(), LuaValue.NONE);
    }

    public LuaValue call(LuaValue arg1, LuaValue arg2) {
        return invokeMethod(arg1.checkuserdata(), arg2);
    }

    public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
        return invokeMethod(arg1.checkuserdata(), LuaValue.varargsOf(arg2, arg3));
    }

    public Varargs invoke(Varargs args) {
        return invokeMethod1(args.checkuserdata(1), args.subargs(2));
    }

    LuaValue invokeMethod(Object instance, Varargs args) {
        Object[] a = convertArgs(args);
        try {
            return CoerceJavaToLua.coerce(method.invoke(instance, a));
        } catch (InvocationTargetException e) {
            throw new LuaError(e.getTargetException());
        } catch (Exception e) {
            return LuaValue.error("coercion error " + e);
        }
    }

    Varargs invokeMethod1(Object instance, Varargs args) {
        Object[] a = convertArgs(args);
        try {
            Object ret = method.invoke(instance, a);
            return java2lua(ret);
        } catch (InvocationTargetException e) {
            throw new LuaError(e.getTargetException());
        } catch (Exception e) {
            return LuaValue.error("coercion error " + e);
        }
    }

    private Varargs java2lua(Object o) {
        if (o == null) {
            return LuaValue.NIL;
        } else if (o.getClass().isArray()) {
            // 返回了对象数组，对应lua的多返回值
            int length = Array.getLength(o);
            LuaValue[] rets = new LuaValue[length];
            if (length > 0) {
                for (int i = 0; i < length; ++i) {
                    rets[i] = object2lua(Array.get(o, i));
                }
            }
            return varargsOf(rets);
        } else {
            // 单一返回值
            return object2lua(o);
        }
    }

    private LuaValue object2lua(Object o) {
        LuaValue ret;
        if (o == null) {
            ret = LuaValue.NIL;
        } else if (o instanceof List) {
            ret = new LuaTable();
            List list = (List) o;
            if (list.size() > 0) {
                ret.presize(list.size());
                for (int i = 0; i < list.size(); ++i) {
                    ret.set(i + 1, object2lua(list.get(i)));
                }
            }
        } else if (o instanceof Object[]) {
            ret = new LuaTable();
            Object[] objects = (Object[]) o;
            if (objects.length > 0) {
                ret.presize(objects.length);
                for (int i = 0; i < objects.length; ++i) {
                    ret.set(i + 1, object2lua(objects[i]));
                }
            }
        } else if (o instanceof Map) {
            ret = new LuaTable();
            Map map = (Map) o;
            if (map.size() > 0) {
                ret.presize(map.size());
                for (Object n : map.keySet()) {
                    Object v = map.get(n);
                    ret.set(object2lua(n), object2lua(v));
                }
            }
        } else if (o instanceof Long || o instanceof Integer) {
            ret = LuaNumber.valueOf(((Number) o).longValue());
        } else if (o instanceof Float || o instanceof Double) {
            ret = LuaNumber.valueOf(((Number) o).doubleValue());
        } else if (o instanceof byte[]) {
            ret = LuaString.valueOf((byte[]) o);
        } else if (o instanceof Boolean) {
            ret = LuaNumber.valueOf(((Boolean) o).booleanValue());
        } else if (o instanceof String) {
            ret = LuaString.valueOf(o.toString());
        } else {
            ret =LuaValue.userdataOf(o);
        }
        return ret;
    }

    /**
     * LuaValue that represents an overloaded Java method.
     * <p>
     * On invocation, will pick the best method from the list, and invoke it.
     * <p>
     * This class is not used directly.
     * It is returned by calls to calls to {@link JavaInstance#get(LuaValue key)}
     * when an overloaded method is named.
     */
    static class Overload extends LuaFunction {

        final JavaMethod[] methods;

        Overload(JavaMethod[] methods) {
            this.methods = methods;
        }

        public LuaValue call() {
            return error("method cannot be called without instance");
        }

        public LuaValue call(LuaValue arg) {
            return invokeBestMethod(arg.checkuserdata(), LuaValue.NONE);
        }

        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            return invokeBestMethod(arg1.checkuserdata(), arg2);
        }

        public LuaValue call(LuaValue arg1, LuaValue arg2, LuaValue arg3) {
            return invokeBestMethod(arg1.checkuserdata(), LuaValue.varargsOf(arg2, arg3));
        }

        public Varargs invoke(Varargs args) {
            return invokeBestMethod(args.checkuserdata(1), args.subargs(2));
        }

        private LuaValue invokeBestMethod(Object instance, Varargs args) {
            JavaMethod best = null;
            int score = CoerceLuaToJava.SCORE_UNCOERCIBLE;
            for (int i = 0; i < methods.length; i++) {
                int s = methods[i].score(args);
                if (s < score) {
                    score = s;
                    best = methods[i];
                    if (score == 0)
                        break;
                }
            }

            // any match?
            if (best == null)
                LuaValue.error("no coercible public method");

            // invoke it
            return best.invokeMethod(instance, args);
        }
    }

}
