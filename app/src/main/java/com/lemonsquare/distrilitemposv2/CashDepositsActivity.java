package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.File;
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

import es.dmoral.toasty.Toasty;

public class CashDepositsActivity extends Activity {

    DBController controller = new DBController(this);
    ListView CDDetails,CDHeader;
    ArrayList<HashMap<String, String>>hmCDHeader;
    ListAdapter laCDHeader,laCDDetails;
    HashMap<String, String> mCDHeader,mCDDetail;
    List<HashMap<String, String>> CDViewCDeposits;
    BottomNavigationView CDmenu;
    String Pid,PAmt,PBank,PAcctNo,PBranch,PDeposited,PDate,PTime,PBCode;
    int Pposition,PDADValue;
    final Calendar myCalendar = Calendar.getInstance();
    TextView tvCOHTotal,tvCDTotalItem,tvDADCancel,tvDADAccept,tvDADTitle,tvDCADCancel,tvDADDelete;
    MaterialBetterSpinner mbsDADBank;
    Button btnDADDate,btnDADTime;
    EditText etDADAccntno,etDADAmt,etDADDate,etDADTime;
    boolean changing = false;
    Double dbCDTotalAmt = 0.00;
    int isOpen = 0;
    final DecimalFormat COHAmt = new DecimalFormat("#.00");

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cashdeposits);

        CDHeader = (ListView) findViewById(R.id.lvCDHeader);
        CDDetails = (ListView) findViewById(R.id.lvCDDetails);
        CDmenu = (BottomNavigationView) findViewById(R.id.btCDNavigation);


        ViewCDHeaderListview();

        if (controller.fetchCashDeposited().size() > 0){
            CDViewCDeposits = controller.fetchCashDeposits();
        }else{
            CDViewCDeposits = controller.fetchNull();
        }

        ViewCDDetailListview();

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

                String objPid = (String) obj.get("ID");
                Pid = objPid;
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
                        }
                        return true;
                    }
                });


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
            laCDHeader = new SimpleAdapter(this, hmCDHeader, R.layout.item_cashdeposit,
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
            laCDDetails = new SimpleAdapter(this, CDViewCDeposits, R.layout.item_cashdeposit,
                    new String[]{"ID","Bank","AcctNo","Branch", "Amt","DateDeposited"}, new int[]{
                    R.id.rowsID,R.id.rowsBank,R.id.rowsAccntNo,R.id.rowsBranch ,R.id.rowsAmt}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rid = (TextView) view.findViewById(R.id.rowsID);
                    rid.setVisibility(View.GONE);
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

        final Dialog alertDialog = new Dialog(CashDepositsActivity.this);

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


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(CashDepositsActivity.this,
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
                        new DatePickerDialog(CashDepositsActivity.this, datePickerListener, mYear, mMonth, mDay);


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

                TimePickerDialog timePickerDialog = new TimePickerDialog(CashDepositsActivity.this,
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
                    PAmt =  etDADAmt.getText().toString();

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

                        computecashdeposit();

                        Double dbAmount = Double.valueOf(COHAmt.format(dbCDTotalAmt));


                        if (dbAmount < (Double.valueOf(etDADAmt.getText().toString()))){
                            etDADAmt.setError("amount exceeded");
                        }else{
                            controller.insertCashDeposited(PBank,PAcctNo,PBranch,null,PAmt.replace(",",""),PDeposited,null);
                            controller.insertCashDepositedCashier(PBank,PAcctNo,PBranch,null,PAmt.replace(",",""),PDeposited,null);

                            CDViewCDeposits.clear();
                            CDViewCDeposits = controller.fetchCashDeposits();
                            ViewCDDetailListview();

                            //Toasty.success(getApplicationContext(), "cash deposit successfully added", Toast.LENGTH_LONG).show();


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
                        }

                    }else{

                        computecashdepositforupdate();

                        if (dbCDTotalAmt < Double.valueOf(etDADAmt.getText().toString())){
                            etDADAmt.setError("amount exceeded");
                        }else{
                            controller.updateCashDeposited(PBank,PAcctNo,PBranch,null,PAmt.replace(",",""),PDeposited,null,Pid);
                            controller.updateCashDepositedCashier(PBank,PAcctNo,PBranch,null,PAmt.replace(",",""),PDeposited,null,Pid);

                            CDViewCDeposits.clear();
                            CDViewCDeposits = controller.fetchCashDeposits();
                            ViewCDDetailListview();

                            //Toasty.info(getApplicationContext(), "cash deposit successfully updated", Toast.LENGTH_LONG).show();

                            controller.export();
                            Toasty.success(getApplicationContext(),"database backup successfully",Toast.LENGTH_LONG).show();

                            alertDialog.dismiss();
                        }
                    }

                }
            }
        });

        alertDialog.show();

        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(CashDepositsActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    public void computecashdeposit(){

        dbCDTotalAmt = 0.00;

        for (int i = 0; i < CDDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(i);
            String objAmt = (String) obj.get("Amt");
            dbCDTotalAmt = Double.parseDouble(objAmt.replace(",","")) + dbCDTotalAmt;
        }

        dbCDTotalAmt = controller.fetchSUMPaymentItem() - dbCDTotalAmt;

    }

    public void computecashdepositforupdate(){

        dbCDTotalAmt = 0.00;

        for (int i = 0; i < CDDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(i);
            if (Pposition != i){
                String objAmt = (String) obj.get("Amt");
                dbCDTotalAmt = Double.parseDouble(objAmt.replace(",","")) + dbCDTotalAmt;
            }
        }

        dbCDTotalAmt = (Double.valueOf(COHAmt.format(controller.fetchSUMPaymentItem()))) - dbCDTotalAmt;

    }






    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(CashDepositsActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Cash Deposits")
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
        Intent IntentMainActivity = new Intent(CashDepositsActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }
}

