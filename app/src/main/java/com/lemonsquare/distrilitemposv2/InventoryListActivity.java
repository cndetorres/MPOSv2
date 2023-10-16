package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;

import es.dmoral.toasty.Toasty;


public class InventoryListActivity extends Activity {

    DBController controller = new DBController(this);
    ListView ILDetails;
    List<HashMap<String, String>> ILViewIList;
    SimpleAdapter laILDetails;
    EditText etILSearch;
    BottomNavigationView menu;
    ImageView ivILCancel;

    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventorylist);

        ILDetails = (ListView) findViewById(R.id.lvILDetail);
        etILSearch = (EditText) findViewById(R.id.etILSearch);
        menu = (BottomNavigationView) findViewById(R.id.btILNavigation);
        ivILCancel = (ImageView) findViewById(R.id.ivILCancel);

        ILViewIList = controller.fetchInventoryList();

        DetailActivity();

        ivILCancel.setVisibility(View.GONE);

        etILSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                if (arg0.length() == 0){

                    ivILCancel.setVisibility(View.GONE);
                    ILViewIList = controller.fetchInventoryList();
                    DetailActivity();

                }else{

                    ivILCancel.setVisibility(View.VISIBLE);
                    ILDetails.invalidateViews();
                    ILViewIList = controller.fetchInventoryList(etILSearch.getText().toString());
                    DetailActivity();

                }
            }

        });

        ivILCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                etILSearch.getText().clear();
            }
        });

        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mil_stransfer:

                                if (controller.fetchISSDR() == 2){
                                    Toasty.info(InventoryListActivity.this,"please time in first", Toast.LENGTH_LONG).show();
                                }else if (controller.fetchTimeInOutComplete() == 1){
                                    Toasty.info(InventoryListActivity.this,"you have already completed your transaction for today", Toast.LENGTH_LONG).show();
                                }else{
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                        return false;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();

                                    controller.PSTSLocNum = 0;

                                    Intent intentStockTransferActivity = new Intent(InventoryListActivity.this, StockTransferActivity.class);
                                    startActivity(intentStockTransferActivity);
                                    finish();
                                }




                                break;
                            case R.id.mil_sreceiveing:

                                if (controller.fetchISSDR() == 2){
                                    Toasty.info(InventoryListActivity.this,"please time in first", Toast.LENGTH_LONG).show();
                                }else if (controller.fetchTimeInOutComplete() == 1){
                                    Toasty.info(InventoryListActivity.this,"you have already completed your transaction for today", Toast.LENGTH_LONG).show();
                                }else{
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                        return false;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();

                                    /*Intent intentStockReceivingActivity = new Intent(InventoryListActivity.this, StockReceivingActivity.class);
                                    startActivity(intentStockReceivingActivity);
                                    finish();*/
                                    new IntentIntegrator(InventoryListActivity.this).setCaptureActivity(ReceivingDialog.class).initiateScan();
                                }

                                break;

                        }
                        return true;
                    }
                });

    }


    public void DetailActivity() {


        try {
            laILDetails = new SimpleAdapter(this, ILViewIList, R.layout.item_inventorylist,
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
            ILDetails.setAdapter(laILDetails);
        } catch (Exception e) {

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public void showResultDialogue(final String result) {

        String srqrid,stocktransferid,sloctransferto,transferto;

        try {

            byte[] data = Base64.decode(result, Base64.DEFAULT);
            String text = new StringBuilder(new String(data, "UTF-8")).reverse().toString();
            System.out.println(text);

            srqrid = StringUtils.substringBefore(text,">");
            stocktransferid = StringUtils.substringBefore(srqrid,"<");
            sloctransferto = StringUtils.substringAfter(srqrid,"<");
            transferto = StringUtils.substringAfter(sloctransferto,".");

            if(controller.fetchStockTransfeID(stocktransferid) == 1){
                Toasty.error(this, "You have already received the stocks from MPOS.", Toast.LENGTH_LONG).show();
            }else if(!controller.fetchdbSettings().get(7).equals(transferto)){
                Toasty.error(this, "Stock transfer receiver is: " + controller.fetchdbSettings().get(6), Toast.LENGTH_LONG).show();
            }else{
                Intent intent = new Intent(InventoryListActivity.this, StockReceivingQRActivity.class);
                intent.putExtra("result", text);
                startActivity(intent);
            }



        }catch (Exception e) {
            Toasty.error(this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
        }

    }

    public void onBackPressed() {
        Intent IntentMainActivity = new Intent(InventoryListActivity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }



}


