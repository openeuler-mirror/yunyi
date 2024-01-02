package com.tongtech.common.constant;

/**
 * 控制台中通用常量信息
 *
 * @author Zhang Chenlong
 */
public class ConsoleConstants
{
    /**
     * 节点管理器实例名前缀，实例名：实例名前缀 + 主机ID
     * 如：HID18
     */
    public static final long CENTER_SERVICE_ID = 1L;

    //public static final long OPERATION_TIMEOUT = 1000 * 60 * 2; //超时时间2分钟，单位毫秒

    public static final String STATUS_RUNNING = "RUNNING";

    public static final String CONFIG_TYPE_LICENSE = "center-lic";

    public static final String CONFIG_SYS_INITIALIZED_KEY = "sys.initialized";

    public static final String CONFIG_SYS_DEVELOPMENT_MODE_KEY = "sys.development.mode";


    public final static String MAIN_ARG_RESTORE = "--restore";

    public final static String MAIN_ARG_INITIALIZE = "--initialize";

    public final static String MAIN_ARG_INITIALIZE_SHORT = "-i";

    public final static String MAIN_ARG_DEVELOPMENT = "--development";

    public final static String MAIN_ARG_DEVELOPMENT_SHORT = "-d";

    public final static int STATISTIC_GROUPS = 10; //检测统计时，分组数量

    public final static int STATISTIC_GROUP_MIN_SECOND = 600; //查询时间范围小于等于该值后，就逐条查询；大于就进行分组统计


}
