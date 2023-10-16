package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.lemonsquare.distrilitemposv2.CustomerListExpandableListview.DialogCustomerSelecttransaction;
import static com.lemonsquare.distrilitemposv2.DBController.alCheckNumber;

public class RouteScheduleExpandableListview extends BaseExpandableListAdapter {

    private Context _context;
    List<HashMap<String, String>> RSHeader;
    List<HashMap<String, String>> CLCustomerLogs;

    public static final int request_checkin = 0;

    public RouteScheduleExpandableListview(Context context, List<HashMap<String, String>> listCLHeader) {
        this._context = context;
        this.RSHeader = listCLHeader;

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


        controller.PCCode = RSHeader.get(groupPosition).get("CustomerCode");
        controller.PCLName = RSHeader.get(groupPosition).get("Customer");
        controller.PTerms = RSHeader.get(groupPosition).get("Terms");

        controller.dbLReturns = 0.00;

        try{
            controller.PLimit = Double.valueOf(RSHeader.get(groupPosition).get("Limit"));
        }catch (Exception e){
            controller.PLimit = 0;
        }

        String ccode,scode;
        ccode = controller.PCCode;
        scode = ccode.substring(0,3);

        if (scode.equals("WLK") || scode.equals("INH") || controller.PCLName.equals(controller.fetchdbSettings().get(6) + "-CASH SALES") || controller.fetchCustomer().get(5).equals("6")){
            controller.PIsWlk = 1;
        }else{
            controller.PIsWlk = 0;
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


        CLBInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(_context, NewCustomerActivity2.class);
                myIntent.putExtra("isAdd", "2");
                myIntent.putExtra("CustomerCode", RSHeader.get(groupPosition).get("CustomerCode"));
                _context.startActivity(myIntent);
            }
        });

        CLBARBal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (controller.fetchCountARBalancesItem() > 0){
                    controller.Prscl = 1;
                    controller.PCNm = 0;

                    Intent myIntent = new Intent(_context, AccountsReceivableActivity.class);
                    _context.startActivity(myIntent);
                }else{

                    messagebox(controller.PCLName + " has no pending AR",_context);
                }
            }
        });

        CLBTransact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                    }
                }

            }
        });




        return convertView;
    }



    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;

    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.RSHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.RSHeader.size();
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
            convertView = infalInflater.inflate(R.layout.item_routeschedule, null);

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

        TextView rowsSchedule= (TextView) convertView
                .findViewById(R.id.rowsSchedule);

        TextView rowsAddress = (TextView) convertView
                .findViewById(R.id.rowsAddress);

        rowsCustomerCode.setText(RSHeader.get(groupPosition).get("CustomerCode"));
        rowsCustomerCode.setVisibility(View.GONE);
        rowsCustomer.setText(RSHeader.get(groupPosition).get("Customer"));
        rowsSchedule.setText(RSHeader.get(groupPosition).get("Schedule"));
        rowsAddress.setText(RSHeader.get(groupPosition).get("Address"));

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




}

