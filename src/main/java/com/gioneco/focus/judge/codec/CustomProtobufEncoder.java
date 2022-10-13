package com.gioneco.focus.judge.codec;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 参考ProtobufVarint32LengthFieldPrepender 和 ProtobufEncoder
 *
 * @author dj
 */
@Sharable
public class CustomProtobufEncoder extends MessageToByteEncoder<MessageLite> {



    @Override
    protected void encode(ChannelHandlerContext ctx, MessageLite msg, ByteBuf out) throws Exception {
        byte[] body = msg.toByteArray();
//        out.writeFloat(1.0F);
        out.writeByte(0);
        out.writeByte(1);
        out.writeInt(body.length);
        out.writeByte(0);
        out.writeByte(getType(msg));
        out.writeBytes(body);
    }



    private Byte getType(MessageLite msg) {
        Byte typeByMsg = MessageManager.getTypeByMsg(msg);
        if (null == typeByMsg) {
            throw new RuntimeException("未找到报文类型");
        }
        return typeByMsg;
    }
}