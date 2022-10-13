package com.gioneco.focus.judge.util;

import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author DJ
 * @className ImageBufferUtil
 * @Description
 * @date 2022-09-30 13:53
 */
@Slf4j
public class ImageBufferUtil {
    /**
     * BufferedImage 编码转换为 base64
     * @param bufferedImage
     * @return
     */
    public static String BufferedImageToBase64(BufferedImage bufferedImage) {
        //io流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            //写入流中
            ImageIO.write(bufferedImage, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //转换成字节
        byte[] bytes = baos.toByteArray();
        BASE64Encoder encoder = new BASE64Encoder();
        //转换成base64串
        String png_base64 = encoder.encodeBuffer(bytes).trim();
        //删除 \r\n
        png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");
//        System.out.println("data:image/jpg;base64," + png_base64);
        return  png_base64;
    }
    
    /**
     * 将BufferedImage转换为InputStream
     * @param image
     * @return
     */
    public static InputStream bufferedImageToInputStream(BufferedImage image){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", os);
            InputStream input = new ByteArrayInputStream(os.toByteArray());
            return input;
        } catch (IOException e) {
            log.error("提示:",e);
        }
        return null;
    }
    
    /**
     * base64 编码转换为 BufferedImage
     * @param base64
     * @return
     */
    public  static BufferedImage base64ToBufferedImage(String base64) {
        BASE64Decoder decoder = new sun.misc.BASE64Decoder();
        try {
            byte[] bytes1 = decoder.decodeBuffer(base64);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes1);
            return ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 对图片进行反色处理
     */
    public static BufferedImage imageOpposite(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        int srcRGBs[] = image.getRGB(0, 0, width, height, null, 0, width);
        int rgb[]=new int[3];
        int rs[][]=new int[width][ height];
        int gs[][]=new int[width][ height];
        int bs[][]=new int[width][ height];
        BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for(int j=0; j<height; j++) {
            for (int i = 0; i < width; i++) {
                ImageUtil.decodeColor(srcRGBs[j*width+i],rgb); //rgb[0]=R,rgb[1]=G,rgb[2]=B
                rs[i][j]=rgb[0];   //Rֵ
                gs[i][j]=rgb[1];   //Gֵ
                bs[i][j]=rgb[2];   //bֵ
            }
        }
        for (int j = 0; j < height; j++) {
            for(int i=0; i<width; i++) {
                rgb[0]=(int)(255-rs[i][j]);//用255减去原图像的像素值就是反色的图像
                rgb[1]=(int)(255-gs[i][j]);
                rgb[2]=(int)(255-bs[i][j]);
                destImage.setRGB(i,j, ImageUtil.encodeColor(rgb));
            }
        }
        
        return destImage;
    }
    
    
    /**
     * 对直方图进行均衡化
     * @param leftImage
     * @return
     */
    public static BufferedImage imageHistogram(BufferedImage leftImage) {
        int width = leftImage.getWidth();
        int height = leftImage.getHeight();
        int srcRGBs[] = leftImage.getRGB(0, 0, width, height, null, 0, width);
        int rgb[]=new int[3];
        int rgb1[]=new int[3];
        BufferedImage destImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        double count0[] = new double[256];
        double count1[] = new double[256];
        double count2[] = new double[256];
        for (int j = 0; j < height; j++) {
            for(int i=0; i<width; i++) {
                
                ImageUtil.decodeColor(srcRGBs[j*width+i],rgb);
                if (count0[rgb[0]]==0){
                    count0[rgb[0]]=1;
                }else {
                    count0[rgb[0]]+=1; //统计各灰度值在整张图片中出现的个数
                }
                if (count1[rgb[1]]==0){
                    count1[rgb[1]]=1;
                }else {
                    count1[rgb[1]]+=1;
                }
                if (count2[rgb[2]]==0){
                    count2[rgb[2]]=1;
                }else {
                    count2[rgb[2]]+=1;
                }
            }
        }
        
        double gl0[] = new double[256];
        for (int i=0;i<256;i++){
            gl0[i]=count0[i]/(width*height);  //统计该灰度值所占整体的比例
        }
        double gl1[] = new double[256];
        for (int i=0;i<256;i++){
            gl1[i]=count1[i]/(width*height);
        }
        double gl2[] = new double[256];
        for (int i=0;i<256;i++){
            gl2[i]=count2[i]/(width*height);
        }
        
        double sk0[]=new double[256];
        for (int i=0;i<256;i++){
            for (int j=0;j<=i;j++){
                sk0[i]+=gl0[j];   //统计累计直方图,即累计百分比
            }
        }
        double sk1[]=new double[256];
        for (int i=0;i<256;i++){
            for (int j=0;j<=i;j++){
                sk1[i]+=gl1[j];
            }
        }
        double sk2[]=new double[256];
        for (int i=0;i<256;i++){
            for (int j=0;j<=i;j++){
                sk2[i]+=gl2[j];
            }
        }
        
        double Sk0[]=new double[256];
        for (int i=0;i<256;i++){
            Sk0[i]=((255)*sk0[i]+0.5);  //记录该像素值变换后的新像素值
        }
        double Sk1[]=new double[256];
        for (int i=0;i<256;i++){
            Sk1[i]=((255)*sk1[i]+0.5);
        }
        double Sk2[]=new double[256];
        for (int i=0;i<256;i++){
            Sk2[i]=((255)*sk2[i]+0.5);
        }
        
        
        for (int j=0;j<height;j++){
            for(int i=0;i<width;i++){
                ImageUtil.decodeColor(srcRGBs[j*width+i],rgb);
                rgb1[0]=(int)Sk0[rgb[0]];  //将新像素值赋给原本的像素值
                rgb1[1]=(int)Sk1[rgb[1]];
                rgb1[2]=(int)Sk2[rgb[2]];
                destImage.setRGB(i,j, ImageUtil.encodeColor(rgb1));
            }
        }
        return destImage;
    }
    
    
}
