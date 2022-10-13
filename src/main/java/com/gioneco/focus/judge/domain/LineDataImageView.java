package com.gioneco.focus.judge.domain;


import com.gioneco.focus.judge.protobuf.SenderRequestMsg;
import com.gioneco.focus.judge.util.ImageBufferUtil;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author DJ
 * @className LinaDataImageView
 * @Description
 * @date 2022-06-13 16:39
 */
@Slf4j
public class LineDataImageView extends ImageView {
    public static final int LINE_DATA_START_FLAG = 1;
    public static final int LINE_DATA_MID_FLAG = 2;
    public static final int LINE_DATA_END_FLAG = 3;
    
    private int flag;
    
    private int line;
    
    public LineDataImageView() {
    
    }
    
    public static LineDataImageView buildMain(SenderRequestMsg.ImageMsg msg) {
        Image mainImage = byteConvertImage(msg.getMainData().toByteArray(), true);
        LineDataImageView lineDataImageView = new LineDataImageView(mainImage, msg.getLineFlag());
        lineDataImageView.setTranslateY(110);
        lineDataImageView.setSmooth(true);
        lineDataImageView.setFitHeight(350);
        lineDataImageView.setFitWidth(3);
        lineDataImageView.setLine(msg.getLineId());
        return lineDataImageView;
    }
    
    public static LineDataImageView buildSide(SenderRequestMsg.ImageMsg msg) {
        if (msg != null) {
            byte[] bytes = msg.getSideData().toByteArray();
            if (bytes != null && bytes.length != 0) {
                Image sideImage = byteConvertImage(bytes, false);
                LineDataImageView lineDataImageView = new LineDataImageView(sideImage, msg.getLineFlag());
                lineDataImageView.setTranslateY(700);
                lineDataImageView.setSmooth(true);
                lineDataImageView.setFitHeight(150);
                lineDataImageView.setFitWidth(3);
                lineDataImageView.setLine(msg.getLineId());
                return lineDataImageView;
            }
        }
        return null;
        
    }
    
    public LineDataImageView(String url) {
        super(url);
    }
    
    public LineDataImageView(Image image, int flag) {
        super(image);
        this.flag = flag;
    }
    
    public boolean isStartLine() {
        return flag == LINE_DATA_START_FLAG;
    }
    
    public boolean isEndLine() {
        return flag == LINE_DATA_END_FLAG;
    }
    
    public int getFlag() {
        return flag;
    }
    
    private static Image byteConvertImage(byte[] bytes, boolean isMain) {
        try (
                InputStream is = new ByteArrayInputStream(bytes);
        ) {
//            BufferedImage bImageFromConvert = ImageIO.read(in);
//            BufferedImage bufferedImage = ImageBufferUtil.imageOpposite(bImageFromConvert);
//            InputStream is = ImageBufferUtil.bufferedImageToInputStream(bufferedImage);
            if (is != null) {
                if (isMain) {
                    return new Image(is, 3, 350, false, true);
                } else {
                    return new Image(is, 3, 150, false, true);
                }
            }
//            return new Image(is);
        } catch (IOException e) {
            log.error("列数据转换错误",e);
        }
        return null;
    }
    
    public void setLine(int line) {
        this.line = line;
    }
    
    public int getLine() {
        return line;
    }
}
