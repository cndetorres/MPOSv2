package com.lemonsquare.distrilitemposv2.Model;

public class HeaderList {
    String firstColumn;
    String secondColumn;
    String thirdColumn;

    public HeaderList(String firstColumn,String secondColumn,String thirdColumn){
        this.firstColumn = firstColumn;
        this.secondColumn = secondColumn;
        this.thirdColumn = thirdColumn;
    }

    public String getFirstColumn(){
        return  firstColumn;
    }
    public String getSecondColumn(){
        return  secondColumn;
    }
    public String getThirdColumn(){
        return  thirdColumn;
    }

}
