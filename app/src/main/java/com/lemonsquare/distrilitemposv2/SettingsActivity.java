package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;


public class SettingsActivity extends Activity {

    DBController controller = new DBController(this);
    ListView SDetails;
    ArrayList<HashMap<String, String>> hmSDetails;
    ListAdapter laSDetails;
    HashMap<String, String> mSDetail;
    String[] sSDetails = new String[]{"APPLICATION STATUS","TERMINAL ID","SERVER ADDRESS","DB NAME",
            "DB USER","DB PASSWORD","SALES DISTRICT","SLOC","PLANT","STD PMT TERM DAYS","CONTACT NUMBER HEADER",
            "CONTACT NUMBER ORDERS","CONTACT NUMBER CUSTOMER SERVICE","OFFICE ADDRESS","DEF PRICELIST","MOV AVE BUFFER",
            "MINIMUM AMOUNT"};
    ArrayList<String> alSSInfo;
    String PHeader,PDetail,PColumnName,PColumnValue;
    int PPosition;
    TextView tvTitle,tvCancel,tvSave;
    RadioButton rbFSalesman,rbETransaction,rbFRecon,rbFUpload,rbFDownload;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SDetails = (ListView) findViewById(R.id.lvSDetail);

        alSSInfo = controller.fetchdbSettings();
        ViewListview();

        SDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                if (position != 0){

                    HashMap<String, Object> obj = (HashMap<String, Object>) laSDetails.getItem(position);

                    String objHeader = (String) obj.get("Header");
                    PHeader = objHeader;
                    String objDetail = (String) obj.get("Detail");
                    PDetail = objDetail;
                    PPosition = position;

                    DialogSettings();

                }else{

                    HashMap<String, Object> obj = (HashMap<String, Object>) laSDetails.getItem(position);

                    String objHeader = (String) obj.get("Header");
                    PHeader = objHeader;
                    String objDetail = (String) obj.get("Detail");
                    PDetail = objDetail;
                    PPosition = position;

                    DialogApplicationStatus();

                }
            }
        });

    }

    public void ViewListview() {

        hmSDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sSDetails.length; i++) {
            mSDetail = new HashMap<String, String>();
            mSDetail.put("Header", sSDetails[i]);
            mSDetail.put("Detail",alSSInfo.get(i));
            hmSDetails.add(mSDetail);
        }

        try {
            laSDetails = new SimpleAdapter(this, hmSDetails, R.layout.item_settings,
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

            SDetails.setAdapter(laSDetails);

        } catch (Exception e) {

        }
    }

    public void UpdateListview(){

        mSDetail = new HashMap<String, String>();
        mSDetail.put("Header",  PHeader);
        mSDetail.put("Detail",  PDetail);
        hmSDetails.set(PPosition,mSDetail);

        try {
            laSDetails = new SimpleAdapter(this, hmSDetails, R.layout.item_settings,
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

            SDetails.setAdapter(laSDetails);
        } catch (Exception e) {

        }
    }

    public void DialogSettings() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        tvTitle = (TextView)dialogView.findViewById(R.id.tvDSTitle);
        tvCancel = (TextView)dialogView.findViewById(R.id.tvDSCancel);
        tvSave = (TextView)dialogView.findViewById(R.id.tvDSSave);
        final EditText dsDetail =(EditText)dialogView.findViewById(R.id.etDSDetail);

        tvTitle.setText(PHeader);
        dsDetail.setHint(PHeader);
        dsDetail.setText(PDetail);
        dsDetail.setSelection(dsDetail.getText().length());

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PDetail = dsDetail.getText().toString();
                if (PPosition == 1){
                    PColumnName = "TerminalID";
                }else if (PPosition == 2){
                    PColumnName = "ServerAddress";
                }
                else if (PPosition == 3){
                    PColumnName = "DatabaseName";
                }
                else if (PPosition == 4){
                    PColumnName = "DatabaseUsername";
                }
                else if (PPosition == 5){
                    PColumnName = "DatabasePassword";
                }
                else if (PPosition == 6){
                    PColumnName = "SalesDistrict";
                }
                else if (PPosition == 7){
                    PColumnName = "Sloc";
                }
                else if (PPosition == 8){
                    PColumnName = "Plant";
                }
                else if (PPosition == 9){
                    PColumnName = "PaymentTermDays";
                }
                else if (PPosition == 10){
                    PColumnName = "ContactNumberHeader";
                }
                else if (PPosition == 11){
                    PColumnName = "ContactNumberOrders";
                }
                else if (PPosition == 12){
                    PColumnName = "ContactNumberCustomerService";
                }
                else if (PPosition == 13){
                    PColumnName = "OfficeAddress";
                }
                else if (PPosition == 14){
                    PColumnName = "DefaultPricelist";
                }
                else if (PPosition == 15){
                    PColumnName = "MovingAverageBuffer";
                }
                else {
                    PColumnName = "MinimumAmount";
                }

                controller.updateSettings(PColumnName,dsDetail.getText().toString());
                UpdateListview();
                alertDialog.dismiss();

                Toasty.info(getApplicationContext(), PHeader + " has been updated", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();

    }

    public void DialogApplicationStatus() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        LayoutInflater inflater = SettingsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_applicationstatus, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        tvTitle = (TextView)dialogView.findViewById(R.id.tvDASTitle);
        tvCancel = (TextView)dialogView.findViewById(R.id.tvDASCancel);
        tvSave = (TextView)dialogView.findViewById(R.id.tvDASSave);
        rbFSalesman = (RadioButton)dialogView.findViewById(R.id.rbDASFSalesman);
        rbETransaction = (RadioButton)dialogView.findViewById(R.id.rbDASETransaction);
        rbFRecon = (RadioButton)dialogView.findViewById(R.id.rbDASFRecon);
        rbFUpload = (RadioButton)dialogView.findViewById(R.id.rbDASFUpload);
        rbFDownload = (RadioButton)dialogView.findViewById(R.id.rbDASFDownload);

        tvTitle.setText(PHeader);

        if (PDetail.equals("For Salesman")){
            rbFSalesman.setChecked(true);
        }else if (PDetail.equals("Ended Transaction")){
            rbETransaction.setChecked(true);
        }else if (PDetail.equals("Done with PID. For Recon")){
            rbFRecon.setChecked(true);
        }else if (PDetail.equals("For Upload")){
            rbFUpload.setChecked(true);
        }else{
            rbFDownload.setChecked(true);
        }

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PColumnName = "Status";

                if(rbFSalesman.isChecked()){
                    PColumnValue = "1";
                    PDetail = "For Salesman";
                }else if (rbETransaction.isChecked()){
                    PColumnValue = "2";
                    PDetail = "Ended Transaction";
                }else if (rbFRecon.isChecked()){
                    PColumnValue = "3";
                    PDetail = "Done with PID. For Recon";
                }else if (rbFUpload.isChecked()){
                    PColumnValue = "4";
                    PDetail = "For Upload";
                }else{
                    PColumnValue = "5";
                    PDetail = "For Download";
                }

                controller.updateSettings(PColumnName,PColumnValue);
                UpdateListview();
                alertDialog.dismiss();

                Toasty.info(getApplicationContext(), PHeader + " has been updated", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();

    }

    public void onBackPressed() {
        Intent IntentMainActivity = new Intent(SettingsActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }

}


