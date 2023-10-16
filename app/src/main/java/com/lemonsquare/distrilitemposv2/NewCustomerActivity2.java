package com.lemonsquare.distrilitemposv2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;


public class NewCustomerActivity2 extends Activity {

    DBController controller = new DBController(this);
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static GoogleApiClient mGoogleApiClient;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";

    Criteria criteria;
    android.location.LocationListener locationListener;
    LocationManager locationManager;
    Looper looper;
    boolean enabled;
    Double lat, lng;
    String pleaseWait;

    MaterialBetterSpinner etNCHBRGY, etNCHRegion, etNCBBRGY, etNCBRegion, mbsNCCType, etNCBCity, etNCBProvince, etNCHCity, etNCHProvince, orderday, deliveryday;
    CheckBox chkNCM, chkNCT, chkNCW, chkNCTH, chkNCF, chkNCS, chkNCHome;
    EditText etNCCName, etNCOName, etNCTelephone, etNCMobile,
            etNCHPCode, etNCRemarks, etNCHBHURFP, etNCHStreet, etNCHSUBDZONE, etNCBBHURFP, etNCBStreet, etNCBSUBDZONE, timeFrom, timeTo;
    TextView tvNCHCity, tvNCHProvince, tvNCHPCode, tvNCDP, tvNCHHURFP, tvNCHStreet, tvNCHSUBDZONE, tvNCHBRGY, etNCBPCode, tvNCHRegion, tvSAPAddress,
            tvNSAPAddress, tvNCCCode, tvICCCode, tvNCLimit, tvICLimit, tvNCCExpo, tvICCExpo, tvNCPaymentTerms, tvICPaymentTerms, tvSAPHAddress, tvNSAPHAddress, tvNCRemarks;
    List<String> NCListSettings;
    String NCCCode;
    ImageView ivNCPhoto, ivNCCamera;
    TabHost tabHost;
    BottomNavigationView NWBuyersInfo;
    ListView lvBIHeader, lvCDDetails, lvFHeader, lvFDetails;
    ArrayList<HashMap<String, String>> hmFHeader, hmCOHDetails, hmCDHeader, hmFDetails;
    ListAdapter laCDHeader, laFHeader;
    SimpleAdapter laCOHDetails, laFDetails;
    HashMap<String, String> mFHeader, mCOHDetail, mCDHeader, mFDetail;
    BottomNavigationView btNCNavigation, btOINavigation, btFNavigation;
    final Calendar myCalendar = Calendar.getInstance();

    int isAttachment;
    int isPhoto;
    String isAdd;
    String customerCode;
    List<HashMap<String, String>> alCustomerInfo;
    List<HashMap<String, String>> alBuyersInfo, alFrequencyInfo;
    private long mLastClickTime = 0;

    boolean changing = false;
    String[] regioncategory;


    String dbbackup = "";

    static final int READ_BLOCK_SIZE = 100;

    private CountDownTimer attcountdowntimer;

    String strBuyer, strBirthday, strGender, strAddress, strCPNo, strCategory, strFavorite, strWkNum, strCallDay, strCallSeq;
    int globalposition;
    int isBuyer = 0;
    int isFrequency = 0;
    String route;

    Button btnTimeFrom, btnTimeTo;

