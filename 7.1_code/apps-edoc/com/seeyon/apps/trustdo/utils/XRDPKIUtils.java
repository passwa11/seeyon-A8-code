package com.seeyon.apps.trustdo.utils;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.logging.Log;

//import com.seeyon.apps.ocip.util.OcipEdocUtil;
import com.seeyon.ctp.common.log.CtpLogFactory;

/**
 * 加解密工具类
 * @author zhaopeng
 *
 */
public class XRDPKIUtils {

	private static final Log LOGGER = CtpLogFactory.getLog(XRDPKIUtils.class);
	/** 对称加密 */
	public static byte[] getECBResult(byte[] mToEncode, byte[] desKey) {
		byte[] data = null;
		try {
			SecretKeyFactory keyfactory = SecretKeyFactory
					.getInstance("DESede");
			DESedeKeySpec keyspec = new DESedeKeySpec(desKey);
			SecretKey deskey = keyfactory.generateSecret(keyspec);
			// 不建议使用ECB ？模式是否需要修改
			// http://www.cnblogs.com/alisecurity/p/5312083.html
			Cipher cpher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cpher.init(Cipher.ENCRYPT_MODE, deskey);
			data = cpher.doFinal(mToEncode);
		} catch (Exception e) {
			LOGGER.error(e);
			//e.printStackTrace();
		}
		return data;
	}

	/**
	 * 对称解密算法
	 * 
	 * @param content
	 * @return
	 */

	public static byte[] decrypt(byte[] data, byte[] desKey) {
		try {

			SecretKeyFactory keyfactory = SecretKeyFactory
					.getInstance("DESede");
			DESedeKeySpec keyspec = new DESedeKeySpec(desKey);
			SecretKey deskey = keyfactory.generateSecret(keyspec);

			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, deskey);
			return cipher.doFinal(data);
		} catch (Exception ex) {
			// 解密失败，打日志
			LOGGER.error(ex);
			//ex.printStackTrace();
		}
		return null;
	}

	/**
	 * RSA加密
	 * 
	 * @param str
	 * @param publicKeyBase64
	 * @return 加密信息
	 */
	public static byte[] EncRSA(String str, byte[] publicKeyByte) {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyByte);
		byte[] plainText = null;
		try {
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publicKey = keyFactory.generatePublic(keySpec);
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			plainText = cipher.doFinal(str.getBytes());
		} catch (Exception e) {
			LOGGER.error(e);
			//e.printStackTrace();
		} 
		return plainText;
	}

}
