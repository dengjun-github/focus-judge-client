package com.gioneco.focus.judge.domain;

import cn.hutool.core.util.NumberUtil;
import com.gioneco.focus.judge.protobuf.SenderRequestMsg;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author DJ
 * @className Frame
 * @Description 违禁品框
 * @date 2022-09-29 17:35
 */
@Data
@AllArgsConstructor
public class Frame {
    
    /**
     * 左上脚顶点X坐标
     */
    private double x1;
    /**
     * 左上角顶点Y坐标
     */
    private double y1;
    /**
     * 右下角顶点X坐标
     */
    private double x2;
    /**
     * 右下角顶点Y坐标
     */
    private double y2;
    

    
    public Frame(ContrabandInfo contrabandInfo,PackageDataCanvas packageDataCanvas) {
        double h = contrabandInfo.getHeight().doubleValue();
        double w = contrabandInfo.getWidth().doubleValue();
        double x = contrabandInfo.getX().doubleValue();
        double y = contrabandInfo.getY().doubleValue();
        this.x1 = (int) NumberUtil.mul(x, packageDataCanvas.prefWidth(-1));
        this.x2 = (int) NumberUtil.add(x1, NumberUtil.mul(w, packageDataCanvas.prefWidth(-1)));
        this.y1 = (int) NumberUtil.mul(y, packageDataCanvas.prefHeight(-1));
        this.y2 = (int) NumberUtil.add(y1, NumberUtil.mul(h, packageDataCanvas.prefHeight(-1)));
    }
}
