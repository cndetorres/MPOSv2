package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class NotBuyingCustomerReport  extends Activity {

    DBController controller = new DBController(this);
    ListView NBCDetails,NBCHeader;
    ArrayList<HashMap<String, String>> hmNBCHeader;
    ListAdapter laNBCHeader,laNBCDetails;
    HashMap<String, String> mNBCHeader;
    List<HashMap<String, String>> NBCViewPList;
    String customerCode,customerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notbuyingcustomer);


        NBCDetails = (ListView) findViewById(R.id.lvNBCDetail);
        NBCHeader = (ListView) findViewById(R.id.lvNBCHeader);


        ViewHeaderListview();
        NBCViewPList = controller.fetchNotBuyingCustomer();
        ViewDetailListview();

        NBCDetails.setOnItemLongClickListener (new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View v, final int position, long id) {

                HashMap<String, Object> obj = (HashMap<String, Object>) laNBCDetails.getItem(position);

                customerCode = (String) obj.get("CustCode");
                customerName = (String) obj.get("CustName");
                deactivationRequest();

                return true;
            }
        });

    }

    public void ViewHeaderListview() {

        hmNBCHeader = new ArrayList<HashMap<String, String>>();
        mNBCHeader = new HashMap<String, String>();

        mNBCHeader.put("CustCode", "CUSTCODE");
        mNBCHeader.put("CustName", "CUSTNAME");
        mNBCHeader.put("PrevMon", "PREV MON");
        mNBCHeader.put("2MonsAgo", "2MONS AGO");
        mNBCHeader.put("3MonsAgo", "3MONS AGO");
        hmNBCHeader.add(mNBCHeader);

        try {
            laNBCHeader = new SimpleAdapter(this, hmNBCHeader, R.layout.item_notbuyingcustomer,
                    new String[]{"CustCode","CustName","PrevMon","2MonsAgo","3MonsAgo"}, new int[]{
                    R.id.rowsCustCode, R.id.rowsCustName, R.id.rowsPrevMon, R.id.rows2MonsAgo, R.id.rows3MonsAgo}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView custCode = (TextView) view.findViewById(R.id.rowsCustCode);
                    TextView custName = (TextView) view.findViewById(R.id.rowsCustName);
                    TextView prevMon = (TextView) view.findViewById(R.id.rowsPrevMon);
                    TextView twoMonsAgo = (TextView) view.findViewById(R.id.rows2MonsAgo);
                    TextView threeMonsAgo = (TextView) view.findViewById(R.id.rows3MonsAgo);
                    if (position % 2 == 0) {
                        custCode.setTextColor(Color.WHITE);
                        custName.setTextColor(Color.WHITE);
                        prevMon.setTextColor(Color.WHITE);
                        twoMonsAgo.setTextColor(Color.WHITE);
                        threeMonsAgo.setTextColor(Color.WHITE);
                        custCode.setTypeface(null, Typeface.BOLD);
                        custName.setTypeface(null, Typeface.BOLD);
                        prevMon.setTypeface(null, Typeface.BOLD);
                        twoMonsAgo.setTypeface(null, Typeface.BOLD);
                        threeMonsAgo.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            NBCHeader.setAdapter(laNBCHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laNBCDetails = new SimpleAdapter(this,NBCViewPList, R.layout.item_notbuyingcustomer,
                    new String[]{"CustCode","CustName","PrevMon","2MonsAgo","3MonsAgo"}, new int[]{
                    R.id.rowsCustCode, R.id.rowsCustName, R.id.rowsPrevMon, R.id.rows2MonsAgo, R.id.rows3MonsAgo}) {
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
            NBCDetails.setAdapter(laNBCDetails);
        } catch (Exception e) {

        }
    }

    public void deactivationRequest() {

        final Dialog alertDialog = new Dialog(NotBuyingCustomerReport.this);

        alertDialog.setContentView(R.layout.dialog_notbuyingcustomerremarks);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);


        final MaterialBetterSpinner spDRReason = (MaterialBetterSpinner) alertDialog.findViewById(R.id.spDRReason);
        TextView tvDADTitle = (TextView) alertDialog.findViewById(R.id.tvDADTitle);
        TextView tvDRCancel = (TextView) alertDialog.findViewById(R.id.tvDRCancel);
        TextView tvDRSubmit = (TextView) alertDialog.findViewById(R.id.tvDRSubmit);

        tvDADTitle.setText(customerName);


        String[] reason = controller.fetchNotBuyingRemarks();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, reason);
        spDRReason.setAdapter(arrayAdapter);



        tvDRCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(spDRReason.getWindowToken(), 0);
                alertDialog.dismiss();
            }
        });

        tvDRSubmit.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (Utils.isStringNullOrWhiteSpace(spDRReason.getText().toString())){
                    Toasty.error(getApplicationContext(),"please choose remarks", Toast.LENGTH_LONG).show();
                }else{

                    controller.requestDeactivation(customerCode,spDRReason.getText().toString());

                    Toasty.success(getApplicationContext(),"Deactivation request successfully saved",Toast.LENGTH_LONG).show();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(spDRReason.getWindowToken(), 0);

                    alertDialog.dismiss();
                }
            }
        });

        alertDialog.show();

        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }



    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(NotBuyingCustomerReport.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }

}
