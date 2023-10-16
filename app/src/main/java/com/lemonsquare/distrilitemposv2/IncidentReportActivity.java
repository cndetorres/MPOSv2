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

public class IncidentReportActivity extends Activity {

    DBController controller = new DBController(this);
    ListView IRDetails,IRHeader;
    ArrayList<HashMap<String, String>> hmIRHeader;
    ListAdapter laIRHeader,laIRDetails;
    HashMap<String, String> mIRHeader;
    List<HashMap<String, String>> IRViewIReport;
    BottomNavigationView menu;
    List<String> IRListIReport;
    TextView tvIRDTime,tvIRType,tvIRRBy,tvIRDetails,tvIRReference,tvIROk;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incidentreport);

        IRHeader = (ListView) findViewById(R.id.lvIRHeader);
        IRDetails = (ListView) findViewById(R.id.lvIRDetails);
        menu = (BottomNavigationView) findViewById(R.id.btIRNavigation);

        IRViewIReport = controller.fetchIncidentReport();

        IRDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laIRDetails.getItem(position);

                String objId = (String) obj.get("Id");
                controller.PIiD =  objId;

                DialogIncidentReport();

            }
        });

        ViewHeaderListview();
        ViewDetailListview();

        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mir_add:

                                if (controller.fetchISSDR() == 2){
                                    Toasty.info(IncidentReportActivity.this,"please time in first", Toast.LENGTH_LONG).show();
                                }else if (controller.fetchTimeInOutComplete() == 1){
                                    Toasty.info(IncidentReportActivity.this,"you have already completed your transaction for today", Toast.LENGTH_LONG).show();
                                }else{
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                        return false;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();

                                    Intent intentNewIncdentReport = new Intent(IncidentReportActivity.this, NewIncidentReportActivity.class);
                                    startActivity(intentNewIncdentReport);
                                    finish();
                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmIRHeader = new ArrayList<HashMap<String, String>>();
        mIRHeader = new HashMap<String, String>();

        mIRHeader.put("Id", "ID");
        mIRHeader.put("Incident Name", "INCIDENT NAME");
        mIRHeader.put("Details", "DETAILS");
        hmIRHeader.add(mIRHeader);

        try {
            laIRHeader = new SimpleAdapter(this, hmIRHeader, R.layout.item_incidentreport,
                    new String[]{"Id", "Incident Name", "Details"}, new int[]{
                    R.id.rowsID, R.id.rowsIName, R.id.rowsDetail}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rid = (TextView) view.findViewById(R.id.rowsID);
                    TextView riname = (TextView) view.findViewById(R.id.rowsIName);
                    TextView rdetail = (TextView) view.findViewById(R.id.rowsDetail);
                    if (position % 2 == 0) {
                        rid.setTextColor(Color.WHITE);
                        riname.setTextColor(Color.WHITE);
                        rdetail.setTextColor(Color.WHITE);
                        rid.setTypeface(null, Typeface.BOLD);
                        riname.setTypeface(null, Typeface.BOLD);
                        rdetail.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            IRHeader.setAdapter(laIRHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laIRDetails = new SimpleAdapter(this, IRViewIReport, R.layout.item_incidentreport,
                    new String[]{"Id", "Incident Name", "Details"}, new int[]{
                    R.id.rowsID, R.id.rowsIName, R.id.rowsDetail}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    if (position % 2 == 1) {
                        view.setBackgroundColor(Color.parseColor("#fff2dc"));
                    } else {
                        view.setBackgroundColor(Color.parseColor("#f7e1a8"));
                    }
                    return view;

                }
            };
            IRDetails.setAdapter(laIRDetails);
        } catch (Exception e) {

        }
    }

    public void DialogIncidentReport() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(IncidentReportActivity.this);
        LayoutInflater inflater = IncidentReportActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_incidentreport, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        tvIRDTime = (TextView)dialogView.findViewById(R.id.tvDIRDDateTime);
        tvIRType = (TextView)dialogView.findViewById(R.id.tvDIRDType);
        tvIRRBy = (TextView)dialogView.findViewById(R.id.tvDIRDRBy);
        tvIRDetails = (TextView)dialogView.findViewById(R.id.tvDIRDDetails);
        tvIRReference = (TextView)dialogView.findViewById(R.id.tvDIRDReference);
        tvIROk = (TextView)dialogView.findViewById(R.id.tvDIROk);

        IRListIReport = controller.fetchIRInfo();

        tvIRDTime.setText(IRListIReport.get(0));
        tvIRType.setText(IRListIReport.get(1));
        tvIRRBy.setText(IRListIReport.get(2));
        tvIRDetails.setText(IRListIReport.get(3));
        tvIRReference.setText(IRListIReport.get(4));

        tvIROk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(IncidentReportActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }

}



