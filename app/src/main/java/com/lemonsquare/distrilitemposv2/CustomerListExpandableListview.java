package com.lemonsquare.distrilitemposv2;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import es.dmoral.toasty.Toasty;

import static com.lemonsquare.distrilitemposv2.DBController.alCheckNumber;

public class CustomerListExpandableListview extends BaseExpandableListAdapter {

    private Context _context;
    List<HashMap<String, String>> CLHeader;

    public static final int request_checkin = 0;
    public static String datediff = "";

    List<HashMap<String, String>> CLCustomerLogs;

    public CustomerListExpandableListview(Context context, List<HashMap<String, String>> listCLHeader) {
        this._context = context;
        this.CLHeader = listCLHeader;

    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return 1;

    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_customerlistbtn, null);
        }

        if (groupPosition % 2 == 1) {
            convertView.setBackgroundResource(R.color.odd);
        } else {
            convertView.setBackgroundResource(R.color.even);
        }

        final DBController controller = new DBController(_context);

        Button CLBMap = (Button) convertView.findViewById(R.id.CLBMap);
        Button CLBInfo = (Button) convertView.findViewById(R.id.CLBInfo);
        Button CLBARBal = (Button) convertView.findViewById(R.id.CLBARBal);
        Button CLBTransact = (Button) convertView.findViewById(R.id.CLBTransact);

        if (controller.PIsCustomer == 0){
            if (controller.transact.equals("YES")){
                CLBTransact.setVisibility(View.VISIBLE);
            }else{
                CLBTransact.setVisibility(View.GONE);
            }
        }

        controller.PCCode = "";
        controller.PCLName = "";
        controller.PTerms = "";
        controller.PLimit = 0.00;

        controller.PCCode = CLHeader.get(groupPosition).get("CustomerCode");
        controller.PCLName = CLHeader.get(groupPosition).get("Customer");
        controller.PTerms = StringUtils.substringAfter(CLHeader.get(groupPosition).get("Terms"),":");
        controller.PLimit = Double.valueOf(StringUtils.substringAfter(CLHeader.get(groupPosition).get("Limit"), ":"));


     /*   controller.dbLReturns = 0.00;
        controller.PPayment = 0;
        controller.dbGAmt = 0.00;
        controller.dbNSales = 0.00;
        controller.dbCGiven = 0.00;
        controller.PDiscAmt = 0.00;
        controller.PDiscount = 0.00;*/

        String ccode,scode;
        ccode = controller.PCCode;
        scode = ccode.substring(0,3);

        if (scode.equals("WLK") || scode.equals("INH") || controller.PCLName.equals(controller.fetchdbSettings().get(6) + "-CASH SALES") || controller.fetchCustomer().get(5).equals("6")){
            controller.PIsWlk = 1;
            CLBInfo.setVisibility(View.GONE);
        }else{
            controller.PIsWlk = 0;
            CLBInfo.setVisibility(View.VISIBLE);
        }


        CLBMap.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                /*Double longitude;
                Double latitude;
                longitude = 120.9542;
                latitude = 14.7542;

                String uri =  "http://maps.google.com/maps?q=loc:" + latitude + "," + longitude + " (MAP)";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                _context.startActivity(intent);*/

                String location;
                location =  controller.fetchLocation(controller.PCCode);

                if (location ==  null){
                    Toasty.error(_context,"no location tag", Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("geo:" + location));
                    _context.startActivity(intent);
                }


            }
        });

        CLBInfo.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent myIntent = new Intent(_context, NewCustomerActivity2.class);
                myIntent.putExtra("isAdd", "2");
                myIntent.putExtra("CustomerCode", CLHeader.get(groupPosition).get("CustomerCode"));
                _context.startActivity(myIntent);

            }
        });

        CLBARBal.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (controller.fetchCountARBalancesItem() > 0){
                    controller.Prscl = 2;
                    controller.PCNm = 0;

                    Intent myIntent = new Intent(_context, AccountsReceivableActivity.class);
                    _context.startActivity(myIntent);
                }else{

                    messagebox(controller.PCLName + " has no pending AR",_context);
                }
            }
        });

        CLBTransact.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (controller.fetchISSDR() == 2){
                    Toasty.info(_context,"please time in first", Toast.LENGTH_LONG).show();
                }else if (controller.fetchTimeInOutComplete() == 1){
                    Toasty.info(_context,"you have already completed your transaction for today", Toast.LENGTH_LONG).show();
                }else{
                    controller.dbLReturns = 0.00;
                    controller.PPayment = 0;
                    controller.dbGAmt = 0.00;
                    controller.dbNSales = 0.00;
                    controller.dbCGiven = 0.00;
                    controller.PDiscAmt = 0.00;
                    controller.PDiscount = 0.00;
                    controller.PIndicator = 0;
                    controller.PIsSOrder = 1;
                    controller.PDefaultPricelist = controller.fetchCustomer().get(4);
                    alCheckNumber.clear();
                    controller.Prscl = 2;

                    if (controller.fetchCustomer().get(5).equals("6")){

                        messagebox("Please use the CASH customer to transact with new customers",_context);

                    }else {

                        if (controller.PCCode.equals("CAS" + controller.fetchdbSettings().get(6))) {
                            Intent myIntent = new Intent(_context, NewCustomerCheckInActivity.class);
                            ((Activity) _context).startActivityForResult(myIntent,request_checkin);
                        }else if (controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))) {
                            Intent myIntent = new Intent(_context, SalesOrderActivity.class);
                            ((Activity) _context).startActivityForResult(myIntent,request_checkin);
                        }else if (controller.fetchActiveLogs() == 0){
                            if (controller.fetchCustomerExists(controller.PCCode) == 1){
                                messagebox("You have already finish activity to " + controller.PCCode + " - " + controller.PCLName ,_context);
                            }else{
                                controller.PRItem = 0;
                                controller.Prscl = 2;

                                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;
                                Intent myIntent = new Intent(_context, CustomerCheckInActivity.class);
                                ((Activity) _context).startActivityForResult(myIntent,request_checkin);
                            }
                        }else{

                                CLCustomerLogs = controller.fetchActiveCustomerLogs();
                                if (CLCustomerLogs.get(0).get("CustomerCode").equals(controller.PCCode)){

                                    if (CLCustomerLogs.get(0).get("Txn").equals("1")){
                                        controller.PRItem = 0;
                                        controller.Prscl = 2;

                                        controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;
                                        Intent myIntent = new Intent(_context, ReturnedItemActivity.class);
                                        _context.startActivity(myIntent);
                                    }else if (CLCustomerLogs.get(0).get("Txn").equals("2")){
                                        Intent myIntent = new Intent(_context, CustomerInventoryActivity.class);
                                        _context.startActivity(myIntent);
                                    }else if (CLCustomerLogs.get(0).get("Txn").equals("3")){
                                        Intent myIntent = new Intent(_context, SalesOrderActivity.class);
                                        _context.startActivity(myIntent);
                                    }else if (CLCustomerLogs.get(0).get("Txn").equals("4")){
                                        Intent myIntent = new Intent(_context, CheckDisplayActivity.class);
                                        _context.startActivity(myIntent);
                                    }else if (CLCustomerLogs.get(0).get("Txn").equals("5")){
                                        Intent myIntent = new Intent(_context, PriceSurveyActivity.class);
                                        myIntent.putExtra("CustomerCode", controller.PCCode);
                                        _context.startActivity(myIntent);
                                    }else{
                                        Intent myIntent = new Intent(_context, CustomerCheckInActivity.class);
                                        _context.startActivity(myIntent);
                                    }
                                }else{
                                    messagebox("You have pending transaction to " + CLCustomerLogs.get(0).get("CustomerCode") + "-" + CLCustomerLogs.get(0).get("CustomerName"),_context);
                                }

                        }

                        /*else if (controller.fetchLastOdometerCustomer().equals(controller.PCCode)){
                            DialogCustomerSelecttransaction(_context);
                        }else{

                        }*/

                    }




                //messagebox(controller.PDefaultPricelist,_context);
                //messagebox(controller.PDefaultPricelist,_context);





                    /*if (controller.fetchLastOdometerCustomer().equals(controller.PCCode)) {
                        DialogCustomerSelecttransaction(_context);
                    } else {
                        if (controller.PCCode.equals("CAS" + controller.fetchdbSettings().get(6))) {

                            controller.Prscl = 2;

                            controller.PIsSOrder = 1;

                            *//*controller.PPayment = 0;

                            controller.PIndicator = 0;

                            controller.dbGAmt = 0.00;
                            controller.dbNSales = 0.00;
                            controller.dbCGiven = 0.00;
                            controller.PDiscAmt = 0.00;
                            controller.PDiscount = 0.00;*//*

                            controller.PCLName = "";

                            *//*Intent intentNewCustomerCheckInActivity = new Intent(CustomerListActivity.this, NewCustomerCheckInActivity.class);
                            startActivity(intentNewCustomerCheckInActivity);
                            finish();*//*

                            Intent myIntent = new Intent(_context, NewCustomerCheckInActivity.class);
                            ((Activity) _context).startActivityForResult(myIntent,request_checkin);

                        } else {
                            *//*Intent intentCustomerCheckInActivity = new Intent(CustomerListActivity.this, CustomerCheckInActivity.class);
                            startActivity(intentCustomerCheckInActivity);
                            finish();*//*

                            Intent myIntent = new Intent(_context, CustomerCheckInActivity.class);
                            ((Activity) _context).startActivityForResult(myIntent,request_checkin);



                        }

                    }*/
                }
            }
        });

        return convertView;
    }

    public static void DialogCustomerSelecttransaction(final Context context) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        View dialogView = inflater.inflate(R.layout.dialog_customerselecttransaction, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        TextView tvDCTCustName = (TextView) dialogView.findViewById(R.id.tvDCTCustName);
        final RadioButton rbDCTARPMent = (RadioButton) dialogView.findViewById(R.id.rbDCTARPMent);
        final RadioButton rbDCTReturns = (RadioButton) dialogView.findViewById(R.id.rbDCTReturns);
        final RadioButton rbDCTNSales = (RadioButton) dialogView.findViewById(R.id.rbDCTNSales);
        final RadioButton rbDCTPSurvey = (RadioButton) dialogView.findViewById(R.id.rbDCTPSurvey);
        TextView tvDCTCancel = (TextView) dialogView.findViewById(R.id.tvDCTCancel);
        TextView tvDCTOk = ( TextView) dialogView.findViewById(R.id.tvDCTOk);

        final DBController controller = new DBController(context);

        if (controller.fetchCountPriceSurvey() == 0){
            rbDCTPSurvey.setVisibility(View.GONE);
        }

        tvDCTCustName.setText(controller.PCLName);

        if (controller.PIsWlk == 1){
            rbDCTARPMent.setVisibility(View.GONE);
            rbDCTReturns.setVisibility(View.GONE);
            rbDCTPSurvey.setVisibility(View.GONE);
            rbDCTNSales.setChecked(true);
        }else{
            rbDCTARPMent.setChecked(true);
        }

        tvDCTCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                controller.Prscl = 0;
                alertDialog.dismiss();
            }
        });

        tvDCTOk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                if (rbDCTARPMent.isChecked() == false && rbDCTReturns.isChecked() == false && rbDCTNSales.isChecked() == false && rbDCTPSurvey.isChecked() == false){
                    messagebox("please select transaction",context);
                }else{

                    controller.dbLReturns = 0.00;
                    controller.PPayment = 0;
                    controller.dbGAmt = 0.00;
                    controller.dbNSales = 0.00;
                    controller.dbCGiven = 0.00;
                    controller.PDiscAmt = 0.00;
                    controller.PDiscount = 0.00;
                    controller.PIndicator = 0;

                    //controller.PCCode = controller.fetchCCodeCustomers();

                    if (rbDCTARPMent.isChecked() == true){
                        //controller.PIndicator = 0;

                        //DecimalFormat ARAmt = new DecimalFormat("#,###,##0.00");
                        if (controller.fetchCountARBalancesItem() > 0){
                            controller.Prscl = 2;
                            controller.PIsSOrder = 0;
                        /*    controller. dbCGiven = 0.00;
                            controller.dbLReturns = 0.00;*/

                            Intent myIntent = new Intent(context, ARPaymentActivity.class);
                            context.startActivity(myIntent);
                        }else{
                            messagebox(controller.PCLName + " has no pending AR",context);
                        }
                    }else if (rbDCTReturns.isChecked() == true){
                        controller.PRItem = 0;
                        controller.Prscl = 2;

                        controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;

                        Intent myIntent = new Intent(context, ReturnedItemActivity.class);
                        context.startActivity(myIntent);
                    }else if (rbDCTPSurvey.isChecked() == true){
                        Intent myIntent = new Intent(context, PriceSurveyActivity.class);
                        myIntent.putExtra("CustomerCode", controller.PCCode);
                        context.startActivity(myIntent);
                    }else {

                        controller.Prscl = 2;

                        controller.PIsSOrder = 1;

                        /*controller.PPayment = 0;

                        controller.PIndicator = 0;

                        controller.dbGAmt = 0.00;
                        controller.dbNSales = 0.00;
                        controller.dbCGiven = 0.00;
                        controller.PDiscAmt = 0.00;
                        controller.PDiscount = 0.00;*/ //01/11/2019

                        if (controller.PIsWlk == 1) {

                            if (controller.fetchSalesH().equals("2") && controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))){
                                messagebox("you have exceeded the transaction limit",context);
                            }else{
                                Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                context.startActivity(myIntent);
                            }

                        } else {

                            if (controller.PTerms.equals("COD")){

                                controller.dbLReturns = controller.fetchSUMAmtARBalancesCOD();
                                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;

                                if (controller.dbLReturns != 0.00){
                                    controller.dbLReturns = controller.dbLReturns * -1;
                                }

                                //messagebox(String.valueOf(controller.dbLReturns),context);

                                Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                context.startActivity(myIntent);
                            }else{

                                controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1 ;

                                if (controller.fetchLateAR().equals("")){
                                    if (controller.fetchCreditExpo() == 0.00){
                                        controller.lesslimit = controller.PLimit ;
                                        Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                        context.startActivity(myIntent);
                                    }else{
                                        controller.lesslimit = controller.PLimit - controller.fetchCreditExpo() ;
                                        Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                        context.startActivity(myIntent);
                                    }

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

                                    //controller.dbLReturns = 0.00;


                                    if (Integer.valueOf(controller.fetchCreditTerms().get(0)) < Integer.valueOf(datediff)){


                                       /* Double overduepayment = 0.00;

                                        overduepayment = controller.fetchCreditExpo()- controller.fetchSUMAmtPdPaymentCash();*/

                                        /*if (overduepayment == 0.00){
                                            controller.lesslimit = controller.PLimit;
                                            Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                            context.startActivity(myIntent);
                                        }else{
                                            if (controller.fetchCreditExpo() < 0.00){
                                                controller.lesslimit = controller.PLimit;
                                                controller.dbLReturns = 0.00;
                                                Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                                context.startActivity(myIntent);
                                            }else{
                                                messagebox("Customer has an overdue balance",context);
                                            }

                                        }*/
                                        if (controller.fetchCreditExpo() == 0.00){
                                            controller.lesslimit = controller.PLimit;
                                            Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                            context.startActivity(myIntent);
                                        }else if (controller.fetchSUMARBalances() == 0.00 ){

                                            if (controller.fetchCreditExpo() >  0.00){
                                                if (controller.fetchSUMAmtPdPaymentCheck() > 0.00){
                                                    messagebox("Customer has an overdue balance",context);
                                                }else if (controller.fetchSUMAmtPdPaymentCashOD() == 0.00){
                                                    messagebox("Customer has an overdue balance",context);
                                                }else{
                                                    controller.lesslimit = controller.PLimit - controller.fetchCreditExpo();
                                                    Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                                    context.startActivity(myIntent);
                                                }

                                            }else{
                                                controller.lesslimit = controller.PLimit - controller.fetchCreditExpo();
                                                Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                                context.startActivity(myIntent);
                                            }
                                        }else{
                                            messagebox("Customer has an overdue balance",context);
                                        }

                                    }else{

                                        if (!controller.fetchLateAR().equals("") ){ //&& !controller.fetchCountPayment().equals("")
                                            //if(controller.PTerms.equals("15-D") || controller.PTerms.equals("21-D") || controller.PTerms.equals("30-D")|| controller.PTerms.equals("7-1D")){
                                            //    messagebox("you need to pay your previous balance",context);
                                            //}else{
                                                /*controller.lesslimit = controller.PLimit - controller.fetchSUMARBalances();
                                                //messagebox("Limit:" + String.valueOf(controller.lesslimit));
                                                Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                                context.startActivity(myIntent);*/

                                               /* if (controller.fetchChkDtAmt().size() > 0){
                                                    Double lessAmt = 0.00;

                                                    for (int i = 0; i < controller.fetchChkDtAmt().size(); i++){

                                                        String dtfrom = controller.fetchLateAR();
                                                        Date dt1 = new Date(dtfrom);
                                                        Date dt2 = new Date(controller.fetchChkDtAmt().get(i).get("ChkDt"));

                                                        Calendar caldt1 = Calendar.getInstance();
                                                        caldt1.setTime(dt1);
                                                        Calendar caldt2 = Calendar.getInstance();
                                                        caldt2.setTime(dt2);

                                                        datediff2 = String.valueOf(calculateDays(dt1, dt2));

                                                        if ((Integer.valueOf(controller.fetchCreditTerms().get(0)) <  Integer.valueOf(datediff2))){
                                                            lessAmt = lessAmt + Double.valueOf(controller.fetchChkDtAmt().get(i).get("Amt"));
                                                        }

                                                    }

                                                    controller.lesslimit = controller.PLimit - controller.fetchCreditExpo() - lessAmt;
                                                    messagebox("Limits:" + String.valueOf(controller.lesslimit),context);
                                                    *//*Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                                    context.startActivity(myIntent);*//*

                                                }else{*/
                                                    controller.lesslimit = controller.PLimit - controller.fetchCreditExpo();
                                                    //messagebox("Limit:" + String.valueOf(controller.lesslimit),context);
                                                    Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                                    context.startActivity(myIntent);
                                                //}

                                            //}

                                        }else{

                                                controller.lesslimit = controller.PLimit;
                                                //messagebox("Limit:" + String.valueOf(controller.lesslimit));
                                                Intent myIntent = new Intent(context, SalesOrderActivity.class);
                                                context.startActivity(myIntent);

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

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;

    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.CLHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.CLHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {


        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.item_customerlist, null);

        }

        if (groupPosition % 2 == 1) {
            convertView.setBackgroundResource(R.color.odd);
        } else {
            convertView.setBackgroundResource(R.color.even);
        }

        TextView rowsCustomerCode = (TextView) convertView
                .findViewById(R.id.rowsCustomerCode);

        TextView rowsCustomer = (TextView) convertView
                .findViewById(R.id.rowsCustomer);

        TextView rowsLimit = (TextView) convertView
                .findViewById(R.id.rowsLimit);

        TextView rowsTerms = (TextView) convertView
                .findViewById(R.id.rowsTerms);

        rowsCustomerCode.setText(CLHeader.get(groupPosition).get("CustomerCode"));
        rowsCustomerCode.setVisibility(View.GONE);
        rowsCustomer.setText(CLHeader.get(groupPosition).get("Customer"));
        rowsLimit.setText(CLHeader.get(groupPosition).get("Limit"));
        rowsTerms.setText(CLHeader.get(groupPosition).get("Terms"));

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public static void  messagebox(String alerttext,Context context) {

        new android.support.v7.app.AlertDialog.Builder(context)
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



}
