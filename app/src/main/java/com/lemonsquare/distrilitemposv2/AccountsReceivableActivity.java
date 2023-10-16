package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AccountsReceivableActivity extends Activity {

    DBController controller = new DBController(this);
    ListView ARDetails,ARHeader;
    ArrayList<HashMap<String, String>> hmARHeader;
    ListAdapter laARHeader,laARDetails;
    HashMap<String, String> mARHeader;
    List<HashMap<String, String>> ARViewAReceivable;
    TextView tvARCName,tvARTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsreceivable);


        ARDetails = (ListView) findViewById(R.id.lvARDetail);
        ARHeader = (ListView) findViewById(R.id.lvARHeader);
        tvARCName = (TextView) findViewById(R.id.tvARCName);
        tvARTotal = (TextView) findViewById(R.id.tvARTotal);

        tvARCName.setText(controller.PCLName);
        DecimalFormat ARAmt = new DecimalFormat("#,###,##0.00");
        tvARTotal.setText("TOTAL: " + ARAmt.format(controller.fetchSUMARBalances()));

        //controller.PCCode = controller.fetchCCodeCustomers();

        ARViewAReceivable = controller.fetchAccountsReceivable();

        ViewHeaderListview();
        ViewDetailListview();

    }

    public void ViewHeaderListview() {

        hmARHeader = new ArrayList<HashMap<String, String>>();
        mARHeader = new HashMap<String, String>();

        mARHeader.put("SID", "SALES ID");
        mARHeader.put("BillDt", "BILL DT");
        mARHeader.put("Amt", "AMOUNT");
        mARHeader.put("Payment", "PAYMENT");
        mARHeader.put("Balance", "BALANCE");
        hmARHeader.add(mARHeader);

        try {
            laARHeader = new SimpleAdapter(this, hmARHeader, R.layout.item_accountsreceivable,
                    new String[]{"SID", "BillDt", "Amt", "Payment", "Balance"}, new int[]{
                    R.id.rowsSalesID, R.id.rowsBillDt, R.id.rowsAmt, R.id.rowsPayment,R.id.rowsBalance}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rsid = (TextView) view.findViewById(R.id.rowsSalesID);
                    TextView rbilldt = (TextView) view.findViewById(R.id.rowsBillDt);
                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);
                    TextView rpment = (TextView) view.findViewById(R.id.rowsPayment);
                    TextView rbal = (TextView) view.findViewById(R.id.rowsBalance);
                    if (position % 2 == 0) {
                        rsid.setTextColor(Color.WHITE);
                        rbilldt.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rpment.setTextColor(Color.WHITE);
                        rbal.setTextColor(Color.WHITE);
                        rsid.setTypeface(null, Typeface.BOLD);
                        rbilldt.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        rpment.setTypeface(null, Typeface.BOLD);
                        rbal.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            ARHeader.setAdapter(laARHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laARDetails = new SimpleAdapter(this, ARViewAReceivable, R.layout.item_accountsreceivable,
                    new String[]{"SID", "BillDt", "Amt", "Payment", "Balance"}, new int[]{
                    R.id.rowsSalesID, R.id.rowsBillDt, R.id.rowsAmt, R.id.rowsPayment,R.id.rowsBalance}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;

                }
            };
            ARDetails.setAdapter(laARDetails);
        } catch (Exception e) {

        }
    }

    public void onBackPressed() {

       /* if (controller.Prscl == 1){
            Intent IntentRouteScheduleActivity = new Intent(AccountsReceivableActivity.this, RouteScheduleActivity.class);
            startActivity(IntentRouteScheduleActivity);
            finish();

        }else{
            finish();
        }*/
        finish();

    }

}


