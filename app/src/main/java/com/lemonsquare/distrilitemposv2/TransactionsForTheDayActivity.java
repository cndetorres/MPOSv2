package com.lemonsquare.distrilitemposv2;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class TransactionsForTheDayActivity extends Activity {

    DBController controller = new DBController(this);
    ListView TDDetails,TDHeader;
    ArrayList<HashMap<String, String>> hmTDHeader;
    ListAdapter laTDHeader,laTDDetails;
    HashMap<String, String> mTDHeader;
    List<HashMap<String, String>> TDViewTDay,TDDViewTDay,alPrintSOrderD,ARViewAReceivable,alPrintChecks,alPrintSOrderDPromo;
    TextView tvTitle,tvDTDTTyp;
    List<String> TDListSettings;
    DecimalFormat TDAmt = new DecimalFormat("#,##0.00");

    String SRSRid;
    List<HashMap<String, String>> stocktransferitem;

    ArrayList<String> alPrintStockTransferH,alPrintStockTransferD,alPrintStockReceivingH,alPrintStockReceivingD,alPrintReturnsH,alPrintReturnsD,alPrintSOrderH,alPrintARPaymentH;



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

    String transferQR;
    String transferItems;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactionsfortheday);


        TDDetails = (ListView) findViewById(R.id.lvTDDetail);
        TDHeader = (ListView) findViewById(R.id.lvTDHeader);

        TDViewTDay = controller.fetchTransactionsfortheDay();

        TDListSettings = controller.fetchdbSettings();;

        ViewHeaderListview();
        ViewDetailListview();

        readprintername();

        TDDetails.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                TDDViewTDay = controller.fetchTransactionsfortheDay(TDViewTDay.get(position).get("ID"));
                DialogTransactionsForTheDay();

                //HashMap<String, Object> obj = (HashMap<String, Object>) laTDDetails.getItem(position);

                //Toasty.error(TransactionsForTheDayActivity.this, TDViewTDay.get(position).get("ID"), Toast.LENGTH_LONG).show();


            }
        });

    }

    public void ViewHeaderListview() {

        hmTDHeader = new ArrayList<HashMap<String, String>>();
        mTDHeader = new HashMap<String, String>();

        mTDHeader.put("Type", "TYPE");
        mTDHeader.put("Customer", "CUSTOMER");
        mTDHeader.put("Time", "TIME");
        mTDHeader.put("Amt", "AMOUNT");
        hmTDHeader.add(mTDHeader);

        try {
            laTDHeader = new SimpleAdapter(this, hmTDHeader, R.layout.item_transactionsfortheday,
                    new String[]{"Type", "Customer", "Time", "Amt"}, new int[]{
                    R.id.rowsType, R.id.rowsCustomer, R.id.rowsTime, R.id.rowsAmt}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rtype = (TextView) view.findViewById(R.id.rowsType);
                    TextView rcustomer = (TextView) view.findViewById(R.id.rowsCustomer);
                    TextView rtime = (TextView) view.findViewById(R.id.rowsTime);
                    TextView ramt = (TextView) view.findViewById(R.id.rowsAmt);
                    if (position % 2 == 0) {
                        rtype.setTextColor(Color.WHITE);
                        rcustomer.setTextColor(Color.WHITE);
                        rtime.setTextColor(Color.WHITE);
                        ramt.setTextColor(Color.WHITE);
                        rtype.setTypeface(null, Typeface.BOLD);
                        rcustomer.setTypeface(null, Typeface.BOLD);
                        rtime.setTypeface(null, Typeface.BOLD);
                        ramt.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            TDHeader.setAdapter(laTDHeader);
        } catch (Exception e) {

        }
    }

    public void ViewDetailListview(){

        try {
            laTDDetails = new SimpleAdapter(this, TDViewTDay, R.layout.item_transactionsfortheday,
                    new String[]{"Type", "Customer", "Time", "Amt"}, new int[]{
                    R.id.rowsType, R.id.rowsCustomer, R.id.rowsTime, R.id.rowsAmt}) {
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
            TDDetails.setAdapter(laTDDetails);
        } catch (Exception e) {

        }
    }

    public void DialogTransactionsForTheDay() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TransactionsForTheDayActivity.this);
        LayoutInflater inflater = TransactionsForTheDayActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_transactionsfortheday, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        final TextView tvDTDTId = (TextView)dialogView.findViewById(R.id.tvDTDTId);
        tvDTDTTyp = (TextView)dialogView.findViewById(R.id.tvDTDTTyp);
        final TextView tvDTDCustomer = (TextView)dialogView.findViewById(R.id.tvDTDCustomer);
        final TextView tvDTDAmt = (TextView)dialogView.findViewById(R.id.tvDTDAmt);
        final TextView tvDTDDtTm = (TextView)dialogView.findViewById(R.id.tvDTDDtTm);
        TextView tvDTDOk = (TextView)dialogView.findViewById(R.id.tvDTDOk);
        TextView tvDTDPrint = (TextView)dialogView.findViewById(R.id.tvDTDPrint);
        TextView tvDTDViewQR = (TextView)dialogView.findViewById(R.id.tvDTDViewQR);

        tvDTDTId.setText(TDDViewTDay.get(0).get("ID"));
        tvDTDTTyp.setText(TDDViewTDay.get(0).get("Type"));
        tvDTDCustomer.setText(TDDViewTDay.get(0).get("Customer"));
        tvDTDAmt.setText(TDDViewTDay.get(0).get("Amt"));
        tvDTDDtTm.setText(TDDViewTDay.get(0).get("Time"));

        transferQR = "";
        transferItems = "";

        if (tvDTDTTyp.getText().toString().equals("ST")){

        }else if (tvDTDTTyp.getText().toString().equals("TMOUT")){
            tvDTDPrint.setVisibility(View.GONE);
            tvDTDViewQR.setVisibility(View.GONE);
        }else if (tvDTDTTyp.getText().toString().equals("TMIN")){
            tvDTDPrint.setVisibility(View.GONE);
            tvDTDViewQR.setVisibility(View.GONE);
        }else if (tvDTDTTyp.getText().toString().equals("CI")){
            tvDTDPrint.setVisibility(View.GONE);
            tvDTDViewQR.setVisibility(View.GONE);
        } else{
            tvDTDViewQR.setVisibility(View.GONE);
        }

        tvDTDOk.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvDTDViewQR.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                stocktransferitem = controller.fetchStockTransferItem(tvDTDTId.getText().toString().replace(tvDTDTTyp.getText().toString(),""));

                for(int i = 0; i < stocktransferitem.size(); i++) {

                    transferItems = transferItems + stocktransferitem.get(i).get("MaterialCode") + "," + stocktransferitem.get(i).get("Qty") + "/";

                }

                transferQR = tvDTDTId.getText().toString().replace(tvDTDTTyp.getText().toString(),"") + "<" + controller.fetchdbSettings().get(6) + "." + tvDTDCustomer.getText().toString() + ">" + transferItems;

                byte[] encodeValue = Base64.encode(new StringBuffer(transferQR).reverse().toString().getBytes(), Base64.DEFAULT);
                transferQR = new String(encodeValue).trim();

                Intent intent = new Intent(TransactionsForTheDayActivity.this, StockTransferQRActivity.class);
                intent.putExtra("qr", transferQR);
                startActivity(intent);


            }
        });

        tvDTDPrint.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {

                if (tvDTDTTyp.getText().toString().equals("ST")){
                    controller.PSTiD = tvDTDTId.getText().toString().replace(tvDTDTTyp.getText().toString(),"");
                    alPrintStockTransferH = controller.printStockTransferH(controller.PSTiD);
                }else if (tvDTDTTyp.getText().toString().equals("SR")){
                    SRSRid = tvDTDTId.getText().toString().replace(tvDTDTTyp.getText().toString(),"");
                    //Toasty.success(getApplicationContext(), SRSRid, Toast.LENGTH_LONG).show();
                }else if (tvDTDTTyp.getText().toString().equals("RET")){
                    controller.RICCode = tvDTDTId.getText().toString().replace(tvDTDTTyp.getText().toString(),"");
                }else if (tvDTDTTyp.getText().toString().equals("SALES")){
                    controller.SIDCode = tvDTDTId.getText().toString().replace(tvDTDTTyp.getText().toString(),"");
                }else{
                    controller.PDCode = tvDTDTId.getText().toString().replace(tvDTDTTyp.getText().toString(),"");
                }

                new Task().execute();
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

    //stock transfer

    void sendDataStockTransfer() {
        try {


            byte[] printformat = new byte[]{0x1B,0x21,0x03};
            mmOutputStream.write(printformat);

            String companyName = controller.fetchdbSettings().get(19);
            String title = "STOCK TRANSFER RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;

            printCustom(companyName,3);
            printCustom(title,3);
            printNewLine();
            printText(WithDate("Stock Transfer To: " + alPrintStockTransferH.get(0),alPrintStockTransferH.get(1)));
            //printText(leftMidRightAlign("Stock Transfer To: " + alPrintStockTransferH.get(0),"         " + alPrintStockTransferH.get(1), ""));
            //printCustom(getDateTime(),0);
            //printCustom("Stock Transfer To: " + controller.PSTSLoc,0);
            printNewLine();
            printCustom("Stock Transfer From: " + alPrintStockTransferH.get(2),0);
            printCustom("Terminal: " + TDListSettings.get(1),0);
            printCustom("Reference ID: " + controller.PSTiD,0);
            printNewLine();

            int countqty = 0;

            printText(getLine(57));
            printNewLine();

            for(int i = 1; i <= controller.fetchCountItem(controller.PSTiD,"StockTransferItem","StockTransferId"); i++) {


                alPrintStockTransferD = controller.printStockTransferD(controller.PSTiD,i);

                String item;
                item = alPrintStockTransferD.get(0);
                if (item.length() > 35){
                    item = item.substring(0,35);
                }


                printText(PrintDetails(item, alPrintStockTransferD.get(1), alPrintStockTransferD.get(2)));


                //printText(PrintDetails(controller.alSTItems.get(i), controller.alSTUnit.get(i), controller.alSTQty.get(i)));
                printNewLine();
                countqty = countqty + Integer.valueOf(alPrintStockTransferD.get(2));


                //Toasty.success(getApplicationContext(), String.valueOf(alPrintStockTransferD.size()), Toast.LENGTH_LONG).show();

            }

            printText(getLine(57));
            printNewLine();

            printText(PrintDetails("TOTAL","",String.valueOf(countqty)));

            printNewLine();
            printNewLine();



            printCustom("Salesman: " + alPrintStockTransferH.get(3),0);
            printNewLine();
            printNewLine();

            printCustom("SIGNATURE",0);

            printNewLine();

            Bitmap bmp = BitmapFactory.decodeByteArray(controller.fetchSignature(controller.PSTiD), 0, controller.fetchSignature(controller.PSTiD).length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmp, 250, 90, false);

            printImage(resizedbmp);

            printCustom("Signed by: " + alPrintStockTransferH.get(4),0);

            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();



            // tell the user data were sent
            //myLabel.setText("Data sent.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String PrintDetails(String str1, String str2,String str3) {
        int fulllength = 47;
        int strlength = 5;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength - str3length;

        String ans = str1 + getSpace(anslength) + str2  + getSpace(anslength2) +  getSpace(anslength3) +  str3;
        return ans;
    }

    //end of stock transfer

    //start of stock transfer

    void sendDataStockReceiving() {
        try {

            byte[] printformat = new byte[]{0x1B,0x21,0x03};
            mmOutputStream.write(printformat);

            String companyName = controller.fetchdbSettings().get(19);
            String title = "STOCK RECEIVING RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;

            printCustom(companyName,3);
            printCustom(title,3);
            printNewLine();

            alPrintStockReceivingH = controller.printStockReceivingH(SRSRid);

            //printText(leftMidRightAlign("Stock Receiving To: " + alPrintStockReceivingH.get(1),"         " + alPrintStockReceivingH.get(0), ""));
            printText(WithDate("Stock Receiving To: " + alPrintStockReceivingH.get(1),alPrintStockReceivingH.get(0)));
            printNewLine();
            printCustom("Terminal: " + TDListSettings.get(1),0);
            printCustom("Reference ID: " + SRSRid,0);

            printNewLine();

            int countqty = 0;

            printText(getLine(57));
            printNewLine();



            for(int i = 1; i <= controller.fetchCountItem(SRSRid,"StockReceivingItem","StockReceivingId"); i++) {


                alPrintStockReceivingD = controller.printStockReceivingD(SRSRid,i);

                String item;
                item = alPrintStockReceivingD.get(1);
                if (item.length() > 35){
                    item = item.substring(0,35);
                }


                printText(PrintDetailsSR(alPrintStockReceivingD.get(0),item, alPrintStockReceivingD.get(2), alPrintStockReceivingD.get(3)));


                //printText(PrintDetails(controller.alSTItems.get(i), controller.alSTUnit.get(i), controller.alSTQty.get(i)));
                printNewLine();
                countqty = countqty + Integer.valueOf(alPrintStockReceivingD.get(3));


                //Toasty.success(getApplicationContext(), String.valueOf(alPrintStockTransferD.size()), Toast.LENGTH_LONG).show();

            }

            printText(getLine(57));
            printNewLine();

            printText(PrintDetailsSR("TOTAL","","",String.valueOf(countqty)));

            printNewLine();
            printNewLine();


            printCustom("Issued by: " + alPrintStockReceivingH.get(2),0);
            printNewLine();
            printNewLine();
            printNewLine();
            printNewLine();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String PrintDetailsSR(String str0,String str1, String str2,String str3) {
        int fulllength = 42;
        int strlength = 5;

        int str0length = str0.length();
        int anslength0 = strlength - str0length;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength - str3length;

        String ans = str0 + getSpace(anslength0) + str1 + getSpace(anslength) + str2  + getSpace(anslength2) +  getSpace(anslength3) +  str3;
        return ans;
    }

    //end of Stock Receiving

    //start of Return Item

    void sendDataReturnedItem() {
        try {

            byte[] printformat = new byte[]{0x1B,0x21,0x03};
            mmOutputStream.write(printformat);

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.print_logo);

            printImage(bmp);

            String companyName = controller.fetchdbSettings().get(19);
            String title = "RETURN SLIP";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;
            String address,contactNo;
            address = getSpace(printHalfLen - (TDListSettings.get(13).length()/2)) + TDListSettings.get(13);
            contactNo = getSpace(printHalfLen - (TDListSettings.get(10).length()/2)) + TDListSettings.get(10);

            printCustom(companyName,3);
            printCustom( address,0);
            printCustom( contactNo,0);
            printNewLine();
            printCustom(title,3);
            printNewLine();

            alPrintReturnsH = controller.printReturnsH(controller.RICCode);

            printText(WithDate("Customer Code: " + alPrintReturnsH.get(0), alPrintReturnsH.get(1)));
            printNewLine();
            printCustom("Customer: " + alPrintReturnsH.get(2),0);
            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1),0);
            printCustom("Terminal: " + TDListSettings.get(1),0);
            printCustom("ReturnID: " + controller.RICCode,0);
            printNewLine();

            printText(getLine(57));
            printNewLine();

            printCustom(alPrintReturnsH.get(4),0);

            double TotalAmtReturns = 0.00;


            for(int i = 1; i <= controller.fetchCountItem(controller.RICCode,"ReturnItem","RetId"); i++) {


                alPrintReturnsD = controller.printReturnsD(controller.RICCode,i);

                alPrintReturnsD = controller.printReturnsD(controller.RICCode,i);

                String item;
                item = alPrintReturnsD.get(1);
                if (item.length() > 35){
                    item = item.substring(0,35);
                }


                printText(PrintDetailsRI(alPrintReturnsD.get(0),item, alPrintReturnsD.get(2), alPrintReturnsD.get(3)));

                TotalAmtReturns = TotalAmtReturns + Double.valueOf(alPrintReturnsD.get(3).replace(",",""));


                //printText(PrintDetails(controller.alSTItems.get(i), controller.alSTUnit.get(i), controller.alSTQty.get(i)));
                printNewLine();

                //Toasty.success(getApplicationContext(), String.valueOf(alPrintStockTransferD.size()), Toast.LENGTH_LONG).show();

            }

            printText(getLine(57));

            printNewLine();
            printText(PrintDetailsRI("","Gross Amount","",String.valueOf(TDAmt.format(TotalAmtReturns))));
            printNewLine();

            double disc = 0.00;

            disc =  TotalAmtReturns - Double.valueOf(alPrintReturnsH.get(3).replace(",",""));

            if (disc != 0.00){
                printText(PrintDetailsRI("","Discount","",String.valueOf(TDAmt.format(disc))));
                printNewLine();
            }


            printNewLine();
            printText(PrintDetailsRI("","Net Amount","",alPrintReturnsH.get(3)));
            printNewLine();
            printNewLine();

            printCustom("SIGNATURE",0);

            printNewLine();

            Bitmap bmps = BitmapFactory.decodeByteArray(controller.fetchSignatureRItem(controller.RICCode), 0, controller.fetchSignatureRItem(controller.RICCode).length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmps, 250, 90, false);

            printImage(resizedbmp);

            printCustom("Signed by: " + alPrintReturnsH.get(5),0);
            printNewLine();

            String website = controller.fetchdbSettings().get(20);
            if (website.equals("")){
                website = "";
            }else{
                website = getSpace(printHalfLen - (website.length()/2)) + website;
            }
            String footerTitle = "CUSTOMER SERVICE";
            String footerContactNo = TDListSettings.get(12);
            String forOrders = "For Orders please contact";
            String orderContactNo = TDListSettings.get(11);
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

    private String PrintDetailsRI(String str0,String str1, String str2,String str3) {
        int fulllength = 36;
        int strlength = 8;
        int strlength0 = 4;
        int strlength1 = 9;

        int str0length = str0.length();
        int anslength0 = strlength0 - str0length;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength1 - str3length;

        String ans = str0 + getSpace(anslength0) + str1 + getSpace(anslength) + str2  + getSpace(anslength2) +  getSpace(anslength3) +  str3;
        return ans;
    }

    //end of returned item

    //start of SalesOrder
    void sendDataSalesOrder() {
        try {

            byte[] printformat = new byte[]{0x1B,0x21,0x03};
            mmOutputStream.write(printformat);

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.print_logo);

            printImage(bmp);

            String companyName = controller.fetchdbSettings().get(19);
            String title = "DELIVERY RECEIPT";
            int printHalfLen = 28;
            companyName = getSpace(printHalfLen - (companyName.length()/2)) + companyName;
            title = getSpace(printHalfLen - (title.length()/2)) + title;
            String address,contactNo;
            address = getSpace(printHalfLen - (TDListSettings.get(13).length()/2)) + TDListSettings.get(13);
            contactNo = getSpace(printHalfLen - (TDListSettings.get(10).length()/2)) + TDListSettings.get(10);

            printCustom(companyName,3);
            printCustom( address,0);
            printCustom( contactNo,0);
            printNewLine();
            printCustom(title,3);
            printNewLine();

            String PaymentMode = "\\ ";

            int tmsmode = 0;

            for(int i = 0; i < controller.printPaymentItemD(controller.SIDCode).size() ; i++){

                if (controller.printPaymentItemD(controller.SIDCode).get(i).get("PaymentMode").equals("TMS")){
                    tmsmode = 1;
                }else if(controller.printPaymentItemD(controller.SIDCode).get(i).get("PaymentMode").equals("CASH SALES")){
                    tmsmode = 2;
                }else if (controller.printPaymentItemD(controller.SIDCode).get(i).get("PaymentMode").equals("CHARGED SALES")){
                    tmsmode = 3;
                }
                PaymentMode = PaymentMode + controller.printPaymentItemD(controller.SIDCode).get(i).get("PaymentMode") + " \\" ;


            }

            printCustom(PaymentMode,0);


            alPrintSOrderH = controller.printSOrderH(controller.SIDCode);

            printText(WithDate("Customer Code: " + alPrintSOrderH.get(0), alPrintSOrderH.get(1)));
            printNewLine();
            printCustom("Customer: " + alPrintSOrderH.get(2),0);
            printCustom("Salesman: " + controller.fetchRIDNmUsers().get(1),0);
            printCustom("Terminal: " + TDListSettings.get(1),0);
            printCustom("Sales ID: " + controller.SIDCode,0);
            printCustom("Payment ID: " + alPrintSOrderH.get(3),0);
            printCustom("Invoice No.: " + alPrintSOrderH.get(13),0);
            printNewLine();

            alPrintSOrderD = controller.printSOrderD(controller.SIDCode);

            printText(PrintDetailsSO("#","QTY","ITEM","PRICE","AMOUNT"));
            printNewLine();

            printText(getLine(57));
            printNewLine();

            int totalqty;
            totalqty = 0;

            for(int i = 0; i < alPrintSOrderD.size() ; i++){

                String item;
                item = alPrintSOrderD.get(i).get("MatDesc");
                if (item.length() > 29){
                    item = item.substring(0,29);
                }

                totalqty = totalqty + Integer.valueOf(alPrintSOrderD.get(i).get("Qty"));

                printText(PrintDetailsSO(String.valueOf(i+1),alPrintSOrderD.get(i).get("Qty"),item,alPrintSOrderD.get(i).get("Amt"),alPrintSOrderD.get(i).get("Total")));
                printNewLine();
            }

            printText(getLine(57));
            printNewLine();


            alPrintSOrderDPromo = controller.printSOrderPromo(controller.SIDCode);
            if (alPrintSOrderDPromo.size() != 0){
                printNewLine();
                printText(PrintDetailsSO("#","QTY","PROMO ITEM","PRICE","AMOUNT"));
                printNewLine();

                printText(getLine(57));
                printNewLine();

                for(int i = 0; i < alPrintSOrderDPromo.size() ; i++){

                    String item;
                    item = alPrintSOrderDPromo.get(i).get("MatDesc");
                    if (item.length() > 29){
                        item = item.substring(0,29);
                    }

                    totalqty = totalqty + Integer.valueOf(alPrintSOrderDPromo.get(i).get("Qty"));

                    printText(PrintDetailsSO(String.valueOf(i+1),alPrintSOrderDPromo.get(i).get("Qty"),item,alPrintSOrderDPromo.get(i).get("Amt"),alPrintSOrderDPromo.get(i).get("Total")));
                    printNewLine();
                }

                printText(getLine(57));
                printNewLine();
            }


            int grosstotal = 0;
            grosstotal = alPrintSOrderD.size() + alPrintSOrderDPromo.size();

            printText2(PrintGrossDetailsSO("Gross Total: (" + grosstotal + " ITEM/S)" , String.valueOf(totalqty),alPrintSOrderH.get(4)));
            printNewLine();
            printNewLine();

            if (!alPrintSOrderH.get(12).equals("0.00")){
                printText(PrintAmtDetailsSO("Discount:", alPrintSOrderH.get(12)));
                printNewLine();
            }

            if (!alPrintSOrderH.get(5).equals("0.00") && tmsmode == 3){

            }else{
                printText(PrintAmtDetailsSO("Less Returns:", alPrintSOrderH.get(5)));
                printNewLine();
            }


            printText2(PrintAmtDetailsSO("Net Amount:", alPrintSOrderH.get(6)));
            printNewLine();
            printNewLine();

            if (!alPrintSOrderH.get(7).equals("0.00")){
                printText(PrintAmtDetailsSO("Cash Received:", alPrintSOrderH.get(7)));
                printNewLine();
            }



            if (tmsmode == 1){

            }else if (tmsmode == 2 && alPrintSOrderH.get(8).equals("0.00")){

            }else{
                printText(PrintAmtDetails("Checks Received:", alPrintSOrderH.get(8)));
                printNewLine();
            }


            if (!alPrintSOrderH.get(9).equals("0.00")){
                printText(PrintAmtDetailsSO("Charged Sales:", alPrintSOrderH.get(9)));
                printNewLine();
            }

            if (tmsmode == 1){
                printText(PrintAmtDetailsSO("TMS Payment:", alPrintSOrderH.get(6)));
                printNewLine();
            }

            printText(PrintAmtDetailsSO("Change:", alPrintSOrderH.get(10)));
            printNewLine();

            printText(getLine(57));
            printNewLine();

            printCustom("IMPORTANT",0);
            printCustom("PLEASE MAKE CHECK PAYABLE TO:",0);
            printCustom(controller.fetchdbSettings().get(19),0);
            printCustom("ALWAYS ASK FOR PROVISIONAL RECEIPT OR OFFICIAL RECEIPT",0);
            printCustom("WHEN MAKING PAYMENT, AND KEEP IT ON FILE. THIS IS YOUR",0);
            printCustom("PROOF OF PAYMENT",0);

            printNewLine();
            printCustom("SIGNATURE",0);

            printNewLine();

            Bitmap bmps = BitmapFactory.decodeByteArray(controller.fetchSignatureSItem(controller.SIDCode), 0, controller.fetchSignatureSItem(controller.SIDCode).length);

            Bitmap resizedbmp = Bitmap.createScaledBitmap(bmps, 250, 90, false);

            printImage(resizedbmp);

            printCustom("Signed by:" + alPrintSOrderH.get(11),0);
            printNewLine();

            String website = controller.fetchdbSettings().get(20);
            if (website.equals("")){
                website = "";
            }else{
                website = getSpace(printHalfLen - (website.length()/2)) + website;
            }
            String footerTitle = "CUSTOMER SERVICE";
            String footerContactNo = TDListSettings.get(12);
            String forOrders = "For Orders please contact";
            String orderContactNo = TDListSettings.get(11);
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

    private String PrintAmtDetailsSO(String str1,String str2) {
        int fulllength = 42;
        int strlength = 15;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;


        String ans = str1 + getSpace(anslength) +  getSpace(anslength2) + str2  ;
        return ans;
    }
    private String PrintGrossDetailsSO(String str1,String str2,String str3) {
        int fulllength = 33;
        int strlength2 = 5;
        int strlength3 = 19;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2  = strlength2 - str2length;

        int str3length = str3.length();
        int anslength3 = strlength3 - str3length;


        String ans = str1 + getSpace(anslength)  + str2 +  getSpace(anslength2) +  getSpace(anslength3) + str3 ;
        return ans;
    }

    private String PrintDetailsSO(String str4,String str0,String str1, String str2,String str3) {
        int fulllength = 30;
        int strlength = 8;
        int strlength0 = 6;
        int strlength2 = 10;
        int strlength4 =3;

        int str4length = str4.length();
        int anslength4 = strlength4 - str4length;

        int str0length = str0.length();
        int anslength0 = strlength0 - str0length;

        int str1length = str1.length();
        int anslength  = fulllength - str1length;

        int str2length = str2.length();
        int anslength2 = strlength - str2length;
        int str3length = str3.length();
        int anslength3 = strlength2 - str3length;

        String ans =  str4 + getSpace(anslength4) + str1 + getSpace(anslength) + str0 + getSpace(anslength0) + str2  + getSpace(anslength2) + getSpace(anslength3) +  str3;
        return ans;
    }

    //end of Sales Order

    //start of AR

    void sendDataAR() {
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
            address = getSpace(printHalfLen - (TDListSettings.get(13).length()/2)) + TDListSettings.get(13);
            contactNo = getSpace(printHalfLen - (TDListSettings.get(10).length()/2)) + TDListSettings.get(10);

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
            printCustom("Terminal: " + TDListSettings.get(1),0);
            printCustom("Payment ID: " + controller.PDCode,0);
            printNewLine();

            printText(PrintAmtDetails("Amount Due:", alPrintARPaymentH.get(5)));
            printNewLine();
            printText(PrintAmtDetails("Less Returns:", alPrintARPaymentH.get(4)));
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

            controller.PCCode = alPrintARPaymentH.get(0);
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
            String footerContactNo = TDListSettings.get(12);
            String forOrders = "For Orders please contact";
            String orderContactNo = TDListSettings.get(11);
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

            byte[] cc = new byte[]{0x1B,0x21,0x03};  // 0- normal size text
            //byte[] cc1 = new byte[]{0x1B,0x21,0x00};  // 0- normal size text
            byte[] bb = new byte[]{0x1B,0x21,0x08};  // 1- only bold text
            byte[] bb2 = new byte[]{0x1B,0x21,0x20}; // 2- bold with medium text
            byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
            // Print normal text
            mmOutputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printText2(String msg) {
        try {
            byte[] bb3 = new byte[]{0x1B,0x21,0x10}; // 3- bold with large text
            // Print normal text
            mmOutputStream.write(bb3);
            mmOutputStream.write(msg.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //print byte[]
    private void printText(byte[] msg) {
        try {
            // Print normal text
            mmOutputStream.write(msg);
            printNewLine();
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
            ans = str1 + new String(new char[n]).replace("\0", " ") + str2 + "     " + str3;
        }
        return ans;
    }

    public void ProgressDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TransactionsForTheDayActivity.this);
        LayoutInflater inflater = TransactionsForTheDayActivity.this.getLayoutInflater();
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

            }else{
                Toasty.error(getApplicationContext(),"bluetooth device not found" , Toast.LENGTH_LONG).show();

            }

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            findBT();

            openBT();
            if (bluetoothstatus == 1){
                //progress();A

                if(tvDTDTTyp.getText().toString().equals("ST")){
                    sendDataStockTransfer();
                }else if (tvDTDTTyp.getText().toString().equals("SR")){
                    sendDataStockReceiving();
                }else if (tvDTDTTyp.getText().toString().equals("RET")){
                    sendDataReturnedItem();
                }else if (tvDTDTTyp.getText().toString().equals("SALES")){
                    sendDataSalesOrder();
                }else{
                    sendDataAR();
                }
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

    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(TransactionsForTheDayActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }


}


