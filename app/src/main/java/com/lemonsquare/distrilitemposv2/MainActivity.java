package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import es.dmoral.toasty.Toasty;


public class MainActivity extends Activity {

    DBController controller = new DBController(this);
    ListView MDetails;
    TextView tvMUser,tvMVersion,tvDCTCustName,tvDCTCancel,tvDCTOk;;
    String[] sMTAdministrator = new String[]{"SETTINGS","DATA","LOG OUT"};
    Integer[] sMIAdministrator = new Integer[] {R.drawable.img_settings,R.drawable.img_database,R.drawable.img_logout};
    String[] sMTAgent = new String[]{"MY PERFORMANCE","ROUTE","INVENTORY","CUSTOMERS","MISC","DEPOSITS","ORDERING","ATTENDANCE","LOG OUT"};
    Integer[] sMIAgent = new Integer[] {R.drawable.img_performance,R.drawable.img_route,R.drawable.img_inventory,R.drawable.img_customer,R.drawable.img_miscellaneous,R.drawable.img_deposits,R.drawable.img_ordering,R.drawable.img_clock,R.drawable.img_logout};
    String[] sMTChecker = new String[]{"INVENTORY","LOG OUT"};
    Integer[] sMIChecker = new Integer[] {R.drawable.img_checker_inventory,R.drawable.img_logout};
    String[] sMTCashier = new String[]{"RECON","LOG OUT"};
    Integer[] sMICashier = new Integer[] {R.drawable.img_recon,R.drawable.img_logout};
    RadioButton rbDCTARPMent,rbDCTReturns,rbDCTNSales;
    String datediff = "";

    private long mLastClickTime = 0;

    final static int RQS_1 = 1;

    String promoInfo;
    static final int READ_BLOCK_SIZE = 100;
    ArrayList<String> listPromo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MDetails=(ListView)findViewById(R.id.lvMDetails);
        tvMUser = (TextView) findViewById(R.id.tvMUser);
        tvMVersion = (TextView) findViewById(R.id.tvMVersion);

        /*notification();*/


        final Context context = getApplicationContext();
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String MVersion = "not available";

