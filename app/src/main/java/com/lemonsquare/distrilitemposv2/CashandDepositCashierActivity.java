package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import es.dmoral.toasty.Toasty;


public class CashandDepositCashierActivity extends Activity {


    DBController controller = new DBController(this);
    ListView COHDetails,COHHeader,CDDetails,CDHeader;
    ArrayList<HashMap<String, String>> hmCOHHeader,hmCDHeader;
    ListAdapter laCOHHeader,laCOHDetails,laCDHeader,laCDDetails;
    HashMap<String, String> mCOHHeader,mCOHDetail,mCDHeader,mCDDetail;
    BottomNavigationView COHmenu,CDmenu;
    String[] sCOHDenomination = new String[]{"1000","500","200","100","50","20"};
    EditText etCOHCoins,etDADAccntno,etDADAmt,etDADDate,etDADTime;
    TextView tvCOHTotal,tvCOHDenomination,tvCOHCancel,tvCOHSave,tvCDTotalItem,tvDADCancel,tvDADAccept,tvDADTitle,tvDCADCancel,tvDCADSignOff,tvDADDelete;
    MaterialBetterSpinner mbsDADBank;
    TabHost tabHost;
    String PDenomination,Pqty,PAmt,PBCode,PBank,PAcctNo,PBranch,PDeposited,PDate,PTime;
    int Pposition,PDADValue;
    Button btnCOHAdd,btnCOHSub,btnDADDate,btnDADTime;
    Integer intCOHQty = 0;
    Integer intCOHTDenomination = 0;
    Integer COHTAmt = 0;
    Double dbCOHCoins = 0.00;
    Double dbCOHTCoins = 0.00;
    Double dbCOHTotal = 0.00;
    Double dbCOHTotalCoins;
    Double dbCDTotalAmt = 0.00;
    Double dbCOHTotalAmt = 0.00;
    Double dbCOHTAmtCoins;
    Double InitialTotalCash;
    DecimalFormat CADAmt,CADCAmt;
    final Calendar myCalendar = Calendar.getInstance();
    boolean changing = false;
    Double changevalue = 0.00;
    Double OriginalAmtValue = 0.00;
    int isOpen = 0;

