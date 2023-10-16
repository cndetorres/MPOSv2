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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class SalesOrderActivity extends Activity {

    DBController controller = new DBController(this);
    ListView SODetails,SOHeader;
    ArrayList<HashMap<String, String>> hmSOHeader;
    ListAdapter laSOHeader,laSODetails;
    HashMap<String, String> mSOHeader,mSODetails;
    BottomNavigationView btSOmenu;
    TextView tvSOCust, tvSOStockItem,tvSOPriceItem,tvDASONext,tvDASOOk,tvSOGAmtItem;
    TextView tvDSOEItem, tvDSOEStockItem,tvDSOEUnitItem,tvDSOEPriceItem,tvDSOETotalItem,tvDSOEDelete,tvDSOESave,tvSODiscItem;
    String Pproducts,PUPrice,Punit,PQty,PTotal,Pprice;
    Button btnSOAdd,btnSOSub,btnSOScan,btnDSOESub,btnDSOEAdd;
    AutoCompleteTextView product;
    Integer intSOQty = 0;
    Integer Pposition,Pscan;
    Double intDSOEPrice;
    ArrayList<String> alSTPAmtMaterial,alPrintSOrderH,alCollectionTurnOver;
    List<String> SOListSettings;
    EditText STTSloc,qty;
    LinearLayout llSOLReturns,llSONAmt,llSODisc;
    DecimalFormat SOAmt = new DecimalFormat("#,###,##0.00");
    DecimalFormat Amt = new DecimalFormat("######0.00");
    TextView tvSOLReturnsItem,tvSONAmtItem;
    int reprint;
    List<HashMap<String, String>> alPrintSOrderD,alPrintSOrderDPromo;

    String promoProducts,promoPrice,promoQty,promoTotal;
    int promoPosition;

    int promoQtyLAC,promoQtyCC;


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

    static int HEAD_WIDTH = 576;
    static int COLOR_THRESHOLD = 128;

    private final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte) 0x1B, (byte) 0x56};
    private final byte[] SET_LINE_SPACING = new byte[]{0x1B, 0x33};

    boolean isClickedTwice;

    private long mLastClickTime = 0;

    int isPromo = 0;
    EditText invoiceNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salesorder);

        tvSOCust = (TextView) findViewById(R.id.tvSOCust);
        SOHeader = (ListView) findViewById(R.id.lvSOHeader);
        SODetails = (ListView) findViewById(R.id.lvSODetails);
        btSOmenu = (BottomNavigationView) findViewById(R.id.btSONavigation);
        llSOLReturns = (LinearLayout) findViewById(R.id.llSOLReturns);
        llSONAmt = (LinearLayout) findViewById(R.id.llSONAmt);
        tvSOGAmtItem = (TextView) findViewById(R.id.tvSOGAmtItem);
        tvSOLReturnsItem = (TextView) findViewById(R.id.tvSOLReturnsItem);
        tvSONAmtItem = (TextView) findViewById(R.id.tvSONAmtItem);
        llSODisc = (LinearLayout) findViewById(R.id.llSODisc);
        tvSODiscItem = (TextView) findViewById(R.id.tvSODiscItem);
        invoiceNo = (EditText) findViewById(R.id.etInvoiceNo);

        readprintername();

        tvSOCust.setText(controller.PCLName);

        ViewHeaderListview();

        SOListSettings = controller.fetchdbSettings();

        isClickedTwice = false;

        reprint = 0;

        if (controller.dbLReturns == 0.00 && controller.PDiscount == 0.00){
            llSOLReturns.setVisibility(View.GONE);
            llSODisc.setVisibility(View.GONE);
            llSONAmt.setVisibility(View.GONE);
        }else if (controller.dbLReturns == 0.00 && controller.PDiscount > 0.00){
            llSOLReturns.setVisibility(View.GONE);
        }else if (controller.dbLReturns > 0.00 && controller.PDiscount == 0.00){
            llSODisc.setVisibility(View.GONE);
        }

        tvSODiscItem.setText(SOAmt.format(controller.PDiscAmt));

        if (controller.PIndicator ==  0){
            controller.hmSODetails = controller.fetchNull();
            /*if (controller.MustCarry().get(5).equals("0")){
                DialogSuggestion();
            }*/
        }else if(controller.PIndicator ==  1){
            invoiceNo.setText(controller.PInvoiceNo);
            ViewDetailListview();
        }else{
            invoiceNo.setText(controller.PInvoiceNo);
            invoiceNo.setEnabled(false);
            ViewDetailListview();
            printdialog();

        }

        /*(if (!controller.PTerms.equals("COD")){
            controller.lesslimit = (controller.PLimit) - (controller.fetchTotalBalARBalances() + controller.fetchTotalSales());
        }*/

        tvSOGAmtItem.setText(String.valueOf(SOAmt.format(controller.dbGAmt)));
        tvSOLReturnsItem.setText(String.valueOf(SOAmt.format(controller.dbLReturns)));
        tvSONAmtItem.setText(String.valueOf(SOAmt.format(controller.dbNSales)));

        btSOmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mso_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                DialogAddItem();


                                break;

                            case R.id.mso_done:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                controller.PIndicator = 1;

                                if (SODetails.getCount() == 0){

                                    if (!controller.PCLName.equals(controller.fetchdbSettings().get(6) + "-CASH SALES")){
                                        if (!controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))){
                                            messagebox2("Nothing to pay. Do you want to proceed to the next activity?");
                                        }else{
                                            finish();
                                        }
                                    }else{
                                        finish();
                                    }


                                }else{

                                    if (invoiceNo.getText().toString().equals("")){
                                        Toasty.error(SalesOrderActivity.this,"Please input invoice no.",Toast.LENGTH_LONG).show();
                                    }else{

                                        controller.PInvoiceNo = invoiceNo.getText().toString();

                                        final ArrayList<String> alADetails = new ArrayList<String>();
                                        alADetails.clear();
                                        for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
                                            HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);

                                            String objProducts = (String) obj.get("Item");
                                            alADetails.add(objProducts);
                                        }

                                        if (!alADetails.contains("CHEESE CK PK") ||
                                                !alADetails.contains("WT CHO PK") ||
                                                !alADetails.contains("LAVA CK CHOCO PK") ||
                                                !alADetails.contains("INPT IBT IBNG FLVRS PK")){
                                            DialogSuggestion();
                                        }else{
                                            DoneOrder();
                                        }
                                    }
                                }

                                break;

                        }
                        return true;
                    }
                });


        SODetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(position);

                String objProducts = (String) obj.get("Item");
                Pproducts = objProducts;
                String objUPrice = (String) obj.get("UPrice");
                PUPrice = objUPrice;
                Pprice = StringUtils.substringBefore(objUPrice,"/");
                Punit = StringUtils.substringAfter(objUPrice,"/");
                String objQty = (String) obj.get("Qty");
                PQty = objQty;
                String objTotal = (String) obj.get("Total");
                PTotal= objTotal;
                Pposition = position;

                DialogEditItem();

                /*isPromo = 0;

                if (controller.PIsWlk == 0){
                    if (controller.fetchPromoType().equals("FREE")){
                        alPromo = controller.fetchPromoAvailable(Pproducts);
                        if (alPromo.size() != 0){
                            if(Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0)) != 0){
                                HashMap<String, Object> objPromo = (HashMap<String, Object>) laSODetails.getItem(position + 1);
                                String objPromoProducts = (String) objPromo.get("Item");
                                promoProducts = objPromoProducts;
                                String objPromoUPrice = (String) objPromo.get("UPrice");
                                promoPrice = objPromoUPrice;
                                String objPromoQty = (String) objPromo.get("Qty");
                                promoQty = objPromoQty;
                                String objPromoTotal = (String) objPromo.get("Total");
                                promoTotal= objPromoTotal;
                                promoPosition = position + 1;
                                isPromo = 1;
                                DialogEditItem();
                            }else{
                                DialogEditItem();
                            }
                    }else{
                            DialogEditItem();
                        }

                    }else{
                        if (controller.fetchCntPromoD(Pproducts) == 0){
                            DialogEditItem();
                        }else{
                            Toasty.info(getApplicationContext(),"promo item not editable", Toast.LENGTH_LONG).show();
                        }
                    }
                }else{
                    DialogEditItem();
                }*/



            }
        });



    }

    private void DoneOrder(){

        if (controller.PIsWlk == 1 && controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6)) && controller.dbGAmt > 2000){
            messagebox("you have exceeded your maximum limit");
        }else if (controller.dbLReturns > 0.00){
            if (Double.valueOf(tvSONAmtItem.getText().toString().replace(",","")) <= 0.00){
                messagebox("nothing to pay");
            }else if(Double.valueOf(tvSONAmtItem.getText().toString().replace(",","")) > controller.lesslimit && !controller.PTerms.equals("COD")){
                messagebox("you have exceeded your maximum limit");
            }else{
                new AlertDialog.Builder(SalesOrderActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Sales Order")
                        .setMessage("Charge " +  String.valueOf(SOAmt.format(controller.dbNSales)) + " pesos to customer?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                controller.PPayment = 0;

                                controller.dbAmtDue = Double.valueOf(SOAmt.format(controller.dbNSales).replace(",",""));
                                controller.dbAmtPd = 0.00;
                                controller.dbBalance = Double.valueOf(SOAmt.format(controller.dbNSales).replace(",",""));

                                Intent intentPaymentDetailsActivity = new Intent(SalesOrderActivity.this,PaymentDetailsActivity.class);
                                startActivity(intentPaymentDetailsActivity);
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }

        }else if (controller.PDiscount > 0.00){
            if (Double.valueOf(tvSONAmtItem.getText().toString().replace(",","")) <= 0.00){
                messagebox("nothing to pay");
            }else if(Double.valueOf(tvSONAmtItem.getText().toString().replace(",","")) > controller.lesslimit && !controller.PTerms.equals("COD")){
                messagebox("you have exceeded your maximum limit");
            }else{
                new AlertDialog.Builder(SalesOrderActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Sales Order")
                        .setMessage("Charge " +  String.valueOf(SOAmt.format(controller.dbNSales)) + " pesos to customer?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                controller.PPayment = 0;

                                controller.dbAmtDue = Double.valueOf(SOAmt.format(controller.dbNSales).replace(",",""));
                                controller.dbAmtPd = 0.00;
                                controller.dbBalance = Double.valueOf(SOAmt.format(controller.dbNSales).replace(",",""));

                                Intent intentPaymentDetailsActivity = new Intent(SalesOrderActivity.this,PaymentDetailsActivity.class);
                                startActivity(intentPaymentDetailsActivity);
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }

        }else {
            if (Double.valueOf(tvSOGAmtItem.getText().toString().replace(",", "")) <= 0.00) {
                messagebox("nothing to pay");
            } else if (Double.valueOf(tvSOGAmtItem.getText().toString().replace(",", "")) > controller.lesslimit && !controller.PTerms.equals("COD")) {
                messagebox("you have exceeded your maximum limit");
            } else {
                new AlertDialog.Builder(SalesOrderActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Sales Order")
                        .setMessage("Charge " + String.valueOf(SOAmt.format(controller.dbGAmt)) + " pesos to customer?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                controller.PPayment = 0;

                                controller.dbAmtDue = Double.valueOf(SOAmt.format(controller.dbGAmt).replace(",", ""));
                                controller.dbAmtPd = 0.00;
                                controller.dbBalance = Double.valueOf(SOAmt.format(controller.dbGAmt).replace(",", ""));

                                Intent intentPaymentDetailsActivity = new Intent(SalesOrderActivity.this, PaymentDetailsActivity.class);
                                startActivity(intentPaymentDetailsActivity);
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }
    }



    public void ViewHeaderListview() {

        hmSOHeader = new ArrayList<HashMap<String, String>>();
        mSOHeader = new HashMap<String, String>();

        mSOHeader.put("Item", "ITEM");
        mSOHeader.put("UPrice", "U PRICE");
        mSOHeader.put("Qty", "QTY");
        mSOHeader.put("Total", "TOTAL");
        hmSOHeader.add(mSOHeader);

        try {
            laSOHeader = new SimpleAdapter(this, hmSOHeader, R.layout.item_salesorder,
                    new String[]{"Item", "UPrice", "Qty", "Total"}, new int[]{
                    R.id.rowsItem, R.id.rowsUPrice, R.id.rowsQty, R.id.rowsTotal}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView ruprice = (TextView) view.findViewById(R.id.rowsUPrice);
                    TextView rqty = (TextView) view.findViewById(R.id.rowsQty);
                    TextView rtotal = (TextView) view.findViewById(R.id.rowsTotal);
                    if (position % 2 == 0) {
                        ritem.setTextColor(Color.WHITE);
                        ruprice.setTextColor(Color.WHITE);
                        rqty.setTextColor(Color.WHITE);
                        rtotal.setTextColor(Color.WHITE);
                        ritem.setTypeface(null, Typeface.BOLD);
                        ruprice.setTypeface(null, Typeface.BOLD);
                        rqty.setTypeface(null, Typeface.BOLD);
                        rtotal.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            SOHeader.setAdapter(laSOHeader);
        } catch (Exception e) {

        }
    }

    public void AddDetailListview(String products,String price,String qty,String total){

        mSODetails = new HashMap<String, String>();


        mSODetails.put("Item",products);
        mSODetails.put("UPrice", price);
        mSODetails.put("Qty", qty);
        mSODetails.put("Total", total);
        controller.hmSODetails.add(mSODetails);

        try {
            laSODetails = new SimpleAdapter(this, controller.hmSODetails, R.layout.item_salesorder,
                    new String[]{"Item", "UPrice", "Qty", "Total"}, new int[]{
                    R.id.rowsItem, R.id.rowsUPrice, R.id.rowsQty, R.id.rowsTotal}) {
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
            SODetails.setAdapter(laSODetails);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laSODetails = new SimpleAdapter(this, controller.hmSODetails, R.layout.item_salesorder,
                    new String[]{"Item", "UPrice", "Qty", "Total"}, new int[]{
                    R.id.rowsItem, R.id.rowsUPrice, R.id.rowsQty, R.id.rowsTotal}) {
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
            SODetails.setAdapter(laSODetails);
        } catch (Exception e) {

        }
    }

    private void promoRecompute(){

        for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);
            String objProducts = (String) obj.get("Item");

            if (objProducts.equals(controller.fetchPromoBundleItem())){

                if (isPromo == 2){
                    controller.PMName = objProducts;
                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterialwithDisc();
                    String objQty = (String) obj.get("Qty");
                    Double TAmt = Double.valueOf(objQty) * Double.valueOf(alSTPAmtMaterial.get(2));
                    Pposition = i;
                    UpdateDetailListview(objProducts,alSTPAmtMaterial.get(2) + "/" + alSTPAmtMaterial.get(0),objQty,String.valueOf(SOAmt.format(TAmt)));
                }else{
                    controller.PMName = objProducts;
                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                    String objQty = (String) obj.get("Qty");
                    Double TAmt = Double.valueOf(objQty) * Double.valueOf(alSTPAmtMaterial.get(2));
                    Pposition = i;
                    UpdateDetailListview(objProducts,alSTPAmtMaterial.get(2) + "/" + alSTPAmtMaterial.get(0),objQty,String.valueOf(SOAmt.format(TAmt)));
                }
            }

        }

    }

    public void UpdateDetailListview(String products,String price, String qty,String total){

        mSODetails = new HashMap<String, String>();


        mSODetails.put("Item",products);
        mSODetails.put("UPrice", price);
        mSODetails.put("Qty", qty);
        mSODetails.put("Total", total);
        controller.hmSODetails.set(Pposition,mSODetails);

        /*controller.hmSODetails.get(0).get("Item");
        controller.hmSODetails.set(Pposition,mSODetails);*/

        try {
            laSODetails = new SimpleAdapter(this, controller.hmSODetails, R.layout.item_salesorder,
                    new String[]{"Item", "UPrice", "Qty", "Total"}, new int[]{
                    R.id.rowsItem, R.id.rowsUPrice, R.id.rowsQty, R.id.rowsTotal}) {
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
            SODetails.setAdapter(laSODetails);
        } catch (Exception e) {

        }
    }

    public void DeleteDetailListview(String products,String price, String qty,String total){

        mSODetails = new HashMap<String, String>();


        mSODetails.put("Item",products);
        mSODetails.put("UPrice", price);
        mSODetails.put("Qty", qty);
        mSODetails.put("Total", total);
        controller.hmSODetails.remove(mSODetails);

        try {
            laSODetails = new SimpleAdapter(this, controller.hmSODetails, R.layout.item_salesorder,
                    new String[]{"Item", "UPrice", "Qty", "Total"}, new int[]{
                    R.id.rowsItem, R.id.rowsUPrice, R.id.rowsQty, R.id.rowsTotal}) {
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
            SODetails.setAdapter(laSODetails);
        } catch (Exception e) {

        }
    }

    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SalesOrderActivity.this);
        LayoutInflater inflater = SalesOrderActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additemsalesorder, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        tvDASOOk = (TextView) dialogView.findViewById(R.id.tvDASOOk);
        tvDASONext = (TextView) dialogView.findViewById(R.id.tvDASONext);
        btnSOAdd = (Button) dialogView.findViewById(R.id.btnDASOAdd);
        btnSOSub = (Button) dialogView.findViewById(R.id.btnDASOSub);
        btnSOScan = (Button) dialogView.findViewById(R.id.btnDASOScan);
        product =(AutoCompleteTextView) dialogView.findViewById(R.id.acDASOProduct);
        qty =(EditText)dialogView.findViewById(R.id.etDASOQty);
        final ImageView ivDSRList =(ImageView)dialogView.findViewById(R.id.ivDASOList);
        tvSOStockItem = (TextView) dialogView.findViewById(R.id.tvDASOStockItem);
        tvSOPriceItem = (TextView) dialogView.findViewById(R.id.tvDASOPriceItem);


        final String products[] = controller.fetchSTMaterials();


        final List<String> productlist = Arrays.asList(products);

        final ArrayAdapter<String> aaAProducts = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, products);

        product.setAdapter(aaAProducts);
        product.setThreshold(1);

        tvSOStockItem.setText("0");
        tvSOPriceItem.setText("0.00");
        promoQty = "0";


        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int pos,
                                    long id) {
                try{

                    promoQtyCC = 0;
                    promoQtyLAC = 0;
                    isPromo = 0;

                    controller.PMName = product.getText().toString();
                    if (controller.fetchPromoType().equals("DISC")){
                        if (controller.fetchCountMPOSPromoCustomer() == 1){
                            if(SODetails.getCount() !=0){
                                final ArrayList<String> alADetails = new ArrayList<String>();
                                alADetails.clear();
                                for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
                                    HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);

                                    String objProducts = (String) obj.get("Item");

                                    if (objProducts.equals(controller.fetchPromoItem())){
                                        promoQtyCC = Integer.valueOf((String) obj.get("Qty"));
                                    }else if (objProducts.equals(controller.fetchPromoBundleItem())){
                                        promoQtyLAC = Integer.valueOf((String) obj.get("Qty"));
                                    }

                                    alADetails.add(objProducts);
                                }

                                if (alADetails.contains(controller.fetchPromoItem()) && alADetails.contains(controller.fetchPromoBundleItem())){
                                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                                    isPromo = 0;
                                } else if (alADetails.contains(controller.fetchPromoItem()) && product.getText().toString().equals(controller.fetchPromoBundleItem())){
                                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterialwithDisc();
                                    isPromo = 1;
                                }else if (alADetails.contains(controller.fetchPromoBundleItem()) && product.getText().toString().equals(controller.fetchPromoItem())){
                                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                                    isPromo = 2;
                                }else{
                                    alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                                }
                            }else{
                                alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                            }
                        }else{
                            alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                        }
                    }else{
                        alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                    }

                    tvSOStockItem.setText(alSTPAmtMaterial.get(1));
                    tvSOPriceItem.setText(alSTPAmtMaterial.get(2));
                    Punit = alSTPAmtMaterial.get(0);
                    qty.requestFocus();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                }catch (Exception e){
                    Toasty.error(getApplicationContext(), e.getMessage().toString()/*"no pricelist maintained in " + product.getText().toString()*/ , Toast.LENGTH_LONG).show();
                }

            }
        });

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

        /*if(qty.requestFocus()) {
            btnSOScan.setVisibility(View.VISIBLE);
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
                    btnSOScan.setVisibility(View.VISIBLE);
                }else{
                    btnSOScan.setVisibility(View.GONE);
                }
            }

        });*/

        tvDASOOk.setOnClickListener(new OnSingleClickListener() {
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
                }else if (isPromo == 1 && Integer.valueOf(qty.getText().toString()) > promoQtyCC){
                    messagebox("item must be less than or equal to promo bundle");
                }else if (isPromo == 2 && Integer.valueOf(qty.getText().toString()) < promoQtyLAC){
                    messagebox("item must be greater than or equal to promo bundle");
                } else if (Integer.parseInt(qty.getText().toString()) == 0){
                    //messagebox("quantity must not be zero(0)");
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                    isClickedTwice = false;
                    alertDialog.dismiss();
                }else if (Integer.parseInt(qty.getText().toString()) > Integer.parseInt(tvSOStockItem.getText().toString())){
                    messagebox("out of stock");
                }else {
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    PQty = qty.getText().toString();
                    PUPrice = tvSOPriceItem.getText().toString() + "/" + Punit;
                    Double TAmt = Double.valueOf(tvSOPriceItem.getText().toString().replace(",","")) * Double.valueOf(PQty);
                    PTotal = String.valueOf(SOAmt.format(TAmt));

                    if (SODetails.getCount() == 0){

                        AddDetailListview(Pproducts,PUPrice,PQty,PTotal);

                        //FREE
                        /*if (controller.fetchPromoType().equals("FREE")){
                            if (controller.PIsWlk == 0){
                                alPromo = controller.fetchPromoAvailable(Pproducts);
                                if(alPromo.size() != 0){
                                    if(Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0)) != 0){
                                        AddDetailListview(alPromo.get(2), SOAmt.format(Double.valueOf(alPromo.get(3)))+ "/" + alPromo.get(4),
                                                String.valueOf((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1))),
                                                SOAmt.format((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1)) * Integer.valueOf(alPromo.get(3))));
                                    }
                                }
                            }

                        }*/


                        computegrossamt();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                        Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                        isClickedTwice = false;
                        alertDialog.dismiss();
                    }else{
                        final ArrayList<String> alADetails = new ArrayList<String>();
                        alADetails.clear();
                        for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);

                            String objProducts = (String) obj.get("Item");
                            alADetails.add(objProducts);
                        }
                        if (alADetails.contains(Pproducts)){
                            messagebox(Pproducts + " already in the list");
                        }else{
                            AddDetailListview(Pproducts,PUPrice,PQty,PTotal);

                            //FREE
                           /* if (controller.fetchPromoType().equals("FREE")){
                                if (controller.PIsWlk == 0){
                                    alPromo = controller.fetchPromoAvailable(Pproducts);
                                    if(alPromo.size() != 0){
                                        if(Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0)) != 0){
                                            AddDetailListview(alPromo.get(2), SOAmt.format(Double.valueOf(alPromo.get(3)))+ "/" + alPromo.get(4),
                                                    String.valueOf((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1))),
                                                    SOAmt.format((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1)) * Integer.valueOf(alPromo.get(3))));
                                        }
                                    }
                                }

                            }*/

                            if (isPromo == 2){
                                promoRecompute();
                                messagebox("System detected promo bundle item. This will trigger recompute.");
                            }

                            computegrossamt();
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

        tvDASONext.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (product.getText().toString().equals("")) {
                    messagebox("please select item");
                }else if (!productlist.contains(product.getText().toString())){
                    messagebox("product not in list");
                }else if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0){
                    messagebox("quantity must not be zero(0)");
                }else if (isPromo == 1 && Integer.valueOf(qty.getText().toString()) > promoQtyCC){
                    messagebox("item must be less than or equal to promo bundle");
                }else if (isPromo == 2 && Integer.valueOf(qty.getText().toString()) < promoQtyLAC){
                    messagebox("item must be greater than or equal to promo bundle");
                }else if (Integer.parseInt(qty.getText().toString()) > Integer.parseInt(tvSOStockItem.getText().toString())){
                    messagebox("out of stock");
                }else {
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    PQty = qty.getText().toString();
                    PUPrice = tvSOPriceItem.getText().toString() + "/" + Punit;
                    Double TAmt = Double.valueOf(tvSOPriceItem.getText().toString().replace(",","")) * Double.valueOf(PQty);
                    PTotal = String.valueOf(SOAmt.format(TAmt));

                    if (SODetails.getCount() == 0){
                        AddDetailListview(Pproducts,PUPrice,PQty,PTotal);

                        //FREE
                      /*  if (controller.fetchPromoType().equals("FREE")){
                            if (controller.PIsWlk == 0){
                                alPromo = controller.fetchPromoAvailable(Pproducts);
                                if(alPromo.size() != 0){
                                    if(Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0)) != 0){
                                        AddDetailListview(alPromo.get(2), SOAmt.format(Double.valueOf(alPromo.get(3)))+ "/" + alPromo.get(4),
                                                String.valueOf((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1))),
                                                SOAmt.format((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1)) * Integer.valueOf(alPromo.get(3))));
                                    }
                                }
                            }

                        }
*/
                        qty.setText("");
                        product.setText("");
                        tvSOStockItem.setText("0");
                        tvSOPriceItem.setText("0.00");
                        computegrossamt();
                        product.requestFocus();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                        Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                    }else{
                        final ArrayList<String> alADetails = new ArrayList<String>();
                        alADetails.clear();
                        for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);

                            String objProducts = (String) obj.get("Item");
                            alADetails.add(objProducts);
                        }
                        if (alADetails.contains(Pproducts)){
                            messagebox(Pproducts + " already in the list");
                        }else{
                            AddDetailListview(Pproducts,PUPrice,PQty,PTotal);

                            //FREE
                            /*if (controller.fetchPromoType().equals("FREE")){
                                if (controller.PIsWlk == 0){
                                    alPromo = controller.fetchPromoAvailable(Pproducts);
                                    if(alPromo.size() != 0){
                                        if(Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0)) != 0){
                                            AddDetailListview(alPromo.get(2), SOAmt.format(Double.valueOf(alPromo.get(3)))+ "/" + alPromo.get(4),
                                                    String.valueOf((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1))),
                                                    SOAmt.format((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1)) * Integer.valueOf(alPromo.get(3))));
                                        }
                                    }
                                }

                            }*/

                            if (isPromo == 2){
                                promoRecompute();
                                messagebox("System detected promo bundle item. This will trigger recompute.");
                            }

                            qty.setText("");
                            product.setText("");
                            tvSOStockItem.setText("0");
                            tvSOPriceItem.setText("0.00");
                            computegrossamt();
                            product.requestFocus();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                            Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
        });

        btnSOAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else if (Integer.parseInt(qty.getText().toString()) == Integer.parseInt(tvSOStockItem.getText().toString())){
                    messagebox("out of stock");
                }else{
                    intSOQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intSOQty.toString());
                }

            }
        });

        btnSOSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intSOQty = Integer.parseInt(qty.getText().toString()) - 1;

                    qty.setText(intSOQty.toString());
                }
            }
        });

        ivDSRList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                product.showDropDown();
                //btnSOScan.setVisibility(View.GONE);
            }
        });

        btnSOScan.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                new IntentIntegrator(SalesOrderActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });

        alertDialog.show();

    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SalesOrderActivity.this);
        LayoutInflater inflater = SalesOrderActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edititemsalesorder, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        tvDSOEDelete = (TextView) dialogView.findViewById(R.id.tvDSOEDelete);
        tvDSOESave = (TextView) dialogView.findViewById(R.id.tvDSOESave);
        btnDSOEAdd = (Button) dialogView.findViewById(R.id.btnDSOEAdd);
        btnDSOESub = (Button) dialogView.findViewById(R.id.btnDSOESub);
        tvDSOEItem = (TextView) dialogView.findViewById(R.id.tvDSOEItem);
        tvDSOEStockItem = (TextView) dialogView.findViewById(R.id.tvDSOEStockItem);
        tvDSOEUnitItem = (TextView) dialogView.findViewById(R.id.tvDSOEUnitItem);
        tvDSOEPriceItem = (TextView) dialogView.findViewById(R.id.tvDSOEPriceItem);
        tvDSOETotalItem = (TextView) dialogView.findViewById(R.id.tvDSOETotalItem);
        final EditText qty =(EditText)dialogView.findViewById(R.id.etDSOEQty);

        tvDSOEItem.setText(Pproducts);
        tvDSOEUnitItem.setText(Punit);
        qty.setText(PQty);

        isPromo = 0;

        if (controller.fetchPromoType().equals("DISC")) {
            if (controller.fetchCountMPOSPromoCustomer() == 1) {

                    for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
                        HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);
                        String objProducts = (String) obj.get("Item");

                        if (objProducts.equals(controller.fetchPromoItem())) {
                            promoQtyCC = Integer.valueOf((String) obj.get("Qty"));
                            if (tvDSOEItem.getText().toString().equals(controller.fetchPromoBundleItem())){
                                isPromo = 1;
                            }
                        }else if(objProducts.equals(controller.fetchPromoBundleItem())){
                            promoQtyLAC = Integer.valueOf((String) obj.get("Qty"));
                            if (tvDSOEItem.getText().toString().equals(controller.fetchPromoItem())){
                                isPromo = 2;
                            }
                        }

                    }
                }
        }

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

        controller.PMName = tvDSOEItem.getText().toString();
        alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();

        tvDSOEStockItem.setText(alSTPAmtMaterial.get(1));
        tvDSOEPriceItem.setText(Pprice);

        //intDSOEPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvDSOEPriceItem.getText().toString());
        tvDSOETotalItem.setText(PTotal);

        btnDSOEAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else if (Integer.parseInt(qty.getText().toString()) == Integer.parseInt(tvDSOEStockItem.getText().toString())){
                    messagebox("out of stock");
                }else{
                    intSOQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intSOQty.toString());
                    intDSOEPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvDSOEPriceItem.getText().toString().replace(",",""));
                    tvDSOETotalItem.setText(String.valueOf(SOAmt.format(intDSOEPrice)));
                }

            }
        });

        btnDSOESub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intSOQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intSOQty.toString());
                    intDSOEPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvDSOEPriceItem.getText().toString().replace(",",""));
                    tvDSOETotalItem.setText(String.valueOf(SOAmt.format(intDSOEPrice)));
                }

            }
        });

        tvDSOEDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                new AlertDialog.Builder(SalesOrderActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Close")
                        .setMessage("Are you sure you want to delete " + Pproducts +" ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toasty.error(getApplicationContext(),  Pproducts + " successfully deleted", Toast.LENGTH_LONG).show();
                                DeleteDetailListview(Pproducts,PUPrice,PQty,PTotal);

                                if (controller.fetchPromoType().equals("DISC")) {
                                    if (controller.fetchCountMPOSPromoCustomer() == 1) {

                                        for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
                                            HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);
                                            String objProducts = (String) obj.get("Item");

                                            if (objProducts.equals(controller.fetchPromoBundleItem())) {
                                                if (tvDSOEItem.getText().toString().equals(controller.fetchPromoItem())){
                                                    isPromo = 3;
                                                }
                                            }
                                        }
                                    }
                                }

                                if (isPromo == 3){
                                    promoRecompute();
                                    messagebox("System detected deleted promo bundle item. This will trigger recompute.");
                                }

                               /* if (controller.PIsWlk == 0){
                                    if (isPromo == 1){
                                        DeleteDetailListview(promoProducts,promoPrice,promoQty,promoTotal);
                                    }
                                }*/
                                computegrossamt();

                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                                alertDialog.dismiss();

                            }

                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        });

        tvDSOESave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0) {
                    messagebox("quantity must not be zero(0)");
                } else if (Integer.valueOf(qty.getText().toString()) > Integer.parseInt(tvDSOEStockItem.getText().toString())) {
                    messagebox("out of stock");
                }else if (isPromo == 1 && Integer.valueOf(qty.getText().toString()) > promoQtyCC){
                    messagebox("item must be less than or equal to promo bundle");
                }else if (isPromo == 2 && Integer.valueOf(qty.getText().toString()) < promoQtyLAC){
                    messagebox("item must be greater than or equal to promo bundle");
                } else {
                    PQty = qty.getText().toString();

                    intDSOEPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvDSOEPriceItem.getText().toString().replace(",",""));
                    tvDSOETotalItem.setText(String.valueOf(SOAmt.format(intDSOEPrice)));

                    PTotal = tvDSOETotalItem.getText().toString();
                    UpdateDetailListview(Pproducts,PUPrice,PQty,PTotal);

                    /*if (controller.PIsWlk == 0){
                        UpdateDetailListview(Pproducts,PUPrice,PQty,PTotal);
                        if (isPromo == 1){
                            DeleteDetailListview(Pproducts,PUPrice,PQty,PTotal);
                            DeleteDetailListview(promoProducts,promoPrice,promoQty,promoTotal);

                            AddDetailListview(Pproducts,PUPrice,PQty,PTotal);
                            alPromo = controller.fetchPromoAvailable(Pproducts);
                            if(alPromo.size() != 0){
                                if(Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0)) != 0){
                                    AddDetailListview(alPromo.get(2), SOAmt.format(Double.valueOf(alPromo.get(3)))+ "/" + alPromo.get(4),
                                            String.valueOf((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1))),
                                            SOAmt.format((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1)) * Integer.valueOf(alPromo.get(3))));
                                }
                            }

                        }else{

                            alPromo = controller.fetchPromoAvailable(Pproducts);
                            if(alPromo.size() != 0){
                                if(Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0)) != 0){
                                    DeleteDetailListview(Pproducts,PUPrice,PQty,PTotal);
                                    AddDetailListview(Pproducts,PUPrice,PQty,PTotal);
                                    AddDetailListview(alPromo.get(2), SOAmt.format(Double.valueOf(alPromo.get(3)))+ "/" + alPromo.get(4),
                                            String.valueOf((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1))),
                                            SOAmt.format((Integer.valueOf(PQty)/Integer.valueOf(alPromo.get(0))) * Integer.valueOf(alPromo.get(1)) * Integer.valueOf(alPromo.get(3))));
                                }else{
                                    UpdateDetailListview(Pproducts,PUPrice,PQty,PTotal);
                                }
                            }else{
                                UpdateDetailListview(Pproducts,PUPrice,PQty,PTotal);
                            }

                        }
                    }else{
                        UpdateDetailListview(Pproducts,PUPrice,PQty,PTotal);
                    }*/

                    computegrossamt();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                    Toasty.info(getApplicationContext(), Pproducts + " successfully updated", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
            }
        });




        alertDialog.show();

    }

    public void DialogSuggestion() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SalesOrderActivity.this);
        LayoutInflater inflater = SalesOrderActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_incomplete_mustcarry, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        TextView suggestion = (TextView) dialogView.findViewById(R.id.tvSuggestion);
        TextView suggestion2 = (TextView) dialogView.findViewById(R.id.tvSuggestion2);
        CheckBox cheeseCake = (CheckBox) dialogView.findViewById(R.id.chkCheeseCake);
        CheckBox whattaChocolahat = (CheckBox) dialogView.findViewById(R.id.chkWhattaChocolahat);
        CheckBox lavaChoco = (CheckBox) dialogView.findViewById(R.id.chkLavaChocolate);
        CheckBox inipitAssorted = (CheckBox) dialogView.findViewById(R.id.chkInipitAssorted);

        TextView ok = (TextView) dialogView.findViewById(R.id.tvOk);
        TextView close = (TextView) dialogView.findViewById(R.id.tvClose);

        suggestion.setText("Customer "+ controller.PCLName +" must carry the following items.");

        final ArrayList<String> alADetails = new ArrayList<String>();
        alADetails.clear();
        for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);

            String objProducts = (String) obj.get("Item");
            alADetails.add(objProducts);
        }


        if (alADetails.contains("CHEESE CK PK")){
            cheeseCake.setVisibility(View.GONE);
        }
        if  (alADetails.contains("WT CHO PK")){
            whattaChocolahat.setVisibility(View.GONE);
        }
        if  (alADetails.contains("LAVA CK CHOCO PK")){
            lavaChoco.setVisibility(View.GONE);
        }
        if  (alADetails.contains("INPT IBT IBNG FLVRS PK")){
            inipitAssorted.setVisibility(View.GONE);
        }


        suggestion2.setText("Ask the customer to place an order.");

        alertDialog.setCanceledOnTouchOutside(false);

        ok.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
                DoneOrder();
            }
        });

        close.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
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

            if(controller.FetchNameMaterials().get(0).equals("")){
                messagebox("Material not found");
            }else{

                controller.PMName = controller.FetchNameMaterials().get(0);
                alSTPAmtMaterial = controller.fetchUnitQtyPriceMaterial();
                if (alSTPAmtMaterial.get(1).equals("0")){
                    messagebox("not enough stocks");
                }else{
                    tvSOStockItem.setText(alSTPAmtMaterial.get(1));
                    tvSOPriceItem.setText(alSTPAmtMaterial.get(2));
                    product.setText(controller.PMName);
                    qty.requestFocus();
                }
            }

    }

    void computegrossamt(){

        controller.dbGAmt = 0.00;
        controller.dbNSales = 0.00;

        for (int i = 0; i < SODetails.getAdapter().getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laSODetails.getItem(i);

            String objTotal = (String) obj.get("Total");
            controller.dbGAmt = controller.dbGAmt + Double.valueOf(objTotal.replace(",",""));
        }

        if (controller.dbLReturns > 0.00){
            tvSOGAmtItem.setText(String.valueOf(SOAmt.format(controller.dbGAmt)));

            if (controller.PDiscount > 0.00){
                controller.PDiscAmt = controller.dbGAmt * controller.PDiscount;
                tvSODiscItem.setText(String.valueOf(SOAmt.format(controller.PDiscAmt)));
                controller.dbNSales = controller.dbGAmt - controller.PDiscAmt -Double.valueOf(tvSOLReturnsItem.getText().toString().replace(",",""));
            }else{
                controller.dbNSales = controller.dbGAmt - Double.valueOf(tvSOLReturnsItem.getText().toString().replace(",",""));
            }

            if (controller.dbGAmt <= 0.00){
                controller.dbNSales = 0.00;
                tvSONAmtItem.setText(String.valueOf(SOAmt.format(controller.dbNSales)));
            }else{
                tvSONAmtItem.setText(String.valueOf(SOAmt.format(controller.dbNSales)));
            }
        }else{
            tvSOGAmtItem.setText(String.valueOf(SOAmt.format(controller.dbGAmt)));
            if (controller.PDiscount > 0.00){
                controller.PDiscAmt = controller.dbGAmt * controller.PDiscount;
                tvSODiscItem.setText(String.valueOf(SOAmt.format(controller.PDiscAmt)));
                controller.dbNSales = controller.dbGAmt - controller.PDiscAmt;
                tvSONAmtItem.setText(String.valueOf(SOAmt.format(controller.dbNSales)));
            }

        }

    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(SalesOrderActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Sales Order")
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

    void  messagebox2(String alerttext) {

        new AlertDialog.Builder(SalesOrderActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Sales Order")
                .setMessage(alerttext)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                        Calendar defaultDate = Calendar.getInstance();
                        String logDate;
                        logDate = defaultDateFormat2.format(defaultDate.getTime());
                        controller.insertCustomerLogsItem(controller.fetchLogID(),4,logDate,1);

                        dialog.dismiss();

                        Intent intent = new Intent(SalesOrderActivity.this, CheckDisplayActivity.class);
                        startActivity(intent);
                        finish();

                    }

                })
                .setNegativeButton("No", null)
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

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.print_logo);

            printImage(bmp);

            String companyName = controller.fetchdbSettings().get(19);
            String title = "DELIVERY RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;
            String address,contactNo;
            address = getSpace(printHalfLen - (SOListSettings.get(13).length()/2)) + SOListSettings.get(13);
            contactNo = getSpace(printHalfLen - (SOListSettings.get(10).length()/2)) + SOListSettings.get(10);

            printCustom(companyName,3);
            printCustom( address,0);
            printCustom( contactNo,0);
            printNewLine();
            printCustom(title,3);
            printNewLine();

            String PaymentMode = "\\ ";
            int tmsmode = 0;

            for(int i = 0; i < controller.printPaymentItemD(controller.SIDCode).size() ; i++){

                if (controller.printPaymentItemD(controller.SIDCode).get(i).get("PaymentMode").equals("TMS")){
                    tmsmode = 1;
                }else if(controller.printPaymentItemD(controller.SIDCode).get(i).get("PaymentMode").equals("CASH SALES")){
                    tmsmode = 2;
                }
                PaymentMode = PaymentMode + controller.printPaymentItemD(controller.SIDCode).get(i).get("PaymentMode") + " \\" ;


            }

            printCustom(PaymentMode,0);


            alPrintSOrderH = controller.printSOrderH(controller.SIDCode);

            printText(WithDate("Customer Code: " + alPrintSOrderH.get(0), alPrintSOrderH.get(1)));
            printNewLine();
            printCustom("Customer: " + alPrintSOrderH.get(2),0);
            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1),0);
            printCustom("Terminal: " + SOListSettings.get(1),0);
            printCustom("Sales ID: " + controller.SIDCode,0);
            printCustom("Payment ID: " + alPrintSOrderH.get(3),0);
            printCustom("Invoice No.: " + alPrintSOrderH.get(13),0);
            printNewLine();

            alPrintSOrderD = controller.printSOrderD(controller.SIDCode);
            alPrintSOrderDPromo = controller.printSOrderPromo(controller.SIDCode);

            printText(PrintDetails("#","QTY","ITEM","PRICE","AMOUNT"));
            printNewLine();

            printText(getLine(57));
            printNewLine();

            int totalqty;
            totalqty = 0;

            for(int i = 0; i < alPrintSOrderD.size() ; i++){

                String item;
                item = alPrintSOrderD.get(i).get("MatDesc");
                if (item.length() > 29){
                    item = item.substring(0,29);
                }

                totalqty = totalqty + Integer.valueOf(alPrintSOrderD.get(i).get("Qty"));

                printText(PrintDetails(String.valueOf(i+1),alPrintSOrderD.get(i).get("Qty"),item,alPrintSOrderD.get(i).get("Amt"),alPrintSOrderD.get(i).get("Total")));
                printNewLine();
            }

            printText(getLine(57));
            printNewLine();

            alPrintSOrderDPromo = controller.printSOrderPromo(controller.SIDCode);
            if (alPrintSOrderDPromo.size() != 0){
                printNewLine();
                printText(PrintDetails("#","QTY","PROMO ITEM","PRICE","AMOUNT"));
                printNewLine();

                printText(getLine(57));
                printNewLine();

                for(int i = 0; i < alPrintSOrderDPromo.size() ; i++){

                    String item;
                    item = alPrintSOrderDPromo.get(i).get("MatDesc");
                    if (item.length() > 29){
                        item = item.substring(0,29);
                    }

                    totalqty = totalqty + Integer.valueOf(alPrintSOrderDPromo.get(i).get("Qty"));

                    printText(PrintDetails(String.valueOf(i+1),alPrintSOrderDPromo.get(i).get("Qty"),item,alPrintSOrderDPromo.get(i).get("Amt"),alPrintSOrderDPromo.get(i).get("Total")));
                    printNewLine();
                }

                printText(getLine(57));
                printNewLine();
            }


            int grosstotal = 0;
            grosstotal = alPrintSOrderD.size() + alPrintSOrderDPromo.size();

            printText2(PrintGrossDetailsSO("Gross Total: (" + grosstotal+ " ITEM/S)",String.valueOf(totalqty), alPrintSOrderH.get(4)));
            printNewLine();
            printNewLine();

            if (!alPrintSOrderH.get(12).equals("0.00")){
                printText(PrintAmtDetails("Discount:", alPrintSOrderH.get(12)));
                printNewLine();

            }


            if (!alPrintSOrderH.get(5).equals("0.00") && tmsmode == 3){

            }else{
                printText(PrintAmtDetails("Less Returns:", alPrintSOrderH.get(5)));
                printNewLine();
            }


            printText2(PrintAmtDetails("Net Amount:", alPrintSOrderH.get(6)));
            printNewLine();
            printNewLine();

            if (!alPrintSOrderH.get(7).equals("0.00")){
                printText(PrintAmtDetails("Cash Received:", alPrintSOrderH.get(7)));
                printNewLine();
            }

            if (tmsmode == 1){

            }else if (tmsmode == 2 && alPrintSOrderH.get(8).equals("0.00")){

            }else{
                printText(PrintAmtDetails("Checks Received:", alPrintSOrderH.get(8)));
                printNewLine();
            }

            if (!alPrintSOrderH.get(9).equals("0.00")){
                printText(PrintAmtDetails("Charged Sales:", alPrintSOrderH.get(9)));
                printNewLine();
            }

            if (tmsmode == 1){
                printText(PrintAmtDetails("TMS Payment:", alPrintSOrderH.get(6)));
                printNewLine();
            }



            printText(PrintAmtDetails("Change:", alPrintSOrderH.get(10)));
            printNewLine();

            printText(getLine(57));
            printNewLine();

            printCustom("IMPORTANT",0);
            printCustom("PLEASE MAKE CHECK PAYABLE TO:",0);
            printCustom(controller.fetchdbSettings().get(19),0);
            printCustom("ALWAYS ASK FOR PROVISIONAL RECEIPT OR OFFICIAL RECEIPT",0);
            printCustom("WHEN MAKING PAYMENT, AND KEEP IT ON FILE. THIS IS YOUR",0);
            printCustom("PROOF OF PAYMENT",0);

            printNewLine();
            printCustom("SIGNATURE",0);

            printNewLine();

            Bitmap bmps = BitmapFactory.decodeByteArray(controller.fetchSignatureSItem(controller.SIDCode), 0, controller.fetchSignatureSItem(controller.SIDCode).length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmps, 250, 90, false);

            printImage(resizedbmp);

            printCustom("Signed by:" + alPrintSOrderH.get(11),0);
            printNewLine();

            String website = controller.fetchdbSettings().get(20);
            if (website.equals("")){
                website = "";
            }else{
                website = getSpace(printHalfLen - (website.length()/2)) + website;
            }
            String footerTitle = "CUSTOMER SERVICE";
            String footerContactNo = SOListSettings.get(12);
            String forOrders = "For Orders please contact";
            String orderContactNo = SOListSettings.get(11);
            String thankYou = "Thank you for your patronage!";

            footerTitle = getSpace(printHalfLen - (footerTitle.length()/2)) + footerTitle;
            footerContactNo = getSpace(printHalfLen - (footerContactNo.length()/2)) + footerContactNo;
            forOrders = getSpace(printHalfLen - (forOrders.length()/2)) + forOrders;
            orderContactNo = getSpace(printHalfLen - (orderContactNo.length()/2)) + orderContactNo;
            thankYou = getSpace(printHalfLen - (thankYou.length()/2)) + thankYou;


            printCustom(website,0);
            printCustom(footerTitle,0);
            printCustom( footerContactNo,0);
            printCustom(forOrders,0);
            printCustom( orderContactNo,0);
            printNewLine();
            printCustom(thankYou,0);



            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String PrintGrossDetailsSO(String str1,String str2,String str3) {
        int fulllength = 33;
        int strlength2 = 5;
        int strlength3 = 19;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2  = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength3 - str3length;


        String ans = str1 + getSpace(anslength)  + str2 +  getSpace(anslength2) +  getSpace(anslength3) + str3 ;
        return ans;
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

    private String PrintAmtDetails(String str1,String str2) {
        int fulllength = 42;
        int strlength = 15;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;


        String ans = str1 + getSpace(anslength) +  getSpace(anslength2) + str2  ;
        return ans;
    }

    private String PrintDetails(String str4,String str0,String str1, String str2,String str3) {
        int fulllength = 30;
        int strlength = 8;
        int strlength0 = 6;
        int strlength2 = 10;
        int strlength4 =3;

        int str4length = str4.length();
        int anslength4 = strlength4 - str4length;

        int str0length = str0.length();
        int anslength0 = strlength0 - str0length;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength2 - str3length;

        String ans =  str4 + getSpace(anslength4) + str1 + getSpace(anslength) + str0 + getSpace(anslength0) + str2  + getSpace(anslength2) + getSpace(anslength3) +  str3;
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
    private void printText2(String msg) {
        try {
            byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
            // Print normal text
            mmOutputStream.write(bb3);
            mmOutputStream.write(msg.getBytes());
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

        new AlertDialog.Builder(SalesOrderActivity.this)
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

        new AlertDialog.Builder(SalesOrderActivity.this)
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

    public void ProgressDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SalesOrderActivity.this);
        LayoutInflater inflater = SalesOrderActivity.this.getLayoutInflater();
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

    void returnactivity(){

        /*controller.PCNm = 1;

        if (controller.Prscl == 1){

            Intent IntentRouteScheduleActivity = new Intent(SalesOrderActivity.this, RouteScheduleActivity.class);
            startActivity(IntentRouteScheduleActivity);
            finish();

        }else if (controller.Prscl == 2){
            finish();
        }else if (controller.Prscl == 3){

            Intent IntentMainActivity = new Intent(SalesOrderActivity.this, MainActivity.class);
            startActivity(IntentMainActivity);
            finish();
        }else{
            controller.PCNm = 0;
            Intent IntentCustomerListActivity = new Intent(SalesOrderActivity.this, CustomerListActivity.class);
            startActivity(IntentCustomerListActivity);
            finish();
        }*/

        if (!controller.PCLName.equals(controller.fetchdbSettings().get(6) + "-CASH SALES")){
            if (!controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))){
                Intent intent = new Intent(SalesOrderActivity.this, CheckDisplayActivity.class);
                startActivity(intent);
                finish();
            }else{
                finish();
            }
        }else{
            finish();
        }




    }



    public void onBackPressed() {

        finish();

    }



}
