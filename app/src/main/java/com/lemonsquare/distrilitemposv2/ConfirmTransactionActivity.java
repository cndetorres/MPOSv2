package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.lemonsquare.distrilitemposv2.NewCustomerActivity2.READ_BLOCK_SIZE;

public class ConfirmTransactionActivity extends Activity {

    DBController controller = new DBController(this);
    Button btnCTClear;
    File file;
    LinearLayout llCTSignature;
    View view;
    signature Signature;
    Bitmap bitmap;
    byte[] bArray,bArray1;
    BottomNavigationView menu;
    EditText etCTName;
    List<String> STListSettings,VRListOdometerReading;
    Double dSTTotalMaterial = 0.00;
    Double dSTTotal = 0.00;
    Double dbPCash = 0.00;
    Double dbPCheck = 0.00;
    Double dbPCharge = 0.00;
    Double check = 0.00;
    int PIsSignature = 0;
    String VRCode;
    String PRetID,PItem,PQty,PTotal,PRemarks,PQty1000,PQty500,PQty200,PQty100,PQty50,PQty20,Pid,PCheckDt,PCheckNo,PLiq,
    PBank,PAcctNo,PBranch,PAmt,PDeposited,PUPrice,PPMode,PPRemarks,PDCode;
    List<String> RIListSettings;
    DecimalFormat ARPAmt = new DecimalFormat("######0.00");
    boolean locked = true;
    String datediff;
    int countPayment = 0;
    int countPaymentItem = 0;
    int countPaymentSummary = 0;
    boolean changing = false;
    String dbBackUpPath = "";
    ArrayList<HashMap<String, String>> hmSODetails;
    List<HashMap<String, String>>  hmPDDetails;

    private long mLastClickTime = 0;

    String exports;

