package me.qKing12.AuctionMaster.Utils;

import me.qKing12.AuctionMaster.AuctionMaster;

import java.math.RoundingMode;
import java.text.NumberFormat;

public class NumberFormatHelper {

    private NumberFormat numberFormat;
    public boolean useDecimals;

    public NumberFormatHelper(){
        this.numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        if(AuctionMaster.plugin.getConfig().getBoolean("number-format.use-decimals")){
            numberFormat.setMaximumFractionDigits(2);
            useDecimals=true;
        }
        else {
            numberFormat.setRoundingMode(RoundingMode.FLOOR);
            numberFormat.setMaximumFractionDigits(0);
            useDecimals=false;
        }
    }

    public String formatNumber(Double number){
        return numberFormat.format(number);
    }

}
