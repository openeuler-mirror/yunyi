
package com.tongtech.proxy.core.crypto;

import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;

public class BogusSSLContextFactory {

    private static final Log logger = ProxyConfig.getServerLog();

    /**
     * 使用的协议
     */
    private static final String PROTOCOL = "TLS";

    private static final String KEY_MANAGER_FACTORY_ALGORITHM;

    static {
        String algorithm = Security
                .getProperty("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }
        KEY_MANAGER_FACTORY_ALGORITHM = algorithm;
    }

    /**
     * Bougus Server certificate keystore file name.
     */
    private static final String BOGUS_KEYSTORE = "server.cert";

    // NOTE: keystore 使用 keytool命令创建:
    // keytool -genkey -alias fortress -keysize 4096 -validity 7300 \
    // -keyalg RSA -dname "CN=AGK Tech., OU=Private CA, \
    // O=AGK Tech., L=Beijing, S=Beijing, C=CN" \
    // -keypass agk+Bogus -storepass agk+bogUs -keystore fortress.cert
    //
    // NOTE2: Export the public key:
    // keytool -export -alias fortress -keystore fortress.cert \
    // -storepass agk+bogUs -rfc -file public.cer
    //
    private static final String KEYSTORE_CREATE_STR = "keytool -genkey -alias fortress -keysize 4096 -validity 7300 -keyalg RSA -dname \"CN=agk.com, OU=XXX CA, O=AGK Tech., L=Beijing, S=Beijing, C=CN\" -keypass boguspw -storepass boguspw -keystore ";
    /**
     * keystore文件的密码.
     */
    private static final char[] BOGUS_PW = {'a', 'g', 'k', '+', 'b', 'o', 'g',
            'U', 's'};

    private static SSLContext serverInstance = null;

    private static SSLContext clientInstance = null;

    /**
     * 用于创建netty的SslHandler
     *
     * @return
     * @throws GeneralSecurityException
     */
    public static SSLEngine createServerSSLEngine() throws GeneralSecurityException {
        SSLEngine sslEngine = getInstance(true).createSSLEngine();
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }

    public static SSLEngine createClientSSLEngine() throws GeneralSecurityException {
        SSLEngine sslEngine = getInstance(false).createSSLEngine();
        sslEngine.setUseClientMode(true);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }

    /**
     * Get SSLContext singleton.
     *
     * @return SSLContext
     * @throws GeneralSecurityException
     */
    public static SSLContext getInstance(boolean server)
            throws GeneralSecurityException {

        SSLContext retInstance = null;
        if (server) { // is server
            if (serverInstance == null) {
                synchronized (BogusSSLContextFactory.class) {
                    if (serverInstance == null) {
                        try {
                            serverInstance = createBougusServerSSLContext();
                            logger.debugLog("BogusSSLContextFactory::getInstance() New SSLContext created.");
                        } catch (Exception ioe) {
                            logger.infoLog("BogusSSLContextFactory::getInstance() Can't create Server SSLContext: {}", ioe);
                            throw new GeneralSecurityException("Can't create Server SSLContext: " + ioe.getMessage());
                        }
                    }
                }
            }
            retInstance = serverInstance;
        } else {// is client
            if (clientInstance == null) {
                synchronized (BogusSSLContextFactory.class) {
                    if (clientInstance == null) {
                        clientInstance = createBougusClientSSLContext();
                    }
                }
            }
            retInstance = clientInstance;
        }
        return retInstance;
    }

    private static SSLContext createBougusServerSSLContext()
            throws GeneralSecurityException, IOException {

        // Create keystore
        KeyStore ks = KeyStore.getInstance("JKS");
        InputStream in = null;
        try {
            File path_file = new File(System.getProperty("server.home", ".")
                    + "/resource/" + BOGUS_KEYSTORE);
            if (!path_file.isFile()) {
                // create a new keystor
                if (logger.isDebug()) {
                    logger.debugLog("BogusSSLContextFactory::createBougusServerSSLContext() No found certification file '{}', use the default certificate."
                            , path_file.getAbsolutePath());
                }
                in = new ByteArrayInputStream(DefaultCertificate.DEFAULT_CERTIFICATE);
            } else {
                in = new FileInputStream(path_file);
            }
            ks.load(in, BOGUS_PW);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ignored) {
                }
            }
        }

        // 设置key-manager使用程序提供的keystore
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KEY_MANAGER_FACTORY_ALGORITHM);
        kmf.init(ks, BOGUS_PW);

        // 初始化SSLContext.
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
        sslContext.init(kmf.getKeyManagers(),
                (new BogusTrustManagerFactory()).getTrustManagers(), null);

        return sslContext;

    }

    private static SSLContext createBougusClientSSLContext()
            throws GeneralSecurityException {

        SSLContext context = SSLContext.getInstance(PROTOCOL);
        context.init(null, (new BogusTrustManagerFactory()).getTrustManagers(),
                null);
        return context;

    }
}
