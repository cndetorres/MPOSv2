package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class SalesDailyReportActivity extends Activity {

    EditText sales,collection,bo,deposits,checks,cashonhand,odometer,nofcalls,nofchecks;
    Button submit;
    String message;
    DBController controller = new DBController(this);
    double cashOnHand = 0.00;
    List<HashMap<String, String>> alSalesItemsToday,alEndingInventoryToday,alBankRemittance;
    String salesItem;
    String salesID;
    final static int RQS_2 = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesdailyreport);

        sales = (EditText) findViewById(R.id.etSales);
        collection = (EditText)findViewById(R.id.etCollection);
        bo = (EditText) findViewById(R.id.etBO);
        deposits = (EditText) findViewById(R.id.etCashDeposited);
        checks = (EditText)findViewById(R.id.etChecks);
        cashonhand = (EditText)findViewById(R.id.etCashOnHand);
        odometer = (EditText)findViewById(R.id.etOdometer);
        submit = (Button) findViewById(R.id.btnSubmit);
        nofcalls = (EditText) findViewById(R.id.etNoOfCalls);
        nofchecks = (EditText)findViewById(R.id.etNoOfChecks);

        DecimalFormat SPAmt = new DecimalFormat("#,##0.00");
        final DecimalFormat COHAmt = new DecimalFormat("#.00");

        /*sdst.setText(controller.fetchdbSettings().get(6));*/
        sales.setText(controller.fetchSalesTodayDSR());
        collection.setText(String.valueOf(SPAmt.format(controller.fetchCollTodaySDR())));
        bo.setText(controller.fetchBOTodayDSR());
        cashOnHand = Double.valueOf(COHAmt.format(controller.fetchSUMPaymentItemSDR())) - controller.fetchSUMCashDepositedSDR();

       /* if(cashOnHand < 0){
            cashOnHand = 0.00;
        }*/
        cashonhand.setText(String.valueOf(SPAmt.format(cashOnHand)));
        deposits.setText(String.valueOf(SPAmt.format(controller.fetchSUMCashDepositedSDR())));
        checks.setText(String.valueOf(SPAmt.format(controller.fetchSUMChecksSDR())));
        odometer.setText(String.valueOf(controller.fetchLastOdoToday() - controller.fetchFirstOdoToday()));
        nofcalls.setText(String.valueOf(controller.fetchCountCalls()));
        nofchecks.setText(String.valueOf(controller.fetchCountChecks()));
        //alarmcancel();



        submit.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (controller.fetchISSDR() == 2){
                    Toasty.info(getApplicationContext(),"please time in first", Toast.LENGTH_LONG).show();
                }else if (controller.fetchISSDR() == 1){
                    Toasty.info(getApplicationContext(),"you have already submitted daily sales report", Toast.LENGTH_LONG).show();
                }else{
                    new AlertDialog.Builder(SalesDailyReportActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Daily Sales Report")
                            .setMessage("Are you sure you want to submit daily sales report?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {


                                    alarmcancel();
                                    notification();

                                    message = "DSTDSR" + " " + controller.fetchdbSettings().get(3) + "," + controller.fetchdbSettings().get(6) +"," + sales.getText().toString().replace(",","")  + "," + collection.getText().toString().replace(",","") + "," + bo.getText().toString().replace(",","") + "," + deposits.getText().toString().replace(",","") + "," + checks.getText().toString().replace(",","") + "," + cashonhand.getText().toString().replace(",","") + "," + nofchecks.getText().toString() + "," + nofcalls.getText().toString() + ","+  odometer.getText().toString();
                                    Utils.sendSMS(SalesDailyReportActivity.this,message);



                                    DateFormat defaultDateFormat = new SimpleDateFormat("yyMMdd");
                                    Calendar defaultDate = Calendar.getInstance();
                                    String todayDate = defaultDateFormat.format(defaultDate.getTime());

                                    salesID = todayDate + controller.fetchdbSettings().get(6);

                                    //sendSalesItem();
                                    //sendEndingInventory();
                                    //bankRemittance();

                                    controller.updateAttendanceDSR();

                                    String strDefaultDateTime;
                                    long defaultDateTime = System.currentTimeMillis();
                                    DateFormat sdfDateTime = new SimpleDateFormat("yyMMddHHmmss");
                                    strDefaultDateTime = sdfDateTime.format(defaultDateTime);
                                    Timestamp tsDateTime = new Timestamp(System.currentTimeMillis());



                                    controller.insertTransaction("DSR" + strDefaultDateTime ,"DSR",controller.fetchdbSettings().get(6),controller.fetchdbSettings().get(6),tsDateTime.getTime(),0.0);



                                    Toasty.success(getApplicationContext(),"daily sales report successfully sent", Toast.LENGTH_LONG).show();
                                    Intent main = new Intent(SalesDailyReportActivity.this, DailyAttendanceActivity.class);
                                    startActivity(main);
                                    finish();
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();

                }
                }
        });
    }

    private void alarmcancel(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("Activity", "channel1");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }
    private void notification(){

        try{
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            Calendar defaultDate = Calendar.getInstance();
            String alarmDate = sdf.format(defaultDate.getTime());
            Date alarmDateFormat = sdf.parse(alarmDate);
            Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
            intent.putExtra("Activity", "channel2");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_2, intent, 0);
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmDateFormat.getTime(), pendingIntent);

        }catch (ParseException e){

        }

    }


    public void sendSalesItem(){
        alSalesItemsToday = controller.fetchSalesItemToday();
        if (alSalesItemsToday.size()>0){
            int count = 1;
            salesItem = "";
            for(int i = 0; i < alSalesItemsToday.size();i++){
                salesItem = salesItem + alSalesItemsToday.get(i).get("ExtMatGrp") + ":" + alSalesItemsToday.get(i).get("Qty") + "/";
                if (count%9 == 0){
                    message = "SLSITM " + "2" + ","+ salesID + "," + controller.fetchdbSettings().get(6) + "," + salesItem;
                    Utils.sendSMS(SalesDailyReportActivity.this,message);
                    salesItem = "";
                }else if (count == alSalesItemsToday.size()){
                    message = "SLSITM " + "2" + ","+ salesID + "," + controller.fetchdbSettings().get(6) + "," + salesItem;
                    Utils.sendSMS(SalesDailyReportActivity.this,message);
                }
                count ++;

            }
        }
    }

    public void sendEndingInventory(){
        alEndingInventoryToday = controller.fetchEndingInventoryToday();
        if (alEndingInventoryToday.size()>0){
            int count = 1;
            salesItem = "";
            for(int i = 0; i < alEndingInventoryToday.size();i++){
                salesItem = salesItem + alEndingInventoryToday.get(i).get("ExtMatGrp") + ":" + alEndingInventoryToday.get(i).get("Qty") + "/";
                if (count%9 == 0){
                    message = "SLSITM " + "3" + ","+ salesID + "," + controller.fetchdbSettings().get(6) + "," + salesItem;
                    Utils.sendSMS(SalesDailyReportActivity.this,message);
                    salesItem = "";
                }else if (count == alEndingInventoryToday.size()){
                    message = "SLSITM " + "3" + ","+ salesID + "," + controller.fetchdbSettings().get(6) + "," + salesItem;
                    Utils.sendSMS(SalesDailyReportActivity.this,message);
                }
                count ++;

            }
        }
    }

    private void bankRemittance(){
        alBankRemittance = controller.fetchBankRemittance();
        String remittance;
        if (alBankRemittance.size()>0){
            remittance = "";
            for(int i = 0; i < alBankRemittance.size();i++){
                remittance = remittance + alBankRemittance.get(i).get("Bank") + ":" + alBankRemittance.get(i).get("AcctNo") + "." + alBankRemittance.get(i).get("Amt") + "/";
            }
            message = "REMIT " + controller.fetchdbSettings().get(6) + "," + remittance;
            Utils.sendSMS(SalesDailyReportActivity.this,message);
        }


    }

    @Override
    public void onBackPressed() {

        Intent main = new Intent(SalesDailyReportActivity.this, MiscellaneousActivity.class);
        startActivity(main);
        finish();

    }
}