    android.support.v7.app.AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmtransaction);

        btnCTClear = (Button) findViewById(R.id.btnCTClear);
        menu = (BottomNavigationView) findViewById(R.id.btCTNavigation);
        llCTSignature = (LinearLayout) findViewById(R.id.llCTSignature);
        etCTName = (EditText) findViewById(R.id.etCTName);

        Signature = new signature(getApplicationContext(), null);
        Signature.setBackgroundColor(Color.WHITE);
        llCTSignature.addView(Signature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        view = llCTSignature;

        controller.PMNumber = "";
        VRCode = "";
        controller.RICCode = "";
        controller.SIDCode = "";
        controller.PMNumber = "";

        //Toasty.success(getApplicationContext(), "AmountPaid:" + String.valueOf(controller.dbAmtPd) + " AmountDue" + String.valueOf(controller.dbAmtDue), Toast.LENGTH_LONG).show();


        STListSettings = controller.fetchdbSettings();

        readexports();

        //Toast.makeText(this,exports,Toast.LENGTH_LONG).show();

        btnCTClear.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                Signature.clear();
                PIsSignature = 0;
            }
        });

        if (controller.PINum == 1){
            etCTName.setHint("name");
            controller.PCName = "Id";
            controller.PTName = "PhysicalInventory";
            controller.PMNumber = controller.fetchMaxNumTCTSequence();

            VRListOdometerReading = controller.fetchCustRTOdometer();

            DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
            String date = df.format(Calendar.getInstance().getTime());

            VRCode = date + STListSettings.get(1) + controller.PMNumber;

        }else if (controller.PINum == 2){
            etCTName.setHint("name");

        }else if (controller.PINum == 3){
            etCTName.setHint("customer name");
            controller.PCName = "Id";
            controller.PTName = "Returns";
            controller.PMNumber = controller.fetchMaxNumTCTSequence();
            RIListSettings = controller.fetchdbSettings();
            DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
            String date = df.format(Calendar.getInstance().getTime());

            controller.RICCode = date + RIListSettings.get(1) + controller.PMNumber;
        }else if (controller.PINum == 4){

            hmSODetails = controller.hmSODetails;
            hmPDDetails = controller.hmPDDetails;
            etCTName.setHint("customer name");

        }else if (controller.PINum == 5){

            controller.PCName = "Id";
            controller.PTName = "StockTransfer";
            controller.PMNumber = controller.fetchMaxNumTCTSequence();

            STListSettings = controller.fetchdbSettings();

            DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
            String date = df.format(Calendar.getInstance().getTime());

            controller.PSTiD = "";
            controller.PSTiD = date + STListSettings.get(1) + controller.PMNumber;
        }


        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mct_confirm:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if (PIsSignature == 0){
                                    messagebox("please enter your signature");
                                }else if (isStringNullOrWhiteSpace(etCTName.getText().toString())){
                                    etCTName.setError("please input name");
                                }else if (controller.PINum == 4 && (hmSODetails.size() == 0 || hmPDDetails.size() == 0)){
                                        messagebox2("System detects no item/s to save, Please retransact.");
                                } else{

                                    if (locked) {
                                        locked = false;

                                    view.setDrawingCacheEnabled(true);
                                    Signature.save(view);

                                        new Task().execute();

                                        Toasty.success(getApplicationContext(), "database backup successfully", Toast.LENGTH_LONG).show();

                                }

                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    private void backup(){

            File exportpath = new File(exports);
            if (exportpath.exists()){
                controller.export(exports);
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


    }

           class Task extends AsyncTask<String, Integer, Boolean> {
            @Override
            protected void onPreExecute() {
                //progressBar.setVisibility(View.VISIBLE);
                ProgressDialogView();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                // progressBar.setVisibility(View.GONE);
                progressDialog.dismiss();
                super.onPostExecute(result);
            }

        @Override
        protected Boolean doInBackground(String... params) {

            if (controller.PINum == 1) {

                DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
                String date = df.format(Calendar.getInstance().getTime());

                if (controller.fetchCountPhysicalInventory().equals("0")) {
                    controller.insertPhysicalInventory(VRListOdometerReading.get(0), controller.PUiD, VRListOdometerReading.get(1), date, bArray, controller.PMNumber, VRCode);
                } else {
                    controller.updatePhysicalInventory(VRListOdometerReading.get(0), controller.PUiD, VRListOdometerReading.get(1), date, bArray, controller.PMNumber, VRCode);
                }

                controller.updateTableSequence(Integer.valueOf(controller.PMNumber) + 1);

                for (int i = 0; i < controller.VRValidateReturns.size(); i++) {
                    Map<String, String> map = controller.VRValidateReturns.get(i);
                    for (Map.Entry<String, String> entry : map.entrySet()) {

                        if (entry.getKey().equals("_id")) {
                            PRetID = entry.getValue();
                        } else if (entry.getKey().equals("Item")) {
                            PItem = entry.getValue();
                        } else if (entry.getKey().equals("Qty")) {
                            PQty = StringUtils.substringBefore(entry.getValue(), " ");
                        }


                    }
                    controller.PMName = PItem;
                    controller.updateValidatedQtyRItem(Integer.valueOf(PQty), PRetID);

                }

                for (int i = 0; i < controller.IViewInventory.size(); i++) {

                    Map<String, String> map = controller.IViewInventory.get(i);
                    for (Map.Entry<String, String> entry : map.entrySet()) {


                        if (entry.getKey().equals("Item")) {
                            PItem = entry.getValue();
                        } else if (entry.getKey().equals("Qty")) {
                            PQty = entry.getValue();
                        }
                    }
                    controller.PMName = PItem;
                    controller.updateEndingQtyInventory(Integer.valueOf(PQty));
                }

                Timestamp tmCCIDtTime = new Timestamp(System.currentTimeMillis());
                String OdoMax = "";
                OdoMax = controller.fetchMaxNumTCTSequence("Id", "OdometerReading");
                controller.insertOdometerReading(controller.fetchRIDNmUsers().get(3), controller.PVROdometer, tmCCIDtTime.getTime(), "Final Odometer Reading", controller.fetchRIDNmUsers().get(3), controller.fetchUiDVNoOReading().get(1), OdoMax);
                controller.updateTCTableSequence("Id", "OdometerReading", Integer.valueOf(OdoMax) + 1);


                controller.updateSettings("LastOdometer", controller.PVROdometer);

                controller.updateSettings(3);

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

             /*   File exportpath = new File(exports);
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

                }*/

                backup();



                Intent intentVarianceReportActivity = new Intent(ConfirmTransactionActivity.this, VarianceReportActivity.class);
                startActivity(intentVarianceReportActivity);
                finish();

            } else if (controller.PINum == 2) {

                DateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
                String date = df.format(Calendar.getInstance().getTime());

                Timestamp tsCTDateTime = new Timestamp(System.currentTimeMillis());

                for (int i = 0; i < controller.hmCOHDetails.size(); i++) {

                    Map<String, String> map = controller.hmCOHDetails.get(i);
                    for (Map.Entry<String, String> entry : map.entrySet()) {

                        if (entry.getKey().equals("Qty")) {
                            if (i == 0) {
                                PQty1000 = entry.getValue();
                            } else if (i == 1) {
                                PQty500 = entry.getValue();

                            } else if (i == 2) {
                                PQty200 = entry.getValue();
                            } else if (i == 3) {
                                PQty100 = entry.getValue();
                            } else if (i == 4) {
                                PQty50 = entry.getValue();
                            } else {

                                PQty20 = entry.getValue();
                            }
                        }
                    }
                }


                controller.updateCashOnHandWOAgent(controller.PUiD, PQty1000, PQty500, PQty200, PQty100, PQty50, PQty20, controller.dbPCoins, controller.PCashTotal, date);
                controller.updateCashOnHandDate(date);

                controller.deleteCashDepositedCashier();

                for (int i = 0; i < controller.CDViewCDeposits.size(); i++) {

                    Map<String, String> map = controller.CDViewCDeposits.get(i);
                    for (Map.Entry<String, String> entry : map.entrySet()) {


                        if (entry.getKey().equals("Bank")) {
                            PBank = entry.getValue();
                        } else if (entry.getKey().equals("AcctNo")) {
                            PAcctNo = entry.getValue();
                        } else if (entry.getKey().equals("Branch")) {
                            PBranch = entry.getValue();
                        } else if (entry.getKey().equals("Amt")) {
                            PAmt = entry.getValue();
                        } else {
                            PDeposited = entry.getValue();
                        }
                    }
                    controller.insertCashDepositedCashier(PBank, PAcctNo, PBranch, bArray.toString(), PAmt.replace(",", ""), PDeposited, tsCTDateTime.getTime());
                }

                controller.updateCashDepositedDate(tsCTDateTime.getTime());


                for (int i = 0; i < controller.CASCViewChecks.size(); i++) {

                    Map<String, String> map = controller.CASCViewChecks.get(i);
                    for (Map.Entry<String, String> entry : map.entrySet()) {

                        if (entry.getKey().equals("ID")) {
                            Pid = entry.getValue();
                        } else if (entry.getKey().equals("CheckDt")) {
                            PCheckDt = entry.getValue();
                        } else if (entry.getKey().equals("Bank")) {
                            PBank = entry.getValue();
                        } else if (entry.getKey().equals("CheckNo")) {
                            PCheckNo = entry.getValue();
                        } else if (entry.getKey().equals("Amt")) {
                            PAmt = entry.getValue();
                        } else if (entry.getKey().equals("Liq")){
                            PLiq = entry.getValue();
                        }
                    }

                    if (PLiq.equals("YES")) {

                        try {
                            //SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
                            //Date dateComponent = sdfDate.parse(PCheckDt);
                            //Timestamp timestamp = new Timestamp(dateComponent.getTime());

                            String textDate = PCheckDt;
                            Date actualDate = null;

                            SimpleDateFormat yy = new SimpleDateFormat( "MM/dd/yy" );
                            SimpleDateFormat yyyy = new SimpleDateFormat( "MM/dd/yyyy" );

                            actualDate = yy.parse(textDate);

                            String newdate = yyyy.format(actualDate);
                            Date dateComponent = yyyy.parse(newdate);
                            Timestamp timestamp = new Timestamp(dateComponent.getTime());



                            controller.updateCheck(PBank, timestamp.getTime(), PCheckNo, PAmt.replace(",", ""), 1, date, Pid);

                            //messagebox("YES");

                        } catch (ParseException ex) {


                        }


                    } else {

                        controller.PCName = "IncidentID";
                        controller.PMNumber = controller.fetchMaxNumTSequence();
                        Timestamp NIRtsDtTime = new Timestamp(System.currentTimeMillis());

                        try {

                            String textDate = PCheckDt;
                            Date actualDate = null;

                            SimpleDateFormat tyy = new SimpleDateFormat( "MM/dd/yy" );
                            SimpleDateFormat tyyyy = new SimpleDateFormat( "MM/dd/yyyy" );

                            actualDate = tyy.parse(textDate);

                            String newdate = tyyyy.format(actualDate);
                            Date dateComponent = tyyyy.parse(newdate);
                            Timestamp timestamp = new Timestamp(dateComponent.getTime());



                            controller.updateCheck(PBank, timestamp.getTime(), PCheckNo, PAmt.replace(",", ""), 0, date, Pid);

                        } catch (ParseException ex) {


                        }



                        controller.insertIncidentReport(controller.PMNumber, "5", NIRtsDtTime.getTime(), "UNLIQUIDATED CHECK - " + PBank + " " + PCheckNo, "", "");

                        controller.updateTableSequence(Integer.valueOf(controller.PMNumber) + 1);

                    }
                }

                controller.PCashierName = etCTName.getText().toString();

                double cashonhand = 0.00;
                double cashdeposited = 0.00;
                double cashonhanddeposited = 0.00;
                double checks = 0.00;
                double stockshortage = 0.00;
                double returnshortage = 0.00;

                cashonhand = (controller.fetchTotalCOHand() - controller.fetchTotalCOHandCashier()) *-1;
                                        /*if (cashonhand >= 0.00){
                                            cashonhand = 0.00;
                                        }else{
                                            cashonhand = cashonhand * -1;
                                        }*/

                cashdeposited = (controller.fetchSUMCashDeposited() - controller.fetchSUMCashDepositedCashier()) *-1;
                                        /*if (cashdeposited >= 0.00){
                                            cashdeposited = 0.00;
                                        }else{
                                            cashdeposited = cashdeposited *-1;
                                        }*/

                if (cashonhand<0.00 && cashdeposited <0.00){
                    cashonhanddeposited = (cashonhand + cashdeposited) * -1;
                }else if (cashonhand >= 0.00 && cashdeposited <0.00){
                    cashonhanddeposited = cashonhand - (cashdeposited *-1);
                    if (cashonhanddeposited < 0.00){
                        cashonhanddeposited = cashonhanddeposited * -1;
                    }else{
                        cashonhanddeposited = 0.00;
                    }
                }else if (cashonhand < 0.00 && cashdeposited >= 0.00){
                    cashonhanddeposited = cashdeposited - (cashonhand*-1);
                    if (cashonhanddeposited < 0.00){
                        cashonhanddeposited = cashonhanddeposited * -1;
                    }else{
                        cashonhanddeposited = 0.00;
                    }
                }

                checks = (controller.fetchTotalOAmtChecks() - controller.fetchTotalAmtChecks())*-1;
                if (checks > 0.00){
                    checks = 0.00;
                }else{
                    checks = checks * -1;
                }

                stockshortage = controller.fetchSUMStockShortage()*-1 ;
                if (stockshortage < 0.00){
                    stockshortage = 0.00;
                }

                returnshortage = controller.fetchSUMReturnsShortage()*-1 ;
                if (returnshortage < 0.00){
                    returnshortage = 0.00;
                }

                controller.TotalShortage = cashonhanddeposited  + checks + controller.fetchTotalOAmtChecksUL()+ stockshortage + returnshortage;

                controller.updateCashOnHandCShortage(Double.valueOf(ARPAmt.format(controller.TotalShortage)));

                controller.updateSettings(4);

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

                /*File exportpath = new File(exports);
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

                }*/

                backup();

                controller.PIndicator = 1;

                Intent intentChecksandSummaryActivity = new Intent(ConfirmTransactionActivity.this, ChecksandSummaryActivity.class);
                startActivity(intentChecksandSummaryActivity);
                finish();

            } else if (controller.PINum == 3) {

                final long date = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                final String dateString = sdf.format(date);

                Timestamp tsRIDateTime = new Timestamp(System.currentTimeMillis());

                controller.insertReturns(controller.PCCode, dateString, bArray, controller.PMNumber, controller.RICCode, etCTName.getText().toString(), String.valueOf(controller.dbTotalAmt - (controller.dbTotalAmt * controller.PDiscount)));

                controller.insertTransaction("RET" + controller.RICCode, "RET", controller.PCLName, controller.PCCode, tsRIDateTime.getTime(), controller.dbTotalAmt - (controller.dbTotalAmt * controller.PDiscount));

                if (controller.fetchCreditTerms().get(1).equals("1")) {
                    controller.insertARBalances(controller.PCCode, tsRIDateTime.getTime(), (((controller.dbTotalAmt) * -1)) - (((controller.dbTotalAmt) * -1) * controller.PDiscount), controller.RICCode, (((controller.dbTotalAmt) * -1)) - (((controller.dbTotalAmt) * -1) * controller.PDiscount));
                }

                for (int i = 0; i < controller.RIViewRItems.size(); i++) {
                    PItem = controller.RIViewRItems.get(i).get("Item");
                    PQty = controller.RIViewRItems.get(i).get("Qty");
                    PTotal = controller.RIViewRItems.get(i).get("Total");
                    PRemarks = controller.RIViewRItems.get(i).get("Remarks");

                    controller.PMName = PItem;

                    if (controller.PRType.toUpperCase().equals("RE-Good Returns")) {
                        controller.insertReturnItem(controller.RICCode, controller.fetchMCodeMaterials(), Integer.valueOf(PQty),Integer.valueOf(PQty), Float.valueOf(PTotal.replace(",", "")), StringUtils.substringBefore(controller.PRType, "-"), PRemarks, i + 1);


                        if (controller.fetchCountMaterialInventory() == 0) {
                            controller.PICName = "InvId";
                            controller.PINVNumber = controller.fetchMaxNumInvtTSequence();

                            controller.insertReturnInventory(controller.fetchMCodeMaterials(), RIListSettings.get(6), controller.PINVNumber, PQty);

                            controller.updateInvtTableSequence(Integer.valueOf(controller.PINVNumber) + 1);

                        } else {
                            controller.updateReturnedInventory(Integer.valueOf(PQty));
                        }

                    }
                    else{
                        controller.insertReturnItem(controller.RICCode, controller.fetchMCodeMaterials(), Integer.valueOf(PQty),0, Float.valueOf(PTotal.replace(",", "")), StringUtils.substringBefore(controller.PRType, "-"), PRemarks, i + 1);

                    }

                }

                controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);
                controller.PRItem = 2;
                backup();

                DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                Calendar defaultDate = Calendar.getInstance();
                String logDate;
                logDate = defaultDateFormat2.format(defaultDate.getTime());
                controller.insertCustomerLogsItem(controller.fetchLogID(),2,logDate,0);

                Intent intentReturnedItemActivity = new Intent(ConfirmTransactionActivity.this, ReturnedItemActivity.class);
                startActivity(intentReturnedItemActivity);
                finish();

            } else if (controller.PINum == 4) {

                controller.PCName = "Id";
                controller.PTName = "Sales";
                controller.PMNumber = controller.fetchMaxNumTCTSequence();
                RIListSettings = controller.fetchdbSettings();
                DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
                String strDate = df.format(Calendar.getInstance().getTime());

                controller.SIDCode = strDate + RIListSettings.get(1) + controller.PMNumber;

                final long date = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
                final String dateString = sdf.format(date);

                Timestamp tsCTDateTime = new Timestamp(System.currentTimeMillis());

                controller.insertSales(controller.PCCode, Double.valueOf(ARPAmt.format(controller.PDiscAmt)), Double.valueOf(ARPAmt.format(controller.dbAmtDue)), dateString, etCTName.getText().toString(), bArray,controller.PInvoiceNo,controller.PRemarks, controller.PMNumber, controller.SIDCode);

                controller.insertTransaction("SALES" + controller.SIDCode, "SALES", controller.PCLName, controller.PCCode, tsCTDateTime.getTime(), Double.valueOf(ARPAmt.format(controller.dbAmtDue)));


                for (int i = 0; i < hmSODetails.size(); i++) {

                    PItem = hmSODetails.get(i).get("Item");

                    if (PItem.equals("CHEESE CK PK") || PItem.equals("CHEESE CK BIG CPCK PK")){
                        controller.updateMustCarry("CC");
                    }else if (PItem.equals("WT CHO PK")){
                        controller.updateMustCarry("WCH");
                    }else if (PItem.equals("LAVA CK CHOCO PK")){
                        controller.updateMustCarry("LAC");
                    }else if (PItem.equals("INPT IBT IBNG FLVRS PK")){
                        controller.updateMustCarry("IA");
                    }

                    controller.PMName = PItem;
                    PUPrice = StringUtils.substringBefore(hmSODetails.get(i).get("UPrice"), "/");
                    PQty = hmSODetails.get(i).get("Qty");
                    PTotal = hmSODetails.get(i).get("Total").replace(",", "");
                    controller.insertSalesItem(controller.SIDCode, controller.fetchMCodeMaterials(), PQty, PUPrice, Double.valueOf(PTotal), i + 1);
                    controller.updateSoldQtyInventory(Integer.valueOf(PQty));
                }

                controller.updateMustCarryStatus();

                controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);

                controller.PCName = "Id";
                controller.PTName = "Payment";
                controller.PMNumber = controller.fetchMaxNumTCTSequence();

                STListSettings = controller.fetchdbSettings();

                DateFormat dff = new SimpleDateFormat("yyMMddHHmmss");
                String pdate = dff.format(Calendar.getInstance().getTime());

                PDCode = pdate + STListSettings.get(1) + controller.PMNumber;

                PPRemarks = "";
                for (int i = 0; i < hmPDDetails.size(); i++) {

                    if (PPRemarks.equals("")){
                        PPRemarks = hmPDDetails.get(i).get("Remarks");
                    }else{
                        PPRemarks = PPRemarks + "-" + hmPDDetails.get(i).get("Remarks");
                    }

                }

                do {
                    controller.insertPayment(PPRemarks, Double.valueOf(ARPAmt.format(controller.dbAmtDue)), bArray, tsCTDateTime.getTime(), controller.PMNumber, PDCode);
                    countPayment = Integer.valueOf(controller.fetchcountpayment(PDCode));
                }while(countPayment == 0);



                PPRemarks = "";
                dbPCash = 0.00;
                dbPCheck = 0.00;
                check = 0.00;

                do{
                    for (int i = 0; i < hmPDDetails.size(); i++) {

                        PAmt = hmPDDetails.get(i).get("Amt");
                        PPMode = hmPDDetails.get(i).get("PMode");
                        PRemarks = hmPDDetails.get(i).get("Remarks");
                        PBank = hmPDDetails.get(i).get("Bank");
                        PBranch = hmPDDetails.get(i).get("Branch");
                        PCheckNo = hmPDDetails.get(i).get("CheckNo");
                        PCheckDt = hmPDDetails.get(i).get("CheckDt");

                        if (PPMode.equals("CASH")) {
                            controller.insertPaymentItem(PDCode, controller.SIDCode, "0001", (Double.valueOf(PAmt.replace(",", "")) - controller.dbCGiven), "", tsCTDateTime.getTime(), "", i + 1);
                            dbPCash = (Double.valueOf(PAmt.replace(",", "")) - controller.dbCGiven) + dbPCash;
                        } else if (PPMode.equals("CHECK")) {
                            controller.insertPaymentItem(PDCode, controller.SIDCode, "0002", Double.valueOf(PAmt.replace(",", "")), PCheckNo, tsCTDateTime.getTime(), PBank + PBranch, i + 1);
                            controller.insertChecks(PBank + "-" + PBranch, Long.valueOf(PCheckDt), PCheckNo, Double.valueOf(PAmt.replace(",", "")), PDCode, 0, PCheckNo, Double.valueOf(PAmt.replace(",", "")));
                            comparedate(PCheckDt);
                            if (Integer.valueOf(datediff) > Integer.valueOf(controller.fetchCreditTerms().get(0))){
                                check = check + Double.valueOf(PAmt.replace(",", ""));
                            }
                            dbPCheck = Double.valueOf(PAmt.replace(",", "")) + dbPCheck;
                        } else if (PPMode.equals("TMS")) {
                            controller.insertPaymentItem(PDCode, controller.SIDCode, "0004", Double.valueOf(PAmt.replace(",", "")), "", tsCTDateTime.getTime(), "", i + 1);
                        } else {
                            controller.insertPaymentItem(PDCode, controller.SIDCode, "0003", Double.valueOf(PAmt.replace(",", "")), "", tsCTDateTime.getTime(), "", i + 1);
                            dbPCharge = Double.valueOf(PAmt.replace(",", "")) + dbPCharge;
                        }

                    }
                    countPaymentItem = Integer.valueOf(controller.fetchcountpaymentitem(PDCode));
                }while(countPaymentItem  != hmPDDetails.size());



                if (!dbPCharge.equals(0.00)) {
                    controller.insertARBalances(controller.PCCode, tsCTDateTime.getTime(), Double.valueOf(ARPAmt.format(dbPCharge)), controller.SIDCode, Double.valueOf(ARPAmt.format(dbPCharge)));
                }

                if (controller.dbLReturns > 0.00) {
                    controller.updateFloatARBalance();
                }

                double NPReceived = 0.00;
                if (controller.dbAmtPd > controller.dbAmtDue) {
                    NPReceived = Double.valueOf(ARPAmt.format(controller.dbAmtDue));
                } else {
                    NPReceived = Double.valueOf(ARPAmt.format(controller.dbAmtPd));
                }

                double APExcess = 0.00;

                int isFreebies;
                if (PPMode.equals("TMS")) {
                    isFreebies = 1;
                    APExcess = Double.valueOf(ARPAmt.format(controller.dbAmtDue));
                } else {
                    if (controller.dbBalance < 0.00) {
                        APExcess = Double.valueOf(ARPAmt.format(controller.dbBalance)) * -1;
                    } else {
                        APExcess = 0.00;
                    }
                    isFreebies = 0;
                }

                do {
                    controller.insertPaymentSummary(PDCode, "SALES", controller.fetchRIDNmUsers().get(3), controller.PCCode, Double.valueOf(ARPAmt.format(controller.dbAmtDue + controller.dbLReturns + controller.PDiscAmt)), 0.00, Double.valueOf(ARPAmt.format(controller.PDiscAmt)), Double.valueOf(ARPAmt.format(controller.dbLReturns)), Double.valueOf(ARPAmt.format(controller.dbAmtDue)), Double.valueOf(ARPAmt.format(dbPCash + controller.dbCGiven)), Double.valueOf(ARPAmt.format(dbPCheck)), 0.00, Double.valueOf(ARPAmt.format(dbPCharge)), Double.valueOf(ARPAmt.format(dbPCash - controller.dbCGiven)),
                            Double.valueOf(ARPAmt.format(controller.dbAmtPd)), NPReceived, Double.valueOf(ARPAmt.format(controller.dbCGiven)), APExcess, isFreebies);
                    countPaymentSummary = Integer.valueOf(controller.fetchcountpaymentsummary(PDCode));
                }while (countPaymentSummary == 0);



                controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);

                controller.updateCreditExposure(controller.fetchCreditExpo() + dbPCharge + check);


                backup();

                controller.PIndicator = 2;



                if (!controller.PCLName.equals(controller.fetchdbSettings().get(6) + "-CASH SALES")){
                    if (!controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))){
                        DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                        Calendar defaultDate = Calendar.getInstance();
                        String logDate;
                        logDate = defaultDateFormat2.format(defaultDate.getTime());
                        controller.insertCustomerLogsItem(controller.fetchLogID(),4,logDate,0);
                    }
                }

                Intent intentSalesOrderActivity = new Intent(ConfirmTransactionActivity.this, SalesOrderActivity.class);
                startActivity(intentSalesOrderActivity);
                finish();


            } else if (controller.PINum == 5) {



                for (int i = 0; i < controller.alSTItems.size(); i++) {

                    controller.PMName = controller.alSTItems.get(i);

                    dSTTotalMaterial = Integer.valueOf(controller.alSTQty.get(i)) * controller.fetchAmtPricingList();

                    dSTTotal = dSTTotal + dSTTotalMaterial;

                    int iSTINum = i + 1;

                    controller.insertStockTransferD(controller.PSTiD, controller.fetchMCodeMaterials(), controller.alSTQty.get(i), String.valueOf(iSTINum));

                    controller.updateTransferredQtyInventory(Integer.valueOf(controller.alSTQty.get(i)));


                }



                Timestamp tsSTDateTime = new Timestamp(System.currentTimeMillis());
                controller.insertStockTransferH(controller.PSTiD, tsSTDateTime.getTime(), controller.PMNumber, bArray, controller.PUiD, controller.PSTSLoc, STListSettings.get(6), etCTName.getText().toString(), dSTTotal);

                controller.insertTransaction("ST" + controller.PSTiD, "ST", controller.PSTSLoc, controller.PSTSLoc, tsSTDateTime.getTime(), dSTTotal);

                controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);


                controller.PSTSLocNum = 2;

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


                /*File exportpath = new File(exports);
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

                }*/

                backup();

                Intent intentStockTransferActivity = new Intent(ConfirmTransactionActivity.this, StockTransferActivity.class);
                startActivity(intentStockTransferActivity);
                finish();

            }

          /*  try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }*/

            return null;
        }
    }

    public void ProgressDialogView() {

        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(ConfirmTransactionActivity.this);
        LayoutInflater inflater = ConfirmTransactionActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        dialogBuilder.setView(dialogView);
        progressDialog = dialogBuilder.create();

        TextView title = (TextView) dialogView.findViewById(R.id.tvDPTitle);
        title.setText("Saving info...");

        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

    }

    public class signature extends View {
        private static final float STROKE_WIDTH = 10f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v) {
            if (bitmap == null) {


                bitmap = Bitmap.createBitmap(llCTSignature.getWidth(), llCTSignature.getHeight(), Bitmap.Config.RGB_565);

                Canvas canvas = new Canvas(bitmap);
                v.draw(canvas);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                bArray = bos.toByteArray();

                controller.bArray = bArray;

            }
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            PIsSignature = 1;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:
                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
            Log.v("log_tag", string);
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
    void comparedate(String chkdate){

        long currentdate = System.currentTimeMillis();
        Date checkdate = new Date(Long.valueOf(chkdate));
        SimpleDateFormat Dtfrmat = new SimpleDateFormat(" MM/dd/yyyy", Locale.getDefault());
        Date d1 = new Date(Dtfrmat.format(currentdate));
        Date d2 = new Date(Dtfrmat.format(checkdate));

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        datediff = String.valueOf(calculateDays(d1, d2));

    }
    public static long calculateDays(Date dateEarly, Date dateLater) {
        return (dateLater.getTime() - dateEarly.getTime()) / (24 * 60 * 60 * 1000);
    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(ConfirmTransactionActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm Transaction")
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

    void  messagebox2(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(ConfirmTransactionActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Confirm Transaction")
                .setMessage(alerttext)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        finish();

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

        MediaScannerConnection.scanFile(ConfirmTransactionActivity.this,
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

        if (controller.PINum == 1){
            Intent intentInventoryActivity = new Intent(ConfirmTransactionActivity.this, InventoryActivity.class);
            startActivity(intentInventoryActivity);
            finish();
        }else if (controller.PINum == 2){
            Intent IntentMainActivityActivity = new Intent(ConfirmTransactionActivity.this, MainActivity.class);
            startActivity(IntentMainActivityActivity);
            finish();
        }else if (controller.PINum == 3){
            Intent IntentReturnedItemActivity = new Intent(ConfirmTransactionActivity.this, ReturnedItemActivity.class);
            startActivity(IntentReturnedItemActivity);
            finish();
        }else if (controller.PINum == 4){
            controller.PIndicator = 1;
            Intent IntentSalesOrderActivity = new Intent(ConfirmTransactionActivity.this, SalesOrderActivity.class);
            startActivity(IntentSalesOrderActivity);
            finish();
        }else{
            Intent intentStockTransferActivity = new Intent(ConfirmTransactionActivity.this, StockTransferActivity.class);
            startActivity(intentStockTransferActivity );
            finish();
        }

    }

    void readexports(){

        try {
            FileInputStream fileIn=openFileInput("exports.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            exports = s ;


        } catch (Exception e) {
            exports = "";
        }


    }

}
