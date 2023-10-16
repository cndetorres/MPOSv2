package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import es.dmoral.toasty.Toasty;

public class LogInActivity extends Activity{

    DBController controller = new DBController(this);
    EditText etUsername,etPassword,etVehicle;
    Button btnScan,btnLogin,btnExit;
    TextView tvLVersion;

   /* private List<SettingsList> settingsLists = controller.fetchSettings();
    private SettingsList settingsList = new SettingsList();*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.etLUsername);
        etPassword = (EditText) findViewById(R.id.etLPassword);
        etVehicle = (EditText) findViewById(R.id.etLVehicle);
        btnScan = (Button) findViewById(R.id.btnLScan);
        btnLogin = (Button) findViewById(R.id.btnLLogin);
        btnExit = (Button) findViewById(R.id.btnLExit);
        tvLVersion = (TextView) findViewById(R.id.tvLVersion);

        /*settingsList = new SettingsList();

        Toasty.error(this, settingsList.getSalesDistrict(), Toast.LENGTH_LONG).show();
*/
        final Context context = getApplicationContext();
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        String MVersion = "not available";

        try {
            MVersion = packageManager.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        tvLVersion.setText("DPOS v" + MVersion);

        btnLogin.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (etUsername.getText().toString().equals("")){
                    etUsername.setError("please input username");
                }else if (etPassword.getText().toString().equals("")){
                    etPassword.setError("please input password");
                }else{
                    controller.PUName = etUsername.getText().toString();
                    controller.PPassword = etPassword.getText().toString();

                    if (controller.FetchRoleUser().get(0).equals("")){
                        etUsername.setError("invalid user/password");
                    } else if (controller.FetchRoleUser().get(0).equals("ADM")){
                        controller.updateUsers(1);
                        Intent intentMainActivity = new Intent(LogInActivity.this, MainActivity.class);
                        startActivity(intentMainActivity);
                        finish();
                    }else if (controller.FetchRoleUser().get(0).equals("AGE")){
                        if (!controller.fetchdbSettings().get(0).equals("For Salesman")){
                            etUsername.setError("you are not allowed to log in");
                        }else{

                            if(!controller.FetchSalesDistrict().get(0).equals(controller.fetchSDstSettings())){
                                etUsername.setError("This device is already used by " + controller.fetchSDstSettings());
                            }else{
                                if (etVehicle.getText().toString().equals("")){
                                    etVehicle.setError("please input vehicle number");
                                }else{
                                    if (controller.fetchCountOdometerReading().equals("1")){
                                        if (!etVehicle.getText().toString().equals(controller.fetchUiDVNoOReading().get(1))){
                                            etVehicle.setError("vehicle number did not match");
                                        }else{
                                            controller.updateUsers(1);
                                            Intent intentMainActivity = new Intent(LogInActivity.this, MainActivity.class);
                                            startActivity(intentMainActivity);
                                            finish();
                                        }
                                    }else{
                                        controller.PVVehicleNo = etVehicle.getText().toString();
                                        if (controller.fetchCountVehicleNo().equals("1")){
                                            controller.updateUsers(1);
                                            Intent intentInventoryAcceptanceActivity = new Intent(LogInActivity.this, InventoryAcceptanceActivity.class);
                                            startActivity(intentInventoryAcceptanceActivity);
                                            finish();
                                        }else{
                                            etVehicle.setError("invalid vehicle number");
                                        }
                                    }
                                }


                            }
                        }
                    }else if (controller.FetchRoleUser().get(0).equals("CHE")){
                        if (!controller.fetchdbSettings().get(0).equals("Ended Transaction")){
                            etUsername.setError("you are not allowed to log in");
                        }else{
                            controller.updateUsers(1);
                            Intent intentMainActivity = new Intent(LogInActivity.this, MainActivity.class);
                            startActivity(intentMainActivity);
                            finish();
                        }
                    }else{
                        if (!controller.fetchdbSettings().get(0).equals("Done with PID. For Recon")){
                            etUsername.setError("you are not allowed to log in");
                        }else{
                            controller.updateUsers(1);
                            Intent intentMainActivity = new Intent(LogInActivity.this, MainActivity.class);
                            startActivity(intentMainActivity);
                            finish();
                        }
                    }

                }
            }
        });

        btnScan.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                new IntentIntegrator(LogInActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();
            }
        });

        btnExit.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                new AlertDialog.Builder(LogInActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Exit")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.finishAffinity(LogInActivity.this);
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });


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

        etVehicle.setText(result);

    }

        void  messagebox(String alerttext) {

            new android.support.v7.app.AlertDialog.Builder(LogInActivity.this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Log in")
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
}
