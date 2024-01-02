package com.tongtech.common.utils;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加解密
 *
 */
public class AESUtils {

	/*
	 * 加密用的Key 可以用26个字母和数字组成 使用AES-128-CBC加密模式，key需要为16位。
	 */
	private static final String key="hj7x89H$yuBI0456";
	private static final String iv ="NIfb&95GUY86Gfgh";
	/**
	 * @Description AES算法加密明文
	 * @param data 明文
	 * @return 密文
	 */
	public static String encryptAES(String data)  {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			int blockSize = cipher.getBlockSize();
			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;

			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
			}

			byte[] plaintext = new byte[plaintextLength];
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());  // CBC模式，需要一个向量iv，可增加加密算法的强度

			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);

			return new Base64().encodeToString(encrypted); // BASE64做转码。

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @Description AES算法解密密文
	 * @param data 密文
	 * @return 明文
	 */
	public static String decryptAES(String data){
		try
		{
			byte[] encrypted1 =  new Base64().decode(data);//先用base64解密

			Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
			SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
			IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);// 初始化
			byte[] original = cipher.doFinal(encrypted1);
			String originalString = new String(original);
			return originalString.trim();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*public static void main(String[] args) {
		try {
			boolean matches = "Apassword123_@".matches(Constants.PASSWORD_RULE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/


	/*public static void main(String[] args) {
		String s = "^[\\u4e00-\\u9fa5_a-zA-Z0-9]+[\\u4e00-\\u9fa5_a-zA-Z0-9]$";
		String ss = "^(?!,)(?!.*?,$)(?!，)(?!.*?，$)+$";
		String s1 = "asjksdfhksdl";
		boolean matches = s1.matches(ss);

	}*/
}
