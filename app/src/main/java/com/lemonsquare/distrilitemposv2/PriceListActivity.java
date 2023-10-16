package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PriceListActivity extends Activity {

    DBController controller = new DBController(this);
    ListView PLDetails,PLHeader;
    ArrayList<HashMap<String, String>> hmPLHeader;
    ListAdapter laPLHeader,laPLDetails;
    HashMap<String, String> mPLHeader;
    List<HashMap<String, String>> PLViewPList;
    MaterialBetterSpinner mbsPLList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pricelist);


        PLDetails = (ListView) findViewById(R.id.lvPLDetail);
        PLHeader = (ListView) findViewById(R.id.lvPLHeader);
        mbsPLList = (MaterialBetterSpinner) findViewById(R.id.spPLList);

        mbsPLList.setText(controller.fetchDefaultPListList());
        controller.PPlist = controller.fetchDefaultPListList();
        PLViewPList = controller.fetchPriceList();

        String[] strRCategory = controller.fetchDistinctPListList();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, strRCategory);
        mbsPLList.setAdapter(arrayAdapter);

        mbsPLList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                PLViewPList.clear();
                PLDetails.invalidateViews();

                controller.PPlist = mbsPLList.getText().toString();
                PLViewPList = controller.fetchPriceList();
                ViewDetailListview();
            }
        });

        ViewHeaderListview();
        ViewDetailListview();

    }

    public void ViewHeaderListview() {

        hmPLHeader = new ArrayList<HashMap<String, String>>();
        mPLHeader = new HashMap<String, String>();

        mPLHeader.put("Item", "ITEM");
        mPLHeader.put("List", "LIST");
        mPLHeader.put("Amt", "AMOUNT");
        mPLHeader.put("Unit", "UNIT");
        hmPLHeader.add(mPLHeader);

        try {
            laPLHeader = new SimpleAdapter(this, hmPLHeader, R.layout.item_priceslist,
                    new String[]{"Item", "List", "Amt", "Unit"}, new int[]{
                    R.id.rowsItem, R.id.rowsList, R.id.rowsAmt, R.id.rowsUnit}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView rlist = (TextView) view.findViewById(R.id.rowsList);
                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);
                    TextView runit = (TextView) view.findViewById(R.id.rowsUnit);
                    if (position % 2 == 0) {
                        ritem.setTextColor(Color.WHITE);
                        rlist.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        runit.setTextColor(Color.WHITE);
                        ritem.setTypeface(null, Typeface.BOLD);
                        rlist.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        runit.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            PLHeader.setAdapter(laPLHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laPLDetails = new SimpleAdapter(this, PLViewPList, R.layout.item_priceslist,
                    new String[]{"Item", "List", "Amt", "Unit"}, new int[]{
                    R.id.rowsItem, R.id.rowsList, R.id.rowsAmt, R.id.rowsUnit}) {
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
            PLDetails.setAdapter(laPLDetails);
        } catch (Exception e) {

        }
    }



    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(PriceListActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }

}


