package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.telephony.SmsManager;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

/*import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.lemonsquare.mposv2.Control.Controls;
import com.lemonsquare.mposv2.Control.HeaderControl;
import com.lemonsquare.mposv2.Control.ItemListControl;
import com.lemonsquare.mposv2.Model.HeaderList;
import com.lemonsquare.mposv2.Model.ItemList;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class OrderingActivity extends Activity {

    DBController controller = new DBController(this );
    private List<ItemList> listItem;
    private  ItemListControl itemListControl;

    ListView header;
    BottomNavigationView menu;
    AutoCompleteTextView dropDownItemList;
    EditText qty;

    private long lastClick = 0;
    private HeaderControl headerControl;
    final String messageHeader = "Ordering";
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        menu = (BottomNavigationView) findViewById(R.id.bnvMenu);
        header = (ListView) findViewById(R.id.lvOHeader);

        HeaderList headerList = new HeaderList("MATERIAL NAME","QTY","UNIT");
        List<HeaderList> listHeader = new ArrayList<>();
        listHeader.add(headerList);
        headerControl = new HeaderControl(this, R.layout.item_ordering, listHeader);
        header.setAdapter(headerControl);



        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mo_additem:

                                if (SystemClock.elapsedRealtime() - lastClick < 1000) {
                                    return false;
                                }
                                lastClick = SystemClock.elapsedRealtime();

                                AddItem();

                                break;
                        }
                        return true;
                    }
                });
    }

    private void AddItem(){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additemordering, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        listItem = controller.fetchMaterialsOrdering();
        dropDownItemList = dialogView.findViewById(R.id.acDOCProduct);
        final ImageView list = dialogView.findViewById(R.id.ivDOOList);
        final Button scan = dialogView.findViewById(R.id.btnDOCScan);
        final Button add = dialogView.findViewById(R.id.btnDOCAdd);
        final Button sub = dialogView.findViewById(R.id.btnDOCSub);
        final TextView ok = dialogView.findViewById(R.id.tvDOCOk);
        final TextView next = dialogView.findViewById(R.id.tvDOCNext);
        qty = dialogView.findViewById(R.id.etDOCQty);

        itemListControl = new ItemListControl(this, R.layout.item_list, listItem);
        dropDownItemList.setThreshold(1);
        dropDownItemList.setAdapter(itemListControl);


        dropDownItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dropDownItemList.setText(itemListControl.getMaterialNameAtPosition(position));
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dropDownItemList.showDropDown();
            }
        });

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

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer qtyOrdered;
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else{
                    qtyOrdered = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(qtyOrdered.toString());
                }

            }
        });


        sub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer qtyOrdered;
                if (qty.getText().toString().equals("")){
                    Controls.messagebox("Invalid qty",context,messageHeader);
                }else if (Integer.parseInt(qty.getText().toString()) != 1){
                    qtyOrdered = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(qtyOrdered.toString());
                }
            }
        });

        scan.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                new IntentIntegrator(OrderingActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
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



        Toast.makeText(this, "List" + listItem.size(), Toast.LENGTH_LONG).show();
        //Toast.makeText(this, String.valueOf(getPosition(result)), Toast.LENGTH_LONG).show();

       *//* if(itemListControl.getMaterialNameAtPosition(getPosition(result)).equals("")){
            Controls.messagebox("Material not found",context,messageHeader);
        }else{
            dropDownItemList.setText(itemListControl.getMaterialNameAtPosition(getPosition(result)));
            qty.requestFocus();
        }*//*

    }

    private int getPosition(String category) {

        int returnCategpry = 0;
        for(int i = 0; i < listItem.size(); ++i) {
            if(listItem.get(i).getUpc() == category){
                returnCategpry = i;
            }
        }

        return returnCategpry;
    }

    public void onBackPressed() {
        finish();
    }*/
