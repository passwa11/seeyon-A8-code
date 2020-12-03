package com.seeyon.cap4.form.bean.fieldCtrl.utils;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ImgUtil {

    private static final long MAX_SIZE = 200 * 1024L;

    private static final double SCALE = 0.8;

    /**
     * 压缩文件到MAX_SIZE以下
     * @param source
     * @throws IOException
     */
    public static void compress(File source,File target) throws IOException {
        if (!source.exists() || !source.isFile()) {
            throw new IllegalArgumentException("无效的文件：" + source.getAbsolutePath());
        }
        if(MAX_SIZE < source.length()){
            BufferedImage input = ImageIO.read(source);
            int width = input.getWidth();
            int height = input.getHeight();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BufferedImage output;
            int outSize ;
            do {
                width = (int) (width * SCALE);
                height = (int) (height * SCALE);
                output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = output.createGraphics();
                g2d.setBackground(Color.WHITE);
                g2d.fillRect(0, 0, width, height);
                g2d.drawImage(input, 0, 0, width, height, null);
                os.reset();
                ImageIO.write(output,"jpg",os);
                outSize = os.toByteArray().length;
            } while (MAX_SIZE < outSize);
            os.close();
            ImageIO.write(output,"jpg",target);
        }else{
            FileUtils.copyFile(source,target);
        }
    }
}
