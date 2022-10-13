package com.gioneco.focus.judge.client.controller;

import cn.hutool.core.util.StrUtil;
import com.gioneco.focus.judge.ClientApplication;
import com.gioneco.focus.judge.client.view.MainView;
import com.gioneco.focus.judge.domain.RecvClient;
import de.felixroske.jfxsupport.FXMLController;
import io.netty.channel.ChannelFuture;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author DJ
 * @className LoginController
 * @Description 登录界面控制器
 * @date 2022-09-26 11:16
 */
@Slf4j
@FXMLController
public class LoginController implements Initializable {
    @FXML
    public Button loginButton;
    @FXML
    private TextField portText;
    
    @FXML
    private TextField passwordText;
    
    @FXML
    private TextField ipText;
    
    @FXML
    private TextField accountText;
    
    @Resource
    private RecvClient recvClient;
    
    public static String accountName;

    
 
    @Override
    public void initialize(URL location, ResourceBundle resources) {
    
    }
    
    @FXML
    void loginAction(ActionEvent event) {
        String username = accountText.getText();
        String host = ipText.getText();
        String port = portText.getText();
        if (StrUtil.isBlank(host)) {
            host = "localhost";
//            host = "10.255.50.203";
        }
        if (StrUtil.isBlank(port)) {
            port = "10288";
//            port = "30910";
        }
        if (StrUtil.isBlank(username)) {
            username = "ABCD";
        }
        int portNum;
        try {
            portNum = Integer.parseInt(port);
        } catch (NumberFormatException exception) {
            log.info("端口号不正确");
            return;
        }
        String groupId = "1";
    
        loginButton.setText("连接中");
        loginButton.setDisable(true);
        String finalHost = host;
        String finalUsername = username;
        ChannelFuture connectFuture = recvClient.connect(finalHost, portNum);
        connectFuture.addListener(future -> {
            Platform.runLater(() ->{
                if (future.isSuccess()) {
                    recvClient.setUsername(finalUsername);
                    recvClient.setGroupId(groupId);
                    recvClient.login();
                    accountName = finalUsername;
                    toMainView();
                } else {
                    loginButton.setDisable(false);
                    loginButton.setText("连接");
                }
            });
            recvClient.setChannel(connectFuture.sync().channel());
        });
    }
    
    private void toMainView() {
        Stage stage = ClientApplication.getStage();
        stage.setTitle("集中判图客户端");
        stage.setMaximized(true);
        ClientApplication.showView(MainView.class);
        stage.setOnCloseRequest(event -> {
            recvClient.close();
        });
    }
    
    
}
