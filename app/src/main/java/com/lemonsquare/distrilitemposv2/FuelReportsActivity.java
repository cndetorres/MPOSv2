package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class FuelReportsActivity extends Activity {

    DBController controller = new DBController(this);
    ListView FRDetails,FRHeader;
    ArrayList<HashMap<String, String>> hmFRHeader;
    ListAdapter laFRHeader,laFRDetails;
    HashMap<String, String> mFRHeader;
    List<HashMap<String, String>> FRViewFReport,FRDViewFReport;
    BottomNavigationView menu;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuelreports);

        FRHeader = (ListView) findViewById(R.id.lvFRHeader);
        FRDetails = (ListView) findViewById(R.id.lvFRDetails);
        menu = (BottomNavigationView) findViewById(R.id.btFRNavigation);

        FRViewFReport = controller.fetchFuelReports();

        ViewHeaderListview();
        ViewDetailListview();

        FRDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laFRDetails.getItem(position);

                String objID = (String) obj.get("ID");
                FRDViewFReport = controller.fetchFuelReports(objID);

                DialogFuelReport();

            }
        });

        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mfr_add:

                                if (controller.fetchISSDR() == 2){
                                    Toasty.info(FuelReportsActivity.this,"please time in first", Toast.LENGTH_LONG).show();
                                }else if (controller.fetchTimeInOutComplete() == 1){
                                    Toasty.info(FuelReportsActivity.this,"you have already completed your transaction for today", Toast.LENGTH_LONG).show();
                                }else{
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                        return false;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();

                                    Intent intentNewFuelReport = new Intent(FuelReportsActivity.this, NewFuelReportActivity.class);
                                    startActivity(intentNewFuelReport);
                                    finish();
                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmFRHeader = new ArrayList<HashMap<String, String>>();
        mFRHeader = new HashMap<String, String>();

        mFRHeader.put("ID", "#");
        mFRHeader.put("Station", "STATION");
        mFRHeader.put("Odometer", "ODO");
        mFRHeader.put("Liters", "LITERS");
        mFRHeader.put("Time", "TIME");
        mFRHeader.put("Amt", "AMT");
        hmFRHeader.add(mFRHeader);

        try {
            laFRHeader = new SimpleAdapter(this, hmFRHeader, R.layout.item_fuelreports,
                    new String[]{"ID","Station", "Odometer", "Liters","Time","Amt"}, new int[]{
                    R.id.rowsID,R.id.rowsStation, R.id.rowsOdometer, R.id.rowsLiters,R.id.rowsTime,R.id.rowsAmt}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rid = (TextView) view.findViewById(R.id.rowsID);
                    TextView rstation = (TextView) view.findViewById(R.id.rowsStation);
                    TextView rodometer = (TextView) view.findViewById(R.id.rowsOdometer);
                    TextView rliter = (TextView) view.findViewById(R.id.rowsLiters);
                    TextView rtime = (TextView) view.findViewById(R.id.rowsTime);
                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);
                    if (position % 2 == 0) {
                        rid.setTextColor(Color.WHITE);
                        rstation.setTextColor(Color.WHITE);
                        rodometer.setTextColor(Color.WHITE);
                        rliter.setTextColor(Color.WHITE);
                        rtime.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rid.setTypeface(null, Typeface.BOLD);
                        rstation.setTypeface(null, Typeface.BOLD);
                        rodometer.setTypeface(null, Typeface.BOLD);
                        rliter.setTypeface(null, Typeface.BOLD);
                        rtime.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            FRHeader.setAdapter(laFRHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laFRDetails = new SimpleAdapter(this, FRViewFReport, R.layout.item_fuelreports,
                    new String[]{"ID","Station", "Odometer", "Liters","Time","Amt"}, new int[]{
                    R.id.rowsID,R.id.rowsStation, R.id.rowsOdometer, R.id.rowsLiters,R.id.rowsTime,R.id.rowsAmt}) {
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
            FRDetails.setAdapter(laFRDetails);
        } catch (Exception e) {

        }
    }

    public void DialogFuelReport() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(FuelReportsActivity.this);
        LayoutInflater inflater = FuelReportsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fuelreports, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        TextView tvDFRDtTm = (TextView)dialogView.findViewById(R.id.tvDFRDtTm);
        TextView tvDFTDetails = (TextView)dialogView.findViewById(R.id.tvDFTDetails);
        TextView tvDFRAmt = (TextView)dialogView.findViewById(R.id.tvDFRAmt);
        TextView tvDFRLiters = (TextView)dialogView.findViewById(R.id.tvDFRLiters);
        TextView tvDFROReading = (TextView)dialogView.findViewById(R.id.tvDFROReading);
        TextView tvDFRPONo = (TextView)dialogView.findViewById(R.id.tvDFRPONo);
        TextView tvDFRInvoiceNo = (TextView)dialogView.findViewById(R.id.tvDFRInvoiceNo);
        TextView tvDFROk = (TextView)dialogView.findViewById(R.id.tvDFROk);

        tvDFRDtTm.setText(FRDViewFReport.get(0).get("Time"));
        tvDFTDetails.setText(FRDViewFReport.get(0).get("Station"));
        tvDFRAmt.setText(FRDViewFReport.get(0).get("Amt"));
        tvDFRLiters.setText(FRDViewFReport.get(0).get("Liters"));
        tvDFROReading.setText(FRDViewFReport.get(0).get("Odometer"));
        tvDFRPONo.setText(FRDViewFReport.get(0).get("PONum"));
        tvDFRInvoiceNo.setText(FRDViewFReport.get(0).get("InvoiceNum"));


        tvDFROk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }



    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(FuelReportsActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }

}




