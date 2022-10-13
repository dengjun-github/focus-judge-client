package com.gioneco.focus.judge.client.controller;

import cn.hutool.core.util.StrUtil;
import com.gioneco.focus.judge.ClientApplication;
import com.gioneco.focus.judge.domain.LineDataImageView;
import com.gioneco.focus.judge.domain.PackageImageAnchorPane;
import com.gioneco.focus.judge.domain.RecvClient;
import com.gioneco.focus.judge.protobuf.ReceiverRequestMsg;
import com.gioneco.focus.judge.protobuf.SenderRequestMsg;
import com.gioneco.focus.judge.util.GlobalConstant;
import com.gioneco.focus.judge.util.ThreadUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;
import de.felixroske.jfxsupport.FXMLController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author DJ
 * @className MainController
 * @Description 主界面控制器
 * @date 2022-09-26 14:46
 */
@Slf4j
@FXMLController
public class MainController implements Initializable {
    @FXML
    public VBox leftVbox;
    @FXML
    public ImageView suspendImage;
    @FXML
    public Label unpackCountLabel;
    @FXML
    public Label passCountLabel;
    @FXML
    public Label totalCountLabel;
    @FXML
    public Label usernameLabel;
    @FXML
    public Label suspendLabel;
    @FXML
    public Label hourLabel;
    @FXML
    public Label minuteLabel;
    @FXML
    public Label secondLabel;
    @FXML
    private AnchorPane packageImageAp;
    
    
    private final Image suspendImg = new Image("image/suspend.png", 50, 35, true, true);
    
    private final Image startImg = new Image("image/start.png", 50, 35, true, true);
    
    private BooleanProperty isStart = new SimpleBooleanProperty(false);
    
    private final Map<String, PackageImageAnchorPane> cachePackageImageAnchorPaneMap = new HashMap<>(64);
    
    private Timeline animation;
    
    private String currentPackage;
    
    @Resource
    private RecvClient recvClient;
    
