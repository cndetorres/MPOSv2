package com.lemonsquare.distrilitemposv2.Model;

public class ItemList {

    String materialCode;
    String materialName;
    String unit;
    String upc;

    public ItemList(String materialCode,String materialName,String unit,String upc){
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.unit = unit;
        this.upc = upc;
    }


    public String getMaterialCode(){
        return  materialCode;
    }


    public String getMaterialName(){
        return  materialName;
    }

    public String getUnit(){
        return  unit;
    }

    public String getUpc(){
        return  upc;
    }

 }


