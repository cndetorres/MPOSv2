package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MustCarryReportActivity  extends Activity {

    DBController controller = new DBController(this);
    ListView MCDetails,MCHeader;
    ArrayList<HashMap<String, String>> hmMCHeader;
    ListAdapter laMCHeader,laMCDetails;
    HashMap<String, String> mPLHeader;
    List<HashMap<String, String>> MCViewPList;
    MaterialBetterSpinner mbsMCList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mustcarryreport);


        MCDetails = (ListView) findViewById(R.id.lvMCDetail);
        MCHeader = (ListView) findViewById(R.id.lvMCHeader);
        mbsMCList = (MaterialBetterSpinner) findViewById(R.id.spMCList);



        String[] strRCategory = {"COMPLETE","INCOMPLETE"};

        ArrayAdapter<String> arrayAdapter;
        arrayAdapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_dropdown_item_1line, strRCategory);
        mbsMCList.setAdapter(arrayAdapter);

        mbsMCList.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            MCViewPList.clear();
            MCDetails.invalidateViews();

            int Status;
            if (mbsMCList.getText().toString().equals("COMPLETE")){
               Status = 1;
            }else{
                Status = 0;
            }
            MCViewPList = controller.fetchMustCarry(Status);
            ViewDetailListview();
            MCDetails.invalidateViews();
        }
    });


    ViewHeaderListview();

    MCViewPList = controller.fetchMustCarry();
    ViewDetailListview();
    MCDetails.invalidateViews();

}

    public void ViewHeaderListview() {

        hmMCHeader = new ArrayList<HashMap<String, String>>();
        mPLHeader = new HashMap<String, String>();

        mPLHeader.put("CustCode", "CUSTCODE");
        mPLHeader.put("CustName", "CUSTNAME");
        mPLHeader.put("CC", "CC");
        mPLHeader.put("WCH", "WCH");
        mPLHeader.put("LAC", "LAC");
        mPLHeader.put("LAB", "LAB");
        mPLHeader.put("IA", "IA");
        hmMCHeader.add(mPLHeader);

        try {
            laMCHeader = new SimpleAdapter(this, hmMCHeader, R.layout.item_mustcarry,
                    new String[]{"CustCode","CustName","CC","WCH","LAC","LAB","IA"}, new int[]{
                    R.id.rowsCustCode, R.id.rowsCustName, R.id.rowsCC, R.id.rowsWCH,
                    R.id.rowsLAC, R.id.rowsLAB, R.id.rowsIA}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView custCode = (TextView) view.findViewById(R.id.rowsCustCode);
                    TextView custName = (TextView) view.findViewById(R.id.rowsCustName);
                    TextView CC = (TextView) view.findViewById(R.id.rowsCC);
                    TextView WCH = (TextView) view.findViewById(R.id.rowsWCH);
                    TextView LAC = (TextView) view.findViewById(R.id.rowsLAC);
                    TextView LAB = (TextView) view.findViewById(R.id.rowsLAB);
                    TextView IA = (TextView) view.findViewById(R.id.rowsIA);
                    if (position % 2 == 0) {
                        LAB.setVisibility(View.GONE);
                        custCode.setTextColor(Color.WHITE);
                        custName.setTextColor(Color.WHITE);
                        CC.setTextColor(Color.WHITE);
                        WCH.setTextColor(Color.WHITE);
                        LAC.setTextColor(Color.WHITE);
                        LAB.setTextColor(Color.WHITE);
                        IA.setTextColor(Color.WHITE);
                        custCode.setTypeface(null, Typeface.BOLD);
                        custName.setTypeface(null, Typeface.BOLD);
                        CC.setTypeface(null, Typeface.BOLD);
                        WCH.setTypeface(null, Typeface.BOLD);
                        LAC.setTypeface(null, Typeface.BOLD);
                        LAB.setTypeface(null, Typeface.BOLD);
                        IA.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            MCHeader.setAdapter(laMCHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laMCDetails = new SimpleAdapter(this, MCViewPList, R.layout.item_mustcarry,
                    new String[]{"CustCode","CustName","CC","WCH","LAC","LAB","IA"}, new int[]{
                    R.id.rowsCustCode, R.id.rowsCustName, R.id.rowsCC, R.id.rowsWCH,
                    R.id.rowsLAC, R.id.rowsLAB, R.id.rowsIA}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView CC = (TextView) view.findViewById(R.id.rowsCC);
                    TextView WCH = (TextView) view.findViewById(R.id.rowsWCH);
                    TextView LAC = (TextView) view.findViewById(R.id.rowsLAC);
                    TextView LAB = (TextView) view.findViewById(R.id.rowsLAB);
                    TextView IA = (TextView) view.findViewById(R.id.rowsIA);
                    LAB.setVisibility(View.GONE);

                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;

                }
            };
            MCDetails.setAdapter(laMCDetails);
        } catch (Exception e) {

        }
    }



    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(MustCarryReportActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }

}
