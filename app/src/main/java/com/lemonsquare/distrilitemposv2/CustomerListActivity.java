package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class CustomerListActivity extends Activity {

    DBController controller = new DBController(this);
    ListView CLDetails;
    List<HashMap<String, String>> CLViewCList;
    SimpleAdapter laCLDetails;
    EditText etCLSearch;
        TextView tvCLTitle,tvDCTCustName,tvDCTCancel,tvDCTOk;
    Button btnCLInfo,btnCLARBalance,btnCLTransact;
    BottomNavigationView menu;
    RadioButton rbDCTARPMent,rbDCTReturns,rbDCTNSales;
    String datediff = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerlist);

        CLDetails = (ListView) findViewById(R.id.lvCLDetail);
        etCLSearch = (EditText) findViewById(R.id.etCLSearch);
        menu = (BottomNavigationView) findViewById(R.id.btCLNavigation);

        CLViewCList = controller.fetchCustomerList();

        DetailActivity();
        controller.dbLReturns = 0.00;

        if (controller.PCNm == 1){
            DialogCustomerSelecttransaction();
            CLDetails.setSelection(controller.PLVposition);
        }else{
            CLDetails.setSelection(controller.PLVposition);
        }

        etCLSearch.addTextChangedListener(new TextWatcher() {

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

                    CLViewCList.clear();
                    CLDetails.invalidateViews();

                    CLViewCList = controller.fetchCustomerList();
                    DetailActivity();
                }else{

                    CLViewCList.clear();
                    CLDetails.invalidateViews();
                    controller.PSCName = etCLSearch.getText().toString();
                    CLViewCList = controller.searchCustomerList();
                    DetailActivity();
                }
            }

        });

        CLDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                HashMap<String, Object> obj = (HashMap<String, Object>) laCLDetails.getItem(position);

                String objCustomerCode = (String) obj.get("CustomerCode");
                controller.PCCode = objCustomerCode.replace("'","");
                String objCustomer = (String) obj.get("Customer");
                controller.PCLName = objCustomer; //.replace("'","");
                String objTerms = (String) obj.get("Terms");
                controller.PTerms = StringUtils.substringAfter(objTerms, ":");
                String objLimit = (String) obj.get("Limit");

                try{
                    controller.PLimit = Double.valueOf(StringUtils.substringAfter(objLimit, ":"));
                }catch (Exception e){
                    controller.PLimit = 0;
                }

                controller.PLVposition = position;

                DialogCustomerList();

            }
        });

        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mc_add:

                                Intent intentNewIncdentReport = new Intent(CustomerListActivity.this, NewCustomerActivity.class);
                                startActivity(intentNewIncdentReport);
                                finish();

                                break;

                        }
                        return true;
                    }
                });

    }


    public void DetailActivity() {


        try {
            laCLDetails = new SimpleAdapter(this, CLViewCList, R.layout.item_customerlist,
                    new String[]{"CustomerCode","Customer", "Limit", "Terms"}, new int[]{
                    R.id.rowsCustomerCode,R.id.rowsCustomer, R.id.rowsLimit, R.id.rowsTerms}) {
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
            CLDetails.setAdapter(laCLDetails);
        } catch (Exception e) {

        }

    }

    public void DialogCustomerList() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CustomerListActivity.this);
        LayoutInflater inflater = CustomerListActivity.this.getLayoutInflater();
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

                controller.Prscl = 2;
                controller.PCNm = 0;

                Intent intentCustomerInfoActivity = new Intent(CustomerListActivity.this, CustomerInfoActivity.class);
                startActivity(intentCustomerInfoActivity);
                finish();

            }
        });

        btnCLARBalance.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //controller.PCCode = controller.fetchCCodeCustomers();

                if (controller.fetchCountARBalancesItem() > 0){
                    controller.Prscl = 2;
                    controller.PCNm = 0;

                    Intent intentARBalanceActivity = new Intent(CustomerListActivity.this, AccountsReceivableActivity.class);
                    startActivity(intentARBalanceActivity);
                    finish();
                }else{

                    messagebox(controller.PCLName + " has no pending AR");

                }

            }
        });

        btnCLTransact.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                controller.Prscl = 2;

                if (controller.PLimit == 0){

                    messagebox("Please use the CASH customer to transact with new customers");

                }else{
                    //controller.PCCode = controller.fetchCCodeCustomers();

                    if (controller.fetchLastOdometerCustomer().equals(controller.PCCode)){
                        alertDialog.dismiss();
                        DialogCustomerSelecttransaction();
                    }else{
                        if (controller.PCCode.equals("CAS" + controller.fetchdbSettings().get(6))){

                            controller.Prscl = 2;

                            controller.PIsSOrder = 1;

                            controller.PPayment = 0;

                            controller.PIndicator = 0;

                            controller.dbGAmt = 0.00;
                            controller.dbNSales = 0.00;
                            controller.dbCGiven = 0.00;
                            controller.PDiscAmt = 0.00;
                            controller.PDiscount = 0.00;

                            controller.PCLName = "";

                            Intent intentNewCustomerCheckInActivity = new Intent(CustomerListActivity.this, NewCustomerCheckInActivity.class);
                            startActivity(intentNewCustomerCheckInActivity);
                            finish();
                        }else{
                            Intent intentCustomerCheckInActivity = new Intent(CustomerListActivity.this, CustomerCheckInActivity.class);
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CustomerListActivity.this);
        LayoutInflater inflater = CustomerListActivity.this.getLayoutInflater();
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


        if (controller.PIsWlk == 1){
            rbDCTARPMent.setVisibility(View.GONE);
            rbDCTReturns.setVisibility(View.GONE);
            rbDCTNSales.setChecked(true);
        }else{
            rbDCTARPMent.setChecked(true);
        }

        tvDCTCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controller.Prscl = 0;
                alertDialog.dismiss();
            }
        });

        tvDCTOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (rbDCTARPMent.isChecked() == false && rbDCTReturns.isChecked() == false && rbDCTNSales.isChecked() == false){
                    messagebox("please select transaction");
                }else{

                    //controller.PCCode = controller.fetchCCodeCustomers();

                    if (rbDCTARPMent.isChecked() == true){
                        controller.PIndicator = 0;

                        //DecimalFormat ARAmt = new DecimalFormat("#,###,##0.00");
                        if (controller.fetchCountARBalancesItem() > 0){
                            controller.Prscl = 2;
                            controller.PIsSOrder = 0;
                            controller. dbCGiven = 0.00;
                            controller.dbLReturns = 0.00;

                            Intent intentARBalanceActivity = new Intent(CustomerListActivity.this, ARPaymentActivity.class);
                            startActivity(intentARBalanceActivity);
                            finish();
                        }else{
                            messagebox(controller.PCLName + " has no pending AR");
                        }
                    }else if (rbDCTReturns.isChecked() == true){
                        controller.PRItem = 0;
                        controller.Prscl = 2;

                        controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;

                        Intent IntentReturnedItemActivity = new Intent(CustomerListActivity.this, ReturnedItemActivity.class);
                        startActivity(IntentReturnedItemActivity);
                        finish();
                    }else {

                        controller.Prscl = 2;

                        controller.PIsSOrder = 1;

                        controller.PPayment = 0;

                        controller.PIndicator = 0;

                        controller.dbGAmt = 0.00;
                        controller.dbNSales = 0.00;
                        controller.dbCGiven = 0.00;
                        controller.PDiscAmt = 0.00;
                        controller.PDiscount = 0.00;

                        if (controller.PIsWlk == 1) {

                            if (controller.fetchSalesH().equals("2") && controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))){
                                messagebox("you have exceeded the transaction limit");
                            }else{
                                Intent IntentSalesOrderActivity = new Intent(CustomerListActivity.this, SalesOrderActivity.class);
                                startActivity(IntentSalesOrderActivity);
                                finish();
                            }

                        } else {

                            if (controller.PTerms.equals("COD")){

                                controller.dbLReturns = Double.valueOf(controller.fetchBalanceARBal(controller.PCCode));
                                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;

                                if (controller.dbLReturns != 0.00){
                                    controller.dbLReturns = controller.dbLReturns * -1;
                                }

                                Intent IntentSalesOrderActivity = new Intent(CustomerListActivity.this, SalesOrderActivity.class);
                                startActivity(IntentSalesOrderActivity);
                                finish();

                            }else{

                                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;

                                if (controller.fetchLateAR().equals("")){
                                    controller.lesslimit = controller.PLimit ;
                                    Intent IntentSalesOrderActivity = new Intent(CustomerListActivity.this, SalesOrderActivity.class);
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


                                        Double overduepayment = 0.00;

                                        overduepayment = controller.fetchSUMAmtARBalances()- controller.fetchSUMAmtPdPaymentCash();

                                        if (overduepayment == 0.00){
                                            controller.lesslimit = controller.PLimit;
                                            Intent IntentSalesOrderActivity = new Intent(CustomerListActivity.this, SalesOrderActivity.class);
                                            startActivity(IntentSalesOrderActivity);
                                            finish();
                                        }else{
                                            messagebox("Customer has an overdue balance");
                                        }

                                    }else{

                                        if (!controller.fetchLateAR().equals("") ){ //&& !controller.fetchCountPayment().equals("")
                                            if(controller.PTerms.equals("15-D") || controller.PTerms.equals("21-D") || controller.PTerms.equals("30-D")|| controller.PTerms.equals("7-1D")){
                                                messagebox("you need to pay your previous balance");
                                            }else{
                                                controller.lesslimit = controller.PLimit - controller.fetchSUMARBalances();
                                                //messagebox("Limit:" + String.valueOf(controller.lesslimit));
                                                Intent IntentSalesOrderActivity = new Intent(CustomerListActivity.this, SalesOrderActivity.class);
                                                startActivity(IntentSalesOrderActivity);
                                                finish();
                                            }

                                        }else{
                                            controller.lesslimit = controller.PLimit - controller.fetchSUMARBalances() ;
                                            //messagebox("Limit:" + String.valueOf(controller.lesslimit));
                                            Intent IntentSalesOrderActivity = new Intent(CustomerListActivity.this, SalesOrderActivity.class);
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

        new android.support.v7.app.AlertDialog.Builder(CustomerListActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Customer List")
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

    public void onBackPressed() {

        controller.PCNm = 0;

        Intent IntentMainActivity = new Intent(CustomerListActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }

}


