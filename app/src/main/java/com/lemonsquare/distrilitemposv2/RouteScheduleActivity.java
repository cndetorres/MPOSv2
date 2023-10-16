package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class RouteScheduleActivity extends Activity {

    DBController controller = new DBController(this);
    ListView RSDetails;
    List<HashMap<String, String>> RSViewRSchedule;
    SimpleAdapter laRSDetails;
    EditText etRSSearch;
    TextView tvCLTitle,tvDCTCustName,tvDCTCancel,tvDCTOk;
    Button btnCLInfo,btnCLARBalance,btnCLTransact;
    RadioButton rbDCTARPMent,rbDCTReturns,rbDCTNSales;
    String datediff = "";

    static final int READ_BLOCK_SIZE = 100;

    String routeschedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routeschedule);

        RSDetails = (ListView) findViewById(R.id.lvRSDetail);
        etRSSearch = (EditText) findViewById(R.id.etRSSearch);

        readrouteschedule();
        routesched();

        controller.dbLReturns = 0.00;

        RSViewRSchedule = controller.fetchRouteSchedule();

        DetailActivity();

        if (controller.PCNm == 1){
            DialogCustomerSelecttransaction();
            RSDetails.setSelection(controller.PLVposition);
        }else{
            RSDetails.setSelection(controller.PLVposition);
        }

        RSDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                HashMap<String, Object> obj = (HashMap<String, Object>) laRSDetails.getItem(position);

                String objCustomerCode = (String) obj.get("CustomerCode");
                controller.PCCode = objCustomerCode;
                String objCustomer = (String) obj.get("Customer");
                controller.PCLName = objCustomer;

                controller.PTerms = controller.fetchCustomer().get(1);
                controller.PLimit = Double.valueOf(controller.fetchCustomer().get(2));
                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1;
                controller.PDiscAmt = 0.00;

                controller.PLVposition = position;

                DialogCustomerList();

            }
        });

        etRSSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {


            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                if (arg0.length() == 0){

                    RSViewRSchedule.clear();
                    RSDetails.invalidateViews();

                    RSViewRSchedule = controller.fetchRouteSchedule();
                    DetailActivity();
                }else{

                    RSViewRSchedule.clear();
                    RSDetails.invalidateViews();
                    controller.PSRSchedule = etRSSearch.getText().toString();
                    RSViewRSchedule = controller.searchRouteSchedule();
                    DetailActivity();
                }
            }

        });


    }


    public void DetailActivity() {


        try {
             laRSDetails = new SimpleAdapter(this, RSViewRSchedule, R.layout.item_routeschedule,
                    new String[]{"CustomerCode","Customer", "Schedule", "Address"}, new int[]{
                    R.id.rowsCustomerCode,R.id.rowsCustomer, R.id.rowsSchedule, R.id.rowsAddress}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView rcustomercode = (TextView) view.findViewById(R.id.rowsCustomerCode);
                    rcustomercode.setVisibility(View.GONE);
                    if (position % 2 == 1) {
                        view.setBackgroundColor(Color.parseColor("#fff2dc"));
                    } else {
                        view.setBackgroundColor(Color.parseColor("#f7e1a8"));
                    }
                    return view;
                }
            };
            RSDetails.setAdapter(laRSDetails);
        } catch (Exception e) {

        }

    }

    public void DialogCustomerList() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RouteScheduleActivity.this);
        LayoutInflater inflater = RouteScheduleActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customerlist, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        tvCLTitle = (TextView)dialogView.findViewById(R.id.tvCLTitle);
        btnCLInfo = (Button) dialogView.findViewById(R.id.btnCLInfo);
        btnCLARBalance = (Button) dialogView.findViewById(R.id.btnCLARBalance);
        btnCLTransact= (Button) dialogView.findViewById(R.id.btnCLTransact);

        tvCLTitle.setText(controller.PCLName);

        if (controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6)) || controller.PCCode.equals("INH" + controller.fetchdbSettings().get(6)) || controller.PCCode.equals("CAS" + controller.fetchdbSettings().get(6))){
            controller.PIsWlk = 1;
        }else{
            controller.PIsWlk = 0;
        }

        btnCLInfo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                controller.Prscl = 1;
                controller.PCNm = 0;

                Intent intentCustomerInfoActivity = new Intent(RouteScheduleActivity.this, CustomerInfoActivity.class);
                startActivity(intentCustomerInfoActivity);
                finish();
            }
        });

        btnCLARBalance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //controller.PCCode = controller.fetchCCodeCustomers();

                if (controller.fetchCountARBalancesItem() > 0){
                    controller.Prscl = 1;
                    controller.PCNm = 0;

                    Intent intentARBalanceActivity = new Intent(RouteScheduleActivity.this, AccountsReceivableActivity.class);
                    startActivity(intentARBalanceActivity);
                    finish();
                }else{
                    messagebox(controller.PCLName + " has no pending AR");
                }

            }
        });

        btnCLTransact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                    //controller.PCCode = controller.fetchCCodeCustomers();
                    /*controller.Prscl = 1;
                    controller.PTerms = controller.fetchCustomer().get(1);
                    controller.PLimit = Double.valueOf(controller.fetchCustomer().get(2));
                    controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1;
                    controller.PDiscAmt = 0.00;

                    if (controller.fetchLastOdometerCustomer().equals(controller.PCCode)){
                        alertDialog.dismiss();
                        DialogCustomerSelecttransaction();
                    }else{
                        Intent intentCustomerCheckInActivity = new Intent(RouteScheduleActivity.this, CustomerCheckInActivity.class);
                        startActivity(intentCustomerCheckInActivity);
                        finish();
                    }*/

                controller.Prscl = 1;

                if (controller.PLimit == 0){

                    messagebox("Please use the CASH customer to transact with new customers");

                }else {
                    //controller.PCCode = controller.fetchCCodeCustomers();

                    if (controller.fetchLastOdometerCustomer().equals(controller.PCCode)) {
                        alertDialog.dismiss();
                        DialogCustomerSelecttransaction();
                    } else {
                        if (controller.PCCode.equals("CAS" + controller.fetchdbSettings().get(6))) {
                            Intent intentNewCustomerCheckInActivity = new Intent(RouteScheduleActivity.this, NewCustomerCheckInActivity.class);
                            startActivity(intentNewCustomerCheckInActivity);
                            finish();
                        } else {
                            Intent intentCustomerCheckInActivity = new Intent(RouteScheduleActivity.this, CustomerCheckInActivity.class);
                            startActivity(intentCustomerCheckInActivity);
                            finish();
                        }

                    }
                }


            }
        });


        alertDialog.show();

    }

    public void DialogCustomerSelecttransaction() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RouteScheduleActivity.this);
        LayoutInflater inflater = RouteScheduleActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_customerselecttransaction, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

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
                controller.Prscl = 0;
                alertDialog.dismiss();
            }
        });

        tvDCTOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (rbDCTARPMent.isChecked() == false && rbDCTReturns.isChecked() == false && rbDCTNSales.isChecked() == false) {
                    messagebox("please select transaction");
                } else {
                    if (rbDCTARPMent.isChecked() == true) {
                        controller.PIndicator = 0;
                        //controller.PCCode = controller.fetchCCodeCustomers();

                        if (controller.fetchCountARBalancesItem() > 0) {
                            controller.Prscl = 1;
                            controller.PIsSOrder = 0;
                            controller. dbCGiven = 0.00;
                            controller.dbLReturns = 0.00;

                            Intent intentARBalanceActivity = new Intent(RouteScheduleActivity.this, ARPaymentActivity.class);
                            startActivity(intentARBalanceActivity);
                            finish();
                        } else {
                            messagebox(controller.PCLName + " has no pending AR");
                        }
                    } else if (rbDCTReturns.isChecked() == true) {
                        controller.PRItem = 0;
                        controller.Prscl = 1;
                        controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;
                        Intent IntentReturnedItemActivity = new Intent(RouteScheduleActivity.this, ReturnedItemActivity.class);
                        startActivity(IntentReturnedItemActivity);
                        finish();
                    } else {
                        /*controller.Prscl = 1;

                        controller.PIsSOrder = 1;

                        controller.PPayment = 0;

                        controller.PIndicator = 0;

                        controller.dbGAmt = 0.00;
                        controller.dbNSales = 0.00;
                        controller.dbCGiven = 0.00;
                        controller.PDiscAmt = 0.00;


                        if (controller.PTerms.equals("COD")){



                            controller.dbLReturns = Double.valueOf(controller.fetchBalanceARBal(controller.PCCode));



                            if (controller.dbLReturns != 0.00){
                                controller.dbLReturns = controller.dbLReturns * -1;
                            }

                            Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                            startActivity(IntentSalesOrderActivity);
                            finish();

                        }else{
                            if (controller.fetchLateAR().equals("")){
                                Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
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
                                    if (!controller.fetchLateAR().equals("")){
                                        if(controller.PTerms.equals("15-D") || controller.PTerms.equals("21-D") || controller.PTerms.equals("30-D")|| controller.PTerms.equals("7-1D")){
                                            messagebox("you need to pay your previous balance");
                                        }else{
                                            Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                            startActivity(IntentSalesOrderActivity);
                                            finish();
                                        }

                                    }else{
                                        Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                        startActivity(IntentSalesOrderActivity);
                                        finish();
                                    }
                                }
                            }*/

                        controller.Prscl = 1;

                        controller.PIsSOrder = 1;

                        controller.PPayment = 0;

                        controller.PIndicator = 0;

                        controller.dbGAmt = 0.00;
                        controller.dbNSales = 0.00;
                        controller.dbCGiven = 0.00;
                        controller.PDiscAmt = 0.00;
                        controller.PDiscount = 0.00;

                        if (controller.PIsWlk == 1) {

                            if (controller.fetchSalesH().equals("2") && controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))) {
                                messagebox("you have exceeded the transaction limit");
                            } else {
                                Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                startActivity(IntentSalesOrderActivity);
                                finish();
                            }

                        } else {

                            if (controller.PTerms.equals("COD")) {

                                controller.dbLReturns = Double.valueOf(controller.fetchBalanceARBal(controller.PCCode));
                                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;

                                if (controller.dbLReturns != 0.00) {
                                    controller.dbLReturns = controller.dbLReturns * -1;
                                }

                                Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                startActivity(IntentSalesOrderActivity);
                                finish();

                            } else {

                                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1;

                                if (controller.fetchLateAR().equals("")) {
                                    controller.lesslimit = controller.PLimit;
                                    Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                    startActivity(IntentSalesOrderActivity);
                                    finish();
                                } else {
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


                                    if (Integer.valueOf(controller.fetchCreditTerms().get(0)) < Integer.valueOf(datediff)) {


                                        Double overduepayment = 0.00;

                                        overduepayment = controller.fetchSUMAmtARBalances() - controller.fetchSUMAmtPdPaymentCash();

                                        if (overduepayment == 0.00) {
                                            controller.lesslimit = controller.PLimit;
                                            Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                            startActivity(IntentSalesOrderActivity);
                                            finish();
                                        } else {
                                            messagebox("Customer has an overdue balance");
                                        }

                                    } else {

                                        if (!controller.fetchLateAR().equals("") ) {//&& !controller.fetchCountPayment().equals("")
                                            if (controller.PTerms.equals("15-D") || controller.PTerms.equals("21-D") || controller.PTerms.equals("30-D") || controller.PTerms.equals("7-1D")) {
                                                messagebox("you need to pay your previous balance");
                                            } else {
                                                controller.lesslimit = controller.PLimit - controller.fetchSUMARBalances();
                                                //messagebox("Limit:" + String.valueOf(controller.lesslimit));
                                                Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                                startActivity(IntentSalesOrderActivity);
                                                finish();
                                            }

                                        } else {
                                            controller.lesslimit = controller.PLimit - controller.fetchSUMARBalances();
                                            //messagebox("Limit:" + String.valueOf(controller.lesslimit));
                                            Intent IntentSalesOrderActivity = new Intent(RouteScheduleActivity.this, SalesOrderActivity.class);
                                            startActivity(IntentSalesOrderActivity);
                                            finish();
                                        }


                                    }
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

        new AlertDialog.Builder(RouteScheduleActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Route Schedule")
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

    public static long calculateDays(Date dateEarly, Date dateLater) {
        return (dateLater.getTime() - dateEarly.getTime()) / (24 * 60 * 60 * 1000);
    }

    void routesched(){
        String day = "";
        controller.days.clear();
        for (int i = 0;i<routeschedule.length();i++){
            if(routeschedule.charAt(i) == ','){
                controller.days.add(day);
                day = "";
            }else{
                day = day + routeschedule.charAt(i);
            }
        }
        controller.days.add(day);
    }

    void readrouteschedule() {

        try {
            FileInputStream fileIn = openFileInput("routeschedule.txt");
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            routeschedule = s;


        } catch (Exception e) {
            routeschedule = "";
        }
    }

    public void onBackPressed() {

        controller.PCNm = 0;

        Intent IntentMainActivity = new Intent(RouteScheduleActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }



}

