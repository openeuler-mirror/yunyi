package com.tongtech.common.utils;

import com.tongtech.common.config.AppHomeConfig;
import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.utils.file.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class TemplateUtils {
    public static String readLocalTemplate(String templateFileName) {

        try {
            File templateFile = AppHomeConfig.getAbsoluteFile(AppHomeConfig.TEMPLATE_PATH, templateFileName);
            return FileUtils.readFileToString(templateFile, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("read template file an exception occurs.");
        }
    }
}
