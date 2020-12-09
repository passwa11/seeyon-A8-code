package com.seeyon.ctp.common.barCode.uitl;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.zxing.common.BitMatrix;

public class BarLogo {

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * 生成带logo的二维码
     *
     * @param matrix   二维码矩阵相关
     * @param format   二维码图片格式
     * @param file     二维码图片文件
     * @param logoPath logo路径
     * @throws IOException
     */
    public static void writeToFile2(BitMatrix matrix, String format, File file, String logoPath, int logoWidth, int logoHeight) throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        Graphics2D gs = image.createGraphics();

        /**
         * 新增读取classpath下的图片
         * 路径必须以classpath:开头
         */
        InputStream inputStream;
        if (logoPath.contains("classpath:")) {
            logoPath = logoPath.substring(10);
            inputStream = BarLogo.class.getClassLoader().getResourceAsStream(logoPath);
        } else {
            inputStream = new FileInputStream(new File(logoPath));
        }
        Image img = ImageIO.read(inputStream);
        int x = (matrix.getWidth() - logoWidth) / 2;
        int y = (matrix.getHeight() - logoHeight) / 2;
        gs.drawImage(img, x, y, logoWidth, logoHeight, null);
        Shape shape = new RoundRectangle2D.Float(x, y, logoWidth, logoWidth, 6, 6);
        gs.setStroke(new BasicStroke(3f));
        gs.draw(shape);
        gs.dispose();
        img.flush();
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    public static void writeToFile(BitMatrix matrix, String format, File file, String logoPath, int logoWidth, int logoHeight) throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        try {
            QRCodeUtil.insertImage(image, logoPath, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }

    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }

        return image;
    }

}
