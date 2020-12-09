package com.seeyon.ctp.common.barCode.uitl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.BeansOfTypeListener;
import com.seeyon.ctp.common.barCode.manager.BarCodeTypeManager;
import com.seeyon.ctp.common.barCode.vo.BarCodeParamVo;
import com.seeyon.ctp.dubbo.RefreshInterfacesAfterUpdate;
import com.seeyon.ctp.util.Strings;

/**
 * 二维码组件，采用google开源项目zxing
 *
 * @author wangfeng
 */
public class BarCodeUtil {

    /**
     * 生成图片的格式
     */
    public static final String FORMAT = "png";

    public static final String CHARSET = "UTF-8";

    public static final int CONTENT_MAX_LENGTH = 1000;

//	public static void main(String[] args)throws Exception{
//		File file = new File("D://qrcodeImage.png");
//		String contents = "你好";
//		encode(contents,new java.io.FileOutputStream(file));
//		String str = decode(new java.io.FileInputStream(file));
//		System.out.println(str);
//	}

    private static Map<String, BarCodeTypeManager> barCodeTypeManagerMap = new HashMap<String, BarCodeTypeManager>();

    static {
        init();
    }


    public static void init() {
        fillBarCodeTypeManager();
        AppContext.addBeansOfTypeListener(BarCodeTypeManager.class, new BeansOfTypeListener() {
            @Override
            public void onChange(Class clazz) {
                fillBarCodeTypeManager();
            }
        });
    }

    private static void fillBarCodeTypeManager() {
        barCodeTypeManagerMap.clear();
        Map<String, BarCodeTypeManager> map = AppContext.getBeansOfType(BarCodeTypeManager.class);
        for (Map.Entry<String, BarCodeTypeManager> entry : map.entrySet()) {
            BarCodeTypeManager manager = entry.getValue();
            barCodeTypeManagerMap.put(manager.getType(), manager);
        }
    }

    /**
     * 根据type key获取对应的实现类，
     * 如果不存在，则返回默认实现
     *
     * @param typeKey key
     * @return 实现
     */
    public static BarCodeTypeManager getTypeManager(String typeKey) {
        return getTypeManager(typeKey, true);
    }

    /**
     * 根据 type key 获取对应的实现类
     *
     * @param typeKey     key
     * @param needDefault 是否需要默认实现，当根据key查询对应的实现时，不存在时,否则返回null
     * @return 实现
     */
    public static BarCodeTypeManager getTypeManager(String typeKey, boolean needDefault) {

        BarCodeTypeManager bar = barCodeTypeManagerMap.get(typeKey);
        if (bar == null && needDefault) {
            bar = barCodeTypeManagerMap.get(BarCodeTypeManager.BASE_BAR_CODE_TYPE_KEY);
        }
        return bar;
    }

    /**
     * 为指定的内容生成二维码
     *
     * @param contents 内容
     * @throws WriterException
     * @throws IOException
     */
    public static void encode(String contents, BarCodeParamVo paramVo, OutputStream out) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(contents, paramVo.getBarcodeFormat(), paramVo.getWidth(), paramVo.getHeight(), paramVo.getHintParam());
        MatrixToImageWriter.writeToStream(matrix, paramVo.getFileExt(), out, new MatrixToImageConfig());
    }

    /**
     * 为指定的内容生成二维码
     *
     * @param contents 内容
     * @param f        生成的文件
     * @throws WriterException
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    public static void encode(String contents, BarCodeParamVo paramVo, File f) throws WriterException, IOException {
        Map<EncodeHintType, Object> typeMap = (Map<EncodeHintType, Object>) paramVo.getHintParam();
        try {
            QRCodeUtil.encode(contents, paramVo.getLogoPath(), true, f);
        } catch (Exception e) {
            e.printStackTrace();
        }
//	    BitMatrix matrix = new MultiFormatWriter().encode(contents, paramVo.getBarcodeFormat(), paramVo.getWidth(), paramVo.getHeight(), paramVo.getHintParam());
//		if (Strings.isNotBlank(paramVo.getLogoPath())) {//生成带logo的二维码
//
//		    BarLogo.writeToFile(matrix, paramVo.getFileExt(), f, paramVo.getLogoPath(), paramVo.getLogoWidth(), paramVo.getLogoHeight());
//		} else {
//	        MatrixToImageWriter.writeToFile(matrix, paramVo.getFileExt(), f,new MatrixToImageConfig());
//		}
    }

    /**
     * 根据输入流读取二维码内容
     *
     * @param in 二维码图片输入流
     * @throws IOException
     * @throws NotFoundException
     */
    public static String decode(InputStream in) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(in);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Map<DecodeHintType, String> hints = new HashMap<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, CHARSET);
        Result result = new MultiFormatReader().decode(bitmap, hints);
        return result.toString();
    }

    /**
     * 校验字符串长度是否超过二维码支持的最大长度
     *
     * @param content 字符串
     * @return true 超过
     */
    @Deprecated
    public static boolean checkContentLength(String content) {
        return getStringLength(content, false) > CONTENT_MAX_LENGTH;
    }

    /**
     * 判断生成的内容是否大于自定义的长度限制
     *
     * @param content
     * @param maxLength
     * @return
     */
    public static boolean checkContentLength(String content, int maxLength) {
        return getStringLength(content, false) > maxLength;
    }

    /**
     * 获取字符串长度
     *
     * @param content    字符串
     * @param needFormat 是否需要做特殊处理：中文按照3个字符处理
     * @return 字符串长度
     */
    public static int getStringLength(String content, boolean needFormat) {
        if (Strings.isBlank(content)) {
            return 0;
        }
        if (needFormat) {
            int len = 0;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                //中文字符按照3个字符长度算
                if (c <= 255) {
                    len += 1;
                } else {
                    len += 3;
                }
            }
            return len;
        }
        return content.length();
    }
}
