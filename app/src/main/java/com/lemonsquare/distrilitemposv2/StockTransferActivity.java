package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class StockTransferActivity extends Activity {

    DBController controller = new DBController(this);
    ListView STDetails,STHeader;
    ArrayList<HashMap<String, String>> hmSTHeader,hmSTDetails;
    ListAdapter laSTHeader,laSTDetails;
    HashMap<String, String> mSTHeader,mSTDetail;
    BottomNavigationView menu;
    Button btnSTAdd,btnSTSub,btnSTScan,btnDSTScan;
    AutoCompleteTextView product;
    Integer intSTQty = 0;
    String Pproducts,Pqty,Punit;
    Integer Pposition,Pscan;
    Double intSTTPrice;
    TextView tvSTItem, tvSTStockItem,tvSTUnitItem,tvSTPriceItem,tvSTTotalItem,tvSTOk,tvSTNext,tvSTDelete,tvSTSave;
    ArrayList<String> alSTPAmtMaterial,alPrintStockTransferH,alPrintStockTransferD;
    List<String> STListSettings;
    EditText STTSloc,qty;
    DecimalFormat STAmt = new DecimalFormat("#,###,##0.00");
    boolean changing = false;

    private long mLastClickTime = 0;

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


    static int HEAD_WIDTH = 576;
    static int COLOR_THRESHOLD = 128;

    private final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte) 0x1B, (byte) 0x56};
    private final byte[] SET_LINE_SPACING = new byte[]{0x1B, 0x33};

    String transferQR;
    String transferItems;

    boolean isClickedTwice;

    //1-OPEN   2-CLOSE   3-FOUND   4-NOT FOUND

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stocktransfer);

        STHeader = (ListView) findViewById(R.id.lvSTHeader);
        STDetails = (ListView) findViewById(R.id.lvSTDetails);
        STTSloc = (EditText) findViewById(R.id.etSTTSLoc);
        menu = (BottomNavigationView) findViewById(R.id.btSTNavigation);
        btnSTScan = (Button) findViewById(R.id.btnSTScan);

        readprintername();

        isClickedTwice = false;

        controller.PINum = 5;

        STListSettings = controller.fetchdbSettings();

        ViewHeaderListview();



        if (controller.PSTSLocNum == 1){

            STTSloc.setText(controller.PSTSLoc);

            for(int i = 0; i < controller.alSTItems.size(); i++) {

                Pproducts = controller.alSTItems.get(i);
                Pqty = controller.alSTQty.get(i);
                Punit = controller.alSTUnit.get(i);
                ViewDetailListview();

            }

        }else if (controller.PSTSLocNum == 2){

            transferQR = "";
            transferItems = "";

            alPrintStockTransferH = controller.printStockTransferH(controller.PSTiD);

            STTSloc.setText(controller.PSTSLoc);

            for(int i = 0; i < controller.alSTItems.size(); i++) {

                Pproducts = controller.alSTItems.get(i);
                Pqty = controller.alSTQty.get(i);
                Punit = controller.alSTUnit.get(i);
                ViewDetailListview();

                controller.PMName = controller.alSTItems.get(i);
                transferItems = transferItems + controller.fetchMCodeMaterials() + "," + controller.alSTQty.get(i) + "/";

            }

            transferQR = controller.PSTiD + "<" + controller.fetchdbSettings().get(6) + "." +  controller.PSTSLoc + ">" + transferItems;

            byte[] encodeValue = Base64.encode(new StringBuffer(transferQR).reverse().toString().getBytes(), Base64.DEFAULT);
            transferQR = new String(encodeValue).trim();

            Intent intent = new Intent(StockTransferActivity.this, StockTransferQRActivity.class);
            intent.putExtra("qr", transferQR);
            startActivity(intent);

           printdialog();

        }

        STDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laSTDetails.getItem(position);

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

        btnSTScan.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                Pscan = 1;
                new IntentIntegrator(StockTransferActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });

        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mst_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                DialogAddItem();


                                break;

                            case R.id.mst_done:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if (STTSloc.getText().toString().equals("")){
                                    messagebox("please scan TSLOC");
                                }else {

                                    controller.PSTSLoc = STTSloc.getText().toString();

                                    if (STDetails.getCount() == 0) {
                                        messagebox("nothing to transfer");
                                    } else {

                                        new AlertDialog.Builder(StockTransferActivity.this)
                                                .setIcon(android.R.drawable.ic_dialog_alert)
                                                .setTitle("Stock Transfer")
                                                .setMessage("Are you sure you want to transfer the items below?")
                                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        controller.alSTItems.clear();
                                                        controller.alSTQty.clear();
                                                        controller.alSTUnit.clear();

                                                        for (int i = 0; i < STDetails.getCount(); i++) {
                                                            HashMap<String, Object> obj = (HashMap<String, Object>) laSTDetails.getItem(i);
                                                            String objProduct = (String) obj.get("Item");
                                                            String objQty = (String) obj.get("Qty");
                                                            String objUnit = (String) obj.get("Unit");


                                                            controller.alSTItems.add(objProduct);
                                                            controller.alSTQty.add(objQty);
                                                            controller.alSTUnit.add(objUnit);

                                                        }


                                                        controller.PSTSLocNum = 1;

                                                        //controller.PSTiD = STSTid;

                                                        Intent IntentConfirmTransactionActivity = new Intent(StockTransferActivity.this, ConfirmTransactionActivity.class);
                                                        startActivity(IntentConfirmTransactionActivity);
                                                        finish();

                                                    }

                                                })
                                                .setNegativeButton("No", null)
                                                .show();

                                    }
                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmSTHeader = new ArrayList<HashMap<String, String>>();
        mSTHeader = new HashMap<String, String>();
        hmSTDetails = new ArrayList<HashMap<String, String>>();

        mSTHeader.put("Item", "ITEM");
        mSTHeader.put("Qty", "QUANTITY");
        mSTHeader.put("Unit", "UNIT");
        hmSTHeader.add(mSTHeader);

        try {
            laSTHeader = new SimpleAdapter(this, hmSTHeader, R.layout.item_stocktransfer,
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

            STHeader.setAdapter(laSTHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        mSTDetail = new HashMap<String, String>();

        mSTDetail.put("Item", Pproducts);
        mSTDetail.put("Qty", Pqty);
        mSTDetail.put("Unit", Punit);
        hmSTDetails.add(mSTDetail);

        try {
            laSTDetails = new SimpleAdapter(this, hmSTDetails,  R.layout.item_stocktransfer,
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
            STDetails.setAdapter(laSTDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateDetailListview(){

        mSTDetail = new HashMap<String, String>();

        mSTDetail.put("Item", Pproducts);
        mSTDetail.put("Qty", Pqty);
        mSTDetail.put("Unit", Punit);
        hmSTDetails.set(Pposition,mSTDetail);

        try {
            laSTDetails = new SimpleAdapter(this, hmSTDetails,  R.layout.item_stocktransfer,
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
            STDetails.setAdapter(laSTDetails);
        } catch (Exception e) {

        }
    }

    public void DeleteDetailListView(){

        mSTDetail = new HashMap<String, String>();

        mSTDetail.put("Item", Pproducts);
        mSTDetail.put("Qty", Pqty);
        mSTDetail.put("Unit", Punit);
        hmSTDetails.remove(mSTDetail);

        try {
            laSTDetails = new SimpleAdapter(this, hmSTDetails,  R.layout.item_stocktransfer,
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
            STDetails.setAdapter(laSTDetails);
        } catch (Exception e) {

        }
    }

    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StockTransferActivity.this);
        LayoutInflater inflater = StockTransferActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additemstocktransfer, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        tvSTOk = (TextView) dialogView.findViewById(R.id.tvDSTOk);
        tvSTNext = (TextView) dialogView.findViewById(R.id.tvDSTNext);
        btnSTAdd = (Button) dialogView.findViewById(R.id.btnDSTAdd);
        btnSTSub = (Button) dialogView.findViewById(R.id.btnDSTSub);
        btnDSTScan = (Button) dialogView.findViewById(R.id.btnDSTScan);
        product =(AutoCompleteTextView) dialogView.findViewById(R.id.acDSTProduct);
        qty =(EditText)dialogView.findViewById(R.id.etDSTQty);
        final ImageView ivDSRList =(ImageView)dialogView.findViewById(R.id.ivDSTList);
        tvSTStockItem = (TextView) dialogView.findViewById(R.id.tvDSTStockItem);
        tvSTPriceItem = (TextView) dialogView.findViewById(R.id.tvDSTPriceItem);


        final String products[] = controller.fetchSTMaterials();

        final List<String> productlist = Arrays.asList(products);

        final ArrayAdapter<String> aaAProducts = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, products);

        product.setAdapter(aaAProducts);
        product.setThreshold(1);

        tvSTStockItem.setText("0");
        tvSTPriceItem.setText("0.00");

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
                String prodtoupper = s.toString();
                prodtoupper.toUpperCase();

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                /*if (product.getText().toString().length() == 0){
                    btnDSTScan.setVisibility(View.VISIBLE);
                }else{
                    btnDSTScan.setVisibility(View.GONE);
                }
            }

        });

        if(qty.requestFocus()) {
            btnDSTScan.setVisibility(View.VISIBLE);
        }*/

        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                try {
                    controller.PMName = product.getText().toString();
                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                    tvSTStockItem.setText(alSTPAmtMaterial.get(1));
                    tvSTPriceItem.setText(alSTPAmtMaterial.get(2));
                    //btnDSTScan.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                    qty.requestFocus();
                }catch (Exception e){
                    Toasty.error(getApplicationContext(),"no pricelist maintained in " + product.getText().toString() , Toast.LENGTH_LONG).show();
                }

            }
        });


        tvSTOk.setOnClickListener(new OnSingleClickListener() {
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
                }else if (Integer.parseInt(qty.getText().toString()) > Integer.parseInt(tvSTStockItem.getText().toString())){
                    messagebox("out of stock");
                }else {
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    Pqty = qty.getText().toString();

                    //try {

                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                    Punit = alSTPAmtMaterial.get(0);

                    /*} catch (Exception e) {
                        Toasty.error(getApplicationContext(),"no pricelist maintained in " + Pproducts , Toast.LENGTH_LONG).show();
                        product.setText("");
                        qty.setText("");
                        product.requestFocus();
                    }*/

                    if (STDetails.getCount() == 0){
                        ViewDetailListview();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                        Toasty.success(getApplicationContext(), Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                        isClickedTwice = false;
                        alertDialog.dismiss();
                    }else{
                        final ArrayList<String> alADetails = new ArrayList<String>();
                        alADetails.clear();
                        for (int i = 0; i < STDetails.getAdapter().getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laSTDetails.getItem(i);

                            String objProducts = (String) obj.get("Item");
                            alADetails.add(objProducts);
                        }
                        if (alADetails.contains(Pproducts)){
                            messagebox(Pproducts + " already in the list");
                        }else{
                            ViewDetailListview();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                            isClickedTwice = false;
                            alertDialog.dismiss();
                            Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
        });

        tvSTNext.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (product.getText().toString().equals("")) {
                    messagebox("please select product");
                }else if (!productlist.contains(product.getText().toString())){
                    messagebox("product not in list");
                }else if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0){
                    messagebox("quantity must not be zero(0)");
                }else if (Integer.parseInt(qty.getText().toString()) > Integer.parseInt(tvSTStockItem.getText().toString())){
                    messagebox("out of stock");
                }else {
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    Pqty = qty.getText().toString();
                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                    Punit = alSTPAmtMaterial.get(0);

                    if (STDetails.getCount() == 0){
                        ViewDetailListview();
                        qty.setText("");
                        product.setText("");
                        tvSTStockItem.setText("0");
                        tvSTPriceItem.setText("0.00");
                        product.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                        Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                    }else{
                        final ArrayList<String> alADetails = new ArrayList<String>();
                        alADetails.clear();
                        for (int i = 0; i < STDetails.getAdapter().getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laSTDetails.getItem(i);

                            String objProducts = (String) obj.get("Item");
                            alADetails.add(objProducts);
                        }
                        if (alADetails.contains(Pproducts)){
                            messagebox(Pproducts + " already in the list");
                        }else{
                            ViewDetailListview();
                            qty.setText("");
                            product.setText("");
                            tvSTStockItem.setText("0");
                            tvSTPriceItem.setText("0.00");
                            product.requestFocus();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                            Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
        });

        btnSTAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else if (Integer.parseInt(qty.getText().toString()) == Integer.parseInt(tvSTStockItem.getText().toString())){
                    messagebox("out of stock");
                }else{
                    intSTQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intSTQty.toString());
                }

            }
        });

        btnSTSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if(Integer.parseInt(qty.getText().toString()) != 0){
                    intSTQty = Integer.parseInt(qty.getText().toString()) - 1;

                    qty.setText(intSTQty.toString());
                }
            }
        });

        ivDSRList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                product.showDropDown();
                //btnDSTScan.setVisibility(View.GONE);
            }
        });

        btnDSTScan.setOnClickListener(new OnSingleClickListener() {
                                       public void onSingleClick(View v) {
                                           Pscan = 2;
                                           new IntentIntegrator(StockTransferActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
                                       }
        });


        alertDialog.show();

    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StockTransferActivity.this);
        LayoutInflater inflater = StockTransferActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edititemstocktransfer, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        tvSTDelete = (TextView) dialogView.findViewById(R.id.tvDSTDelete);
        tvSTSave = (TextView) dialogView.findViewById(R.id.tvDSTSave);
        btnSTAdd = (Button) dialogView.findViewById(R.id.btnDSTAdd);
        btnSTSub = (Button) dialogView.findViewById(R.id.btnDSTSub);
        tvSTItem = (TextView) dialogView.findViewById(R.id.tvDSTItem);
        tvSTStockItem = (TextView) dialogView.findViewById(R.id.tvDSTStockItem);
        tvSTUnitItem = (TextView) dialogView.findViewById(R.id.tvDSTUnitItem);
        tvSTPriceItem = (TextView) dialogView.findViewById(R.id.tvDSTPriceItem);
        tvSTTotalItem = (TextView) dialogView.findViewById(R.id.tvDSTTotalItem);
        final EditText qty =(EditText)dialogView.findViewById(R.id.etDSTQty);

        tvSTItem.setText(Pproducts);
        tvSTUnitItem.setText(Punit);
        qty.setText(Pqty);

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

        controller.PMName = tvSTItem.getText().toString();
        alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();

        tvSTStockItem.setText(alSTPAmtMaterial.get(1));
        tvSTPriceItem.setText(alSTPAmtMaterial.get(2));

        intSTTPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvSTPriceItem.getText().toString().replace(",",""));
        tvSTTotalItem.setText(String.valueOf(STAmt.format(intSTTPrice)));

        btnSTAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else if (intSTQty == Integer.parseInt(tvSTStockItem.getText().toString())){
                    messagebox("out of stock");
                }else{
                    intSTQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intSTQty.toString());
                    intSTTPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvSTPriceItem.getText().toString().replace(",",""));
                    tvSTTotalItem.setText(String.valueOf(STAmt.format(intSTTPrice)));
                }

            }
        });

        btnSTSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (intSTQty != 0){
                    intSTQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intSTQty.toString());
                    intSTTPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvSTPriceItem.getText().toString().replace(",",""));
                    tvSTTotalItem.setText(String.valueOf(STAmt.format(intSTTPrice)));
                }

            }
        });

        tvSTDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                Toasty.error(getApplicationContext(),  Pproducts + " successfully deleted", Toast.LENGTH_LONG).show();
                DeleteDetailListView();
                alertDialog.dismiss();

            }
        });

        tvSTSave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.valueOf(qty.getText().toString()) >  Integer.parseInt(tvSTStockItem.getText().toString())) {
                    messagebox("out of stock");
                } else {
                    Pproducts = tvSTItem.getText().toString();
                    Pqty = qty.getText().toString();
                    Punit = tvSTUnitItem.getText().toString();

                    UpdateDetailListview();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                    Toasty.info(getApplicationContext(), Pproducts + " successfully updated", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
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
        if (Pscan == 1){
            STTSloc.setText(result);
        }else{

            controller.PMatName = result;

            if(controller.FetchNameMaterials().get(0).equals("")){
                Toast.makeText(this, "Material not found", Toast.LENGTH_LONG).show();
            }else{

                controller.PMName = controller.FetchNameMaterials().get(0);
                alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                if (alSTPAmtMaterial.get(1).equals("0")){
                    messagebox("not enough stocks");
                }else{
                    tvSTStockItem.setText(alSTPAmtMaterial.get(1));
                    tvSTPriceItem.setText(alSTPAmtMaterial.get(2));
                    product.setText(controller.PMName);
                    qty.requestFocus();
                }
            }
        }


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
            String title = "STOCK TRANSFER RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;

            printCustom(companyName,3);
            printCustom(title,3);
            printNewLine();
            //printText(leftMidRightAlign("Stock Transfer To: " + alPrintStockTransferH.get(0),"         " + alPrintStockTransferH.get(1), ""));
            //printCustom(getDateTime(),0);
            //printCustom("Stock Transfer To: " + controller.PSTSLoc,0);
            printText(WithDate("Stock Transfer To: " + alPrintStockTransferH.get(0),alPrintStockTransferH.get(1)));
            printNewLine();
            printCustom("Stock Transfer From: " + alPrintStockTransferH.get(2),0);
            printCustom("Terminal: " + STListSettings.get(1),0);
            printCustom("Reference ID: " + controller.PSTiD,0);
            printNewLine();

            int countqty = 0;

            printText(getLine(57));
            printNewLine();

            for(int i = 1; i <= controller.fetchCountItem(controller.PSTiD,"StockTransferItem","StockTransferId"); i++) {


                alPrintStockTransferD = controller.printStockTransferD(controller.PSTiD,i);

                String item;
                item = alPrintStockTransferD.get(0);
                if (item.length() > 35){
                    item = item.substring(0,35);
                }


                    printText(PrintDetails(item, alPrintStockTransferD.get(1), alPrintStockTransferD.get(2)));


                //printText(PrintDetails(controller.alSTItems.get(i), controller.alSTUnit.get(i), controller.alSTQty.get(i)));
                printNewLine();
                countqty = countqty + Integer.valueOf(alPrintStockTransferD.get(2));


                //Toasty.success(getApplicationContext(), String.valueOf(alPrintStockTransferD.size()), Toast.LENGTH_LONG).show();

            }

            printText(getLine(57));
            printNewLine();

            printText(PrintDetails("TOTAL","",String.valueOf(countqty)));

            printNewLine();
            printNewLine();



            printCustom("Salesman: " + alPrintStockTransferH.get(3),0);
            printNewLine();
            printNewLine();

            printCustom("SIGNATURE",0);

            printNewLine();

            Bitmap bmp = BitmapFactory.decodeByteArray(controller.fetchSignature(controller.PSTiD), 0, controller.fetchSignature(controller.PSTiD).length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmp, 250, 90, false);

            printImage(resizedbmp);

            printCustom("Signed by: " + alPrintStockTransferH.get(4),0);

            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printImage(Bitmap image) throws IOException {

        Bitmap bmp = image;
        if (image.getWidth() > HEAD_WIDTH) {
            bmp = scaleToWidth(image, HEAD_WIDTH);
        }
        BitSet bitsImageData = toPrinterGraphic(bmp);

        byte[] setLineSpacing24Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 24);
        byte[] setLineSpacing30Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 30);

        mmOutputStream.write(setLineSpacing24Dots);

        byte heightLSB = (byte) (bmp.getHeight() & 0xFF);
        byte heightMSB = (byte) ((bmp.getHeight() & 0xFF00) >> 8);

        mmOutputStream.write(buildPOSCommand(SELECT_BIT_IMAGE_MODE, heightMSB, heightLSB));
        int offset = 0;
        while (offset < bmp.getHeight()) {
            for (int x = 0; x < HEAD_WIDTH; ) {
                byte slice = 0;
                for (int b = 0; b < 8; ++b) {
                    int i = (offset * HEAD_WIDTH) + x + b;
                    boolean v = false;
                    if (i < bitsImageData.length()) {
                        v = bitsImageData.get(i);
                    }
                    slice |= (byte) ((v ? 1 : 0) << (7 - b));
                }

                mmOutputStream.write(slice);
                x += 8;
            }
            offset++;
        }
        mmOutputStream.write(setLineSpacing30Dots);
        printNewLine();
    }

    private static BitSet toPrinterGraphic(Bitmap source) {
        BitSet printerGraphic = new BitSet(source.getWidth() * HEAD_WIDTH);
        try {
            int k = 0;
            for (int x = 0; x < source.getHeight(); x++) {
                for (int y = 0; y < HEAD_WIDTH; y++) {
                    if (y < source.getWidth()) {
                        int pixel = source.getPixel(y, x);
                        int red = Color.red(pixel);
                        int green = Color.green(pixel);
                        int blue = Color.blue(pixel);
                        int alpha = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                        printerGraphic.set(k, alpha < COLOR_THRESHOLD);
                    } else {
                        printerGraphic.set(k, false);
                    }
                    k++;
                }
            }
        } catch (Exception e) {
            //Log.e(TAG, e.toString());
        }

        return printerGraphic;
    }

    private static Bitmap scaleToWidth(Bitmap src, int width) {
        float aspectRatio = src.getWidth() / (float) src.getHeight();
        int height = Math.round(width / aspectRatio);
        return Bitmap.createScaledBitmap(src, width, height, false);
    }

    private static byte[] buildPOSCommand(byte[] command, byte... args) {
        byte[] posCommand = new byte[command.length + args.length];
        System.arraycopy(command, 0, posCommand, 0, command.length);
        System.arraycopy(args, 0, posCommand, command.length, args.length);
        return posCommand;
    }

    // close the connection to bluetooth printer.
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

    private String PrintDetails(String str1, String str2,String str3) {
        int fulllength = 47;
        int strlength = 5;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength - str3length;

        String ans = str1 + getSpace(anslength) + str2  + getSpace(anslength2)  + getSpace(anslength3) +  str3;
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

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            mmOutputStream.write(msg);
            printNewLine();
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
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2 + "     " + str3;
        }
        return ans;
    }


    void  messagebox(String alerttext) {

        new AlertDialog.Builder(StockTransferActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Stock Transfer")
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

    void printdialog(){

        new AlertDialog.Builder(StockTransferActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Print Receipt")
                .setMessage("Do you want to print receipt?")
                .setCancelable(false)
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

        new AlertDialog.Builder(StockTransferActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Print Receipt")
                .setCancelable(false)
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(StockTransferActivity.this);
        LayoutInflater inflater = StockTransferActivity.this.getLayoutInflater();
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
        Intent IntentInventoryListActivity = new Intent(StockTransferActivity.this, InventoryListActivity.class);
        startActivity(IntentInventoryListActivity);
        finish();
    }




    public void onBackPressed() {
        Intent IntentInventoryListActivity = new Intent(StockTransferActivity.this, InventoryListActivity.class);
        startActivity(IntentInventoryListActivity);
        finish();
    }

}