    @Resource
    private ScheduledExecutorService packageJudgeExecutor;
    
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listenPackageAnchorPaneStatus();
        usernameLabel.setText(LoginController.accountName);
        suspendImage.setImage(startImg);
        clientStatusListen();
        handleOnKeyReleased();
        suspendLabel.requestFocus();
        initTimer();
    }
    
    
    /**
     * 放行按钮事件
     *
     * @param event
     */
    @FXML
    void passAction(ActionEvent event) {
    }
    
    /**
     * 开包按钮事件
     *
     * @param event
     */
    @FXML
    void unpackAction(ActionEvent event) {
    }
    
    /**
     * 暂停按钮事件
     *
     * @param mouseEvent
     */
    @FXML
    void suspend(MouseEvent mouseEvent) {
        doSuspend();
    }
    
    /**
     * 包裹列数据接收
     *
     * @param msg 列数据消息
     */
    public void recvPackage(SenderRequestMsg.ImageMsg msg) {
        LineDataImageView main = LineDataImageView.buildMain(msg);
        LineDataImageView side = LineDataImageView.buildSide(msg);
        
        PackageImageAnchorPane packageImageAnchorPane = getPackageImageAnchorPane(msg);
        packageImageAnchorPane.handlePackage(main, side);
        
        nodeMoving(main.prefWidth(-1));
        if (main.isEndLine()) {
            receiveEndMoving(50);
        }
        packageImageAp.getChildren().removeIf(it -> it == leftVbox);
        packageImageAp.getChildren().add(leftVbox);
        
    }
    
    private PackageImageAnchorPane getPackageImageAnchorPane(SenderRequestMsg.ImageMsg msg) {
        PackageImageAnchorPane packageImageAnchorPane = cachePackageImageAnchorPaneMap.get(msg.getPackageId());
        if (packageImageAnchorPane == null) {
            currentPackage = msg.getPackageId();
            //总数加一
            addTotal();
            //创建一个PackageImageAnchorPane
            packageImageAnchorPane = new PackageImageAnchorPane(this);
            packageImageAnchorPane.initLayout(packageImageAp, msg.getPackageId());
            
            cachePackageImageAnchorPaneMap.put(msg.getPackageId(), packageImageAnchorPane);
        }
        return packageImageAnchorPane;
    }
    
    /**
     * 包裹移动
     *
     * @param distance
     */
    public void nodeMoving(double distance) {
        List<String> deleted = new ArrayList<>();
        cachePackageImageAnchorPaneMap.forEach((k, v) -> {
            //判断是否出界
            if (v.moving(distance, leftVbox.getWidth())) {
                //添加到删除集合中
                deleted.add(k);
                v.pass();
            }
        });
        //删除缓存
        deleted.forEach(cachePackageImageAnchorPaneMap::remove);
    }
    
    public void receiveEndMoving(double distance) {
        cachePackageImageAnchorPaneMap.values().forEach(it -> it.movingEmpty(distance));
    }
    
    /**
     * 监听包裹的超时状态
     */
    private void listenPackageAnchorPaneStatus() {
        packageJudgeExecutor.scheduleAtFixedRate(() ->{
            cachePackageImageAnchorPaneMap.values().forEach(PackageImageAnchorPane::overtimePass);
        }, 0, 500L, TimeUnit.MILLISECONDS);
    }
    
   
    
    /**
     * 状态监听
     */
    private void clientStatusListen() {
        isStart.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                //如果为true,说明开始判图
                suspendImage.setImage(suspendImg);
                suspendLabel.setText("暂停[esc]");
                animation.play();
            } else {
                //为false,则正在暂停
                suspendImage.setImage(startImg);
                suspendLabel.setText("开始[esc]");
                animation.stop();
            }
        });
    }
    
    /**
     * 处理按键事件
     */
    private void handleOnKeyReleased() {
        //空格键单独处理
        ClientApplication.getScene().setOnKeyTyped(event -> {
            if (" ".equals(event.getCharacter())) {
                handleCurrentPackage(false);
            }
        });
        ClientApplication.getScene().setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE:
                    doSuspend();
                    break;
                //空格键单独处理
/*                case SPACE:
                    addPassCount();
                    break;*/
                case ENTER:
                    handleCurrentPackage(true);
                    break;
                default:
                    log.info("无效按键");
            }
        });
    }
    
    
    /**
     * 处理暂停事件
     */
    private void doSuspend() {
        isStart.set(!isStart.get());
        
        ReceiverRequestMsg.ClientStatus clientStatus = ReceiverRequestMsg
                .ClientStatus
                .newBuilder()
                .setStatus(isStart.get() ? GlobalConstant.CLIENT_JUDGING_STATUS : GlobalConstant.CLIENT_SUSPEND_STATUS)
                .setUsername(LoginController.accountName)
                .build();
        recvClient.write(clientStatus);
    }
    
    
    /**
     * 开包数+1
     */
    public void addUnpackCount() {
        Platform.runLater(() -> {
            unpackCountLabel.setText(String.valueOf(Integer.parseInt(unpackCountLabel.getText()) + 1));
        });
    }
    
    /**
     * 放行数+1
     */
    public void addPassCount() {
        Platform.runLater(() -> {
            passCountLabel.setText(String.valueOf(Integer.parseInt(passCountLabel.getText()) + 1));
        });
    }
    
    /**
     * 总数+1
     */
    private void addTotal() {
        Platform.runLater(() -> {
            totalCountLabel.setText(String.valueOf(Integer.parseInt(totalCountLabel.getText()) + 1));
        });
    }
    
    
    
    /**
     * 创建计时器
     */
    private void initTimer() {
        animation = new Timeline(new KeyFrame(Duration.millis(1000), event -> {
            secondLabel.setText(String.valueOf(Integer.parseInt(secondLabel.getText()) + 1));
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        timeListen();
    }
    
    /**
     * 计时器监听
     */
    private void timeListen() {
        secondLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Integer.parseInt(newValue) >= 60) {
                secondLabel.setText("00");
                minuteLabel.setText(String.valueOf(Integer.parseInt(minuteLabel.getText()) + 1));
            }
            if (newValue.length() == 1) {
                secondLabel.setText("0" + secondLabel.getText());
            }
        });
        minuteLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            if (Integer.parseInt(newValue) > 60) {
                secondLabel.setText("00");
                hourLabel.setText(String.valueOf(Integer.parseInt(hourLabel.getText()) + 1));
            }
            if (newValue.length() == 1) {
                minuteLabel.setText("0" + minuteLabel.getText());
            }
        });
        hourLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() == 1) {
                hourLabel.setText("0" + hourLabel.getText());
            }
        });
    }
    
    /**
     * 处理正在接收的包裹
     * @param isUnpack 是否开包 true-开包台;false-放行
     */
    private void handleCurrentPackage(boolean isUnpack) {
        //获取当前包裹
        if (StrUtil.isBlank(currentPackage)) {
            return;
        }
        PackageImageAnchorPane currentImageAp = cachePackageImageAnchorPaneMap.get(currentPackage);
        if (null == currentImageAp) {
            return;
        }
        currentImageAp.setCloseAction(isUnpack?GlobalConstant.CLOSE_ACTION_UNPACK:GlobalConstant.CLOSE_ACTION_PASS);
    }
  
}