        try {
            MVersion = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tvMVersion.setText("VERSION " + MVersion);

        //startService(new Intent(this, BackgroundLocationUpdateService.class));

        tvMUser.setText(controller.fetchSDstSettings() + "-" + controller.fetchRIDNmUsers().get(1));
        controller.PUName = controller.fetchRIDNmUsers().get(2);

        if(controller.fetchRIDNmUsers().get(0).equals("ADM")){
            CustomMainListActivity adapter = new
                    CustomMainListActivity(MainActivity.this, sMTAdministrator, sMIAdministrator);
            MDetails.setAdapter(adapter);
        }else if (controller.fetchRIDNmUsers().get(0).equals("AGE")){

            readpromoinfo();
            if (!promoInfo.equals("")){
                File productinfopath = new File(promoInfo);
                File[] strFileName = productinfopath.listFiles();

                try {
                    listPromo = new ArrayList<String>();
                    listPromo.clear();
                    for (int i = 0; i < strFileName.length;i++){
                        if (strFileName[i].getName().endsWith(".png") || strFileName[i].getName().endsWith(".jpg")){
                            listPromo.add(strFileName[i].getName());
                        }
                    }
                    Random randomNo = new Random();
                    int num = randomNo.nextInt(listPromo.size());

                    ViewPromoInfo(promoInfo + "/" + listPromo.get(num));

                }catch (Exception e){
                    Toasty.error(this, "no defined file path", Toast.LENGTH_LONG).show();
                }
            }


            CustomMainListActivity adapter = new
                    CustomMainListActivity(MainActivity.this, sMTAgent, sMIAgent);
            MDetails.setAdapter(adapter);

            controller.PUiD = controller.fetchRIDNmUsers().get(3);

        }else if (controller.fetchRIDNmUsers().get(0).equals("CHE")){
            CustomMainListActivity adapter = new
                    CustomMainListActivity(MainActivity.this, sMTChecker, sMIChecker);
            MDetails.setAdapter(adapter);
            controller.PUiD = controller.fetchRIDNmUsers().get(3);
        }else{
            CustomMainListActivity adapter = new
                    CustomMainListActivity(MainActivity.this, sMTCashier, sMICashier);
            MDetails.setAdapter(adapter);
            controller.PUiD = controller.fetchRIDNmUsers().get(3);
        }

        MDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    final int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();



                        controller.PCNm = 0;

                        if (controller.fetchRIDNmUsers().get(0).equals("ADM")){
                            if (position == 0){

                                Intent intentSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
                                startActivity(intentSettingsActivity);
                                finish();

                            }else if (position == 1){

                                Intent intentDatabaseSettingsActivity = new Intent(MainActivity.this, DatabaseSettingsActivity.class);
                                startActivity(intentDatabaseSettingsActivity);
                                finish();

                            }else{
                                new AlertDialog.Builder(MainActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Log out")
                                        .setMessage("Are you sure you want to log out?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                controller.updateUsers(0);

                                                Intent intentScreenActivity = new Intent(MainActivity.this, ScreenActivity.class);
                                                startActivity(intentScreenActivity);
                                                finish();

                                            }

                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                mLastClickTime = 0;
                                            }

                                        })
                                        .show();
                            }
                        }else if (controller.fetchRIDNmUsers().get(0).equals("AGE")){

                            if (position == 0){
                                Intent intentPerformanceActivity = new Intent(MainActivity.this, PerformanceActivity.class);
                                startActivity(intentPerformanceActivity);
                                finish();

                               /*messagebox(Utils.randimizer());*/


                            } else if (position == 1){
                                controller.PLVposition = 0;
                                readtransact();
                                Intent intentRouteScheduleActivity = new Intent(MainActivity.this, RouteSchedule2Activity.class);
                                startActivity(intentRouteScheduleActivity);
                                finish();
                            }else if (position == 2){
                                Intent intentInventoryListActivity = new Intent(MainActivity.this, InventoryListActivity.class);
                                startActivity(intentInventoryListActivity);
                                finish();
                            }else if (position == 3){
                                controller.PLVposition = 0;
                                readtransact();
                                Intent intentCustomerListActivity = new Intent(MainActivity.this, CustomerList2Activity.class);
                                startActivity(intentCustomerListActivity);
                                finish();
                            }else if (position == 4){
                                Intent intentMiscellaneousActivity = new Intent(MainActivity.this, MiscellaneousActivity.class);
                                startActivity(intentMiscellaneousActivity);
                                finish();
                                //}
                                //else if (position == 5){
                                //    new IntentIntegrator(MainActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
                            }else if (position == 5){
                                Intent intentCashDepositsActivity = new Intent(MainActivity.this, CashDepositsActivity.class);
                                startActivity(intentCashDepositsActivity);
                                finish();
                            }else if (position == 6){
                                if (controller.fetchISSDR() == 2){
                                    Toasty.info(MainActivity.this,"please time in first", Toast.LENGTH_LONG).show();
                                }else if (controller.fetchTimeInOutComplete() == 1){
                                    Toasty.info(MainActivity.this,"you have already completed your transaction for today", Toast.LENGTH_LONG).show();
                                }else{
                                    Intent intentOrderingActivity = new Intent(MainActivity.this, OrderingActivity.class);
                                    startActivity(intentOrderingActivity);
                                }
                            }else if (position == 7){
                                Intent intentDailyAttendanceActivity = new Intent(MainActivity.this, DailyAttendanceActivity.class);
                                startActivity(intentDailyAttendanceActivity);
                                finish();
                            }else{
                                new AlertDialog.Builder(MainActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Log out")
                                        .setMessage("Are you sure you want to log out?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                controller.updateUsers(0);

                                                Intent intentScreenActivity = new Intent(MainActivity.this, ScreenActivity.class);
                                                startActivity(intentScreenActivity);
                                                finish();
                                            }

                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                mLastClickTime = 0;
                                            }

                                        })
                                        .show();
                            }
                        }else if (controller.fetchRIDNmUsers().get(0).equals("CHE")){
                            if (position == 0){

                                controller.PVRNum = 0;
                                controller.PINum = 0;

                                Intent intentValidateReturnsActivity = new Intent(MainActivity.this, ValidateReturnsActivity.class);
                                startActivity(intentValidateReturnsActivity);
                                finish();

                            }else {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Log out")
                                        .setMessage("Are you sure you want to log out?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                controller.updateUsers(0);

                                                Intent intentScreenActivity = new Intent(MainActivity.this, ScreenActivity.class);
                                                startActivity(intentScreenActivity);
                                                finish();
                                            }

                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                mLastClickTime = 0;
                                            }

                                        })
                                        .show();
                            }
                        } else{
                            if (position == 0){

                                Intent intentCashandDepositCashierActivity = new Intent(MainActivity.this, CashandDepositCashierActivity.class);
                                startActivity(intentCashandDepositCashierActivity);
                                finish();

                            }else {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Log out")
                                        .setMessage("Are you sure you want to log out?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                controller.updateUsers(0);

                                                Intent intentScreenActivity = new Intent(MainActivity.this, ScreenActivity.class);
                                                startActivity(intentScreenActivity);
                                                finish();
                                            }

                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                mLastClickTime = 0;
                                            }

                                        })
                                        .show();
                            }
                        }

            }
        });


    }

    void readpromoinfo(){

        try {
            FileInputStream fileIn=openFileInput("promoinfo.txt");
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
            promoInfo = s ;


        } catch (Exception e) {
            promoInfo = "";
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //We will get scan results here
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //check for null
        if (result != null) {
            if (result.getContents() == null) {
                Toasty.error(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //show dialogue with result
                showResultDialogue(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showResultDialogue(final String result) {

        if (controller.fetchCountCustomer(result) == 1){

            controller.PCCode = result;
            controller.PCLName = controller.fetchCustomer().get(0).replace("'","");
            controller.PTerms = controller.fetchCustomer().get(1);
            controller.PLimit = Double.valueOf(controller.fetchCustomer().get(2));

            if (controller.fetchLastOdometerCustomer().equals(result)){
                DialogCustomerSelecttransaction();
            }else{
                Intent intentCustomerCheckInActivity = new Intent(MainActivity.this, CustomerCheckInActivity.class);
                startActivity(intentCustomerCheckInActivity);
                finish();
            }


        }else{
            messagebox("customer not found");
        }



    }

    public void DialogCustomerSelecttransaction() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customerselecttransaction, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();


        tvDCTCustName = (TextView) dialogView.findViewById(R.id.tvDCTCustName);
        rbDCTARPMent = (RadioButton) dialogView.findViewById(R.id.rbDCTARPMent);
        rbDCTReturns = (RadioButton) dialogView.findViewById(R.id.rbDCTReturns);
        rbDCTNSales = (RadioButton) dialogView.findViewById(R.id.rbDCTNSales);
        tvDCTCancel = (TextView) dialogView.findViewById(R.id.tvDCTCancel);
        tvDCTOk = ( TextView) dialogView.findViewById(R.id.tvDCTOk);

        tvDCTCustName.setText(controller.PCLName);

        rbDCTARPMent.setChecked(true);

        tvDCTCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alertDialog.dismiss();
                controller.Prscl = 0;
            }
        });

        tvDCTOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (rbDCTARPMent.isChecked() == false && rbDCTReturns.isChecked() == false && rbDCTNSales.isChecked() == false){
                    //messagebox("please select transaction");
                }else{
                    if (rbDCTARPMent.isChecked() == true){
                        controller.PIndicator = 0;
                        //controller.PCCode = controller.fetchCCodeCustomers();
                        controller.Prscl = 3;
                        DecimalFormat ARAmt = new DecimalFormat("#,###,##0.00");
                        if (!ARAmt.format(controller.fetchSUMARBalances()).equals("0.00")){


                            Intent intentARBalanceActivity = new Intent(MainActivity.this, ARPaymentActivity.class);
                            startActivity(intentARBalanceActivity);
                            finish();
                        }else{
                            messagebox(controller.PCLName + " has no pending AR");
                        }
                    }else if (rbDCTReturns.isChecked() == true){
                        controller.PRItem = 0;
                        controller.Prscl = 3;
                        Intent IntentReturnedItemActivity = new Intent(MainActivity.this, ReturnedItemActivity.class);
                        startActivity(IntentReturnedItemActivity);
                        finish();
                    }else{
                        controller.Prscl = 3;

                        controller.PIsSOrder = 1;

                        controller.PPayment = 0;

                        controller.PIndicator = 0;

                        controller.dbGAmt = 0.00;
                        controller.dbNSales = 0.00;
                        controller.dbCGiven = 0.00;


                        if (controller.PTerms.equals("COD")){



                            controller.dbLReturns = Double.valueOf(controller.fetchBalanceARBal(controller.PCCode));



                            if (controller.dbLReturns != 0.00){
                                controller.dbLReturns = controller.dbLReturns * -1;
                            }

                            Intent IntentSalesOrderActivity = new Intent(MainActivity.this, SalesOrderActivity.class);
                            startActivity(IntentSalesOrderActivity);
                            finish();

                        }else{

                            if (controller.fetchLateAR().equals("")){
                                Intent IntentSalesOrderActivity = new Intent(MainActivity.this, SalesOrderActivity.class);
                                startActivity(IntentSalesOrderActivity);
                                finish();
                            }else{
                                String datefrom = controller.fetchLateAR();

                                long ldate = System.currentTimeMillis();
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

                                Date d1 = new Date(datefrom);
                                Date d2 = new Date(sdf.format(ldate));

                                Calendar cal1 = Calendar.getInstance();
                                cal1.setTime(d1);
                                Calendar cal2 = Calendar.getInstance();
                                cal2.setTime(d2);

                                datediff = String.valueOf(calculateDays(d1, d2));

                                controller.dbLReturns = 0.00;

                                if (Integer.valueOf(controller.fetchCreditTerms().get(0)) < Integer.valueOf(datediff)){
                                    messagebox("Customer has an overdue balance");
                                }else{
                                    Intent IntentSalesOrderActivity = new Intent(MainActivity.this, SalesOrderActivity.class);
                                    startActivity(IntentSalesOrderActivity);
                                    finish();
                                }
                            }

                        }


                    }

                }
            }
        });


        alertDialog.show();

    }

    void  messagebox(String alerttext) {

        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);

        dlgAlert.setMessage(alerttext);
        dlgAlert.setTitle("Main");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();

        dlgAlert.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    public static long calculateDays(Date dateEarly, Date dateLater) {
        return (dateLater.getTime() - dateEarly.getTime()) / (24 * 60 * 60 * 1000);
    }

    public void ViewPromoInfo(String path) {

        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = MainActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_promoinfo, null);
        dialogBuilder.setView(dialogView);
        final android.app.AlertDialog alertDialog = dialogBuilder.create();

        TextView ok = (TextView) dialogView.findViewById(R.id.tvOk);

        alertDialog.setCanceledOnTouchOutside(false);

        File imgFile = new  File(path);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ImageView promoImage = (ImageView) dialogView.findViewById(R.id.ivImage);

            promoImage.setImageBitmap(myBitmap);

        }

        ok.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });


        alertDialog.show();


    }



    public void onBackPressed() {

        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Close")
                .setMessage("Are you sure you want to close the DPOS application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        ActivityCompat.finishAffinity(MainActivity.this);

                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mLastClickTime = 0;
                    }

                })
                .show();

    }

    void readtransact(){

        try {
            FileInputStream fileIn=openFileInput("transact.txt");
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
            controller.transact = s ;


        } catch (Exception e) {
            controller.transact = "YES";
        }


    }




}
