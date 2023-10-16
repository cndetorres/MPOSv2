package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.lemonsquare.distrilitemposv2.DBController.alCheckNumber;

public class PaymentActivity extends Activity {

    DBController controller = new DBController(this);
    TextView tvPCust,tvPAmtDueItem,tvPAmtPdItem,tvPBank,tvPBranch,tvPAcctNo,tvPDate;
    MaterialBetterSpinner spPPMode;
    EditText etPAmt,etPRemarks,etPBank,etPBranch,etPAcctNo,etPDate;
    Button btnPDate,btnPCancel,btnPAccept;
    String[] strPCash = {"CASH"};
    String[] strPOCash = {"CASH","TMS"};
    String[] strPCashCSales = {"CASH"};
    String[] strPOCashCSales = {"CASH","CHARGED SALES","TMS"};
    String[] strPCashCheck = {"CASH","CHECK"};
    String[] strPOCashCheck = {"CASH","CHARGED SALES","CHECK","TMS"};
    final Calendar myCalendar = Calendar.getInstance();
    String datediff;
    final DecimalFormat ARAmt = new DecimalFormat("#,###,##0.00");
    android.support.v7.app.AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvPCust = (TextView) findViewById(R.id.tvPCust);
        tvPAmtDueItem = (TextView) findViewById(R.id.tvPAmtDueItem);
        tvPAmtPdItem = (TextView) findViewById(R.id.tvPAmtPdItem);
        spPPMode = (MaterialBetterSpinner) findViewById(R.id.spPPMode);
        etPAmt = (EditText) findViewById(R.id.etPAmt);
        etPRemarks = (EditText) findViewById(R.id.etPRemarks);
        tvPBank = (TextView) findViewById(R.id.tvPBank);
        etPBank = (EditText) findViewById(R.id.etPBank);
        tvPBranch = (TextView) findViewById(R.id.tvPBranch);
        etPBranch = (EditText) findViewById(R.id.etPBranch);
        tvPAcctNo = (TextView) findViewById(R.id.tvPAcctNo);
        etPAcctNo = (EditText) findViewById(R.id.etPAcctNo);
        tvPDate = (TextView) findViewById(R.id.tvPDate);
        btnPDate = (Button) findViewById(R.id.btnPDate);
        etPDate = (EditText) findViewById(R.id.etPDate);
        btnPCancel = (Button) findViewById(R.id.btnPCancel);
        btnPAccept = (Button) findViewById(R.id.btnPAccept);




        if (controller.fetchCreditTerms().get(1).equals("1")){
            if (controller.PIsSOrder == 1){
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPOCash);
                spPPMode.setAdapter(arrayAdapter);
                    spPPMode.setText("CASH");


            }else{
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPCash);
                spPPMode.setAdapter(arrayAdapter);

