package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class InventoryActivity extends Activity {

    DBController controller = new DBController(this);
    ListView IHeader,IDetails;
    SimpleAdapter laIDetails;
    BottomNavigationView Imenu;
    ArrayList<HashMap<String, String>> hmIHeader;
    ListAdapter laIHeader;
    HashMap<String, String> mIHeader,mIDetail;
    String Pproducts,Pqty,Punit;
    Button btnIAdd,btnISub,btnDEIAdd,btnDEISub,btnDEIScan;
    TextView tvIItem,tvIUnit,tvICancel,tvISave,tvDEIOk,tvDEINext;
    AutoCompleteTextView product;
    EditText qty;
    Integer intIQty = 0;
    int Pposition;
    ArrayList<String> alSIUnitInventory;
    List<String> productlist;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        IHeader = (ListView) findViewById(R.id.lvIHeader);
        IDetails = (ListView) findViewById(R.id.lvIDetail);
        Imenu = (BottomNavigationView) findViewById(R.id.btINavigation);

        if (controller.PINum == 0){
            controller.IViewInventory = controller.fetchInventory();
        }

        ViewHeaderListview();
        ViewDetailListview();

        IDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laIDetails.getItem(position);

                String objItem = (String) obj.get("Item");
                Pproducts = objItem;
                String objQty = (String) obj.get("Qty");
                Pqty = objQty;
                String objUnit = (String) obj.get("Unit");
                Punit = objUnit;
                Pposition = position;

                DialogEditItem();

            }
        });



        Imenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mi_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                DialogAddItem();

                                break;
                            case R.id.mi_done:



                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                new AlertDialog.Builder(InventoryActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Inventory")
                                        .setMessage("Are you done checking the inventory?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                Toasty.success(getApplicationContext(), "transaction successful", Toast.LENGTH_LONG).show();

                                                controller.PINum = 1;
                                                Intent intentConfirmTransactionActivity = new Intent(InventoryActivity.this, ConfirmTransactionActivity.class);
                                                startActivity(intentConfirmTransactionActivity);
                                                finish();

                                            }

                                        })
                                        .setNegativeButton("No", null)
                                        .show();



                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmIHeader = new ArrayList<HashMap<String, String>>();
        mIHeader = new HashMap<String, String>();


        mIHeader.put("Item", "ITEM");
        mIHeader.put("Qty", "QTY");
        mIHeader.put("Unit", "UNIT");
        hmIHeader.add(mIHeader);

        try {
            laIHeader = new SimpleAdapter(this, hmIHeader,  R.layout.item_inventory,
                    new String[]{"Item", "Qty", "Unit"}, new int[]{
                    R.id.rowsItem, R.id.rowsQty, R.id.rowsUnit}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView rqty = (TextView) view.findViewById(R.id.rowsQty);
                    TextView runit = (TextView) view.findViewById(R.id.rowsUnit);
                    if (position % 2 == 0) {
                        ritem.setTextColor(Color.WHITE);
                        rqty.setTextColor(Color.WHITE);
                        runit.setTextColor(Color.WHITE);
                        ritem.setTypeface(null, Typeface.BOLD);
                        rqty.setTypeface(null, Typeface.BOLD);
                        runit.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            IHeader.setAdapter(laIHeader);
        } catch (Exception e) {

        }
    }


    public void ViewDetailListview() {


        try {
            laIDetails = new SimpleAdapter(this, controller.IViewInventory, R.layout.item_inventory,
                    new String[]{"Item", "Qty", "Unit"}, new int[]{
                    R.id.rowsItem, R.id.rowsQty, R.id.rowsUnit}) {
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
            IDetails.setAdapter(laIDetails);
        } catch (Exception e) {

        }

    }

    public void UpdateDetailListview(){

        mIDetail = new HashMap<String, String>();

        mIDetail.put("Item", Pproducts);
        mIDetail.put("Qty", Pqty);
        mIDetail.put("Unit", Punit);
        controller.IViewInventory.set(Pposition,mIDetail);
        laIDetails.notifyDataSetChanged();

//
//        try {
//            laIDetails = new SimpleAdapter(this, controller.IViewInventory,R.layout.item_inventory,
//                    new String[]{"Item", "Qty", "Unit"}, new int[]{
//                    R.id.rowsItem, R.id.rowsQty, R.id.rowsUnit}) {
//                @Override
//                public View getView(int position, View convertView, ViewGroup parent) {
//                    View view = super.getView(position, convertView, parent);
//                    if (position % 2 == 1) {
//                        view.setBackgroundColor(Color.parseColor("#fff2dc"));
//                    } else {
//                        view.setBackgroundColor(Color.parseColor("#f7e1a8"));
//                    }
//                    return view;
//
//                }
//            };
//            IDetails.setAdapter(laIDetails);
//            IDetails.setSelection(Pposition);
//
//        } catch (Exception e) {
//
//        }
    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InventoryActivity.this);
        LayoutInflater inflater = InventoryActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edititeminventory, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        btnIAdd = (Button) dialogView.findViewById(R.id.btnDIAdd);
        btnISub = (Button) dialogView.findViewById(R.id.btnDISub);
        tvIItem = (TextView) dialogView.findViewById(R.id.tvDIItem);
        tvIUnit = (TextView) dialogView.findViewById(R.id.tvDIUnitItem);
        tvICancel = (TextView) dialogView.findViewById(R.id.tvDICancel);
        tvISave = (TextView) dialogView.findViewById(R.id.tvDISave);
        final EditText qty = (EditText) dialogView.findViewById(R.id.etDIQty);

        tvIItem.setText(Pproducts);
        if (Pqty.equals("0")){
            qty.setText("");
        }else{
            qty.setText(Pqty);
        }

        qty.requestFocus();

        qty.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                /*if (!changing && qty.getText().toString().startsWith("0")){
                    changing = true;
                    qty.setText(qty.getText().toString().replace("0", ""));
                }
                changing = false;*/
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (qty.length() > 1){
                    if (qty.getText().toString().substring(0,1).equals("0")){
                        qty.setText(qty.getText().toString().substring(1));
                        qty.setSelection(qty.getText().length());
                    }
                }
            }

        });


        tvIUnit.setText(Punit);

        btnIAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else{
                    intIQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intIQty.toString());
                }


            }
        });

        btnISub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0) {
                    intIQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intIQty.toString());
                }

            }
        });

        tvICancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                alertDialog.dismiss();

            }
        });

        tvISave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")) {
                    messagebox("quantity must not be blank");
                } else {

                    Pqty = qty.getText().toString();

                    UpdateDetailListview();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                    Toasty.success(getApplicationContext(), Pproducts + " successfully updated", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog.show();
    }

    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InventoryActivity.this);
        LayoutInflater inflater = InventoryActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additeminventory, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        tvDEIOk = (TextView) dialogView.findViewById(R.id.tvDEIOk);
        tvDEINext = (TextView) dialogView.findViewById(R.id.tvDEINext);
        btnDEIAdd = (Button) dialogView.findViewById(R.id.btnDEIAdd);
        btnDEISub = (Button) dialogView.findViewById(R.id.btnDEISub);
        btnDEIScan = (Button) dialogView.findViewById(R.id.btnDEIScan);
        product = (AutoCompleteTextView) dialogView.findViewById(R.id.acDEIProduct);
        qty = (EditText) dialogView.findViewById(R.id.etDEIQty);
        final ImageView ivDSRList = (ImageView) dialogView.findViewById(R.id.ivDEIList);


        final String products[] = controller.fetchMNameInventory();

        productlist = Arrays.asList(products);

        final ArrayAdapter<String> aaAProducts = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, products);

        product.setAdapter(aaAProducts);
        product.setThreshold(1);
        product.requestFocus();

        qty.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                /*if (!changing && qty.getText().toString().startsWith("0")){
                    changing = true;
                    qty.setText(qty.getText().toString().replace("0", ""));
                }
                changing = false;*/
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (qty.length() > 1){
                    if (qty.getText().toString().substring(0,1).equals("0")){
                        qty.setText(qty.getText().toString().substring(1));
                        qty.setSelection(qty.getText().length());
                    }
                }
            }

        });

        /*product.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                btnDEIScan.setVisibility(View.VISIBLE);
                qty.requestFocus();

            }
        });

        if(qty.requestFocus()) {
            btnDEIScan.setVisibility(View.VISIBLE);
        }

        product.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (product.getText().toString().length() == 0){
                    btnDEIScan.setVisibility(View.VISIBLE);
                }else{
                    btnDEIScan.setVisibility(View.GONE);
                }
            }

        });*/

        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                qty.requestFocus();

            }
        });

        tvDEIOk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (product.getText().toString().equals("") && qty.getText().toString().equals("")) {
                    alertDialog.dismiss();
                }else if (product.getText().toString().equals("")) {
                    //messagebox("please select product");
                    alertDialog.dismiss();
                } else if (!productlist.contains(product.getText().toString())) {
                    messagebox("product not in list");
                } else if (qty.getText().toString().equals("")) {
                    //messagebox("invalid quantity");
                    alertDialog.dismiss();
                } else if (Integer.parseInt(qty.getText().toString()) == 0) {
                    //messagebox("quantity must not be zero(0)");
                    alertDialog.dismiss();
                } else {

                    for (int i = 0; i < IDetails.getCount(); i++) {
                    HashMap<String, Object> obj = (HashMap<String, Object>) laIDetails.getItem(i);
                    String objProduct = (String) obj.get("Item");
                    if (objProduct.equals(product.getText().toString())){

                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    Pqty = qty.getText().toString();
                    alSIUnitInventory = controller.fetchUnitQtyPriceMaterialInventory();
                    Punit = alSIUnitInventory.get(0);
                    Pposition = i;

                    UpdateDetailListview();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                    Toasty.info(getApplicationContext(), Pproducts + " successfully updated", Toast.LENGTH_LONG).show();

                    alertDialog.dismiss();
                        }
                    }
                }
            }
        });

       tvDEINext.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (product.getText().toString().equals("") && qty.getText().toString().equals("")) {
                    alertDialog.dismiss();
                }else if (product.getText().toString().equals("")) {
                    messagebox("please select product");
                }else if (!productlist.contains(product.getText().toString())){
                    messagebox("product not in list");
                }else if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else {

                }
                    for (int i = 0; i < IDetails.getCount(); i++) {
                        HashMap<String, Object> obj = (HashMap<String, Object>) laIDetails.getItem(i);
                        String objProduct = (String) obj.get("Item");
                        if (objProduct.equals(product.getText().toString())) {

                            controller.PMName = product.getText().toString();
                            Pproducts = controller.PMName;
                            Pqty = qty.getText().toString();
                            alSIUnitInventory = controller.fetchUnitQtyPriceMaterialInventory();
                            Punit = alSIUnitInventory.get(0);
                            Pposition = i;

                            UpdateDetailListview();

                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                            Toasty.info(getApplicationContext(), Pproducts + " successfully updated", Toast.LENGTH_LONG).show();

                            product.setText("");
                            qty.setText("");

                        }
                    }
            }
        });

        btnDEIAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else{
                    intIQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intIQty.toString());
                }
            }
        });

        btnDEISub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (intIQty != 0){
                    intIQty = Integer.parseInt(qty.getText().toString()) - 1;

                    qty.setText(intIQty.toString());
                }
            }
        });

        ivDSRList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                product.showDropDown();
                //btnDEIScan.setVisibility(View.GONE);
            }
        });

        btnDEIScan.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                new IntentIntegrator(InventoryActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });


        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toasty.error(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            } else {
                showResultDialogue(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showResultDialogue(final String result) {

        controller.PMatName = result;
        if (!productlist.contains(controller.FetchNameMaterials().get(0))){
            messagebox("material not found");
        }else{
            product.setText(controller.FetchNameMaterials().get(0));
            qty.requestFocus();
        }


    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(InventoryActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Inventory")
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

        controller.PINum = 1;

        Intent IntentValidateReturnsActivity = new Intent(InventoryActivity.this, ValidateReturnsActivity.class);
        startActivity(IntentValidateReturnsActivity);
        finish();
    }



}

