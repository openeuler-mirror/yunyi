package com.tongtech.proxy.core.crypto;

import java.math.BigInteger;

public class PrivateKey {
	public String getAlgorithm() {
		return "ssh-rsa";
	}

	// 私钥
	private BigInteger d;

	// 公钥
	private BigInteger e;

	// 摸
	private BigInteger n;

	public PrivateKey(BigInteger d, BigInteger e, BigInteger n) {
		this.d = d;
		this.e = e;
		this.n = n;
	}

	/**
	 * 私钥
	 * 
	 * @return
	 */
	public String getD() {
		return d.toString(KeyGenerator.BIGINTENCODING);
	}

	/**
	 * 公钥
	 * 
	 * @return
	 */
	public String getE() {
		return e.toString(KeyGenerator.BIGINTENCODING);
	}

	/**
	 * 摸
	 * 
	 * @return
	 */
	public String getN() {
		return n.toString(KeyGenerator.BIGINTENCODING);
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

	/**
	 * 用私钥解密
	 * 
	 * @param m
	 * @return
	 */
	public synchronized BigInteger decrypt(BigInteger m) {
		return m.modPow(d, n);
	}

	@Override
	public int hashCode() {
		return d.hashCode() ^ e.hashCode() ^ n.hashCode();
	}

	public boolean equals(Object o) {
		if (!(o instanceof PrivateKey))
			return false;
		PrivateKey k = (PrivateKey) o;
		if (!this.d.equals(k.d))
			return false;
		if (!this.e.equals(k.e))
			return false;
		if (!this.n.equals(k.n))
			return false;
		return true;
	}

}
