package com.seeyon.apps.common.kit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Description
 * 
 * <pre>
 * base64转换成图片或者文件
 * </pre>
 * 
 * Date 2019年4月4日 上午9:16:44<br>
 * Copyright(c) Beijing Seeyon Software Co.,LTD
 */
public class Base64Kit {
	
	static BASE64Encoder encoder = new sun.misc.BASE64Encoder();    
    static BASE64Decoder decoder = new sun.misc.BASE64Decoder();  

	public static String getBase64(File file) {
		FileInputStream fin = null;
		BufferedInputStream bin = null;
		ByteArrayOutputStream baos = null;
		BufferedOutputStream bout = null;
		try {
			// 建立读取文件的文件输出流
			fin = new FileInputStream(file);
			// 在文件输出流上安装节点流（更大效率读取）
			bin = new BufferedInputStream(fin);
			// 创建一个新的 byte 数组输出流，它具有指定大小的缓冲区容量
			baos = new ByteArrayOutputStream();
			// 创建一个新的缓冲输出流，以将数据写入指定的底层输出流
			bout = new BufferedOutputStream(baos);
			byte[] buffer = new byte[1024];
			int len = bin.read(buffer);
			while (len != -1) {
				bout.write(buffer, 0, len);
				len = bin.read(buffer);
			}
			// 刷新此输出流并强制写出所有缓冲的输出字节，必须这行代码，否则有可能有问题
			bout.flush();
			byte[] bytes = baos.toByteArray();
			// sun公司的API
			return encoder.encodeBuffer(bytes).trim();
			// apache公司的API
			// return Base64.encodeBase64String(bytes);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fin.close();
				bin.close();
				// 关闭 ByteArrayOutputStream 无效。此类中的方法在关闭此流后仍可被调用，而不会产生任何 IOException
				// baos.close();
				bout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 将base64编码转换成PDF
	 * 
	 * @param base64sString
	 *            1.使用BASE64Decoder对编码的字符串解码成字节数组
	 *            2.使用底层输入流ByteArrayInputStream对象从字节数组中获取数据；
	 *            3.建立从底层输入流中读取数据的BufferedInputStream缓冲输出流对象；
	 *            4.使用BufferedOutputStream和FileOutputSteam输出数据到指定的文件中
	 */
	public static void base64StringToFile(String base64sString, String path) {
		BufferedInputStream bin = null;
		FileOutputStream fout = null;
		BufferedOutputStream bout = null;
		try {
			// 将base64编码的字符串解码成字节数组
			byte[] bytes = decoder.decodeBuffer(base64sString);
			// apache公司的API
			// byte[] bytes = Base64.decodeBase64(base64sString);
			// 创建一个将bytes作为其缓冲区的ByteArrayInputStream对象
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			// 创建从底层输入流中读取数据的缓冲输入流对象
			bin = new BufferedInputStream(bais);
			// 指定输出的文件http://m.nvzi91.cn/nxby/29355.html
			File file = new File(path);
			// 创建到指定文件的输出流
			fout = new FileOutputStream(file);
			// 为文件输出流对接缓冲输出流对象
			bout = new BufferedOutputStream(fout);
			byte[] buffers = new byte[1024];
			int len = bin.read(buffers);
			while (len != -1) {
				bout.write(buffers, 0, len);
				len = bin.read(buffers);
			}
			// 刷新此输出流并强制写出所有缓冲的输出字节，必须这行代码，否则有可能有问题
			bout.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bin.close();
				fout.close();
				bout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @Description: 将base64编码字符串转换为图片
	 * @Author:
	 * @CreateTime:
	 * @param imgStr
	 *            base64编码字符串
	 * @param path
	 *            图片路径-具体到文件
	 * @return
	 */
	public static boolean generateImage(String imgStr, String path) {
		if (imgStr == null)
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// 解密
			byte[] b = decoder.decodeBuffer(imgStr);
			// 处理数据
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {
					b[i] += 256;
				}
			}
			OutputStream out = new FileOutputStream(path);
			out.write(b);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * @Description: 根据图片地址转换为base64编码字符串
	 * @Author:
	 * @CreateTime:
	 * @return
	 */
	public static String getImageStr(String imgFile) {
		InputStream inputStream = null;
		byte[] data = null;
		try {
			inputStream = new FileInputStream(imgFile);
			data = new byte[inputStream.available()];
			inputStream.read(data);
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 加密
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data);
	}

}
