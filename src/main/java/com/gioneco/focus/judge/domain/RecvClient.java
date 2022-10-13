package com.gioneco.focus.judge.domain;

import com.gioneco.focus.judge.codec.CustomProtobufDecoder;
import com.gioneco.focus.judge.codec.CustomProtobufEncoder;
import com.gioneco.focus.judge.handler.RecvImageHandler;
import com.gioneco.focus.judge.protobuf.ReceiverRequestMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;

/**
 * @author DJ
 * @className ReciveClient
 * @Description
 * @date 2022-05-09 10:55
 */
@Slf4j
@Component
public class RecvClient {

    private String username;

    private String groupId;

    private Channel channel;
    
    @Resource
    private RecvImageHandler recvImageHandler;
    
    private NioEventLoopGroup worker = new NioEventLoopGroup();

    public  ChannelFuture connect(String host,int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(worker);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("decoder", new CustomProtobufDecoder());
                ch.pipeline().addLast("encoder", new CustomProtobufEncoder());
                ch.pipeline().addLast(recvImageHandler);
            }
        });
        return bootstrap.connect(host, port);
    }

    public ReceiverRequestMsg.LoginMsg loginMsgBuilder() {
        ReceiverRequestMsg.LoginMsg.Builder builder = ReceiverRequestMsg.LoginMsg.newBuilder();
        builder.setUsername(username);
        builder.setPassword("123456");
        builder.setGroupId(groupId);
        return builder.build();
    }


    public void login() {
        worker.submit(() -> {
            ReceiverRequestMsg.LoginMsg login = loginMsgBuilder();
            channel.writeAndFlush(login);
        });
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public String getGroupId() {
        return groupId;
    }


    public  void close() {
        if (channel != null) {
            channel.close();
        }
        worker.shutdownGracefully();
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    public void write(Object msg) {
        channel.writeAndFlush(msg);
    }
    
}
