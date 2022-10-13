package com.gioneco.focus.judge.domain;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import com.gioneco.focus.judge.protobuf.SenderRequestMsg;
import com.gioneco.focus.judge.util.ImageBufferUtil;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author DJ
 * @className PackageDataImageView
 * @Description 包裹图片画布
 * @date 2022-06-13 16:40
 */

@Slf4j
public class PackageDataCanvas extends ImageView {
    
    @Getter
    private String packageId;
    
    private final List<ImageView> childes = new ArrayList<>();
    
    private PixelWriter pw;
    
    private WritableImage wi2;
    
    private final Label contrabandType = new Label();
    
    private boolean isShow = false;
    
    @Getter
    private final List<ContrabandInfo> contrabandInfos = new ArrayList<>(32);
    
    private PackageImageAnchorPane parentAp;
    

    
    public PackageDataCanvas() {
    
    }
    
    public PackageDataCanvas(String packageId, PackageImageAnchorPane parentAp) {
        this.packageId = packageId;
        this.parentAp = parentAp;
    }
    
   
    public void addLineImage(LineDataImageView lineDataImageView) {
        childes.add(lineDataImageView);
    }
    
    public void show() {
        if (CollUtil.isNotEmpty(childes) && !isShow) {
            double width = childes.stream().collect(Collectors.summarizingDouble(it -> it.getImage().getWidth())).getSum();
            double height = childes.get(0).prefHeight(-1);
            setFitWidth(width);
            setFitHeight(height);
            WritableImage wi = new WritableImage((int) width, (int) height);
            pw = wi.getPixelWriter();
            //将所有列的图片复制到该视图中
            AtomicInteger xAtomicInt = new AtomicInteger();
            childes.forEach(it -> {
                Image image = it.getImage();
                if (it.getImage().isError()) {
                    Exception exception = it.getImage().getException();
                    log.error("画图错误", exception);
                }
                PixelReader pr = image.getPixelReader();
                if (pr != null) {
                    //开始写入像素
                    pw.setPixels(xAtomicInt.get(), 0, (int) it.getImage().getWidth(), (int) it.prefHeight(-1), pr, 0, 0);
                    xAtomicInt.addAndGet((int) it.getImage().getWidth());
                } else {
                    log.error("出现空列");
                }
            });
            //将写完的图片放在
            setImage(wi);
            wi2 = wi;
            //设置坐标为第一个元素的
            setTranslateX(childes.get(0).getTranslateX());
            setTranslateY(childes.get(0).getTranslateY());
            mouseClick();
            isShow = true;
            parentAp.setStartTime(System.currentTimeMillis());
        }
        
    }
    
    
    public void drawContraband(SenderRequestMsg.ContrabandInfoMsg info) {
        ContrabandInfo contrabandInfo = new ContrabandInfo(info);
        contrabandInfos.add(contrabandInfo);
        Frame frame = new Frame(contrabandInfo,this);
        drawFrame(frame);
//        log.info("计算  id = {} : x1 = {},y1 = {},x2 = {}, y2 = {}", packageId, x1, y1, x2, y2);
    }
    
    public void drawContraband(List<SenderRequestMsg.ContrabandInfoMsg> mainInfoList) {
        Platform.runLater(() -> {
            List<String> collect = mainInfoList.stream().map(SenderRequestMsg.ContrabandInfoMsg::getType).collect(Collectors.toList());
            contrabandType.setText(String.join(",", collect));
            contrabandType.setFont(Font.font("Fong Song", 16));
            contrabandType.setTextFill(Color.RED);
        });
        mainInfoList.forEach(this::drawContraband);
    }
    
    public void mouseClick() {
        Image image = getImage();
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                if (!parentAp.isUnpacked()) {
                    //获取鼠标(x,y)坐标
                    double x = event.getX();
                    double y = event.getY();
                    //鼠标位置前后各增加50px
                    int x1 = (int) NumberUtil.sub(x, 50);
                    int y1 = (int) NumberUtil.sub(y, 50);
                    int x2 = (int) NumberUtil.add(x, 50);
                    int y2 = (int) NumberUtil.add(y, 50);
                    if (x2 > image.getWidth()) {
                        x2 = (int) image.getWidth();
                    }
                    if (y2 > image.getHeight()) {
                        y2 = (int) image.getHeight();
                    }
                    if (x1 < 0) {
                        x1 = 0;
                    }
                    if (y1 < 0) {
                        y1 = 0;
                    }
                    Frame frame = new Frame(x1, y1, x2, y2);
                    contrabandInfos.add(new ContrabandInfo(frame,this));
                    parentAp.unpack();
                    drawFrame(frame);
                } else {
                    log.info("包裹{}已经开包", packageId);
                }
            }
        });
    }
    
    /**
     * 画违禁品框
     *
     */
    private void drawFrame(Frame frame) {
        Platform.runLater(() -> {
            for (int i = 0; i < getFitWidth(); i++) {
                for (int j = 0; j < getFitHeight(); j++) {
                    //左边界
                    if (i == frame.getX1() && j >= frame.getY1() && j <= frame.getY2()) {
                        pw.setColor(i, j, Color.RED);
                    }
                    //右边界
                    if (i == frame.getX2() && j >= frame.getY1() && j <= frame.getY2()) {
                        pw.setColor(i, j, Color.RED);
                    }
                    //上边界
                    if (j == frame.getY1() && i >= frame.getX1() && i <= frame.getX2()) {
                        pw.setColor(i, j, Color.RED);
                    }
                    //下边界
                    if (j == frame.getY2() && i >= frame.getX1() && i <= frame.getX2()) {
                        pw.setColor(i, j, Color.RED);
                    }
                }
            }
        });
    }
    
    /**
     * 临时图片(不包含违禁品标注框)
     * @return 临时图片
     */
    public Image temporaryImage() {
        Image image = this.getImage();
        WritableImage wi = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelWriter pixelWriter = wi.getPixelWriter();
        PixelReader pr = image.getPixelReader();
        pixelWriter.setPixels(0, 0, (int) image.getWidth(), (int) image.getHeight(), pr, 0, 0);
        return wi;
    }
    
}
