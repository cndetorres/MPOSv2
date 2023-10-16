package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class PriceSurveyActivity extends Activity {

    DBController controller = new DBController(this);
    ListView PSHeader,PSDetails;
    SimpleAdapter laPSDetails;
    BottomNavigationView PSmenu;
    ArrayList<HashMap<String, String>> hmPSHeader;
    ListAdapter laPSHeader;
    HashMap<String, String> mPSHeader,mPSDetail;
    String customerCode;
    List<HashMap<String, String>> priceSurveyList;
    private long mLastClickTime = 0;
    DecimalFormat PSAmt = new DecimalFormat("#,###,##0.00");
    Context context = this;

    Boolean isNothing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricesurvey);

        PSHeader = (ListView) findViewById(R.id.lvPSHeader);
        PSDetails = (ListView) findViewById(R.id.lvPSDetails);
        PSmenu = (BottomNavigationView) findViewById(R.id.btPSNavigation);

        ViewHeaderListview();
        Intent intent = getIntent();
        customerCode = intent.getStringExtra("CustomerCode");
        priceSurveyList = controller.fetchPriceSurveyList(controller.fetchSegNm(customerCode));
        ViewDetailListview();

        PSDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laPSDetails.getItem(position);

                String objItem = (String) obj.get("Item");
                String objPrice = (String) obj.get("Price");
                String objUnit = (String) obj.get("Unit");
                DialogEditItem(objItem,objPrice,objUnit,position);

            }
        });

        PSmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mps_submit:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if (laPSDetails.getCount() == 0){
                                    isNothing = true;
                                    messagebox("No data on the list. Do you want to proceed to the next activity?");

                                }else{
                                    isNothing = false;
                                    messagebox("Are you sure you want to send Price Survey data?");
                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmPSHeader = new ArrayList<HashMap<String, String>>();
        mPSHeader = new HashMap<String, String>();


        mPSHeader.put("Item", "ITEM");
        mPSHeader.put("Price", "PRICE");
        mPSHeader.put("Unit", "UNIT");
        hmPSHeader.add(mPSHeader);

        try {
            laPSHeader = new SimpleAdapter(this, hmPSHeader,  R.layout.item_pricesurvey,
                    new String[]{"Item", "Price", "Unit"}, new int[]{
                    R.id.rowsItem, R.id.rowsPrice, R.id.rowsUnit}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView rqty = (TextView) view.findViewById(R.id.rowsPrice);
                    TextView runit = (TextView) view.findViewById(R.id.rowsUnit);
                    if (position % 2 == 0) {
                        ritem.setTextColor(Color.WHITE);
                        rqty.setTextColor(Color.WHITE);
                        runit.setTextColor(Color.WHITE);
                        ritem.setTypeface(null, Typeface.BOLD);
                        rqty.setTypeface(null, Typeface.BOLD);
                        runit.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            PSHeader.setAdapter(laPSHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview() {


        try {
            laPSDetails = new SimpleAdapter(this, priceSurveyList, R.layout.item_pricesurvey,
                    new String[]{"Item", "Price", "Unit"}, new int[]{
                    R.id.rowsItem, R.id.rowsPrice, R.id.rowsUnit}) {
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
            PSDetails.setAdapter(laPSDetails);
        } catch (Exception e) {

        }

    }

    public void DialogEditItem(String product, String price, String unit, final int position) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PriceSurveyActivity.this);
        LayoutInflater inflater = PriceSurveyActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_editpricesurvey, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


        final TextView tvItem = (TextView) dialogView.findViewById(R.id.tvPSItem);
        final TextView tvUnit = (TextView) dialogView.findViewById(R.id.tvPSUnitItem);
        final EditText etPrice = (EditText) dialogView.findViewById(R.id.etPSPrice);
        TextView cancel = (TextView) dialogView.findViewById(R.id.tvPSCancel);
        TextView save = (TextView) dialogView.findViewById(R.id.tvPSSave);

        tvItem.setText(product);
        etPrice.setText(price);
        tvUnit.setText(unit);

        etPrice.requestFocus();

        etPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etPrice.length() > 1){
                    if (etPrice.getText().toString().substring(0,1).equals("0") || etPrice.getText().toString().substring(0,1).equals(".")){
                        etPrice.setText(etPrice.getText().toString().substring(1));
                        etPrice.setSelection(etPrice.getText().length());
                    }
                }
            }

        });

        cancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etPrice.getWindowToken(), 0);
                alertDialog.dismiss();

            }
        });

        save.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (Double.valueOf(etPrice.getText().toString()) > 999){
                    Toasty.error(getApplicationContext(),"Invalid Amount" , Toast.LENGTH_LONG).show();
                }else{
                    UpdateDetailListview(tvItem.getText().toString(),PSAmt.format(Double.valueOf(etPrice.getText().toString())),tvUnit.getText().toString(),position);
                    Toasty.success(getApplicationContext(),"Item updated" , Toast.LENGTH_LONG).show();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etPrice.getWindowToken(), 0);
                    alertDialog.dismiss();
                }

            }
        });

        alertDialog.show();
    }


    public void UpdateDetailListview(String product,String price, String unit,int position){

        mPSDetail = new HashMap<String, String>();

        mPSDetail.put("Item", product);
        mPSDetail.put("Price", price);
        mPSDetail.put("Unit", unit);
        priceSurveyList.set(position,mPSDetail);
        laPSDetails.notifyDataSetChanged();

    }


    void  messagebox(String alerttext) {

        new android.app.AlertDialog.Builder(PriceSurveyActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Price Survey")
                .setMessage(alerttext)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                        Calendar defaultDate = Calendar.getInstance();
                        String logDate;
                        logDate = defaultDateFormat2.format(defaultDate.getTime());

                        if (isNothing){
                            controller.insertCustomerLogsItem(controller.fetchLogID(),6,logDate,1);
                            Toasty.success(getApplicationContext(), "Display info successfully skipped.", Toast.LENGTH_LONG).show();
                        }else{
                            int count = 1;
                            String item = "";
                            String message = "";

                            for (int i = 0; i < laPSDetails.getCount(); i++) {

                                HashMap<String, Object> obj = (HashMap<String, Object>) laPSDetails.getItem(i);
                                String objProduct = (String) obj.get("Item");
                                String itemCode = controller.fetchProdCd(objProduct);
                                String objPrice = (String) obj.get("Price");


                                item = item + itemCode + ":" + objPrice + "/";

                                if (count%10 == 0) {
                                    message = "PSMPOS " + customerCode + "," + item;
                                    Utils.sendSMS(PriceSurveyActivity.this,message);
                                    item = "";
                                }else if (count == laPSDetails.getCount()) {
                                    message = "PSMPOS " + customerCode + "," + item;
                                    Utils.sendSMS(PriceSurveyActivity.this,message);
                                }
                                count ++;

                            }



                            logDate = defaultDateFormat2.format(defaultDate.getTime());
                            controller.insertCustomerLogsItem(controller.fetchLogID(),6,logDate,0);


                            Toasty.success(getApplicationContext(), "Price Survey successfully sent", Toast.LENGTH_LONG).show();
                        }

                        Intent intent = new Intent(context, CustomerCheckInActivity.class);
                        intent.putExtra("CustomerCode", controller.PCCode);
                        startActivity(intent);
                        finish();

                        dialog.dismiss();

                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public void onBackPressed() {
        finish();
    }
}
