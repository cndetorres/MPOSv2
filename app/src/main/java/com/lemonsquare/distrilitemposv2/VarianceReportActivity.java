package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class VarianceReportActivity extends Activity {

    DBController controller = new DBController(this);
    ListView RVHeader,RVDetails,IVHeader,IVDetails;
    ArrayList<HashMap<String, String>> hmRVHeader,hmIVHeader;
    ListAdapter laRVHeader,laRVDetails,laIVHeader,laIVDetails;
    HashMap<String, String> mRVHeader,mIVHeader;
    public static List<HashMap<String, String>> VRViewReturnVariance,VRViewInventoryVariance;
    BottomNavigationView VRmenu;
    List<HashMap<String, String>> alPrintReturnsH,alPrintGoodStocksH;

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

    static int HEAD_WIDTH = 576;
    static int COLOR_THRESHOLD = 128;

    private final byte[] SELECT_BIT_IMAGE_MODE = new byte[]{(byte) 0x1B, (byte) 0x56};
    private final byte[] SET_LINE_SPACING = new byte[]{0x1B, 0x33};

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variancereport);

        RVHeader = (ListView) findViewById(R.id.lvVRRVHeader);
        RVDetails = (ListView) findViewById(R.id.lvVRRVDetail);
        IVHeader = (ListView) findViewById(R.id.lvVRIVHeader);
        IVDetails = (ListView) findViewById(R.id.lvVRIVDetail);
        VRmenu = (BottomNavigationView) findViewById(R.id.btVRNavigation);

        readprintername();

        VRViewReturnVariance = controller.fetchReturnVariance();
        VRViewInventoryVariance = controller.fetchInventoryVariance();


        ViewRVHeaderListview();
        ViewRVDetailListview();
        ViewIVHeaderListview();
        ViewIVDetailListview();

        VRmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mvr_exit:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                new AlertDialog.Builder(VarianceReportActivity.this)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle("Exit")
                                        .setMessage("Are you sure you want to exit?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                controller.updateUsers(0);

                                                //controller.updateSettings(3);

                                                //controller.export();

                                                Intent intentLogInActivity = new Intent(VarianceReportActivity.this, LogInActivity.class);
                                                startActivity(intentLogInActivity);
                                                finish();
                                            }

                                        })
                                        .setNegativeButton("No", null)
                                        .show();

                                break;

                            case R.id.mvr_print:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                printdialog();

                                break;

                        }


                        return true;
                    }
    });

    }

    public void ViewRVHeaderListview() {

        hmRVHeader = new ArrayList<HashMap<String, String>>();
        mRVHeader = new HashMap<String, String>();


        mRVHeader.put("Item", "ITEM");
        mRVHeader.put("Qty", "VARIANCE");
        mRVHeader.put("Unit", "UNIT");
        hmRVHeader.add(mRVHeader);

        try {
            laRVHeader = new SimpleAdapter(this, hmRVHeader,  R.layout.item_returnvariance,
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

            RVHeader.setAdapter(laRVHeader);
        } catch (Exception e) {

        }
    }


    public void ViewRVDetailListview() {


        try {
            laRVDetails = new SimpleAdapter(this, VRViewReturnVariance, R.layout.item_returnvariance,
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
            RVDetails.setAdapter(laRVDetails);
        } catch (Exception e) {

        }

    }

    public void ViewIVHeaderListview() {

        hmIVHeader = new ArrayList<HashMap<String, String>>();
        mIVHeader = new HashMap<String, String>();


        mIVHeader.put("Item", "ITEM");
        mIVHeader.put("Qty", "VARIANCE");
        mIVHeader.put("Unit", "UNIT");
        hmIVHeader.add(mIVHeader);

        try {
            laIVHeader = new SimpleAdapter(this, hmIVHeader,  R.layout.item_inventoryvariance,
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

            IVHeader.setAdapter(laIVHeader);
        } catch (Exception e) {

        }
    }


    public void ViewIVDetailListview() {


        try {
            laIVDetails = new SimpleAdapter(this, VRViewInventoryVariance, R.layout.item_inventoryvariance,
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
            IVDetails.setAdapter(laIVDetails);
        } catch (Exception e) {

        }

    }

    void  messagebox(String alerttext) {

        new AlertDialog.Builder(VarianceReportActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Variance Report")
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
            String title = "VARIANCE REPORT RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;
            String title2 = "RETURNS";
            title2 = getSpace(printHalfLen - (title2.length()/2)) + title2;
            String title3 = "GOOD STOCK";
            title3 = getSpace(printHalfLen - (title3.length()/2)) + title3;

            printCustom(companyName,3);
            printCustom("Salesman: " + controller.printVReportH().get(0),0);
            printCustom("Checking Date: " + controller.printVReportH().get(1) ,0);
            printCustom("Checker: " + controller.fetchRIDNmUsers().get(1),0);
            printCustom("Reference No.: " + controller.printVReportH().get(2),0);
            printCustom("Final Odometer Reading: " + controller.fetchdbSettings().get(17),0);
            printNewLine();
            printCustom(title,3);
            printNewLine();
            printCustom(title2,0);

            printText(getLine(57));
            printNewLine();

            printText(PrintReturnVarianceDetails("CUSTCODE","NAME","UNIT","TYPE","SYS","VAL","VAR"));
            printNewLine();

            alPrintReturnsH = controller.fetchReturnVariance();

            for(int i = 0; i < alPrintReturnsH.size(); i++) {

                String item;
                item = alPrintReturnsH.get(i).get("Item");
                if (item.length() > 25){
                    item = item.substring(0,25);
                }

                printText(PrintReturnVarianceDetails(alPrintReturnsH.get(i).get("CustCode"),item,alPrintReturnsH.get(i).get("Unit"),alPrintReturnsH.get(i).get("RetType"),alPrintReturnsH.get(i).get("Sys"),alPrintReturnsH.get(i).get("Val"),alPrintReturnsH.get(i).get("Qty")));
                printNewLine();
                printCustom("Remarks: " + alPrintReturnsH.get(i).get("Remarks"),0);
            }
            printText(getLine(57));
            printNewLine();
            printNewLine();

            printCustom(title3,3);
            printNewLine();

            alPrintGoodStocksH = controller.fetchInventoryVariance();

            printText(getLine(57));
            printNewLine();

            printText(PrintGoodStockDetails("NAME","SYS","VAL","VAR"));
            printNewLine();

            int end = 0;

            for(int i = 0; i < alPrintGoodStocksH.size(); i++) {
                printText(PrintGoodStockDetails(alPrintGoodStocksH.get(i).get("Item"),alPrintGoodStocksH.get(i).get("Sys"),alPrintGoodStocksH.get(i).get("Val"),alPrintGoodStocksH.get(i).get("Qty")));
                end = Integer.valueOf(alPrintGoodStocksH.get(i).get("Val")) + end;
                printNewLine();
            }
            printText(getLine(57));
            printNewLine();
            printNewLine();

            double ave;
            ave = (Double.valueOf(end)/Double.valueOf(controller.fetchBegInventory()));

            DecimalFormat VRPcnt = new DecimalFormat("##0.00%");

            printCustom("                     RETURN PERCENTAGE",3);
            printNewLine();
            printText(PrintReturnPercentage("Salesman: " + controller.printVReportH().get(0),"BEG","END","AVE"));
            printNewLine();
            printText(PrintReturnPercentage("Checking Date: " + controller.printVReportH().get(1),String.valueOf(controller.fetchBegInventory()),String.valueOf(end),String.valueOf(VRPcnt.format(ave))));
            printNewLine();
            printCustom("Sales District: " + controller.fetchdbSettings().get(6),0);

            printText(getLine(57));

            printNewLine();
            printCustom("SIGNATURE",0);
            printNewLine();

            Bitmap bmp = BitmapFactory.decodeByteArray(controller.bArray, 0, controller.bArray.length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmp, 250, 90, false);

            printImage(resizedbmp);



            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printImage(Bitmap image) throws IOException {

        Bitmap bmp = image;
        if (image.getWidth() > HEAD_WIDTH) {
            bmp = scaleToWidth(image, HEAD_WIDTH);
        }
        BitSet bitsImageData = toPrinterGraphic(bmp);

        byte[] setLineSpacing24Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 24);
        byte[] setLineSpacing30Dots = buildPOSCommand(SET_LINE_SPACING, (byte) 30);

        mmOutputStream.write(setLineSpacing24Dots);

        byte heightLSB = (byte) (bmp.getHeight() & 0xFF);
        byte heightMSB = (byte) ((bmp.getHeight() & 0xFF00) >> 8);

        mmOutputStream.write(buildPOSCommand(SELECT_BIT_IMAGE_MODE, heightMSB, heightLSB));
        int offset = 0;
        while (offset < bmp.getHeight()) {
            for (int x = 0; x < HEAD_WIDTH; ) {
                byte slice = 0;
                for (int b = 0; b < 8; ++b) {
                    int i = (offset * HEAD_WIDTH) + x + b;
                    boolean v = false;
                    if (i < bitsImageData.length()) {
                        v = bitsImageData.get(i);
                    }
                    slice |= (byte) ((v ? 1 : 0) << (7 - b));
                }

                mmOutputStream.write(slice);
                x += 8;
            }
            offset++;
        }
        mmOutputStream.write(setLineSpacing30Dots);
        printNewLine();
    }

    private static BitSet toPrinterGraphic(Bitmap source) {
        BitSet printerGraphic = new BitSet(source.getWidth() * HEAD_WIDTH);
        try {
            int k = 0;
            for (int x = 0; x < source.getHeight(); x++) {
                for (int y = 0; y < HEAD_WIDTH; y++) {
                    if (y < source.getWidth()) {
                        int pixel = source.getPixel(y, x);
                        int red = Color.red(pixel);
                        int green = Color.green(pixel);
                        int blue = Color.blue(pixel);
                        int alpha = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                        printerGraphic.set(k, alpha < COLOR_THRESHOLD);
                    } else {
                        printerGraphic.set(k, false);
                    }
                    k++;
                }
            }
        } catch (Exception e) {
            //Log.e(TAG, e.toString());
        }

        return printerGraphic;
    }

    private static Bitmap scaleToWidth(Bitmap src, int width) {
        float aspectRatio = src.getWidth() / (float) src.getHeight();
        int height = Math.round(width / aspectRatio);
        return Bitmap.createScaledBitmap(src, width, height, false);
    }

    private static byte[] buildPOSCommand(byte[] command, byte... args) {
        byte[] posCommand = new byte[command.length + args.length];
        System.arraycopy(command, 0, posCommand, 0, command.length);
        System.arraycopy(args, 0, posCommand, command.length, args.length);
        return posCommand;
    }



    private String PrintReturnVarianceDetails(String str1, String str2,String str3,String str4,String str5,String str6,String str7) {
        int strlength1 = 9;
        int strlength2 = 26;
        int strlength3 = 4;

        int str1length = str1.length();
        int anslength  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength3 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength3 - str4length;

        int str5length = str5.length();
        int anslength5 = strlength3 - str5length;

        int str6length = str6.length();
        int anslength6 = strlength3 - str6length;

        int str7length = str7.length();
        int anslength7 = strlength3 - str7length;

        String ans =  str1 + getSpace(anslength) + str2  + getSpace(anslength2) +  " " +   str3 + getSpace(anslength3)   + " " + str4 +  getSpace(anslength4) + getSpace(anslength5) + str5 + getSpace(anslength6) + str6 + getSpace(anslength7) + str7;
        return ans;
    }

    private String PrintGoodStockDetails(String str1, String str2,String str3,String str4) {
        int strlength1 = 45;
        int strlength2 = 4;

        int str1length = str1.length();
        int anslength  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength2 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength2 - str4length;


        String ans =  str1 + getSpace(anslength)   + getSpace(anslength2) + str2 +   getSpace(anslength3) +   str3    + getSpace(anslength4) +  str4;
        return ans;
    }

    private String PrintReturnPercentage(String str1, String str2,String str3,String str4) {
        int strlength1 = 36;
        int strlength2 = 6;
        int strlength3 = 9;

        int str1length = str1.length();
        int anslength  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength2 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength3 - str4length;


        String ans =  str1 + getSpace(anslength)   + getSpace(anslength2) + str2 +   getSpace(anslength3) +   str3    + getSpace(anslength4) +  str4;
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
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy hh:mm a");
        String formattedDate = df.format(c.getTime());

        return formattedDate;
    }

    void printdialog(){

        new AlertDialog.Builder(VarianceReportActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Print Receipt")
                .setCancelable(false)
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

                        //returnactivity();
                        dialog.dismiss();
                        closeBT();

                    }
                })
                .show();

    }

    void reprintdialog(){

        new AlertDialog.Builder(VarianceReportActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Print Receipt")
                .setCancelable(false)
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

                        //returnactivity();
                        dialog.dismiss();
                        closeBT();

                    }
                })
                .show();

    }



    void returnactivity(){

        controller.updateUsers(0);

        controller.updateSettings(3);

        Intent intentLogInActivity = new Intent(VarianceReportActivity.this, LogInActivity.class);
        startActivity(intentLogInActivity);
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

    public void ProgressDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(VarianceReportActivity.this);
        LayoutInflater inflater = VarianceReportActivity.this.getLayoutInflater();
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

    public void onBackPressed() {

        new AlertDialog.Builder(VarianceReportActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        controller.updateUsers(0);

                        controller.updateSettings(3);

                        Intent intentLogInActivity = new Intent(VarianceReportActivity.this, LogInActivity.class);
                        startActivity(intentLogInActivity);
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();

    }
}
