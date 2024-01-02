package com.tongtech.proxy.core.crypto;

import java.math.BigInteger;

public class PublicKey {
	// 公钥
	private BigInteger e;

	// 摸
	private BigInteger n;

	public PublicKey(String e, String n) {
		this.e = new BigInteger(e, KeyGenerator.BIGINTENCODING);
		this.n = new BigInteger(n, KeyGenerator.BIGINTENCODING);
	}

	/**
	 * 用公钥加密
	 * 
	 * @param m
	 * @return
	 */
	public BigInteger encrypt(BigInteger m) {
		return m.modPow(e, n);
	}
}
