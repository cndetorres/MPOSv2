package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class ProductInfoActivity extends Activity {

    DBController controller = new DBController(this);
    ListView PILDetails;
    ArrayList<HashMap<String, String>> hmPILDetails;
    ListAdapter laPILDetails;
    HashMap<String, String> mPILDetail;
    ArrayList<String> sPILDetails;
    String path;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productinfo);

        PILDetails = (ListView) findViewById(R.id.lvPILDetail);

        File productinfopath = new File(controller.productinfo);
        File[] strFileName = productinfopath.listFiles();

        try {
            sPILDetails = new ArrayList<String>();
            for (int i = 0; i < strFileName.length;i++){
                if (strFileName[i].getName().endsWith(".png")){
                    sPILDetails.add(strFileName[i].getName());
                }
            }
        }catch (Exception e){
            Toasty.error(this, "no defined file path", Toast.LENGTH_LONG).show();
        }


        ViewListview();

        PILDetails.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                path = controller.productinfo + "/" + hmPILDetails.get(position).get("Header");

                ViewProductInfo();
            }
        });

    }

    public void ViewProductInfo() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProductInfoActivity.this);
        LayoutInflater inflater = ProductInfoActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_viewproductinfo, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setCanceledOnTouchOutside(false);

        File imgFile = new  File(path);

        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            ZoomageView ivImage = (ZoomageView) dialogView.findViewById(R.id.ivImage);

            ivImage.setImageBitmap(myBitmap);

        }

        alertDialog.show();


    }







    public void ViewListview() {

        hmPILDetails = new ArrayList<HashMap<String, String>>();

        for(int i = 0; i < sPILDetails.size(); i++) {
            mPILDetail = new HashMap<String, String>();
            mPILDetail.put("Header", sPILDetails.get(i));
            hmPILDetails.add(mPILDetail);
        }

        try {
            laPILDetails = new SimpleAdapter(this, hmPILDetails, R.layout.item_productinfolist,
                    new String[]{"Header"}, new int[]{
                    R.id.rowsHeader}) {
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

            PILDetails.setAdapter(laPILDetails);

        } catch (Exception e) {

        }
    }


    public void onBackPressed() {
        Intent IntentMiscellaneousActivity = new Intent(ProductInfoActivity.this, MiscellaneousActivity.class);
        startActivity(IntentMiscellaneousActivity);
        finish();
    }
}

