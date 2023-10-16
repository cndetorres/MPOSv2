package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.app.DatePickerDialog;
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class CustomerInventoryActivity extends Activity {

    DBController controller = new DBController(this);
    ListView CIDetails,CIHeader;
    ArrayList<HashMap<String, String>> hmCIHeader;
    ListAdapter laCIHeader,laCIDetails;
    HashMap<String, String> mCIHeader,mCIDetails;
    MaterialBetterSpinner mbsCIList;
    TextView tvCICust,tvDCIOk,tvDRINext,tvDCIEDelete,tvDCIESave,tvDCIEItem, tvDCIEStockItem,tvDCIEUnitItem,tvDCIEPriceItem,tvDCIETotalItem;
    BottomNavigationView btCINavigation;
    ImageView ivDCIList,ivDCICalendar;
    AutoCompleteTextView product;
    Button btnDCIScan,btnDCISub,btnDCIAdd,btnDCIESub,btnDCIEAdd;
    EditText qty,etDCIDate,etDCIEDate;
    final Calendar myCalendar = Calendar.getInstance();
    Integer intCIQty = 0;
    String Pproducts,PQty,PUnit,PExpDate;
    Integer Pposition;
    Context context = CustomerInventoryActivity.this;

    String datediff = "";

    boolean isClickedTwice;

    boolean isSkip;

    private long mLastClickTime = 0;

    List<HashMap<String, String>> CIViewCItems;
    List<String> CIListSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerinventory);

        CIDetails = (ListView) findViewById(R.id.lvCIDetails);
        CIHeader = (ListView) findViewById(R.id.lvCIHeader);

        btCINavigation = (BottomNavigationView) findViewById(R.id.btCINavigation);


        isClickedTwice = false;
        isSkip = false;

        ViewHeaderListview();

        //ViewDetailListview();

        CIDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laCIDetails.getItem(position);

                String objProducts = (String) obj.get("Item");
                Pproducts = objProducts;
                String objQty = (String) obj.get("Qty");
                PQty = objQty ;
                String objTotal = (String) obj.get("Unit");
                PUnit= objTotal;
                String objExpDate = (String) obj.get("Exp Date");
                PExpDate= objExpDate;
                Pposition = position;

                DialogEditItem();

            }
        });

        btCINavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mci_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();


                                DialogAddItem();


                                break;

                            case R.id.mci_done:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }

                                if (CIDetails.getCount() == 0){
                                    messagebox("nothing to inventory");
                                }else {

                                    controller.Prscl = 2;

                                    controller.PIsSOrder = 1;

                                    if (controller.PIsWlk == 1) {

                                        if (controller.fetchSalesH().equals("2") && controller.PCCode.equals("WLK" + controller.fetchdbSettings().get(6))) {
                                            isSkip = true;
                                            messagebox2("you have exceeded the transaction limit. This will skip Sales Order Activity.");
                                        } else {
                                            isSkip = false;
                                            messagebox2("Are you sure you want to save inventory below?");
                                        }

                                    } else {

                                        if (controller.PTerms.equals("COD")) {

                                            controller.dbLReturns = controller.fetchSUMAmtARBalancesCOD();
                                            controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1;

                                            if (controller.dbLReturns != 0.00) {
                                                controller.dbLReturns = controller.dbLReturns * -1;
                                            }

                                            isSkip = false;
                                            messagebox2("Are you sure you want to save inventory below?");
                                        } else {

                                            controller.PDiscount = Double.valueOf(controller.fetchCustomer().get(3)) * -1;

                                            if (controller.fetchLateAR().equals("")) {
                                                if (controller.fetchCreditExpo() == 0.00) {
                                                    controller.lesslimit = controller.PLimit;
                                                    isSkip = false;
                                                    messagebox2("Are you sure you want to save inventory below?");
                                                } else {
                                                    controller.lesslimit = controller.PLimit - controller.fetchCreditExpo();
                                                    isSkip = false;
                                                    messagebox2("Are you sure you want to save inventory below?");
                                                }

                                            } else {
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


                                                if (Integer.valueOf(controller.fetchCreditTerms().get(0)) < Integer.valueOf(datediff)) {

                                                    if (controller.fetchCreditExpo() == 0.00) {
                                                        controller.lesslimit = controller.PLimit;
                                                        isSkip = false;
                                                        messagebox2("Are you sure you want to save inventory below?");
                                                    } else if (controller.fetchSUMARBalances() == 0.00) {

                                                        if (controller.fetchCreditExpo() > 0.00) {
                                                            if (controller.fetchSUMAmtPdPaymentCheck() > 0.00) {
                                                                isSkip = true;
                                                                messagebox2("Customer has an overdue balance. This will skip Sales Order Activity.");
                                                            } else if (controller.fetchSUMAmtPdPaymentCashOD() == 0.00) {
                                                                isSkip = true;
                                                                messagebox2("Customer has an overdue balance. This will skip Sales Order Activity.");
                                                            } else {
                                                                controller.lesslimit = controller.PLimit - controller.fetchCreditExpo();
                                                                isSkip = false;
                                                                messagebox2("Are you sure you want to save inventory below?");
                                                            }

                                                        } else {
                                                            controller.lesslimit = controller.PLimit - controller.fetchCreditExpo();
                                                            isSkip = false;
                                                            messagebox2("Are you sure you want to save inventory below?");
                                                        }
                                                    } else {
                                                        isSkip = true;
                                                        messagebox2("Customer has an overdue balance.This will skip Sales Order Activity.");
                                                    }

                                                } else {

                                                    if (!controller.fetchLateAR().equals("")) {

                                                        controller.lesslimit = controller.PLimit - controller.fetchCreditExpo();
                                                        isSkip = false;
                                                        messagebox2("Are you sure you want to save inventory below?");
                                                    } else {

                                                        controller.lesslimit = controller.PLimit;
                                                        isSkip = false;
                                                        messagebox2("Are you sure you want to save inventory below?");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }



                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmCIHeader = new ArrayList<HashMap<String, String>>();
        mCIHeader = new HashMap<String, String>();
        CIViewCItems = new ArrayList<HashMap<String, String>>();

        mCIHeader.put("Item", "ITEM");
        mCIHeader.put("Qty", "QTY");
        mCIHeader.put("Unit", "UNIT");
        mCIHeader.put("Exp Date", "EXP DATE");
        hmCIHeader.add(mCIHeader);

        try {
            laCIHeader = new SimpleAdapter(this, hmCIHeader, R.layout.item_customerinventory,
                    new String[]{"Item", "Qty", "Unit", "Exp Date"}, new int[]{
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

            CIHeader.setAdapter(laCIHeader);
        } catch (Exception e) {

        }
    }


    public void AddDetailListview(){


        mCIDetails = new HashMap<String, String>();


        mCIDetails.put("Item",Pproducts);
        mCIDetails.put("Qty", PQty);
        mCIDetails.put("Unit", PUnit);
        mCIDetails.put("Exp Date", PExpDate);
        CIViewCItems.add(mCIDetails);

        try {
            laCIDetails = new SimpleAdapter(this, CIViewCItems, R.layout.item_customerinventory,
                    new String[]{"Item", "Qty", "Unit", "Exp Date"}, new int[]{
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
            CIDetails.setAdapter(laCIDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateDetailListview(){

        mCIDetails = new HashMap<String, String>();

        mCIDetails.put("Item",Pproducts);
        mCIDetails.put("Qty", PQty);
        mCIDetails.put("Unit", PUnit);
        mCIDetails.put("Exp Date", PExpDate);
        CIViewCItems.set(Pposition,mCIDetails);

        try {
            laCIDetails = new SimpleAdapter(this, CIViewCItems, R.layout.item_customerinventory,
                    new String[]{"Item", "Qty", "Unit", "Exp Date"}, new int[]{
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
            CIDetails.setAdapter(laCIDetails);
        } catch (Exception e) {

        }
    }

    public void DeleteDetailListView(){

        mCIDetails = new HashMap<String, String>();

        mCIDetails.put("Item",Pproducts);
        mCIDetails.put("Qty", PQty);
        mCIDetails.put("Unit", PUnit);
        mCIDetails.put("Exp Date", PExpDate);
        CIViewCItems.remove(mCIDetails);

        try {
            laCIDetails = new SimpleAdapter(this, CIViewCItems, R.layout.item_customerinventory,
                    new String[]{"Item", "Qty", "Unit", "Exp Date"}, new int[]{
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
            CIDetails.setAdapter(laCIDetails);
        } catch (Exception e) {

        }
    }

    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CustomerInventoryActivity.this);
        LayoutInflater inflater = CustomerInventoryActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additemreturneditem, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        ivDCIList = (ImageView) dialogView.findViewById(R.id.ivDRIList);
        ivDCICalendar = (ImageView) dialogView.findViewById(R.id.ivBICalendar);
        product = (AutoCompleteTextView) dialogView.findViewById(R.id.acDRIProduct);
        btnDCIScan = (Button) dialogView.findViewById(R.id.btnDRIScan);
        btnDCIAdd = (Button) dialogView.findViewById(R.id.btnDRIAdd);
        btnDCISub = (Button) dialogView.findViewById(R.id.btnDRISub);
        qty = (EditText) dialogView.findViewById(R.id.etDRIQty);
        etDCIDate = (EditText) dialogView.findViewById(R.id.etBIDate);
        tvDCIOk = (TextView) dialogView.findViewById(R.id.tvDRIOk);
        tvDRINext = (TextView) dialogView.findViewById(R.id.tvDRINext);

        final String products[] = controller.fetchCINameMaterials();

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

        product.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                qty.requestFocus();
            }
        });

        ivDCIList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                product.showDropDown();
                //btnDRIScan.setVisibility(View.GONE);
            }
        });

        btnDCIScan.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                new IntentIntegrator(CustomerInventoryActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });

        btnDCIAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else {
                    intCIQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intCIQty.toString());
                }

            }
        });

        btnDCISub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox( "invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intCIQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intCIQty.toString());
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
                etDCIDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        ivDCICalendar.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                btnDCIScan.setVisibility(View.VISIBLE);
                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog2 =
                        new DatePickerDialog(CustomerInventoryActivity.this, datePickerListener2, mYear, mMonth, mDay);

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

        tvDCIOk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                if (product.getText().toString().equals("") && qty.getText().toString().equals("") && etDCIDate.getText().toString().equals("")){
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
                    }else if (etDCIDate.getText().toString().equals("")){
                        messagebox("please add expiration date");
                    }else if (qty.getText().toString().equals("0")){
                        //messagebox("quantity must not be zero(0)");
                        isClickedTwice = false;
                        alertDialog.dismiss();
                    }else{
                        controller.PMName = product.getText().toString();
                        Pproducts =  controller.PMName;
                        PQty = qty.getText().toString();

                        try {


                            PExpDate = etDCIDate.getText().toString();
                            PUnit = controller.fetchUnit(product.getText().toString());

                            if (CIDetails.getCount() == 0){
                                AddDetailListview();
                                //computeTotalAmt();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                                isClickedTwice = false;
                                alertDialog.dismiss();
                            }else{
                                final ArrayList<String> alADetails = new ArrayList<String>();
                                alADetails.clear();
                                for (int i = 0; i < CIDetails.getAdapter().getCount(); i++) {
                                    HashMap<String, Object> obj = (HashMap<String, Object>) laCIDetails.getItem(i);

                                    String objProducts = (String) obj.get("Item");
                                    String objRemarks = (String) obj.get("Remarks");
                                    alADetails.add(objProducts+objRemarks);
                                }
                                if (alADetails.contains(Pproducts+etDCIDate.getText().toString())){
                                    messagebox(Pproducts  + "  already in the list");
                                }else{
                                    AddDetailListview();
                                    //computeTotalAmt();
                                    isClickedTwice = false;
                                    alertDialog.dismiss();
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                    Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                                }
                            }

                        } catch (Exception e) {
                            Toasty.error(getApplicationContext(),e.getMessage().toString(), Toast.LENGTH_LONG).show();
                            product.setText("");
                            qty.setText("");
                            etDCIDate.setText("");
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
                }else if (etDCIDate.getText().toString().equals("")){
                    messagebox("please add expiration date");
                }else if (qty.getText().toString().equals("0")){
                    messagebox("quantity must not be zero(0)");
                }else{
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    PQty = qty.getText().toString();

                    try {
                        PExpDate = etDCIDate.getText().toString();
                        PUnit = controller.fetchUnit(product.getText().toString());

                        if (CIDetails.getCount() == 0){
                            AddDetailListview();
                            //computeTotalAmt();
                            product.getText().clear();
                            qty.getText().clear();
                            etDCIDate.getText().clear();
                            product.requestFocus();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                            Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                        }else{
                            final ArrayList<String> alADetails = new ArrayList<String>();
                            alADetails.clear();
                            for (int i = 0; i < CIDetails.getAdapter().getCount(); i++) {
                                HashMap<String, Object> obj = (HashMap<String, Object>) laCIDetails.getItem(i);

                                String objProducts = (String) obj.get("Item");
                                String objRemarks = (String) obj.get("Remarks");
                                alADetails.add(objProducts+objRemarks);
                            }
                            if (alADetails.contains(Pproducts+etDCIDate.getText().toString())){
                                messagebox(Pproducts  + "  already in the list");
                            }else{
                                AddDetailListview();
                                product.getText().clear();
                                qty.getText().clear();
                                etDCIDate.getText().clear();
                                product.requestFocus();
                                //computeTotalAmt();
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
                        etDCIDate.setText("");
                        product.requestFocus();
                    }

                }
            }
        });

        product.requestFocus();

        alertDialog.show();

    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CustomerInventoryActivity.this);
        LayoutInflater inflater = CustomerInventoryActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_editcustomerinventory, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        tvDCIEDelete = (TextView) dialogView.findViewById(R.id.tvDCIEDelete);
        tvDCIESave = (TextView) dialogView.findViewById(R.id.tvDCIESave);
        btnDCIEAdd = (Button) dialogView.findViewById(R.id.btnDCIEAdd);
        btnDCIESub = (Button) dialogView.findViewById(R.id.btnDCIESub);
        tvDCIEItem = (TextView) dialogView.findViewById(R.id.tvDCIEItem);
        tvDCIEUnitItem = (TextView) dialogView.findViewById(R.id.tvDCIEUnitItem);
        final EditText qty =(EditText)dialogView.findViewById(R.id.etDCIEQty);
        etDCIEDate = (EditText) dialogView.findViewById(R.id.etDCIEDate);
        ImageView ivDRIECalendar = (ImageView) dialogView.findViewById(R.id.ivDCIECalendar);

        tvDCIEItem.setText(Pproducts);
        //tvDCIETotalItem.setText(PTotal);
        tvDCIEUnitItem.setText(PUnit);
        qty.setText(PQty);
        etDCIEDate.setText(PExpDate);

        qty.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
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

        controller.PMName = tvDCIEItem.getText().toString();

        btnDCIEAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else {
                    intCIQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intCIQty.toString());
                }

            }
        });

        btnDCIESub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intCIQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intCIQty.toString());
                }

            }
        });

        tvDCIEDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                new AlertDialog.Builder(CustomerInventoryActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Close")
                        .setMessage("Are you sure you want to delete " + Pproducts +" ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Toasty.error(getApplicationContext(),  Pproducts + " successfully deleted", Toast.LENGTH_LONG).show();
                                DeleteDetailListView();
                                //computeTotalAmt();
                                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                                alertDialog.dismiss();

                            }

                        })
                        .setNegativeButton("No", null)
                        .show();


            }
        });

        tvDCIESave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0) {
                    messagebox("quantity must not be zero(0)");
                }else{
                    Pproducts = tvDCIEItem.getText().toString();
                    PQty = qty.getText().toString();

                    PExpDate = etDCIEDate.getText().toString();

                    UpdateDetailListview();

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
                etDCIEDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        ivDRIECalendar.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                btnDCIScan.setVisibility(View.VISIBLE);
                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog2 =
                        new DatePickerDialog(CustomerInventoryActivity.this, datePickerListener2, mYear, mMonth, mDay);

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
            Toasty.error(this, "Material not found", Toast.LENGTH_LONG).show();
        }else{
            product.setText(controller.FetchNameMaterials().get(0));
            btnDCIScan.setVisibility(View.VISIBLE);
            qty.requestFocus();
        }


    }

    public void computeTotalAmt(){

        controller.dbTotalAmt = 0.00;

        for (int i = 0; i < CIDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCIDetails.getItem(i);
            String objAmt = (String) obj.get("Total");
            controller.dbTotalAmt = Double.parseDouble(objAmt.replace(",","")) + controller.dbTotalAmt;
        }
    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(CustomerInventoryActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Customer Inventory")
                .setMessage(alerttext)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        /*DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                        Calendar defaultDate = Calendar.getInstance();
                        String logDate;
                        logDate = defaultDateFormat2.format(defaultDate.getTime());
                        controller.insertCustomerLogsItem(controller.fetchLogID(),3,logDate,1);*/

                        dialog.dismiss();

                       /* Intent intent = new Intent(context, CheckDisplayActivity.class);
                        startActivity(intent);
                        finish();*/

                    }

                })
                .show();
    }
    void  messagebox2(String alerttext) {

        new AlertDialog.Builder(CustomerInventoryActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Customer Inventory")
                .setMessage(alerttext)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String inventoryID;
                        controller.PCName = "Id";
                        controller.PTName = "CustomerInventory";
                        controller.PMNumber = controller.fetchMaxNumTCTSequence();
                        CIListSettings = controller.fetchdbSettings();
                        DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
                        String strDate = df.format(Calendar.getInstance().getTime());
                        inventoryID =  strDate + CIListSettings.get(1) + controller.PMNumber;

                        DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                        Calendar defaultDate = Calendar.getInstance();
                        String logDate;
                        logDate = defaultDateFormat2.format(defaultDate.getTime());
                        Timestamp tsRIDateTime = new Timestamp(System.currentTimeMillis());

                        controller.insertCustomerInventory(controller.PCCode,logDate,inventoryID,controller.PUName);

                        for (int i = 0; i < CIViewCItems.size(); i++) {
                            controller.PMName = CIViewCItems.get(i).get("Item");
                            PQty = CIViewCItems.get(i).get("Qty");
                            PExpDate = CIViewCItems.get(i).get("Exp Date");
                            controller.insertCustomerInventoryItem(inventoryID,controller.fetchMCodeMaterials(),PQty,PExpDate);
                        }

                        controller.updateTableSequence(Integer.valueOf(controller.PMNumber) + 1);
                        controller.insertTransaction("CI" + inventoryID, "CI", controller.PCLName, controller.PCCode, tsRIDateTime.getTime(), 0.00);

                        if (isSkip){

                            controller.insertCustomerLogsItem(controller.fetchLogID(),3,logDate,1);
                            dialog.dismiss();

                            Intent intent = new Intent(context, CheckDisplayActivity.class);
                            startActivity(intent);
                            finish();
                        }else{

                            controller.insertCustomerLogsItem(controller.fetchLogID(),3,logDate,0);

                            dialog.dismiss();

                            Intent intent = new Intent(context, SalesOrderActivity.class);
                            startActivity(intent);
                            finish();
                        }





                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public static long calculateDays(Date dateEarly, Date dateLater) {
        return (dateLater.getTime() - dateEarly.getTime()) / (24 * 60 * 60 * 1000);
    }

    public void onBackPressed() {

        finish();
    }

}


