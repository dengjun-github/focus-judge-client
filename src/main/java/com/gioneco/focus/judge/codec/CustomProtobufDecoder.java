package com.gioneco.focus.judge.codec;

import com.google.protobuf.MessageLite;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 参考ProtobufVarint32FrameDecoder 和 ProtobufDecoder
 *
 * @author dj
 */
public class CustomProtobufDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 如果可读长度小于包头长度，退出。
        while (in.readableBytes() > 8) {
            in.markReaderIndex();

//            byte low = in.readByte();
//            byte high = in.readByte();
//            short s0 = (short) (low & 0xff);
//            short s1 = (short) (high & 0xff);
//            s1 <<= 8;
//            short length = (short) (s0 | s1);
            //版本号
//            float version = in.readFloat();
            ByteBuf buf = in.readBytes(2);
            //报文长度
            int length = in.readInt();
            // 获取包头中的protobuf类型
            in.readByte();
            byte dataType = in.readByte();

            // 如果可读长度小于body长度，恢复读指针，退出。
            //todo 可能会有smallEnding bigEnding问题
            if (in.readableBytes() < length) {
                in.resetReaderIndex();
                return;
            }

            // 读取body
            ByteBuf bodyByteBuf = in.readBytes(length);

            byte[] array;
            int offset;

            int readableLen = bodyByteBuf.readableBytes();
            if (bodyByteBuf.hasArray()) {
                array = bodyByteBuf.array();
                offset = bodyByteBuf.arrayOffset() + bodyByteBuf.readerIndex();
            } else {
                array = new byte[readableLen];
                bodyByteBuf.getBytes(bodyByteBuf.readerIndex(), array, 0, readableLen);
                offset = 0;
            }

            //反序列化
            MessageLite result = decodeBody(dataType, array, offset, readableLen);
            out.add(result);
            bodyByteBuf.release();
//            ReferenceCountUtil.release(in);
        }
    }

    public MessageLite decodeBody(byte dataType, byte[] array, int offset, int length) throws Exception {
        MessageLite messageByType = MessageManager.getMessageByType(dataType);
        if (messageByType == null) {
            throw new ClassNotFoundException("未找到指定报文类型,type = " + dataType);
        }
        return messageByType.getParserForType().parseFrom(array, offset, length);
    }
}