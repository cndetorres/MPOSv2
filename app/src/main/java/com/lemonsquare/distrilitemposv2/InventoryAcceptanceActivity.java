package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class InventoryAcceptanceActivity extends Activity {

    DBController controller = new DBController(this);
    ListView IADetails,IAHeader;
    ArrayList<HashMap<String, String>> hmIAHeader;
    ListAdapter laIAHeader,laIADetails;
    HashMap<String, String> mIAHeader;
    BottomNavigationView IAmenu;
    List<HashMap<String, String>> IAViewIAcceptance;
    EditText etDICPassword,etDICOdeometer;
    TextView tvDICCancel,tvDICAccept;
    ArrayList<String> alPrintInventoryAcceptanceH;
    List<HashMap<String, String>> alInventoryAcceptance;

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

    boolean locked = true;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventoryacceptance);


        IADetails = (ListView) findViewById(R.id.lvIADetails);
        IAHeader = (ListView) findViewById(R.id.lvIAHeader);
        IAmenu = (BottomNavigationView) findViewById(R.id.btIANavigation);

        readprintername();

        IAViewIAcceptance = controller.fetchInventoryAcceptance();

        /*controller.PCName = "Id";
        controller.PMNumber = controller.fetchMaxNumTSequence();*/

        controller.PCName = "Id";
        controller.PTName = "OdometerReading";
        controller.PMNumber = controller.fetchMaxNumTCTSequence();

        ViewHeaderListview();
        ViewDetailListview();

        IAmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.ma_exit:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                new AlertDialog.Builder(InventoryAcceptanceActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Reject")
                                        .setMessage("Do you want to reject inventory?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                controller.updateSettings("Status","5");
                                                controller.updateUsers(0);

                                                Intent intentScreenActivity = new Intent(InventoryAcceptanceActivity.this, ScreenActivity.class);
                                                startActivity(intentScreenActivity);
                                                finish();

                                                Toasty.error(getApplicationContext(), "Please return device to IT Admin", Toast.LENGTH_LONG).show();

                                            }

                                        })
                                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                new AlertDialog.Builder(InventoryAcceptanceActivity.this)
                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                        .setTitle("Exit")
                                                        .setMessage("Do you want to exit?")
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                                controller.updateUsers(0);

                                                                Intent intentScreenActivity = new Intent(InventoryAcceptanceActivity.this, ScreenActivity.class);
                                                                startActivity(intentScreenActivity);
                                                                finish();
                                                            }

                                                        })
                                                        .setNegativeButton("No", null)
                                                        .show();

                                            }

                                        })
                                        .show();

                                break;

                            case R.id.ma_accept:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                    DialogInventoryAcceptance();

                                break;
                        }
                        return true;
                    }
                });
    }

    public void ViewHeaderListview() {

        hmIAHeader = new ArrayList<HashMap<String, String>>();
        mIAHeader = new HashMap<String, String>();

        mIAHeader.put("Item", "ITEM");
        mIAHeader.put("Unit", "UNIT");
        mIAHeader.put("Prev", " PREV");
        mIAHeader.put("New", " NEW");
        mIAHeader.put("Total", " TOTAL");
        hmIAHeader.add(mIAHeader);

        try {
            laIAHeader = new SimpleAdapter(this, hmIAHeader, R.layout.item_inventoryacceptance,
                    new String[]{"Item", "Unit", "Prev", "New", "Total"}, new int[]{
                    R.id.rowsItem, R.id.rowsUnit, R.id.rowsPrev, R.id.rowsNew, R.id.rowsTotal}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView ritem = (TextView) view.findViewById(R.id.rowsItem);
                    TextView runit = (TextView) view.findViewById(R.id.rowsUnit);
                    TextView rprev = (TextView) view.findViewById(R.id.rowsPrev);
                    TextView rnew = (TextView) view.findViewById(R.id.rowsNew);
                    TextView rtotal = (TextView) view.findViewById(R.id.rowsTotal);
                    if (position % 2 == 0) {
                        ritem.setTextColor(Color.WHITE);
                        runit.setTextColor(Color.WHITE);
                        rprev.setTextColor(Color.WHITE);
                        rnew.setTextColor(Color.WHITE);
                        rtotal.setTextColor(Color.WHITE);
                        ritem.setTypeface(null, Typeface.BOLD);
                        runit.setTypeface(null, Typeface.BOLD);
                        rprev.setTypeface(null, Typeface.BOLD);
                        rnew.setTypeface(null, Typeface.BOLD);
                        rtotal.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            IAHeader.setAdapter(laIAHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laIADetails = new SimpleAdapter(this, IAViewIAcceptance, R.layout.item_inventoryacceptance,
                    new String[]{"Item", "Unit", "Prev", "New", "Total"}, new int[]{
                    R.id.rowsItem, R.id.rowsUnit, R.id.rowsPrev, R.id.rowsNew, R.id.rowsTotal}) {
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
            IADetails.setAdapter(laIADetails);
        } catch (Exception e) {

        }
    }

    public void DialogInventoryAcceptance() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InventoryAcceptanceActivity.this);
        LayoutInflater inflater = InventoryAcceptanceActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_inventoryacceptance, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        etDICPassword = (EditText) dialogView.findViewById(R.id.etDICPassword);
        etDICOdeometer = (EditText) dialogView.findViewById(R.id.etDICOdeometer);
        tvDICCancel = (TextView) dialogView.findViewById(R.id.tvDICCancel);
        tvDICAccept = (TextView) dialogView.findViewById(R.id.tvDICAccept);

        tvDICCancel.setOnClickListener(new OnSingleClickListener() {
                                        public void onSingleClick(View v) {
                                            locked = true;
                                            alertDialog.dismiss();
                                        }
        });

        tvDICAccept.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                if (etDICPassword.getText().toString().equals("")){
                    etDICPassword.setError("please input password");
                }else if (etDICOdeometer.getText().toString().equals("")){
                    etDICOdeometer.setError("please input odometer");
                }else if (etDICOdeometer.getText().toString().equals("0")){
                    etDICOdeometer.setError("invalid odometer number");
                }else{
                    if (!controller.fetchRIDNmUsers().get(4).equals(etDICPassword.getText().toString())){
                        etDICPassword.setError("invalid password");
                    }else{

                        Timestamp tmIADtTime = new Timestamp(System.currentTimeMillis());

                        controller.insertOdometerReading(controller.fetchRIDNmUsers().get(3),etDICOdeometer.getText().toString(),tmIADtTime.getTime(),"Initial Odometer Reading",controller.fetchRIDNmUsers().get(3),controller.PVVehicleNo,controller.PMNumber);
                        DateFormat defaultDateFormat = new SimpleDateFormat("yyMMdd");
                        Calendar defaultDate = Calendar.getInstance();
                        String todayDate = defaultDateFormat.format(defaultDate.getTime());
                        controller.insertLocationLog(controller.fetchRIDNmUsers().get(3),controller.PMNumber,0.00,0.00,todayDate);

                        controller.updateTCTableSequence(Integer.valueOf(controller.PMNumber) + 1);
                        controller.updateSettings("LastOdometer",etDICOdeometer.getText().toString());

                        /*alInventoryAcceptance = controller.fetchInventoryAcceptanceToday();
                        String salesID = "";
                        String salesItem = "";
                        String message = "";

                        DateFormat idefaultDateFormat = new SimpleDateFormat("yyMMdd");
                        Calendar idefaultDate = Calendar.getInstance();
                        String itodayDate = idefaultDateFormat.format(idefaultDate.getTime());
                        salesID = itodayDate + controller.fetchdbSettings().get(6);

                        if (alInventoryAcceptance.size()>0){
                            int count = 1;
                            salesItem = "";
                            for(int i = 0; i < alInventoryAcceptance.size();i++){
                                salesItem = salesItem + alInventoryAcceptance.get(i).get("ExtMatGrp") + ":" + alInventoryAcceptance.get(i).get("Qty") + "/";
                                if (count%9 == 0){
                                    message = "SLSITM " +"1" + "," + salesID + "," + controller.fetchdbSettings().get(6) + "," + salesItem;
                                    Utils.sendSMS(InventoryAcceptanceActivity.this,message);
                                    salesItem = "";
                                }else if (count == alInventoryAcceptance.size()){
                                    message = "SLSITM " +"1" + "," + salesID + "," + controller.fetchdbSettings().get(6) + "," + salesItem;
                                    Utils.sendSMS(InventoryAcceptanceActivity.this,message);
                                }
                                count ++;

                            }
                        }
*/
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etDICOdeometer.getWindowToken(), 0);

                        alertDialog.dismiss();

                        printdialog();
                    }
                }
            }
        });

        alertDialog.show();



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
            String title = "INVENTORY ACCEPTANCE RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;

            printCustom(companyName,3);
            printCustom(title,3);
            printNewLine();

            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1), 1);
            //controller.fetchUiDVNoOReading().get(1)
            printCustom("Vehicle: " + controller.fetchUiDVNoOReading().get(1),1);
            printCustom("Sales District: " + controller.fetchdbSettings().get(6),1);
            //controller.fetchUiDVNoOReading().get(2)
            printCustom("Initial Odometer Reading: " + controller.fetchUiDVNoOReading().get(2),1);
            printCustom("Terminal: " + controller.fetchdbSettings().get(1),1);
            printText(WithDate("Initial Inventory" ,getDateTime()));
            printNewLine();

            printText(getLine(55));

            printNewLine();

            printText(PrintDetails("NAME","PREV","NEW","TOTAL"));
            printNewLine();

            int countqty = 0;
            for(int i = 1; i <= controller.fetchCountInventoryItem(); i++) {


                alPrintInventoryAcceptanceH = controller.printInventoryAcceptanceH(i);


                printText(PrintDetails(alPrintInventoryAcceptanceH.get(0),alPrintInventoryAcceptanceH.get(2), alPrintInventoryAcceptanceH.get(3), alPrintInventoryAcceptanceH.get(4)));


                //printText(PrintDetails(controller.alSTItems.get(i), controller.alSTUnit.get(i), controller.alSTQty.get(i)));
                printNewLine();
                countqty = countqty + Integer.valueOf(alPrintInventoryAcceptanceH.get(4));


                //Toasty.success(getApplicationContext(), String.valueOf(alPrintStockTransferD.size()), Toast.LENGTH_LONG).show();

            }

            printText(getLine(55));
            printNewLine();

            printText(PrintDetails("TOTAL QUANTITY","","",String.valueOf(countqty)));

            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String WithDate(String str1, String str2){
        int strlength = 28;
        int strlength1 = 29;

        int str1length = str1.length();
        int anslength  = strlength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength1 - str2length;

        String ans = str1 + getSpace(anslength) + getSpace(anslength2) + str2;

        return ans;
    }

    private String PrintDetails(String str1, String str2,String str3,String str4) {
        int fulllength = 39;
        int strlength1 = 5;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength1 - str2length;
        int str3length = str3.length();
        int anslength3 = strlength1 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength1 - str4length;

        String ans =  str1 + getSpace(anslength) + getSpace(anslength2) + str2  + " " + getSpace(anslength3)  +   str3 + " " +  getSpace(anslength4) + str4;
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

    void printdialog(){

        new AlertDialog.Builder(InventoryAcceptanceActivity.this)
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

        new AlertDialog.Builder(InventoryAcceptanceActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setTitle("Print Receipt")
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(InventoryAcceptanceActivity.this);
        LayoutInflater inflater = InventoryAcceptanceActivity.this.getLayoutInflater();
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

        Intent intentMainActivity = new Intent(InventoryAcceptanceActivity.this, MainActivity.class);
        startActivity(intentMainActivity);
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

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(InventoryAcceptanceActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Inventory Acceptance")
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

