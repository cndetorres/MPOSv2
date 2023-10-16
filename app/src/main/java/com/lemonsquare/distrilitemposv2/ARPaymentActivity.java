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

import org.apache.commons.lang3.StringUtils;

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

public class ARPaymentActivity extends Activity {

    DBController controller = new DBController(this);
    ListView ARDetails,ARHeader;
    ArrayList<HashMap<String, String>> hmARHeader;
    ListAdapter laARHeader,laARDetails;
    HashMap<String, String> mARHeader;
    List<HashMap<String, String>> ARViewAReceivable;
    TextView tvARCName,tvARTotal;
    BottomNavigationView ARmenu;
    List<String> ARPListSettings;
    ArrayList<String> alPrintARPaymentH;
    List<HashMap<String, String>> alPrintARPaymentD,alPrintChecks;

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
        setContentView(R.layout.activity_arpayment);

        ARDetails = (ListView) findViewById(R.id.lvARDetail);
        ARHeader = (ListView) findViewById(R.id.lvARHeader);
        tvARCName = (TextView) findViewById(R.id.tvARCName);
        tvARTotal = (TextView) findViewById(R.id.tvARTotal);
        ARmenu = (BottomNavigationView) findViewById(R.id.btARNavigation);

        readprintername();

        tvARCName.setText(controller.PCLName);
        DecimalFormat ARAmt = new DecimalFormat("#,###,##0.00");
        tvARTotal.setText("TOTAL: " + ARAmt.format(controller.fetchSUMARBalances()));

        //controller.PCCode = controller.fetchCCodeCustomers();

        ARViewAReceivable = controller.fetchAccountsReceivable();

        ARPListSettings = controller.fetchdbSettings();

        ViewHeaderListview();
        ViewDetailListview();

        if (controller.PIndicator == 1 ){
            printdialog();
        }

        ARmenu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.marp_next:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                if ( Double.parseDouble(StringUtils.substringAfter(tvARTotal.getText().toString(), ":").replace(",",""))<= 0.00){
                                    messagebox(controller.PCLName + " has no pending AR");
                                }else{
                                    computeAmountDue();
                                    computeAmountPaid();
                                    fetchBillDt();

                                    controller.PPayment = 0;
                                    controller.PIsSOrder = 0;

                                    controller.dbCGiven = 0.00;
                                    controller.dbBalance = 0.00;

                                    Intent IntentPaymentDetailsActivity = new Intent(ARPaymentActivity.this, PaymentDetailsActivity.class);
                                    startActivity(IntentPaymentDetailsActivity);
                                    finish();
                                }

