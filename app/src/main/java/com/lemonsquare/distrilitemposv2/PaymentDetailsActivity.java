package com.lemonsquare.distrilitemposv2;



import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class PaymentDetailsActivity extends Activity {

    DBController controller = new DBController(this);
    ListView PDDetails,PDHeader;
    ArrayList<HashMap<String, String>> hmPDHeader;
    ListAdapter laPDHeader,laPDDetails;
    HashMap<String, String> mPDHeader,mPDDetail;
    TextView tvPDAmtDueItem,tvPDAmtPdItem,tvPDCChangeItem,tvPDBalanceItem,tvPDLReturnsItem,tvPDDiscItem;
    public static String PAmt,PPMode,PRemarks,PBank,PBranch,PCheckNo,PCheckDt;
    BottomNavigationView btPDNavigation;
    String objPMode,objPPMode;
    List<String> PDListSettings;
    String PPRemarks;
    Double PCCheck,PCCash;
    Double PPAmt = 0.00;
    Double PPPayment = 0.00;
    Double PPBalance = 0.00;
    Double PCash,PRet,PCheck;
    LinearLayout llPDLReturns,llPDDisc;
    int ultracounter;
    boolean locked = true;
    DecimalFormat ARAmt = new DecimalFormat("#,###,##0.00");
    DecimalFormat ARPAmt = new DecimalFormat("######0.00");
    String datediff,datediff2;
    Double check = 0.00;
    int withchange;

    android.support.v7.app.AlertDialog progressDialog;

    private long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paymentdetails);


        PDDetails = (ListView) findViewById(R.id.lvPDDetail);
        PDHeader = (ListView) findViewById(R.id.lvPDHeader);
        tvPDAmtDueItem = (TextView) findViewById(R.id.tvPDAmtDueItem);
        tvPDAmtPdItem = (TextView) findViewById(R.id.tvPDAmtPdItem);
        tvPDCChangeItem = (TextView) findViewById(R.id.tvPDCChangeItem);
        tvPDBalanceItem = (TextView) findViewById(R.id.tvPDBalanceItem);
        btPDNavigation = (BottomNavigationView) findViewById(R.id.btPDNavigation);
        llPDLReturns = (LinearLayout) findViewById(R.id.llPDLReturns);
        tvPDLReturnsItem = (TextView) findViewById(R.id.tvPDLReturnsItem);
        llPDDisc = (LinearLayout) findViewById(R.id.llPDDisc);
        tvPDDiscItem = (TextView) findViewById(R.id.tvPDDiscItem);

        //tvARCName.setText(controller.PCLName);

        controller.PCashExist = 0;
        withchange = 0;

        tvPDAmtDueItem.setText(ARAmt.format((controller.dbAmtDue + controller.dbLReturns + controller.PDiscAmt)));

        tvPDAmtPdItem.setText(ARAmt.format(controller.dbAmtPd));

        ViewHeaderListview();



        if (controller.PIsSOrder == 1){

            if (controller.dbLReturns == 0.00){
                llPDLReturns.setVisibility(View.GONE);

            }else{
                tvPDLReturnsItem.setText(ARAmt.format(controller.dbLReturns));
            }

            if (controller.PDiscAmt == 0.00){
                llPDDisc.setVisibility(View.GONE);
            }else{
                tvPDDiscItem.setText(ARAmt.format(controller.PDiscAmt));
            }

            if (controller.PPayment == 0){
                tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                controller.dbCGiven = 0.00;
                tvPDCChangeItem.setText(ARAmt.format(controller.dbCGiven));
                controller.hmPDDetails = controller.fetchNull();

            }else if (controller.PPayment == 1){
                ViewDetailListview();
                tvPDAmtPdItem.setText(ARAmt.format(controller.dbAmtPd));
                if (controller.dbBalance >= 0.00){
                    tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                }else{
                    controller.dbCGiven = 0.00;
                    controller.dbCGiven = controller.dbBalance*-1;
                    controller.dbBalance = 0.00;
                    tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                    tvPDCChangeItem.setText(ARAmt.format(controller.dbCGiven));
                }
            }else if (controller.PPayment == 2) {

                ViewDetailListview();
                AddDetailListview();

                controller.dbBalance = controller.dbBalance - (Double.parseDouble(PAmt.replace(",", "")));

                controller.dbAmtPd = controller.dbAmtPd + (Double.parseDouble(PAmt.replace(",", "")));

                tvPDAmtPdItem.setText(ARAmt.format(controller.dbAmtPd));

                if (PPMode.equals("CASH")) {
                    if (controller.dbBalance >= 0.00) {
                        tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                    } else {
                        //tvPDBalanceItem.setText("0.00");
                        controller.dbCGiven = (Double.parseDouble(tvPDCChangeItem.getText().toString().replace(",", ""))) + (controller.dbBalance * -1);
                        controller.dbBalance = 0.00;
                        tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                        tvPDCChangeItem.setText(ARAmt.format(controller.dbCGiven));
                    }
                } else {
                    //tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));

                    if (controller.dbBalance <= 0.00){
                        objPPMode = "";
                        for (int i = 0; i < PDDetails.getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laPDDetails.getItem(i);
                            objPMode = (String) obj.get("PMode");

                            if (objPMode.equals("CASH")) {
                                objPPMode = "CASH";
                            }
                        }

                        if (objPPMode.equals("CASH")) {
                            //tvPDBalanceItem.setText("0.00");
                            controller.dbCGiven = (Double.parseDouble(tvPDCChangeItem.getText().toString().replace(",", ""))) + (controller.dbBalance * -1);
                            controller.dbBalance = 0.00;
                            tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                            tvPDCChangeItem.setText(ARAmt.format(controller.dbCGiven));
                        }else{
                            tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                        }
                    }else{
                        tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                    }
                }
            }else{
                tvPDAmtPdItem.setText("TMS TRANSACTION");
                tvPDBalanceItem.setText("0.00");
                AddDetailListview();
            }

        }else{

            llPDLReturns.setVisibility(View.GONE);
            llPDDisc.setVisibility(View.GONE);

            controller.PCName = "Id";
            controller.PTName = "Payment";
            controller.PMNumber = controller.fetchMaxNumTCTSequence();

            PDListSettings = controller.fetchdbSettings();

            DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
            String date = df.format(Calendar.getInstance().getTime());

            controller.PDCode = date + PDListSettings.get(1) + controller.PMNumber;


            if (controller.PPayment == 0){
                if (controller.dbAmtPd > 0){
                    controller.hmPDDetails = controller.fetchNull();
                    PAmt = ARAmt.format(controller.dbAmtPd);
                    PPMode = "RET/OVR";
                    PRemarks = "";
                    PBank = "";
                    PBranch = "";
                    PCheckNo = "";
                    PCheckDt = "";
                    AddDetailListview();
                }else{
                    controller.hmPDDetails = controller.fetchNull();
                }

                controller.dbBalance = controller.fetchSUMARBalances();

                tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));

            }else if (controller.PPayment == 1){
                ViewDetailListview();
                tvPDAmtPdItem.setText(ARAmt.format(controller.dbAmtPd));
                if (controller.dbBalance >= 0.00){
                    tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                }else{
                    //tvPDBalanceItem.setText("0.00");
                    controller.dbCGiven = 0.00;
                    controller.dbCGiven = controller.dbBalance*-1;
                    controller.dbBalance = 0.00;
                    tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                    tvPDCChangeItem.setText(ARAmt.format(controller.dbCGiven));
                }
            }else {
                ViewDetailListview();
                AddDetailListview();

                controller.dbBalance = controller.dbBalance - (Double.parseDouble(PAmt.replace(",", "")));

                controller.dbAmtPd = controller.dbAmtPd + (Double.parseDouble(PAmt.replace(",", "")));

                tvPDAmtPdItem.setText(ARAmt.format(controller.dbAmtPd));

                if (PPMode.equals("CASH")) {
                    if (controller.dbBalance >= 0.00) {
                        tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                    } else {
                        //tvPDBalanceItem.setText("0.00");
                        controller.dbCGiven = (Double.parseDouble(tvPDCChangeItem.getText().toString().replace(",", ""))) + (controller.dbBalance * -1);
                        controller.dbBalance = 0.00;
                        tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                        tvPDCChangeItem.setText(ARAmt.format(controller.dbCGiven));
                    }
                } else {
                    //tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));

                    if (controller.dbBalance <= 0.00){
                        objPPMode = "";
                        for (int i = 0; i < PDDetails.getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laPDDetails.getItem(i);
                            objPMode = (String) obj.get("PMode");

                            if (objPMode.equals("CASH")) {
                                objPPMode = "CASH";
                            }
                        }

                        if (objPPMode.equals("CASH")) {
                            //tvPDBalanceItem.setText("0.00");
                            controller.dbCGiven = (Double.parseDouble(tvPDCChangeItem.getText().toString().replace(",", ""))) + (controller.dbBalance * -1);
                            controller.dbBalance = 0.00;
                            tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                            tvPDCChangeItem.setText(ARAmt.format(controller.dbCGiven));
                        }else{
                            tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                        }
                    }else{
                        tvPDBalanceItem.setText(ARAmt.format(controller.dbBalance));
                    }
                }
            }
        }

        btPDNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mpd_addmore:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if (controller.dbBalance <= 0.00){
                                    messagebox("nothing to pay");
                                }else{

                                    for (int i = 0; i < PDDetails.getCount(); i++) {
                                        HashMap<String, Object> obj = (HashMap<String, Object>) laPDDetails.getItem(i);
                                        objPMode = (String) obj.get("PMode");

                                        if (objPMode.equals("CASH")) {
                                            controller.PCashExist = 1;
                                        }else if (objPMode.equals("CHARGED SALES")){
                                            controller.PCashExist = 2;
                                        }else {
                                            controller.PCashExist = 0;
                                        }
                                    }

                                    Intent IntentPaymentActivity = new Intent(PaymentDetailsActivity.this, PaymentActivity.class);
                                    startActivity(IntentPaymentActivity);
                                    finish();
                                }

                                break;

                            case R.id.mpd_submit:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if (PDDetails.getCount() == 0){
                                    messagebox("no payments found");
                                    //Toast.makeText(getApplicationContext(), "no payments found", Toast.LENGTH_LONG).show();
                                } else {

                                    if (controller.PIsSOrder == 1){

                                        if (Double.valueOf(tvPDBalanceItem.getText().toString().replace(",","")) > 0.00){
                                            messagebox("Amount is not enough");
                                        }else{

                                            if (locked) {
                                                locked = false;

                                                if (controller.dbCGiven > 0.00) {
                                                    messageboxwithintent("Change is: " + tvPDCChangeItem.getText().toString());
                                                } else {
                                                    gotonextactivity();
                                                }

                                            }


                                        }

                                    }else {
                                        HashMap<String, Object> PDobj = (HashMap<String, Object>) laPDDetails.getItem(0);
                                        objPMode = (String) PDobj.get("PMode");
                                        if (objPMode.equals("RET/OVR") && PDDetails.getCount() == 1) {
                                            messagebox("please add another payment mode");
                                            //Toast.makeText(getApplicationContext(), "please add another payment mode", Toast.LENGTH_LONG).show();
                                        } else {

                                            if (locked) {
                                                locked = false;

                                                new MyTask().execute();
                                                Toasty.success(getApplicationContext(), "database successfully backup", Toast.LENGTH_LONG).show();
                                                if (!tvPDCChangeItem.getText().toString().equals("0.00")) {
                                                    messageboxwithintent("Change is: " + tvPDCChangeItem.getText().toString());
                                                } else {
                                                    gotonextactivity();
                                                }

                                        }

                                    }
                                    }
                                }
                                break;

                        }
                        return true;
                    }
                });

        if (controller.PIsSOrder == 0){
            if (controller.PIndicator == 0){
                if (controller.hmPDDetails.size() == 0){
                    Intent IntentPaymentActivity = new Intent(PaymentDetailsActivity.this, PaymentActivity.class);
                    startActivity(IntentPaymentActivity);
                    finish();
                }
            }

        }else{

            if (controller.PIndicator == 1){
                if (controller.hmPDDetails.size() == 0){
                    Intent IntentPaymentActivity = new Intent(PaymentDetailsActivity.this, PaymentActivity.class);
                    startActivity(IntentPaymentActivity);
                    finish();
                }
            }
        }



    }

    public void ViewHeaderListview() {

        hmPDHeader = new ArrayList<HashMap<String, String>>();
        //controller.hmPDDetails = new ArrayList<HashMap<String, String>>();
        mPDHeader = new HashMap<String, String>();

        mPDHeader.put("Amt", "AMOUNT");
        mPDHeader.put("PMode", "PAYMENT MODE");
        hmPDHeader.add(mPDHeader);

        try {
            laPDHeader = new SimpleAdapter(this, hmPDHeader, R.layout.item_paymentdetails,
                    new String[]{"Amt", "PMode"}, new int[]{
                    R.id.rowsAmt, R.id.rowsPMode}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);
                    TextView rpment = (TextView) view.findViewById(R.id.rowsPMode);
                    if (position % 2 == 0) {
                        ramt.setTextColor(Color.WHITE);
                        rpment.setTextColor(Color.WHITE);
                        ramt.setTypeface(null, Typeface.BOLD);
                        rpment.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            PDHeader.setAdapter(laPDHeader);
        } catch (Exception e) {

        }
    }
    public void AddDetailListview(){

        mPDDetail = new HashMap<String, String>();

        mPDDetail.put("Amt", PAmt);
        mPDDetail.put("PMode", PPMode);
        mPDDetail.put("Remarks", PRemarks);
        mPDDetail.put("Bank", PBank);
        mPDDetail.put("Branch", PBranch);
        mPDDetail.put("CheckNo", PCheckNo);
        mPDDetail.put("CheckDt", PCheckDt);
        controller.hmPDDetails.add(mPDDetail);

        try {
            laPDDetails = new SimpleAdapter(this, controller.hmPDDetails, R.layout.item_paymentdetails,
                    new String[]{"Amt", "PMode", "Remarks", "Bank", "Branch","CheckNo","CheckDt"}, new int[]{
                    R.id.rowsAmt, R.id.rowsPMode,R.id.rowsRemarks,R.id.rowsBank,R.id.rowsBranch,R.id.rowsCheckNo,R.id.rowsCheckDt}) {



                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rremarks = (TextView) view.findViewById(R.id.rowsRemarks);
                    TextView rbank = (TextView) view.findViewById(R.id.rowsBank);
                    TextView rbranch = (TextView) view.findViewById(R.id.rowsBranch);
                    TextView rcheckno = (TextView) view.findViewById(R.id.rowsCheckNo);
                    TextView rcheckdt = (TextView) view.findViewById(R.id.rowsCheckDt);
                    rremarks.setVisibility(View.GONE);
                    rbank.setVisibility(View.GONE);
                    rbranch.setVisibility(View.GONE);
                    rcheckno.setVisibility(View.GONE);
                    rcheckdt.setVisibility(View.GONE);

                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;

                }
            };
            PDDetails.setAdapter(laPDDetails);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laPDDetails = new SimpleAdapter(this, controller.hmPDDetails, R.layout.item_paymentdetails,
                    new String[]{"Amt", "PMode", "Remarks", "Bank", "Branch","CheckNo","CheckDt"}, new int[]{
                    R.id.rowsAmt, R.id.rowsPMode,R.id.rowsRemarks,R.id.rowsBank,R.id.rowsBranch,R.id.rowsCheckNo,R.id.rowsCheckDt}) {

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rremarks = (TextView) view.findViewById(R.id.rowsRemarks);
                    TextView rbank = (TextView) view.findViewById(R.id.rowsBank);
                    TextView rbranch = (TextView) view.findViewById(R.id.rowsBranch);
                    TextView rcheckno = (TextView) view.findViewById(R.id.rowsCheckNo);
                    TextView rcheckdt = (TextView) view.findViewById(R.id.rowsCheckDt);
                    rremarks.setVisibility(View.GONE);
                    rbank.setVisibility(View.GONE);
                    rbranch.setVisibility(View.GONE);
                    rcheckno.setVisibility(View.GONE);
                    rcheckdt.setVisibility(View.GONE);

                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;

                }
            };
            PDDetails.setAdapter(laPDDetails);
        } catch (Exception e) {

        }
    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(PaymentDetailsActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Payment")
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

    void  messageboxwithintent(String alerttext) {

        new AlertDialog.Builder(PaymentDetailsActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Payment")
                .setMessage(alerttext)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        gotonextactivity();

                    }

                })
                .show();
    }



    public void gotonextactivity(){

        if (controller.PIsSOrder == 1){

            controller.PINum = 4;

            Intent IntentConfirmTransactionActivity = new Intent(PaymentDetailsActivity.this, ConfirmTransactionActivity.class);
            startActivity(IntentConfirmTransactionActivity);
            finish();

        }else{

            controller.PIndicator = 1;

            Intent IntentARPaymentActivity = new Intent(PaymentDetailsActivity.this, ARPaymentActivity.class);
            startActivity(IntentARPaymentActivity);
            finish();

        }

    }

    private void scanMedia(File path) {
        Uri uri = Uri.fromFile(path);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(PaymentDetailsActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    void comparedate(String chkdate){

        //long currentdate = System.currentTimeMillis();
        Date checkdate = new Date(Long.valueOf(chkdate));
        SimpleDateFormat Dtfrmat = new SimpleDateFormat(" MM/dd/yyyy", Locale.getDefault());
        Date d1 = new Date(controller.fetchLateAR());
        Date d2 = new Date(Dtfrmat.format(checkdate));

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        datediff = String.valueOf(calculateDays(d1, d2));

    }

    void comparedate(){

        String datefrom = controller.fetchLateAR();

        long ldate = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

        Date d1 = new Date(datefrom);
        Date d2 = new Date(sdf.format(ldate));

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(d1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(d2);

        datediff2 = String.valueOf(calculateDays(d1, d2));

    }


    public static long calculateDays(Date dateEarly, Date dateLater) {
        return (dateLater.getTime() - dateEarly.getTime()) / (24 * 60 * 60 * 1000);
    }
    private class MyTask extends AsyncTask<String,String,String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(PaymentDetailsActivity.this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setIcon(R.drawable.ic_save_progress); // you can set your own icon here
            pd.setTitle("Please Wait...");
            pd.setMessage("Saving data");
            pd.setIndeterminate(false);
            pd.setCancelable(false); // this will disable the back button
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {

            savedata();
            return "";

        }

        @Override
        protected void onPostExecute(String result) {


            pd.dismiss();

        }
    }

    public void savedata(){


        PCCheck = (Double.parseDouble(tvPDAmtPdItem.getText().toString().replace(",", "")));

        Timestamp tmPDDtTime = new Timestamp(System.currentTimeMillis());

        PPRemarks = "";

        for (int i = 0; i < PDDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laPDDetails.getItem(i);
            String objRemarks = (String) obj.get("Remarks");
            if (PPRemarks.equals("")) {
                PPRemarks = objRemarks;
            } else {
                PPRemarks = PPRemarks + "-" + objRemarks;
            }

        }

        byte[] emptyArray = new byte[0];

        controller.insertPayment(PPRemarks, Double.valueOf(ARPAmt.format(PCCheck)), emptyArray, tmPDDtTime.getTime(), controller.PMNumber, controller.PDCode);



        //controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);


        //int count = PDDetails.getCount() - 1;
        int counter = 1;
        PCash = 0.00;
        PRet = 0.00;
        PCheck = 0.00;
        check = 0.00;

        for (int i = 0; i < PDDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laPDDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            String objPMode = (String) obj.get("PMode");
            String objBank = (String) obj.get("Bank");
            String objBranch = (String) obj.get("Branch");
            String objCheckNo = (String) obj.get("CheckNo");
            String objCheckDt = (String) obj.get("CheckDt");

            if (objPMode.equals("RET/OVR")) {

                PCCash = Double.valueOf(objAmt.replace(",", ""));
                PRet = Double.valueOf(objAmt.replace(",", ""));

                do {

                    try {
                        PCCash = Double.valueOf(ARPAmt.format(PCCash));
                        PPAmt = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(0)));
                        PPPayment = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(1)));
                        PPBalance = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(2)));

                        if (PCCash > PPBalance) {
                            PPPayment = PPPayment + PPBalance;
                            PCCash = PCCash - PPBalance;
                            controller.insertPaymentItem(controller.PDCode, controller.fetchARBalanceSID(), "0005", Double.valueOf(ARPAmt.format(PPBalance)), "", tmPDDtTime.getTime(), "", counter);
                            PPBalance = 0.00;

                        } else {
                            PPPayment = PPPayment + PCCash;
                            PPBalance = PPAmt - PPPayment;
                            controller.insertPaymentItem(controller.PDCode, controller.fetchARBalanceSID(), "0005", Double.valueOf(ARPAmt.format(PCCash)), "", tmPDDtTime.getTime(), "", counter);
                            PCCash = 0.00;
                        }

                        controller.updateARBalance(Double.valueOf(ARPAmt.format(PPPayment)), Double.valueOf(ARPAmt.format(PPBalance)), controller.fetchARBalanceSID());
                        counter = counter + 1;

                    } catch (Exception e) {
                        Toasty.error(PaymentDetailsActivity.this, String.valueOf(PCCash), Toast.LENGTH_LONG).show();
                        PCCash = 0.00;
                    }
                } while (PCCash > 0.00);
                controller.updateFloatARBalance();

                                                    /*for (int a = 0; a <= controller.fetchARBalanceSIDFloat().size() - 1; a++) {

                                                        String SID = controller.fetchARBalanceSIDFloat().get(a);

                                                        PPAmt = Double.valueOf(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSIDFloat().get(a)).get(0));

                                                        controller.updateARBalance(PPAmt,0.00, SID);
                                                    }*/


            } else if (objPMode.equals("CASH")) {

                //controller.insertPaymentItem(PDCode,SPDCode,"0001",Double.valueOf(objAmt.replace(",","")),"",tmPDDtTime.getTime(),"",i+1);

                PCCash = Double.valueOf(objAmt.replace(",", "")) - controller.dbCGiven;


                PCash = PCCash;

                do {

                    try {
                        PCCash = Double.valueOf(ARPAmt.format(PCCash));
                        PPAmt = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(0)));
                        PPPayment = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(1)));
                        PPBalance = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(2)));

                        if (PCCash > PPBalance) {
                            PPPayment = PPPayment + PPBalance;
                            PCCash = PCCash - PPBalance;
                            controller.insertPaymentItem(controller.PDCode, controller.fetchARBalanceSID(), "0001", Double.valueOf(ARPAmt.format(PPBalance)), "", tmPDDtTime.getTime(), "", counter);
                            PPBalance = 0.00;

                        } else {

                            PPPayment = PPPayment + PCCash;
                            PPBalance = PPAmt - PPPayment;
                            controller.insertPaymentItem(controller.PDCode, controller.fetchARBalanceSID(), "0001", Double.valueOf(ARPAmt.format(PCCash)), "", tmPDDtTime.getTime(), "", counter);
                            PCCash = 0.00;
                        }

                        counter = counter + 1;
                        controller.updateARBalance(Double.valueOf(ARPAmt.format(PPPayment)), Double.valueOf(ARPAmt.format(PPBalance)), controller.fetchARBalanceSID());
                    } catch (Exception e) {
                        Toasty.error(PaymentDetailsActivity.this, String.valueOf(PCCash), Toast.LENGTH_LONG).show();
                        PCCash = 0.00;
                    }
                } while (PCCash > 0.00);


            } else if (objPMode.equals("CHECK")) {

                //controller.insertPaymentItem(PDCode,SPDCode,"0002",Double.valueOf(objAmt.replace(",","")),objCheckNo,Long.valueOf(objCheckDt),objBank + "-" + objBranch,i+1);

                PCCash = Double.valueOf(objAmt.replace(",", ""));
                PCheck = PCheck + PCCash;


                do {

                    try {
                        PCCash = Double.valueOf(ARPAmt.format(PCCash));
                        PPAmt = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(0)));
                        PPPayment = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(1)));
                        PPBalance = Double.valueOf(ARPAmt.format(controller.fetchARBalanceAmtPmentBal(controller.fetchARBalanceSID()).get(2)));

                        //messagebox(String.valueOf(PPBalance));

                        if (PCCash > PPBalance) {
                            PPPayment = PPPayment + PPBalance;
                            PCCash = PCCash - PPBalance;
                            controller.insertPaymentItem(controller.PDCode, controller.fetchARBalanceSID(), "0002", Double.valueOf(ARPAmt.format(PPBalance)), objCheckNo, Long.valueOf(objCheckDt), objBank + "-" + objBranch, counter);
                            PPBalance = 0.00;

                        } else {

                            PPPayment = PPPayment + PCCash;
                            PPBalance = PPAmt - PPPayment;
                            controller.insertPaymentItem(controller.PDCode, controller.fetchARBalanceSID(), "0002", Double.valueOf(ARPAmt.format(PCCash)), objCheckNo, Long.valueOf(objCheckDt), objBank + "-" + objBranch, counter);
                            PCCash = 0.00;
                        }
                        //messagebox("no way");

                        ultracounter = counter;

                        counter = counter + 1;
                        controller.updateARBalance(Double.valueOf(ARPAmt.format(PPPayment)), Double.valueOf(ARPAmt.format(PPBalance)), controller.fetchARBalanceSID());
                    } catch (Exception e) {
                        controller.updateARPayment(controller.PDCode,ultracounter,PCCash);
                        //controller.insertPaymentItem(controller.PDCode, controller.fetchARBalanceSID(), "0002", Double.valueOf(ARPAmt.format(PCCash)), objCheckNo, Long.valueOf(objCheckDt), objBank + "-" + objBranch, counter);
                        //messagebox("catch");
                        PCCash = 0.00;
                    }

                } while (PCCash > 0.00);

                controller.insertChecks(objBank + "-" + objBranch, Long.valueOf(objCheckDt), objCheckNo, Double.valueOf(objAmt.replace(",", "")), controller.PDCode, 0, objCheckNo, Double.valueOf(objAmt.replace(",", "")));
                comparedate();
                if (Integer.valueOf(controller.fetchCreditTerms().get(0)) >= Integer.valueOf(datediff2)){
                    comparedate(objCheckDt);
                    if (Integer.valueOf(controller.fetchCreditTerms().get(0)) >= Integer.valueOf(datediff)){
                        check = check + Double.valueOf(objAmt.replace(",", ""));
                    }
                }
            }
        }

        double APExcess = 0.00;
        if (Double.valueOf(tvPDBalanceItem.getText().toString().replace(",", "")) < 0.00) {
            APExcess = Double.valueOf(tvPDBalanceItem.getText().toString().replace(",", "")) * -1;
        } else {
            APExcess = 0.00;
        }

        double NPReceived = 0.00;
        NPReceived = PCCheck - controller.dbCGiven - APExcess - PRet;

        controller.insertTransaction("AR" + controller.PDCode, "AR", controller.PCLName, controller.PCCode, tmPDDtTime.getTime(), Double.valueOf(ARPAmt.format(NPReceived)));

        controller.insertPaymentSummary(controller.PDCode, "AR", controller.fetchRIDNmUsers().get(3), controller.PCCode, Double.valueOf(tvPDAmtDueItem.getText().toString().replace(",", "")), 0.00, 0.00, 0.00, Double.valueOf(tvPDAmtDueItem.getText().toString().replace(",", "")) - Double.valueOf(ARPAmt.format(PRet)), Double.valueOf(ARPAmt.format(PCash + controller.dbCGiven)), Double.valueOf(ARPAmt.format(PCheck)), Double.valueOf(ARPAmt.format(PRet)), 0.00, Double.valueOf(ARPAmt.format(PCash)) - Double.valueOf(ARPAmt.format(controller.dbCGiven)),
                Double.valueOf(tvPDAmtPdItem.getText().toString().replace(",", "")), Double.valueOf(ARPAmt.format(NPReceived)), Double.valueOf(tvPDCChangeItem.getText().toString().replace(",", "")), APExcess, 0);

        controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);

       /* if (!tvPDCChangeItem.getText().toString().equals("0.00")) {
            withchange = 1;
        } else {
            withchange = 0;
        }*/
        controller.updateCreditExposure(controller.fetchCreditExpo() - PCash - check);



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
        //Toasty.success(getApplicationContext(), "database backup successfully", Toast.LENGTH_LONG).show();

    }

    public void ProgressDialogView() {

        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(PaymentDetailsActivity.this);
        LayoutInflater inflater = PaymentDetailsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        dialogBuilder.setView(dialogView);
        progressDialog = dialogBuilder.create();

        TextView title = (TextView) dialogView.findViewById(R.id.tvDPTitle);
        title.setText("Saving info...");

        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

    }



    public void onBackPressed() {

        if (controller.PIsSOrder == 1){

            controller.PIndicator = 1;

            Intent IntentSalesOrderActivity = new Intent(PaymentDetailsActivity.this, SalesOrderActivity.class);
            startActivity(IntentSalesOrderActivity);
            finish();

        }else{

            controller.PIndicator = 0;

            Intent IntentARPaymentActivity = new Intent(PaymentDetailsActivity.this, ARPaymentActivity.class);
            startActivity(IntentARPaymentActivity);
            finish();

        }
    }

}



