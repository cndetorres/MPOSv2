package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class ValidateReturnsActivity extends Activity {

    DBController controller = new DBController(this);
    ListView VRDetails,VRHeader;
    ArrayList<HashMap<String, String>> hmVRHeader;
    ListAdapter laVRHeader,laVRDetails;
    HashMap<String, String> mVRHeader,mVRDetail;
    BottomNavigationView VRmenu;
    EditText etVROdometer;
    String Pretid,Ptype,Pproducts,Pqty,Punit,PCustomer,Premarks;
    Button btnVRAdd,btnVRSub;
    TextView tvDVRRType,tvDVRCustomer,tvVRItem,tvDVRRemarks,tvVRCancel,tvVRSave;
    Integer intVRQty = 0;
    int Pposition;

    private long mLastClickTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validatereturns);

        VRHeader = (ListView) findViewById(R.id.lvVRHeader);
        VRDetails = (ListView) findViewById(R.id.lvVRDetail);
        VRmenu = (BottomNavigationView) findViewById(R.id.btVRNavigation);
        etVROdometer = (EditText) findViewById(R.id.etVROdomoter);

        if (controller.PVRNum == 0){
            controller.VRValidateReturns = controller.fetchValidateReturns();
        }else{
            etVROdometer.setText(controller.PVROdometer);
        }

        ViewHeaderListview();
        ViewDetailListview();


        VRDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();


                HashMap<String, Object> obj = (HashMap<String, Object>) laVRDetails.getItem(position);

                String objRetID = (String) obj.get("_id");
                Pretid = objRetID;
                String objType = (String) obj.get("Type");
                Ptype = objType;
                String objItem = (String) obj.get("Item");
                Pproducts = objItem;
                String objQty = (String) obj.get("Qty");
                Pqty = StringUtils.substringBefore(objQty, " ");
                Punit = StringUtils.substringAfter(objQty, " ");
                String objCustomer = (String) obj.get("Customer");
                PCustomer = objCustomer;
                String objRemarks = (String) obj.get("Remarks");
                Premarks = objRemarks;
                Pposition = position;

                DialogEditItem();

            }
        });


        VRmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mvr_next:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if (etVROdometer.getText().toString().equals("")){
                                    etVROdometer.setError("odometer reading is required");
                                }else{
                                    controller.PVROdometer = etVROdometer.getText().toString();

                                    controller.PVRNum = 1;

                                    Intent intentInventoryActivity = new Intent(ValidateReturnsActivity.this, InventoryActivity.class);
                                    startActivity(intentInventoryActivity);
                                    finish();
                                }

                                break;
                        }
                        return true;
                    }
                });



    }

    public void ViewHeaderListview() {

        hmVRHeader = new ArrayList<HashMap<String, String>>();
        mVRHeader = new HashMap<String, String>();

        mVRHeader.put("Type", "TYPE");
        mVRHeader.put("Item", "ITEM");
        mVRHeader.put("Qty", "QTY");
        mVRHeader.put("Customer", "CUSTOMER");
        mVRHeader.put("Remarks", "REMARKS");
        hmVRHeader.add(mVRHeader);

        try {
            laVRHeader = new SimpleAdapter(this, hmVRHeader, R.layout.item_validatereturns,
                    new String[]{"Type", "Item", "Qty", "Customer","Remarks"}, new int[]{
                    R.id.rowsType, R.id.rowsItem, R.id.rowsQty, R.id.rowsCustomer,R.id.rowsRemarks}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rtype = (TextView) view.findViewById(R.id.rowsType);
                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView rqty = (TextView) view.findViewById(R.id.rowsQty);
                    TextView rcustomer = (TextView) view.findViewById(R.id.rowsCustomer);
                    TextView rremarks = (TextView) view.findViewById(R.id.rowsRemarks);
                    if (position % 2 == 0) {
                        rtype.setTextColor(Color.WHITE);
                        ritem.setTextColor(Color.WHITE);
                        rqty.setTextColor(Color.WHITE);
                        rcustomer.setTextColor(Color.WHITE);
                        rremarks.setTextColor(Color.WHITE);
                        rtype.setTypeface(null, Typeface.BOLD);
                        ritem.setTypeface(null, Typeface.BOLD);
                        rqty.setTypeface(null, Typeface.BOLD);
                        rcustomer.setTypeface(null, Typeface.BOLD);
                        rremarks.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            VRHeader.setAdapter(laVRHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview() {

        mVRDetail = new HashMap<String, String>();

        try {
            laVRDetails = new SimpleAdapter(this, controller.VRValidateReturns,R.layout.item_validatereturns,
                    new String[]{"_id","Type", "Item", "Qty", "Customer","Remarks"}, new int[]{
                    R.id.rowsRetID,R.id.rowsType, R.id.rowsItem, R.id.rowsQty, R.id.rowsCustomer,R.id.rowsRemarks}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rretid = (TextView) view.findViewById(R.id.rowsRetID);
                    rretid.setVisibility(View.GONE);

                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;
                }
            };

            VRDetails.setAdapter(laVRDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateDetailListview(){

        mVRDetail = new HashMap<String, String>();

        mVRDetail.put("_id",Pretid);
        mVRDetail.put("Type", Ptype);
        mVRDetail.put("Item", Pproducts);
        mVRDetail.put("Qty", Pqty + " " + Punit);
        mVRDetail.put("Customer", PCustomer);
        mVRDetail.put("Remarks", Premarks);
        controller.VRValidateReturns.set(Pposition,mVRDetail);

        try {
            laVRDetails = new SimpleAdapter(this, controller.VRValidateReturns,R.layout.item_validatereturns,
                    new String[]{"_id","Type", "Item", "Qty", "Customer","Remarks"}, new int[]{
                    R.id.rowsRetID,R.id.rowsType, R.id.rowsItem, R.id.rowsQty, R.id.rowsCustomer,R.id.rowsRemarks}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rretid = (TextView) view.findViewById(R.id.rowsRetID);
                    rretid.setVisibility(View.GONE);

                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;

                }
            };
            VRDetails.setAdapter(laVRDetails);
        } catch (Exception e) {

        }
    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ValidateReturnsActivity.this);
        LayoutInflater inflater = ValidateReturnsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edititemvalidatereturns, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        btnVRAdd = (Button) dialogView.findViewById(R.id.btnDVRAdd);
        btnVRSub = (Button) dialogView.findViewById(R.id.btnDVRSub);
        tvDVRRType = (TextView) dialogView.findViewById(R.id.tvDVRRType);
        tvDVRCustomer = (TextView) dialogView.findViewById(R.id.tvDVRCustomer);
        tvVRItem = (TextView) dialogView.findViewById(R.id.tvDVRItem);
        tvDVRRemarks = (TextView) dialogView.findViewById(R.id.tvDVRRemarks);
        tvVRCancel = (TextView) dialogView.findViewById(R.id.tvDVRCancel);
        tvVRSave = (TextView) dialogView.findViewById(R.id.tvDVRSave);
        final EditText qty = (EditText) dialogView.findViewById(R.id.etDVRQty);

        tvDVRRType.setText(Ptype + "-" + controller.fetchReturnTypeName(Ptype));
        tvDVRCustomer.setText(PCustomer);
        tvVRItem.setText(Pproducts);
        tvDVRRemarks.setText(Premarks);

        qty.setText(Pqty);

        btnVRAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else{
                    intVRQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intVRQty.toString());
                }


            }
        });

        btnVRSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    qty.setError("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0) {
                    intVRQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intVRQty.toString());
                }


            }
        });

        tvVRCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                alertDialog.dismiss();

            }
        });

        tvVRSave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")) {
                    qty.setError("invalid quantity");
                } else {

                    Pqty = qty.getText().toString();

                    UpdateDetailListview();

                    Toasty.info(getApplicationContext(), Pproducts + " successfully updated", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog.show();
    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(ValidateReturnsActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Validate Returns")
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


    public void onBackPressed() {
        Intent IntentMainActitvity = new Intent(ValidateReturnsActivity.this, MainActivity.class);
        startActivity(IntentMainActitvity);
        finish();
    }

}





