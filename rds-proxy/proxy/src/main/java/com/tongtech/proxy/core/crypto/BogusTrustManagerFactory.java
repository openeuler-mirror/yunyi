package com.tongtech.proxy.core.crypto;

import javax.net.ssl.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class BogusTrustManagerFactory extends TrustManagerFactory {
    private static final X509TrustManager X509 = new X509TrustManager() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            // Do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    private static final TrustManager[] X509_MANAGERS = new TrustManager[] { X509 };

    /**
     * Creates a new BogusTrustManagerFactory instance
     */
    public BogusTrustManagerFactory() {
        super(new BogusTrustManagerFactorySpi(), new Provider("CoreBogus", 1.0, "") {
            private static final long serialVersionUID = -4024169055312053827L;
        }, "MinaBogus");
    }

    private static class BogusTrustManagerFactorySpi extends TrustManagerFactorySpi {
        /**
         * {@inheritDoc}
         */
        @Override
        protected TrustManager[] engineGetTrustManagers() {
            return X509_MANAGERS;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void engineInit(KeyStore keystore) throws KeyStoreException {
            // noop
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
                throws InvalidAlgorithmParameterException {
            // noop
        }

    }
}
