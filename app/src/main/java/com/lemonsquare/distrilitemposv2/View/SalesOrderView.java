package com.lemonsquare.distrilitemposv2.View;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.lemonsquare.distrilitemposv2.Control._cSOComputation;
import com.lemonsquare.distrilitemposv2.DBController;
import com.lemonsquare.distrilitemposv2.Model._mSOComputation;
import com.lemonsquare.distrilitemposv2.R;

public class SalesOrderView extends Activity {

    String customerCode;
    DBController dbController = new DBController(this );
    _mSOComputation msoController = new _mSOComputation();
    _cSOComputation csoController;
    Context context = SalesOrderView.this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesorder);

        Intent intent = getIntent();
        customerCode = intent.getStringExtra("customerCode");

        //msoController.setDisc(Utils.convertToDouble(dbController.fetchCustInfo(customerCode).get(3)));

        Toast.makeText(context,String.valueOf(msoController.getDisc()),Toast.LENGTH_LONG).show();


    }

    public void onBackPressed() {

        finish();

    }
}
