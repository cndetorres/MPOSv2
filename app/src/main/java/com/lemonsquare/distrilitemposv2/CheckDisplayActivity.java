package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class CheckDisplayActivity extends Activity {

    BottomNavigationView btCDNavigation;
    boolean isClickedTwice;
    private long mLastClickTime = 0;
    DBController controller = new DBController(this);
    Context context = this;
    ListView CDDetails,CDHeader;
    ArrayList<HashMap<String, String>> hmCDHeader;
    ListAdapter laCDHeader;

    SimpleAdapter laCDDetails;
    List<HashMap<String, String>> ViewCDItems;
    HashMap<String, String> mCDHeader;

    ImageView photo;
    EditText displayType,assetNo;

    HashMap<String, String> mCDDetails;
    ArrayList<HashMap<String, String>> hmCDDetails;
    Boolean isPhoto;
    Boolean isAdd;
    String PDisplayType,PAssetNo,PPicture;
    int Pposition;
    Boolean isNothing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkdisplay);

        isClickedTwice = false;
        btCDNavigation = (BottomNavigationView) findViewById(R.id.btCDNavigation);
        CDDetails = (ListView) findViewById(R.id.lvCDDetails);
        CDHeader = (ListView) findViewById(R.id.lvCDHeader);

        ViewHeaderListview();

        CDDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(position);
                String objDisplayType = (String) obj.get("DisplayType");
                PDisplayType = objDisplayType;
                String objAssetNo = (String) obj.get("AssetNo");
                PAssetNo = objAssetNo;
                String objPicture = (String) obj.get("Picture");
                PPicture = objPicture;
                Pposition = position;
                isAdd = false;
                DialogAddItem();

            }
        });

        btCDNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mcd_additem:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return false;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime();

                                isAdd = true;
                                DialogAddItem();


                                break;

                            case R.id.mcd_submit:

                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                                    return false;
                                }

                                if (CDDetails.getCount() == 0){
                                    isNothing = true;
                                    messagebox("No data on the list. Do you want to proceed to the next activity?");
                                }else{
                                    isNothing = false;
                                    messagebox("Are you sure you want to save display info?");
                                }

                                break;

                        }
                        return true;
                    }
                });
    }
    public void DialogAddItem() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(CheckDisplayActivity.this);
        LayoutInflater inflater = CheckDisplayActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_addcheckdisplay, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);

        photo  = (ImageView) dialogView.findViewById(R.id.ivCDPhoto);
        ImageView camera = (ImageView) dialogView.findViewById(R.id.ivCDCamera);
        displayType = (EditText) dialogView.findViewById(R.id.etDCDDisplayType);
        Button scan = (Button) dialogView.findViewById(R.id.btnDCDScan);
        assetNo = (EditText) dialogView.findViewById(R.id.etDCDAssetNo);
        TextView cancel = (TextView) dialogView.findViewById(R.id.tvDCDCancel);
        TextView save = (TextView) dialogView.findViewById(R.id.tvDCDSave);

        if (!isAdd){
            displayType.setText(PDisplayType);
            assetNo.setText(PAssetNo);
            Bitmap resizedbmp = Bitmap.createScaledBitmap(StringToBitMap(PPicture), 400, 300, false);
            photo.setImageBitmap(resizedbmp);
            save.setText("DELETE");
            save.setTextColor(getResources().getColor(R.color.red));
            camera.setVisibility(View.GONE);
            scan.setEnabled(false);
        }

        camera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                isPhoto = false;

                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 7);

            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new IntentIntegrator(CheckDisplayActivity.this).setCaptureActivity(ScanActivity.class).initiateScan();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

               alertDialog.dismiss();

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (save.getText().toString().equals("DELETE")){
                    hmCDDetails.remove(Pposition);
                    hmCDDetails.clear();
                    laCDDetails.notifyDataSetChanged();
                    Toasty.error(getApplicationContext(), "Display info has been deleted.", Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }else{
                    if (!isPhoto){
                        Toasty.info(getApplicationContext(), "Please add photo of display.", Toast.LENGTH_LONG).show();
                    }else if (displayType.getText().toString().equals("")){
                        Toasty.info(getApplicationContext(), "Please scan display QR.", Toast.LENGTH_LONG).show();
                    }else {
                        final ArrayList<String> alADetails = new ArrayList<String>();
                        alADetails.clear();

                        if (CDDetails.getCount() > 0) {
                            for (int i = 0; i < CDDetails.getAdapter().getCount(); i++) {
                                HashMap<String, Object> obj = (HashMap<String, Object>) laCDDetails.getItem(i);
                                String objAssetNo = (String) obj.get("AssetNo");
                                alADetails.add(objAssetNo);
                            }
                        }
                        if (alADetails.contains(assetNo.getText().toString())) {
                            Toasty.error(getApplicationContext(), "Display info already in the list", Toast.LENGTH_LONG).show();
                        } else {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            Bitmap bitmap = ((BitmapDrawable) photo.getDrawable()).getBitmap();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                            byte[] bytes = byteArrayOutputStream.toByteArray();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String stringPhoto;
                                stringPhoto = Base64.getEncoder().encodeToString(bytes);
                                AddDetailListview(displayType.getText().toString(), assetNo.getText().toString(), stringPhoto);
                                Toasty.success(getApplicationContext(), "Display info successfully added.", Toast.LENGTH_LONG).show();
                                alertDialog.dismiss();
                            }
                        }
                    }
                }
            }
        });


        alertDialog.show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case 7:
                switch (resultCode) {
                    case RESULT_OK:

                        isPhoto = true;

                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        Bitmap bmpimg = Bitmap.createScaledBitmap(bitmap, 400, 300, true);

                        photo.setImageBitmap(bmpimg);

                        break;

                    case RESULT_CANCELED:

                        isPhoto = false;

                        Toasty.error(getApplicationContext(), "Unable to capture image", Toast.LENGTH_LONG).show();
                        break;
                }
            default:
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() == null) {
                        Toasty.error(this, "Scan Cancelled", Toast.LENGTH_LONG).show();
                    } else {
                        showResultDialogue(result.getContents());
                    }
                } else {
                    super.onActivityResult(requestCode, resultCode, data);
                }
        }
    }

    public void showResultDialogue(final String result) {

        String displayQR = StringUtils.substringAfterLast(result, "/");
        byte[] decodedBytes = new byte[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decodedBytes = Base64.getDecoder().decode(displayQR);
        }

        StringBuilder reverseString = new StringBuilder(new String(decodedBytes));
        reverseString.reverse();

        String reversedString = reverseString.toString();

        assetNo.setText(reversedString);

        if (reverseString.substring(0,3).equals("WIR")){
            displayType.setText("WIRE RACK");
        }


    }

    public void ViewHeaderListview() {

        hmCDHeader = new ArrayList<HashMap<String, String>>();
        mCDHeader = new HashMap<String, String>();
        hmCDDetails = new ArrayList<HashMap<String, String>>();

        mCDHeader.put("DisplayType", "DISPLAY TYPE");
        mCDHeader.put("AssetNo", "ASSET NO");
        mCDHeader.put("Picture", "PICTURE");
        hmCDHeader.add(mCDHeader);

        try {
            laCDHeader = new SimpleAdapter(this, hmCDHeader, R.layout.item_checkdisplay_header,
                    new String[]{"DisplayType", "AssetNo", "Picture"}, new int[]{
                    R.id.rowsDisplayType, R.id.rowsAssetNo, R.id.rowsPicture}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    TextView rdisplaytype = (TextView) view.findViewById(R.id.rowsDisplayType);
                    TextView rassetno = (TextView) view.findViewById(R.id.rowsAssetNo);
                    TextView rpicture = (TextView) view.findViewById(R.id.rowsPicture);
                    if (position % 2 == 0) {
                        rdisplaytype.setTextColor(Color.WHITE);
                        rassetno.setTextColor(Color.WHITE);
                        rpicture.setTextColor(Color.WHITE);
                        rdisplaytype.setTypeface(null, Typeface.BOLD);
                        rassetno.setTypeface(null, Typeface.BOLD);
                        rpicture.setTypeface(null, Typeface.BOLD);
                        view.setBackgroundResource(R.color.header);
                    }
                    return view;
                }
            };

            CDHeader.setAdapter(laCDHeader);
        } catch (Exception e) {

        }
    }

    public void AddDetailListview(String displayType,String assetNo,String picture){

        mCDDetails = new HashMap<String, String>();
        mCDDetails.put("DisplayType",displayType);
        mCDDetails.put("AssetNo", assetNo);
        mCDDetails.put("Picture", picture);
        hmCDDetails.add(mCDDetails);

        try {
            laCDDetails = new SimpleAdapter(this, hmCDDetails, R.layout.item_checkdisplay_details,
                    new String[]{"DisplayType", "AssetNo", "Picture"}, new int[]{
                    R.id.rowsDisplayType, R.id.rowsAssetNo, R.id.rowsPicture}) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    ImageView rpicture = (ImageView) view.findViewById(R.id.rowsPicture);
                    Bitmap resizedbmp = Bitmap.createScaledBitmap(StringToBitMap(picture), 100, 100, false);
                    rpicture.setImageBitmap(resizedbmp);

                    if (position % 2 == 1) {
                        view.setBackgroundResource(R.color.odd);
                    } else {
                        view.setBackgroundResource(R.color.even);
                    }
                    return view;

                }
            };
            CDDetails.setAdapter(laCDDetails);
        } catch (Exception e) {

        }


    }

    void  messagebox(String alerttext) {

        new android.app.AlertDialog.Builder(CheckDisplayActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Check Display")
                .setMessage(alerttext)
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DateFormat defaultDateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss aa");
                        DateFormat defaultDateFormat1 = new SimpleDateFormat("yyMMddHHmmss");
                        Calendar defaultDate = Calendar.getInstance();
                        String logDate,displayID;
                        logDate = defaultDateFormat2.format(defaultDate.getTime());
                        displayID = "CD" + defaultDateFormat1.format(defaultDate.getTime());

                        if (isNothing){
                            controller.insertCustomerLogsItem(controller.fetchLogID(),5,logDate,1);
                            Toasty.success(getApplicationContext(), "Display info successfully skipped.", Toast.LENGTH_LONG).show();
                        }else{
                            controller.insertCustomerLogsItem(controller.fetchLogID(),5,logDate,0);
                            controller.insertCheckDisplay(controller.PCCode,displayID,logDate,controller.PUName);
                            for (int i = 0; i < hmCDDetails.size(); i++) {
                                byte[] imageInByte;
                                String image = hmCDDetails.get(i).get("Picture");
                                Bitmap resizedbmp = Bitmap.createScaledBitmap(StringToBitMap(image), 400, 300, false);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                resizedbmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                imageInByte = baos.toByteArray();
                                controller.insertCheckDisplayItem(displayID,hmCDDetails.get(i).get("DisplayType"),hmCDDetails.get(i).get("AssetNo"),imageInByte);
                            }

                            Toasty.success(getApplicationContext(), "Display info successfully saved.", Toast.LENGTH_LONG).show();
                        }

                        Intent intent = new Intent(context, PriceSurveyActivity.class);
                        intent.putExtra("CustomerCode", controller.PCCode);
                        startActivity(intent);
                        finish();

                        dialog.dismiss();

                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= new byte[0];
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                encodeByte = Base64.getDecoder().decode(encodedString);
            }
            Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

}

