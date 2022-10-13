package com.gioneco.focus.judge.domain;

import cn.hutool.core.util.StrUtil;
import com.gioneco.focus.judge.client.controller.MainController;
import com.gioneco.focus.judge.protobuf.ReceiverRequestMsg;
import com.gioneco.focus.judge.util.BeanUtil;
import com.gioneco.focus.judge.util.ImageBufferUtil;
import com.gioneco.focus.judge.util.ThreadUtils;
import impl.jfxtras.styles.jmetro.ProgressBarSkin;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.gioneco.focus.judge.util.GlobalConstant.CLOSE_ACTION_PASS;
import static com.gioneco.focus.judge.util.GlobalConstant.CLOSE_ACTION_UNPACK;


/**
 * @author DJ
 * @className PackageImage
 * @Description 包裹图片布局
 * @date 2022-09-26 16:56
 */
@Slf4j
public class PackageImageAnchorPane extends AnchorPane {
    
    private TranslateTransition packageImageTranslateTransition;
    private TranslateTransition locationLabelTranslateTransition;
    
    /**
     * 父级容器
     */
    private Pane parent;
    
    /**
     * 包裹ID
     */
    private String packageId;
    
    /**
     * 位置信息
     */
    private Label locationLabel;
    
    
    /**
     * 主视角画布
     */
    private PackageDataCanvas mainCanvas;
    /**
     * 侧视角画布
     */
    private PackageDataCanvas sideCanvas;
    
    /**
     * 主视角完整图片Image(不含标注框)
     */
    private Image mainImage;
    /**
     * 侧视角完整图片Image(不含标注框)
     */
    private Image sideImage;
    
    /**
     * 包裹移动距离
     */
    private double x = 10;
    
    /**
     * 是否已经开包
     */
    private boolean unpacked = false;
    
    /**
     * 开包放行标志
     */
    private final Label unpackLabel = new Label();
    
    /**
     * 接收完成后,判图开始时间
     */
    @Setter
    private long startTime = 0;
    
    private final static int OVERTIME = 5000;
    
    private MainController mainController;
    
    /**
     * 接收完成时的动作
     */
    @Setter
    private String closeAction;
    
    /**
     * 主视角完成状态
     */
    private boolean mainCompleted = false;
    
    /**
     * 俯视角完成状态
     */
    private boolean sideCompleted = false;
    
    public PackageImageAnchorPane(MainController mainController) {
        this.mainController = mainController;
    }
    
    public PackageImageAnchorPane() {
    }
    
    public PackageImageAnchorPane(Node... children) {
        super(children);
    }
    
    public void initLayout(Pane parent, String packageId) {
        this.parent = parent;
        this.packageId = packageId;
        setTranslateX(parent.prefWidth(-1));
        setTranslateY(20);
        setPrefWidth(1000);
        setPrefHeight(900);
        setBorderColor("red");
        packageImageTranslateTransition = new TranslateTransition(Duration.millis(500), this);
        mainCanvas = new PackageDataCanvas(packageId, this);
        sideCanvas = new PackageDataCanvas(packageId, this);
        getChildren().add(mainCanvas);
        getChildren().add(sideCanvas);
        showStationText(packageId);
        parent.getChildren().add(this);
        mouseAction();
    }
    
    
    public void handlePackage(LineDataImageView main, LineDataImageView side) {
        Platform.runLater(() -> {
            if (main != null) {
                main.setTranslateX(x);
                getChildren().add(main);
                x += main.prefWidth(-1);
                //接收最后一列
                if (main.isEndLine()) {
                    handleSingleCompleted(true);
                }
                mainCanvas.addLineImage(main);
            }
        });
        
        Platform.runLater(() -> {
            if (side != null) {
                side.setTranslateX(x);
                getChildren().add(side);
                sideCanvas.addLineImage(side);
                if (side.isEndLine()) {
                    handleSingleCompleted(false);
                }
            } else {
                handleSingleCompleted(false);
            }
        });
    }
    
    
    public void setBorderColor(String color) {
        setStyle("-fx-border-style: solid;" +
                "-fx-border-color: " + color + ";" +
                " -fx-border-width: 3;" +
                " -fx-border-radius: 10");
    }
    
