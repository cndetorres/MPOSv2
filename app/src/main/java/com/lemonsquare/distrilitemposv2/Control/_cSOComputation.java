package com.lemonsquare.distrilitemposv2.Control;

import com.lemonsquare.distrilitemposv2.Model._mSOComputation;
import com.lemonsquare.distrilitemposv2.View.SalesOrderView;

public class _cSOComputation {

    private _mSOComputation model;
    private SalesOrderView view;

    public _cSOComputation(_mSOComputation model, SalesOrderView view){
        this.model = model;
        this.view = view;
    }

    public void setGrossAmt(Double grossAmt){
        model.setGrossAmt(grossAmt);
    }

    public Double getGrossAmt(){
        return model.getGrossAmt();
    }

    public void setDisc(Double disc){
        model.setDisc(disc);
    }

    public Double getDisc(){
        return model.getDisc();
    }

    public void setLessReturns(Double lessReturns){
        model.setLessReturns(lessReturns);
    }

    public Double getLessReturns(){
        return model.getLessReturns();
    }

    public void setNetAmt(Double netAmt){
         model.setNetAmt(netAmt);
    }

    public Double getNetAmt(){
        return model.getNetAmt();
    }
}
