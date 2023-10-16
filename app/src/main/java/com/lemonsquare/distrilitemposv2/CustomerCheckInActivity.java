package com.lemonsquare.distrilitemposv2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;

import static com.karumi.dexter.BuildConfig.APPLICATION_ID;


public class CustomerCheckInActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String mLastUpdateTime;


    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    private static final int REQUEST_CHECK_SETTINGS = 100;


    DBController controller = new DBController(this);
    TextView tvCCICustomer, tvCCILOdometer;
    EditText etCCIOdometer;
    MaterialBetterSpinner etCCIRemarks;
    List<String> CCIListSettings;
    Button btnCCISubmit;

    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    private Boolean mRequestingLocationUpdates;

    private CountDownTimer attcountdowntimer;
    ImageView img;
    Context context = this;

    TextView tvRemarks,tvCCILOMeter,tvCCINOdometer;
    Boolean isCheckIn;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_customercheckin);
        ButterKnife.bind(this);

        init();

        restoreValuesFromBundle(savedInstanceState);

        tvCCICustomer = (TextView) findViewById(R.id.tvCCICustomer);
        tvCCILOdometer = (TextView) findViewById(R.id.tvCCIOmeter);
        etCCIOdometer = (EditText) findViewById(R.id.etCCILDOmeter);
        etCCIRemarks = (MaterialBetterSpinner) findViewById(R.id.etCCIRemarks);
        btnCCISubmit = (Button) findViewById(R.id.btnCCISubmit);
        img = (ImageView) findViewById(R.id.ivImg);
        tvRemarks = (TextView) findViewById(R.id.tvCCIRemarks);
        tvCCILOMeter = (TextView) findViewById(R.id.tvCCILOMeter);
        tvCCINOdometer = (TextView) findViewById(R.id.tvCCINOdometer);
        title = (TextView) findViewById(R.id.tvTitle);
        if (!controller.fetchLogID().equals("")){
            title.setText("Check Out");
            img.setBackground(getResources().getDrawable(R.drawable.img_checkout));
            tvCCILOdometer.setVisibility(View.GONE);
            etCCIOdometer.setVisibility(View.GONE);
            etCCIRemarks.setVisibility(View.GONE);
            tvRemarks.setVisibility(View.GONE);
            tvCCINOdometer.setVisibility(View.GONE);
            tvCCILOMeter.setVisibility(View.GONE);
            isCheckIn = false;
        }else{
            img.setBackground(getResources().getDrawable(R.drawable.img_checkin));
            isCheckIn = true;
        }


        tvCCICustomer.setText(controller.PCLName);

        CCIListSettings = controller.fetchdbSettings();

        tvCCILOdometer.setText(CCIListSettings.get(17));

        controller.PCName = "Id";
        controller.PTName = "OdometerReading";
        controller.PMNumber = controller.fetchMaxNumTCTSequence();
        controller.PRemarks = "";

        String[] reason = controller.fetchCheckInRemarks();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, reason);
        etCCIRemarks.setAdapter(arrayAdapter);

        controller.Plat = 0.00;
        controller.Plong = 0.00;

        btnCCISubmit.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (!isCheckIn){
                    if(isLocationEnabled(CustomerCheckInActivity.this)){
                        btnCCISubmit.setEnabled(false);
                        etCCIOdometer.setEnabled(false);
                        etCCIRemarks.setEnabled(false);
                        btnCCISubmit.setText("PLEASE WAIT...");
                        startLocationButtonClick();
                        getlocation();
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etCCIOdometer.getWindowToken(), 0);

                    }else{
                        btnCCISubmit.setEnabled(false);
                        etCCIOdometer.setEnabled(false);
                        etCCIRemarks.setEnabled(false);
                        btnCCISubmit.setText("PLEASE WAIT...");
                        startLocationButtonClick();

                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etCCIOdometer.getWindowToken(), 0);

                    }
                }else{
                    if (etCCIOdometer.getText().toString().equals("")) {
                        etCCIOdometer.setError("please input odometer reading");
                    }else if (Utils.isStringNullOrWhiteSpace(etCCIRemarks.getText().toString())) {
                        etCCIRemarks.setError("please choose remarks");
                    }else if ((Integer.valueOf(tvCCILOdometer.getText().toString()) > Integer.valueOf(etCCIOdometer.getText().toString()))) {
                        etCCIOdometer.setError("odometer reading must be higher than the previous odometer reading");
                    }else if ((controller.PCCode.equals("INH" + controller.fetchdbSettings().get(6)))) {
                        etCCIRemarks.setError("please choose remarks");
                    } else {

                        if(isLocationEnabled(CustomerCheckInActivity.this)){
                            btnCCISubmit.setEnabled(false);
                            etCCIOdometer.setEnabled(false);
                            etCCIRemarks.setEnabled(false);
                            btnCCISubmit.setText("PLEASE WAIT...");
                            startLocationButtonClick();
                            getlocation();
                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etCCIOdometer.getWindowToken(), 0);

                        }else{
                            btnCCISubmit.setEnabled(false);
                            etCCIOdometer.setEnabled(false);
                            etCCIRemarks.setEnabled(false);
                            btnCCISubmit.setText("PLEASE WAIT...");
                            startLocationButtonClick();

                            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etCCIOdometer.getWindowToken(), 0);

                        }
                }






                }
            }
        });


    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(CustomerCheckInActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Customer Check in")
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


    void getlocation(){

        attcountdowntimer = new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                mRequestingLocationUpdates = false;
                stopLocationUpdates();
                checkin();

            }
        }.start();

    }


    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Restoring values from saved instance state
     */
    private void restoreValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("is_requesting_updates")) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean("is_requesting_updates");
            }

            if (savedInstanceState.containsKey("last_known_location")) {
                mCurrentLocation = savedInstanceState.getParcelable("last_known_location");
            }

            if (savedInstanceState.containsKey("last_updated_on")) {
                mLastUpdateTime = savedInstanceState.getString("last_updated_on");
            }
        }

        updateLocationUI();
    }
    public void checkin(){



            Timestamp tmCCIDtTime = new Timestamp(System.currentTimeMillis());
            Calendar defaultDate = Calendar.getInstance();
            DateFormat defaultDateFormat1 = new SimpleDateFormat("yyMMddHHmmss");
            DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
            String logID,logDate;
            logID = defaultDateFormat1.format(defaultDate.getTime());
            logDate = defaultDateFormat2.format(defaultDate.getTime());

            if (!controller.fetchLogID().equals("")){
                controller.updateCheckOut(logID.substring(0,6),logID.substring(6),String.valueOf(controller.Plat) +"," + String.valueOf(controller.Plong));
                controller.insertCustomerLogsItem(controller.fetchLogID(),7,logDate,0);
                controller.updateCustomerLogs();
                finish();
            }else{
                controller.insertOdometerReading(controller.PCCode,etCCIOdometer.getText().toString(),tmCCIDtTime.getTime(),etCCIRemarks.getText().toString(),controller.fetchUiDVNoOReading().get(0),controller.fetchUiDVNoOReading().get(1),controller.PMNumber);
                controller.updateSettings("LastOdometer",etCCIOdometer.getText().toString());
                DateFormat defaultDateFormat = new SimpleDateFormat("yyMMdd");
                String todayDate = defaultDateFormat.format(defaultDate.getTime());
                controller.insertLocationLog(controller.PCCode,controller.PMNumber,controller.Plat,controller.Plong,todayDate);
                controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);
                controller.PCNm = 1;
                controller.insertCustomerCheckin(logID.substring(0,6),logID.substring(6),String.valueOf(controller.Plat) +"," + String.valueOf(controller.Plong),controller.PCCode,etCCIRemarks.getText().toString());
                if (etCCIRemarks.getText().toString().equals("STORE CHECK IN")){
                    controller.insertCustomerLogs(logID,controller.PCCode,logDate,0);
                    controller.insertCustomerLogsItem(logID,1,logDate,0);
                    Intent intentMainActivity = new Intent(CustomerCheckInActivity.this, ReturnedItemActivity.class);
                    startActivity(intentMainActivity);
                    finish();
                }else{
                    controller.updateCheckOut(logID.substring(0,6),logID.substring(6),String.valueOf(controller.Plat) +"," + String.valueOf(controller.Plong));
                    controller.insertCustomerLogs(logID,controller.PCCode,logDate,1);
                    finish();
                }



            }


            //setResult(RESULT_OK);
            //finish();

    }


    /**
     * Update the UI displaying the location data
     * and toggling the buttons
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            controller.Plat = mCurrentLocation.getLatitude();
            controller.Plong = mCurrentLocation.getLongitude();
            //Toast.makeText(getApplicationContext(), location, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
        outState.putParcelable("last_known_location", mCurrentLocation);
        outState.putString("last_updated_on", mLastUpdateTime);

    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocationUI();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(CustomerCheckInActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                //Toast.makeText(AttendanceActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocationUI();
                    }
                });
    }

    public void startLocationButtonClick() {
        // Requesting ACCESS_FINE_LOCATION using Dexter library
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mRequestingLocationUpdates = true;
                        startLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            // open device settings when the permission is
                            // denied permanently
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.e(TAG, "User agreed to make required location settings changes.");
                        getlocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.e(TAG, "User chose not to make required location settings changes.");
                        mRequestingLocationUpdates = false;
                        btnCCISubmit.setEnabled(true);
                        etCCIOdometer.setEnabled(true);
                        etCCIRemarks.setEnabled(true);
                        btnCCISubmit.setText("SUBMIT");
                        Toasty.error(getApplicationContext(), "Check in failed, please allow location permission", Toast.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates && checkPermissions()) {
            startLocationUpdates();
        }

        updateLocationUI();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (mRequestingLocationUpdates) {
            stopLocationUpdates();
        }
    }
    public void onBackPressed() {
        finish();
    }
}