public class OrderingActivity extends Activity {
    DBController controller = new DBController(this);
    ListView ODetails,OHeader,POHeader,PODetails;
    ArrayList<HashMap<String, String>> hmOHeader,hmODetails;
    SimpleAdapter laOHeader,laODetails;
    HashMap<String, String> mOHeader,mODetail;
    BottomNavigationView menu,POMenu;
    AutoCompleteTextView product;
    EditText qty,ORemarks,PORemarks;
    Button btnOAdd,btnOSub,btnOScan;
    Integer intOQty = 0;
    String Pproducts,Pqty,Punit;
    Integer Pposition;
    Double intOTPrice;
    TextView tvOItem, tvOStockItem,tvOUnitItem,tvOPriceItem,tvOTotalItem,tvDOCOk,tvDOCNext,tvDOCDelete,tvDOCSave;
    ArrayList<String> alOPAmtMaterial;
    List<String> OListSettings;
    DecimalFormat OAmt = new DecimalFormat("#,###,##0.00");
    String purchaseOrderID = null;
    EditText deliveryDate;
    final Calendar myCalendar = Calendar.getInstance();
    ImageView ivDeliveryCalendar;

    List<HashMap<String, String>> alPrintPurchaseOrder;
    ArrayList<String> arrayListTxt = new ArrayList<String>();

    int status;

    int isConnected;
    int isTxtOnline;
    int isRegularOrder;
    String orderNo;
    String order;

    String remarks;

    boolean isClickedTwice;

    private long mLastClickTime = 0;
    int addDay;

    TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        OHeader = (ListView) findViewById(R.id.lvOHeader);
        ODetails = (ListView) findViewById(R.id.lvODetails);
        POHeader = (ListView) findViewById(R.id.lvPOHeader);
        PODetails = (ListView) findViewById(R.id.lvPODetails);
        menu = (BottomNavigationView) findViewById(R.id.bnvMenu);
        POMenu = (BottomNavigationView) findViewById(R.id.bnvPOMenu);
        deliveryDate = (EditText) findViewById(R.id.etDeliveryDate);
        ivDeliveryCalendar = (ImageView) findViewById(R.id.ivDeliveryCalendar);
        ORemarks = (EditText) findViewById(R.id.etORemarks);
        PORemarks = (EditText) findViewById(R.id.etPORemarks);

        /*Toasty.info(getApplicationContext(), "200721".substring(2,6), Toast.LENGTH_LONG).show();*/

        tabHost = (TabHost) findViewById(R.id.tabCADHost);
        tabHost.setup();

        TabHost.TabSpec spec = tabHost.newTabSpec("REGULAR ORDER");
        spec.setContent(R.id.tabREGULARORDERS);
        spec.setIndicator("REGULAR ORDER");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("PROMO ORDER");
        spec.setContent(R.id.tabPROMOORDERS);
        spec.setIndicator("PROMO ORDER");
        tabHost.addTab(spec);

        isRegularOrder = 1;
        remarks = "";
        ViewHeaderListview();

