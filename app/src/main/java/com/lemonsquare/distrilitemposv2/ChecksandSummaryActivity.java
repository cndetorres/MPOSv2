package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class ChecksandSummaryActivity extends Activity {

    DBController controller = new DBController(this);
    ListView CASCDetails,CASCHeader,CASVUCDetails,CASVUCHeader,CASVCVDetails,CASVCVHeader,CASSDetails;
    ArrayList<HashMap<String, String>> hmCASCHeader,hmCASVUCHeader,hmCASVCVHeader;

    ListAdapter laCASCHeader,laCASCDetails,laCASVUCHeader,laCASVUCDetails,laCASVCVHeader,laCASVCVDetails,laCASSDetails;
    HashMap<String, String> mCASCHeader,mCASVUCHeader,mCASVCVHeader,mCASSDetails,mCASCDetail,mCASVCUDetail,mCASVCVDetail;
    String[] sCASSHeader = new String[]{"SYSTEM GEN TOTAL","VALIDATED TOTAL","STOCK SHORTAGE","SHORT/OVER CASH","SHORT/OVER CHECK"};//,"SYSTEM GEN TOTAL SALES","SALES COLLECTED"
    DecimalFormat CASSAmt;
    EditText etDCBanks,etDCAccntNo,etDCAmt,etDCDate;
    Button btnDCDate;
    CheckBox chkDCLiquidated;
    BottomNavigationView CASCmenu,CASVmenu,CASSmenu;
    TextView tvDCCancel,tvDCAccept;
    final Calendar myCalendar = Calendar.getInstance();
    int Pposition;
    String Pid,PChkdt,PBank,PChkno,PAmt,PLiq, PItem,PFrom,PTo;
    List<HashMap<String, String>> alPrintCheck,alVariance;


    double dbCASVUTAmt;
    double dbCASSSOCheck,dbCASSSOLCheck;
    int isLiq;

    TabHost tabHost;

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

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checksandsummary);

        CASCHeader = (ListView) findViewById(R.id.lvCASCHeader);
        CASCDetails = (ListView) findViewById(R.id.lvCASCDetails);

        CASVUCHeader = (ListView) findViewById(R.id.lvCASVUCHeader);
        CASVUCDetails = (ListView) findViewById(R.id.lvCASVUCDetails);

        CASVCVHeader = (ListView) findViewById(R.id.lvCASVCVHeader);
        CASVCVDetails = (ListView) findViewById(R.id.lvCASVCVDetails);

        CASSDetails = (ListView) findViewById(R.id.lvCASSDetails);

        CASCmenu = (BottomNavigationView) findViewById(R.id.btCASCNavigation);
        CASVmenu = (BottomNavigationView) findViewById(R.id.btCASVNavigation);
        CASSmenu = (BottomNavigationView) findViewById(R.id.btCASSNavigation);


        tabHost = (TabHost) findViewById(R.id.tabCASHost);
        tabHost.setup();

        readprintername();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec("CHECKS");
        spec.setContent(R.id.tabCASChecks);
        spec.setIndicator("CHECKS");
        tabHost.addTab(spec);

        ViewCASCHeaderListview();
        controller.CASCViewChecks = controller.fetchChecks();
        ViewCASCDetailListview();

        //Tab 2
        spec = tabHost.newTabSpec("VARIANCE");
        spec.setContent(R.id.tabCASVariance);
        spec.setIndicator("VARIANCE");
        tabHost.addTab(spec);

        ViewCASVUCHeaderListview();
        controller.CASVUCViewVariance = controller.fetchChecks();
        transferChecksToVariance();

        if (controller.PIndicator == 0){
            ViewCASVCVHeaderListview();
            addvarianceresult();
        }


        computeSOChecks();

        //computeCheckVarianceAmt();

        //Tab 3
        spec = tabHost.newTabSpec("SUMMARY");
        spec.setContent(R.id.tabCASSummary);
        spec.setIndicator("SUMMARY");
        tabHost.addTab(spec);

        ViewCASSDetailListview();

        CASCDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laCASCDetails.getItem(position);

                String objPid = (String) obj.get("ID");
                Pid = objPid;
                String objChkdt = (String) obj.get("CheckDt");
                PChkdt = objChkdt;
                String objBank = (String) obj.get("Bank");
                PBank = objBank ;
                String objChkno = (String) obj.get("CheckNo");
                PChkno= objChkno;
                String objAmt = (String) obj.get("Amt");
                PAmt= objAmt;
                String objLiq = (String) obj.get("Liq");
                PLiq= objLiq;
                Pposition = position;

                DialogChecks();

            }
        });

        CASCmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcc_next:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();


                                new android.support.v7.app.AlertDialog.Builder(ChecksandSummaryActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Check and Summary")
                                        .setMessage("Are you sure you want to accept payments?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                controller.PINum = 2;

                                                Intent IntentConfirmTransactionActivity = new Intent(ChecksandSummaryActivity.this, ConfirmTransactionActivity.class);
                                                startActivity(IntentConfirmTransactionActivity);
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


        CASVmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcv_next:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                new android.support.v7.app.AlertDialog.Builder(ChecksandSummaryActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Check and Summary")
                                        .setMessage("Are you sure you want to accept payments?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {


                                                controller.PINum = 2;

                                                Intent IntentConfirmTransactionActivity = new Intent(ChecksandSummaryActivity.this, ConfirmTransactionActivity.class);
                                                startActivity(IntentConfirmTransactionActivity);
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

        CASSmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcs_next:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                new android.support.v7.app.AlertDialog.Builder(ChecksandSummaryActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Check and Summary")
                                        .setMessage("Are you sure you want to accept payments?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {


                                                controller.PINum = 2;

                                                Intent IntentConfirmTransactionActivity = new Intent(ChecksandSummaryActivity.this, ConfirmTransactionActivity.class);
                                                startActivity(IntentConfirmTransactionActivity);
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

        if (controller.PIndicator == 1){

            printdialog();

        }

    }

    public void ViewCASCHeaderListview() {

        hmCASCHeader = new ArrayList<HashMap<String, String>>();
        mCASCHeader = new HashMap<String, String>();

        mCASCHeader.put("CheckDt", "CHECK DT");
        mCASCHeader.put("Bank", "BANK");
        mCASCHeader.put("CheckNo", "CHECK #");
        mCASCHeader.put("Amt", "AMT");
        mCASCHeader.put("Liq", "LIQ");
        hmCASCHeader.add(mCASCHeader);

        try {
            laCASCHeader = new SimpleAdapter(this, hmCASCHeader, R.layout.item_checks,
                    new String[]{"CheckDt","Bank", "CheckNo","Amt","Liq"}, new int[]{
                    R.id.rowsCheckDt,R.id.rowsBank, R.id.rowsCheckNo, R.id.rowsAmt, R.id.rowsLiq}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rid = (TextView) view.findViewById(R.id.rowsID);
                    TextView rcheckdt = (TextView) view.findViewById(R.id.rowsCheckDt);
                    TextView rbank = (TextView) view.findViewById(R.id.rowsBank);
                    TextView rcheckno = (TextView) view.findViewById(R.id.rowsCheckNo);
                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);
                    TextView rliq =  (TextView) view.findViewById(R.id.rowsLiq);

                    rid.setVisibility(View.GONE);

                    if (position % 2 == 0) {
                        rcheckdt.setTextColor(Color.WHITE);
                        rbank.setTextColor(Color.WHITE);
                        rcheckno.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rliq.setTextColor(Color.WHITE);
                        rcheckdt.setTypeface(null, Typeface.BOLD);
                        rbank.setTypeface(null, Typeface.BOLD);
                        rcheckno.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        rliq.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            CASCHeader.setAdapter(laCASCHeader);
        } catch (Exception e) {

        }
    }

    public void ViewCASCDetailListview() {

        try {
            laCASCDetails = new SimpleAdapter(this, controller.CASCViewChecks,R.layout.item_checks,
                    new String[]{"ID","CheckDt","Bank", "CheckNo","Amt","Liq"}, new int[]{
                    R.id.rowsID ,R.id.rowsCheckDt,R.id.rowsBank, R.id.rowsCheckNo, R.id.rowsAmt, R.id.rowsLiq}) {
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

            CASCDetails.setAdapter(laCASCDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateCASCDetailListview(){

        mCASCDetail = new HashMap<String, String>();
        mCASCDetail.put("ID", Pid);
        mCASCDetail.put("CheckDt", PChkdt);
        mCASCDetail.put("Bank", PBank);
        mCASCDetail.put("CheckNo", PChkno);
        mCASCDetail.put("Amt", PAmt);
        mCASCDetail.put("Liq", PLiq);
        controller.CASCViewChecks.set(Pposition,mCASCDetail);

        try {
            laCASCDetails = new SimpleAdapter(this, controller.CASCViewChecks,R.layout.item_checks,
                    new String[]{"ID","CheckDt","Bank", "CheckNo","Amt","Liq"}, new int[]{
                    R.id.rowsID ,R.id.rowsCheckDt,R.id.rowsBank, R.id.rowsCheckNo, R.id.rowsAmt, R.id.rowsLiq}) {
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
            CASCDetails.setAdapter(laCASCDetails);
        } catch (Exception e) {

        }
    }

    public void ViewCASVUCHeaderListview() {

        hmCASVUCHeader = new ArrayList<HashMap<String, String>>();
        mCASVUCHeader = new HashMap<String, String>();

        mCASVUCHeader.put("CheckDt", "CHECK DT");
        mCASVUCHeader.put("Bank", "BANK");
        mCASVUCHeader.put("CheckNo", "CHECK #");
        mCASVUCHeader.put("Amt", "AMT");
        hmCASVUCHeader.add(mCASVUCHeader);

        try {
            laCASVUCHeader = new SimpleAdapter(this, hmCASVUCHeader, R.layout.item_unliquidatedchecks,
                    new String[]{"CheckDt","Bank", "CheckNo","Amt"}, new int[]{
                    R.id.rowsCheckDt,R.id.rowsBank, R.id.rowsCheckNo, R.id.rowsAmt}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rcheckdt = (TextView) view.findViewById(R.id.rowsCheckDt);
                    TextView rbank = (TextView) view.findViewById(R.id.rowsBank);
                    TextView rcheckno = (TextView) view.findViewById(R.id.rowsCheckNo);
                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);

                    if (position % 2 == 0) {
                        rcheckdt.setTextColor(Color.WHITE);
                        rbank.setTextColor(Color.WHITE);
                        rcheckno.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rcheckdt.setTypeface(null, Typeface.BOLD);
                        rbank.setTypeface(null, Typeface.BOLD);
                        rcheckno.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            CASVUCHeader.setAdapter(laCASVUCHeader);
        } catch (Exception e) {

        }
    }

    public void AddCASVCUDetailListview(){

        mCASVCUDetail = new HashMap<String, String>();

        mCASVCUDetail.put("CheckDt", PChkdt);
        mCASVCUDetail.put("Bank", PBank);
        mCASVCUDetail.put("CheckNo", PChkno);
        mCASVCUDetail.put("Amt", PAmt);
        controller.CASVUCViewVariance.add(mCASVCUDetail);

        try {
            laCASVUCDetails = new SimpleAdapter(this,  controller.CASVUCViewVariance, R.layout.item_unliquidatedchecks,
                    new String[]{"CheckDt","Bank", "CheckNo","Amt"}, new int[]{
                    R.id.rowsCheckDt,R.id.rowsBank, R.id.rowsCheckNo, R.id.rowsAmt}) {
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
            CASVUCDetails.setAdapter(laCASVUCDetails);
        } catch (Exception e) {

        }
    }

    public void ViewCASVCVHeaderListview() {

        controller.hmCASVCVDetail = new ArrayList<HashMap<String, String>>();
        hmCASVCVHeader = new ArrayList<HashMap<String, String>>();
        mCASVCVHeader = new HashMap<String, String>();

        mCASVCVHeader.put("Bank", "BANK");
        mCASVCVHeader.put("CheckNo", "CHECK #");
        mCASVCVHeader.put("Item", "ITEM");
        mCASVCVHeader.put("From", "FROM");
        mCASVCVHeader.put("To", "TO");
        hmCASVCVHeader.add(mCASVCVHeader);

        try {
            laCASVCVHeader = new SimpleAdapter(this, hmCASVCVHeader, R.layout.item_checkvariance,
                    new String[]{"Bank", "CheckNo","Item","From","To"}, new int[]{
                   R.id.rowsBank, R.id.rowsCheckNo,  R.id.rowsItem, R.id.rowsFrom, R.id.rowsTo}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {

                    View view = super.getView(position, convertView, parent);


                    TextView rbank = (TextView) view.findViewById(R.id.rowsBank);
                    TextView rcheckno = (TextView) view.findViewById(R.id.rowsCheckNo);
                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView rfrom = (TextView) view.findViewById(R.id.rowsFrom);
                    TextView rto = (TextView) view.findViewById(R.id.rowsTo);

                    if (position % 2 == 0) {
                        rbank.setTextColor(Color.WHITE);
                        rcheckno.setTextColor(Color.WHITE);
                        ritem.setTextColor(Color.WHITE);
                        rfrom.setTextColor(Color.WHITE);
                        rto.setTextColor(Color.WHITE);
                        rbank.setTypeface(null, Typeface.BOLD);
                        rcheckno.setTypeface(null, Typeface.BOLD);
                        ritem.setTypeface(null, Typeface.BOLD);
                        rfrom.setTypeface(null, Typeface.BOLD);
                        rto.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            CASVCVHeader.setAdapter(laCASVCVHeader);
        } catch (Exception e) {

        }
    }

    public void AddCASVCVDetailListview() {

        mCASVCVDetail = new HashMap<String, String>();

        mCASVCVDetail.put("Bank", PBank);
        mCASVCVDetail.put("CheckNo", PChkno);
        mCASVCVDetail.put("Item", PItem);
        mCASVCVDetail.put("From", PFrom);
        mCASVCVDetail.put("To", PTo);
        controller.hmCASVCVDetail.add(mCASVCVDetail);

        try {
            laCASVCVDetails = new SimpleAdapter(this, controller.hmCASVCVDetail,R.layout.item_checkvariance,
                    new String[]{"Bank", "CheckNo","Item","From","To"}, new int[]{
                    R.id.rowsBank, R.id.rowsCheckNo,  R.id.rowsItem, R.id.rowsFrom, R.id.rowsTo}) {
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

            CASVCVDetails.setAdapter(laCASVCVDetails);
        } catch (Exception e) {

        }
    }



    public void ViewCASSDetailListview() {

        controller.hmCASSDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sCASSHeader.length; i++) {
            mCASSDetails = new HashMap<String, String>();
            mCASSDetails.put("Header",sCASSHeader[i]);
            CASSAmt = new DecimalFormat("#,###,##0.00");
            if (i == 0){
                mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(controller.fetchSUMPaymentItem() + controller.fetchSUMCheckPItem())));
            }else if (i == 1){
                controller.PValidatedTotal = controller.PValidatedTotal + dbCASSSOLCheck;
                mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(controller.PValidatedTotal)));
            }else if (i == 2){
                mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(controller.fetchSUMStockShortage() + controller.fetchSUMReturnsShortage())));
            }else if (i == 3){
                if (controller.PCashTotal != 0.00){
                    controller.PCashTotal = controller.PCashTotal * -1;
                }
                //controller.PCashTotal = ((controller.fetchSUMPaymentItem() + controller.fetchSUMCheckPItem()) - controller.PValidatedTotal)*-1;
                mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(controller.PCashTotal)));
            }else if (i == 4){
                mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(dbCASSSOCheck)));
            }/*else if (i == 5){
                mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(controller.fetchSUMAmtSales())));
            }else if (i == 6){
                mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(controller.fetchSUMNetPaymentsReceived())));
            }*/

            controller.hmCASSDetails.add(mCASSDetails);
        }

        try {
            laCASSDetails = new SimpleAdapter(this, controller.hmCASSDetails,R.layout.item_summary,
                    new String[]{"Header","Detail"}, new int[]{
                    R.id.rowsHeader,R.id.rowsDetail}) {
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

            CASSDetails.setAdapter(laCASSDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateValidatedTotal(){

        mCASSDetails = new HashMap<String, String>();

        CASSAmt = new DecimalFormat("#,###,##0.00");

        mCASSDetails.put("Header","VALIDATED TOTAL");
        mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(controller.PValidatedTotal)));
        controller.hmCASSDetails.set(Pposition,mCASSDetails);

        try {
            laCASSDetails = new SimpleAdapter(this, controller.hmCASSDetails,R.layout.item_summary,
                    new String[]{"Header","Detail"}, new int[]{
                    R.id.rowsHeader,R.id.rowsDetail}) {
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

            CASSDetails.setAdapter(laCASSDetails);
        } catch (Exception e) {

        }


    }

    public void UpdateCASSDetailListview() {

        mCASSDetails = new HashMap<String, String>();

        CASSAmt = new DecimalFormat("#,###,##0.00");

        mCASSDetails.put("Header","SHORT/OVER CHECK");
        mCASSDetails.put("Detail",String.valueOf(CASSAmt.format(dbCASSSOCheck)));
        controller.hmCASSDetails.set(Pposition,mCASSDetails);


        try {
            laCASSDetails = new SimpleAdapter(this, controller.hmCASSDetails,R.layout.item_summary,
                    new String[]{"Header","Detail"}, new int[]{
                    R.id.rowsHeader,R.id.rowsDetail}) {
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

            CASSDetails.setAdapter(laCASSDetails);
        } catch (Exception e) {

        }
    }

    public void addvarianceresult(){
        for (int i = 0; i < CASCDetails.getAdapter().getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCASCDetails.getItem(i);
            String objID = (String) obj.get("ID");
            String objCheckDt = (String) obj.get("CheckDt");
            String objBank = (String) obj.get("Bank");
            String objCheckNo = (String) obj.get("CheckNo");
            String objAmt = (String) obj.get("Amt");

            alVariance = controller.fetchChecksOriginal(objID);

            if(!objBank.equals(alVariance.get(0).get("Bank"))){
                PBank = objBank;
                PChkno = objCheckNo;
                PItem = "Bank";
                PFrom = alVariance.get(0).get("Bank");
                PTo = objBank;
                AddCASVCVDetailListview();
            }

            if(!objCheckNo.equals(alVariance.get(0).get("CheckNo"))){
                PBank = objBank;
                PChkno = objCheckNo;
                PItem = "CNo";
                PFrom = alVariance.get(0).get("CheckNo");
                PTo = objCheckNo;
                AddCASVCVDetailListview();
            }

            if(!objCheckDt.equals(alVariance.get(0).get("CheckDt"))){
                PBank = objBank;
                PChkno = objCheckNo;
                PItem = "CDt";
                PFrom = alVariance.get(0).get("CheckDt");
                PTo = objCheckDt;
                AddCASVCVDetailListview();
            }

            if(!objAmt.equals(alVariance.get(0).get("Amt"))){
                PBank = objBank;
                PChkno = objCheckNo;
                PItem = "Amt";
                PFrom = alVariance.get(0).get("Amt");
                PTo = objAmt;
                AddCASVCVDetailListview();
            }
        }
    }

    public void computeCheckVarianceAmt(){

        dbCASVUTAmt= 0.00;

        for (int i = 0; i < CASVUCDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCASVUCDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCASVUTAmt = Double.parseDouble(objAmt.replace(",","")) + dbCASVUTAmt;
        }

    }

    public void computeSOChecks(){

            dbCASSSOCheck= 0.00;
            dbCASSSOLCheck = 0.00;

            /*try{
                if (laCASVUCDetails.getCount() !=0){
                    for (int i = 0; i < CASVUCDetails.getCount(); i++) {
                        HashMap<String, Object> obj = (HashMap<String, Object>) laCASVUCDetails.getItem(i);
                        String objAmt = (String) obj.get("Amt");
                        dbCASSSOCheck = Double.parseDouble(objAmt.replace(",","")) + dbCASSSOCheck;
                    }
                }
            }catch (Exception e) {

            }*/

        for (int i = 0; i < CASCDetails.getAdapter().getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCASCDetails.getItem(i);
            String objLiq = (String) obj.get("Liq");
            if (objLiq.equals("YES")){
                String objAmt = (String) obj.get("Amt");
                dbCASSSOCheck = Double.parseDouble(objAmt.replace(",","")) + dbCASSSOCheck;
            }
        }
        dbCASSSOLCheck = dbCASSSOCheck;
        dbCASSSOCheck =   dbCASSSOCheck - controller.fetchOAmtChecks();



    }

    public void transferChecksToVariance(){

        controller.CASVUCViewVariance.clear();
        CASVUCDetails.invalidateViews();

        for (int i = 0; i < CASCDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCASCDetails.getItem(i);
            String objLiq = (String) obj.get("Liq");
            if (objLiq.equals("NO")){
                String objChkdT = (String) obj.get("CheckDt");
                PChkdt = objChkdT;
                String objBank = (String) obj.get("Bank");
                PBank = objBank;
                String objCheckNo = (String) obj.get("CheckNo");
                PChkno = objCheckNo;
                String objAmt = (String) obj.get("Amt");
                PAmt = objAmt;

                AddCASVCUDetailListview();

            }
        }

    }



    public void DialogChecks() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ChecksandSummaryActivity.this);
        LayoutInflater inflater = ChecksandSummaryActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_checks, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        etDCBanks = (EditText) dialogView.findViewById(R.id.etDCBank);
        etDCAccntNo = (EditText) dialogView.findViewById(R.id.etDCAcctNo);
        etDCAmt = (EditText) dialogView.findViewById(R.id.etDCAmt);
        etDCDate = (EditText) dialogView.findViewById(R.id.etDCDate);
        btnDCDate = (Button) dialogView.findViewById(R.id.btnDCDate);
        chkDCLiquidated = (CheckBox) dialogView.findViewById(R.id.chkDCLiquidated);
        tvDCCancel = (TextView) dialogView.findViewById(R.id.tvDCCancel);
        tvDCAccept = (TextView) dialogView.findViewById(R.id.tvDCAccept);

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        etDCBanks.setText(PBank);
        etDCAccntNo.setText(PChkno);
        etDCAmt.setText(PAmt);
        etDCDate.setText(PChkdt);
        if (PLiq.equals("NO")){
            chkDCLiquidated.setChecked(false);
        }else{
            chkDCLiquidated.setChecked(true);
        }

        final DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                etDCDate.setText(sdf.format(myCalendar.getTime()));
            }

        };

        btnDCDate.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {


                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog =
                        new DatePickerDialog(ChecksandSummaryActivity.this, datePickerListener, mYear, mMonth, mDay);


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

        tvDCCancel.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        etDCBanks.setText(PBank);
        etDCAccntNo.setText(PChkno);
        etDCAmt.setText(PAmt);
        etDCDate.setText(PChkdt);
        isLiq = 0;
        if (PLiq.equals("NO")){
            chkDCLiquidated.setChecked(false);
        }else{
            isLiq = 1;
            chkDCLiquidated.setChecked(true);
        }

        tvDCAccept.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (etDCBanks.getText().toString().equals("")){
                    etDCBanks.setError("please input bank");
                }else if (etDCAccntNo.getText().toString().equals("")){
                    etDCAccntNo.setError("please input check no");
                }else if (etDCAmt.getText().toString().equals("")){
                    etDCAmt.setError("please input amount");
                }else if (etDCDate.getText().toString().equals("")){
                    etDCDate.setError("please choose date");
                }else{

                    CASSAmt = new DecimalFormat("#,###,##0.00");

                    PBank = etDCBanks.getText().toString();
                    PChkno = etDCAccntNo.getText().toString();
                    PChkdt = etDCDate.getText().toString();
                    PAmt = etDCAmt.getText().toString();

                    if (chkDCLiquidated.isChecked()){
                        PLiq = "YES";
                        UpdateCASCDetailListview();
                        transferChecksToVariance();

                        if (isLiq != 1){
                            controller.PValidatedTotal = controller.PValidatedTotal + Double.valueOf(etDCAmt.getText().toString().replace(",",""));
                            Pposition = 1;
                            UpdateValidatedTotal();
                        }

                    }else{
                        PLiq = "NO";
                        UpdateCASCDetailListview();
                        transferChecksToVariance();
                        controller.PValidatedTotal = controller.PValidatedTotal - Double.valueOf(etDCAmt.getText().toString().replace(",",""));
                        Pposition = 1;
                        UpdateValidatedTotal();

                    }

                    computeSOChecks();
                    Pposition = 4;
                    UpdateCASSDetailListview();



                    controller.hmCASVCVDetail.clear();
                    CASVCVDetails.invalidateViews();

                    addvarianceresult();

                    Toasty.info(getApplicationContext(), "checks successfully updated", Toast.LENGTH_LONG).show();

                    alertDialog.dismiss();
                }
            }
        });


        alertDialog.show();

    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(ChecksandSummaryActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Check and Summary")
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
            String title = "VARIANCE REPORT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;


            printCustom(companyName,3);
            printCustom(title,3);
            printNewLine();
            printText(WithDate("Cashier: " + controller.PCashierName , getDateTime()));
            printNewLine();
            printCustom("Salesman: " + controller.printVReportH().get(0),0);
            printCustom("Terminal: " + controller.fetchdbSettings().get(1),0);
            printNewLine();

            DecimalFormat CASAmt = new DecimalFormat("#,##0.00");

            printCustom("                       COINS & BILLS",0);
            printText(PrintCoinsBillsDetails("AGENT","CASHIER","VARIANCE"));
            printNewLine();

            Double CoinsBills;
            CoinsBills = controller.fetchTotalCOHand() - controller.fetchTotalCOHandCashier();
            if (CoinsBills != 0.00){
                CoinsBills = CoinsBills * -1;
            }

            printText(PrintCoinsBillsDetails(CASAmt.format(controller.fetchTotalCOHand()),CASAmt.format(controller.fetchTotalCOHandCashier()),CASAmt.format(CoinsBills)));
            printNewLine();
            printText(getLine(57));
            printNewLine();

            alPrintCheck = controller.fetchChecksUnliq();
            Double TotAmtCheck = 0.00;

            if (alPrintCheck.size() > 0){
                printCustom("                   UNLIQUIDATED CHECK",3);

                printNewLine();
                for(int i = 0; i < alPrintCheck.size(); i++) {
                    printText(PrintCheckDetails(alPrintCheck.get(i).get("CheckDt"),alPrintCheck.get(i).get("Bank"),alPrintCheck.get(i).get("CheckNo"),alPrintCheck.get(i).get("Amt")));
                    printNewLine();
                    TotAmtCheck = TotAmtCheck + Double.valueOf(alPrintCheck.get(i).get("Amt").replace(",",""));
                }
                printText(getLine(57));
                printNewLine();
            }

            if (controller.hmCASVCVDetail.size() > 0){
                printCustom("                           CHECK",0);
                printText(PrintCheckVarianceDetails("BANK","CHECKNO","ITEM","FROM","TO"));
                printNewLine();
                for (int i = 0; i < controller.hmCASVCVDetail.size(); i++) {

                    Map<String, String> map = controller.hmCASVCVDetail.get(i);
                    for (Map.Entry<String, String> entry : map.entrySet()) {
                        if (entry.getKey().equals("Bank")) {
                            PBank = entry.getValue();
                        } else if (entry.getKey().equals("CheckNo")) {
                            PChkno = entry.getValue();
                        } else if (entry.getKey().equals("Item")) {
                            PItem = entry.getValue();
                        } else if (entry.getKey().equals("From")) {
                            PFrom = entry.getValue();
                        } else {
                            PTo = entry.getValue();
                        }
                    }
                    printText(PrintCheckVarianceDetails(PBank,PChkno,PItem,PFrom,PTo));
                    printNewLine();
                }
                printText(getLine(57));
                printNewLine();
            }

            printCustom("                       CASH DEPOSITED",0);
            printText(PrintCoinsBillsDetails("AGENT","CASHIER","VARIANCE"));
            printNewLine();

            Double CashDeposited;
            CashDeposited = controller.fetchSUMCashDeposited() - controller.fetchSUMCashDepositedCashier();
            if (CashDeposited != 0.00){
                CashDeposited = CashDeposited * -1;
            }

            printText(PrintCoinsBillsDetails(CASAmt.format(controller.fetchSUMCashDeposited()),CASAmt.format(controller.fetchSUMCashDepositedCashier()),CASAmt.format(CashDeposited)));
            printNewLine();
            printText(getLine(57));
            printNewLine();
            printNewLine();

            /*double cashonhand = 0.00;
            double cashdeposited = 0.00;
            double checks = 0.00;
            double stockshortage = 0.00;
            double returnshortage = 0.00;

            cashonhand = (controller.fetchTotalCOHand() - controller.fetchTotalCOHandCashier()) *-1;
            if (cashonhand >= 0.00){
                cashonhand = 0.00;
            }else{
                cashonhand = cashonhand * -1;
            }

            cashdeposited = (controller.fetchSUMCashDeposited() - controller.fetchSUMCashDepositedCashier()) *-1;
            if (cashdeposited >= 0.00){
                cashdeposited = 0.00;
            }else{
                cashdeposited = cashdeposited *-1;
            }

            checks = controller.fetchTotalOAmtChecks() - controller.fetchTotalAmtChecks();
            if (checks > 0.00){
                checks = 0.00;
            }else{
                checks = checks * -1;
            }

            stockshortage = controller.fetchSUMStockShortage()*-1 ;
            if (stockshortage < 0.00){
                stockshortage = 0.00;
            }

            returnshortage = controller.fetchSUMReturnsShortage()*-1 ;
            if (returnshortage < 0.00){
                returnshortage = 0.00;
            }*/


            controller.TotalShortage = controller.fetchCashShortageCashOnHand();


            if (controller.TotalShortage > 0.00){
                printNewLine();
                printCustom("                  AUTHORITY TO DEDUCT",3);
                printText(WithDate("Cashier: " + controller.fetchRIDNmUsers().get(1) ,  getDateTime()));
                printNewLine();
                printCustom("Salesman: " + controller.printVReportH().get(0),0);
                printCustom("Terminal: " + controller.fetchdbSettings().get(1),0);
                printNewLine();
                printCustom("This is to authorize HR/Acctg Dept to deduct",0);
                printCustom("from my salary and/or incentives amounting to",0);
                printCustom("              " + CASAmt.format(controller.TotalShortage),0);
                printCustom("due to short remittance of cash on this date " + getDate(),0);
                printNewLine();
                printNewLine();
                printText(getLine(57));
                printNewLine();
                printCustom("              SIGNATURE OVER PRINTED NAME",0);
            }

            printNewLine();
            printNewLine();
            printCustom("                       GATE PASS",3);
            printNewLine();
            printText(WithDate("Cashier: " + controller.fetchRIDNmUsers().get(1) ,getDateTime()));
            printNewLine();
            printCustom("Salesman: " + controller.printVReportH().get(0),0);
            printCustom("Terminal: " + controller.fetchdbSettings().get(1),0);
            printNewLine();
            printCustom("SIGNATURE",0);

            printNewLine();

            Bitmap bmp = BitmapFactory.decodeByteArray(controller.bArray, 0, controller.bArray.length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmp, 250, 90, false);

            printImage(resizedbmp);


            printText(getLine(57));

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
        int strlength = 27;
        int strlength1 = 30;

        int str1length = str1.length();
        int anslength  = strlength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength1 - str2length;

        String ans = str1 + getSpace(anslength) + getSpace(anslength2) + str2;

        return ans;
    }

    private String PrintCoinsBillsDetails(String str1, String str2,String str3) {
        int strlength1 = 15;
        int strlength2 = 27;

        int str1length = str1.length();
        int anslength  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength1 - str3length;


        String ans =  str1 + getSpace(anslength)   + str2 + getSpace(anslength2) +    getSpace(anslength3) + str3 ;
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

    private String PrintCheckVarianceDetails(String str1,String str2,String str3,String str4,String str5) {
        int strlength1 = 14;
        int strlength2 = 12;
        int strlength3 = 5;

        int str1length = str1.length();
        int anslength1  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength1 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength3 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength2 - str4length;

        int str5length = str5.length();
        int anslength5 = strlength2 - str5length;


        String ans = str1 + getSpace(anslength1) +  str2  + getSpace(anslength2)   + str3 + getSpace(anslength3) +  str4 + getSpace(anslength4) +  getSpace(anslength5) + str5;
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

    private String getDate() {

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }
    void printdialog(){

        new android.support.v7.app.AlertDialog.Builder(ChecksandSummaryActivity.this)
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

                        returnactivity();
                        closeBT();

                    }
                })
                .show();

    }

    void reprintdialog(){

        new android.support.v7.app.AlertDialog.Builder(ChecksandSummaryActivity.this)
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ChecksandSummaryActivity.this);
        LayoutInflater inflater = ChecksandSummaryActivity.this.getLayoutInflater();
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

        Intent intentLogInActivity = new Intent(ChecksandSummaryActivity.this, LogInActivity.class);
        startActivity(intentLogInActivity);
        finish();

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

    public void onBackPressed() {
        Intent IntentMainActivityActivity = new Intent(ChecksandSummaryActivity.this, MainActivity.class);
        startActivity(IntentMainActivityActivity);
        finish();
    }
}