    private long mLastClickTime = 0;
    final DecimalFormat COHAmt = new DecimalFormat("#.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashanddepositcashier);

        COHHeader = (ListView) findViewById(R.id.lvCOHHeader);
        COHDetails = (ListView) findViewById(R.id.lvCOHDetails);
        COHmenu = (BottomNavigationView) findViewById(R.id.btCOHNavigation);
        etCOHCoins = (EditText) findViewById(R.id.etCOHCoins);
        tvCOHTotal = (TextView) findViewById(R.id.tvCOHTotalItem);
        CDHeader = (ListView) findViewById(R.id.lvCDHeader);
        CDDetails = (ListView) findViewById(R.id.lvCDDetails);
        CDmenu = (BottomNavigationView) findViewById(R.id.btCDNavigation);
        tvCDTotalItem = (TextView) findViewById(R.id.tvCDTotalItem);

        tabHost = (TabHost) findViewById(R.id.tabCADHost);
        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("CASH ON HAND");
        spec.setContent(R.id.tabCADCOHand);
        spec.setIndicator("CASH ON HAND");
        tabHost.addTab(spec);

        ViewCOHHeaderListview();
        ViewCOHDetailListview();

        dbCOHTotalCoins = Double.valueOf(COHAmt.format(controller.fetchSUMPaymentItem())) - controller.fetchSUMCashDeposited();

        CADAmt = new DecimalFormat("#,###,##0.00");
        CADCAmt = new DecimalFormat("######0.00");


        etCOHCoins.setText(String.valueOf(controller.fetchCashOnHandCashier().get(6)));

        computeCashOnHand();

        etCOHCoins.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                computeCashOnHand();
            }

        });

        COHDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(position);

                String objDenomination = (String) obj.get("Denomination");
                PDenomination = objDenomination;
                String objQty = (String) obj.get("Qty");
                Pqty = objQty ;
                String objAmt = (String) obj.get("Amt");
                PAmt= objAmt;
                Pposition = position;

                DialogCOHDenomination();

            }
        });

        CDDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(position);

                String objBank = (String) obj.get("Bank");
                PBank = objBank;
                String objAcctNo = (String) obj.get("AcctNo");
                PAcctNo = objAcctNo ;
                String objBranch = (String) obj.get("Branch");
                PBranch= objBranch;
                String objAmt = (String) obj.get("Amt");
                PAmt= objAmt;
                String objDateDeposited = (String) obj.get("DateDeposited");
                PDeposited= objDateDeposited;
                Pposition = position;

                PDADValue = 2;
                AddEditDeposits();

            }
        });


        COHmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcoh_next:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                controller.PIndicator = 0;

                                controller.PValidatedTotal = Double.valueOf(tvCOHTotal.getText().toString().replace(",","")) + Double.valueOf(tvCDTotalItem.getText().toString().replace(",",""));

                                controller.PCashTotal = InitialTotalCash -  controller.PValidatedTotal;


                                if (etCOHCoins.getText().toString().equals("")){
                                    controller.dbPCoins = "0.00";
                                }else{
                                    controller.dbPCoins = etCOHCoins.getText().toString().replace(",","");
                                }



                                Intent IntentChecksandSummary = new Intent(CashandDepositCashierActivity.this, ChecksandSummaryActivity.class);
                                startActivity(IntentChecksandSummary);
                                finish();

                                break;


                        }
                        return true;
                    }
                });


        //Tab 2
        spec = tabHost.newTabSpec("CASH DEPOSITS");
        spec.setContent(R.id.tabCADCDeposits);
        spec.setIndicator("CASH DEPOSITS");
        tabHost.addTab(spec);

        ViewCDHeaderListview();
        controller.CDViewCDeposits = controller.fetchCashDepositedCashier();
        ViewCDDetailListview();

        computeCashDeposits();

        InitialTotalCash = controller.fetchSUMPaymentItem();

        CDmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcd_adddeposit:

                                if (isOpen == 0){
                                    PDADValue = 1;
                                    AddEditDeposits();
                                    isOpen = 1;
                                }



                                break;

                            case R.id.mcd_next:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                controller.PIndicator = 0;

                                controller.PValidatedTotal = Double.valueOf(tvCOHTotal.getText().toString().replace(",","")) + Double.valueOf(tvCDTotalItem.getText().toString().replace(",",""));

                                controller.PCashTotal = InitialTotalCash -  controller.PValidatedTotal;

                                controller.dbPCoins = etCOHCoins.getText().toString().replace(",","");

                                Intent IntentChecksandSummary = new Intent(CashandDepositCashierActivity.this, ChecksandSummaryActivity.class);
                                startActivity(IntentChecksandSummary);
                                finish();

                                break;


                        }
                        return true;
                    }
                });


    }

    public void ViewCOHHeaderListview() {

        hmCOHHeader = new ArrayList<HashMap<String, String>>();
        mCOHHeader = new HashMap<String, String>();

        mCOHHeader.put("Denomination", "DENOMINATION");
        mCOHHeader.put("Qty", "QUANTITY");
        mCOHHeader.put("Amt", "AMOUNT");
        hmCOHHeader.add(mCOHHeader);

        try {
            laCOHHeader = new SimpleAdapter(this, hmCOHHeader, R.layout.item_cashonhand,
                    new String[]{"Denomination","Qty", "Amt"}, new int[]{
                    R.id.rowsDenomination,R.id.rowsQty, R.id.rowsAmt}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rdenomination = (TextView) view.findViewById(R.id.rowsDenomination);
                    TextView rqty = (TextView) view.findViewById(R.id.rowsQty);
                    TextView ramt= (TextView) view.findViewById(R.id.rowsAmt);

                    if (position % 2 == 0) {
                        rdenomination.setTextColor(Color.WHITE);
                        rqty.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rdenomination.setTypeface(null, Typeface.BOLD);
                        rqty.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            COHHeader.setAdapter(laCOHHeader);
        } catch (Exception e) {

        }
    }

    public void ViewCOHDetailListview() {

        controller.hmCOHDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sCOHDenomination.length; i++) {
            mCOHDetail = new HashMap<String, String>();
            mCOHDetail.put("Denomination",sCOHDenomination[i]);
            mCOHDetail.put("Qty",controller.fetchCashOnHandCashier().get(i));
            int Amt;
            Amt = Integer.valueOf(sCOHDenomination[i]) * Integer.valueOf(controller.fetchCashOnHandCashier().get(i));
            mCOHDetail.put("Amt",String.valueOf(Amt));
            controller.hmCOHDetails.add(mCOHDetail);
        }

        try {
            laCOHDetails = new SimpleAdapter(this, controller.hmCOHDetails,R.layout.item_cashonhand,
                    new String[]{"Denomination","Qty", "Amt"}, new int[]{
                    R.id.rowsDenomination,R.id.rowsQty, R.id.rowsAmt}) {
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

            COHDetails.setAdapter(laCOHDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateCOHDetailListview(){

        mCOHDetail = new HashMap<String, String>();

        mCOHDetail.put("Denomination", PDenomination);
        mCOHDetail.put("Qty",Pqty);
        mCOHDetail.put("Amt",PAmt);
        controller.hmCOHDetails.set(Pposition,mCOHDetail);

        try {
            laCOHDetails = new SimpleAdapter(this, controller.hmCOHDetails,  R.layout.item_cashonhand,
                    new String[]{"Denomination","Qty", "Amt"}, new int[]{
                    R.id.rowsDenomination,R.id.rowsQty, R.id.rowsAmt}) {
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
            COHDetails.setAdapter(laCOHDetails);
        } catch (Exception e) {

        }
    }

    public void DialogCOHDenomination() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CashandDepositCashierActivity.this);
        LayoutInflater inflater = CashandDepositCashierActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cashonhanddenomination, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        tvCOHDenomination = (TextView) dialogView.findViewById(R.id.tvDCOHDenomination);
        tvCOHSave = (TextView) dialogView.findViewById(R.id.tvDCOHSave);
        tvCOHCancel = (TextView) dialogView.findViewById(R.id.tvDCOHCancel);
        btnCOHAdd = (Button) dialogView.findViewById(R.id.btnDCOHAdd);
        btnCOHSub = (Button) dialogView.findViewById(R.id.btnDCOHSub);
        final EditText etCOHQty = (EditText) dialogView.findViewById(R.id.etDCOHQty);

        tvCOHDenomination.setText(PDenomination);

        if (Pqty.equals("0")){
            etCOHQty.setText("");
        }else{
            etCOHQty.setText(Pqty);
        }

        etCOHQty.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etCOHQty.length() > 1){
                    if (etCOHQty.getText().toString().substring(0,1).equals("0")){
                        etCOHQty.setText(etCOHQty.getText().toString().substring(1));
                        etCOHQty.setSelection(etCOHQty.getText().length());
                    }
                }
            }

        });


        btnCOHAdd.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (etCOHQty.getText().toString().equals("")){
                    intCOHQty = 1;
                    etCOHQty.setText(intCOHQty.toString());
                }else{
                    intCOHQty = Integer.parseInt(etCOHQty.getText().toString()) + 1;
                    etCOHQty.setText(intCOHQty.toString());
                }
            }
        });

        btnCOHSub.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (etCOHQty.getText().toString().equals("")){
                    etCOHQty.setError("invalid quantity");
                }else if (intCOHQty != -1){
                    intCOHQty = Integer.parseInt(etCOHQty.getText().toString()) - 1;
                    etCOHQty.setText(intCOHQty.toString());
                }

            }
        });

        tvCOHCancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvCOHSave.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (etCOHQty.getText().toString().equals("")) {
                    etCOHQty.setError("invalid quantity");
                } else {
                    COHTAmt = 0;
                    dbCOHCoins = Double.parseDouble(tvCOHTotal.getText().toString().replace(",", ""));
                    intCOHTDenomination = Integer.valueOf(etCOHQty.getText().toString()) * Integer.parseInt(tvCOHDenomination.getText().toString());

                    for (int i = 0; i < COHDetails.getCount(); i++) {
                        HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
                        String objDenomination = (String) obj.get("Denomination");
                        PDenomination = objDenomination;
                        String objAmt;
                        if (PDenomination.equals(tvCOHDenomination.getText().toString())) {
                            objAmt = String.valueOf(intCOHTDenomination);
                        } else {
                            objAmt = (String) obj.get("Amt");
                        }

                        COHTAmt = Integer.parseInt(objAmt) + COHTAmt;
                    }

                    Double dbAmount = Double.valueOf(COHAmt.format(dbCOHCoins));

                    if (dbAmount < COHTAmt) {
                        etCOHQty.setError("amount exceeded");
                    } else {
                        Pqty = etCOHQty.getText().toString();
                        PDenomination = tvCOHDenomination.getText().toString();
                        PAmt = intCOHTDenomination.toString();
                        COHTAmt = 0;

                        UpdateCOHDetailListview();

                        for (int i = 0; i < COHDetails.getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
                            String objAmt = (String) obj.get("Amt");
                            COHTAmt = Integer.parseInt(objAmt) + COHTAmt;
                        }

                        dbCOHTotal = Double.parseDouble(tvCOHTotal.getText().toString().replace(",", ""));

                        dbCOHTCoins = dbCOHTotal - COHTAmt;

                        DecimalFormat COHAmt = new DecimalFormat("#,##0.00");
                        etCOHCoins.setText(COHAmt.format(dbCOHTCoins));

                        alertDialog.dismiss();
                    }
                }
            }
        });

        alertDialog.show();

    }

    public void ViewCDHeaderListview() {

        hmCDHeader = new ArrayList<HashMap<String, String>>();
        mCDHeader = new HashMap<String, String>();

        mCDHeader.put("Bank", "BANK");
        mCDHeader.put("AcctNo", "ACCOUNT NO");
        mCDHeader.put("Branch", "BRANCH");
        mCDHeader.put("Amt", "AMOUNT");
        hmCDHeader.add(mCDHeader);

        try {
            laCDHeader = new SimpleAdapter(this, hmCDHeader, R.layout.item_cashdeposits,
                    new String[]{"Bank","AcctNo","Branch", "Amt"}, new int[]{
                    R.id.rowsBank,R.id.rowsAccntNo,R.id.rowsBranch ,R.id.rowsAmt}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rbank = (TextView) view.findViewById(R.id.rowsBank);
                    TextView raccntno = (TextView) view.findViewById(R.id.rowsAccntNo);
                    TextView rbranch = (TextView) view.findViewById(R.id.rowsBranch);
                    TextView ramt= (TextView) view.findViewById(R.id.rowsAmt);

                    if (position % 2 == 0) {
                        rbank.setTextColor(Color.WHITE);
                        raccntno.setTextColor(Color.WHITE);
                        rbranch.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rbank.setTypeface(null, Typeface.BOLD);
                        raccntno.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        rbranch.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            CDHeader.setAdapter(laCDHeader);
        } catch (Exception e) {

        }
    }

    public void ViewCDDetailListview(){

        try {
            laCDDetails = new SimpleAdapter(this,controller.CDViewCDeposits, R.layout.item_cashdeposits,
                    new String[]{"Bank","AcctNo","Branch", "Amt","DateDeposited"}, new int[]{
                    R.id.rowsBank,R.id.rowsAccntNo,R.id.rowsBranch ,R.id.rowsAmt}) {
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
            CDDetails.setAdapter(laCDDetails);
        } catch (Exception e) {

        }
    }

    public void AddCDDetailListview(){

        mCDDetail = new HashMap<String, String>();

        mCDDetail.put("Bank", PBank);
        mCDDetail.put("AcctNo", PAcctNo);
        mCDDetail.put("Branch", PBranch);
        mCDDetail.put("Amt", PAmt);
        mCDDetail.put("DateDeposited", PDeposited);
        controller.CDViewCDeposits.add(mCDDetail);

        try {
            laCDDetails = new SimpleAdapter(this, controller.CDViewCDeposits,   R.layout.item_cashdeposits,
                    new String[]{"Bank","AcctNo","Branch", "Amt","DateDeposited"}, new int[]{
                    R.id.rowsBank,R.id.rowsAccntNo,R.id.rowsBranch ,R.id.rowsAmt}) {
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
            CDDetails.setAdapter(laCDDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateCDDetailListview(){

        mCDDetail = new HashMap<String, String>();

        mCDDetail.put("Bank", PBank);
        mCDDetail.put("AcctNo", PAcctNo);
        mCDDetail.put("Branch", PBranch);
        mCDDetail.put("Amt", PAmt);
        mCDDetail.put("DateDeposited", PDeposited);
        controller.CDViewCDeposits.set(Pposition,mCDDetail);

        try {
            laCDDetails = new SimpleAdapter(this, controller.CDViewCDeposits,   R.layout.item_cashdeposits,
                    new String[]{"Bank","AcctNo","Branch", "Amt","DateDeposited"}, new int[]{
                    R.id.rowsBank,R.id.rowsAccntNo,R.id.rowsBranch ,R.id.rowsAmt}) {
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
            CDDetails.setAdapter(laCDDetails);
        } catch (Exception e) {

        }
    }

    public void DeleteCDDetailListview(){

        mCDDetail = new HashMap<String, String>();

        mCDDetail.put("Bank", PBank);
        mCDDetail.put("AcctNo", PAcctNo);
        mCDDetail.put("Branch", PBranch);
        mCDDetail.put("Amt", PAmt);
        mCDDetail.put("DateDeposited", PDeposited);
        controller.CDViewCDeposits.remove(mCDDetail);

        try {
            laCDDetails = new SimpleAdapter(this, controller.CDViewCDeposits,   R.layout.item_cashdeposits,
                    new String[]{"Bank","AcctNo","Branch", "Amt","DateDeposited"}, new int[]{
                    R.id.rowsBank,R.id.rowsAccntNo,R.id.rowsBranch ,R.id.rowsAmt}) {
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
            CDDetails.setAdapter(laCDDetails);
        } catch (Exception e) {

        }
    }

    public void AddEditDeposits() {

        final Dialog alertDialog = new Dialog(CashandDepositCashierActivity.this);

        alertDialog.setContentView(R.layout.dialog_adddeposits);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        tvDADTitle = (TextView) alertDialog.findViewById(R.id.tvDADTitle);
        mbsDADBank = (MaterialBetterSpinner) alertDialog.findViewById(R.id.spDADBank);
        etDADAccntno = (EditText) alertDialog.findViewById(R.id.etDADAcctNo);
        etDADAmt = (EditText) alertDialog.findViewById(R.id.etDADAmt);
        btnDADDate = (Button) alertDialog.findViewById(R.id.btnDADDate);
        btnDADTime = (Button) alertDialog.findViewById(R.id.btnDADTime);
        etDADDate = (EditText) alertDialog.findViewById(R.id.etDADDate);
        etDADTime = (EditText) alertDialog.findViewById(R.id.etDADTime);
        tvDADDelete = (TextView) alertDialog.findViewById(R.id.tvDADDelete);
        tvDADCancel = (TextView) alertDialog.findViewById(R.id.tvDADCancel);
        tvDADAccept = (TextView) alertDialog.findViewById(R.id.tvDADAccept);

        if (PDADValue == 2){
            mbsDADBank.setText(PBank);
            etDADAccntno.setText(PAcctNo);
            etDADAmt.setText(PAmt.replace(",",""));
            OriginalAmtValue = Double.valueOf(PAmt.replace(",",""));

            PBCode = PBranch;

            Date DADDtTime = new Date(Long.valueOf(PDeposited));
            SimpleDateFormat SDFDADDt = new SimpleDateFormat(" MM/dd/yyyy", Locale.getDefault());
            PDate = SDFDADDt.format(DADDtTime);
            SimpleDateFormat SDFDADTime = new SimpleDateFormat(" hh:mm a", Locale.getDefault());
            etDADDate.setText(PDate);
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss");
            PTime = sdfTime.format(DADDtTime);
            etDADTime.setText(SDFDADTime.format(DADDtTime));

            tvDADTitle.setText("Edit Deposits");

        }else{
            Long DADDtTime = System.currentTimeMillis();
            SimpleDateFormat SDFDADDt = new SimpleDateFormat(" MM/dd/yyyy", Locale.getDefault());
            PDate = SDFDADDt.format(DADDtTime);
            SimpleDateFormat SDFDADTime = new SimpleDateFormat(" hh:mm a", Locale.getDefault());
            etDADDate.setText(PDate);
            SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss");
            PTime = sdfTime.format(DADDtTime);
            etDADTime.setText(SDFDADTime.format(DADDtTime));

            tvDADDelete.setVisibility(View.INVISIBLE);
        }

        String[] strDADBAccounts = controller.fetchBankBAccounts();


        ArrayAdapter<String>adapter = new ArrayAdapter<String>(CashandDepositCashierActivity.this,
                android.R.layout.simple_dropdown_item_1line,strDADBAccounts );

        mbsDADBank.setAdapter(adapter);

        mbsDADBank.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                controller.PCADBank = mbsDADBank.getText().toString();
                etDADAccntno.setText(controller.fetchBABankAccounts().get(0));
                PBCode = controller.fetchBABankAccounts().get(1);
            }
        });

        etDADAmt.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (!changing && etDADAmt.getText().toString().startsWith("0")){
                    changing = true;
                    etDADAmt.setText(etDADAmt.getText().toString().replace("0", ""));
                }else if(!changing && etDADAmt.getText().toString().startsWith(".")){
                    changing = true;
                    etDADAmt.setText(etDADAmt.getText().toString().replace(".", ""));
                }
                changing = false;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

        });

        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                PDate = sdf.format(myCalendar.getTime());
                etDADDate.setText(PDate);
            }

        };

        btnDADDate.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {


                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog =
                        new DatePickerDialog(CashandDepositCashierActivity.this, datePickerListener, mYear, mMonth, mDay);


                Date today = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(today);
                c.add( Calendar.MONTH, -1 );
                long minDate = c.getTime().getTime();

                Date otherday = new Date();
                Calendar d = Calendar.getInstance();
                d.setTime(otherday);
                d.add( Calendar.MONTH, + 1 );
                long maxDate = d.getTime().getTime();

                datePickerDialog.getDatePicker().setMinDate(minDate);
                datePickerDialog.getDatePicker().setMaxDate(maxDate);
                datePickerDialog.show();

            }
        });

        btnDADTime.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {

                Calendar e = Calendar.getInstance();
                int hour = e.get(Calendar.HOUR_OF_DAY);
                int minute = e.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(CashandDepositCashierActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                if (hourOfDay < 12 && hourOfDay >= 0) {
                                    etDADTime.setText(String.format("%02d:%02d", hourOfDay, minute) + " AM");
                                    PTime = hourOfDay +":" +minute+ ":" + "00";
                                } else {
                                    hourOfDay -= 12;
                                    if(hourOfDay == 0) {
                                        hourOfDay = 12;
                                    }
                                    etDADTime.setText(String.format("%02d:%02d", hourOfDay, minute) + " PM");
                                    PTime = hourOfDay +":" +minute+ ":" + "00";
                                }

                                //SRTime.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();

            }
        });


        tvDADCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                isOpen = 0;
                alertDialog.dismiss();
            }
        });

        tvDADAccept.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (!etDADAmt.getText().toString().equals("")) {
                    if (PDADValue == 2 ){
                        changevalue = OriginalAmtValue - Double.valueOf(etDADAmt.getText().toString().replace(",",""));
                        changevalue = changevalue * - 1;
                    }
                }

                if (mbsDADBank.getText().toString().equals("")){
                    mbsDADBank.setError("please select bank");
                }else if(etDADAmt.getText().toString().equals("")){
                    etDADAmt.setError("please input amount");
                }else if(etDADDate.getText().toString().equals("")){
                    etDADDate.setError("please select date");
                }else if(etDADTime.getText().toString().equals("")){
                    etDADTime.setError("please select time");
                }else{

                    PBank = mbsDADBank.getText().toString();
                    PAcctNo = etDADAccntno.getText().toString();
                    PBranch = PBCode;
                    DecimalFormat CDAmt = new DecimalFormat("#,##0.00");
                    PAmt =  CDAmt.format(Double.valueOf(etDADAmt.getText().toString()));

                    try{
                        SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
                        Date dateComponent = sdfDate.parse(PDate);

                        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss");
                        Date timeComponent = sdfTime.parse(PTime);

                        Calendar timeCalendar = Calendar.getInstance();
                        timeCalendar.setTimeInMillis(timeComponent.getTime());
                        Calendar combinedDate = Calendar.getInstance();
                        combinedDate.setTimeInMillis(dateComponent.getTime());
                        combinedDate.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
                        combinedDate.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));

                        Date date = combinedDate.getTime();
                        Timestamp timestamp = new Timestamp(date.getTime());

                        PDeposited = String .valueOf(timestamp.getTime());

                    }catch(ParseException ex){

                    }
               /*     int amtexceed;
                    amtexceed = 0;
                if (PDADValue == 1 && Double.valueOf(etCOHCoins.getText().toString().replace(",","")) < Double.valueOf(etDADAmt.getText().toString().replace(",",""))){
                    amtexceed = 1;
                }else if (PDADValue == 2 && Double.valueOf(etCOHCoins.getText().toString().replace(",","")) < changevalue) {
                    amtexceed = 1;
                }*/


                    if (PDADValue == 1){
                       /* if (amtexceed ==1){
                            etCOHCoins.setText("0.00");
                        }else{
                            etCOHCoins.setText(CADAmt.format(Double.valueOf(etCOHCoins.getText().toString().replace(",","")) - Double.valueOf(etDADAmt.getText().toString().replace(",",""))));
                        }*/

                        AddCDDetailListview();
                        //computeCashOnHand();
                        computeCashDeposits();
                        Toasty.success(getApplicationContext(), "cash deposit successfully added", Toast.LENGTH_LONG).show();
                    }else{

                       /* if (amtexceed ==1){
                            etCOHCoins.setText("0.00");
                        }else{
                            if (changevalue >= 0.00){
                                etCOHCoins.setText(CADAmt.format(Double.valueOf(etCOHCoins.getText().toString().replace(",","")) - changevalue));
                            }else{
                                etCOHCoins.setText(CADAmt.format(Double.valueOf(etCOHCoins.getText().toString().replace(",","")) + (changevalue*-1)));
                            }
                        }*/


                        UpdateCDDetailListview();
                        //computeCashOnHand();
                        computeCashDeposits();
                        Toasty.info(getApplicationContext(), "cash deposit successfully updated", Toast.LENGTH_LONG).show();
                    }

                    //computeCashDeposits();
                    isOpen = 0;
                    alertDialog.dismiss();

                }
            }
        });

        tvDADDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                DeleteCDDetailListview();
                //computeCashDeposits();
                //etCOHCoins.setText(CADAmt.format(Double.valueOf(etCOHCoins.getText().toString().replace(",","")) + OriginalAmtValue));
                //computeCashOnHand();
                computeCashDeposits();
                alertDialog.dismiss();
                Toasty.error(getApplicationContext(), "cash deposit successfully deleted", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();

        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    public void computeCashOnHand(){


        dbCOHTotalAmt = 0.00;
        String Coins;

        if (etCOHCoins.getText().toString().equals("")){
            Coins = "0";
        }else if (etCOHCoins.getText().toString().equals(".")){
            Coins = "0";
        }else{
            Coins = etCOHCoins.getText().toString();
        }

        for (int i = 0; i < COHDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCOHTotalAmt  = Double.parseDouble(objAmt) +  dbCOHTotalAmt ;
        }

        dbCOHTAmtCoins = dbCOHTotalAmt + Double.parseDouble(Coins.replace(",",""));

        tvCOHTotal.setText(String.valueOf(CADAmt.format(dbCOHTAmtCoins)));

    }

    public void computeCashDeposits(){

        dbCDTotalAmt = 0.00;

        for (int i = 0; i < CDDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCDTotalAmt = Double.parseDouble(objAmt.replace(",","")) + dbCDTotalAmt;
        }

        tvCDTotalItem.setText(String.valueOf(CADAmt.format(dbCDTotalAmt)));

       /* dbCDTotalAmt = 0.00;

        for (int i = 0; i < CDDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCDTotalAmt = Double.parseDouble(objAmt.replace(",","")) + dbCDTotalAmt;
        }

        tvCDTotalItem.setText(String.valueOf(CADAmt.format(dbCDTotalAmt)));*/

        /*dbCOHTotalAmt = 0.00;

        for (int i = 0; i < COHDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCOHTotalAmt  = Double.parseDouble(objAmt) +  dbCOHTotalAmt ;
        }



        etCOHCoins.setText(String.valueOf(CADAmt.format(((controller.fetchSUMPaymentItem() - dbCDTotalAmt)- dbCOHTotalAmt))));

        dbCOHTAmtCoins = dbCOHTotalAmt + Double.parseDouble(etCOHCoins.getText().toString().replace(",",""));

        tvCOHTotal.setText(String.valueOf(CADAmt.format(dbCOHTAmtCoins)));*/

    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(CashandDepositCashierActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Cash and Deposit")
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
        Intent IntentMainActivityActivity = new Intent(CashandDepositCashierActivity.this, MainActivity.class);
        startActivity(IntentMainActivityActivity);
        finish();
    }
}
