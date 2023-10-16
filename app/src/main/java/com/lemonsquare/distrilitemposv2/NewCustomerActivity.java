package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;

public class NewCustomerActivity extends Activity{

    DBController controller = new DBController(this);
    MaterialBetterSpinner mbsNCCType;
    CheckBox chkNCM,chkNCT,chkNCW,chkNCTH,chkNCF,chkNCS,chkNCHome;
    EditText etNCCName,etNCOName,etNCTelephone,etNCMobile,etNCBAddress,etNCBCity,etNCBRegion,etNCBPCode,etNCHAddress,
            etNCHCity,etNCHRegion,etNCHPCode,etNCRemarks;
    TextView tvNCHAddress,tvNCHCity,tvNCHRegion,tvNCHPCode,tvNCDP;
    Button btnNCSubmit;
    List<String> NCListSettings;
    String NCCCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newcustomer);

        etNCCName = (EditText) findViewById(R.id.etNCCName);
        mbsNCCType = (MaterialBetterSpinner) findViewById(R.id.spNCCType);
        etNCOName = (EditText) findViewById(R.id.etNCOName);
        chkNCM = (CheckBox) findViewById(R.id.chkNCMonday);
        chkNCT = (CheckBox) findViewById(R.id.chkNCTuesday);
        chkNCW = (CheckBox) findViewById(R.id.chkNCWednesday);
        chkNCTH = (CheckBox) findViewById(R.id.chkNCThursday);
        chkNCF = (CheckBox) findViewById(R.id.chkNCFriday);
        chkNCS = (CheckBox) findViewById(R.id.chkNCSaturday);
        etNCTelephone = (EditText) findViewById(R.id.etNCTelephone);
        etNCMobile = (EditText) findViewById(R.id.etNCMphone);
        etNCBAddress = (EditText) findViewById(R.id.etNCBAddress);
        etNCBCity = (EditText) findViewById(R.id.etNCBCity);
        etNCBRegion = (EditText) findViewById(R.id.etNCBRegion);
        etNCBPCode = (EditText) findViewById(R.id.etNCBPCode);
        chkNCHome = (CheckBox) findViewById(R.id.chkNCHome);
        etNCMobile = (EditText) findViewById(R.id.etNCMphone);
        tvNCHAddress = (TextView) findViewById(R.id.tvNCHAddress);
        etNCHAddress = (EditText) findViewById(R.id.etNCHAddress);
        tvNCHCity = (TextView) findViewById(R.id.tvNCHCity);
        etNCHCity = (EditText) findViewById(R.id.etNCHCity);
        tvNCHRegion = (TextView) findViewById(R.id.tvNCHRegion);
        etNCHRegion = (EditText) findViewById(R.id.etNCHRegion);
        tvNCHPCode= (TextView) findViewById(R.id.tvNCHPCode);
        etNCHPCode = (EditText) findViewById(R.id.etNCHPCode);
        etNCRemarks = (EditText) findViewById(R.id.etNCRemarks);
        btnNCSubmit = (Button) findViewById(R.id.btnNCSubmit);
        tvNCDP = (TextView) findViewById(R.id.tvNCDP);

        controller.PCName = "Id";
        controller.PTName = "Customer";
        controller.PMNumber = controller.fetchMaxNumTCTSequence();

        controller.dbLReturns = 0.00;


        NCListSettings = controller.fetchdbSettings();

        controller.PDefaultPricelist = NCListSettings.get(14);

        DateFormat df = new SimpleDateFormat("yyMMdd");
        String date = df.format(Calendar.getInstance().getTime());

        NCCCode = "";

        NCCCode = "NW" + date + NCListSettings.get(1) + controller.PMNumber;

        String[] strNIRRType = controller.fetchCusTypeDescCustomerType();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, strNIRRType);
        mbsNCCType.setAdapter(arrayAdapter);

        chkNCHome.setChecked(true);
        UnViewHomeAddress();

        chkNCHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UnViewHomeAddress();
                } else {
                    ViewHomeAddress();
                }
            }
        });

        tvNCDP.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                DialogDataPrivacy();
            }
        });


        btnNCSubmit.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (isStringNullOrWhiteSpace(etNCCName.getText().toString())){
                    etNCCName.setError("please input customer name");
                }else if (mbsNCCType.getText().toString().equals("")){
                    mbsNCCType.setError("please select customer type");
                }else if (isStringNullOrWhiteSpace(etNCOName.getText().toString())){
                    etNCOName.setError("please input owner name");
                }else if (isStringNullOrWhiteSpace(etNCBAddress.getText().toString())){
                    etNCBAddress.setError("please input business address");
                }else if (isStringNullOrWhiteSpace(etNCBCity.getText().toString())){
                    etNCBCity.setError("please input city");
                }else {


                    if (chkNCHome.isChecked()){
                        etNCHAddress.setText(etNCBAddress.getText().toString());
                        etNCHCity.setText(etNCBCity.getText().toString());
                        etNCHRegion.setText(etNCBRegion.getText().toString());
                        etNCHPCode.setText(etNCBPCode.getText().toString());
                    }


                    /*controller.insertCustomer(NCCCode,etNCCName.getText().toString(),etNCOName.getText().toString(),etNCTelephone.getText().toString(),NCVisitDays(),NCListSettings.get(14),NCListSettings.get(6),etNCBCity.getText().toString(),
                            etNCBAddress.getText().toString(), StringUtils.substringBefore(mbsNCCType.getText().toString(), "-"),etNCMobile.getText().toString(),etNCBPCode.getText().toString(),etNCBRegion.getText().toString(),
                            etNCHAddress.getText().toString(),etNCHPCode.getText().toString(),etNCHCity.getText().toString(),etNCHRegion.getText().toString(),controller.PMNumber, NCListSettings.get(9),etNCRemarks.getText().toString());*/

                    controller.PCLName = NCCCode + "-" + etNCCName.getText().toString();

                    controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);

                   /* try{
                        controller.export();
                        scanFile(controller.backupDB.getAbsolutePath());
                        //scanMedia(controller.backupDB);
                    }catch (Exception e){
                        controller.exports = "/storage/emulated/0/Documents/Exports";
                        controller.export();
                        //scanMedia(controller.backupDB);
                        scanFile(controller.backupDB.getAbsolutePath());
                    }*/

                    File exportpath = new File(controller.exports);
                    if (exportpath.exists()){
                        controller.export();
                        scanFile(controller.backupDB.getAbsolutePath());
                    }else{
                        String exportfile;
                        exportfile = "/storage/emulated/0/Documents/Exports";
                        File export = new File(exportfile);
                        if (export.exists()){
                            controller.export(exportfile);
                            scanFile(controller.backupDB.getAbsolutePath());
                        }else{
                            export.mkdir();
                            scanFile(export.getAbsolutePath());
                            controller.export(exportfile);
                            scanFile(controller.backupDB.getAbsolutePath());
                        }

                    }


                    Toasty.success(getApplicationContext(),"database backup successfully",Toast.LENGTH_LONG).show();
                    //Toasty.success(getApplicationContext(), "new customer successfully saved!", Toast.LENGTH_LONG).show();

                    new AlertDialog.Builder(NewCustomerActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("New Customer")
                            .setCancelable(false)
                            .setMessage("Customer successfull created, do you want to transact now?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    controller.Prscl = 2;

                                    controller.PIsSOrder = 1;

                                    controller.PPayment = 0;

                                    controller.PIndicator = 0;

                                    controller.dbGAmt = 0.00;
                                    controller.dbNSales = 0.00;
                                    controller.dbCGiven = 0.00;
                                    controller.PDiscAmt = 0.00;
                                    controller.PDiscount = 0.00;

                                    controller.PNCCode = NCCCode + etNCCName.getText().toString();



                                    Intent intentNewCustomerCheckInActivity = new Intent(NewCustomerActivity.this, NewCustomerCheckInActivity.class);
                                    startActivity(intentNewCustomerCheckInActivity);

                                    finish();

                                }

                            })
                            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }

                            })
                            .show();



                }
            }
        });

    }

    public void DialogDataPrivacy() {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(NewCustomerActivity.this);
        LayoutInflater inflater = NewCustomerActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_dataprivacy, null);
        dialogBuilder.setView(dialogView);
        final android.app.AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        TextView tvDROk = (TextView) dialogView.findViewById(R.id.tvDROk);

        TextView tvRDP = (TextView) dialogView.findViewById(R.id.tvRDP);


        tvRDP.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);


        tvDROk.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });



        alertDialog.show();

    }

    public void ViewHomeAddress(){
        tvNCHAddress.setVisibility(View.VISIBLE);
        etNCHAddress.setVisibility(View.VISIBLE);
        tvNCHCity.setVisibility(View.VISIBLE);
        etNCHCity.setVisibility(View.VISIBLE);
        tvNCHRegion.setVisibility(View.VISIBLE);
        etNCHRegion.setVisibility(View.VISIBLE);
        tvNCHPCode.setVisibility(View.VISIBLE);
        etNCHPCode.setVisibility(View.VISIBLE);
    }

    public void UnViewHomeAddress(){
        tvNCHAddress.setVisibility(View.GONE);
        etNCHAddress.setVisibility(View.GONE);;
        tvNCHCity.setVisibility(View.GONE);
        etNCHCity.setVisibility(View.GONE);
        tvNCHRegion.setVisibility(View.GONE);
        etNCHRegion.setVisibility(View.GONE);
        tvNCHPCode.setVisibility(View.GONE);
        etNCHPCode.setVisibility(View.GONE);

        etNCHAddress.getText().clear();
        etNCHCity.getText().clear();
        etNCHRegion.getText().clear();
        etNCHPCode.getText().clear();
    }

    private String NCVisitDays() {
        List<String> visitDays = new ArrayList<>();
        if (chkNCM.isChecked()) {
            visitDays.add("1");
        }
        if (chkNCT.isChecked()) {
            visitDays.add("2");
        }
        if (chkNCW.isChecked()) {
            visitDays.add("3");
        }
        if (chkNCTH.isChecked()) {
            visitDays.add("4");
        }
        if (chkNCF.isChecked()) {
            visitDays.add("5");
        }
        if (chkNCS.isChecked()) {
            visitDays.add("6");
        }
        return TextUtils.join(",", visitDays);
    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(NewCustomerActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("New Customer")
                .setMessage(alerttext)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }

                })
                .show();
    }

    private void scanMedia(File path) {
        Uri uri = Uri.fromFile(path);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(NewCustomerActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    public static boolean isStringNullOrWhiteSpace(String value) {
        if (value == null) {
            return true;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }


    public void onBackPressed() {

        finish();
    }

}
