package com.gioneco.focus.judge.handler;

import com.gioneco.focus.judge.client.controller.MainController;
import com.gioneco.focus.judge.protobuf.SenderRequestMsg;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author DJ
 * @className RecvImageHandler
 * @Description
 * @date 2022-05-18 11:02
 */
@Component
public class RecvImageHandler extends SimpleChannelInboundHandler<SenderRequestMsg.ImageMsg> {
    
    @Autowired
    private MainController mainController;

    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SenderRequestMsg.ImageMsg msg) throws Exception {
        handleMsg(msg);
    }

    public void handleMsg(SenderRequestMsg.ImageMsg msg) {
        Platform.runLater(()-> mainController.recvPackage(msg));
    }
    
    
   
}
