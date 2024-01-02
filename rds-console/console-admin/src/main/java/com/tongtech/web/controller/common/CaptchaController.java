package com.tongtech.web.controller.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.utils.StringUtils;
import com.tongtech.system.service.SysObjectCacheService;
import com.wf.captcha.ArithmeticCaptcha;
import com.wf.captcha.ChineseCaptcha;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.code.kaptcha.Producer;
import com.tongtech.common.config.UhConsoleConfig;
import com.tongtech.common.constant.CacheConstants;
import com.tongtech.common.constant.Constants;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.utils.sign.Base64;
import com.tongtech.common.utils.uuid.IdUtils;
import com.tongtech.system.service.ISysConfigService;

/**
 * 验证码操作处理
 *
 * @author XiaoZhangTongZhi
 */
@RestController
public class CaptchaController {

    private static final Integer captchaWidth = 115;

    private static final Integer captchaHeight = 42;

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private SysObjectCacheService redisCache;

    @Autowired
    private ISysConfigService configService;


    /**
     * 生成验证码
     */
    @GetMapping("/web-api/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) {
        AjaxResult ajax = AjaxResult.success();
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled) {
            return ajax;
        }

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;
        Captcha captcha = null;
        // 生成验证码
        String captchaType = UhConsoleConfig.getCaptchaType();
        if ("rds-math".equals(captchaType)) {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        } else if ("rds-char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        } else if ("easy-math".equals(captchaType)) {
            captcha = new ArithmeticCaptcha(captchaWidth, captchaHeight);
        } else if ("easy-chinese".equals(captchaType)) {
            captcha = new ChineseCaptcha(captchaWidth, captchaHeight);
        } else if ("easy-char".equals(captchaType)) {
            captcha = new SpecCaptcha(captchaWidth, captchaHeight);
        } else {
            throw new ServiceException("验证码配置有误，请检查配置字段！");
        }

        ajax.put("uuid", uuid);

        // ry-math  ry-char
        if ("rds-math".equals(captchaType) || "rds-char".equals(captchaType)) {
            // 转换流信息写出
            FastByteArrayOutputStream os = new FastByteArrayOutputStream();
            try {
                ImageIO.write(image, "jpg", os);
            } catch (IOException e) {
                return AjaxResult.error(e.getMessage());
            }
            ajax.put("img", Base64.encode(os.toByteArray()));
        } else {

            code = captcha.text();

            if (StringUtils.isEmpty(code))
                throw new ServiceException("验证码获取失败，请稍后重试！");
            ajax.put("img", captcha.toBase64().split(",")[1]);
        }

        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        return ajax;

    }
}
