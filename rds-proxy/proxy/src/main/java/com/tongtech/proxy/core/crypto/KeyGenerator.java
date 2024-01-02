package com.tongtech.proxy.core.crypto;

import java.io.*;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Properties;

public class KeyGenerator {

	// 密钥长度，目前2048是“绝对”安全的
	// 如果需要增加加密强度，直接增大该值即可，程序会自动判断并更新文件中保存的密钥
	public static final int KEYLEN = 4096;

	// 密钥转换为字符串的方式，目前配置是用BASE16方式转换，程序支持该配置最大为36
	public static final int BIGINTENCODING = 16;

	public static final SecureRandom SRNG = new SecureRandom();

	private static String PRIV_TITLE = "PrivateKey";

	private static String PUB_TITLE = "PublicKey";

	private static String MOD_TITLE = "Modulus";

	private static final String SaveFile = System.getProperty("server.home",
			".")
			+ "/etc/.certification.keystore";

	private static PrivateKey PrivateKey = null;

	public static PrivateKey generateRSAPrivateKey() {

		if (PrivateKey != null) {
			return PrivateKey;
		}

		try {
			PrivateKey = loadPrivateKey();
		} catch (Exception e) {
			PrivateKey = null;
		}

		if (PrivateKey == null) {
			try {
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
				keyGen.initialize(KEYLEN, SRNG);
				KeyPair pair = keyGen.generateKeyPair();
				RSAPrivateCrtKey prvKey = (RSAPrivateCrtKey) pair.getPrivate();

				PrivateKey = new PrivateKey(prvKey.getPrivateExponent(), prvKey
						.getPublicExponent(), prvKey.getModulus());

				savePrivateKey(PrivateKey);
			} catch (Exception e) {
				e.printStackTrace();
				//throw new IOException(e.toString());
			}
		}

		if (PrivateKey == null) {
			throw new NullPointerException("private-key is null");
		}

		return PrivateKey;
	}

	/**
	 * 保存PrivateKey对象
	 * 
	 * @param key
	 * @throws IOException
	 */
	private static void savePrivateKey(PrivateKey key) throws IOException {
		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(SaveFile)));
		wr.write(PUB_TITLE + " = " + key.getE() + "\n");
		wr.write(PRIV_TITLE + " = " + key.getD() + "\n");
		wr.write(MOD_TITLE + " = " + key.getN() + "\n");
		wr.close();
	}

	/**
	 * 读PrivateKey对象
	 * 
	 * @return
	 * @throws IOException
	 */
	private static PrivateKey loadPrivateKey() throws IOException {
		Properties pro = new Properties();
		pro.load(new FileInputStream(SaveFile));
		BigInteger d = new BigInteger(pro.getProperty(PRIV_TITLE),
				BIGINTENCODING);
		BigInteger e = new BigInteger(pro.getProperty(PUB_TITLE),
				BIGINTENCODING);
		BigInteger n = new BigInteger(pro.getProperty(MOD_TITLE),
				BIGINTENCODING);
		PrivateKey key = new PrivateKey(d, e, n);
		BigInteger i = new BigInteger("1234567");
		if (n.bitLength() < KEYLEN || !i.equals(key.decrypt(key.encrypt(i)))) {
			throw new IllegalStateException("file data error");
		}
		return key;
	}

}
