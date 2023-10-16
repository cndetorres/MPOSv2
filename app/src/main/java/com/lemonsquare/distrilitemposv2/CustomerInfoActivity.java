package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class CustomerInfoActivity extends Activity {

    DBController controller = new DBController(this);
    ListView CIDetails;
    ArrayList<HashMap<String, String>> hmCIDetails;
    ListAdapter laCIDetails;
    HashMap<String, String> mCIDetail;
    String[] sCIDetails = new String[]{"STORE NAME","OWNER NAME","CUSTOMER CODE","CTYPE","C LIMIT","C EXPO",
    "ROUTE","TELEPHONE","MOBILE NO","BUSINESS ADDRESS","HOME ADDRESS","REMARKS"};
    ArrayList<String> alCICInfo;
    String PHeader,PDetail,PColumnName;
    int PPosition;
    TextView tvTitle,tvCancel,tvSave;
    List<String> lCIBAddress,lCIHAddress;

    private long mLastClickTime = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerinfo);

        CIDetails = (ListView) findViewById(R.id.lvCIDetail);

        //controller.PCCode = controller.fetchCCodeCustomers();

        /*if (controller.fetchSUMARBalances() < 0.00){
            controller.updateCreditExpo(0.00);
        }else{
            controller.updateCreditExpo(controller.fetchSUMARBalances());
        }*/

        //controller.updateCreditExpo(controller.fetchSUMARBalances() + controller.fetchSUMChecks());//- controller.fetchSUMAmtPdPaymentCash()

        alCICInfo = controller.fetchCustomerInfo();
        ViewListview();

        CIDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                    if (position == 7 || position == 8 || position == 11){

                        HashMap<String, Object> obj = (HashMap<String, Object>) laCIDetails.getItem(position);

                        String objHeader = (String) obj.get("Header");
                        PHeader = objHeader;
                        String objDetail = (String) obj.get("Detail");
                        PDetail = objDetail;
                        PPosition = position;

                        DialogCustomerInfo();

                    }else if ((position == 9 || position == 10 )){

                        HashMap<String, Object> obj = (HashMap<String, Object>) laCIDetails.getItem(position);

                        String objHeader = (String) obj.get("Header");
                        PHeader = objHeader;
                        String objDetail = (String) obj.get("Detail");
                        PDetail = objDetail;
                        PPosition = position;

                        DialogCustomerAddress();


                }


            }
        });




    }

    public void ViewListview() {

        hmCIDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sCIDetails.length; i++) {
            mCIDetail = new HashMap<String, String>();
            mCIDetail.put("Header", sCIDetails[i]);
            mCIDetail.put("Detail",alCICInfo.get(i));
            hmCIDetails.add(mCIDetail);
        }

        try {
            laCIDetails = new SimpleAdapter(this, hmCIDetails, R.layout.item_customerinfo,
                    new String[]{"Header","Detail"}, new int[]{
                    R.id.rowsHeader,R.id.rowsDetail}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    ImageView ivIcon = (ImageView) view.findViewById(R.id.ivIcon);


                        if (position == 7){
                            ivIcon.setVisibility(View.VISIBLE);
                        }else if (position == 8){
                            ivIcon.setVisibility(View.VISIBLE);
                        }else if (position == 9){
                            ivIcon.setVisibility(View.VISIBLE);
                        }else if (position == 10){
                            ivIcon.setVisibility(View.VISIBLE);
                        }else if (position == 11){
                            ivIcon.setVisibility(View.VISIBLE);
                        }else {
                            ivIcon.setVisibility(View.INVISIBLE);
                        }

                    if (position % 2 == 1) {
                        view.setBackgroundColor(Color.parseColor("#fff2dc"));
                    } else {
                        view.setBackgroundColor(Color.parseColor("#f7e1a8"));
                    }
                    return view;
                }
            };

            CIDetails.setAdapter(laCIDetails);

        } catch (Exception e) {

        }
    }

    public void DialogCustomerInfo() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CustomerInfoActivity.this);
        LayoutInflater inflater =CustomerInfoActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customerinfo, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        tvTitle = (TextView)dialogView.findViewById(R.id.tvDCITitle);
        tvCancel = (TextView)dialogView.findViewById(R.id.tvDCICancel);
        tvSave = (TextView)dialogView.findViewById(R.id.tvDCISave);
        final EditText ciDetail =(EditText)dialogView.findViewById(R.id.etDCIDetail);

        tvTitle.setText(PHeader);
        ciDetail.setHint(PHeader);
        ciDetail.setText(PDetail);
        ciDetail.setSelection(ciDetail.getText().length());

        tvCancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        if (PPosition == 7){
            ciDetail.setFilters(new InputFilter[] { new InputFilter.LengthFilter(8) });
            ciDetail.setInputType(InputType.TYPE_CLASS_PHONE);
        }else if (PPosition == 8){
            ciDetail.setFilters(new InputFilter[] { new InputFilter.LengthFilter(13) });
            ciDetail.setInputType(InputType.TYPE_CLASS_PHONE);
        }

        tvSave.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                PDetail = ciDetail.getText().toString();
                if (PPosition == 7){
                    PColumnName = "ContactNumber";
                }else if (PPosition == 8){
                    PColumnName = "MobileNumber";
                }
                else if (PPosition ==  11){
                    PColumnName = "Remarks";
                }

                if (controller.fetchCustomer().get(5).equals("6")){
                    controller.updateCustomerInfo(PColumnName,ciDetail.getText().toString(),6);
                }else{
                    controller.updateCustomerInfo(PColumnName,ciDetail.getText().toString(),7);
                }


                RefreshListView();
                alertDialog.dismiss();

                Toast.makeText(getApplicationContext(), PHeader + " has been updated", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();

    }

    public void DialogCustomerAddress() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CustomerInfoActivity.this);
        LayoutInflater inflater =CustomerInfoActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customeraddress, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        tvTitle = (TextView)dialogView.findViewById(R.id.tvDCATitle);
        tvCancel = (TextView)dialogView.findViewById(R.id.tvDCACancel);
        tvSave = (TextView)dialogView.findViewById(R.id.tvDCASave);
        final EditText ciStreet =(EditText)dialogView.findViewById(R.id.etDCAStreet);
        final EditText ciCity =(EditText)dialogView.findViewById(R.id.etDCACity);
        final EditText ciRegion =(EditText)dialogView.findViewById(R.id.etDCARegion);
        final EditText ciPCode =(EditText)dialogView.findViewById(R.id.etDCAPCode);

        lCIBAddress = controller.fetchCustomerInfoBAddress();
        lCIHAddress = controller.fetchCustomerInfoHAddress();

        if (PPosition == 9){
            ciStreet.setText(lCIBAddress.get(0));
            ciCity.setText(lCIBAddress.get(1));
            ciRegion.setText(lCIBAddress.get(2));
            ciPCode.setText(lCIBAddress.get(3));
        }else if (PPosition == 10){
            ciStreet.setText(lCIHAddress.get(0));
            ciCity.setText(lCIHAddress.get(1));
            ciRegion.setText(lCIHAddress.get(2));
            ciPCode.setText(lCIHAddress.get(3));
        }

        tvTitle.setText(PHeader);

        tvCancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvSave.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (controller.fetchCustomer().get(5).equals("6")){
                    if (PPosition == 9){
                        controller.updateCustomerInfo("Street",ciStreet.getText().toString(),6);
                        controller.updateCustomerInfo("City",ciCity.getText().toString(),6);
                        controller.updateCustomerInfo("Region",ciRegion.getText().toString(),6);
                        controller.updateCustomerInfo("Postal",ciPCode.getText().toString(),6);
                    }else if (PPosition == 10) {
                        controller.updateCustomerInfo("StreetH",ciStreet.getText().toString(),6);
                        controller.updateCustomerInfo("CityH",ciCity.getText().toString(),6);
                        controller.updateCustomerInfo("RegionH",ciRegion.getText().toString(),6);
                        controller.updateCustomerInfo("PostalH",ciPCode.getText().toString(),6);
                    }
                }else{
                    if (PPosition == 9){
                        controller.updateCustomerInfo("Street",ciStreet.getText().toString(),7);
                        controller.updateCustomerInfo("City",ciCity.getText().toString(),7);
                        controller.updateCustomerInfo("Region",ciRegion.getText().toString(),7);
                        controller.updateCustomerInfo("Postal",ciPCode.getText().toString(),7);
                    }else if (PPosition == 10) {
                        controller.updateCustomerInfo("StreetH",ciStreet.getText().toString(),7);
                        controller.updateCustomerInfo("CityH",ciCity.getText().toString(),7);
                        controller.updateCustomerInfo("RegionH",ciRegion.getText().toString(),7);
                        controller.updateCustomerInfo("PostalH",ciPCode.getText().toString(),7);
                    }
                }




                RefreshListView();

                alertDialog.dismiss();


                Toasty.info(getApplicationContext(), PHeader + " has been updated", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();

    }

    public void RefreshListView() {

        alCICInfo.clear();
        CIDetails.invalidateViews();

        alCICInfo = controller.fetchCustomerInfo();
        ViewListview();

    }

    public void onBackPressed() {

        /*if (controller.Prscl == 1){
            Intent IntentRouteScheduleActivity = new Intent(CustomerInfoActivity.this, RouteScheduleActivity.class);
            startActivity(IntentRouteScheduleActivity);
            finish();

        }else{
            Intent IntentCustomerListActivity = new Intent(CustomerInfoActivity.this, CustomerListActivity.class);
            startActivity(IntentCustomerListActivity);
            finish();
        }*/

        finish();

    }
}


