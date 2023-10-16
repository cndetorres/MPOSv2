package com.lemonsquare.distrilitemposv2;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class ScreenActivity extends Activity {

    DBController controller = new DBController(this);
    private static int SPLASH_TIME_OUT = 1000;
    List<String> SListRIDNmUsers;
    public static final int MULTIPLE_PERMISSIONS = 10;
    String[] permissions;
    Boolean isFirstRun;
    String filename;

    static final int READ_BLOCK_SIZE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);


        permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS};

        controller.PCNm = 0;

        isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (checkPermissions()) {

            if (isFirstRun) {
                createfilepathimports();
                createfilepathexports();
                createsdcardpath();
                routesched();
                readimports();
                readexports();
                filepath();
                readvideos();
                readproductinfo();
            }else{
                readimports();
                readexports();
                filepath();
                createsdcardpath();
                readvideos();
                readproductinfo();
            }
        }

        //checkPermissions();

    }

    private void createfilepathimports(){
        try {
            FileOutputStream fileout=openFileOutput("imports.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write("/storage/emulated/0/Documents/Imports");
            outputWriter.close();
        } catch (Exception e) {

            //e.printStackTrace();
        }


    }

    private void createfilepathexports(){
        try {
            FileOutputStream fileout=openFileOutput("exports.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write("/storage/emulated/0/Documents/Exports");
            outputWriter.close();
        } catch (Exception e) {

            //e.printStackTrace();
        }
    }

    private void routesched(){


        try {
            FileOutputStream fileout=openFileOutput("routeschedule.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            outputWriter.write("");
            outputWriter.close();
        } catch (Exception e) {

            //e.printStackTrace();
        }

    }

    void readimports(){

        try {
            FileInputStream fileIn=openFileInput("imports.txt");
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
            controller.imports = s ;


        } catch (Exception e) {
            controller.imports = "";
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
            controller.exports = s ;


        } catch (Exception e) {
            controller.exports = "";
        }


    }

    private String findRemovableMediaMountPoint() {

        try{
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
        }catch (Exception e){
            return null;
        }

    }

    private  void createsdcardpath(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardpath = new File(findRemovableMediaMountPoint() + "Android/data/com.lemonsquare.distrilitemposv2/files");
            if (!sdcardpath.exists()){
                sdcardpath.mkdir();
                scanFile(sdcardpath.getAbsolutePath());
            }
        }
    }

    private void filepath(){

            /*if (controller.imports.equals("")){
                createfilepath();
                readexports();
                readimports();
            }else if (controller.exports.equals("")){
                createfilepath();
                readexports();
                readimports();
            }*/

        File importpath = new File(controller.imports);
        File exportpath = new File(controller.exports);

        if (!importpath.exists() && !exportpath.exists()) {
            messagebox("Missing upload/download Location folder path");
            if (controller.imports.contains("Android/data/com.lemonsquare.distrilitemposv2/files")){
                createfilepathimports();
                createfilepathexports();
            }else{
                importpath.mkdirs();
                exportpath.mkdirs();

                File importTxt = new File(importpath.getAbsolutePath()+"/mediascanner.txt");
                File exportTxt = new File(exportpath.getAbsolutePath()+"/mediascanner.txt");

                importTxt.mkdir();
                exportTxt.mkdir();

                scanFile(importTxt.getAbsolutePath());
                scanFile(exportTxt.getAbsolutePath());

            }




            //scanMedia(importpath);
            //scanMedia(exportpath);



        }else if (importpath.exists() && !exportpath.exists()){
            messagebox("Missing upload/download Location folder path");
            if (controller.exports.contains("Android/data/com.lemonsquare.distrilitemposv2/files")){
                createfilepathexports();
            }else{
                exportpath.mkdirs();

                File exportTxt = new File(exportpath.getAbsolutePath()+"/mediascanner.txt");
                exportTxt.mkdir();
                scanFile(exportTxt.getAbsolutePath());

            }
            //scanMedia(exportpath);
        }else if (!importpath.exists() && exportpath.exists()){
            messagebox("Missing upload/download Location folder path");
            if (controller.imports.contains("Android/data/com.lemonsquare.distrilitemposv2/files")){
                createfilepathimports();
            }else{
                importpath.mkdirs();

                File importTxt = new File(importpath.getAbsolutePath()+"/mediascanner.txt");
                importTxt.mkdir();
                scanFile(importTxt.getAbsolutePath());

            }

            //scanMedia(importpath);
        }else{


            try{
                File[] strFileName = importpath.listFiles();

                filename = "";

                for (int i = 0; i < strFileName.length;i++){
                    if (strFileName[i].toString().substring(strFileName[i].toString().lastIndexOf(".") + 1,
                            strFileName[i].toString().length()).equals("db")){
                        filename = strFileName[i].getName();
                    }
                }

                if (filename.equals("") && isFirstRun) {
                    messagebox("Database not found");
                }else if (!filename.equals("") && isFirstRun){
                    importfile();

                    controller.updateStatus();

                    getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                            .putBoolean("isFirstRun", false).commit();

                    Intent intentLogInActivity = new Intent(ScreenActivity.this, LogInActivity.class);
                    startActivity(intentLogInActivity);
                    finish();
                }else if (!filename.equals("") && controller.fetchdbSettings().get(0).equals("For Download")){

                    importfile();


                    controller.updateStatus();

                    Intent intentLogInActivity = new Intent(ScreenActivity.this, LogInActivity.class);
                    startActivity(intentLogInActivity);
                    finish();
                }else if (!filename.equals("") && !controller.fetchdbSettings().get(0).equals("For Download")){
                    messageboxlogin("A new DB was detected but not processed, change the application status to \"For Download\" to replace all the data");
                }else{

                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {

                            if (controller.fetchCountStatusUsers().equals("1")){
                                if (controller.fetchCountOdometerReading().equals("1")){
                                    Intent intentMainActivity = new Intent(ScreenActivity.this, MainActivity.class);
                                    startActivity(intentMainActivity);
                                    finish();
                                }else{
                                    if (controller.fetchRIDNmUsers().get(0).equals("ADM")){
                                        Intent intentMainActivity = new Intent(ScreenActivity.this, MainActivity.class);
                                        startActivity(intentMainActivity);
                                        finish();
                                    }else if (controller.fetchdbSettings().get(0).equals("For Download")) {
                                        Intent intentInventoryAcceptanceActivity = new Intent(ScreenActivity.this, LogInActivity.class);
                                        startActivity(intentInventoryAcceptanceActivity);
                                        finish();
                                    }else{
                                        Intent intentInventoryAcceptanceActivity = new Intent(ScreenActivity.this, InventoryAcceptanceActivity.class);
                                        startActivity(intentInventoryAcceptanceActivity);
                                        finish();
                                    }
                                }

                            }else{
                                Intent intentLogInActivity = new Intent(ScreenActivity.this, LogInActivity.class);
                                startActivity(intentLogInActivity);
                                finish();
                            }

                        }
                    }, SPLASH_TIME_OUT);

                }

            }catch (Exception e){
                Toasty.error(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                createfilepathimports();
            }

        }
    }

    private void importfile(){

        try {

            controller.getWritableDatabase();

            File sd = new File(controller.imports);
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/com.lemonsquare.distrilitemposv2/databases/mpos";
                String backupDBPath = filename;
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (backupDB.exists()) {
                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(backupDB).getChannel();
                        FileChannel dst = new FileOutputStream(currentDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();

                        Toasty.info(getApplicationContext(), "database has been updated", Toast.LENGTH_SHORT).show();

                        scanFile(backupDB.getAbsolutePath());
                        backupDB.delete();

                    }
                } else {
                    Toasty.error(getApplicationContext(), "database not found", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toasty.error(getApplicationContext(), "Database restoration failed", Toast.LENGTH_SHORT).show();
        }

    }

    private  boolean checkPermissions() {

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
    }

    private void scanMedia(File path) {
        Uri uri = Uri.fromFile(path);
        Intent scanFileIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        sendBroadcast(scanFileIntent);
    }

    void readvideos(){

        try {
            FileInputStream fileIn=openFileInput("videos.txt");
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
            controller.videos = s ;


        } catch (Exception e) {
            controller.videos = "";
        }


    }

    void readproductinfo(){

        try {
            FileInputStream fileIn=openFileInput("productinfo.txt");
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
            controller.productinfo = s ;


        } catch (Exception e) {
            controller.productinfo = "";
        }


    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(ScreenActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("DPOS Setup")
                .setCancelable(false)
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

    private void scanFile(String path) {

        MediaScannerConnection.scanFile(ScreenActivity.this,
                new String[] { path }, null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("TAG", "Finished scanning " + path);
                    }
                });
    }



    void  messageboxlogin(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(ScreenActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("DPOS Setup")
                .setCancelable(false)
                .setMessage(alerttext)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                        if (controller.fetchCountStatusUsers().equals("1")){
                            if (controller.fetchCountOdometerReading().equals("1")){
                                Intent intentMainActivity = new Intent(ScreenActivity.this, MainActivity.class);
                                startActivity(intentMainActivity);
                                finish();
                            }else{
                                if (controller.fetchRIDNmUsers().get(0).equals("ADM")){
                                    Intent intentMainActivity = new Intent(ScreenActivity.this, MainActivity.class);
                                    startActivity(intentMainActivity);
                                    finish();
                                }else{
                                    Intent intentInventoryAcceptanceActivity = new Intent(ScreenActivity.this, InventoryAcceptanceActivity.class);
                                    startActivity(intentInventoryAcceptanceActivity);
                                    finish();
                                }
                            }

                        }else{
                            Intent intentLogInActivity = new Intent(ScreenActivity.this, LogInActivity.class);
                            startActivity(intentLogInActivity);
                            finish();
                        }

                    }

                })
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissionsList[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS:{
                if (grantResults.length > 0) {
                    String permissionsDenied = "";
                    for (String per : permissionsList) {
                        if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                            permissionsDenied += "\n" + per;

                        }

                    }

                }
                return;
            }
        }
    }

}
