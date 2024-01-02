package com.tongtech.proxy.objects;

/**
 * config in proxy.xml:
 *
 *     <LuaObjects>
 *         <SC pi="3.14159">
 *             com.server.objects.Scientific
 *         </SC>
 *     </LuaObjects>
 *
 */
public class Scientific {
    /**
     * 演示参数调用和多返回值
     *
     * eval "local i,j = SC:add(1,2);return i;"
     * :3
     * eval "local i,j = SC:add(1,2);return j"
     * $2
     * OK
     *
     * @param i 被加数
     * @param j 加数
     * @return 运算结果和文字说明，对应lua中的2个返回值（一个整数，一个字符串）
     */
    public Object[] add(int i, int j) {
        return new Object[]{i + j, "OK"};
    }

    /**
     * 演示从配置文件中读参数
     *
     * eval "return SC:angle2Radian(30)"
     * $8
     * 9.549305
     *
     * @param angle 角度
     * @return 弧度
     */
    public double angle2Radian(double angle) {
        String cfg_pi = System.getProperty("com.server.objects.Scientific-pi");
        double pi = Math.PI;
        if (cfg_pi != null) {
            try {
                pi = Double.parseDouble(cfg_pi);
            } catch (Throwable t) {
            }
        }
        return angle / pi;
    }
}