        for(int i = 0; i < tabHost.getTabWidget().getChildCount(); ++i){
            tabHost.getTabWidget().getChildAt(i).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int count;

                    if (isRegularOrder == 1){
                        count = ODetails.getCount();
                    }else{
                        count = PODetails.getCount();
                    }




                    if (count > 0){
                        String title;

                        if (isRegularOrder == 1){
                            title = "REGULAR ORDER";
                        }else{
                            title = "PROMO ORDER";
                        }

                        new AlertDialog.Builder(OrderingActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(title)
                                .setCancelable(false)
                                .setMessage("Are you sure you want to leave " + title + " tab? This will delete all the inputs made in ordering.")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        if(tabHost.getCurrentTab() == 0){
                                            deleteall();
                                            tabHost.setCurrentTab(1);
                                            isRegularOrder = 2;
                                            ViewHeaderListview();
                                            hmODetails.clear();

                                        }else{
                                            deleteall();
                                            tabHost.setCurrentTab(0);
                                            isRegularOrder = 1;
                                            ViewHeaderListview();
                                            hmODetails.clear();
                                        }

                                    }

                                })
                                .setNegativeButton("No", null)
                                .show();
                    }else{
                        if(tabHost.getCurrentTab() == 0){
                            tabHost.setCurrentTab(1);
                            isRegularOrder = 2;
                            ViewHeaderListview();

                        }else{
                            tabHost.setCurrentTab(0);
                            isRegularOrder = 1;
                            ViewHeaderListview();

                        }
                    }


                }
            });
        }

        /*tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                if (tabId.equals("REGULAR ORDER")){
                    isRegularOrder = 1;
                    ViewHeaderListview();
                }else{
                    isRegularOrder = 2;
                    ViewHeaderListview();
                }

            }
        });*/


        isClickedTwice = false;
        status = 0;

        OListSettings = controller.fetchdbSettings();

        DateFormat defaultDeliveryDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Calendar defaultDeliveryDate = Calendar.getInstance();
        int dayOfWeek = defaultDeliveryDate.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek == Calendar.SATURDAY){
            addDay = 2;
        }else{
            addDay = 1;
        }
        defaultDeliveryDate.add(Calendar.DAY_OF_MONTH, addDay);
        deliveryDate.setText(defaultDeliveryDateFormat.format(defaultDeliveryDate.getTime()));




        final DatePickerDialog.OnDateSetListener datePickerListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd/yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                int dayOfWeek = myCalendar.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek == Calendar.SUNDAY){
                    Toasty.error(getApplicationContext(),"invalid delivery date",Toast.LENGTH_LONG).show();
                }else{
                    deliveryDate.setText(sdf.format(myCalendar.getTime()));
                }
            }

        };

        ivDeliveryCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);
                e.add(Calendar.DAY_OF_MONTH, addDay);

                DatePickerDialog datePickerDialog2 =
                        new DatePickerDialog(OrderingActivity.this, datePickerListener2, mYear, mMonth, mDay);

                Date otherday = new Date();
                Calendar d = Calendar.getInstance();
                d.setTime(otherday);
                d.add( Calendar.WEEK_OF_MONTH, 1 );
                long maxDate = d.getTime().getTime();

                datePickerDialog2.getDatePicker().setMinDate(e.getTimeInMillis());
                datePickerDialog2.getDatePicker().setMaxDate(maxDate);

                datePickerDialog2.show();


            }
        });






        ODetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }

                HashMap<String, Object> obj = (HashMap<String, Object>) laODetails.getItem(position);

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

        PODetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }

                HashMap<String, Object> obj = (HashMap<String, Object>) laODetails.getItem(position);

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
                            case R.id.mo_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }



                                DialogAddItem();


                                break;

                            case R.id.mo_done:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }

                                if (ODetails.getCount() == 0){
                                    messagebox("nothing to order");
                                }else{




                                    if (isNetworkConnected()){

                                        if (controller.fetchCountPurchaseOrder(String.valueOf(isRegularOrder)) == 0){
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("Are you sure you want to order online?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 1;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();


                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }else{
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("Are you sure you want to proceed with order online? By click yes, previous order will be cancelled.")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 1;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();


                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }



                                    }else{

                                        if (controller.fetchCountPurchaseOrder(String.valueOf(isRegularOrder)) == 0){
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("No internet connection, order via SMS?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 0;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();
                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }else{
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("Are you sure you want to proceed with order offline? By click yes, previous order will be cancelled.")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 0;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();
                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }


                                    }





                                }


                                break;

                        }
                        return true;
                    }
                });

        POMenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mpo_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }

                                if (ArrayUtils.isEmpty(controller.fetchMNameMaterialsPacksPromo())){
                                    messagebox("No Promo items available");
                                }else{
                                    DialogAddItem();
                                }
                                break;

                            case R.id.mpo_done:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }

                                if (PODetails.getCount() == 0){
                                    messagebox("nothing to order");
                                }else{

                                    if (isNetworkConnected()){

                                        if (controller.fetchCountPurchaseOrder(String.valueOf(isRegularOrder)) == 0){
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("Are you sure you want to order online?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 1;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();


                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }else{
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("Are you sure yu want to proceed with order online? By click yes, previous order will be cancelled?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 1;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();


                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }



                                    }else{

                                        if (controller.fetchCountPurchaseOrder(String.valueOf(isRegularOrder)) == 0){
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("No internet connection, order via SMS?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 0;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();
                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }else{
                                            new AlertDialog.Builder(OrderingActivity.this)
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Ordering")
                                                    .setCancelable(false)
                                                    .setMessage("Are you sure yu want to proceed with order offline? By click yes, previous order will be cancelled?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            isTxtOnline = 0;
                                                            SaveOrderLocal();
                                                            new MyTask().execute();
                                                        }

                                                    })
                                                    .setNegativeButton("No", null)
                                                    .show();
                                        }


                                    }
                                }
                                break;

                        }
                        return true;
                    }
                });

    }

    private void deleteall(){

            DeleteDetailListViewAll();

    }

    private class MyTask extends AsyncTask<String,String,String> {

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {

            pd = new ProgressDialog(OrderingActivity.this);
            pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pd.setIcon(R.drawable.ic_save_progress);
            pd.setTitle("Please wait...");
            pd.setMessage("Saving data");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected String doInBackground(String... params) {


            String connection;

            if (isTxtOnline == 0){
                sendOrderSMS();
                connection = "SMS";
            }else {
                ModuleConn modulecon = new ModuleConn(OrderingActivity.this);
                alPrintPurchaseOrder = controller.fetchPurchaseOrder(purchaseOrderID);
                String strConnection = "";
                try {
                    order = "";
                    orderNo = "";

                    for (int i = 0; i < alPrintPurchaseOrder.size(); i++) {
                        order = order + alPrintPurchaseOrder.get(i).get("MaterialCode") + ":" + alPrintPurchaseOrder.get(i).get("Qty") + "/";
                    }

                    strConnection = modulecon.SP_INS_ORDR_STAGING(alPrintPurchaseOrder.get(0).get("ID"), alPrintPurchaseOrder.get(0).get("DeliveryDate"), alPrintPurchaseOrder.get(0).get("AgentCode"), order);
                } catch (SQLException e) {

                }

                connection = strConnection;
            }

            return connection;

        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("SMS")){
                messageboxTxt("Ordering via SMS successfully sent.");
            }else if (result.length() == 0){
                alPrintPurchaseOrder = controller.fetchPurchaseOrder(purchaseOrderID);
                if(alPrintPurchaseOrder.size()> 0) {
                    new AlertDialog.Builder(OrderingActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Ordering")
                            .setCancelable(false)
                            .setMessage("No internet connection, do you want to retry ordering online?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isTxtOnline = 1;
                                    new MyTask().execute();
                                }

                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AlertDialog.Builder(OrderingActivity.this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Ordering")
                                            .setCancelable(false)
                                            .setMessage("Order via SMS?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    isTxtOnline = 0;
                                                    new MyTask().execute();
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    new AlertDialog.Builder(OrderingActivity.this)
                                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                                            .setTitle("Ordering")
                                                            .setCancelable(false)
                                                            .setMessage("Are you sure you want to cancel purchased order?")
                                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Toasty.info(getApplicationContext(), "Order was cancelled", Toast.LENGTH_LONG).show();
                                                                    finish();
                                                                }
                                                            })
                                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    isTxtOnline = 1;
                                                                    new MyTask().execute();
                                                                }

                                                            })
                                                            .show();
                                                }

                                            })
                                            .show();
                                }

                            })
                            .show();
                }
            }else if (result.substring(0,4).equals("Your")){
                messageboxTxt(result);
                /*try{
                    if (!remarks.equals("")){
                        modulecon.SP_UPDATE_ORDRDNO(alPrintPurchaseOrder.get(0).get("ID"),remarks);
                    }
                }catch (SQLException e){
                }*/
            }else if (result.substring(0,3).equals("You")){
                messageboxTxt(result);
                /*try{
                    if (!remarks.equals("")){
                        modulecon.SP_UPDATE_ORDRDNO(alPrintPurchaseOrder.get(0).get("ID"),remarks);
                    }
                }catch (SQLException e){
                }*/
            }else if (result.substring(0,5).equals("Sorry")) {
               messagebox(result);
            }else if (result.equals("Ordering is currently not allowed until futher notice.")) {
                messagebox(result);
            }else{
                alPrintPurchaseOrder = controller.fetchPurchaseOrder(purchaseOrderID);
                if(alPrintPurchaseOrder.size()> 0) {
                    new AlertDialog.Builder(OrderingActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Ordering")
                            .setCancelable(false)
                            .setMessage("No internet connection, do you want to retry ordering online?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    isTxtOnline = 1;
                                    new MyTask().execute();
                                }

                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AlertDialog.Builder(OrderingActivity.this)
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .setTitle("Ordering")
                                            .setCancelable(false)
                                            .setMessage("Order via SMS?")
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    isTxtOnline = 0;
                                                    new MyTask().execute();
                                                }
                                            })
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    new AlertDialog.Builder(OrderingActivity.this)
                                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                                            .setTitle("Ordering")
                                                            .setCancelable(false)
                                                            .setMessage("Are you sure you want to cancel purchased order?")
                                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Toasty.info(getApplicationContext(), "Order was cancelled", Toast.LENGTH_LONG).show();
                                                                    finish();
                                                                }
                                                            })
                                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    isTxtOnline = 1;
                                                                    new MyTask().execute();
                                                                }

                                                            })
                                                            .show();
                                                }

                                            })
                                            .show();
                                }

                            })
                            .show();
                }
            }

            pd.dismiss();

        }


    }

    private void UpdateItemList(){
        for(int i = 0; i < arrayListTxt.size();i++){
            controller.updatePurchaseOrderItem(purchaseOrderID,arrayListTxt.get(i));
        }
    }

    private void SaveOrderLocal(){
        DateFormat df = new SimpleDateFormat("MMddHHmmss");
        DateFormat df2 = new SimpleDateFormat("MM/dd/yyyy");
        String date = df.format(Calendar.getInstance().getTime());
        String purchaseOrderDate = df2.format(Calendar.getInstance().getTime());

        purchaseOrderID = "";
        purchaseOrderID = String.valueOf(isRegularOrder) + String.valueOf(isTxtOnline) + date + OListSettings.get(6);

        if (isRegularOrder == 1){
            remarks = ORemarks.getText().toString();
        }else{
            remarks = PORemarks.getText().toString();
        }

        controller.insertPurchaseOrder(purchaseOrderID,OListSettings.get(6),purchaseOrderDate,deliveryDate.getText().toString());

        for (int i = 0; i < laODetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laODetails.getItem(i);
            String objProduct = (String) obj.get("Item");
            controller.PMName = objProduct;
            String objQty = (String) obj.get("Qty");

            int iSONum = i + 1;

            controller.updatePromoCustomers(controller.fetchMCodeMaterials(),Integer.valueOf(objQty));
            controller.insertPurchaseOrderItem(purchaseOrderID,iSONum,controller.fetchExtMatGrp(),Integer.valueOf(objQty));

        }
    }

    private void sendOrderSMS(){

        alPrintPurchaseOrder = controller.fetchPurchaseOrder(purchaseOrderID);
        int count = 1;
        order = "";
        orderNo = "";
        String message;
        message = "";

        for(int i = 0; i < alPrintPurchaseOrder.size();i++){

            order = order + alPrintPurchaseOrder.get(i).get("MaterialCode") + ":" + alPrintPurchaseOrder.get(i).get("Qty") + "/";
            if (count%9 == 0){
            message = "DSTORD " + controller.fetchdbSettings().get(3) + "," + alPrintPurchaseOrder.get(i).get("ID") +
                            "," + alPrintPurchaseOrder.get(i).get("DeliveryDate") + "," + alPrintPurchaseOrder.get(i).get("AgentCode") + "," + order;
                Utils.sendSMS(OrderingActivity.this,message);
                order = "";
            }else if (count == alPrintPurchaseOrder.size()){
                message = "DSTORD " + controller.fetchdbSettings().get(3) + "," + alPrintPurchaseOrder.get(i).get("ID") +
                            "," + alPrintPurchaseOrder.get(i).get("DeliveryDate") + "," + alPrintPurchaseOrder.get(i).get("AgentCode") + "," + order;
                Utils.sendSMS(OrderingActivity.this,message);
            }
            count ++;

        }

      /*  if (!remarks.equals("")){
            message= "ORDREM " + alPrintPurchaseOrder.get(0).get("ID") + "," + remarks;
            Utils.sendSMS(OrderingActivity.this,message);
        }*/

        controller.updatePurchaseOrderItemAll(purchaseOrderID);

    }

    private void backUP(){
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
    }



    private void sendSms(String phoneNumber, String message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public void ViewHeaderListview() {

        hmOHeader = new ArrayList<HashMap<String, String>>();
        mOHeader = new HashMap<String, String>();
        hmODetails = new ArrayList<HashMap<String, String>>();

        mOHeader.put("Item", "ITEM");
        mOHeader.put("Qty", "QUANTITY");
        mOHeader.put("Unit", "UNIT");
        hmOHeader.add(mOHeader);

        try {
            laOHeader = new SimpleAdapter(this, hmOHeader, R.layout.item_ordering,
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

            if (isRegularOrder == 1){
                OHeader.setAdapter(laOHeader);
            }else{
                POHeader.setAdapter(laOHeader);
            }


        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        mODetail = new HashMap<String, String>();

        mODetail.put("Item", Pproducts);
        mODetail.put("Qty", Pqty);
        mODetail.put("Unit", Punit);
        hmODetails.add(mODetail);

        try {
            laODetails = new SimpleAdapter(this, hmODetails,  R.layout.item_ordering,
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

            if (isRegularOrder == 1){
                ODetails.setAdapter(laODetails);
            }else{
                PODetails.setAdapter(laODetails);
            }

        } catch (Exception e) {

        }
    }

    public void UpdateDetailListview(){

        mODetail = new HashMap<String, String>();

        mODetail.put("Item", Pproducts);
        mODetail.put("Qty", Pqty);
        mODetail.put("Unit", Punit);
        hmODetails.set(Pposition,mODetail);

        try {
            laODetails = new SimpleAdapter(this, hmODetails,  R.layout.item_ordering,
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
            if (isRegularOrder == 1){
                ODetails.setAdapter(laODetails);
            }else{
                PODetails.setAdapter(laODetails);
            }
        } catch (Exception e) {

        }
    }

    public void DeleteDetailListView(){

        mODetail = new HashMap<String, String>();

        mODetail.put("Item", Pproducts);
        mODetail.put("Qty", Pqty);
        mODetail.put("Unit", Punit);
        hmODetails.remove(mODetail);

        try {
            laODetails = new SimpleAdapter(this, hmODetails,  R.layout.item_ordering,
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
            if (isRegularOrder == 1){
                ODetails.setAdapter(laODetails);
            }else{
                PODetails.setAdapter(laODetails);
            }
        } catch (Exception e) {

        }
    }

    public void DeleteDetailListViewAll(){

        hmODetails.clear();
        laODetails.notifyDataSetChanged();

    }

    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_additemordering, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        tvDOCOk = (TextView) dialogView.findViewById(R.id.tvDOCOk);
        tvDOCNext = (TextView) dialogView.findViewById(R.id.tvDOCNext);
        btnOAdd = (Button) dialogView.findViewById(R.id.btnDOCAdd);
        btnOSub = (Button) dialogView.findViewById(R.id.btnDOCSub);
        btnOScan = (Button) dialogView.findViewById(R.id.btnDOCScan);
        product =(AutoCompleteTextView) dialogView.findViewById(R.id.acDOCProduct);
        qty =(EditText)dialogView.findViewById(R.id.etDOCQty);
        final ImageView ivDSRList =(ImageView)dialogView.findViewById(R.id.ivDOOList);
        final LinearLayout llDOAllocation = (LinearLayout) dialogView.findViewById(R.id.llDOAllocation);
        final TextView tvDOAllocationItem = (TextView) dialogView.findViewById(R.id.tvDOAllocationItem);
        final String[] products;

        if (isRegularOrder == 1){
             products = controller.fetchMNameMaterialsPacks();
            llDOAllocation.setVisibility(View.GONE);
        }else{
            products = controller.fetchMNameMaterialsPacksPromo();
        }



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


                if(isRegularOrder == 2){
                    String balance = controller.fetchBalance(product.getText().toString());
                    tvDOAllocationItem.setText(balance);
                }
                qty.requestFocus();

            }
        });

        tvDOCOk.setOnClickListener(new OnSingleClickListener() {
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
                }else if (isRegularOrder == 2 && Integer.parseInt(qty.getText().toString()) > Integer.parseInt(tvDOAllocationItem.getText().toString())){
                    messagebox("quantity must be less than allocation");
                }else{
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    Pqty = qty.getText().toString();

                    try {

                        alOPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                        Punit = alOPAmtMaterial.get(0);

                        if (ODetails.getCount() == 0){
                            ViewDetailListview();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);
                            Toasty.success(getApplicationContext(),  Pproducts + " successfully added", Toast.LENGTH_LONG).show();
                            isClickedTwice = false;
                            alertDialog.dismiss();
                        }else{
                            final ArrayList<String> alADetails = new ArrayList<String>();
                            alADetails.clear();
                            for (int i = 0; i < ODetails.getAdapter().getCount(); i++) {
                                HashMap<String, Object> obj = (HashMap<String, Object>) laODetails.getItem(i);

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

        tvDOCNext.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (product.getText().toString().equals("")) {
                    messagebox("please select product");
                }else if (!productlist.contains(product.getText().toString())){
                    messagebox("product not in list");
                }else if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0){
                    messagebox("quantity must not be zero(0)");
                }else if (isRegularOrder == 2 && Integer.parseInt(qty.getText().toString()) > Integer.parseInt(tvDOAllocationItem.getText().toString())){
                    messagebox("quantity must be less than allocation");
                }else {
                    controller.PMName = product.getText().toString();
                    Pproducts =  controller.PMName;
                    Pqty = qty.getText().toString();

                    try {

                        alOPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();
                        Punit = alOPAmtMaterial.get(0);

                        if (ODetails.getCount() == 0){
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
                            for (int i = 0; i < ODetails.getAdapter().getCount(); i++) {
                                HashMap<String, Object> obj = (HashMap<String, Object>) laODetails.getItem(i);

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

        btnOAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else{
                    intOQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intOQty.toString());
                }
            }
        });

        btnOSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) != 0){
                    intOQty = Integer.parseInt(qty.getText().toString()) - 1;

                    qty.setText(intOQty.toString());
                }
            }
        });

        ivDSRList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                product.showDropDown();
                //btnSRScan.setVisibility(View.GONE);
            }
        });

        btnOScan.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                new IntentIntegrator(OrderingActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });


        alertDialog.show();

    }

    public void DialogEditItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edititemordering, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        tvDOCDelete = (TextView) dialogView.findViewById(R.id.tvDOCDelete);
        tvDOCSave = (TextView) dialogView.findViewById(R.id.tvDOCSave);
        btnOAdd = (Button) dialogView.findViewById(R.id.btnDOCAdd);
        btnOSub = (Button) dialogView.findViewById(R.id.btnDOCSub);
        tvOItem = (TextView) dialogView.findViewById(R.id.tvDOCItem);
        tvOUnitItem = (TextView) dialogView.findViewById(R.id.tvDOCUnitItem);
        final EditText qty =(EditText)dialogView.findViewById(R.id.etDOCQty);
        final LinearLayout llDOAllocation = (LinearLayout) dialogView.findViewById(R.id.llDOAllocation);
        final TextView tvDOAllocationItem = (TextView) dialogView.findViewById(R.id.tvDOAllocationItem);

        tvOItem.setText(Pproducts);
        tvOUnitItem.setText(Punit);
        qty.setText(Pqty);

        if (isRegularOrder == 1){
            llDOAllocation.setVisibility(View.GONE);
        }else{
            String balance = controller.fetchBalance(Pproducts);
            tvDOAllocationItem.setText(balance);
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

        controller.PMName = tvOItem.getText().toString();
        alOPAmtMaterial = controller.fetchUnitQtyPriceMaterialInventory();

        /*tvOStockItem.setText(alOPAmtMaterial.get(1));
        tvOPriceItem.setText(alOPAmtMaterial.get(2));*/

        btnOAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (qty.getText().toString().equals("")){
                    qty.setText("1");
                }else {
                    intOQty = Integer.parseInt(qty.getText().toString()) + 1;
                    qty.setText(intOQty.toString());
                }

            }
        });

        btnOSub.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (intOQty != 0){
                    intOQty = Integer.parseInt(qty.getText().toString()) - 1;
                    qty.setText(intOQty.toString());
                }

            }
        });

        tvDOCDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {


                Toasty.error(getApplicationContext(),  Pproducts + " successfully deleted", Toast.LENGTH_LONG).show();
                DeleteDetailListView();

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(qty.getWindowToken(), 0);

                alertDialog.dismiss();

            }
        });

        tvDOCSave.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (qty.getText().toString().equals("")){
                    messagebox("invalid quantity");
                }else if (Integer.parseInt(qty.getText().toString()) == 0) {
                    messagebox("quantity must not be zero(0)");
                }else if (isRegularOrder == 2 && Integer.parseInt(qty.getText().toString()) > Integer.parseInt(tvDOAllocationItem.getText().toString())){
                    messagebox("quantity must be less than allocation");
                }else{
                    Pproducts = tvOItem.getText().toString();
                    Pqty = qty.getText().toString();
                    Punit = tvOUnitItem.getText().toString();

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

        if(controller.FetchNameMaterialsPacks().get(0).equals("")){
            messagebox("material not found");
        }else{
            product.setText(controller.FetchNameMaterials().get(0));
            qty.requestFocus();
        }


    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(OrderingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Ordering")
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

    void  messageboxTxt(String alerttext) {

        new AlertDialog.Builder(OrderingActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Ordering")
                .setMessage(alerttext)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }

                })
                .show();
    }


    private void scanMedia(File path) {
        Uri uri = Uri.fromFile(path);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(OrderingActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void onBackPressed() {

        Intent intent = new Intent(OrderingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
}
