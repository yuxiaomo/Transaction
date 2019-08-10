package com.transaction.core.exchange.zhaobi;


import com.transaction.core.entity.AmountPrice;

import java.text.DecimalFormat;

// 处理数量
public class Deal {

    // 处理数量
    public static String dealCount(double count, String sy){
        String result = "error";
        switch (sy){
            case "BTY":
                // 保留小数点后一位
                result = new DecimalFormat("0.0").format(count);
                return result;
            case "YCC":
                // 保留整数位
                result = new DecimalFormat("0.0").format(count);
                return result;
        }
        return result;
    }

    // 处理价格
    public static String dealPrice(double price, String sy){
        String result = "error";
        switch (sy){
            case "BTY":
                // 保留小数点后6位
                result = new DecimalFormat("0.000000").format(price);
                return result;
            case "USDT":
                // 保留小数点后6位
                result = new DecimalFormat("0.000000").format(price);
                return result;
        }
        return result;
    }

    // p1 p2 p3 分别是第一二三步具体的usdt  type第二步是买还是卖
    public static AmountPrice getAcuallyUSDT(AmountPrice amountPrice, double sy1, double sy12, double sy2, String type){

        // 获取最小的
        double min = sy1;
        if(sy12 < min) {
            min = sy12;
        }
        if (sy2 < min) {
            min = sy2;
        }

        AmountPrice a = new AmountPrice();
        a.setSy1Price(amountPrice.getSy1Price());
        a.setSy2Price(amountPrice.getSy2Price());
        a.setSy12Price(amountPrice.getSy12Price());

        // 买bty ，bty 买 ycc， 。卖掉ycc
        if(type == "BUY") {
            if (min == sy1){
                a.setMinUSDT(sy1);
                // 以第一步为基准 可以知道多少bty
                double bty = amountPrice.getSy1Amount();
                a.setSy1Amount(bty);
                // 第二步的数量 根据第一步的bty来获取
                double ycc = getAnotherSyAmount1(bty,amountPrice.getSy12Price(),type);
                // 第二步买入多少ycc  第三步就卖出多少ycc
                a.setSy12Amount(ycc);
                a.setSy2Amount(ycc);

            }

            if (min == sy12){
                a.setMinUSDT(sy12);
                // 以第二步为基准  能知道第二步买入多少ycc
                double ycc = amountPrice.getSy12Amount();
                // 获取第一步所需要的bty数量
                double bty = getAnotherSyAmount2(ycc,amountPrice.getSy12Price(),type);
                a.setSy1Amount(bty);
                a.setSy12Amount(ycc);
                a.setSy2Amount(ycc);

            }

            if (min == sy2){
                // 以第三步为基准 卖出多少ycc
                double ycc = amountPrice.getSy2Amount();
                // 第一步根据ycc 获取bty
                double bty = getAnotherSyAmount2(ycc,amountPrice.getSy12Price(),type);

                a.setSy1Amount(bty);
                // 第二步买入多少ycc
                a.setSy12Amount(ycc);
                a.setSy2Amount(ycc);

            }
        }

        // 买ycc ，卖ycc 得到bty。卖掉bty
        if(type == "SELL") {
            if (min == sy1){
                a.setMinUSDT(sy1);
                // 以第三步为基准 可以知道卖出多少bty
                double bty = amountPrice.getSy1Amount();

                // 第二步的数量 根据第3步的bty来获取  // 需要知道卖掉多少ycc才能买到第三步的bty
                double ycc = getAnotherSyAmount1(bty,amountPrice.getSy12Price(),type);

                // 第2步卖出多少ycc  第1步就卖出多少ycc

                //第一步买入多少ycc
                a.setSy2Amount(ycc);
                //第二步 卖出多少ycc 获得bty
                a.setSy12Amount(ycc);
                //第三步 卖出多少bty
                a.setSy1Amount(bty);

            }

            if (min == sy12){
                a.setMinUSDT(sy12);
                // 以第二步为基准  能知道第二步卖出多少ycc
                double ycc = amountPrice.getSy12Amount();
                // 获取第2步所得到的bty数量
                double bty = getAnotherSyAmount2(ycc,amountPrice.getSy12Price(),type);

                //第一步买入多少ycc
                a.setSy2Amount(ycc);
                // 第二步卖出多少ycc
                a.setSy12Amount(ycc);
                //第三步卖出多少bty
                a.setSy1Amount(bty);

            }

            if (min == sy2){
                // 以第1步为基准 买入多少ycc
                double ycc = amountPrice.getSy2Amount();
                // ycc 第二步卖出ycc能获取多少bty获取bty
                double bty = getAnotherSyAmount2(ycc,amountPrice.getSy12Price(),type);

                //第一步买入多少ycc
                a.setSy2Amount(ycc);
                // 第二步卖出多少ycc
                a.setSy12Amount(ycc);
                //第三步卖出多少bty
                a.setSy1Amount(bty);

            }
        }



        return a;
    }


    // 都是根据bty获取ycc数量
    //  第一步买入比特元，有bty的数量，需要知道能在第二步买多少ycc
    // amount bty  price bty-ycc    bty*(1-0.001)/price = ycc

    //  第3步卖出比特元，有bty的数量，需要知道第二步卖多少ycc能得到bty ycc为手续费
    // amount bty  price bty-ycc    ycc*(1-0.001)*price = bty   ycc = bty/(1-0.001)/price
    public static double getAnotherSyAmount1(double amount, double price, String type){
        if (type == "BUY"){
            return  amount*(1-0.001)/price;
        }
        if (type == "SELL"){
            return  amount/(1-0.001)/price;
        }

        return 0;
    }


    // 都是根据ycc获取bty数量
    // 0.052  24.34  23.18  0.0157  买100个就是100个  手续费从usdt扣
    // bty 买 ycc  amount=ycc  price = bty-ycc  返回需要第一步购买的bty数量  bty*(1-0.001) = ycc * price  bty = ( ycc * price)/(1-0.001)
    //  第三步只需要卖掉第二步ycc数量就行  bty做手续费 所有bty稍微多点
    // ycc 卖掉获取bty  amount = ycc 需要知道我要在第三步卖掉多少bty  第一步只要买同样ycc就行 ycc*(1-0.001) = bty/price    bty =  ycc*(1-0.001)*price
    // bty做手续费 所有bty稍微少点
    public static double getAnotherSyAmount2(double amount, double price, String type){
        if (type == "BUY"){
            return amount*price/(1-0.001);
        }

        if (type == "SELL"){
            return amount*price*(1-0.001);
        }

        return 0;
    }




}