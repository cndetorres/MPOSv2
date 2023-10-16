package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ViewReturnsActivity extends Activity {

    DBController controller = new DBController(this);
    ListView VRDetails,VRHeader;
    ArrayList<HashMap<String, String>> hmVRHeader;
    ListAdapter laVRHeader,laVRDetails;
    HashMap<String, String> mVRHeader;
    List<HashMap<String, String>> VRViewVReturns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewreturns);


        VRDetails = (ListView) findViewById(R.id.lvVRDetail);
        VRHeader = (ListView) findViewById(R.id.lvVRHeader);

        VRViewVReturns = controller.fetchViewReturns();

        ViewHeaderListview();
        ViewDetailListview();

    }

    public void ViewHeaderListview() {

        hmVRHeader = new ArrayList<HashMap<String, String>>();
        mVRHeader = new HashMap<String, String>();

        mVRHeader.put("Type", "TYPE");
        mVRHeader.put("Item", "ITEM");
        mVRHeader.put("Qty", " QTY");
        mVRHeader.put("Customer", "CUSTOMER");
        mVRHeader.put("Remarks", "REMARKS");
        hmVRHeader.add(mVRHeader);

        try {
            laVRHeader = new SimpleAdapter(this, hmVRHeader, R.layout.item_viewreturns,
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

    public void ViewDetailListview(){

        try {
            laVRDetails = new SimpleAdapter(this, VRViewVReturns, R.layout.item_viewreturns,
                    new String[]{"Type", "Item", "Qty", "Customer","Remarks"}, new int[]{
                    R.id.rowsType, R.id.rowsItem, R.id.rowsQty, R.id.rowsCustomer,R.id.rowsRemarks})  {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
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

    public void onBackPressed() {
        Intent IntentMiscellaneousActivityActivity = new Intent(ViewReturnsActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivityActivity);
        finish();
    }


}


