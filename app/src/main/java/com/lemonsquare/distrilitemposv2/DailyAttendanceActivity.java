package com.lemonsquare.distrilitemposv2;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class DailyAttendanceActivity extends Activity {

    TextView TDate,TTime;
    Button btnTTimein,btnTTimeout;
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
    Double lat,lng;
    int inout;

    final static int RQS_1 = 1;

    final SmsManager smsManager = SmsManager.getDefault();

    private CountDownTimer attcountdowntimer;
    String defaultDate;
    int ISSDR;
    LinearLayout llReliever;
    MaterialBetterSpinner relieverList;
    ImageView cancel;
    String reliever;
    long currentTimeDate;
    SimpleDateFormat notifDate = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailyattendance);

        TDate = (TextView) findViewById(R.id.tvTDate);
        TTime = (TextView) findViewById(R.id.tvTTime);
        btnTTimein = (Button) findViewById(R.id.btnTTimein);
        btnTTimeout = (Button) findViewById(R.id.btnTTimeout);
        /*llReliever = (LinearLayout) findViewById(R.id.llReliever);
        relieverList = (MaterialBetterSpinner) findViewById(R.id.spReliever);
        cancel = (ImageView) findViewById(R.id.ivCancel) ;*/

        DateFormat df2 = new SimpleDateFormat("yyyyMMdd");
        defaultDate = df2.format(Calendar.getInstance().getTime());

        currentTimeDate = System.currentTimeMillis();
        final SimpleDateFormat sdfTDate = new SimpleDateFormat("EEE, dd MMMM yyyy");



        TDate.setText(sdfTDate.format(currentTimeDate));
        reliever = "N/A";

        lat = 0.00;
        lng = 0.00;
        ISSDR = 0;

        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TTime.setText(new SimpleDateFormat("hh:mm:ss a", Locale.US).format(new Date()));
                someHandler.postDelayed(this, 1000);
            }
        }, 10);


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

                //checkin();

            }

        };

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
        enabled = locationManager .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // This is the Best And IMPORTANT part
        looper = null;

        if (controller.fetchNotTimeOutLate() == 1){
            //llReliever.setVisibility(View.GONE);
            ISSDR = 0;
            btnTTimein.setEnabled(false);
            btnTTimein.setBackgroundColor(Color.parseColor("#7E888686"));
        }else if (controller.fetchNotTimeIn() == 0){

            /*String[] strReliver = controller.fetchReliever();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(DailyAttendanceActivity.this,
                    android.R.layout.simple_dropdown_item_1line,strReliver );
            relieverList.setAdapter(adapter);

            cancel.setVisibility(View.GONE);*/

            btnTTimeout.setEnabled(false);
            btnTTimeout.setBackgroundColor(Color.parseColor("#7E888686"));
        }else if (controller.fetchNotTimeOut() == 1){
            //llReliever.setVisibility(View.GONE);
            ISSDR = 1;
            btnTTimein.setEnabled(false);
            btnTTimein.setBackgroundColor(Color.parseColor("#7E888686"));
        }else if (controller.fetchTimeInOutComplete() == 1){
            //llReliever.setVisibility(View.GONE);
            btnTTimein.setEnabled(false);
            btnTTimein.setBackgroundColor(Color.parseColor("#7E888686"));
            btnTTimeout.setEnabled(false);
            btnTTimeout.setBackgroundColor(Color.parseColor("#7E888686"));

            Toasty.info(getApplicationContext(), "Attendance already completed", Toast.LENGTH_LONG).show();
        }

        /*relieverList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() ==0){
                    cancel.setVisibility(View.GONE);
                }else{
                    cancel.setVisibility(View.VISIBLE);
                }
            }
        });*/

        /*cancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                relieverList.setText("");
                relieverList.dismissDropDown();
            }
        });*/

        btnTTimein.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new AlertDialog.Builder(DailyAttendanceActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Time in")
                        .setMessage("Are you sure you want to time in?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                inout = 1;
                                notification();
                                btnTTimein.setText("Please wait...");
                                btnTTimein.setEnabled(false);
                                initGoogleAPIClient();//Init Google API Client
                                if (!enabled) {
                                    checkPermissions();
                                }else{
                                    getlocation();
                                }
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();

            }
        });

        btnTTimeout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (controller.fetchSDR(defaultDate) == 0){
                    Toasty.info(getApplicationContext(), "Please submit daily sales report first!", Toast.LENGTH_LONG).show();
                }else{
                    new AlertDialog.Builder(DailyAttendanceActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Time out")
                            .setMessage("Are you sure you want to time out?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    inout = 2;

                                    btnTTimeout.setText("Please wait...");
                                    btnTTimeout.setEnabled(false);
                                    initGoogleAPIClient();//Init Google API Client
                                    if (!enabled) {
                                        checkPermissions();
                                    }else{
                                        getlocation();
                                    }
                                }

                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });

    }

    private void notification(){

        try{

            String currentDate = notifDate.format(currentTimeDate);
            String alarmDate = currentDate + " 17:00:00";
            SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
            Date alarmDateFormat = sdf.parse(alarmDate);

            Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
            intent.putExtra("Activity", "channel1");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), RQS_1, intent, 0);
            AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alarmDateFormat.getTime(),1000*60*30, pendingIntent);

        }catch (ParseException e){

        }

    }

    void backgroundtimer(){
        attcountdowntimer = new CountDownTimer(900000, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                getlocation();
            }
        }.start();
    }

    void getlocation(){

        attcountdowntimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                try {
                    if (ActivityCompat.checkSelfPermission(DailyAttendanceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DailyAttendanceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }else{
                        locationManager.requestSingleUpdate(criteria, locationListener, looper);
                    }
                } catch (Exception e) {
                    timeinout();
                }
            }

            public void onFinish() {
                timeinout();
                locationManager.removeUpdates(locationListener);
            }
        }.start();

    }

    public void timeinout(){

        String strDefaultTime;
        String strDefaultDateTime;
        long defaultDateTime = System.currentTimeMillis();
        SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm:ss a");
        DateFormat sdfDateTime = new SimpleDateFormat("yyMMddHHmmss");
        Timestamp tsDateTime = new Timestamp(System.currentTimeMillis());
        strDefaultTime = sdfTime.format(defaultDateTime);
        strDefaultDateTime = sdfDateTime.format(defaultDateTime);
        String message;
        reliever = "N/A";

        if (inout == 1){
            controller.insertAttendance(defaultDate,strDefaultTime,String.valueOf(lat) + "," + String.valueOf(lng),reliever);
            controller.insertTransaction("TMIN" + strDefaultDateTime ,"TMIN",controller.fetchdbSettings().get(6),controller.fetchdbSettings().get(6),tsDateTime.getTime(),0.0);
            message = "DSTATT " + controller.fetchdbSettings().get(3) + " " + controller.fetchdbSettings().get(6) + " " + "TMI" + " " + String.valueOf(lat) + "," + String.valueOf(lng) + " " + reliever;
            Utils.sendSMS(DailyAttendanceActivity.this,message);

        }else if (inout == 2){
            controller.updateAttendance(defaultDate,strDefaultTime,String.valueOf(lat) + "," + String.valueOf(lng),ISSDR);
            controller.insertTransaction("TMOUT" + strDefaultDateTime ,"TMOUT",controller.fetchdbSettings().get(6),controller.fetchdbSettings().get(6),tsDateTime.getTime(),0.0);
            message = "DSTATT " + controller.fetchdbSettings().get(3) + " " + controller.fetchdbSettings().get(6) + " " + "TMO" + " "  + String.valueOf(lat) + "," + String.valueOf(lng)  + " " + reliever;
            Utils.sendSMS(DailyAttendanceActivity.this,message);
        }



            Toasty.success(getApplicationContext(), "Attendance has been sent!", Toast.LENGTH_LONG).show();

            Intent i = new Intent(DailyAttendanceActivity.this,MainActivity.class);
            startActivity(i);
            finish();


    }

    private void initGoogleAPIClient() {
        //Without Google API Client Auto Location Dialog will not work
        mGoogleApiClient = new GoogleApiClient.Builder(DailyAttendanceActivity.this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /* Check Location Permission for Marshmallow Devices */
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 26) {
            if (ContextCompat.checkSelfPermission(DailyAttendanceActivity.this,
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(DailyAttendanceActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(DailyAttendanceActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION_INTENT_ID);

        } else {
            ActivityCompat.requestPermissions(DailyAttendanceActivity.this,
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
                            status.startResolutionForResult(DailyAttendanceActivity.this, REQUEST_CHECK_SETTINGS);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e("Settings", "Result OK");
                        updateGPSStatus("Please wait...");

                        getlocation();

                        break;
                    case RESULT_CANCELED:

                        Toasty.error(getApplicationContext(), "Attendance did not send, Please click OK to proceed", Toast.LENGTH_LONG).show();

                        if (inout == 1){
                            updateGPSStatus("TIME IN");
                            btnTTimein.setEnabled(true);
                            relieverList.setEnabled(true);
                        }else if (inout == 2){
                            updateGPSStatus("TIME OUT");
                            btnTTimeout.setEnabled(true);
                        }

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

    //Method to update GPS status text
    private void updateGPSStatus(String status) {
        if (inout == 1){
            btnTTimein.setText(status);
        }else if (inout == 2){
            btnTTimeout.setText(status);
        }

    }

    @Override
    public void onBackPressed() {

            Intent i = new Intent(DailyAttendanceActivity.this,MainActivity.class);
            startActivity(i);
            finish();

    }
}

