package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.DocumentsContract;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import es.dmoral.toasty.Toasty;


public class DatabaseSettingsActivity extends Activity {

    DBController controller = new DBController(this);
    ListView DSDetails;
    ArrayList<HashMap<String, String>> hmDSDetails;
    ListAdapter laDSDetails;
    HashMap<String, String> mDSDetail;
    String[] sDSDetails = new String[]{"UPLOAD LOCATION","DOWNLOAD LOCATION","PRINTER",
            "SMS GATEWAY NO","SMS SORT KEY","ROUTE SCHEDULE","TRANSACT","VIDEO COMMERCIAL LOCATION","PRODUCT INFO LOCATION",
            "PROMO LOCATION"};

    CheckBox chkRSMonday,chkRSTuesday,chkRSWednesday,chkRSThursday,chkRSFriday,chkRSSaturday;

    String printername,routeschedule,imports,exports,transact,videos,productinfo,promoinfo;
    static final int READ_BLOCK_SIZE = 100;

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

    int Location;

    private long mLastClickTime = 0;

    TextView tvTitle,tvCancel,tvSave;
    String PHeader,PDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_databasesettings);

        DSDetails = (ListView) findViewById(R.id.lvDSDetail);


        readimports();
        readexports();
        readprintername();
        readrouteschedule();
        readtransact();
        readvideos();
        readproductinfo();
        readpromoinfo();
        ViewListview();


        DSDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                switch(position) {

                    case 0:

                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intent, 43);
                        Location = 1;

                        break;

                    case 1:

                        Intent intentexport = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intentexport, 42);
                        Location = 2;


                        break;

                    case 2:

                        PrinterList();

                        break;
                    case 3:

                        DialogSettings();

                        break;
                    case 5:

                        RouteScheduling();

                        break;

                    case 6:


                        if (hmDSDetails.get(position).get("Detail").equals("YES")){
                            transact = "NO";
                        }else{
                            transact = "YES";
                        }

                        try {
                        FileOutputStream fileout=openFileOutput("transact.txt", MODE_PRIVATE);
                        OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                        outputWriter.write(transact);
                        outputWriter.close();

                        hmDSDetails.clear();
                        readtransact();

                        ViewListview();

                    } catch (Exception e) {
                        //e.printStackTrace();
                    }

                        break;

                    case 7:

                        Intent intentvideo = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intentvideo, 44);

                        break;
                    case 8:

                        Intent intentproductinfo = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intentproductinfo, 45);

                        break;

                    case 9:

                        Intent intentpromoinfo = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(intentpromoinfo, 46);

                        break;

                        default:

                            break;

                }

            }
        });


    }

    public void DialogSettings() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DatabaseSettingsActivity.this);
        LayoutInflater inflater = DatabaseSettingsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_settings, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        tvTitle = (TextView)dialogView.findViewById(R.id.tvDSTitle);
        tvCancel = (TextView)dialogView.findViewById(R.id.tvDSCancel);
        tvSave = (TextView)dialogView.findViewById(R.id.tvDSSave);
        final EditText dsDetail =(EditText)dialogView.findViewById(R.id.etDSDetail);



        tvTitle.setText("SMSGatewayNo");
        dsDetail.setHint("SMSGatewayNo");
        dsDetail.setText(controller.fetchdbSettings().get(18));
        dsDetail.setSelection(dsDetail.getText().length());

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controller.updateSettings("SMSGatewayNo",dsDetail.getText().toString());
                hmDSDetails.clear();
                ViewListview();
                alertDialog.dismiss();

                Toasty.info(getApplicationContext(), "SMSGatewayNo has been updated", Toast.LENGTH_LONG).show();
            }
        });

        alertDialog.show();

    }


    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 43) {

                Uri uri = data.getData();
                String path = getPath(uri);

                if (path.equals("")) {
                    messagebox("File path should be: Android/data/com.lemonsquare.mposv2/files");
                }
                    try {
                        FileOutputStream fileout=openFileOutput("exports.txt", MODE_PRIVATE);
                        OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                        outputWriter.write(path);
                        outputWriter.close();

                        hmDSDetails.clear();
                        readexports();
                        ViewListview();

                    } catch (Exception e) {

                    }
            }else if (requestCode == 42) {

                Uri uri = data.getData();
                String path = getPath(uri);

                if (path.equals("")){
                    messagebox("File path should be: Android/data/com.lemonsquare.mposv2/files");
                }
                    try {
                        FileOutputStream fileout=openFileOutput("imports.txt", MODE_PRIVATE);
                        OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                        outputWriter.write(path);
                        outputWriter.close();

                        hmDSDetails.clear();
                        readimports();
                        ViewListview();

                    } catch (Exception e) {

                    }

                }else if (requestCode == 44){

                Uri uri = data.getData();
                String path = getPath(uri);

                if (path.equals("")){
                    messagebox("File path should be: Android/data/com.lemonsquare.mposv2/files");
                }else{
                    try {
                        FileOutputStream fileout=openFileOutput("videos.txt", MODE_PRIVATE);
                        OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                        outputWriter.write(path);
                        outputWriter.close();

                        hmDSDetails.clear();
                        readvideos();
                        ViewListview();

                    } catch (Exception e) {

                    }
                }

            }else if(requestCode == 45){
                Uri uri = data.getData();
                String path = getPath(uri);

                if (path.equals("")){
                    messagebox("File path should be: Android/data/com.lemonsquare.mposv2/files");
                }
                try {
                    FileOutputStream fileout=openFileOutput("productinfo.txt", MODE_PRIVATE);
                    OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                    outputWriter.write(path);
                    outputWriter.close();

                    hmDSDetails.clear();
                    readproductinfo();
                    ViewListview();

                } catch (Exception e) {

                }


            }else if(requestCode == 46){
                Uri uri = data.getData();
                String path = getPath(uri);

                if (path.equals("")){
                    messagebox("File path should be: Android/data/com.lemonsquare.mposv2/files");
                }
                try {
                    FileOutputStream fileout=openFileOutput("promoinfo.txt", MODE_PRIVATE);
                    OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                    outputWriter.write(path);
                    outputWriter.close();

                    hmDSDetails.clear();
                    readpromoinfo();
                    ViewListview();

                } catch (Exception e) {

                }


            }
        }
    }

    private String getPath(Uri uri) {


        final String docId = DocumentsContract.getTreeDocumentId(uri);
        final String[] split = docId.split(":");

        String newfilepath;

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
            if (newfilepath.contains("Android/data/com.lemonsquare.mposv2/files")){
                return   findRemovableMediaMountPoint() + newfilepath;
            }else{
                return   "";
            }

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


    public void ViewListview() {

        hmDSDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sDSDetails.length; i++) {
            mDSDetail = new HashMap<String, String>();
            mDSDetail.put("Header", sDSDetails[i]);
            if (i == 0){
                mDSDetail.put("Detail",exports);
            }else if (i == 1){
                mDSDetail.put("Detail",imports);
            }else if (i == 2){
                mDSDetail.put("Detail",printername);
            }else if (i == 3){
                mDSDetail.put("Detail",controller.fetchdbSettings().get(18));
            }else if (i==5){
                mDSDetail.put("Detail",routeschedule);
            }else if (i == 6){
                mDSDetail.put("Detail",transact);
            }else if (i == 7){
                mDSDetail.put("Detail",videos);
            }else if (i == 8){
                mDSDetail.put("Detail",productinfo);
            }else if (i == 9){
                mDSDetail.put("Detail",promoinfo);
            }

            hmDSDetails.add(mDSDetail);
        }

        try {
            laDSDetails = new SimpleAdapter(this, hmDSDetails, R.layout.item_databasesettings,
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

            DSDetails.setAdapter(laDSDetails);

        } catch (Exception e) {

        }
    }

    public void PrinterList() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DatabaseSettingsActivity.this);
        LayoutInflater inflater = DatabaseSettingsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_printer, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        final RadioGroup rgPrinter = (RadioGroup) dialogView.findViewById(R.id.rgPrinter);
        TextView tvDBPCancel = (TextView) dialogView.findViewById(R.id.tvDBPCancel);
        TextView tvDBPSave = (TextView) dialogView.findViewById(R.id.tvDBPSave);

        final List<String> listpaired = new ArrayList<String>();
        for(BluetoothDevice bt : pairedDevices)
            listpaired.add(bt.getName());

        for (int i = 0; i < listpaired.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(listpaired.get(i));
            if (printername.equals(listpaired.get(i))){
                radioButton.setChecked(true);
            }
            radioButton.setId(i);
            rgPrinter.addView(radioButton);
        }

        tvDBPCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvDBPSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedRadioButtonId = rgPrinter.getCheckedRadioButtonId();

                try {
                    FileOutputStream fileout=openFileOutput("printer.txt", MODE_PRIVATE);
                OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                outputWriter.write(listpaired.get(checkedRadioButtonId));
                outputWriter.close();



            } catch (Exception e) {
                //e.printStackTrace();
            }

                alertDialog.dismiss();

                hmDSDetails.clear();
                readprintername();
                ViewListview();

            }
        });

        alertDialog.show();

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

    void readrouteschedule(){

        try {
            FileInputStream fileIn=openFileInput("routeschedule.txt");
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
            routeschedule = s ;


        } catch (Exception e) {
            routeschedule = "";
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
            imports = s ;


        } catch (Exception e) {
            imports = "";
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
            exports = s ;


        } catch (Exception e) {
            exports = "";
        }


    }

    void readtransact(){

        try {
            FileInputStream fileIn=openFileInput("transact.txt");
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
            transact = s ;


        } catch (Exception e) {
            transact = "YES";
        }


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
            videos = s ;


        } catch (Exception e) {
            videos = "";
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
            productinfo = s ;


        } catch (Exception e) {
            productinfo = "";
        }


    }

    void readpromoinfo(){

        try {
            FileInputStream fileIn=openFileInput("promoinfo.txt");
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
            promoinfo = s ;


        } catch (Exception e) {
            promoinfo = "";
        }


    }

    public void RouteScheduling() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DatabaseSettingsActivity.this);
        LayoutInflater inflater = DatabaseSettingsActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_routescheduling, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        TextView tvDSCancel = (TextView ) dialogView.findViewById(R.id.tvDSCancel);
        TextView tvDSSet = (TextView ) dialogView.findViewById(R.id.tvDSSet);
        chkRSMonday = (CheckBox) dialogView.findViewById(R.id.chkRSMonday);
        chkRSTuesday = (CheckBox) dialogView.findViewById(R.id.chkRSTuesday);
        chkRSWednesday = (CheckBox) dialogView.findViewById(R.id.chkRSWednesday);
        chkRSThursday = (CheckBox) dialogView.findViewById(R.id.chkRSThursday);
        chkRSFriday = (CheckBox) dialogView.findViewById(R.id.chkRSFriday);
        chkRSSaturday= (CheckBox) dialogView.findViewById(R.id.chkRSSaturday);

        String day = "";
        ArrayList<String> days= new ArrayList<String>();
        days.clear();

        for (int i = 0;i<routeschedule.length();i++){
            if(routeschedule.charAt(i) == ','){
                 days.add(day);
                 day = "";
            }else{
                day = day + routeschedule.charAt(i);
            }
        }
        days.add(day);


        for (int a= 0;a < days.size();a++){
            if (days.get(a).equals("Mon")){
                chkRSMonday.setChecked(true);
            }else if (days.get(a).equals("Tue")){
                chkRSTuesday.setChecked(true);
            }else if (days.get(a).equals("Wed")){
                chkRSWednesday.setChecked(true);
            }else if (days.get(a).equals("Thu")){
                chkRSThursday.setChecked(true);
            }else if (days.get(a).equals("Fri")){
                chkRSFriday.setChecked(true);
            }else if (days.get(a).equals("Sat")) {
                chkRSSaturday.setChecked(true);
            }

        }

        tvDSCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                }
        });

        tvDSSet.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toasty.error(DatabaseSettingsActivity.this, RSRoute(), Toast.LENGTH_LONG).show();

                try {
                    FileOutputStream fileout=openFileOutput("routeschedule.txt", MODE_PRIVATE);
                    OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                    outputWriter.write(RSRoute());
                    outputWriter.close();
                } catch (Exception e) {

                    //e.printStackTrace();
                }
                alertDialog.dismiss();

                hmDSDetails.clear();
                readrouteschedule();
                ViewListview();

            }
        });



        alertDialog.show();

    }

    private String RSRoute() {
        List<String> visitDays = new ArrayList<>();
        if (chkRSMonday.isChecked()) {
            visitDays.add("Mon");
        }
        if (chkRSTuesday.isChecked()) {
            visitDays.add("Tue");
        }
        if (chkRSWednesday.isChecked()) {
            visitDays.add("Wed");
        }
        if (chkRSThursday.isChecked()) {
            visitDays.add("Thu");
        }
        if (chkRSFriday.isChecked()) {
            visitDays.add("Fri");
        }
        if (chkRSSaturday.isChecked()) {
            visitDays.add("Sat");
        }
        return TextUtils.join(",", visitDays);
    }



    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(DatabaseSettingsActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Database Settings")
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
        Intent IntentMainActivity = new Intent(DatabaseSettingsActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }



}

