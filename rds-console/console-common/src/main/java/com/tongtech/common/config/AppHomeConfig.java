package com.tongtech.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class AppHomeConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppHomeConfig.class);

    public static final String CENTER_PATH = "node-mgr";

    public static final String TEMPLATE_PATH = toLocal("data/template");

    public static final String PACKAGE_PATH = toLocal("data/package");

    public static final String PACKAGE_VERSION_PATH = toLocal("data/package/version");

    private static final String PROFILE_PATH = "data/profile";

    public static  String ETC_PATH = "config";

    public static final String CENTER_LICENSE_FILE = toLocal("apps/center/center.lic");

    public static final String DATABASE_PATH = toLocal("data/db");

    public static final String DATABASE_NAME = "consoledb";

    public static final String DATABASE_BACKUP_PATH = toLocal("data/dbbak");

    private static File pwdDir = null;

    public static String getProfile()
    {
        return PROFILE_PATH;
    }

    /**
     * 获取导入上传路径
     */
    public static String getImportPath()
    {
        return getProfile() + "/import";
    }

    /**
     * 获取头像上传路径
     */
    public static String getAvatarPath()
    {
        return getProfile() + "/avatar";
    }

    /**
     * 获取下载路径
     */
    public static String getDownloadPath()
    {
        return getProfile() + "/download/";
    }

    /**
     * 获取上传路径
     */
    public static String getUploadPath()
    {
        return getProfile() + "/upload";
    }



    public static final File getAbsoluteFile(String baseDir, String fileName)
    {
        File desc = new File(getPWD(), baseDir + File.separator + fileName);

        if (!desc.getParentFile().exists())
        {
            desc.getParentFile().mkdirs();
        }

        return desc;
    }

    public static final String getAbsolutePath(String baseDir, String fileName) {
        File f = getAbsoluteFile(baseDir, fileName);
        return f.getAbsolutePath();
    }


    public static final File getAbsoluteFile(String path)
    {
        File desc = new File(getPWD(), path);

        if (!desc.getParentFile().exists())
        {
            desc.getParentFile().mkdirs();
        }

        return desc;
    }

    public static final String getAbsolutePath(String path) {
        File f = getAbsoluteFile(path);
        return f.getAbsolutePath();
    }

    /**
     * 得到当前工作路径
     * @return
     */
    private static File getPWD() {

        if(pwdDir == null) {
            try {
                pwdDir = new File((new File(".")).getCanonicalPath());
                logger.info("System initialed application home directory at:" + pwdDir.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return pwdDir;
    }


    private static String toLocal(String path) {
        return path.replace('/', File.separatorChar);
    }


}
