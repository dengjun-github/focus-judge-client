package com.gioneco.focus.judge.codec;


import com.gioneco.focus.judge.protobuf.ReceiverRequestMsg;
import com.gioneco.focus.judge.protobuf.SenderRequestMsg;
import com.google.protobuf.MessageLite;

import java.util.HashMap;
import java.util.Map;

/**
 * @author DJ
 * @className MessageManager
 * @Description
 * @date 2022-05-30 13:51
 */
public class MessageManager {

    private final static Map<Byte, MessageLite> MESSAGE_TYPE_MAP = new HashMap<>();

    private final static byte IMAGE_MSG = 0x00;
    private final static byte POINT_MSG = 0x01;
    private final static byte LOGIN_MSG = 0x02;
    private final static byte CONTRABAND_MSG = 0x03;
    private final static byte SENDER_ACCEPT_MSG = 0x04;
    private final static byte RECEIVER_JUDGE_MSG = 0x07;
    private final static byte CLIENT_STATUS = 0x08;



    static {
        MESSAGE_TYPE_MAP.put(IMAGE_MSG, SenderRequestMsg.ImageMsg.getDefaultInstance());
        MESSAGE_TYPE_MAP.put(POINT_MSG, SenderRequestMsg.PointMsg.getDefaultInstance());
        MESSAGE_TYPE_MAP.put(LOGIN_MSG, ReceiverRequestMsg.LoginMsg.getDefaultInstance());
        MESSAGE_TYPE_MAP.put(CONTRABAND_MSG, SenderRequestMsg.ContrabandMsg.getDefaultInstance());
        MESSAGE_TYPE_MAP.put(SENDER_ACCEPT_MSG, SenderRequestMsg.AcceptMsg.getDefaultInstance());
        MESSAGE_TYPE_MAP.put(RECEIVER_JUDGE_MSG, ReceiverRequestMsg.JudgeResultMsg.getDefaultInstance());
        MESSAGE_TYPE_MAP.put(CLIENT_STATUS, ReceiverRequestMsg.ClientStatus.getDefaultInstance());
    }



    public static MessageLite getMessageByType(byte type) {
        return MESSAGE_TYPE_MAP.get(type);
    }

    public static Byte getTypeByMsg(MessageLite msg) {
        for (Map.Entry<Byte, MessageLite> entry : MESSAGE_TYPE_MAP.entrySet()) {
            if (msg.getClass() == entry.getValue().getClass()) {
                return entry.getKey();
            }
        }
        return null;
    }


}
