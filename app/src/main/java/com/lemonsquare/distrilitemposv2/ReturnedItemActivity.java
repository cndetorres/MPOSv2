package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class ReturnedItemActivity extends Activity {

    DBController controller = new DBController(this);
    ListView RIDetails,RIHeader;
    ArrayList<HashMap<String, String>> hmRIHeader;
    ListAdapter laRIHeader,laRIDetails;
    HashMap<String, String> mRIHeader,mRIDetails;
    MaterialBetterSpinner mbsRIList;
    TextView tvRICust,tvDRIOk,tvDRINext,tvDRIEDelete,tvDRIESave,tvDRIEItem, tvDRIEStockItem,tvDRIEUnitItem,tvDRIEPriceItem,tvDRIETotalItem;
    BottomNavigationView btRINavigation;
    ImageView ivDRIList,ivDRICalendar;
    AutoCompleteTextView product;
    Button btnDRIScan,btnDRISub,btnDRIAdd,btnDRIESub,btnDRIEAdd;
    EditText qty,etDRIDate,etDRIEDate;
    final Calendar myCalendar = Calendar.getInstance();
    Integer intRIQty = 0;
    String Pproducts,PQty,PTotal,PRemarks;
    double PRITotal = 0.00;
    Double intRITPrice = 0.00;
    DecimalFormat RIAmt = new DecimalFormat("#,##0.00");
    Integer Pposition;
    List<String> RIListSettings;
    ArrayList<String> alPrintReturnsH,alPrintReturnsD;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_returneditem);

        tvRICust = (TextView) findViewById(R.id.tvRICust);
        RIDetails = (ListView) findViewById(R.id.lvRIDetails);
        RIHeader = (ListView) findViewById(R.id.lvRIHeader);
        mbsRIList = (MaterialBetterSpinner) findViewById(R.id.spRIList);
        btRINavigation = (BottomNavigationView) findViewById(R.id.btRINavigation);

        readprintername();
        isClickedTwice = false;

        tvRICust.setText(controller.PCLName);


        String[] strRCategory = controller.fetchNameRetTypeReturnType();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, strRCategory);
        mbsRIList.setAdapter(arrayAdapter);

        ViewHeaderListview();

        RIListSettings = controller.fetchdbSettings();

        if (controller.PRItem == 0){
            controller.RIViewRItems = controller.fetchNull();
            mbsRIList.setText("ZCR-Bad Order");
        }else if (controller.PRItem == 1){
            mbsRIList.setText(controller.PRType);
        }else{
            mbsRIList.setText(controller.PRType);

            printdialog();

        }

        ViewDetailListview();

        RIDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laRIDetails.getItem(position);

                String objProducts = (String) obj.get("Item");
                Pproducts = objProducts;
                String objQty = (String) obj.get("Qty");
                PQty = objQty ;
                String objTotal = (String) obj.get("Total");
                PTotal= objTotal;
                String objRemarks = (String) obj.get("Remarks");
                PRemarks= objRemarks;
                Pposition = position;

                DialogEditItem();

            }
        });

        btRINavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mri_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();


                                    DialogAddItem();


                                break;

                            case R.id.mri_done:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }

                                if (mbsRIList.getText().toString().equals("")){
                                    mbsRIList.setError("please select return type");
                                }else if (RIDetails.getCount() == 0){
                                    messagebox2("nothing to return. Do you want to proceed to the next activity?");
                                }else{
                                    controller.PINum = 3;
                                    controller.PRItem = 1;
                                    controller.PRType = mbsRIList.getText().toString();
                                    Intent IntentConfirmTransactionActivity = new Intent(ReturnedItemActivity.this, ConfirmTransactionActivity.class);
                                    startActivity(IntentConfirmTransactionActivity);
                                    finish();
                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmRIHeader = new ArrayList<HashMap<String, String>>();
        mRIHeader = new HashMap<String, String>();

        mRIHeader.put("Item", "ITEM");
        mRIHeader.put("Qty", "QTY");
        mRIHeader.put("Total", "TOTAL");
        mRIHeader.put("Remarks", "REMARKS");
        hmRIHeader.add(mRIHeader);

        try {
            laRIHeader = new SimpleAdapter(this, hmRIHeader, R.layout.item_returneditem,
                    new String[]{"Item", "Qty", "Total", "Remarks"}, new int[]{
                    R.id.rowsItem, R.id.rowsQty, R.id.rowsTotal, R.id.rowsRemarks}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView rqty = (TextView) view.findViewById(R.id.rowsQty);
                    TextView rtotal = (TextView) view.findViewById(R.id.rowsTotal);
                    TextView rremarks = (TextView) view.findViewById(R.id.rowsRemarks);
                    if (position % 2 == 0) {
                        ritem.setTextColor(Color.WHITE);
                        rqty.setTextColor(Color.WHITE);
                        rtotal.setTextColor(Color.WHITE);
                        rremarks.setTextColor(Color.WHITE);
                        ritem.setTypeface(null, Typeface.BOLD);
                        rqty.setTypeface(null, Typeface.BOLD);
                        rtotal.setTypeface(null, Typeface.BOLD);
                        rremarks.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            RIHeader.setAdapter(laRIHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laRIDetails = new SimpleAdapter(this, controller.RIViewRItems, R.layout.item_returneditem,
                    new String[]{"Item", "Qty", "Total", "Remarks"}, new int[]{
                    R.id.rowsItem, R.id.rowsQty, R.id.rowsTotal, R.id.rowsRemarks}) {
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
            RIDetails.setAdapter(laRIDetails);
        } catch (Exception e) {

        }
    }

    public void AddDetailListview(){

        mRIDetails = new HashMap<String, String>();

        mRIDetails.put("Item",Pproducts);
        mRIDetails.put("Qty", PQty);
        mRIDetails.put("Total", PTotal);
        mRIDetails.put("Remarks", PRemarks);
        controller.RIViewRItems.add(mRIDetails);

        try {
            laRIDetails = new SimpleAdapter(this, controller.RIViewRItems, R.layout.item_returneditem,
                    new String[]{"Item", "Qty", "Total", "Remarks"}, new int[]{
                    R.id.rowsItem, R.id.rowsQty, R.id.rowsTotal, R.id.rowsRemarks}) {
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
            RIDetails.setAdapter(laRIDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateDetailListview(){

        mRIDetails = new HashMap<String, String>();

        mRIDetails.put("Item",Pproducts);
        mRIDetails.put("Qty", PQty);
        mRIDetails.put("Total", PTotal);
        mRIDetails.put("Remarks", PRemarks);
        controller.RIViewRItems.set(Pposition,mRIDetails);

        try {
            laRIDetails = new SimpleAdapter(this, controller.RIViewRItems, R.layout.item_returneditem,
                    new String[]{"Item", "Qty", "Total", "Remarks"}, new int[]{
                    R.id.rowsItem, R.id.rowsQty, R.id.rowsTotal, R.id.rowsRemarks}) {
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
            RIDetails.setAdapter(laRIDetails);
        } catch (Exception e) {

        }
    }

    public void DeleteDetailListView(){

        mRIDetails = new HashMap<String, String>();

        mRIDetails.put("Item",Pproducts);
        mRIDetails.put("Qty", PQty);
        mRIDetails.put("Total", PTotal);
        mRIDetails.put("Remarks", PRemarks);
        controller.RIViewRItems.remove(mRIDetails);

        try {
            laRIDetails = new SimpleAdapter(this, controller.RIViewRItems, R.layout.item_returneditem,
                    new String[]{"Item", "Qty", "Total", "Remarks"}, new int[]{
                    R.id.rowsItem, R.id.rowsQty, R.id.rowsTotal, R.id.rowsRemarks}) {
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
            RIDetails.setAdapter(laRIDetails);
        } catch (Exception e) {

        }
    }

    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ReturnedItemActivity.this);
        LayoutInflater inflater = ReturnedItemActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additemreturneditem, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        ivDRIList = (ImageView) dialogView.findViewById(R.id.ivDRIList);
        ivDRICalendar = (ImageView) dialogView.findViewById(R.id.ivBICalendar);
        product = (AutoCompleteTextView) dialogView.findViewById(R.id.acDRIProduct);
        btnDRIScan = (Button) dialogView.findViewById(R.id.btnDRIScan);
        btnDRIAdd = (Button) dialogView.findViewById(R.id.btnDRIAdd);
        btnDRISub = (Button) dialogView.findViewById(R.id.btnDRISub);
        qty = (EditText) dialogView.findViewById(R.id.etDRIQty);
        etDRIDate = (EditText) dialogView.findViewById(R.id.etBIDate);
        tvDRIOk = (TextView) dialogView.findViewById(R.id.tvDRIOk);
        tvDRINext = (TextView) dialogView.findViewById(R.id.tvDRINext);

        final String products[] = controller.fetchMNameMaterials();

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
                    btnDRIScan.setVisibility(View.VISIBLE);
                }else{
                    btnDRIScan.setVisibility(View.GONE);
                }
            }

        });

        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                btnDRIScan.setVisibility(View.VISIBLE);
                qty.requestFocus();
            }
        });

        if(qty.requestFocus()) {
            btnDRIScan.setVisibility(View.VISIBLE);
        }*/

        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                qty.requestFocus();
            }
        });

        ivDRIList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                product.showDropDown();
                //btnDRIScan.setVisibility(View.GONE);
            }
        });

        btnDRIScan.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                new IntentIntegrator(ReturnedItemActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });

        btnDRIAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else {
                    intRIQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intRIQty.toString());
                }

            }
        });

        btnDRISub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox( "invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intRIQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intRIQty.toString());
                }

            }
        });

        final DatePickerDialog.OnDateSetListener datePickerListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                etDRIDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        ivDRICalendar.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                btnDRIScan.setVisibility(View.VISIBLE);
                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog2 =
                        new DatePickerDialog(ReturnedItemActivity.this, datePickerListener2, mYear, mMonth, mDay);

                Date today = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(today);
                c.add( Calendar.YEAR, - 1 );
                long minDate = c.getTime().getTime();

                Date otherday = new Date();
                Calendar d = Calendar.getInstance();
                d.setTime(otherday);
                d.add( Calendar.YEAR, + 1 );
                long maxDate = d.getTime().getTime();

                datePickerDialog2.getDatePicker().setMinDate(minDate);
                datePickerDialog2.getDatePicker().setMaxDate(maxDate);
                datePickerDialog2.show();
            }
        });

        tvDRIOk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                if (product.getText().toString().equals("") && qty.getText().toString().equals("") && etDRIDate.getText().toString().equals("")){
                    isClickedTwice = false;
                    alertDialog.dismiss();
                }else{
                    if (product.getText().toString().equals("")){
                        //messagebox("please select product");
                        isClickedTwice = false;
                        alertDialog.dismiss();
                    }else if (!productlist.contains(product.getText().toString())){
                        messagebox("product not in list");
                    }else if(qty.getText().toString().equals("")){
                        //messagebox("invalid quantity");
                        isClickedTwice = false;
                        alertDialog.dismiss();
                    }else if (etDRIDate.getText().toString().equals("")){
                        messagebox("please add remarks");
                    }else if (qty.getText().toString().equals("0")){
                        //messagebox("quantity must not be zero(0)");
                        isClickedTwice = false;
                        alertDialog.dismiss();
                    }else{
                        controller.PMName = product.getText().toString();
                        Pproducts =  controller.PMName;
                        PQty = qty.getText().toString();

                        try {
                        PRITotal = Double.valueOf(PQty) * Double.valueOf(controller.fetchReturnUnitQtyPriceMaterial().get(2));
                        PTotal = String.valueOf(RIAmt.format(PRITotal));

                            PRemarks = etDRIDate.getText().toString();

                            if (RIDetails.getCount() == 0){
                                AddDetailListview();
                                computeTotalAmt();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                                isClickedTwice = false;
                                alertDialog.dismiss();
                            }else{
                                final ArrayList<String> alADetails = new ArrayList<String>();
                                alADetails.clear();
                                for (int i = 0; i < RIDetails.getAdapter().getCount(); i++) {
                                    HashMap<String, Object> obj = (HashMap<String, Object>) laRIDetails.getItem(i);

                                    String objProducts = (String) obj.get("Item");
                                    String objRemarks = (String) obj.get("Remarks");
                                    alADetails.add(objProducts+objRemarks);
                                }
                                if (alADetails.contains(Pproducts+etDRIDate.getText().toString())){
                                    messagebox(Pproducts  + "  already in the list");
                                }else{
                                    AddDetailListview();
                                    computeTotalAmt();
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
                            etDRIDate.setText("");
                            product.requestFocus();
                        }




                    }
                }
            }
        });

        tvDRINext.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                    if (product.getText().toString().equals("")){
                        messagebox("please select product");
                    }else if (!productlist.contains(product.getText().toString())){
                        messagebox("product not in list");
                    }else if(qty.getText().toString().equals("")){
                        messagebox("invalid quantity");
                    }else if (etDRIDate.getText().toString().equals("")){
                        messagebox("please add remarks");
                    }else if (qty.getText().toString().equals("0")){
                        messagebox("quantity must not be zero(0)");
                    }else{
                        controller.PMName = product.getText().toString();
                        Pproducts =  controller.PMName;
                        PQty = qty.getText().toString();

                        try {
                            PRITotal = Double.valueOf(PQty) * Double.valueOf(controller.fetchReturnUnitQtyPriceMaterial().get(2).replace(",",""));
                            PTotal = String.valueOf(RIAmt.format(PRITotal));

                            PRemarks = etDRIDate.getText().toString();

                            if (RIDetails.getCount() == 0){
                                AddDetailListview();
                                computeTotalAmt();
                                product.getText().clear();
                                qty.getText().clear();
                                etDRIDate.getText().clear();
                                product.requestFocus();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                            }else{
                                final ArrayList<String> alADetails = new ArrayList<String>();
                                alADetails.clear();
                                for (int i = 0; i < RIDetails.getAdapter().getCount(); i++) {
                                    HashMap<String, Object> obj = (HashMap<String, Object>) laRIDetails.getItem(i);

                                    String objProducts = (String) obj.get("Item");
                                    String objRemarks = (String) obj.get("Remarks");
                                    alADetails.add(objProducts+objRemarks);
                                }
                                if (alADetails.contains(Pproducts+etDRIDate.getText().toString())){
                                    messagebox(Pproducts  + "  already in the list");
                                }else{
                                    AddDetailListview();
                                    product.getText().clear();
                                    qty.getText().clear();
                                    etDRIDate.getText().clear();
                                    product.requestFocus();
                                    computeTotalAmt();
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                    Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                                }
                            }

                        } catch (Exception e) {
                            Toasty.error(getApplicationContext(),"no pricelist maintained in " + Pproducts , Toast.LENGTH_LONG).show();
                            //Toasty.error(getApplicationContext(),e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            product.setText("");
                            qty.setText("");
                            etDRIDate.setText("");
                            product.requestFocus();
                        }

                    }
                }
        });

        product.requestFocus();

        alertDialog.show();

    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ReturnedItemActivity.this);
        LayoutInflater inflater = ReturnedItemActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edititemreturneditem, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        tvDRIEDelete = (TextView) dialogView.findViewById(R.id.tvDRIEDelete);
        tvDRIESave = (TextView) dialogView.findViewById(R.id.tvDRIESave);
        btnDRIEAdd = (Button) dialogView.findViewById(R.id.btnDRIEAdd);
        btnDRIESub = (Button) dialogView.findViewById(R.id.btnDRIESub);
        tvDRIEItem = (TextView) dialogView.findViewById(R.id.tvDRIEItem);
        tvDRIEStockItem = (TextView) dialogView.findViewById(R.id.tvDRIEStockItem);
        tvDRIEUnitItem = (TextView) dialogView.findViewById(R.id.tvDRIEUnitItem);
        tvDRIEPriceItem = (TextView) dialogView.findViewById(R.id.tvDRIEPriceItem);
        tvDRIETotalItem = (TextView) dialogView.findViewById(R.id.tvDRIETotalItem);
        final EditText qty =(EditText)dialogView.findViewById(R.id.etDRIEQty);
        etDRIEDate = (EditText) dialogView.findViewById(R.id.etDRIEDate);
        ImageView ivDRIECalendar = (ImageView) dialogView.findViewById(R.id.ivDRIECalendar);

        tvDRIEItem.setText(Pproducts);
        tvDRIETotalItem.setText(PTotal);
        qty.setText(PQty);
        etDRIEDate.setText(PRemarks);

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

        controller.PMName = tvDRIEItem.getText().toString();

        tvDRIEUnitItem.setText(controller.fetchReturnUnitQtyPriceMaterial().get(0));
        tvDRIEStockItem.setText(controller.fetchReturnUnitQtyPriceMaterial().get(1));
        tvDRIEPriceItem.setText(controller.fetchReturnUnitQtyPriceMaterial().get(2));

        btnDRIEAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else {
                    intRIQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intRIQty.toString());
                    intRITPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvDRIEPriceItem.getText().toString().replace(",",""));
                    tvDRIETotalItem.setText(RIAmt.format(intRITPrice));
                }

            }
        });

        btnDRIESub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intRIQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intRIQty.toString());
                    intRITPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvDRIEPriceItem.getText().toString().replace(",",""));
                    tvDRIETotalItem.setText(RIAmt.format(intRITPrice));
                }

            }
        });

        tvDRIEDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                new AlertDialog.Builder(ReturnedItemActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Close")
                        .setMessage("Are you sure you want to delete " + Pproducts +" ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toasty.error(getApplicationContext(),  Pproducts + " successfully deleted", Toast.LENGTH_LONG).show();
                                DeleteDetailListView();
                                computeTotalAmt();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                alertDialog.dismiss();

                            }

                        })
                        .setNegativeButton("No", null)
                        .show();


            }
        });

        tvDRIESave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0) {
                    messagebox("quantity must not be zero(0)");
                }else{
                    Pproducts = tvDRIEItem.getText().toString();
                    PQty = qty.getText().toString();

                    intRITPrice = Integer.parseInt(qty.getText().toString()) * Double.parseDouble(tvDRIEPriceItem.getText().toString().replace(",",""));
                    tvDRIETotalItem.setText(RIAmt.format(intRITPrice));

                    PTotal = tvDRIETotalItem.getText().toString();
                    PRemarks = etDRIEDate.getText().toString();

                    UpdateDetailListview();

                    computeTotalAmt();

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                    Toasty.info(getApplicationContext(),  Pproducts + " successfully updated", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
            }
        });

        final DatePickerDialog.OnDateSetListener datePickerListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                etDRIEDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        ivDRIECalendar.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                btnDRIScan.setVisibility(View.VISIBLE);
                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog2 =
                        new DatePickerDialog(ReturnedItemActivity.this, datePickerListener2, mYear, mMonth, mDay);

                Date today = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(today);
                c.add( Calendar.YEAR, - 1 );
                long minDate = c.getTime().getTime();

                Date otherday = new Date();
                Calendar d = Calendar.getInstance();
                d.setTime(otherday);
                d.add( Calendar.YEAR, + 1 );
                long maxDate = d.getTime().getTime();

                datePickerDialog2.getDatePicker().setMinDate(minDate);
                datePickerDialog2.getDatePicker().setMaxDate(maxDate);
                datePickerDialog2.show();
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
            Toasty.error(this, "Material not found", Toast.LENGTH_LONG).show();
        }else{
            product.setText(controller.FetchNameMaterials().get(0));
            btnDRIScan.setVisibility(View.VISIBLE);
            qty.requestFocus();
        }


    }

    public void computeTotalAmt(){

        controller.dbTotalAmt = 0.00;

        for (int i = 0; i < RIDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laRIDetails.getItem(i);
            String objAmt = (String) obj.get("Total");
            controller.dbTotalAmt = Double.parseDouble(objAmt.replace(",","")) + controller.dbTotalAmt;
        }
    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(ReturnedItemActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Returned Item")
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
    void  messagebox2(String alerttext) {

        new AlertDialog.Builder(ReturnedItemActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Returned Item")
                .setMessage(alerttext)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                        Calendar defaultDate = Calendar.getInstance();
                        String logDate;
                        logDate = defaultDateFormat2.format(defaultDate.getTime());
                        controller.insertCustomerLogsItem(controller.fetchLogID(),2,logDate,1);

                        dialog.dismiss();

                        Intent intent = new Intent(ReturnedItemActivity.this, CustomerInventoryActivity.class);
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
            String title = "RETURN SLIP";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;
            String address,contactNo;
            address = getSpace(printHalfLen - (RIListSettings.get(13).length()/2)) + RIListSettings.get(13);
            contactNo = getSpace(printHalfLen - (RIListSettings.get(10).length()/2)) + RIListSettings.get(10);


            printCustom(companyName,3);
            printCustom( address,0);
            printCustom( contactNo,0);
            printNewLine();
            printCustom(title,3);
            printNewLine();

            alPrintReturnsH = controller.printReturnsH(controller.RICCode);

            //printText(leftMidRightAlign("Customer Code: " + alPrintReturnsH.get(0),"         " + alPrintReturnsH.get(1), ""));
            printText(WithDate("Customer Code: " + alPrintReturnsH.get(0),alPrintReturnsH.get(1)));
            printNewLine();
            printCustom("Customer: " + alPrintReturnsH.get(2),0);
            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1),0);
            printCustom("Terminal: " + RIListSettings.get(1),0);
            printCustom("ReturnID: " + controller.RICCode,0);
            printNewLine();

            printText(getLine(57));
            printNewLine();

            printCustom(alPrintReturnsH.get(4),0);

            double TotalAmtReturns = 0.00;


            for(int i = 1; i <= controller.fetchCountItem(controller.RICCode,"ReturnItem","RetId"); i++) {


                alPrintReturnsD = controller.printReturnsD(controller.RICCode,i);

                String item;
                item = alPrintReturnsD.get(1);
                if (item.length() > 35){
                    item = item.substring(0,35);
                }


                printText(PrintDetails(alPrintReturnsD.get(0),item, alPrintReturnsD.get(2), alPrintReturnsD.get(3)));

                TotalAmtReturns = TotalAmtReturns + Double.valueOf(alPrintReturnsD.get(3).replace(",",""));


                //printText(PrintDetails(controller.alSTItems.get(i), controller.alSTUnit.get(i), controller.alSTQty.get(i)));
                printNewLine();

                //Toasty.success(getApplicationContext(), String.valueOf(alPrintStockTransferD.size()), Toast.LENGTH_LONG).show();

            }

            printText(getLine(57));

            printNewLine();
            printText(PrintDetails("","Gross Amount","",String.valueOf(RIAmt.format(TotalAmtReturns))));
            printNewLine();

            double disc = 0.00;

            disc =  TotalAmtReturns - Double.valueOf(alPrintReturnsH.get(3).replace(",",""));

            if (disc != 0.00){
                printText(PrintDetails("","Discount","",String.valueOf(RIAmt.format(disc))));
                printNewLine();
            }


            printNewLine();
            printText(PrintDetails("","Net Amount","",alPrintReturnsH.get(3)));
            printNewLine();
            printNewLine();

            printCustom("SIGNATURE",0);

            printNewLine();

            Bitmap bmps = BitmapFactory.decodeByteArray(controller.fetchSignatureRItem(controller.RICCode), 0, controller.fetchSignatureRItem(controller.RICCode).length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmps, 250, 90, false);

            printImage(resizedbmp);


            printCustom("Signed by: " + alPrintReturnsH.get(5),0);
            printNewLine();


            String website = controller.fetchdbSettings().get(20);
            if (website.equals("")){
                website = "";
            }else{
                website = getSpace(printHalfLen - (website.length()/2)) + website;
            }
            String footerTitle = "CUSTOMER SERVICE";
            String footerContactNo = RIListSettings.get(12);
            String forOrders = "For Orders please contact";
            String orderContactNo = RIListSettings.get(11);
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

    private String PrintDetails(String str0,String str1, String str2,String str3) {
        int fulllength = 36;
        int strlength = 8;
        int strlength0 = 4;
        int strlength1 = 9;

        int str0length = str0.length();
        int anslength0 = strlength0 - str0length;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength1 - str3length;

        String ans = str0 + getSpace(anslength0) + str1 + getSpace(anslength) + str2  + getSpace(anslength2) + getSpace(anslength3) +  str3;
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

        new AlertDialog.Builder(ReturnedItemActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setTitle("Print Receipt")
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

                        dialog.dismiss();

                        Intent intent = new Intent(ReturnedItemActivity.this, CustomerInventoryActivity.class);
                        startActivity(intent);
                        finish();
                        closeBT();

                    }
                })
                .show();

    }

    void reprintdialog(){

        new AlertDialog.Builder(ReturnedItemActivity.this)
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

                        dialog.dismiss();

                        Intent intent = new Intent(ReturnedItemActivity.this, CustomerInventoryActivity.class);
                        startActivity(intent);
                        finish();
                        closeBT();

                    }
                })
                .show();

    }

    public void ProgressDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ReturnedItemActivity.this);
        LayoutInflater inflater = ReturnedItemActivity.this.getLayoutInflater();
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

        /*controller.PCNm = 1;

        if (controller.Prscl == 1){

            Intent IntentRouteScheduleActivity = new Intent(ReturnedItemActivity.this, RouteScheduleActivity.class);
            startActivity(IntentRouteScheduleActivity);
            finish();

        }else if (controller.Prscl == 2){
            finish();
        }else{

            Intent IntentMainActivity = new Intent(ReturnedItemActivity.this, MainActivity.class);
            startActivity(IntentMainActivity);
            finish();
        }*/

        finish();
    }

    public void onBackPressed() {

        finish();
    }

}