    public void cancelBorder() {
        setStyle(null);
    }
    
    /**
     * 鼠标移动
     */
    public void mouseAction() {
        setOnMouseEntered(event -> {
            setBorderColor("blue");
        });
        
        //鼠标退出node
        setOnMouseExited(event -> {
            cancelBorder();
        });
    }
    
    /**
     * 超出边界
     *
     * @return
     */
    private boolean outOfBounds(double limit) {
        if (getTranslateX() + prefWidth(-1) < limit) {
            parent.getChildren().removeIf(node -> node == this);
            return true;
        }
        
        return false;
    }
    
    /**
     * 移动空白
     *
     * @param distance
     * @return
     */
    public void movingEmpty(double distance) {
        packageImageTranslateTransition.setFromX(getTranslateX());
        packageImageTranslateTransition.setByX(-distance);
        packageImageTranslateTransition.play();
    }
    
    /**
     * 节点移动
     *
     * @param distance
     * @param limit
     * @return 是否将该节点删除
     */
    public boolean moving(double distance, double limit) {
        setTranslateX(getTranslateX() - distance);
        return outOfBounds(limit);
    }
    
    private void showStationText(String station) {
        locationLabel = new Label("火车南站-A口安检点");
        locationLabel.setStyle(
                "-fx-background-color: #3C73F5;" +
                        "-fx-background-radius: 50px");
        locationLabel.setFont(new Font("Arial", 20));
        locationLabel.setTextFill(Color.WHITE);
        locationLabel.setPadding(new Insets(8, 20, 8, 20));
        locationLabel.setTranslateX(0);
        locationLabel.setTranslateY(50);
        getChildren().add(locationLabel);
    }
    
    /**
     * 位置信息标签移动
     */
    private void setLabelCenter() {
        Platform.runLater(() -> {
            locationLabelTranslateTransition = new TranslateTransition(Duration.millis(500), locationLabel);
            double v = locationLabel.getTranslateX() + locationLabel.getWidth() / 2;
            double v1 = getPrefWidth() / 2;
            locationLabelTranslateTransition.setFromX(locationLabel.getTranslateX());
            locationLabelTranslateTransition.setByX(v1 - v);
            locationLabelTranslateTransition.play();
        });
    }
    
    /**
     * 包裹开包
     */
    public void unpack() {
        if (unpacked) {
            return;
        }
        //设置只能单次点击
        unpacked = true;
        ThreadUtils.run("buildPackageInfoExecutor", () -> {
            log.info("包裹[{}]正在构建开包消息体", packageId);
            ReceiverRequestMsg.JudgeResultMsg.Builder judgeResultMsgBuilder = ReceiverRequestMsg.JudgeResultMsg.newBuilder();
            judgeResultMsgBuilder.setPackage(packageId);
            judgeResultMsgBuilder.setMain(buildPackageInfo(mainCanvas, mainImage));
            judgeResultMsgBuilder.setSide(buildPackageInfo(sideCanvas, sideImage));
            RecvClient recvClient = (RecvClient) BeanUtil.getBean("recvClient");
            recvClient.write(judgeResultMsgBuilder.build());
        });
        
        setUnpackedLabel(true);
        mainController.addUnpackCount();
    }
    
