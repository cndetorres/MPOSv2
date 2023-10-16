package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class    MiscellaneousActivity extends Activity {

    ListView MDetails;
    ArrayList<HashMap<String, String>> hmMDetails;
    ListAdapter laMDetails;
    HashMap<String, String> mMDetail;
    String[] sMDetails = new String[]{"TRANSACTIONS FOR THE DAY","FUEL REPORTS","INCIDENT REPORTS","VIEW RETURNS","VIEW PRICELIST","VIEW COMMERCIAL LIST","VIEW PRODUCT INFO","DAILY SALES REPORT","MUST CARRY REPORT","NOT BUYING CUSTOMER REPORT","END TRANSACTIONS"};

    private long mLastClickTime = 0;
    String defaultDate;
    DateFormat df2 = new SimpleDateFormat("yyyyMMdd");
    DBController controller = new DBController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miscellaneous);

        MDetails = (ListView) findViewById(R.id.lvMDetail);
        defaultDate = df2.format(Calendar.getInstance().getTime());

        ViewListview();

        MDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (position == 0){
                    Intent intentTransactionsfortheDayActivity = new Intent(MiscellaneousActivity.this, TransactionsForTheDayActivity.class);
                    startActivity(intentTransactionsfortheDayActivity);
                    finish();
                }

                /*else if(position == 1){
                    Intent intentPerfornamceActivity = new Intent(MiscellaneousActivity.this,PerformanceActivity.class);
                    startActivity(intentPerfornamceActivity);
                    finish();
                }*/
                else if(position == 1){
                    Intent intentFuelReportsActivity = new Intent(MiscellaneousActivity.this, FuelReportsActivity.class);
                    startActivity(intentFuelReportsActivity);
                    finish();
                }else if(position == 2){
                    Intent intentIncidentReportsActivity = new Intent(MiscellaneousActivity.this, IncidentReportActivity.class);
                    startActivity(intentIncidentReportsActivity);
                    finish();
                }else if(position == 3){
                    Intent intentViewReturnsActivity = new Intent(MiscellaneousActivity.this, ViewReturnsActivity.class);
                    startActivity(intentViewReturnsActivity);
                    finish();
                }else if(position == 4){
                    Intent intentPriceListActivity = new Intent(MiscellaneousActivity.this, PriceListActivity.class);
                    startActivity(intentPriceListActivity);
                    finish();
                }else if(position == 5){
                    Intent intentVideoListActivity = new Intent(MiscellaneousActivity.this, VideoListActivity.class);
                    startActivity(intentVideoListActivity);
                    finish();
                }else if(position == 6){
                    Intent intentProductInfoActivity = new Intent(MiscellaneousActivity.this, ProductInfoActivity.class);
                    startActivity(intentProductInfoActivity);
                    finish();
                }else if(position == 7){
                    Intent intentSalesDailyReportActivity = new Intent(MiscellaneousActivity.this, SalesDailyReportActivity.class);
                    startActivity(intentSalesDailyReportActivity);
                    finish();
                }else if(position == 8){
                    Intent intentMustCarry = new Intent(MiscellaneousActivity.this, MustCarryReportActivity.class);
                    startActivity(intentMustCarry);
                    finish();
                }else if(position == 9){
                    Intent intentNotBuyingCustomerActivity = new Intent(MiscellaneousActivity.this, NotBuyingCustomerReport.class);
                    startActivity(intentNotBuyingCustomerActivity);
                    finish();
                }
                else{

                   if (controller.fetchSDR(defaultDate) == 0) {
                        Toasty.info(getApplicationContext(), "Please submit daily sales report", Toast.LENGTH_LONG).show();
                    } else if (controller.fetchNotTimeOut() == 1){
                        Toasty.info(getApplicationContext(), "Please click time out", Toast.LENGTH_LONG).show();
                    } else {
                        Intent intentEndTransactionActivity = new Intent(MiscellaneousActivity.this, CashandDepositActivity.class);
                        startActivity(intentEndTransactionActivity);
                        finish();
                   }

                }


            }
        });


    }

    public void ViewListview() {

        hmMDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sMDetails.length; i++) {
            mMDetail = new HashMap<String, String>();
            mMDetail.put("Header", sMDetails[i]);
            hmMDetails.add(mMDetail);
        }

        try {
            laMDetails = new SimpleAdapter(this, hmMDetails, R.layout.item_miscellaneous,
                    new String[]{"Header"}, new int[]{
                    R.id.rowsHeader}) {
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

            MDetails.setAdapter(laMDetails);

        } catch (Exception e) {

        }
    }

    public void onBackPressed() {
        Intent IntentMainActivity = new Intent(MiscellaneousActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }

}



