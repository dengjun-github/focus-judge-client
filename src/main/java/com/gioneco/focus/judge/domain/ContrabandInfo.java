package com.gioneco.focus.judge.domain;

import cn.hutool.core.util.NumberUtil;
import com.gioneco.focus.judge.protobuf.SenderRequestMsg;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author DJ
 * @className ContrabandInfo
 * @Description
 * @date 2022-09-29 19:14
 */
@Data
public class ContrabandInfo {
    /**
     * 违禁品相对x坐标(比值)
     */
    private Double x;
    /**
     * 违禁品相对y坐标(比值)
     */
    private Double y;
    /**
     * 违禁品框相对宽(比值)
     */
    private Double width;
    /**
     * 违禁品框相对高(比值)
     */
    private Double height;
    /**
     * 违禁品类型(英文)
     */
    private String type;
    /**
     * 匹配率
     */
    private Double score;
    /**
     * 是否手工标记 0-否;1-是
     */
    private int manualMarkFlag;
    /**
     * 告警等级
     */
    private int level;
    /**
     * 违禁品类型(英文)
     */
    private String value;
    
    
    public ContrabandInfo() {
    
    }
    
    public ContrabandInfo(SenderRequestMsg.ContrabandInfoMsg info) {
        this.x = info.getX();
        this.y = info.getY();
        this.width = info.getW();
        this.height = info.getH();
        this.type = info.getType();
        this.score = info.getS();
        this.manualMarkFlag = 0;
//        this.level = info.get;
//        this.value = info.getType();
    }
    
    public ContrabandInfo(Frame frame,PackageDataCanvas packageDataCanvas) {
        this.x = NumberUtil.div(frame.getX1(), packageDataCanvas.prefWidth(-1));
        this.y = NumberUtil.div(frame.getY1(),packageDataCanvas.prefHeight(-1));
        this.width = NumberUtil.div(NumberUtil.sub(frame.getX2(), frame.getX1()), packageDataCanvas.prefWidth(-1));
        this.height = NumberUtil.div(NumberUtil.sub(frame.getY2(),frame.getY1()),packageDataCanvas.prefHeight(-1));
        this.level = 3;
        this.manualMarkFlag = 1;
    }
}