    /**
     * 包裹放行
     */
    public void pass() {
        if (unpacked) {
            return;
        }
        //设置只能单次点击
        unpacked = true;
        setUnpackedLabel(false);
        mainController.addPassCount();
    }
    
    
    /**
     * 构建开包包裹详情
     *
     * @param canvas
     * @return
     */
    private ReceiverRequestMsg.PackageInfo buildPackageInfo(PackageDataCanvas canvas, Image image) {
        //构建图片转为base64
        ReceiverRequestMsg.PackageInfo.Builder packageInfoBuilder = ReceiverRequestMsg.PackageInfo.newBuilder();
        String imageBase64 = ImageBufferUtil.BufferedImageToBase64(SwingFXUtils.fromFXImage(image, null));
        
        packageInfoBuilder.setImage(imageBase64);
        //获取违禁品框
        canvas.getContrabandInfos().stream().forEach(it -> {
            ReceiverRequestMsg.FrameInfo.Builder frameInfoBuilder = ReceiverRequestMsg.FrameInfo.newBuilder();
            frameInfoBuilder.setType(StrUtil.isNotBlank(it.getType()) ? it.getType() : "手动标记");
            frameInfoBuilder.setX(it.getX());
            frameInfoBuilder.setY(it.getY());
            frameInfoBuilder.setWidth(it.getWidth());
            frameInfoBuilder.setHeight(it.getHeight());
            frameInfoBuilder.setLevel(it.getLevel());
            frameInfoBuilder.setFlag(it.getManualMarkFlag());
            packageInfoBuilder.addFrames(frameInfoBuilder);
        });
        return packageInfoBuilder.build();
    }
    
    public boolean isUnpacked() {
        return unpacked;
    }
    
    
    private void setUnpackedLabel(boolean isUnpack) {
        Platform.runLater(() -> {
            //设置字体
            unpackLabel.setFont(new Font("宋体", 20));
            //设置内边距
            unpackLabel.setPadding(new Insets(5, 10, 5, 10));
            if (isUnpack) {
                unpackLabel.setText("开包");
                unpackLabel.setTextFill(Color.RED);
                unpackLabel.setStyle("-fx-border-style: solid;" +
                        "-fx-border-color: #ff0000;" +
                        " -fx-border-width: 3;" +
                        " -fx-border-radius: 10");
            } else {
                unpackLabel.setText("放行");
                unpackLabel.setTextFill(Color.GREEN);
                unpackLabel.setStyle("-fx-border-style: solid;" +
                        "-fx-border-color: #008020;" +
                        " -fx-border-width: 3;" +
                        " -fx-border-radius: 10");
            }
            getChildren().add(unpackLabel);
            AnchorPane.setRightAnchor(unpackLabel, 10.0);
            AnchorPane.setTopAnchor(unpackLabel, 15.0);
        });
    }
    
    /**
     * 判断是否超时放行
     */
    public void overtimePass() {
        //5秒超时
        if (!unpacked && startTime != 0 && System.currentTimeMillis() - startTime > OVERTIME) {
            log.info("包裹[{}]已经超时", packageId);
            //超时放行
            pass();
        }
    }
    
    /**
     * 处理单视角完成
     * @param isMain 是否为主视角
     */
    private void handleSingleCompleted(boolean isMain) {
        if (isMain) {
            mainCanvas.show();
            mainCompleted = true;
            mainImage = mainCanvas.temporaryImage();
        } else {
            sideCanvas.show();
            sideCompleted = true;
            sideImage = sideCanvas.temporaryImage();
        }
        completed();
    }
    
    private void completed() {
        //主视角和俯视角同时接收完成后
        if (mainCompleted && sideCompleted) {
            //整体移动10像素
            setPrefWidth(x + 10);
            //发送位置水平居中
            setLabelCenter();
            //取消正在判图边框
            cancelBorder();
            //处理完成时的动作
            handleCurrentPackage();
            //删除列数据
            getChildren().removeIf(it -> it instanceof LineDataImageView);
        }
    }
    
    /**
     * 处理正在接收的包裹动作
     */
    private void handleCurrentPackage() {
        if (StrUtil.isNotBlank(closeAction)) {
            switch (closeAction) {
                case CLOSE_ACTION_UNPACK:
                    unpack();
                    break;
                case CLOSE_ACTION_PASS:
                    pass();
                    break;
                default:
                    break;
            }
        }
    }
    
    
    /**
     * 展示判图进度
     */
    private void showProgress() {
    
        ProgressBar progressBar = new ProgressBar();
        
    }
}
