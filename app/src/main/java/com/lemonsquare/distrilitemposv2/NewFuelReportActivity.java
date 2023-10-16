package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class NewFuelReportActivity extends Activity {

    DBController controller = new DBController(this);
    EditText etNFRDetails,etNFRAmt,etNFRLiters,etNFROdometer,etNFRPONum,etNFRInvNum;
    Button btnNFRSubmit;
    List<String> NFRListUiDVNoOMeter;
    BottomNavigationView btNFRNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newfuelreport);

        etNFRDetails = (EditText) findViewById(R.id.etNFRDetails);
        etNFRAmt = (EditText) findViewById(R.id.etNFRAmt);
        etNFRLiters = (EditText) findViewById(R.id.etNFRLiters);
        etNFROdometer = (EditText) findViewById(R.id.etNFROdo);
        etNFRPONum = (EditText) findViewById(R.id.etNFRPONum);
        etNFRInvNum = (EditText) findViewById(R.id.etNFRInvNum);
        btnNFRSubmit = (Button) findViewById(R.id.btnNFRSubmit);
        //btNFRNavigation = (BottomNavigationView) findViewById(R.id.btNFRNavigation);

        controller.PCName = "FuelReportId";
        controller.PMNumber = controller.fetchMaxNumTSequence();

        NFRListUiDVNoOMeter = controller.fetchUiDVNoOReading();

//        btNFRNavigation.setOnNavigationItemSelectedListener(
//                new BottomNavigationView.OnNavigationItemSelectedListener() {
//                    @Override
//                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                        switch (item.getItemId()) {
//                            case R.id.mnfl_submit:
//
//                                if (etNFRDetails.getText().toString().equals("")) {
//                                    etNFRDetails.setError("please input details");
//                                } else if (etNFRAmt.getText().toString().equals("")) {
//                                    etNFRAmt.setError("please input amount");
//                                } else if (etNFRLiters.getText().toString().equals("")) {
//                                    etNFRLiters.setError("please input liters");
//                                } else if (etNFROdometer.getText().toString().equals("")) {
//                                    etNFROdometer.setError("please input odometer reading");
//                                } else if (etNFRPONum.getText().toString().equals("")) {
//                                    etNFRPONum.setError("please input PO number");
//                                } else {
//                                    Timestamp tmNFRDtTime = new Timestamp(System.currentTimeMillis());
//
//                                    controller.insertFuelReport(controller.PMNumber, tmNFRDtTime.getTime(), etNFRLiters.getText().toString(), etNFRAmt.getText().toString(), etNFRDetails.getText().toString(), etNFROdometer.getText().toString(), etNFRPONum.getText().toString(), NFRListUiDVNoOMeter.get(0), NFRListUiDVNoOMeter.get(1), etNFRInvNum.getText().toString());
//                                    controller.updateTableSequence(Integer.valueOf(controller.PMNumber) + 1);
//
//                                    controller.export();
//
//                                    scanMedia(controller.backupDB);
//
//                                    Toasty.success(getApplicationContext(), "database backup successfully", Toast.LENGTH_LONG).show();
//
//                                    //Toasty.success(getApplicationContext(), "fuel report successfully saved!", Toast.LENGTH_LONG).show();
//
//                                    Intent intentNewIncdentReport = new Intent(NewFuelReportActivity.this, FuelReportsActivity.class);
//                                    startActivity(intentNewIncdentReport);
//                                    finish();
//
//                                }
//
//
//                                break;
//                        }
//                        return true;
//                    }
//                });
//    }

        btnNFRSubmit.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (isStringNullOrWhiteSpace(etNFRDetails.getText().toString())){
                    etNFRDetails.setError("please input details");
                }else if (isStringNullOrWhiteSpace(etNFRAmt.getText().toString())){
                    etNFRAmt.setError("please input amount");
                }else if (isStringNullOrWhiteSpace(etNFRLiters.getText().toString())){
                    etNFRLiters.setError("please input liters");
                }else if (isStringNullOrWhiteSpace(etNFROdometer.getText().toString())){
                    etNFROdometer.setError("please input odometer reading");
                }else if (isStringNullOrWhiteSpace(etNFRPONum.getText().toString())){
                    etNFRPONum.setError("please input PO number");
                }else{
                    Timestamp tmNFRDtTime = new Timestamp(System.currentTimeMillis());

                    controller.insertFuelReport(controller.PMNumber,tmNFRDtTime.getTime(),etNFRLiters.getText().toString(),etNFRAmt.getText().toString(),etNFRDetails.getText().toString(),etNFROdometer.getText().toString(),etNFRPONum.getText().toString(),NFRListUiDVNoOMeter.get(1),NFRListUiDVNoOMeter.get(0),etNFRInvNum.getText().toString());
                    controller.updateTableSequence(Integer.valueOf(controller.PMNumber) + 1);

                    /*try{
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

                    //Toasty.success(getApplicationContext(), "fuel report successfully saved!", Toast.LENGTH_LONG).show();

                    Intent intentNewIncdentReport = new Intent(NewFuelReportActivity.this, FuelReportsActivity.class);
                    startActivity(intentNewIncdentReport);
                    finish();

                }
            }
        });
    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(NewFuelReportActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("New Fuel Report")
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

        MediaScannerConnection.scanFile(NewFuelReportActivity.this,
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
        Intent IntentFuelReportActivity = new Intent(NewFuelReportActivity.this, FuelReportsActivity.class);
        startActivity(IntentFuelReportActivity);
        finish();
    }
}