                                break;

                        }
                        return true;
                    }
                });

    }

    public void ViewHeaderListview() {

        hmARHeader = new ArrayList<HashMap<String, String>>();
        mARHeader = new HashMap<String, String>();

        mARHeader.put("SID", "SALES ID");
        mARHeader.put("BillDt", "BILL DT");
        mARHeader.put("Amt", "AMOUNT");
        mARHeader.put("Payment", "PAYMENT");
        mARHeader.put("Balance", "BALANCE");
        hmARHeader.add(mARHeader);

        try {
            laARHeader = new SimpleAdapter(this, hmARHeader, R.layout.item_accountsreceivable,
                    new String[]{"SID", "BillDt", "Amt", "Payment", "Balance"}, new int[]{
                    R.id.rowsSalesID, R.id.rowsBillDt, R.id.rowsAmt, R.id.rowsPayment,R.id.rowsBalance}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rsid = (TextView) view.findViewById(R.id.rowsSalesID);
                    TextView rbilldt = (TextView) view.findViewById(R.id.rowsBillDt);
                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);
                    TextView rpment = (TextView) view.findViewById(R.id.rowsPayment);
                    TextView rbal = (TextView) view.findViewById(R.id.rowsBalance);
                    if (position % 2 == 0) {
                        rsid.setTextColor(Color.WHITE);
                        rbilldt.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rpment.setTextColor(Color.WHITE);
                        rbal.setTextColor(Color.WHITE);
                        rsid.setTypeface(null, Typeface.BOLD);
                        rbilldt.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        rpment.setTypeface(null, Typeface.BOLD);
                        rbal.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            ARHeader.setAdapter(laARHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laARDetails = new SimpleAdapter(this, ARViewAReceivable, R.layout.item_accountsreceivable,
                    new String[]{"SID", "BillDt", "Amt", "Payment", "Balance"}, new int[]{
                    R.id.rowsSalesID, R.id.rowsBillDt, R.id.rowsAmt, R.id.rowsPayment,R.id.rowsBalance}) {
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
            ARDetails.setAdapter(laARDetails);
        } catch (Exception e) {

        }
    }

    public void computeAmountDue(){

        controller.dbAmtDue = 0.00;

        for (int i = 0; i < ARDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laARDetails.getItem(i);
            String objAmt = (String) obj.get("Balance");
            if (Double.parseDouble(objAmt.replace(",","")) > 0){
                controller.dbAmtDue = Double.parseDouble(objAmt.replace(",","")) + controller.dbAmtDue;
            }

        }
    }

    public void computeAmountPaid(){

        controller.dbAmtPd = 0.00;

        for (int i = 0; i < ARDetails.getCount(); i++) {
            HashMap<String, Object> obj = (HashMap<String, Object>) laARDetails.getItem(i);
            String objAmt = (String) obj.get("Balance");
            if (Double.parseDouble(objAmt.replace(",","")) < 0){
                controller.dbAmtPd = Double.parseDouble(objAmt.replace(",","")) + controller.dbAmtPd;
            }

        }

        if (controller.dbAmtPd != 0.00){
            controller.dbAmtPd = (controller.dbAmtPd) * -1;
        }
    }

    public void fetchBillDt(){


            HashMap<String, Object> obj = (HashMap<String, Object>) laARDetails.getItem(0);
            String objBillDt = (String) obj.get("BillDt");
            controller.PBillDt = objBillDt;


    }

    void  messagebox(String alerttext) {

        new android.support.v7.app.AlertDialog.Builder(ARPaymentActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("AR Payment")
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

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.print_logo);

            printImage(bmp);

            String companyName = controller.fetchdbSettings().get(19);
            String title = "PROVISIONAL RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;
            String address,contactNo;
            address = getSpace(printHalfLen - (ARPListSettings.get(13).length()/2)) + ARPListSettings.get(13);
            contactNo = getSpace(printHalfLen - (ARPListSettings.get(10).length()/2)) + ARPListSettings.get(10);

            printCustom(companyName,3);
            printCustom( address,0);
            printCustom( contactNo,0);
            printNewLine();
            printCustom(title,3);
            printNewLine();

            alPrintARPaymentH = controller.printARPaymentH(controller.PDCode);

            printText(WithDate("Customer Code: " + alPrintARPaymentH.get(0),alPrintARPaymentH.get(1)));
            printNewLine();
            printCustom("Customer: " + alPrintARPaymentH.get(2),0);
            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1),0);
            printCustom("Terminal: " + ARPListSettings.get(1),0);
            printCustom("Payment ID: " + controller.PDCode,0);
            printNewLine();

            printText(PrintAmtDetails("Amount Due:", alPrintARPaymentH.get(5)));
            printNewLine();
            printText(PrintAmtDetails("Less Returns:", (alPrintARPaymentH.get(4))));
            printNewLine();
            printText(PrintAmtDetails("Total Amount Due:", alPrintARPaymentH.get(3)));
            printNewLine();

            printNewLine();
            printText(PrintAmtDetails("Cash Received:", alPrintARPaymentH.get(6)));
            printNewLine();
            printText(PrintAmtDetails("Check Received:", alPrintARPaymentH.get(7)));
            printNewLine();
            if (!alPrintARPaymentH.get(7).equals("0.00")){
                alPrintChecks = controller.fetchCheck(controller.PDCode);
                for(int i = 0; i < alPrintChecks.size() ; i++) {

                    String bank;
                    bank = alPrintChecks.get(i).get("Bank");
                    if (bank.length() > 19){
                        bank = bank.substring(0,19);
                    }

                    printText(PrintCheckDetails(bank,alPrintChecks.get(i).get("CheckDt"),alPrintChecks.get(i).get("CheckNo"),alPrintChecks.get(i).get("Amt")));
                    printNewLine();
                }
            }
            printText(PrintAmtDetails("Change:", alPrintARPaymentH.get(8)));
            printNewLine();
            printNewLine();

            printText(PrintAmtDetails("Total Payments Applied:", alPrintARPaymentH.get(9)));
            printNewLine();
            printNewLine();
            printCustom("Updated ARBalance",0);

            printText(getLine(57));
            printNewLine();

            printText(PrintBalanceDetails("BillDate","SalesId","Amount","Payment","Balance"));
            printNewLine();

            ARViewAReceivable = controller.fetchAccountsReceivable();

            for(int i = 0; i < ARViewAReceivable.size() ; i++) {

                String SID = ARViewAReceivable.get(i).get("SID");


                printText(PrintBalanceDetails(ARViewAReceivable.get(i).get("BillDt"),SID.substring(6),ARViewAReceivable.get(i).get("Amt"),ARViewAReceivable.get(i).get("Payment"),ARViewAReceivable.get(i).get("Balance")));
                printNewLine();

            }
            printText(getLine(57));
            printNewLine();

            String website = controller.fetchdbSettings().get(20);
            if (website.equals("")){
                website = "";
            }else{
                website = getSpace(printHalfLen - (website.length()/2)) + website;
            }
            String footerTitle = "CUSTOMER SERVICE";
            String footerContactNo = ARPListSettings.get(12);
            String forOrders = "For Orders please contact";
            String orderContactNo = ARPListSettings.get(11);
            String thankYou = "Thank you for your patronage!";

            footerTitle = getSpace(printHalfLen - (footerTitle.length()/2)) + footerTitle;
            footerContactNo = getSpace(printHalfLen - (footerContactNo.length()/2)) + footerContactNo;
            forOrders = getSpace(printHalfLen - (forOrders.length()/2)) + forOrders;
            orderContactNo = getSpace(printHalfLen - (orderContactNo.length()/2)) + orderContactNo;
            thankYou = getSpace(printHalfLen - (thankYou.length()/2)) + thankYou;




            printCustom(website,0);
            printCustom(footerTitle,0);
            printCustom( footerContactNo,0);
            printCustom(forOrders,0);
            printCustom( orderContactNo,0);
            printNewLine();
            printCustom(thankYou,0);

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


    private String PrintAmtDetails(String str1,String str2) {
        int fulllength = 40;
        int strlength = 15;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;


        String ans = str1 + getSpace(anslength) + " " + getSpace(anslength2) + str2  ;
        return ans;
    }

    private String PrintCheckDetails(String str1,String str2,String str3,String str4) {
        int strlength1 = 20;
        int strlength2 = 10;
        int strlength3 = 14;

        int str1length = str1.length();
        int anslength1  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength2 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength3 - str4length;


        String ans = str1 + getSpace(anslength1)  + str2 + getSpace(anslength2)  + str3 + getSpace(anslength3) + "  " + getSpace(anslength4)  + str4;
        return ans;
    }

    private String PrintBalanceDetails(String str1,String str2,String str3,String str4,String str5) {
        int strlength1 = 8;
        int strlength2 = 16;
        int strlength3 = 10;

        int str1length = str1.length();
        int anslength1  = strlength1 - str1length;

        int str2length = str2.length();
        int anslength2 = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength3 - str3length;

        int str4length = str4.length();
        int anslength4 = strlength3 - str4length;

        int str5length = str5.length();
        int anslength5 = strlength3 - str5length;


        String ans = str1 + getSpace(anslength1) + " " + str2  + getSpace(anslength2)  + getSpace(anslength3) + str3 + " "   + getSpace(anslength4) + str4 + " "   + getSpace(anslength5) + str5;
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

        new AlertDialog.Builder(ARPaymentActivity.this)
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

        new AlertDialog.Builder(ARPaymentActivity.this)
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ARPaymentActivity.this);
        LayoutInflater inflater = ARPaymentActivity.this.getLayoutInflater();
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
    void returnactivity(){

        controller.PCNm = 1;
        finish();

    }

    public void onBackPressed() {

        returnactivity();

    }

}