                spPPMode.setText("CASH");

            }
        }else if (controller.fetchCreditTerms().get(1).equals("2")){
            if (controller.PIsSOrder == 1){
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPOCashCSales);
                spPPMode.setAdapter(arrayAdapter);


                 if (controller.PTerms.equals("COD")){
                    spPPMode.setText("CASH");
                }else{
                    spPPMode.setText("CHARGED SALES");
                     etPAmt.setText(ARAmt.format(controller.dbAmtDue - controller.dbAmtPd));
                }

            }else{
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPCashCSales);
                spPPMode.setAdapter(arrayAdapter);

                spPPMode.setText("CASH");

            }
        }else{
            if (controller.PIsSOrder == 1){
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPOCashCheck);
                spPPMode.setAdapter(arrayAdapter);


                if (controller.PTerms.equals("COD")){
                    spPPMode.setText("CASH");
                }else{
                    spPPMode.setText("CHARGED SALES");
                    etPAmt.setText(ARAmt.format(controller.dbAmtDue - controller.dbAmtPd));
                }

            }else{
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPCashCheck);
                spPPMode.setAdapter(arrayAdapter);

                spPPMode.setText("CASH");
            }
        }


        hidecheck();

        /*if (controller.fetchCreditTerms().get(1).equals("1")){
            if (controller.PIsSOrder == 1){
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPCashSales);
                spPPMode.setAdapter(arrayAdapter);
            }else{
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                      d  android.R.layout.simple_dropdown_item_1line, strPCash);
                spPPMode.setAdapter(arrayAdapter);
            }

        }else{
            if (controller.PIsSOrder == 1){
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPCashCheckSales);
                spPPMode.setAdapter(arrayAdapter);
            }else{
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_dropdown_item_1line, strPCashCheck);
                spPPMode.setAdapter(arrayAdapter);
            }
        }*/





        tvPCust.setText(controller.PCLName);
        tvPAmtDueItem.setText(ARAmt.format((controller.dbAmtDue)));
        tvPAmtPdItem.setText(ARAmt.format((controller.dbAmtPd)));


        spPPMode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (spPPMode.getText().toString().equals("CASH") && controller.PIsSOrder == 0){
                    hidecheck();
                    etPAmt.setEnabled(true);
                    etPAmt.setText("");
                }else if(spPPMode.getText().toString().equals("CASH") && controller.PIsSOrder == 1){
                    hidecheck();
                    etPAmt.setEnabled(true);
                    etPAmt.setText(ARAmt.format(controller.dbAmtDue - controller.dbAmtPd));
                } else if (spPPMode.getText().toString().equals("TMS")){
                    hidecheck();
                    etPAmt.setEnabled(false);
                    etPAmt.setText("0.00");
                }else if (spPPMode.getText().toString().equals("CHARGED SALES")) {
                    hidecheck();
                    etPAmt.setEnabled(true);
                    etPAmt.setText(ARAmt.format(controller.dbAmtDue - controller.dbAmtPd));
                }else if (spPPMode.getText().toString().equals("CHECK") && controller.PIsSOrder == 0){
                    unhidecheck();
                    etPAmt.setEnabled(true);
                    etPAmt.setText("");
                    long ldate = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    etPDate.setText(sdf.format(ldate));

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    PaymentDetailsActivity.PCheckDt = String .valueOf(timestamp.getTime());

                    Date d1 = new Date(controller.fetchLateAR());
                    Date d2 = new Date(etPDate.getText().toString());

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(d1);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(d2);

                    datediff = String.valueOf(calculateDays(d1, d2));

                }else if (spPPMode.getText().toString().equals("CHECK") && controller.PIsSOrder == 1){
                    unhidecheck();

                    long ldate = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    etPDate.setText(sdf.format(ldate));

                    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                    PaymentDetailsActivity.PCheckDt = String .valueOf(timestamp.getTime());

                    etPAmt.setEnabled(true);
                    etPAmt.setText(ARAmt.format(controller.dbAmtDue - controller.dbAmtPd));
                }
            }
        });

        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                /*myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                etPDate.setText(sdf.format(myCalendar.getTime()));

                Date date = myCalendar.getTime();
                Timestamp timestamp = new Timestamp(date.getTime());
                PaymentDetailsActivity.PCheckDt = String .valueOf(timestamp.getTime());

                if (controller.PIsSOrder == 0){
                    Date date = myCalendar.getTime();
                    Timestamp timestamp = new Timestamp(date.getTime());
                    PaymentDetailsActivity.PCheckDt = String .valueOf(timestamp.getTime());

                    Date d1 = new Date(controller.PBillDt);
                    Date d2 = new Date(etPDate.getText().toString());

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(d1);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(d2);

                    datediff = String.valueOf(calculateDays(d1, d2));
                }*/

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                etPDate.setText(sdf.format(myCalendar.getTime()));
                Date date = myCalendar.getTime();
                Timestamp timestamp = new Timestamp(date.getTime());
                PaymentDetailsActivity.PCheckDt = String .valueOf(timestamp.getTime());

                if (controller.PIsSOrder == 0) {

                    Date d1 = new Date(controller.fetchLateAR());
                    Date d2 = new Date(etPDate.getText().toString());

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(d1);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(d2);

                    datediff = String.valueOf(calculateDays(d1, d2));
                }

            }

        };



        btnPDate.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog =
                        new DatePickerDialog(PaymentActivity.this, datePickerListener, mYear, mMonth, mDay);


                Date today = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(today);
                c.add( Calendar.YEAR, -1 );
                long minDate = c.getTime().getTime();

                Date otherday = new Date();
                Calendar d = Calendar.getInstance();
                d.setTime(otherday);
                d.add( Calendar.YEAR, + 1 );
                long maxDate = d.getTime().getTime();

                datePickerDialog.getDatePicker().setMinDate(minDate);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);
                datePickerDialog.show();
            }
        });

        btnPCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                gotoactivity();
            }
        });

        btnPAccept.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (spPPMode.getText().toString().equals("CASH")){
                    if ( controller.PCashExist == 1){
                        messagebox("Cash transaction already exists");
                    } else if (etPAmt.getText().toString().equals("")){
                        etPAmt.setError("please input amount");
                    }/*else if (Double.valueOf(etPAmt.getText().toString()) < Double.valueOf(controller.fetchdbSettings().get(16)) && controller.PIsSOrder == 0){
                        etPAmt.setError("minimum charge amount is " + controller.fetchdbSettings().get(16) );
                    }*/
                  /*  else if (controller.PCCode.equals("INH" + controller.fetchdbSettings().get(6)) && etPRemarks.getText().toString().equals("")) {
                        etPRemarks.setError("please input remarks");
                    }*/
                    else{
                        PaymentDetailsActivity.PAmt = ARAmt.format(Double.valueOf(etPAmt.getText().toString().replace(",","")));
                        PaymentDetailsActivity.PPMode = spPPMode.getText().toString();
                        PaymentDetailsActivity.PRemarks = etPRemarks.getText().toString();
                        PaymentDetailsActivity.PBank = "";
                        PaymentDetailsActivity.PBranch = "";
                        PaymentDetailsActivity.PCheckNo = "";
                        PaymentDetailsActivity.PCheckDt = "";

                        controller.PPayment = 2;
                        Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                        startActivity(IntentPaymentDetailsActivity);
                        finish();
                    }
                }else if (spPPMode.getText().toString().equals("TMS")){
                    if (etPRemarks.getText().toString().equals("")){
                        etPRemarks.setError("TMS Number is required in Remarks field");
                    }else if (controller.hmPDDetails.size() > 0){
                        messagebox("TMS payment cannot be added with other payment modes");
                    } else{
                        PaymentDetailsActivity.PAmt = ARAmt.format(Double.valueOf(etPAmt.getText().toString().replace(",","")));
                        PaymentDetailsActivity.PPMode = spPPMode.getText().toString();
                        PaymentDetailsActivity.PRemarks = etPRemarks.getText().toString();
                        PaymentDetailsActivity.PBank = "";
                        PaymentDetailsActivity.PBranch = "";
                        PaymentDetailsActivity.PCheckNo = "";
                        PaymentDetailsActivity.PCheckDt = "";
                        controller.dbBalance = 0.00;

                        controller.PPayment = 3;
                        Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                        startActivity(IntentPaymentDetailsActivity);
                        finish();
                    }

                }else if (spPPMode.getText().toString().equals("CHARGED SALES")){
                    if ( controller.PCashExist == 2) {
                        messagebox("Charge transaction already exists");
                    }else if (etPAmt.getText().toString().equals("")){
                        etPAmt.setError("please input amount");
                    }else if (Double.valueOf(etPAmt.getText().toString().replace(",","")) < Double.valueOf(controller.fetchdbSettings().get(16))){
                        etPAmt.setError("minimum charge amount is " + controller.fetchdbSettings().get(16) );
                    //}else if ((Double.valueOf(tvPAmtDueItem.getText().toString().replace(",","")) - Double.valueOf(tvPAmtPdItem.getText().toString().replace(",",""))) > Double.valueOf(etPAmt.getText().toString().replace(",",""))){
                        //messagebox(String.valueOf(controller.dbBalance));
                    //    etPAmt.setError("amount must be less than current balance");
                    }else if(Double.valueOf(etPAmt.getText().toString().replace(",","")) > (controller.dbAmtDue - controller.dbAmtPd)) {
                        etPAmt.setError("Amount must less than the current balance ");
                    }else{
                      /*  messagebox(String.valueOf(controller.dbBalance));

                        messagebox("Amount:" + String.valueOf(Double.valueOf(etPAmt.getText().toString().replace(",",""))));*/
                        PaymentDetailsActivity.PAmt = ARAmt.format(Double.valueOf(etPAmt.getText().toString().replace(",","")));
                        PaymentDetailsActivity.PPMode = spPPMode.getText().toString();
                        PaymentDetailsActivity.PRemarks = etPRemarks.getText().toString();
                        PaymentDetailsActivity.PBank = "";
                        PaymentDetailsActivity.PBranch = "";
                        PaymentDetailsActivity.PCheckNo = "";
                        PaymentDetailsActivity.PCheckDt = "";

                        controller.PPayment = 2;
                        Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                        startActivity(IntentPaymentDetailsActivity);
                        finish();

                    }

                }else{

                    if (etPAmt.getText().toString().equals("")){
                        etPAmt.setError("please input amount");
                    }/*else if (Double.valueOf(etPAmt.getText().toString()) < Double.valueOf(controller.fetchdbSettings().get(16)) && controller.PIsSOrder == 0){
                        etPAmt.setError("minimum charge amount is " + controller.fetchdbSettings().get(16) );
                    }*/else if(isStringNullOrWhiteSpace(etPBank.getText().toString())){
                        etPBank.setError("please input bank");
                    }else if(isStringNullOrWhiteSpace(etPBranch.getText().toString())){
                        etPBranch.setError("please input branch");
                    }else if(isStringNullOrWhiteSpace(etPAcctNo.getText().toString())){
                        etPAcctNo.setError("please input account no");
                    }else if(isStringNullOrWhiteSpace(etPDate.getText().toString())){
                        etPDate.setError("please select date");
                    }else if (controller.fetchcountchecks(etPAcctNo.getText().toString()) > 0){
                        messagebox("check number already exists");
                    }else if (alCheckNumber.contains(etPAcctNo.getText().toString())){
                        messagebox("check number already exists");
                    }
                    else if (controller.PIsSOrder == 0 ){

                        if (Integer.valueOf(controller.fetchCreditTerms().get(0)) < Integer.valueOf(datediff)){

                            new AlertDialog.Builder(PaymentActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Check Payment")
                                    .setMessage("The check date is beyond the allowed days.Allow payment?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            PaymentDetailsActivity.PAmt = ARAmt.format(Double.valueOf(etPAmt.getText().toString().replace(",","")));
                                            PaymentDetailsActivity.PPMode = spPPMode.getText().toString();
                                            PaymentDetailsActivity.PRemarks = etPRemarks.getText().toString();
                                            PaymentDetailsActivity.PBank = etPBank.getText().toString();
                                            PaymentDetailsActivity.PBranch = etPBranch.getText().toString();
                                            PaymentDetailsActivity.PCheckNo = etPAcctNo.getText().toString();
                                            //PaymentDetailsActivity.PCheckDt = etPDate.getText().toString();

                                            controller.PPayment = 2;
                                            Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                                            startActivity(IntentPaymentDetailsActivity);
                                            finish();
                                        }

                                    })
                                    .setNegativeButton("No", null)
                                    .show();

                        }else{

                            PaymentDetailsActivity.PAmt = ARAmt.format(Double.valueOf(etPAmt.getText().toString().replace(",","")));
                            PaymentDetailsActivity.PPMode = spPPMode.getText().toString();
                            PaymentDetailsActivity.PRemarks = etPRemarks.getText().toString();
                            PaymentDetailsActivity.PBank = etPBank.getText().toString();
                            PaymentDetailsActivity.PBranch = etPBranch.getText().toString();
                            PaymentDetailsActivity.PCheckNo = etPAcctNo.getText().toString();
                            //PaymentDetailsActivity.PCheckDt = etPDate.getText().toString();

                            controller.PPayment = 2;
                            Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                            startActivity(IntentPaymentDetailsActivity);
                            finish();

                        }





                    }else{
                            PaymentDetailsActivity.PAmt = ARAmt.format(Double.valueOf(etPAmt.getText().toString().replace(",","")));
                            PaymentDetailsActivity.PPMode = spPPMode.getText().toString();
                            PaymentDetailsActivity.PRemarks = etPRemarks.getText().toString();
                            PaymentDetailsActivity.PBank = etPBank.getText().toString();
                            PaymentDetailsActivity.PBranch = etPBranch.getText().toString();
                            PaymentDetailsActivity.PCheckNo = etPAcctNo.getText().toString();

                        controller.PPayment = 2;
                        Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                        startActivity(IntentPaymentDetailsActivity);
                        finish();

                    }

                    alCheckNumber.add(etPAcctNo.getText().toString());

                }

            }
        });


    }

    void hidecheck(){
        tvPBank.setVisibility(View.GONE);
        etPBank.setVisibility(View.GONE);
        tvPBranch.setVisibility(View.GONE);
        etPBranch.setVisibility(View.GONE);
        tvPAcctNo.setVisibility(View.GONE);
        etPAcctNo.setVisibility(View.GONE);
        tvPDate.setVisibility(View.GONE);
        btnPDate.setVisibility(View.GONE);
        etPDate.setVisibility(View.GONE);
    }
    void unhidecheck(){
        tvPBank.setVisibility(View.VISIBLE);
        etPBank.setVisibility(View.VISIBLE);
        tvPBranch.setVisibility(View.VISIBLE);
        etPBranch.setVisibility(View.VISIBLE);
        tvPAcctNo.setVisibility(View.VISIBLE);
        etPAcctNo.setVisibility(View.VISIBLE);
        tvPDate.setVisibility(View.VISIBLE);
        btnPDate.setVisibility(View.VISIBLE);
        etPDate.setVisibility(View.VISIBLE);
    }
    public static long calculateDays(Date dateEarly, Date dateLater) {
        return (dateLater.getTime() - dateEarly.getTime()) / (24 * 60 * 60 * 1000);
    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(PaymentActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Payment")
                .setCancelable(false)
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



    void gotoactivity(){

        if (controller.hmPDDetails.size() > 0 ){
            controller.PPayment = 1;

            Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
            startActivity(IntentPaymentDetailsActivity);
            finish();
        }else{
            if (controller.PIsSOrder == 0){
                controller.PIndicator = 1;
                Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                startActivity(IntentPaymentDetailsActivity);
                finish();
            }else{

                controller.PIndicator = 0;
                Intent IntentPaymentDetailsActivity = new Intent(PaymentActivity.this, PaymentDetailsActivity.class);
                startActivity(IntentPaymentDetailsActivity);
                finish();
            }
        }
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

        gotoactivity();
    }
}