    String[] dayPart = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};

    String SAPStreet = "";
    String SAPCity = "";
    String SAPStreetH = "";
    String SAPCityH = "";
    String Location = "";
    byte[] imageInByte;

    //TextView gps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newcustomer2);

        locationListener = new android.location.LocationListener() {

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onLocationChanged(Location location) {

                lat = location.getLatitude();
                lng = location.getLongitude();

                //gps.setText(lng +"," + lat);

            }

        };

        readexports();

        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        // Now create a location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // This is the Best And IMPORTANT part
        looper = null;


        //new customer line

        etNCCName = (EditText) findViewById(R.id.etNCCName);
        mbsNCCType = (MaterialBetterSpinner) findViewById(R.id.spNCCType);
        etNCOName = (EditText) findViewById(R.id.etNCOName);
        chkNCM = (CheckBox) findViewById(R.id.chkNCMonday);
        chkNCT = (CheckBox) findViewById(R.id.chkNCTuesday);
        chkNCW = (CheckBox) findViewById(R.id.chkNCWednesday);
        chkNCTH = (CheckBox) findViewById(R.id.chkNCThursday);
        chkNCF = (CheckBox) findViewById(R.id.chkNCFriday);
        chkNCS = (CheckBox) findViewById(R.id.chkNCSaturday);
        etNCTelephone = (EditText) findViewById(R.id.etNCTelephone);
        etNCMobile = (EditText) findViewById(R.id.etNCMphone);

        chkNCHome = (CheckBox) findViewById(R.id.chkNCHome);
        tvNCHCity = (TextView) findViewById(R.id.tvNCHCity);
        etNCHCity = (MaterialBetterSpinner) findViewById(R.id.etNCHCity);
        tvNCHProvince = (TextView) findViewById(R.id.tvNCHProvince);
        etNCHProvince = (MaterialBetterSpinner) findViewById(R.id.etNCHProvince);
        tvNCHPCode= (TextView) findViewById(R.id.tvNCHPCode);
        etNCHPCode = (EditText) findViewById(R.id.etNCHPCode);
        etNCRemarks = (EditText) findViewById(R.id.etNCRemarks);
        tvNCDP = (TextView) findViewById(R.id.tvNCDP);

        etNCBStreet = (EditText) findViewById(R.id.etNCBStreet);
        etNCBBHURFP = (EditText) findViewById(R.id.etNCBBHURFP);
        etNCBSUBDZONE = (EditText) findViewById(R.id.etNCBSUBDZONE);

        tvNCHHURFP = (TextView) findViewById(R.id.tvNCHHURFP);
        tvNCHStreet = (TextView) findViewById(R.id.tvNCHStreet);
        tvNCHSUBDZONE = (TextView) findViewById(R.id.tvNCHSUBDZONE);
        tvNCHBRGY = (TextView)findViewById(R.id.tvNCHBRGY);

        etNCHBHURFP = (EditText) findViewById(R.id.etNCHHURFP);
        etNCHStreet = (EditText) findViewById(R.id.etNCHStreet);
        etNCHSUBDZONE = (EditText) findViewById(R.id.etNCHSUBDZONE);
        etNCHBRGY = (MaterialBetterSpinner) findViewById(R.id.etNCHBRGY);
        etNCBRegion = (MaterialBetterSpinner) findViewById(R.id.etNCBRegion);
        etNCHRegion = (MaterialBetterSpinner) findViewById(R.id.etNCHRegion);
        tvNCHRegion = (TextView) findViewById(R.id.tvNCHRegion);

        ivNCPhoto = (ImageView) findViewById(R.id.ivNCPhoto);
        ivNCCamera = (ImageView) findViewById(R.id.ivNCCamera);

        tvSAPAddress = (TextView) findViewById(R.id.tvSAPAddress);
        tvNSAPAddress = (TextView) findViewById(R.id.tvNSAPAddress);
        tvNCCCode = (TextView) findViewById(R.id.tvNCCCode);
        tvICCCode = (TextView) findViewById(R.id.tvICCCode);
        tvNCLimit = (TextView) findViewById(R.id.tvNCLimit);
        tvICLimit = (TextView) findViewById(R.id.tvICLimit);
        tvNCCExpo = (TextView) findViewById(R.id.tvNCCExpo);
        tvICCExpo = (TextView) findViewById(R.id.tvICCExpo);
        tvNCPaymentTerms = (TextView) findViewById(R.id.tvNCPaymentTerms);
        tvICPaymentTerms = (TextView) findViewById(R.id.tvICPaymentTerms);

        btNCNavigation = (BottomNavigationView) findViewById(R.id.btNCNavigation);
        btOINavigation = (BottomNavigationView) findViewById(R.id.btOINavigation);
        lvBIHeader = (ListView) findViewById(R.id.lvBIHeader);
        lvCDDetails = (ListView) findViewById(R.id.lvCDDetails);

        lvFHeader = (ListView) findViewById(R.id.lvFHeader);

        btFNavigation = (BottomNavigationView) findViewById(R.id.btNCFrequency);

        etNCBBRGY = (MaterialBetterSpinner) findViewById(R.id.etNCBBRGY);
        etNCBCity = (MaterialBetterSpinner) findViewById(R.id.etNCBCity);
        etNCBProvince = (MaterialBetterSpinner) findViewById(R.id.etNCBProvice);
        etNCBPCode = (EditText) findViewById(R.id.etNCBPCode);

        tvSAPHAddress = (TextView) findViewById(R.id.tvSAPHAddress);
        tvNSAPHAddress = (TextView) findViewById(R.id.tvNSAPHAddress);

        tvNCRemarks = (TextView) findViewById(R.id.tvNCRemarks);

        orderday = (MaterialBetterSpinner) findViewById(R.id.mbsOrderDay);
        deliveryday = (MaterialBetterSpinner) findViewById(R.id.mbsDeliveryDay);
        btnTimeFrom = (Button) findViewById(R.id.btnTimeFrom);
        btnTimeTo = (Button) findViewById(R.id.btnTimeTo);
        timeFrom = (EditText) findViewById(R.id.etTimeFrom);
        timeTo = (EditText) findViewById(R.id.etTimeTo);

        lvFDetails = (ListView) findViewById(R.id.lvFDetails);
        //gps = (TextView) findViewById(R.id.tvGPS);

        /*initGoogleAPIClient();
        if (!enabled) {
            checkPermissions();
        }else{
            runlocation();
        }*/



        btnTimeFrom.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {

                Calendar e = Calendar.getInstance();
                int hour = e.get(Calendar.HOUR_OF_DAY);
                int minute = e.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(NewCustomerActivity2.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                if (hourOfDay < 12 && hourOfDay >= 0) {
                                    timeFrom.setText(String.format("%02d:%02d", hourOfDay, minute) + " AM");
                                } else {
                                    hourOfDay -= 12;
                                    if(hourOfDay == 0) {
                                        hourOfDay = 12;
                                    }
                                    timeFrom.setText(String.format("%02d:%02d", hourOfDay, minute) + " PM");
                                }

                                //SRTime.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();

            }
        });

        btnTimeTo.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {

                Calendar e = Calendar.getInstance();
                int hour = e.get(Calendar.HOUR_OF_DAY);
                int minute = e.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(NewCustomerActivity2.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                if (hourOfDay < 12 && hourOfDay >= 0) {
                                    timeTo.setText(String.format("%02d:%02d", hourOfDay, minute) + " AM");
                                } else {
                                    hourOfDay -= 12;
                                    if(hourOfDay == 0) {
                                        hourOfDay = 12;
                                    }
                                    timeTo.setText(String.format("%02d:%02d", hourOfDay, minute) + " PM");
                                }

                                //SRTime.setText(hourOfDay + ":" + minute);
                            }
                        }, hour, minute, false);
                timePickerDialog.show();

            }
        });


        tabHost = (TabHost) findViewById(R.id.tabCADHost);
        tabHost.setup();

        pleaseWait = "0";

        Intent intent = getIntent();
        isAdd = intent.getStringExtra("isAdd");


        if (isAdd.equals("1")){
            tvSAPAddress.setVisibility(View.GONE);
            tvNSAPAddress.setVisibility(View.GONE);
            tvNCCCode.setVisibility(View.GONE);
            tvICCCode.setVisibility(View.GONE);
            tvNCLimit.setVisibility(View.GONE);
            tvICLimit.setVisibility(View.GONE);
            tvNCCExpo.setVisibility(View.GONE);
            tvICCExpo.setVisibility(View.GONE);
            tvNCPaymentTerms.setVisibility(View.GONE);
            tvICPaymentTerms.setVisibility(View.GONE);
            tvSAPHAddress.setVisibility(View.GONE);
            tvNSAPHAddress.setVisibility(View.GONE);

            String[] strNIRRType = controller.fetchCusTypeDescCustomerType();

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, strNIRRType);
            mbsNCCType.setAdapter(arrayAdapter);
            chkNCHome.setChecked(true);
            UnViewHomeAddress();

            ViewCDHeaderListview();
            ViewFHeaderListview();

        }else{
            customerCode = intent.getStringExtra("CustomerCode");
            tvICCCode.setText(customerCode);

            alCustomerInfo = controller.fetchCustomerInfo(customerCode);
            etNCCName.setText(alCustomerInfo.get(0).get("CustomerName"));
            mbsNCCType.setText(alCustomerInfo.get(0).get("CustomerType"));
            etNCOName.setText(alCustomerInfo.get(0).get("OwnerName"));
            tvICLimit.setText(alCustomerInfo.get(0).get("Limit"));
            tvICCExpo.setText(alCustomerInfo.get(0).get("CreditExpo"));
            tvICPaymentTerms.setText(alCustomerInfo.get(0).get("PaymentTerms"));

            route = alCustomerInfo.get(0).get("Route");
            chkNCM.setEnabled(false);
            chkNCT.setEnabled(false);
            chkNCW.setEnabled(false);
            chkNCTH.setEnabled(false);
            chkNCF.setEnabled(false);
            chkNCS.setEnabled(false);

            for (int i = 0;i<route.length();i++){
                if(route.charAt(i) == '1'){
                    chkNCM.setChecked(true);
                }else if(route.charAt(i) == '2'){
                    chkNCT.setChecked(true);
                }else if(route.charAt(i) == '3'){
                    chkNCW.setChecked(true);
                }else if(route.charAt(i) == '4'){
                    chkNCTH.setChecked(true);
                }else if(route.charAt(i) == '5'){
                    chkNCF.setChecked(true);
                }else if(route.charAt(i) == '6'){
                    chkNCS.setChecked(true);
                }
            }




            etNCTelephone.setText(alCustomerInfo.get(0).get("ContactNumber"));
            etNCMobile.setText(alCustomerInfo.get(0).get("MobileNumber"));
            tvNSAPAddress.setText(alCustomerInfo.get(0).get("SAPAddress"));
            etNCBBHURFP.setText(alCustomerInfo.get(0).get("UnitNo"));
            etNCBStreet.setText(alCustomerInfo.get(0).get("Street"));
            etNCBSUBDZONE.setText(alCustomerInfo.get(0).get("Subdv"));
            etNCBBRGY.setText(alCustomerInfo.get(0).get("Barangay"));
            etNCBCity.setText(alCustomerInfo.get(0).get("City"));
            etNCBProvince.setText(alCustomerInfo.get(0).get("Province"));
            etNCBRegion.setText(alCustomerInfo.get(0).get("Region"));
            etNCBPCode.setText(alCustomerInfo.get(0).get("Postal"));

            chkNCHome.setChecked(false);
            ViewHomeAddress();

            tvNSAPHAddress.setText(alCustomerInfo.get(0).get("SAPHAddress"));
            etNCHBHURFP.setText(alCustomerInfo.get(0).get("UnitNoH"));
            etNCHStreet.setText(alCustomerInfo.get(0).get("StreetH"));
            etNCHSUBDZONE.setText(alCustomerInfo.get(0).get("SubdvH"));
            etNCHBRGY.setText(alCustomerInfo.get(0).get("BarangayH"));
            etNCHCity.setText(alCustomerInfo.get(0).get("CityH"));
            etNCHProvince.setText(alCustomerInfo.get(0).get("ProvinceH"));
            etNCHRegion.setText(alCustomerInfo.get(0).get("RegionH"));
            etNCHPCode.setText(alCustomerInfo.get(0).get("PostalH"));

            orderday.setText(alCustomerInfo.get(0).get("OrderDay"));
            deliveryday.setText(alCustomerInfo.get(0).get("DelDay"));
            timeFrom.setText(alCustomerInfo.get(0).get("DelWTimeFrom"));
            timeTo.setText(alCustomerInfo.get(0).get("DelWTimeTo"));

            etNCCName.setEnabled(false);
            mbsNCCType.setEnabled(false);
            mbsNCCType.dismissDropDown();
            etNCOName.setEnabled(false);

            if (controller.fetchImage(customerCode) != null){
                Bitmap bmp = BitmapFactory.decodeByteArray(controller.fetchImage(customerCode), 0, controller.fetchImage(customerCode).length);
                Bitmap resizedbmp = Bitmap.createScaledBitmap(bmp, 400, 250, false);
                ivNCPhoto.setImageBitmap(resizedbmp);
            }

            ViewCDHeaderListview();
            alBuyersInfo = controller.fetchBuyingCustomer(customerCode);

            if (alBuyersInfo.size() > 0){
                for (int i = 0;i < alBuyersInfo.size();i++){
                    AddDetails(alBuyersInfo.get(i).get("BuyerName"),alBuyersInfo.get(i).get("Birthday"),alBuyersInfo.get(i).get("Gender"),alBuyersInfo.get(i).get("Address"),alBuyersInfo.get(i).get("ContactNo"),alBuyersInfo.get(i).get("Category"),alBuyersInfo.get(i).get("Favorite"));
                }
            }

            ViewFHeaderListview();
            alFrequencyInfo = controller.fetchFrequencyCustomer(customerCode);
            if (alFrequencyInfo.size() > 0){
                for (int i = 0;i < alFrequencyInfo.size();i++){
                    AddSeqInfo(alFrequencyInfo.get(i).get("WkNum"),alFrequencyInfo.get(i).get("CallDay"),alFrequencyInfo.get(i).get("CallSeq"));
                }
            }


            etNCRemarks.setVisibility(View.GONE);
            tvNCRemarks.setVisibility(View.GONE);

        }

        TabHost.TabSpec spec = tabHost.newTabSpec("OWNER'S INFO");
        spec.setContent(R.id.tabOWNERSINFO);
        spec.setIndicator("OWNER'S INFO");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("FREQUENCY");
        spec.setContent(R.id.tabFREQUENCY);
        spec.setIndicator("FREQUENCY");
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec("BUYER'S INFO");
        spec.setContent(R.id.tabBUYERSINFO);
        spec.setIndicator("BUYER'S INFO");
        tabHost.addTab(spec);

        etNCBCity.setEnabled(false);
        etNCBProvince.setEnabled(false);

        etNCHCity.setEnabled(false);
        etNCHProvince.setEnabled(false);

        etNCBBRGY.setEnabled(false);
        etNCHBRGY.setEnabled(false);

        regioncategory = controller.fetchRegioncat();
        String[] regionhcategory = controller.fetchRegioncat();

        ArrayAdapter<String> arrayRegionAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, regioncategory);
        etNCBRegion.setAdapter(arrayRegionAdapter);


        ArrayAdapter<String> arrayOrderAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, dayPart);
        orderday.setAdapter(arrayOrderAdapter);

        ArrayAdapter<String> arrayDeliveryAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, dayPart);
        deliveryday.setAdapter(arrayDeliveryAdapter);


        orderday.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(orderday.getText().toString().equals("MONDAY")){
                    deliveryday.setText("TUESDAY");
                }else if(orderday.getText().toString().equals("TUESDAY")){
                    deliveryday.setText("WEDNESDAY");
                }else if(orderday.getText().toString().equals("WEDNESDAY")){
                    deliveryday.setText("THURSDAY");
                }else if(orderday.getText().toString().equals("THURSDAY")){
                    deliveryday.setText("FRIDAY");
                }else if(orderday.getText().toString().equals("FRIDAY")){
                    deliveryday.setText("SATURDAY");
                }else if(orderday.getText().toString().equals("SATURDAY")){
                    deliveryday.setText("MONDAY");
                }

            }
        });

        deliveryday.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(deliveryday.getText().toString().equals("MONDAY")){
                    orderday.setText("SATURDAY");
                }else if(deliveryday.getText().toString().equals("TUESDAY")){
                    orderday.setText("MONDAY");
                }else if(deliveryday.getText().toString().equals("WEDNESDAY")){
                    orderday.setText("TUESDAY");
                }else if(deliveryday.getText().toString().equals("THURSDAY")){
                    orderday.setText("WEDNESDAY");
                }else if(deliveryday.getText().toString().equals("FRIDAY")){
                    orderday.setText("THURSDAY");
                }else if(deliveryday.getText().toString().equals("SATURDAY")){
                    orderday.setText("FRIDAY");
                }
            }
        });


        etNCBRegion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCBProvince.setEnabled(true);
                etNCBProvince.setText("");
                etNCBCity.setText("");
                etNCBBRGY.setText("");
                etNCBStreet.setText("");
                etNCBSUBDZONE.setText("");
                etNCBBHURFP.setText("");
                etNCBPCode.setText("");
                String[] provcategory = controller.fetchProvinceCat(etNCBRegion.getText().toString());
                ArrayAdapter<String> arrayProvAdapter = new ArrayAdapter<String>(NewCustomerActivity2.this,
                        android.R.layout.simple_dropdown_item_1line, provcategory);
                etNCBProvince.setAdapter(arrayProvAdapter);
                etNCBProvince.requestFocus();
            }
        });

        etNCBProvince.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCBCity.setEnabled(true);
                etNCBCity.setText("");
                etNCBBRGY.setText("");
                etNCBStreet.setText("");
                etNCBSUBDZONE.setText("");
                etNCBBHURFP.setText("");
                etNCBPCode.setText("");
                String[] citymuncategory = controller.fetchCityMunicipalityCat(etNCBProvince.getText().toString());
                ArrayAdapter<String> arrayCityMunAdapter = new ArrayAdapter<String>(NewCustomerActivity2.this,
                        android.R.layout.simple_dropdown_item_1line, citymuncategory);
                etNCBCity.setAdapter(arrayCityMunAdapter);
                etNCBCity.requestFocus();
            }
        });

        etNCBCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCBBRGY.setEnabled(true);
                etNCBBRGY.setText("");
                etNCBStreet.setText("");
                etNCBSUBDZONE.setText("");
                etNCBBHURFP.setText("");
                etNCBPCode.setText("");
                if (controller.fetchpostal(etNCBCity.getText().toString()).equals("")){
                    etNCBPCode.setEnabled(true);
                }else{
                    etNCBPCode.setText(controller.fetchpostal(etNCBCity.getText().toString()));
                }

                String[] citymuncategory = controller.fetchBrgy(etNCBCity.getText().toString());
                ArrayAdapter<String> arrayCityMunAdapter = new ArrayAdapter<String>(NewCustomerActivity2.this,
                        android.R.layout.simple_dropdown_item_1line, citymuncategory);
                etNCBBRGY.setAdapter(arrayCityMunAdapter);
                etNCBBRGY.requestFocus();
            }
        });

        etNCBBRGY.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCBStreet.setText("");
                etNCBSUBDZONE.setText("");
                etNCBBHURFP.setText("");
                etNCBSUBDZONE.requestFocus();
            }
        });


        //HOME ADDRESS

        ArrayAdapter<String> arrayRegionHAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, regionhcategory);
        etNCHRegion.setAdapter(arrayRegionHAdapter);

        etNCHRegion.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCHProvince.setEnabled(true);
                etNCHProvince.setText("");
                etNCHCity.setText("");
                etNCHBRGY.setText("");
                etNCHStreet.setText("");
                etNCHSUBDZONE.setText("");
                etNCHBHURFP.setText("");
                etNCHPCode.setText("");
                String[] provcategory = controller.fetchProvinceCat(etNCHRegion.getText().toString());
                ArrayAdapter<String> arrayProvAdapter = new ArrayAdapter<String>(NewCustomerActivity2.this,
                        android.R.layout.simple_dropdown_item_1line, provcategory);
                etNCHProvince.setAdapter(arrayProvAdapter);
                etNCHProvince.requestFocus();
            }
        });

        etNCHProvince.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCHCity.setEnabled(true);
                etNCHCity.setText("");
                etNCHBRGY.setText("");
                etNCHStreet.setText("");
                etNCHSUBDZONE.setText("");
                etNCHBHURFP.setText("");
                etNCHPCode.setText("");
                String[] citymuncategory = controller.fetchCityMunicipalityCat(etNCHProvince.getText().toString());
                ArrayAdapter<String> arrayCityMunAdapter = new ArrayAdapter<String>(NewCustomerActivity2.this,
                        android.R.layout.simple_dropdown_item_1line, citymuncategory);
                etNCHCity.setAdapter(arrayCityMunAdapter);
                etNCHCity.requestFocus();
            }
        });

        etNCHCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCHBRGY.setEnabled(true);
                etNCHBRGY.setText("");
                etNCHStreet.setText("");
                etNCHSUBDZONE.setText("");
                etNCHBHURFP.setText("");
                etNCHPCode.setText("");

                if (controller.fetchpostal(etNCHCity.getText().toString()).equals("")){
                    etNCHPCode.setEnabled(true);
                }else{
                    etNCHPCode.setText(controller.fetchpostal(etNCHCity.getText().toString()));
                }
                String[] citymuncategory = controller.fetchBrgy(etNCHCity.getText().toString());
                ArrayAdapter<String> arrayCityMunAdapter = new ArrayAdapter<String>(NewCustomerActivity2.this,
                        android.R.layout.simple_dropdown_item_1line, citymuncategory);
                etNCHBRGY.setAdapter(arrayCityMunAdapter);
                etNCHBRGY.requestFocus();
            }
        });

        etNCHBRGY.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                etNCHStreet.setText("");
                etNCHSUBDZONE.setText("");
                etNCHBHURFP.setText("");
                etNCHSUBDZONE.requestFocus();
            }
        });

        btNCNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcd_addbuyersinfo:

                                isBuyer = 0;
                                AddBuyersInfo();

                                break;

                            case R.id.mcd_submit:

                                if (isStringNullOrWhiteSpace(etNCCName.getText().toString()) && isAdd.equals("1")){
                                    Toasty.error(getApplicationContext(),"please input trade name",Toast.LENGTH_LONG).show();
                                    etNCCName.requestFocus();
                                }else if (mbsNCCType.getText().toString().equals("") && isAdd.equals("1")){
                                    Toasty.error(getApplicationContext(),"please select channel type",Toast.LENGTH_LONG).show();
                                    mbsNCCType.requestFocus();
                                }else if (isStringNullOrWhiteSpace(etNCOName.getText().toString()) && isAdd.equals("1")){
                                    Toasty.error(getApplicationContext(),"please input owner name",Toast.LENGTH_LONG).show();
                                    etNCOName.requestFocus();
                                }else if (NCVisitDays().equals("")){
                                    Toasty.error(getApplicationContext(),"please check visit days",Toast.LENGTH_LONG).show();
                                }else if (isStringNullOrWhiteSpace(orderday.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose order day",Toast.LENGTH_LONG).show();
                                    orderday.requestFocus();
                                }else if (isStringNullOrWhiteSpace(deliveryday.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose delivery day",Toast.LENGTH_LONG).show();
                                    deliveryday.requestFocus();
                                }else if (isStringNullOrWhiteSpace(timeFrom.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose delivery window time from",Toast.LENGTH_LONG).show();
                                    timeFrom.requestFocus();
                                }else if (isStringNullOrWhiteSpace(timeTo.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose delivery window time to",Toast.LENGTH_LONG).show();
                                    timeTo.requestFocus();
                                }else if (etNCTelephone.length() == 0 && etNCMobile.length() == 0){
                                    Toasty.error(getApplicationContext(),"please input telephone/mobile no",Toast.LENGTH_LONG).show();
                                }else if (etNCTelephone.length() > 0 && etNCTelephone.length() < 8){
                                    Toasty.error(getApplicationContext(),"invalid telephone number",Toast.LENGTH_LONG).show();
                                }else if (etNCMobile.length() > 0 && etNCMobile.length() < 11){
                                    Toasty.error(getApplicationContext(),"invalid mobile number",Toast.LENGTH_LONG).show();
                                }else if (etNCMobile.length() > 2 && !etNCMobile.getText().toString().substring(0,2).equals("09")){
                                    Toasty.error(getApplicationContext(),"invalid mobile number",Toast.LENGTH_LONG).show();
                                }else if (isStringNullOrWhiteSpace(etNCBRegion.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose region",Toast.LENGTH_LONG).show();
                                    etNCBRegion.requestFocus();
                                }else if (!Arrays.asList(regioncategory).contains(etNCBRegion.getText().toString())){
                                    Toasty.error(getApplicationContext(),"region not on the list",Toast.LENGTH_LONG).show();
                                    etNCBRegion.requestFocus();
                                }else if (isStringNullOrWhiteSpace(etNCBProvince.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose province",Toast.LENGTH_LONG).show();
                                    etNCBProvince.requestFocus();
                                }else if (isStringNullOrWhiteSpace(etNCBCity.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose municipality/city",Toast.LENGTH_LONG).show();
                                    etNCBCity.requestFocus();
                                }else if (isStringNullOrWhiteSpace(etNCBBRGY.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please choose brgy",Toast.LENGTH_LONG).show();
                                    etNCBBHURFP.requestFocus();
                                }else if (isStringNullOrWhiteSpace(etNCBStreet.getText().toString())){
                                    Toasty.error(getApplicationContext(),"please input street",Toast.LENGTH_LONG).show();
                                    etNCBStreet.requestFocus();
                                }else if (lvFDetails.getCount() == 0){
                                    Toasty.error(getApplicationContext(),"please add frequency",Toast.LENGTH_LONG).show();
                                }else{
                                    Toasty.info(getApplicationContext(),"Please wait...",Toast.LENGTH_LONG).show();

                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                        return false;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();
                                    pleaseWait = "1";
                                    disable();
                                    initGoogleAPIClient();//Init Google API Client
                                    if (!enabled) {
                                        checkPermissions();
                                    }else{
                                        getlocation();
                                    }
                                }

                                break;
                        }
                        return true;
                    }
                });


        btFNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcd_addfrequency:

                                isFrequency = 0;
                                AddSeqInfo();

                                break;

                            case R.id.mcd_next:

                                    tabHost.setCurrentTab(2);

                                break;
                        }
                        return true;
                    }
                });

        btOINavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.moi_next:
                                    tabHost.setCurrentTab(1);
                                break;
                        }
                        return true;
                    }
                });


        //ViewCOHDetailListview();

        controller.PCName = "Id";
        controller.PTName = "Customer";
        controller.PMNumber = controller.fetchMaxNumTCTSequence();

        controller.dbLReturns = 0.00;

        ivNCCamera.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(intent, 7);

            }

        });


        NCListSettings = controller.fetchdbSettings();

        controller.PDefaultPricelist = NCListSettings.get(14);

        DateFormat df = new SimpleDateFormat("yyMMdd");
        String date = df.format(Calendar.getInstance().getTime());

        NCCCode = "";

        NCCCode = "NW" + date + NCListSettings.get(1) + controller.PMNumber;



        chkNCHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    UnViewHomeAddress();
                    etNCBPCode.requestFocus();
                } else {
                    ViewHomeAddress();
                    etNCHBHURFP.requestFocus();
                }
            }
        });

        tvNCDP.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                DialogDataPrivacy();
            }
        });


        lvCDDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(position);

                String objBuyer = (String) obj.get("Buyer");
                strBuyer = objBuyer;
                String objBirthday = (String) obj.get("Birthday");
                strBirthday = objBirthday ;
                String objGender = (String) obj.get("Gender");
                strGender= objGender;
                String objAddress = (String) obj.get("Address");
                strAddress= objAddress;
                String objCPNo = (String) obj.get("CPNo");
                strCPNo= objCPNo;
                String objCategory = (String) obj.get("Category");
                strCategory= objCategory;
                String objFavorite = (String) obj.get("Favorite");
                strFavorite= objFavorite;
                globalposition = position;

                isBuyer = 1;

                AddBuyersInfo();

            }
        });


        lvFDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                HashMap<String, Object> obj = (HashMap<String, Object>) laFDetails.getItem(position);
                String objWkNum = (String) obj.get("WkNum");
                strWkNum = objWkNum;
                String objCallDay = (String) obj.get("CallDay");
                strCallDay = objCallDay ;
                String objCallSeq = (String) obj.get("CallSeq");
                strCallSeq= objCallSeq;
                globalposition = position;

                isFrequency = 1;

                AddSeqInfo();

            }
        });

        lat = 0.00;
        lng = 0.00;
        isAttachment = 0;
        isPhoto = 0;


    }

    public void AddBuyersInfo() {

        final Dialog alertDialog = new Dialog(NewCustomerActivity2.this);

        alertDialog.setContentView(R.layout.dialog_buyersinfo);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);


        final MaterialBetterSpinner spBIFavorites = (MaterialBetterSpinner) alertDialog.findViewById(R.id.spBIFavorites);
        final EditText etBIName = (EditText) alertDialog.findViewById(R.id.etBIName);
        ImageView ivBICalendar = (ImageView) alertDialog.findViewById(R.id.ivBICalendar);
        final EditText etBIDate = (EditText)alertDialog.findViewById(R.id.etBIDate);
        final RadioButton rbBIMale = (RadioButton) alertDialog.findViewById(R.id.rbBIMale);
        RadioButton rbBIFemale = (RadioButton) alertDialog.findViewById(R.id.rbBIFemale);
        final EditText etBIAddress = (EditText)alertDialog.findViewById(R.id.etBIAddress);
        final EditText etBICNo = (EditText)alertDialog.findViewById(R.id.etBICNo);
        final EditText etBIFavorites = (EditText)alertDialog.findViewById(R.id.etBIFavorites);
        TextView tvBICancel = (TextView) alertDialog.findViewById(R.id.tvBICancel);
        TextView tvBIAdd = (TextView) alertDialog.findViewById(R.id.tvBIAdd);

        if (isBuyer == 1){
            etBIName.setText(strBuyer);
            etBIDate.setText(strBirthday);
            if (strGender.equals("MALE")){
                rbBIMale.setChecked(true);
            }else{
                rbBIFemale.setChecked(true);
            }
            etBIAddress.setText(strAddress);
            etBICNo.setText(strCPNo);
            spBIFavorites.setText(strCategory);
            etBIFavorites.setText(strFavorite);
            tvBIAdd.setText("UPDATE");
        }

        String[] favoritecategory = {"FOOD","COLOR","PLACE","OTHERS"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, favoritecategory);
        spBIFavorites.setAdapter(arrayAdapter);

        final DatePickerDialog.OnDateSetListener datePickerListener2 = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String myFormat = "MM/dd";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);
                etBIDate.setText(sdf.format(myCalendar.getTime()));

            }

        };

        ivBICalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar e = Calendar.getInstance();
                int mYear = e.get(Calendar.YEAR);
                int mMonth = e.get(Calendar.MONTH);
                int mDay = e.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog2 =
                        new DatePickerDialog(NewCustomerActivity2.this, datePickerListener2, mYear, mMonth, mDay);

                datePickerDialog2.show();


            }
        });



        tvBICancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etBIName.getWindowToken(), 0);
                alertDialog.dismiss();
            }
            });

        tvBIAdd.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (isStringNullOrWhiteSpace(etBIName.getText().toString())){
                    Toasty.error(getApplicationContext(),"please input buyers name",Toast.LENGTH_LONG).show();
                }else if (isStringNullOrWhiteSpace(etBIDate.getText().toString())){
                    Toasty.error(getApplicationContext(),"please choose buyers birthday",Toast.LENGTH_LONG).show();
                }else if (isStringNullOrWhiteSpace(etBIAddress.getText().toString())) {
                    Toasty.error(getApplicationContext(),"please input buyers address",Toast.LENGTH_LONG).show();
                }else if (isStringNullOrWhiteSpace(etBICNo.getText().toString())) {
                    Toasty.error(getApplicationContext(),"please input contact number",Toast.LENGTH_LONG).show();
                }else if (etBICNo.getText().toString().length() < 11) {
                    Toasty.error(getApplicationContext(),"invalid contact number",Toast.LENGTH_LONG).show();
                }else if (isStringNullOrWhiteSpace(spBIFavorites.getText().toString())) {
                    Toasty.error(getApplicationContext(),"please choose buyers favorite",Toast.LENGTH_LONG).show();
                }else if (isStringNullOrWhiteSpace(etBIFavorites.getText().toString())) {
                    Toasty.error(getApplicationContext(),"please input buyers favorite",Toast.LENGTH_LONG).show();
                }else if (lvCDDetails.getCount() == 3 && isBuyer == 0){
                    Toasty.error(getApplicationContext(),"you have reach the maximum number of buyers info to add",Toast.LENGTH_LONG).show();
                }else{
                    String gender;
                    if (rbBIMale.isChecked()){
                        gender = "MALE";
                    }else{
                        gender = "FEMALE";
                    }

                    if (isBuyer == 0){
                        AddDetails(etBIName.getText().toString(),etBIDate.getText().toString(),gender,etBIAddress.getText().toString(),etBICNo.getText().toString(),spBIFavorites.getText().toString(),etBIFavorites.getText().toString());
                    }else{
                        UpdateDetails(etBIName.getText().toString(),etBIDate.getText().toString(),gender,etBIAddress.getText().toString(),etBICNo.getText().toString(),spBIFavorites.getText().toString(),etBIFavorites.getText().toString());
                        alertDialog.dismiss();
                    }


                    Toasty.success(getApplicationContext(),"buyers info successfully saved",Toast.LENGTH_LONG).show();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etBIName.getWindowToken(), 0);

                    etBIName.getText().clear();
                    etBIAddress.getText().clear();
                    etBIDate.getText().clear();
                    etBIAddress.getText().clear();
                    etBICNo.getText().clear();
                    etBIFavorites.getText().clear();
                    spBIFavorites.setText("");
                }
            }
        });

        alertDialog.show();

        Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }
    public void AddSeqInfo() {

        final Dialog alertDialog = new Dialog(NewCustomerActivity2.this);

        alertDialog.setContentView(R.layout.dialog_frequency);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);

        final Window window = alertDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        final MaterialBetterSpinner spWkNum = (MaterialBetterSpinner) alertDialog.findViewById(R.id.spWkNum);
        final MaterialBetterSpinner spCallDay = (MaterialBetterSpinner) alertDialog.findViewById(R.id.spCallDay);
        final EditText etSeq = (EditText) alertDialog.findViewById(R.id.etSeq);

        /*spCallDay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    window.setGravity(Gravity.TOP);
                }
            }
        });

        spCallDay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                window.setGravity(Gravity.CENTER);
                etSeq.requestFocus();
            }


        });
*/

        TextView tvBICancel = (TextView) alertDialog.findViewById(R.id.tvBICancel);
        TextView tvBIAdd = (TextView) alertDialog.findViewById(R.id.tvBIAdd);
        TextView tvBIDelete = (TextView) alertDialog.findViewById(R.id.tvBIDelete);

        if (isFrequency == 1){
            spWkNum.setText(strWkNum);
            spCallDay.setText(strCallDay);
            etSeq.setText(strCallSeq);
            tvBIAdd.setText("UPDATE");
        }else{
            spWkNum.setText("");
            spCallDay.setText("");
            etSeq.setText("");
            tvBIDelete.setVisibility(View.GONE);
        }

        etSeq.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (!changing && etSeq.getText().toString().startsWith("0")){
                    changing = true;
                    etSeq.setText(etSeq.getText().toString().replace("0", ""));
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


        final String[] weeknum = {"1","2","3","4"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, weeknum);
        spWkNum.setAdapter(arrayAdapter);

        String[] day = {"MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY"};
        ArrayAdapter<String> arrayAdapterDay = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, day);
        spCallDay.setAdapter(arrayAdapterDay);



        tvBIAdd.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (isFrequency == 0){

                    if(isStringNullOrWhiteSpace(spWkNum.getText().toString())){
                        Toasty.info(getApplicationContext(),"Please choose week number",Toast.LENGTH_LONG).show();
                    }else if (isStringNullOrWhiteSpace(spCallDay.getText().toString())){
                        Toasty.info(getApplicationContext(),"Please choose call day",Toast.LENGTH_LONG).show();
                    }else if (isStringNullOrWhiteSpace(etSeq.getText().toString())){
                        Toasty.info(getApplicationContext(),"Please input sequency",Toast.LENGTH_LONG).show();
                    }else{

                        ArrayList<String> alFDetails = new ArrayList<String>();

                        if (lvFDetails.getCount() > 0 ){
                            alFDetails.clear();
                            for (int i = 0; i < lvFDetails.getAdapter().getCount(); i++) {
                                HashMap<String, Object> obj = (HashMap<String, Object>) laFDetails.getItem(i);
                                String objWkNum = (String) obj.get("WkNum");
                                alFDetails.add(objWkNum);
                            }
                        }

                        if (alFDetails.contains(spWkNum.getText().toString())){
                            Toasty.info(getApplicationContext(),"Week number already on the list",Toast.LENGTH_LONG).show();
                        }else{
                            AddSeqInfo(spWkNum.getText().toString(),spCallDay.getText().toString(),etSeq.getText().toString());
                            spWkNum.setText("");
                            spCallDay.setText("");
                            etSeq.getText().clear();
                            Toasty.success(getApplicationContext(),"Frequency successfully Added",Toast.LENGTH_LONG).show();
                        }


                    }


                }else{

                    if(isStringNullOrWhiteSpace(spWkNum.getText().toString())){
                        Toasty.info(getApplicationContext(),"Please choose week number",Toast.LENGTH_LONG).show();
                    }else if (isStringNullOrWhiteSpace(spCallDay.getText().toString())){
                        Toasty.info(getApplicationContext(),"Please choose call day",Toast.LENGTH_LONG).show();
                    }else if (isStringNullOrWhiteSpace(etSeq.getText().toString())){
                        Toasty.info(getApplicationContext(),"Please input sequency",Toast.LENGTH_LONG).show();
                    }else{

                        ArrayList<String> alFDetails = new ArrayList<String>();
                        if (lvFDetails.getCount() > 0 ){
                            alFDetails.clear();
                            for (int i = 0; i < lvFDetails.getAdapter().getCount(); i++) {
                                if (i != globalposition){
                                    HashMap<String, Object> obj = (HashMap<String, Object>) laFDetails.getItem(i);
                                    String objWkNum = (String) obj.get("WkNum");
                                    alFDetails.add(objWkNum);
                                }
                            }
                        }

                        if (alFDetails.contains(spWkNum.getText().toString())){
                            Toasty.info(getApplicationContext(),"Week number already on the list",Toast.LENGTH_LONG).show();
                        }else{
                            UpdateDetails(spWkNum.getText().toString(),spCallDay.getText().toString(),etSeq.getText().toString());
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etSeq.getWindowToken(), 0);
                            Toasty.info(getApplicationContext(),"Frequency successfully updated",Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }

                }


            }
        });

        tvBICancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

            alertDialog.dismiss();

            }
        });

        tvBIDelete.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                DeleteSequence(spWkNum.getText().toString(),spCallDay.getText().toString(),etSeq.getText().toString());
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSeq.getWindowToken(), 0);
                Toasty.info(getApplicationContext(),"Frequency successfully deleted",Toast.LENGTH_LONG).show();
                alertDialog.dismiss();

            }
        });



        alertDialog.show();


    }

    public void ViewCDHeaderListview() {

        hmCDHeader = new ArrayList<HashMap<String, String>>();
        mCDHeader = new HashMap<String, String>();
        hmCOHDetails = new ArrayList<HashMap<String, String>>();

        mCDHeader.put("Buyer", "BUYER NAME");
        mCDHeader.put("CPNo", "CONTACT #");
        hmCDHeader.add(mCDHeader);

        try {
            laCDHeader = new SimpleAdapter(this, hmCDHeader, R.layout.item_buyersinfo,
                    new String[]{"Buyer","CPNo"}, new int[]{
                    R.id.rowsBuyer,R.id.rowsCPNo}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rbuyer = (TextView) view.findViewById(R.id.rowsBuyer);
                    TextView rcpno = (TextView) view.findViewById(R.id.rowsCPNo);


                    if (position % 2 == 0) {
                        rbuyer.setTextColor(Color.WHITE);
                        rcpno.setTextColor(Color.WHITE);
                        rbuyer.setTypeface(null, Typeface.BOLD);
                        rcpno.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            lvBIHeader.setAdapter(laCDHeader);
        } catch (Exception e) {

        }
    }

    public void ViewFHeaderListview() {

        hmFHeader = new ArrayList<HashMap<String, String>>();
        mFHeader = new HashMap<String, String>();
        hmFDetails = new ArrayList<HashMap<String, String>>();

        mFHeader.put("WkNum", "WEEK NUM");
        mFHeader.put("CallDay", "CALL DAY");
        mFHeader.put("CallSeq", "CALL SEQ");
        hmFHeader.add(mFHeader);

        try {
            laFHeader = new SimpleAdapter(this, hmFHeader, R.layout.items_frequency,
                    new String[]{"WkNum","CallDay","CallSeq"}, new int[]{
                    R.id.rowsWkNum,R.id.rowsCallDay,R.id.rowsSeq}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rwknum = (TextView) view.findViewById(R.id.rowsWkNum);
                    TextView rcallday = (TextView) view.findViewById(R.id.rowsCallDay);
                    TextView rseq = (TextView) view.findViewById(R.id.rowsSeq);

                    if (position % 2 == 0) {
                        rwknum.setTextColor(Color.WHITE);
                        rcallday.setTextColor(Color.WHITE);
                        rseq.setTextColor(Color.WHITE);
                        rwknum.setTypeface(null, Typeface.BOLD);
                        rcallday.setTypeface(null, Typeface.BOLD);
                        rseq.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            lvFHeader.setAdapter(laFHeader);
        } catch (Exception e) {

        }
    }

    public void AddSeqInfo(String WkNum,String CallDay,String CallSeq) {

        mFDetail = new HashMap<String, String>();
        mFDetail.put("WkNum", WkNum);
        mFDetail.put("CallDay", CallDay);
        mFDetail.put("CallSeq", CallSeq);
        hmFDetails.add(mFDetail);


        try {
            laFDetails = new SimpleAdapter(this, hmFDetails,R.layout.items_frequency,
                    new String[]{"WkNum","CallDay","CallSeq"}, new int[]{
                    R.id.rowsWkNum,R.id.rowsCallDay,R.id.rowsSeq}) {
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

            lvFDetails.setAdapter(laFDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateDetails(String WkNum,String CallDay,String CallSeq) {


        mFDetail = new HashMap<String, String>();
        mFDetail.put("WkNum", WkNum);
        mFDetail.put("CallDay", CallDay);
        mFDetail.put("CallSeq", CallSeq);
        hmFDetails.set(globalposition,mFDetail);
        laFDetails.notifyDataSetChanged();

    }

    public void DeleteSequence(String WkNum,String CallDay,String CallSeq) {


        mFDetail = new HashMap<String, String>();
        mFDetail.put("WkNum", WkNum);
        mFDetail.put("CallDay", CallDay);
        mFDetail.put("CallSeq", CallSeq);
        hmFDetails.remove(globalposition);
        laFDetails.notifyDataSetChanged();

    }

    public void AddDetails(String Buyer,String Birthday,String Gender,String Address,String CPNo,String Category,String Favorite) {

        mCOHDetail = new HashMap<String, String>();
        mCOHDetail.put("Buyer",Buyer);
        mCOHDetail.put("Birthday",Birthday);
        mCOHDetail.put("Gender",Gender);
        mCOHDetail.put("Address",Address);
        mCOHDetail.put("CPNo",CPNo);
        mCOHDetail.put("Category",Category);
        mCOHDetail.put("Favorite",Favorite);
        hmCOHDetails.add(mCOHDetail);


        try {
            laCOHDetails = new SimpleAdapter(this, hmCOHDetails,R.layout.item_buyersinfo,
                    new String[]{"Buyer","CPNo"}, new int[]{
                    R.id.rowsBuyer,R.id.rowsCPNo}) {
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

            lvCDDetails.setAdapter(laCOHDetails);
        } catch (Exception e) {

        }
    }

    public void UpdateDetails(String Buyer,String Birthday,String Gender,String Address,String CPNo,String Category,String Favorite) {

        mCOHDetail = new HashMap<String, String>();

        mCOHDetail.put("Buyer",Buyer);
        mCOHDetail.put("Birthday",Birthday);
        mCOHDetail.put("Gender",Gender);
        mCOHDetail.put("Address",Address);
        mCOHDetail.put("CPNo",CPNo);
        mCOHDetail.put("Category",Category);
        mCOHDetail.put("Favorite",Favorite);
        hmCOHDetails.set(globalposition,mCOHDetail);
        laCOHDetails.notifyDataSetChanged();

    }



    public void DialogDataPrivacy() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NewCustomerActivity2.this);
        LayoutInflater inflater = NewCustomerActivity2.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_dataprivacy, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        TextView tvDROk = (TextView) dialogView.findViewById(R.id.tvDROk);

        TextView tvRDP = (TextView) dialogView.findViewById(R.id.tvRDP);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tvRDP.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }


        tvDROk.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });



        alertDialog.show();

    }

    public void ViewHomeAddress(){

        tvNCHHURFP.setVisibility(View.VISIBLE);
        tvNCHStreet.setVisibility(View.VISIBLE);
        tvNCHSUBDZONE.setVisibility(View.VISIBLE);
        tvNCHBRGY.setVisibility(View.VISIBLE);

        etNCHBHURFP.setVisibility(View.VISIBLE);
        etNCHStreet.setVisibility(View.VISIBLE);
        etNCHSUBDZONE.setVisibility(View.VISIBLE);
        etNCHBRGY.setVisibility(View.VISIBLE);

        tvNCHCity.setVisibility(View.VISIBLE);
        etNCHCity.setVisibility(View.VISIBLE);
        tvNCHProvince.setVisibility(View.VISIBLE);
        etNCHProvince.setVisibility(View.VISIBLE);
        tvNCHPCode.setVisibility(View.VISIBLE);
        etNCHPCode.setVisibility(View.VISIBLE);
        tvNCHRegion.setVisibility(View.VISIBLE);
        etNCHRegion.setVisibility(View.VISIBLE);

        if (isAdd.equals("2")){
            tvSAPHAddress.setVisibility(View.VISIBLE);
            tvNSAPHAddress.setVisibility(View.VISIBLE);
        }
    }

    public void UnViewHomeAddress(){

        tvNCHHURFP.setVisibility(View.GONE);
        tvNCHStreet.setVisibility(View.GONE);
        tvNCHSUBDZONE.setVisibility(View.GONE);
        tvNCHBRGY.setVisibility(View.GONE);

        etNCHBHURFP.setVisibility(View.GONE);
        etNCHStreet.setVisibility(View.GONE);
        etNCHSUBDZONE.setVisibility(View.GONE);
        etNCHBRGY.setVisibility(View.GONE);

        tvNCHCity.setVisibility(View.GONE);
        etNCHCity.setVisibility(View.GONE);
        tvNCHProvince.setVisibility(View.GONE);
        etNCHProvince.setVisibility(View.GONE);
        tvNCHPCode.setVisibility(View.GONE);
        etNCHPCode.setVisibility(View.GONE);
        tvNCHRegion.setVisibility(View.GONE);
        etNCHRegion.setVisibility(View.GONE);

        etNCHBHURFP.getText().clear();
        etNCHStreet.getText().clear();
        etNCHSUBDZONE.getText().clear();
        etNCHBRGY.getText().clear();
        etNCHCity.getText().clear();
        etNCHProvince.getText().clear();
        etNCHPCode.getText().clear();
        etNCHRegion.getText().clear();

        if (isAdd.equals("2")){
            tvSAPHAddress.setVisibility(View.GONE);
            tvNSAPHAddress.setVisibility(View.GONE);
        }
    }

    private String NCVisitDays() {
        List<String> visitDays = new ArrayList<>();
        if (chkNCM.isChecked()) {
            visitDays.add("1");
        }
        if (chkNCT.isChecked()) {
            visitDays.add("2");
        }
        if (chkNCW.isChecked()) {
            visitDays.add("3");
        }
        if (chkNCTH.isChecked()) {
            visitDays.add("4");
        }
        if (chkNCF.isChecked()) {
            visitDays.add("5");
        }
        if (chkNCS.isChecked()) {
            visitDays.add("6");
        }
        return TextUtils.join(",", visitDays);
    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(NewCustomerActivity2.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("New Customer")
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

    private void scanMedia(File path) {
        Uri uri = Uri.fromFile(path);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(NewCustomerActivity2.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }

    public static boolean isStringNullOrWhiteSpace(String value) {
        if (value == null) {
            return true;
        }

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                return false;
            }
        }

        return true;
    }




    // LOCATION

    void getlocation(){
        //savenewcustomer();

        attcountdowntimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                try {
                    if (ActivityCompat.checkSelfPermission(NewCustomerActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewCustomerActivity2.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }else{
                        locationManager.requestSingleUpdate(criteria, locationListener, looper);

                    }
                } catch (Exception e) {
                    attcountdowntimer.cancel();
                    savenewcustomer();
                }
            }

            public void onFinish() {
                savenewcustomer();
                locationManager.removeUpdates(locationListener);
            }
        }.start();

    }

    void runlocation(){

        attcountdowntimer = new CountDownTimer(500000, 1000) {
            public void onTick(long millisUntilFinished) {
                try {
                    if (ActivityCompat.checkSelfPermission(NewCustomerActivity2.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewCustomerActivity2.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }else{
                        locationManager.requestSingleUpdate(criteria, locationListener, looper);

                    }
                } catch (Exception e) {
                    attcountdowntimer.cancel();

                }
            }

            public void onFinish() {
                locationManager.removeUpdates(locationListener);
            }
        }.start();

    }

    int ifExists = 0;
    public void savenewcustomer(){

        if (isAdd.equals("1")){

            Log.e("NCCCODE", NCCCode);
            if(controller.fetchCountCustomer(NCCCode) == 0){
                if(controller.fetchCountCustomerName(etNCCName.getText().toString()) > 0) {
                    Log.e("Customer Name", "Entered customer name " + etNCCName.getText().toString() + " exists. Check address");
                    enable();
//                    Toasty.info(getApplicationContext(),"Entered customer exists.",Toast.LENGTH_SHORT).show();
                    ifExists = 1;
//                    Log.e("Customer Address", etNCBRegion.getText().toString() + ", " + etNCBProvince.getText().toString() + ", " + etNCBCity.getText().toString() +", " + etNCBBRGY.getText().toString());
                    if(controller.fetchCountCustomerAddress(etNCBBRGY.getText().toString().trim(),etNCBCity.getText().toString().trim(),etNCBProvince.getText().toString().trim(),etNCBRegion.getText().toString().trim()) > 0) {
                        Log.e("Customer Address", "Address exists. Checking owner name");
                        enable();
//                        Toasty.info(getApplicationContext(),"Entered address exists.",Toast.LENGTH_SHORT).show();
                        ifExists = 1;

                        if(controller.fetchCountCustomerContactPerson(etNCOName.getText().toString()) > 0) {
                            Log.e("Owner Name", "Entered owner name " + etNCOName.getText().toString() + " exists. Failed to add the customer.");
                            enable();
//                            Toasty.info(getApplicationContext(),"Entered owner name exists.",Toast.LENGTH_SHORT).show();
                            ifExists = 1;
                        } else {
//                            addNewCustomer();
                            ifExists = 0;
                            Log.e("Owner Name", "Entered owner name " + etNCOName.getText().toString() + " not exists. Customer can add.");
                        }
                    } else {
                        Log.e("Customer Address", "Address not exists. We can add the customer.");
                        ifExists = 0;
//                        addNewCustomer();
                    }
                } else {
                    ifExists = 0;
                    Log.e("Customer Name", "Entered customer name " + etNCCName.getText().toString() + " not exists. Add customer to local DB.");
//                    addNewCustomer();
                }
            }

            if(ifExists == 0) {
                addNewCustomer();
                Toasty.info(getApplicationContext(),"Adding customer. Please wait.",Toast.LENGTH_SHORT).show();
            } else {
                Toasty.info(getApplicationContext(),"Customer already exists, please check details.",Toast.LENGTH_SHORT).show();
            }
        }
        else {

            int Status;

            if (customerCode.substring(1,2).equals("NW")){
                Status = 6;
            }else{
                Status = 7;
            }

            controller.updateCustomer(customerCode,etNCTelephone.getText().toString(),SAPCity,SAPStreet,etNCBBHURFP.getText().toString(),etNCBStreet.getText().toString(),etNCBSUBDZONE.getText().toString(),etNCBBRGY.getText().toString(),
                    etNCBCity.getText().toString(),etNCBProvince.getText().toString(),etNCBPCode.getText().toString(),etNCBRegion.getText().toString(),etNCMobile.getText().toString(),etNCHBHURFP.getText().toString(),
                    etNCHStreet.getText().toString(),etNCHSUBDZONE.getText().toString(),etNCHBRGY.getText().toString(),etNCHCity.getText().toString(),etNCHProvince.getText().toString(),SAPStreetH,etNCHPCode.getText().toString(),SAPCityH,
                    etNCHRegion.getText().toString(),Location,imageInByte,Status,String.valueOf(lvFDetails.getCount()),orderday.getText().toString(),
                    deliveryday.getText().toString(),timeFrom.getText().toString(),timeTo.getText().toString());

            controller.deleteCustomerFrequency(customerCode);
            controller.deleteCustomerBuying(customerCode);

            if (lvCDDetails.getCount() > 0){
                for (int i = 0; i < laCOHDetails.getCount(); i++) {
                    HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
                    String objBuyer = (String) obj.get("Buyer");
                    strBuyer = objBuyer;
                    String objBirthday = (String) obj.get("Birthday");
                    strBirthday = objBirthday ;
                    String objGender = (String) obj.get("Gender");
                    strGender= objGender;
                    String objAddress = (String) obj.get("Address");
                    strAddress= objAddress;
                    String objCPNo = (String) obj.get("CPNo");
                    strCPNo= objCPNo;
                    String objCategory = (String) obj.get("Category");
                    strCategory= objCategory;
                    String objFavorite = (String) obj.get("Favorite");
                    strFavorite= objFavorite;

                    String strCustomerCode;

                    if (isAdd.equals("1")){
                        strCustomerCode = NCCCode;
                    }else{
                        strCustomerCode = customerCode;
                    }
                    controller.insertCustomerBuying(strCustomerCode,strBuyer,strGender,strCPNo,strCategory,strFavorite,strBirthday,strAddress);
                }
            }

            if (laFDetails.getCount() > 0){
                for (int i = 0; i < laFDetails.getCount(); i++) {
                    HashMap<String, Object> obj = (HashMap<String, Object>) laFDetails.getItem(i);
                    String objWkNum = (String) obj.get("WkNum");
                    strWkNum = objWkNum;
                    String objCallDay = (String) obj.get("CallDay");
                    strCallDay = objCallDay ;
                    String objCallSeq = (String) obj.get("CallSeq");
                    strCallSeq= objCallSeq;

                    String strCustomerCode;

                    if (isAdd.equals("1")){
                        strCustomerCode = NCCCode;
                    }else{
                        strCustomerCode = customerCode;
                    }
                    controller.insertCustomerSequence(strCustomerCode,strWkNum,strCallDay,strCallSeq);
                }
            }

            otherCustomerDetails();

            locationManager.removeUpdates(locationListener);

            Toasty.success(getApplicationContext(),"Customer has been successfully updated",Toast.LENGTH_LONG).show();
            finish();
        }


//
//        if(isAdd.equals("1")){
//
//        }else{
//
//
//        }

    }

    private void addNewCustomer() {

        if (chkNCHome.isChecked()){
            etNCHBHURFP.setText(etNCBBHURFP.getText().toString());
            etNCHStreet.setText(etNCBStreet.getText().toString());
            etNCHSUBDZONE.setText(etNCBSUBDZONE.getText().toString());
            etNCHBRGY.setText(etNCBBRGY.getText().toString());
            etNCHCity.setText(etNCBCity.getText().toString());
            etNCHProvince.setText(etNCBProvince.getText().toString());
            etNCHPCode.setText(etNCBPCode.getText().toString());
            etNCHRegion.setText(etNCBRegion.getText().toString());
        }

        SAPStreet = etNCBBHURFP.getText().toString() + " " + etNCBStreet.getText().toString() + " " + etNCBSUBDZONE.getText().toString() + " " + etNCBBRGY.getText().toString();
        SAPCity = etNCBCity.getText().toString() + " " + etNCBProvince.getText().toString();

        SAPStreetH = etNCHBHURFP.getText().toString() + " " + etNCHStreet.getText().toString() + " " + etNCHSUBDZONE.getText().toString() + " " + etNCHBRGY.getText().toString();
        SAPCityH = etNCHCity.getText().toString() + " " + etNCHProvince.getText().toString();

        Location = String.valueOf(lat) + "," + String.valueOf(lng);

//        byte[] imageInByte;

        if (isPhoto == 0 && isAdd.equals("1")){
            imageInByte = null;
        }else if (isPhoto == 0 && controller.fetchImage(customerCode) == null){
            imageInByte = null;
        }else{
            Bitmap bitmap = ((BitmapDrawable) ivNCPhoto.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            imageInByte = baos.toByteArray();
        }

        controller.insertCustomer(NCCCode,etNCCName.getText().toString(),etNCOName.getText().toString(),etNCTelephone.getText().toString(),NCVisitDays(),NCListSettings.get(14),NCListSettings.get(6),SAPCity,SAPStreet,
        etNCBBHURFP.getText().toString(),etNCBStreet.getText().toString(),etNCBSUBDZONE.getText().toString(),etNCBBRGY.getText().toString(),etNCBCity.getText().toString(),etNCBProvince.getText().toString(),
        etNCBPCode.getText().toString(),etNCBRegion.getText().toString(),StringUtils.substringBefore(mbsNCCType.getText().toString(), "-"),etNCMobile.getText().toString(),
        etNCHBHURFP.getText().toString(),etNCHStreet.getText().toString(),etNCHSUBDZONE.getText().toString(),etNCHBRGY.getText().toString(),etNCHCity.getText().toString(),etNCHProvince.getText().toString(),
        SAPStreetH,etNCHPCode.getText().toString(),SAPCityH,etNCHRegion.getText().toString(),controller.PMNumber, NCListSettings.get(9),etNCRemarks.getText().toString(),Location,imageInByte,String.valueOf(lvFDetails.getCount()),orderday.getText().toString(),
        deliveryday.getText().toString(),timeFrom.getText().toString(),timeTo.getText().toString());

        controller.PCLName = NCCCode + "-" + etNCCName.getText().toString();

        controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);

        locationManager.removeUpdates(locationListener);

        otherCustomerDetails();

//        if (lvCDDetails.getCount() > 0){
//            for (int i = 0; i < laCOHDetails.getCount(); i++) {
//                HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
//                String objBuyer = (String) obj.get("Buyer");
//                strBuyer = objBuyer;
//                String objBirthday = (String) obj.get("Birthday");
//                strBirthday = objBirthday ;
//                String objGender = (String) obj.get("Gender");
//                strGender= objGender;
//                String objAddress = (String) obj.get("Address");
//                strAddress= objAddress;
//                String objCPNo = (String) obj.get("CPNo");
//                strCPNo= objCPNo;
//                String objCategory = (String) obj.get("Category");
//                strCategory= objCategory;
//                String objFavorite = (String) obj.get("Favorite");
//                strFavorite= objFavorite;
//
//                String strCustomerCode;
//
//                if (isAdd.equals("1")){
//                    strCustomerCode = NCCCode;
//                }else{
//                    strCustomerCode = customerCode;
//                }
//                controller.insertCustomerBuying(strCustomerCode,strBuyer,strGender,strCPNo,strCategory,strFavorite,strBirthday,strAddress);
//            }
//        }
//
//        if (laFDetails.getCount() > 0){
//            for (int i = 0; i < laFDetails.getCount(); i++) {
//                HashMap<String, Object> obj = (HashMap<String, Object>) laFDetails.getItem(i);
//                String objWkNum = (String) obj.get("WkNum");
//                strWkNum = objWkNum;
//                String objCallDay = (String) obj.get("CallDay");
//                strCallDay = objCallDay ;
//                String objCallSeq = (String) obj.get("CallSeq");
//                strCallSeq= objCallSeq;
//
//                String strCustomerCode;
//
//                if (isAdd.equals("1")){
//                    strCustomerCode = NCCCode;
//                }else{
//                    strCustomerCode = customerCode;
//                }
//                controller.insertCustomerSequence(strCustomerCode,strWkNum,strCallDay,strCallSeq);
//            }
//        }

        File exportpath = new File(dbbackup);
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
        //Toasty.success(getApplicationContext(), "new customer successfully saved!", Toast.LENGTH_LONG).show();

        new AlertDialog.Builder(NewCustomerActivity2.this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("New Customer")
            .setCancelable(false)
            .setMessage("Customer successfully created, do you want to transact now?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    controller.Prscl = 2;

                    controller.PIsSOrder = 1;

                    controller.PPayment = 0;

                    controller.PIndicator = 0;

                    controller.dbGAmt = 0.00;
                    controller.dbNSales = 0.00;
                    controller.dbCGiven = 0.00;
                    controller.PDiscAmt = 0.00;
                    controller.PDiscount = 0.00;

                    controller.PNCCode = NCCCode + etNCCName.getText().toString();



                    Intent intentNewCustomerCheckInActivity = new Intent(NewCustomerActivity2.this, NewCustomerCheckInActivity.class);
                    startActivity(intentNewCustomerCheckInActivity);

                    finish();

                }

            })
            .setNegativeButton("No",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }

            })
            .show();
    }

    private void otherCustomerDetails() {
        if (lvCDDetails.getCount() > 0){
            for (int i = 0; i < laCOHDetails.getCount(); i++) {
                HashMap<String, Object> obj = (HashMap<String, Object>) laCOHDetails.getItem(i);
                String objBuyer = (String) obj.get("Buyer");
                strBuyer = objBuyer;
                String objBirthday = (String) obj.get("Birthday");
                strBirthday = objBirthday ;
                String objGender = (String) obj.get("Gender");
                strGender= objGender;
                String objAddress = (String) obj.get("Address");
                strAddress= objAddress;
                String objCPNo = (String) obj.get("CPNo");
                strCPNo= objCPNo;
                String objCategory = (String) obj.get("Category");
                strCategory= objCategory;
                String objFavorite = (String) obj.get("Favorite");
                strFavorite= objFavorite;

                String strCustomerCode;

                if (isAdd.equals("1")){
                    strCustomerCode = NCCCode;
                }else{
                    strCustomerCode = customerCode;
                }
                controller.insertCustomerBuying(strCustomerCode,strBuyer,strGender,strCPNo,strCategory,strFavorite,strBirthday,strAddress);
            }
        }

        if (laFDetails.getCount() > 0){
            for (int i = 0; i < laFDetails.getCount(); i++) {
                HashMap<String, Object> obj = (HashMap<String, Object>) laFDetails.getItem(i);
                String objWkNum = (String) obj.get("WkNum");
                strWkNum = objWkNum;
                String objCallDay = (String) obj.get("CallDay");
                strCallDay = objCallDay ;
                String objCallSeq = (String) obj.get("CallSeq");
                strCallSeq= objCallSeq;

                String strCustomerCode;

                if (isAdd.equals("1")){
                    strCustomerCode = NCCCode;
                }else{
                    strCustomerCode = customerCode;
                }
                controller.insertCustomerSequence(strCustomerCode,strWkNum,strCallDay,strCallSeq);
            }
        }
    }

    private void initGoogleAPIClient() {
        //Without Google API Client Auto Location Dialog will not work
        mGoogleApiClient = new GoogleApiClient.Builder(NewCustomerActivity2.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /* Check Location Permission for Marshmallow Devices */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (ContextCompat.checkSelfPermission(NewCustomerActivity2.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED)
                requestLocationPermission();
            else
                showSettingDialog();
        } else
            showSettingDialog();

    }

    /*  Show Popup to access User Permission  */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(NewCustomerActivity2.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(NewCustomerActivity2.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);

        } else {
            ActivityCompat.requestPermissions(NewCustomerActivity2.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);
        }
    }

    /* Show Location Access Dialog */
    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Setting priotity of Location request to high
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        updateGPSStatus("Please wait...");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(NewCustomerActivity2.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    private void disable(){
        etNCCName.setEnabled(false);
        etNCOName.setEnabled(false);
        etNCTelephone.setEnabled(false);
        etNCMobile.setEnabled(false);
        etNCRemarks.setEnabled(false);
        etNCHBHURFP.setEnabled(false);
        etNCHStreet.setEnabled(false);
        etNCHSUBDZONE.setEnabled(false);
        etNCBBHURFP.setEnabled(false);
        etNCBStreet.setEnabled(false);
        etNCBSUBDZONE.setEnabled(false);
        mbsNCCType.setEnabled(false);
        etNCBCity.setEnabled(false);
        etNCBProvince.setEnabled(false);
        etNCBBRGY.setEnabled(false);
        etNCHBRGY.setEnabled(false);
        etNCHCity.setEnabled(false);
        etNCHProvince.setEnabled(false);
    }

    private void enable(){

        if(isAdd.equals("1")){
            etNCCName.setEnabled(true);
            etNCOName.setEnabled(true);
            mbsNCCType.setEnabled(true);
        }

        etNCTelephone.setEnabled(true);
        etNCMobile.setEnabled(true);
        etNCRemarks.setEnabled(true);
        etNCHBHURFP.setEnabled(true);
        etNCHStreet.setEnabled(true);
        etNCHSUBDZONE.setEnabled(true);
        etNCBBHURFP.setEnabled(true);
        etNCBStreet.setEnabled(true);
        etNCBSUBDZONE.setEnabled(true);

        etNCBCity.setEnabled(true);
        etNCBProvince.setEnabled(true);
        etNCBBRGY.setEnabled(true);
        etNCHBRGY.setEnabled(true);
        etNCHCity.setEnabled(true);
        etNCHProvince.setEnabled(true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e("Settings", "Result OK");
                        updateGPSStatus("Please wait...");

                        runlocation();

                        break;
                    case RESULT_CANCELED:

                        Toasty.error(getApplicationContext(), "Unable to get location, Please click OK to proceed", Toast.LENGTH_LONG).show();
                        enable();
                        break;
                }
                break;

            case 7:
                switch (resultCode) {
                    case RESULT_OK:

                        isPhoto = 1;

                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        Bitmap bmpimg = Bitmap.createScaledBitmap(bitmap, 400, 250, true);

                        ivNCPhoto.setImageBitmap(bmpimg);

                        break;

                    case RESULT_CANCELED:

                        isPhoto = 0;
                        Toasty.error(getApplicationContext(), "Unable to capture image", Toast.LENGTH_LONG).show();

                        break;
                }
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gpsLocationReceiver, new IntentFilter(BROADCAST_ACTION));//Register broadcast receiver to check the status of GPS
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unregister receiver on destroy
        if (gpsLocationReceiver != null)
            unregisterReceiver(gpsLocationReceiver);
    }

    //Run on UI
    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            showSettingDialog();
        }
    };

    /* Broadcast receiver to check status of GPS */
    private BroadcastReceiver gpsLocationReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //If Action is Location
            if (intent.getAction().matches(BROADCAST_ACTION)) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                //Check if GPS is turned ON or OFF
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.e("About GPS", "GPS is Enabled in your device");
                    updateGPSStatus("GPS is Enabled in your device");
                } else {
                    //If GPS turned OFF show Location Dialog
                    new Handler().postDelayed(sendUpdatesToUI, 10);
                    // showSettingDialog();
                    updateGPSStatus("GPS is Disabled in your device");
                    Log.e("About GPS", "GPS is Disabled in your device");
                }

            }
        }
    };

    /* On Request permission method to check the permisison is granted or not for Marshmallow+ Devices  */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_INTENT_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //If permission granted show location dialog if APIClient is not null
                    if (mGoogleApiClient == null) {
                        initGoogleAPIClient();
                        showSettingDialog();
                    } else
                        showSettingDialog();


                } else {
                    updateGPSStatus("Location Permission denied.");
                    Toasty.error(getApplicationContext(), "Location Permission denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    void readexports(){

        try {
            FileInputStream fileIn=openFileInput("exports.txt");
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
            dbbackup = s ;


        } catch (Exception e) {
            dbbackup = "";
        }


    }

    //Method to update GPS status text
    private void updateGPSStatus(String status) {
        Log.e(status,"Result");
    }

    @Override
    public void onBackPressed() {

        if(!pleaseWait.equals("1")){
            finish();
        }
    }
}

