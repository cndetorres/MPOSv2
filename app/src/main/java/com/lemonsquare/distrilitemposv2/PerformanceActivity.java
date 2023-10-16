package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class PerformanceActivity extends Activity {

    DBController controller = new DBController(this);
    ListView PTDetails,PMDetails;
    ArrayList<HashMap<String, String>>hmPTDetails,hmPMDetails;
    ListAdapter laPTDetails,laPMDetails;
    HashMap<String, String>mPTDetail,mPMDetail;

    TabHost tabHost;

    String[] sPTDetails = new String[]{"SALES","SALES PERFORMANCE","COLLECTION","COLLECTION PERFORMANCE","BO","BO PERFORMANCE","SALES TARGET AVE","COLLECTION TARGET AVE","SALES REP"};
    String[] sPMDetails = new String[]{"SALES QUOTA","SALES","SALES PERFORMANCE","COLLECTION","COLLECTION PERFORMANCE","BO","BO PERFORMANCE","BALANCE","SALES REP"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_performance);


        PTDetails = (ListView) findViewById(R.id.lvPTDetails);
        PMDetails = (ListView) findViewById(R.id.lvPMDetails);

        tabHost = (TabHost) findViewById(R.id.tabPHost);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("TODAY");
        spec.setContent(R.id.tabPToday);
        spec.setIndicator("TODAY");
        tabHost.addTab(spec);

        ViewPTDetailListview();


        //Tab 2
        spec = tabHost.newTabSpec("THIS MONTH");
        spec.setContent(R.id.tabPMonth);
        spec.setIndicator("THIS MONTH");
        tabHost.addTab(spec);

        ViewPMDetailListview();

    }

    public void ViewPTDetailListview() {

        hmPTDetails = new ArrayList<HashMap<String, String>>();
        DecimalFormat SPAmt = new DecimalFormat("#,##0.00");

        for(int i = 0; i < sPTDetails.length; i++) {
            mPTDetail = new HashMap<String, String>();
            mPTDetail.put("Header", sPTDetails[i]);
            if (i == 0){
                mPTDetail.put("Detail", controller.fetchSalesToday());
            }else if (i == 2){
                //Double totalColl = controller.fetchCollCashToday() + controller.fetchCollCheckToday();
                mPTDetail.put("Detail",String.valueOf(SPAmt.format(controller.fetchCollToday())));
            }else if (i == 4){
                mPTDetail.put("Detail", controller.fetchBOToday());
            }else if (i == 8){
                mPTDetail.put("Detail", controller.fetchdbSettings().get(6));
            }else{
                mPTDetail.put("Detail","0.00");
            }

            hmPTDetails.add(mPTDetail);
        }

        try {
            laPTDetails = new SimpleAdapter(this, hmPTDetails,R.layout.item_summary,
                    new String[]{"Header","Detail"}, new int[]{
                    R.id.rowsHeader,R.id.rowsDetail}) {
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

            PTDetails.setAdapter(laPTDetails);
        } catch (Exception e) {

        }
    }

    public void ViewPMDetailListview() {

        hmPMDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sPMDetails.length; i++) {
            mPMDetail = new HashMap<String, String>();
            mPMDetail.put("Header", sPMDetails[i]);
            String salesPerf;
            if (controller.fetchSalesPerf().size() == 0){
                if (i == 8){
                    salesPerf = controller.fetchdbSettings().get(6);
                }else{
                    salesPerf = "0.00";
                }

            }else{
                salesPerf = controller.fetchSalesPerf().get(i);
            }
            mPMDetail.put("Detail", salesPerf);
            hmPMDetails.add(mPMDetail);
        }

        try {
            laPMDetails = new SimpleAdapter(this, hmPMDetails,R.layout.item_summary,
                    new String[]{"Header","Detail"}, new int[]{
                    R.id.rowsHeader,R.id.rowsDetail}) {
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

            PMDetails.setAdapter(laPMDetails);
        } catch (Exception e) {

        }
    }

    public void onBackPressed() {
        Intent IntentMainActivity = new Intent(PerformanceActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }
}
