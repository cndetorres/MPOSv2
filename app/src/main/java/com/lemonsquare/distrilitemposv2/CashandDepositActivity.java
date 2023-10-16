package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;


public class CashandDepositActivity extends Activity {


    DBController controller = new DBController(this);
    ListView COHDetails,COHHeader,CDDetails,CDHeader;
    ArrayList<HashMap<String, String>> hmCOHHeader,hmCOHDetails,hmCDHeader;
    ListAdapter laCOHHeader,laCOHDetails,laCDHeader,laCDDetails;
    HashMap<String, String> mCOHHeader,mCOHDetail,mCDHeader,mCDDetail;
    List<HashMap<String, String>> CDViewCDeposits;
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
    DecimalFormat CADAmt;
    final Calendar myCalendar = Calendar.getInstance();
    boolean changing = false;
    int PAddvalue;
    List<HashMap<String, String>>  alPrintEndingInventoryH,alPrintCashDepositsH,alPrintCheck;
    ArrayList<String> alPrintCashDeposited;
    int isOpen = 0;

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

    private long mLastClickTime = 0;
    final DecimalFormat COHAmt = new DecimalFormat("#.00");

    List<HashMap<String, String>> alCollectionTurnOver;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashanddeposit);

        COHHeader = (ListView) findViewById(R.id.lvCOHHeader);
        COHDetails = (ListView) findViewById(R.id.lvCOHDetails);
        COHmenu = (BottomNavigationView) findViewById(R.id.btCOHNavigation);
        etCOHCoins = (EditText) findViewById(R.id.etCOHCoins);
        tvCOHTotal = (TextView) findViewById(R.id.tvCOHTotalItem);
        CDHeader = (ListView) findViewById(R.id.lvCDHeader);
        CDDetails = (ListView) findViewById(R.id.lvCDDetails);
        CDmenu = (BottomNavigationView) findViewById(R.id.btCDNavigation);
        tvCDTotalItem = (TextView) findViewById(R.id.tvCDTotalItem);

        dbCOHTotalCoins = Double.valueOf(COHAmt.format(controller.fetchSUMPaymentItem())) - controller.fetchSUMCashDeposited();

        CADAmt = new DecimalFormat("#,###,##0.00");

        etCOHCoins.setText(String.valueOf(CADAmt.format(dbCOHTotalCoins)));
        tvCOHTotal.setText(String.valueOf(CADAmt.format(dbCOHTotalCoins)));
        tvCDTotalItem.setText(String.valueOf(CADAmt.format(controller.fetchSUMCashDeposited())));


        readprintername();

        tabHost = (TabHost) findViewById(R.id.tabCADHost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("CASH ON HAND");
        spec.setContent(R.id.tabCADCOHand);
        spec.setIndicator("CASH ON HAND");
        tabHost.addTab(spec);

        ViewCOHHeaderListview();
        ViewCOHDetailListview();

        COHDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

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
                            case R.id.mcoh_signoff:


                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                DialogEndTransactionConfirmation();

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
        CDViewCDeposits = controller.fetchCashDeposited();
        ViewCDDetailListview();

        CDmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcd_adddeposit:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if (isOpen == 0){
                                    PDADValue = 1;
                                    AddEditDeposits();
                                    isOpen = 1;
                                }



                                break;

                            case R.id.mcd_signoff:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();


                                DialogEndTransactionConfirmation();

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

        hmCOHDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sCOHDenomination.length; i++) {
            mCOHDetail = new HashMap<String, String>();
            mCOHDetail.put("Denomination",sCOHDenomination[i]);
            mCOHDetail.put("Qty","0");
            mCOHDetail.put("Amt","0");
            hmCOHDetails.add(mCOHDetail);
        }

        try {
            laCOHDetails = new SimpleAdapter(this, hmCOHDetails,R.layout.item_cashonhand,
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
        hmCOHDetails.set(Pposition,mCOHDetail);

        try {
            laCOHDetails = new SimpleAdapter(this, hmCOHDetails,  R.layout.item_cashonhand,
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CashandDepositActivity.this);
        LayoutInflater inflater = CashandDepositActivity.this.getLayoutInflater();
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
                }else {
                    intCOHQty = Integer.parseInt(etCOHQty.getText().toString()) + 1;
                    etCOHQty.setText(intCOHQty.toString());
                }
            }
        });

        btnCOHSub.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (etCOHQty.getText().toString().equals("")){
                    etCOHQty.setError("invalid quantity");
                }else if (intCOHQty != 0){
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

                if (etCOHQty.getText().toString().equals("")){
                    etCOHQty.setError("invalid quantity");
                }else{
                    COHTAmt = 0;
                    dbCOHCoins = Double.parseDouble(tvCOHTotal.getText().toString().replace(",",""));
                    intCOHTDenomination = Integer.valueOf(etCOHQty.getText().toString()) * Integer.parseInt(tvCOHDenomination.getText().toString());

                    for (int i = 0; i < COHDetails.getCount(); i++) {
                        HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
                        String objDenomination = (String) obj.get("Denomination");
                        PDenomination = objDenomination;
                        String objAmt;
                        if (PDenomination.equals(tvCOHDenomination.getText().toString())){
                            objAmt = String.valueOf(intCOHTDenomination);
                        }else{
                            objAmt = (String) obj.get("Amt");
                        }

                        COHTAmt = Integer.parseInt(objAmt) + COHTAmt;
                    }

                    Double dbAmount = Double.valueOf(COHAmt.format(dbCOHCoins));

                    if (dbAmount <COHTAmt){
                        etCOHQty.setError("amount exceeded");
                    }else{
                        Pqty = etCOHQty.getText().toString();
                        PDenomination= tvCOHDenomination.getText().toString();
                        PAmt = intCOHTDenomination.toString();
                        COHTAmt = 0;

                        UpdateCOHDetailListview();

                        for (int i = 0; i < COHDetails.getCount(); i++) {
                            HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
                            String objAmt = (String) obj.get("Amt");
                            COHTAmt = Integer.parseInt(objAmt) + COHTAmt;
                        }

                        dbCOHTotal =  Double.parseDouble(tvCOHTotal.getText().toString().replace(",",""));

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
            laCDDetails = new SimpleAdapter(this, CDViewCDeposits, R.layout.item_cashdeposits,
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
        CDViewCDeposits.add(mCDDetail);

        try {
            laCDDetails = new SimpleAdapter(this, CDViewCDeposits,   R.layout.item_cashdeposits,
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
        CDViewCDeposits.set(Pposition,mCDDetail);

        try {
            laCDDetails = new SimpleAdapter(this, CDViewCDeposits,   R.layout.item_cashdeposits,
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

        final Dialog alertDialog = new Dialog(CashandDepositActivity.this);

        alertDialog.setContentView(R.layout.dialog_adddeposits);
        alertDialog.setCanceledOnTouchOutside(false);


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

        tvDADDelete.setVisibility(View.INVISIBLE);

        if (PDADValue == 2){
            mbsDADBank.setText(PBank);
            etDADAccntno.setText(PAcctNo);
            etDADAmt.setText(PAmt.replace(",",""));
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
        }

        String[] strDADBAccounts = controller.fetchBankBAccounts();


        ArrayAdapter<String>adapter = new ArrayAdapter<String>(CashandDepositActivity.this,
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
                        new DatePickerDialog(CashandDepositActivity.this, datePickerListener, mYear, mMonth, mDay);


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

                TimePickerDialog timePickerDialog = new TimePickerDialog(CashandDepositActivity.this,
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

                PAddvalue = 0;

                if (PDADValue == 2){
                    if (!etDADAmt.getText().toString().equals("")) {
                        if (Double.parseDouble(etDADAmt.getText().toString()) > Double.parseDouble(PAmt.replace(",","")));{
                            Double TDADAmt;
                            TDADAmt = (Double.parseDouble(etCOHCoins.getText().toString().replace(",","")) + Double.parseDouble(PAmt.replace(",",""))) - (Double.parseDouble(etDADAmt.getText().toString()));
                            if (TDADAmt < 0.00){
                                PAddvalue = 1;
                            }else{
                                PAddvalue = 0;
                            }
                        }
                    }
                }else{
                    PAddvalue = 0;
                }

                if (mbsDADBank.getText().toString().equals("")){
                    mbsDADBank.setError("please select bank");
                }else if(etDADAmt.getText().toString().equals("")){
                    etDADAmt.setError("please input amount");
                }else if(etDADDate.getText().toString().equals("")){
                    etDADDate.setError("please select date");
                }else if(etDADTime.getText().toString().equals("")){
                    etDADTime.setError("please select time");
                }else if(PDADValue == 1 && Double.valueOf(etCOHCoins.getText().toString().replace(",","")) < Double.valueOf(etDADAmt.getText().toString())) {
                    etDADAmt.setError("amount exceeded");
                }else if (PAddvalue == 1){
                    etDADAmt.setError("amount exceeded");
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

                        if (PDADValue == 1){
                            AddCDDetailListview();
                            Toasty.success(getApplicationContext(), "cash deposit successfully added", Toast.LENGTH_LONG).show();
                        }else{
                            UpdateCDDetailListview();
                            Toasty.info(getApplicationContext(), "cash deposit successfully updated", Toast.LENGTH_LONG).show();
                        }

                        computeCashAndDeposits();

                        isOpen = 0;

                        alertDialog.dismiss();

                    }
                }
        });

        alertDialog.show();

        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    public void DialogEndTransactionConfirmation() {

        android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(CashandDepositActivity.this);
        LayoutInflater inflater = CashandDepositActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cashanddepositendtransaction, null);
        dialogBuilder.setView(dialogView);
        final android.support.v7.app.AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        final EditText etUsername =(EditText)dialogView.findViewById(R.id.etDCADUsername);
        final EditText etPassword =(EditText)dialogView.findViewById(R.id.etDCADPassword);
        tvDCADCancel = (TextView) dialogView.findViewById(R.id.tvDCADCancel);
        tvDCADSignOff = (TextView) dialogView.findViewById(R.id.tvDCADAccept);

        tvDCADCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvDCADSignOff.setOnClickListener(new OnSingleClickListener() {
                                          public void onSingleClick(View v) {


                                                  controller.PUName = etUsername.getText().toString();
                                                  controller.PPassword = etPassword.getText().toString();

                                                  if (etUsername.getText().toString().equals("")) {
                                                      etUsername.setError("please input username");
                                                  } else if (etPassword.getText().toString().equals("")) {
                                                      etPassword.setError("please input password");
                                                  }/*else if(!etUsername.getText().toString().equals(controller.fetchSDstSettings())){
                                                  etUsername.setError("invalid user");
                                              }*/ else if (controller.FetchRoleUser().get(0).equals("")){
                                                      etUsername.setError("invalid user");
                                                  } else {

                                                      InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                                      imm.hideSoftInputFromWindow(etUsername.getWindowToken(), 0);

                                                      alertDialog.dismiss();

                                                      new android.support.v7.app.AlertDialog.Builder(CashandDepositActivity.this)
                                                              .setIcon(android.R.drawable.ic_dialog_alert)
                                                              .setTitle("Cash and Deposit")
                                                              .setMessage("Are you sure you want to sign off?")
                                                              .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                                              {
                                                                  @Override
                                                                  public void onClick(DialogInterface dialog, int which) {
                                                                      controller.deleteCashDeposited();
                                                                      controller.deleteCashDepositedCashier();

                                                                      for (int i = 0; i < CDDetails.getCount(); i++) {
                                                                          HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(i);
                                                                          String objBank = (String) obj.get("Bank");
                                                                          PBank = objBank;
                                                                          String objAcctNo = (String) obj.get("AcctNo");
                                                                          PAcctNo = objAcctNo ;
                                                                          String objBranch = (String) obj.get("Branch");
                                                                          PBranch= objBranch;
                                                                          String objAmt = (String) obj.get("Amt");
                                                                          PAmt= objAmt.replace(",","");
                                                                          String objDateDeposited = (String) obj.get("DateDeposited");
                                                                          PDeposited= objDateDeposited;

                                                                          controller.insertCashDeposited(PBank,PAcctNo,PBranch,null,PAmt.replace(",",""),PDeposited,null);
                                                                          controller.insertCashDepositedCashier(PBank,PAcctNo,PBranch,null,PAmt.replace(",",""),PDeposited,null);
                                                                      }

                                                                      String PQty1000= "";
                                                                      String PQty500= "";
                                                                      String PQty200= "";
                                                                      String PQty100= "";
                                                                      String PQty50= "";
                                                                      String PQty20= "";
                                                                      for (int i = 0; i < COHDetails.getCount(); i++) {
                                                                          HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
                                                                          if (i == 0){
                                                                              String objQty = (String) obj.get("Qty");
                                                                              PQty1000= objQty;
                                                                          }else if (i == 1){
                                                                              String objQty = (String) obj.get("Qty");
                                                                              PQty500= objQty;
                                                                          }else if (i == 2){
                                                                              String objQty = (String) obj.get("Qty");
                                                                              PQty200= objQty;
                                                                          }
                                                                          else if (i == 3){
                                                                              String objQty = (String) obj.get("Qty");
                                                                              PQty100= objQty;
                                                                          }
                                                                          else if (i == 4){
                                                                              String objQty = (String) obj.get("Qty");
                                                                              PQty50= objQty;
                                                                          }
                                                                          else {
                                                                              String objQty = (String) obj.get("Qty");
                                                                              PQty20= objQty;
                                                                          }

                                                                      }

                                                                      if (controller.fetchCountCashOnHand().equals("0")){
                                                                          controller.insertCashOnHand(controller.PUiD,"",PQty1000,PQty500,PQty200,PQty100,PQty50,PQty20,etCOHCoins.getText().toString().replace(",",""),0,"");
                                                                          controller.insertCashOnHandCashier(controller.PUiD,"",PQty1000,PQty500,PQty200,PQty100,PQty50,PQty20,etCOHCoins.getText().toString().replace(",",""),0,"");
                                                                      }else{
                                                                          controller.updateCashOnHand(controller.PUiD,"",PQty1000,PQty500,PQty200,PQty100,PQty50,PQty20,etCOHCoins.getText().toString().replace(",",""),0,"");
                                                                          controller.updateCashOnHandCashier(controller.PUiD,"",PQty1000,PQty500,PQty200,PQty100,PQty50,PQty20,etCOHCoins.getText().toString().replace(",",""),0,"");
                                                                      }

                                                                      //Toasty.success(getApplicationContext(), "transaction successfully saved", Toast.LENGTH_LONG).show();

                                                                      controller.updateSettings(2);



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

                                                                      alertDialog.dismiss();

                                                                      printdialog();


                                                                  }

                                                              })
                                                              .setNegativeButton("No", null)
                                                              .show();

                                                  }
                                              }
                                      });

        alertDialog.show();

    }

    public void computeCashAndDeposits(){

        dbCDTotalAmt = 0.00;

        for (int i = 0; i < CDDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCDTotalAmt = Double.parseDouble(objAmt.replace(",","")) + dbCDTotalAmt;
        }

        tvCDTotalItem.setText(String.valueOf(CADAmt.format(dbCDTotalAmt)));

        dbCOHTotalAmt = 0.00;

        for (int i = 0; i < COHDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCOHTotalAmt  = Double.parseDouble(objAmt) +  dbCOHTotalAmt ;
        }

        etCOHCoins.setText(String.valueOf(CADAmt.format(((Double.valueOf(COHAmt.format(controller.fetchSUMPaymentItem())) - dbCDTotalAmt)- dbCOHTotalAmt))));

        dbCOHTAmtCoins = dbCOHTotalAmt + Double.parseDouble(etCOHCoins.getText().toString().replace(",",""));

        tvCOHTotal.setText(String.valueOf(CADAmt.format(dbCOHTAmtCoins)));

    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(CashandDepositActivity.this)
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
            String title = "ENDING INVENTORY REPORT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;

            printCustom(companyName,3);
            printCustom(title,3);
            printNewLine();

            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1), 0);
            //controller.fetchUiDVNoOReading().get(1)
            printCustom("Vehicle: " + controller.fetchUiDVNoOReading().get(1),0);
            printCustom("Sales District: " + controller.fetchdbSettings().get(6),0);
            //controller.fetchUiDVNoOReading().get(2)
            printCustom("Terminal: " + controller.fetchdbSettings().get(1),0);
            printText(WithDate("Ending Inventory" , getDateTime()));
            printNewLine();

            printText(getLine(55));

            printNewLine();

            int countqty = 0;

            alPrintEndingInventoryH = controller.fetchInventoryList();

            for(int i = 0; i < alPrintEndingInventoryH.size(); i++) {
                printText(PrintInventoryDetails(alPrintEndingInventoryH.get(i).get("Item"),alPrintEndingInventoryH.get(i).get("Qty")));

                printNewLine();
                countqty = countqty + Integer.valueOf(alPrintEndingInventoryH.get(i).get("Qty"));

            }



            printText(getLine(55));
            printNewLine();

            printText(PrintInventoryDetails("TOTAL QUANTITY",String.valueOf(countqty)));

            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();


            printCustom("                DAILY REMITTANCE REPORT",3);
            printNewLine();

            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1), 0);
            //controller.fetchUiDVNoOReading().get(1)
            printCustom("Vehicle: " + controller.fetchUiDVNoOReading().get(1),0);
            printCustom("Sales District: " + controller.fetchdbSettings().get(6),0);
            //controller.fetchUiDVNoOReading().get(2)
            printCustom("Terminal: " + controller.fetchdbSettings().get(1),0);

            printNewLine();
            printCustom("CASH ON HAND",0);


            printText(getLine(55));

            printNewLine();

            alPrintCashDeposited = controller.fetchCashOnHandCashier();

            printText(PrintCashOnHandDetails("DENOMINATION","QTY","AMOUNT"));
            printNewLine();

            DecimalFormat CDAmt = new DecimalFormat("#,##0.00");

            Double TotCOH = 0.00;
            Double TotAmtCOH = 0.00;

            for(int i = 0; i < alPrintCashDeposited.size() - 1; i++) {

                if (i == 0){
                    TotAmtCOH = Double.valueOf(alPrintCashDeposited.get(i)) * 1000;
                    printText(PrintCashOnHandDetails("1000",alPrintCashDeposited.get(i),CDAmt.format(TotAmtCOH)));
                }else if (i == 1){
                    TotAmtCOH = Double.valueOf(alPrintCashDeposited.get(i)) * 500;
                    printText(PrintCashOnHandDetails("500",alPrintCashDeposited.get(i),CDAmt.format(TotAmtCOH)));
                }else if (i == 2){
                    TotAmtCOH = Double.valueOf(alPrintCashDeposited.get(i)) * 200;
                    printText(PrintCashOnHandDetails("200",alPrintCashDeposited.get(i),CDAmt.format(TotAmtCOH)));
                }else if (i == 3){
                    TotAmtCOH = Double.valueOf(alPrintCashDeposited.get(i)) * 100;
                    printText(PrintCashOnHandDetails("100",alPrintCashDeposited.get(i),CDAmt.format(TotAmtCOH)));
                }else if (i == 4){
                    TotAmtCOH = Double.valueOf(alPrintCashDeposited.get(i)) * 50;
                    printText(PrintCashOnHandDetails("50",alPrintCashDeposited.get(i),CDAmt.format(TotAmtCOH)));
                }else{
                    TotAmtCOH = Double.valueOf(alPrintCashDeposited.get(i)) * 20;
                    printText(PrintCashOnHandDetails("20",alPrintCashDeposited.get(i),CDAmt.format(TotAmtCOH)));
                }

                TotCOH = TotCOH + TotAmtCOH;

                printNewLine();
            }

            printText(getLine(55));

            printNewLine();

            printText(PrintCashOnHandDetails("COINS","",CDAmt.format(Double.valueOf(alPrintCashDeposited.get(6)))));
            printNewLine();
            printNewLine();

            printText(PrintCashOnHandDetails("TOTAL CASH ON HAND","",CDAmt.format(Double.valueOf(alPrintCashDeposited.get(6)) + TotCOH)));
            printNewLine();
            printNewLine();
            printNewLine();

            Double TotAmtCashDeposits = 0.00;

            if (controller.fetchCashDeposits().size() > 0){
                printCustom("CASH DEPOSITED",0);

                printText(getLine(55));

                printNewLine();

                alPrintCashDepositsH = controller.printCashDepositsH();

                printText(PrintCashDepositsDetails("BANK","COD","ACCTNO","DATEDEP","AMT"));
                printNewLine();

                for(int i = 0; i < alPrintCashDepositsH.size(); i++) {
                    printText(PrintCashDepositsDetails(alPrintCashDepositsH.get(i).get("Bank"),alPrintCashDepositsH.get(i).get("Branch"),alPrintCashDepositsH.get(i).get("AcctNo"),alPrintCashDepositsH.get(i).get("BillDt"),alPrintCashDepositsH.get(i).get("Amt")));
                    printNewLine();
                    TotAmtCashDeposits = TotAmtCashDeposits + Double.valueOf(alPrintCashDepositsH.get(i).get("Amt").replace(",",""));
                }

                printText(getLine(55));
                printNewLine();



                printText(PrintCashOnHandDetails("TOTAL CASH DEPOSITED","",CDAmt.format(TotAmtCashDeposits)));
                printNewLine();
                printNewLine();
                printNewLine();


            }

            alPrintCheck = controller.fetchChecks();

            Double TotAmtCheck = 0.00;

            if (alPrintCheck.size() > 0){

                printCustom("CHECKS",0);

                printText(getLine(55));

                printNewLine();

                printText(PrintCheckDetails("CHKDT","BANK","CHKNO","AMT"));
                printNewLine();

                for(int i = 0; i < alPrintCheck.size(); i++) {

                    String check;
                    check = alPrintCheck.get(i).get("Bank");
                    if (check.length() > 19){
                        check = check.substring(0,19);
                    }

                    printText(PrintCheckDetails(alPrintCheck.get(i).get("CheckDt"),check,alPrintCheck.get(i).get("CheckNo"),alPrintCheck.get(i).get("Amt")));
                    printNewLine();
                    TotAmtCheck = TotAmtCheck + Double.valueOf(alPrintCheck.get(i).get("Amt").replace(",",""));
                }

                printText(getLine(55));
                printNewLine();



                printText(PrintCashOnHandDetails("TOTAL CHECKS","",CDAmt.format(TotAmtCheck)));
                printNewLine();
                printNewLine();
                printNewLine();

            }

            printText(PrintCashOnHandDetails("TOTAL REMITTANCE","",CDAmt.format(Double.valueOf(alPrintCashDeposited.get(6)) + TotCOH +TotAmtCashDeposits + TotAmtCheck)));

            printNewLine();
            printText(getLine(55));

            printCustom("                   COLLECTION TURNOVER",3);
            printNewLine();
            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1), 0);
            printCustom("Sales District: " + controller.fetchdbSettings().get(6),0);
            printNewLine();



            String salesDate;
            salesDate = "";
            Double amt;
            amt = 0.00;
            int b = 0;

            alCollectionTurnOver = controller.printSCollectionTurnOver();
            for(int i = 0; i < alCollectionTurnOver.size() ; i++){

                if (!salesDate.equals(alCollectionTurnOver.get(i).get("SalesDateTime"))){
                    if (i>0){
                        printNewLine();
                        printNewLine();
                    }
                    salesDate = alCollectionTurnOver.get(i).get("SalesDateTime");
                    printCustom(salesDate,0);
                    printText(PrintCollectionTurnOver("INV NO.","STORE","AMOUNT"));
                }
                printText(PrintCollectionTurnOver(alCollectionTurnOver.get(i).get("Invoice"),alCollectionTurnOver.get(i).get("CustomerName"),alCollectionTurnOver.get(i).get("AmtDue")));
                amt = amt + Double.valueOf(alCollectionTurnOver.get(i).get("AmtDue").replace(",",""));

                b = i;

                if (b + 1 < alCollectionTurnOver.size()){
                    if (!salesDate.equals(alCollectionTurnOver.get(b+1).get("SalesDateTime"))){
                        printText(PrintCashOnHandDetails("TOTAL","",CDAmt.format(amt)));
                        amt = 0.00;
                    }
                }else if (b + 1 == alCollectionTurnOver.size()){
                    printText(PrintCashOnHandDetails("TOTAL","",CDAmt.format(amt)));
                }

            }


            printNewLine();
            printText(getLine(55));

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

    private String PrintInventoryDetails(String str1,String str2) {
        int fulllength = 42;
        int strlength = 14;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;


        String ans = str1 + getSpace(anslength) + " " + getSpace(anslength2) + str2  ;
        return ans;
    }

    private String PrintCashOnHandDetails(String str1,String str2,String str3) {
        int fulllength = 25;
        int strlength = 15;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;

        int str3length = str3.length();
        int anslength3 = strlength  - str3length;


        String ans = str1 + getSpace(anslength) + " " + getSpace(anslength2) + str2 + " " + getSpace(anslength3) + str3  ;
        return ans;
    }

    private String PrintCashDepositsDetails(String str1,String str2,String str3,String str4,String str5) {
        int strlength1 = 9;
        int strlength2 = 15;
        int strlength3 = 10;

        int str1length = str1.length();
        int anslength1  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength3 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength3 - str4length;

        int str5length = str5.length();
        int anslength5 = strlength3 - str5length;


        String ans = str1 + getSpace(anslength1) + " " + str2  + getSpace(anslength2)  + getSpace(anslength3) + str3 + " "   + getSpace(anslength4) + str4 + " "   + getSpace(anslength5) + str5;
        return ans;
    }

    private String PrintCheckDetails(String str1,String str2,String str3,String str4) {
        int strlength1 = 20;
        int strlength2 = 10;
        int strlength3 = 16;

        int str1length = str1.length();
        int anslength1  = strlength2 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength1 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength2 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength3 - str4length;


        String ans = str1 + getSpace(anslength1)  + str2 + getSpace(anslength2)  + str3 + getSpace(anslength3) + " " + getSpace(anslength4)  + str4;
        return ans;
    }

    private String PrintCollectionTurnOver(String str1,String str2,String str3) {
        int strlength1 = 10;
        int strlength2 = 35;

        int str1length = str1.length();
        int anslength1  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength1 - str3length;

        String ans = str1 + getSpace(anslength1) + " " + str2 + getSpace(anslength2) + " " + getSpace(anslength3) + str3;
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
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm:ss a");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
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


    void printdialog(){

        new android.support.v7.app.AlertDialog.Builder(CashandDepositActivity.this)
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

        new android.support.v7.app.AlertDialog.Builder(CashandDepositActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Print Receipt")
                .setMessage("Do you want to reprint receipt?")
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
    public void ProgressDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CashandDepositActivity.this);
        LayoutInflater inflater = CashandDepositActivity.this.getLayoutInflater();
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

        controller.updateUsers(0);

        Intent intentLoginActivity = new Intent(CashandDepositActivity.this, LogInActivity.class);
        startActivity(intentLoginActivity);
        finish();

    }

    private void scanMedia(File path) {
        Uri uri = Uri.fromFile(path);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(CashandDepositActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(CashandDepositActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }
}
