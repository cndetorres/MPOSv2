package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class NewIncidentReportActivity extends Activity {

    DBController controller = new DBController(this);
    MaterialBetterSpinner mbsNIRRType;
    EditText etNIRDetails,etNIRRBy,etNIRReference;
    ImageView ivNIRCamera;
    Button btnNIRSubmit;

    File NIRfile;
    Uri NIRfileUri;
    final int NIRTakePhoto = 1;
    final int NIRPhotoGallery = 1;
    String NIRid;
    Long NIRIDt;

    final int REQUEST_CAPTURE_IMAGE = 100;
    String imageFilePath;
    private String mCurrentPhotoPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newincidentreport);

        mbsNIRRType = (MaterialBetterSpinner) findViewById(R.id.spNIRRType);
        etNIRDetails = (EditText) findViewById(R.id.etNIRDetails);
        etNIRRBy = (EditText) findViewById(R.id.etNIRRBy);
        etNIRReference = (EditText) findViewById(R.id.etNIRReference);
        ivNIRCamera = (ImageView) findViewById(R.id.ivNIRCamera);
        btnNIRSubmit = (Button) findViewById(R.id.btnNIRSubmit);

        controller.PCName = "IncidentID";
        controller.PMNumber = controller.fetchMaxNumTSequence();

        String[] strNIRRType = controller.fetchITypeIncidentTypeName();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, strNIRRType);
        mbsNIRRType.setAdapter(arrayAdapter);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());



        mbsNIRRType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                controller.PIType = StringUtils.substringBefore(mbsNIRRType.getText().toString(), "-");
                NIRid = controller.fetchIDIType();
            }
        });



        etNIRReference.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etNIRReference.getWindowToken(), 0);

                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                // Set your required file type
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Incident Image"),1001);

            }
            });

        etNIRReference.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etNIRReference.getWindowToken(), 0);

                    Intent intent = new Intent();
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    // Set your required file type
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Incident Image"),1001);
                }
            }


        });

        ivNIRCamera.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {


                /*Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, NIRfileUri);
                startActivityForResult(cameraIntent, NIRTakePhoto);*/

                /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                NIRfile = new File(getExternalCacheDir(),
                        String.valueOf(System.currentTimeMillis()) + ".jpg");
                NIRfileUri = Uri.fromFile(NIRfile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, NIRfileUri);
                startActivityForResult(intent, NIRTakePhoto);*/

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {

                File photoFile = createImageFile();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, NIRTakePhoto);
                } catch (IOException ex) {
                }

            }

        });

        btnNIRSubmit.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (isStringNullOrWhiteSpace(mbsNIRRType.getText().toString())) {
                    mbsNIRRType.setError("please choose report type");
                }else if (isStringNullOrWhiteSpace(etNIRDetails.getText().toString())){
                    etNIRDetails.setError("please input details");
                }else if (isStringNullOrWhiteSpace(etNIRRBy.getText().toString())){
                    etNIRRBy.setError("please input reported by");
                }else if (isStringNullOrWhiteSpace(etNIRReference.getText().toString())){
                    etNIRReference.setError("please pick a reference");
                }else{
                    Timestamp NIRtsDtTime = new Timestamp(System.currentTimeMillis());

                    controller.insertIncidentReport(controller.PMNumber,NIRid,NIRtsDtTime.getTime(),etNIRDetails.getText().toString(),etNIRRBy.getText().toString(),etNIRReference.getText().toString());
                    controller.updateTableSequence(Integer.valueOf(controller.PMNumber) + 1);

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

                    Intent intentNewIncdentReport = new Intent(NewIncidentReportActivity.this, IncidentReportActivity.class);
                    startActivity(intentNewIncdentReport);
                    finish();

                }
            }
        });


    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        try {

        if (requestCode == NIRTakePhoto && resultCode == RESULT_OK) {
            etNIRReference.setText(mCurrentPhotoPath);
        } else if (requestCode == 1001){
            Uri currFileURI = data.getData();
            String path = currFileURI.getPath();
            etNIRReference.setText(getPath(path));
        }else{
            etNIRReference.setText("");
        }

        } catch (Exception e) {
            etNIRReference.setText("");
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath();
        return image;
    }

    private String getPath(String Uri) {


        final String docId = Uri;
        final String[] split = docId.split(":");

        String newfilepath;
        split[0] = split[0].replace("/document/","");

        if (split.length > 1){
            if (split[0].equals("home")){
                newfilepath = "Documents/" + split[1];
            }else{
                newfilepath = split[1];
            }

        }else{
            newfilepath = "Documents";
        }

        if (split[0].equals("primary")) {
            return Environment.getExternalStorageDirectory() + "/" +  newfilepath;
        }else if (split[0].equals("home")){
            return Environment.getExternalStorageDirectory() + "/" +  newfilepath;
        }else {
            return findRemovableMediaMountPoint() +  newfilepath;
        }
    }

    private String findRemovableMediaMountPoint() {

        String ANDROID_FOLDER = "Android/data";
        String mountPoint = null;
        File[] externalFilesDirs = getExternalFilesDirs(null);
        for (File extFileDir: externalFilesDirs) {
            if (!Environment.isExternalStorageRemovable(extFileDir)) {
                continue;
            }
            String absolutePath = extFileDir.getAbsolutePath();
            mountPoint = absolutePath.substring(0, absolutePath.indexOf(ANDROID_FOLDER));
        }
        //Log.d(TAG, String.format("Found removable media mounted at %s", mountPoint));
        return mountPoint;
    }


    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(NewIncidentReportActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("New Incedent Report")
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

        MediaScannerConnection.scanFile(NewIncidentReportActivity.this,
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


    public void onBackPressed() {
        Intent IntentIncidentReportActivity = new Intent(NewIncidentReportActivity.this, IncidentReportActivity.class);
        startActivity(IntentIncidentReportActivity);
        finish();
    }

    }
