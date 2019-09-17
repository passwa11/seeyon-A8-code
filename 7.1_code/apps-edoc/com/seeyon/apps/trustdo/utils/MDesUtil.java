package com.seeyon.apps.trustdo.utils;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.util.Base64;
import com.seeyon.ctp.common.exceptions.BusinessException;

  
/** 
* 3DES加密工具类 
 */  
public class MDesUtil {  
    private static final Log log = LogFactory.getLog(MDesUtil.class);
    // 密钥  
    private final static String secretKey = "m1yanfa@seeyon.com119$#M1#$";  
    private final static String iv = "01234567";  
    private final static String encoding = "utf-8";  
  
	/** 
     * DES加密 
     *  
     * @param plainText 普通文本 
     * @return 
     * @throws Exception  
     */  
    public static String encode(String plainText) throws BusinessException {  
        try{
                Key deskey = null;  
                DESedeKeySpec spec = new DESedeKeySpec(secretKey.getBytes());  
                SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");  
                deskey = keyfactory.generateSecret(spec);  
          
                Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");  
                IvParameterSpec ips = new IvParameterSpec(iv.getBytes());  
                cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);  
                byte[] encryptData = cipher.doFinal(plainText.getBytes(encoding));  
                return Base64.encode(encryptData);  
            } catch(Exception e){
                log.error(e.getLocalizedMessage(), e);
            }
        return null;
    }  
  
    /** 
     * DES解密 
     *  
     * @param encryptText 加密文本 
     * @return 
     * @throws Exception 
     */  
    public static String decode(String encryptText) throws BusinessException {  
        try{
            Key deskey = null;  
            DESedeKeySpec spec = new DESedeKeySpec(secretKey.getBytes());  
            SecretKeyFactory keyfactory = SecretKeyFactory.getInstance("desede");  
            deskey = keyfactory.generateSecret(spec);  
            Cipher cipher = Cipher.getInstance("desede/CBC/PKCS5Padding");  
            IvParameterSpec ips = new IvParameterSpec(iv.getBytes());  
            cipher.init(Cipher.DECRYPT_MODE, deskey, ips);  
            byte[] decryptData = cipher.doFinal(Base64.decode(encryptText));  
            return new String(decryptData, encoding);  
        } catch(Exception e){
        	log.error(encryptText +"1");
            log.error(e.getLocalizedMessage(), e);
        }
        return encryptText;
    }  
    
}