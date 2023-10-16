package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class StockReceivingActivity extends Activity {

    DBController controller = new DBController(this);
    ListView SRDetails,SRHeader;
    ArrayList<HashMap<String, String>> hmSRHeader,hmSRDetails;
    ListAdapter laSRHeader,laSRDetails;
    HashMap<String, String> mSRHeader,mSRDetail;
    BottomNavigationView menu;
    AutoCompleteTextView product;
    EditText qty;
    Button btnSRAdd,btnSRSub,btnSRScan;
    Integer intSRQty = 0;
    String Pproducts,Pqty,Punit;
    Integer Pposition;
    Double intSRTPrice;
    TextView tvSRItem, tvSRStockItem,tvSRUnitItem,tvSRPriceItem,tvSRTotalItem,tvSRCancel,tvSRAccept,tvDSRCOk,tvDSRCNext,tvDSRCDelete,tvDSRCSave;
    ArrayList<String> alSRPAmtMaterial,alSRUser,alPrintStockReceivingH,alPrintStockReceivingD;
    List<String> SRListSettings;
    String SRSRid;
    Double dSRTotalMaterial = 0.00;
    Double dSRTotal = 0.00;
    DecimalFormat SRAmt = new DecimalFormat("#,###,##0.00");
    boolean changing = false;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;

    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;

    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    int bluetoothstatus;

    String printername;
    static final int READ_BLOCK_SIZE = 100;

    AlertDialog progressDialog;
    int notfound;

    boolean isClickedTwice;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockreceiving);

        SRHeader = (ListView) findViewById(R.id.lvSRHeader);
        SRDetails = (ListView) findViewById(R.id.lvSRDetails);
        menu = (BottomNavigationView) findViewById(R.id.btSRNavigation);

        readprintername();
        isClickedTwice = false;

        controller.PCName = "Id";
        controller.PTName = "StockReceiving";
        controller.PMNumber = controller.fetchMaxNumTCTSequence();

        SRListSettings = controller.fetchdbSettings();

        DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
        String date = df.format(Calendar.getInstance().getTime());

        SRSRid = date + SRListSettings.get(1) + controller.PMNumber;

        ViewHeaderListview();

        SRDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }

                HashMap<String, Object> obj = (HashMap<String, Object>) laSRDetails.getItem(position);

                String objProducts = (String) obj.get("Item");
                Pproducts = objProducts;
                String objQty = (String) obj.get("Qty");
                Pqty = objQty ;
                String objUnit = (String) obj.get("Unit");
                Punit= objUnit;
                Pposition = position;

                DialogEditItem();

            }
        });

        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.msr_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }

                                DialogAddItem();


                                break;

                            case R.id.msr_done:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }

                                if (SRDetails.getCount() == 0){
                                    messagebox("nothing to receive");
                                }else{
                                    DialogStockReceivingConfirmation();
                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmSRHeader = new ArrayList<HashMap<String, String>>();
        mSRHeader = new HashMap<String, String>();
        hmSRDetails = new ArrayList<HashMap<String, String>>();

        mSRHeader.put("Item", "ITEM");
        mSRHeader.put("Qty", "QUANTITY");
        mSRHeader.put("Unit", "UNIT");
        hmSRHeader.add(mSRHeader);

        try {
            laSRHeader = new SimpleAdapter(this, hmSRHeader, R.layout.item_stockreceiving,
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

            SRHeader.setAdapter(laSRHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        mSRDetail = new HashMap<String, String>();

        mSRDetail.put("Item", Pproducts);
        mSRDetail.put("Qty", Pqty);
        mSRDetail.put("Unit", Punit);
        hmSRDetails.add(mSRDetail);

        try {
            laSRDetails = new SimpleAdapter(this, hmSRDetails,  R.layout.item_stockreceiving,
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
            SRDetails.setAdapter(laSRDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateDetailListview(){

        mSRDetail = new HashMap<String, String>();

        mSRDetail.put("Item", Pproducts);
        mSRDetail.put("Qty", Pqty);
        mSRDetail.put("Unit", Punit);
        hmSRDetails.set(Pposition,mSRDetail);

        try {
            laSRDetails = new SimpleAdapter(this, hmSRDetails,  R.layout.item_stockreceiving,
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
            SRDetails.setAdapter(laSRDetails);
        } catch (Exception e) {

        }
    }

    public void DeleteDetailListView(){

        mSRDetail = new HashMap<String, String>();

        mSRDetail.put("Item", Pproducts);
        mSRDetail.put("Qty", Pqty);
        mSRDetail.put("Unit", Punit);
        hmSRDetails.remove(mSRDetail);

        try {
            laSRDetails = new SimpleAdapter(this, hmSRDetails,  R.layout.item_stockreceiving,
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
            SRDetails.setAdapter(laSRDetails);
        } catch (Exception e) {

        }
    }

    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StockReceivingActivity.this);
        LayoutInflater inflater = StockReceivingActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additemstockreceiving, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        tvDSRCOk = (TextView) dialogView.findViewById(R.id.tvDSRCOk);
        tvDSRCNext = (TextView) dialogView.findViewById(R.id.tvDSRCNext);
        btnSRAdd = (Button) dialogView.findViewById(R.id.btnDSRCAdd);
        btnSRSub = (Button) dialogView.findViewById(R.id.btnDSRCSub);
        btnSRScan = (Button) dialogView.findViewById(R.id.btnDSRCScan);
        product =(AutoCompleteTextView) dialogView.findViewById(R.id.acDSRCProduct);
        qty =(EditText)dialogView.findViewById(R.id.etDSRCQty);
        final ImageView ivDSRList =(ImageView)dialogView.findViewById(R.id.ivDSRCList);


        final String products[] = controller.fetchMNameMaterialsNoPromo();

        final List<String> productlist = Arrays.asList(products);

        final ArrayAdapter<String> aaAProducts = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, products);

        product.setAdapter(aaAProducts);
        product.setThreshold(1);

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

        /*product.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (product.getText().toString().length() == 0){
                    btnSRScan.setVisibility(View.VISIBLE);
                }else{
                    btnSRScan.setVisibility(View.GONE);
                }
            }

        });

        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                btnSRScan.setVisibility(View.VISIBLE);
                qty.requestFocus();
            }
        });

        if(qty.requestFocus()) {
            btnSRScan.setVisibility(View.VISIBLE);
        }*/

        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                qty.requestFocus();
            }
        });

        tvDSRCOk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (product.getText().toString().equals("") && qty.getText().toString().equals("")) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                    isClickedTwice = false;
                    alertDialog.dismiss();
                }else if (product.getText().toString().equals("")) {
                    //messagebox("please select product");
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                    isClickedTwice = false;
                    alertDialog.dismiss();
                }else if (!productlist.contains(product.getText().toString())){
                    messagebox("product not in list");
                }else if (qty.getText().toString().equals("")){
                    //messagebox("invalid quantity");
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                    isClickedTwice = false;
                    alertDialog.dismiss();
                }else if (Integer.parseInt(qty.getText().toString()) == 0){
                    //messagebox("quantity must not be zero(0)");
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                    isClickedTwice = false;
                    alertDialog.dismiss();
                }else {
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    Pqty = qty.getText().toString();

                    try {

                    alSRPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                    Punit = alSRPAmtMaterial.get(0);

                        if (SRDetails.getCount() == 0){
                            ViewDetailListview();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                            Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                            isClickedTwice = false;
                            alertDialog.dismiss();
                        }else{
                            final ArrayList<String> alADetails = new ArrayList<String>();
                            alADetails.clear();
                            for (int i = 0; i < SRDetails.getAdapter().getCount(); i++) {
                                HashMap<String, Object> obj = (HashMap<String, Object>) laSRDetails.getItem(i);

                                String objProducts = (String) obj.get("Item");
                                alADetails.add(objProducts);
                            }
                            if (alADetails.contains(Pproducts)){
                                messagebox(Pproducts + " already in the list");
                            }else{
                                ViewDetailListview();
                                isClickedTwice = false;
                                alertDialog.dismiss();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                            }
                        }

                    } catch (Exception e) {
                        Toasty.error(getApplicationContext(),"no pricelist maintained in " + Pproducts , Toast.LENGTH_LONG).show();
                        product.setText("");
                        qty.setText("");
                        product.requestFocus();
                    }

                }
            }
        });

        tvDSRCNext.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (product.getText().toString().equals("")) {
                    messagebox("please select product");
                }else if (!productlist.contains(product.getText().toString())){
                    messagebox("product not in list");
                }else if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0){
                    messagebox("quantity must not be zero(0)");
                }else {
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    Pqty = qty.getText().toString();

                    try {

                        alSRPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                        Punit = alSRPAmtMaterial.get(0);

                        if (SRDetails.getCount() == 0){
                            ViewDetailListview();
                            product.setText("");
                            qty.setText("");
                            product.requestFocus();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                            Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                        }else{
                            final ArrayList<String> alADetails = new ArrayList<String>();
                            alADetails.clear();
                            for (int i = 0; i < SRDetails.getAdapter().getCount(); i++) {
                                HashMap<String, Object> obj = (HashMap<String, Object>) laSRDetails.getItem(i);

                                String objProducts = (String) obj.get("Item");
                                alADetails.add(objProducts);
                            }
                            if (alADetails.contains(Pproducts)){
                                messagebox(Pproducts + " already in the list");
                            }else{
                                ViewDetailListview();
                                product.setText("");
                                qty.setText("");
                                product.requestFocus();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                            }
                        }

                    } catch (Exception e) {
                        Toasty.error(getApplicationContext(),"no pricelist maintained in " + Pproducts , Toast.LENGTH_LONG).show();
                        product.setText("");
                        qty.setText("");
                        product.requestFocus();
                    }


                }
            }
        });

        btnSRAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else{
                    intSRQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intSRQty.toString());
                }
            }
        });

        btnSRSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intSRQty = Integer.parseInt(qty.getText().toString()) - 1;

                    qty.setText(intSRQty.toString());
                }
            }
        });

        ivDSRList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                product.showDropDown();
                //btnSRScan.setVisibility(View.GONE);
            }
        });

        btnSRScan.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                new IntentIntegrator(StockReceivingActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });


        alertDialog.show();

    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StockReceivingActivity.this);
        LayoutInflater inflater = StockReceivingActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edititemstockreceiving, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        tvDSRCDelete = (TextView) dialogView.findViewById(R.id.tvDSRCDelete);
        tvDSRCSave = (TextView) dialogView.findViewById(R.id.tvDSRCSave);
        btnSRAdd = (Button) dialogView.findViewById(R.id.btnDSRCAdd);
        btnSRSub = (Button) dialogView.findViewById(R.id.btnDSRCSub);
        tvSRItem = (TextView) dialogView.findViewById(R.id.tvDSRCItem);
        tvSRStockItem = (TextView) dialogView.findViewById(R.id.tvDSRCStockItem);
        tvSRUnitItem = (TextView) dialogView.findViewById(R.id.tvDSRCUnitItem);
        tvSRPriceItem = (TextView) dialogView.findViewById(R.id.tvDSRCPriceItem);
        tvSRTotalItem = (TextView) dialogView.findViewById(R.id.tvDSRCTotalItem);
        final EditText qty =(EditText)dialogView.findViewById(R.id.etDSRCQty);

        tvSRItem.setText(Pproducts);
        tvSRUnitItem.setText(Punit);
        qty.setText(Pqty);

        qty.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
               /* if (!changing && qty.getText().toString().startsWith("0")){
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

        controller.PMName = tvSRItem.getText().toString();
        alSRPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();

        tvSRStockItem.setText(alSRPAmtMaterial.get(1));
        tvSRPriceItem.setText(alSRPAmtMaterial.get(2));

        intSRTPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvSRPriceItem.getText().toString().replace(",",""));
        tvSRTotalItem.setText(String.valueOf(SRAmt.format(intSRTPrice)));

        btnSRAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else {
                    intSRQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intSRQty.toString());
                    intSRTPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvSRPriceItem.getText().toString().replace(",",""));
                    tvSRTotalItem.setText(String.valueOf(SRAmt.format(intSRTPrice)));
                }

            }
        });

        btnSRSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (intSRQty != 0){
                    intSRQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intSRQty.toString());
                    intSRTPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvSRPriceItem.getText().toString().replace(",",""));
                    tvSRTotalItem.setText(String.valueOf(SRAmt.format(intSRTPrice)));
                }

            }
        });

        tvDSRCDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {


                Toasty.error(getApplicationContext(),  Pproducts + " successfully deleted", Toast.LENGTH_LONG).show();
                DeleteDetailListView();

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                alertDialog.dismiss();

            }
        });

        tvDSRCSave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0) {
                    messagebox("quantity must not be zero(0)");
                }else{
                    Pproducts = tvSRItem.getText().toString();
                    Pqty = qty.getText().toString();
                    Punit = tvSRUnitItem.getText().toString();

                    UpdateDetailListview();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                    Toasty.info(getApplicationContext(),  Pproducts + " successfully updated", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
            }
        });

        alertDialog.show();

    }

    public void DialogStockReceivingConfirmation() {


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StockReceivingActivity.this);
        LayoutInflater inflater = StockReceivingActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_stockreceivingconfirmation, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        final EditText etUsername =(EditText)dialogView.findViewById(R.id.etDSRUsername);
        final EditText etPassword =(EditText)dialogView.findViewById(R.id.etDSRPassword);
        tvSRCancel = (TextView) dialogView.findViewById(R.id.tvDSRCancel);
        tvSRAccept = (TextView) dialogView.findViewById(R.id.tvDSRAccept);

        tvSRCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvSRAccept.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if(etUsername.getText().toString().equals("")){
                    etUsername.setError("please input username");
                }else if (etPassword.getText().toString().equals("")){
                    etPassword.setError("please input password");
                }else{
                    controller.PCHKName = etUsername.getText().toString();
                    controller.PCHKPassword = etPassword.getText().toString();
                    alSRUser = controller.fetchChkIDUser();
                    if (alSRUser.get(0).equals("0")){
                        etUsername.setError("invalid username/password");
                    }else if (!alSRUser.get(1).equals("CHE")){
                        etUsername.setError("please enter checker account");
                    }else{

                        for (int i = 0; i < SRDetails.getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laSRDetails.getItem(i);
                            String objProduct = (String) obj.get("Item");
                            controller.PMName = objProduct;
                            String objQty = (String) obj.get("Qty");



                            int iSRINum = i + 1;

                            if (controller.fetchCountMaterialInventory() == 0) {
                                controller.PICName = "InvId";
                                controller.PINVNumber = controller.fetchMaxNumInvtTSequence();

                                controller.insertInventory(controller.fetchMCodeMaterials(), SRListSettings.get(6), controller.PINVNumber, objQty);

                                controller.updateInvtTableSequence(Integer.valueOf(controller.PINVNumber) + 1);

                                controller.insertStockReceivingD(SRSRid, String.valueOf(iSRINum), controller.fetchMCodeMaterials(), objQty);


                            } else {

                                controller.updateReceivedInventory(Integer.valueOf(objQty));

                                controller.insertStockReceivingD(SRSRid, String.valueOf(iSRINum), controller.fetchMCodeMaterials(), objQty);

                            }

                            dSRTotalMaterial = Integer.valueOf(objQty) * controller.fetchAmtPricingList();

                            dSRTotal = dSRTotal + dSRTotalMaterial;

                        }

                        Timestamp tsSRDateTime = new Timestamp(System.currentTimeMillis());
                        controller.insertStockReceivingH(SRSRid,controller.PMNumber,tsSRDateTime.getTime(),SRListSettings.get(6),dSRTotal,alSRUser.get(0),controller.PUiD);

                        controller.insertTransaction("SR"+SRSRid,"SR",SRListSettings.get(6),SRListSettings.get(6),tsSRDateTime.getTime(),dSRTotal);


                controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);

                        /*try{
                            controller.export();
                            scanFile(controller.backupDB.getAbsolutePath());
                            //scanMedia(controller.backupDB);
                        }catch (Exception e){
                            controller.exports = "/storage/emulated/0/Documents/Exports";
                            controller.export();
                            //scanMedia(controller.backupDB);
                            scanFile(controller.backupDB.getAbsolutePath());
                        }*/

                        File exportpath = new File(controller.exports);
                        if (exportpath.exists()){
                            controller.export();
                            scanFile(controller.backupDB.getAbsolutePath());
                        }else{
                            String exportfile;
                            exportfile = "/storage/emulated/0/Documents/Exports";
                            File export = new File(exportfile);
                            if (export.exists()){
                                controller.export(exportfile);
                                scanFile(controller.backupDB.getAbsolutePath());
                            }else{
                                export.mkdir();
                                scanFile(export.getAbsolutePath());
                                controller.export(exportfile);
                                scanFile(controller.backupDB.getAbsolutePath());
                            }

                        }

                Toasty.success(getApplicationContext(),"database backup successfully",Toast.LENGTH_LONG).show();

                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etUsername.getWindowToken(), 0);

                alertDialog.dismiss();


                printdialog();

            }
        }


            }
        });


        alertDialog.show();



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //We will get scan results here
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        //check for null
        if (result != null) {
            if (result.getContents() == null) {
                Toasty.error(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
            } else {
                //show dialogue with result
                showResultDialogue(result.getContents());
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showResultDialogue(final String result) {

        controller.PMatName = result;

        if(controller.FetchNameMaterials().get(0).equals("")){
            messagebox("material not found");
        }else{
            product.setText(controller.FetchNameMaterials().get(0));
            qty.requestFocus();
        }


    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(StockReceivingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Stock Receiving")
                .setMessage(alerttext)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }

                })
                .show();
    }

    void findBT() {

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if(mBluetoothAdapter == null) {
                //myLabel.setText("No bluetooth adapter available");
                bluetoothstatus = 4;
            }

            if(!mBluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

            if(pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {

                    // RPP300 is the name of the bluetooth printer device
                    // we got this name from the list of paired devices
                    if (device.getName().equals(printername)) {
                        mmDevice = device;
                        break;
                    }
                }
            }

            //myLabel.setText("Bluetooth device found.");
            bluetoothstatus = 3;

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void openBT() {
        try {

            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            //myLabel.setText("Bluetooth Opened");

            bluetoothstatus = 1;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                //myLabel.setText(data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendData() {
        try {

            byte[] printformat = new byte[]{0x1B,0x21,0x03};
            mmOutputStream.write(printformat);

            String companyName = controller.fetchdbSettings().get(19);
            String title = "STOCK RECEIVING RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;

            printCustom(companyName,3);
            printCustom(title,3);
            printNewLine();

            alPrintStockReceivingH = controller.printStockReceivingH(SRSRid);

            //printText(leftMidRightAlign("Stock Receiving To: " + alPrintStockReceivingH.get(1),"         " + alPrintStockReceivingH.get(0), ""));
            printText(WithDate("Stock Receiving To: " + alPrintStockReceivingH.get(1),alPrintStockReceivingH.get(0)));
            printNewLine();
            printCustom("Terminal: " + SRListSettings.get(1),0);
            printCustom("Reference ID: " + SRSRid,0);

            printNewLine();

            int countqty = 0;

            printText(getLine(57));
            printNewLine();



            for(int i = 1; i <= controller.fetchCountItem(SRSRid,"StockReceivingItem","StockReceivingId"); i++) {


                alPrintStockReceivingD = controller.printStockReceivingD(SRSRid,i);

                String item;
                item = alPrintStockReceivingD.get(1);
                if (item.length() > 35){
                    item = item.substring(0,35);
                }


                printText(PrintDetails(alPrintStockReceivingD.get(0),item, alPrintStockReceivingD.get(2), alPrintStockReceivingD.get(3)));


                //printText(PrintDetails(controller.alSTItems.get(i), controller.alSTUnit.get(i), controller.alSTQty.get(i)));
                printNewLine();
                countqty = countqty + Integer.valueOf(alPrintStockReceivingD.get(3));


                //Toasty.success(getApplicationContext(), String.valueOf(alPrintStockTransferD.size()), Toast.LENGTH_LONG).show();

            }

            printText(getLine(57));
            printNewLine();

            printText(PrintDetails("TOTAL","","",String.valueOf(countqty)));

            printNewLine();
            printNewLine();

            printCustom("Issued by: " + alPrintStockReceivingH.get(2),0);
            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String WithDate(String str1, String str2){
        int strlength = 28;
        int strlength1 = 29;

        int str1length = str1.length();
        int anslength  = strlength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength1 - str2length;

        String ans = str1 + getSpace(anslength) + getSpace(anslength2) + str2;

        return ans;
    }

    private String PrintDetails(String str0,String str1, String str2,String str3) {
        int fulllength = 42;
        int strlength = 5;

        int str0length = str0.length();
        int anslength0 = strlength - str0length;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength - str3length;

        String ans = str0 + getSpace(anslength0) + str1 + getSpace(anslength) + str2  + getSpace(anslength2) +  getSpace(anslength3) +  str3;
        return ans;
    }

    public String getSpace(int count)
    {
        String space="";
        for(int i=0;i<count;i++)
            space+=" ";
        return space;
    }
    public String getLine(int count)
    {
        String space="";
        for(int i=0;i<count;i++)
            space+="-";
        return space;
    }


    void closeBT() {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            //myLabel.setText("Bluetooth Closed");

            bluetoothstatus = 2;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //print custom
    private void printCustom(String msg, int size) {
        //Print config "mode"
        byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
        //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
        byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
        byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
        byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
        try {
            switch (size){
                case 0:
                    mmOutputStream.write(cc);
                    break;
                case 1:
                    mmOutputStream.write(bb);
                    break;
                case 2:
                    mmOutputStream.write(bb2);
                    break;
                case 3:
                    mmOutputStream.write(bb3);
                    break;
            }
            mmOutputStream.write(msg.getBytes());
            mmOutputStream.write(PrinterCommands.LF);
            //outputStream.write(cc);
            //printNewLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printNewLine() {
        try {
            mmOutputStream.write(PrinterCommands.FEED_LINE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printText(String msg) {
        try {
            // Print normal text
            mmOutputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String leftMidRightAlign(String str1, String str2 , String str3) {

        if (str2.length() == 2){
            str2 += new String(" ");
        }

        String ans = str1 +str2;
        if(ans.length() <38){
            int n = (38 - str1.length() + str2.length());
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2 + "         " + str3;
        }
        return ans;
    }

    private String getDateTime() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm a");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }

    void printdialog(){

        new AlertDialog.Builder(StockReceivingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Print Receipt")
                .setCancelable(false)
                .setMessage("Do you want to print receipt?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new Task().execute();

                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        returnactivity();
                        closeBT();

                    }
                })
                .show();

    }

    void reprintdialog(){

        new AlertDialog.Builder(StockReceivingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setTitle("Print Receipt")
                .setMessage("Do you want to reprint receipt?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        new Task().execute();

                    }

                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        returnactivity();
                        closeBT();

                    }
                })
                .show();

    }

    public void ProgressDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StockReceivingActivity.this);
        LayoutInflater inflater = StockReceivingActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        dialogBuilder.setView(dialogView);
        progressDialog = dialogBuilder.create();

        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

    }

    class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            //progressBar.setVisibility(View.VISIBLE);
            ProgressDialog();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
           // progressBar.setVisibility(View.GONE);
            progressDialog.dismiss();
            if (notfound == 1){
                reprintdialog();
            }else{
                Toasty.error(getApplicationContext(),"bluetooth device not found" , Toast.LENGTH_LONG).show();
                reprintdialog();
            }

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            findBT();

            openBT();
            if (bluetoothstatus == 1){
                //progress();
                sendData();
                closeBT();
                notfound = 1;
            }else{
                notfound = 2;
                //messagebox("bluetooth printer not found");
            }

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    void readprintername(){

        try {
            FileInputStream fileIn=openFileInput("printer.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            printername = s ;


        } catch (Exception e) {
            printername = "";
        }


    }

    void returnactivity(){

        Intent IntentInventoryListActivity = new Intent(StockReceivingActivity.this, InventoryListActivity.class);
        startActivity(IntentInventoryListActivity);
        finish();

    }

    private void scanMedia(File path) {
        Uri uri = Uri.fromFile(path);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(StockReceivingActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }


    public void onBackPressed() {

        returnactivity();

    }

}




