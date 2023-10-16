package com.lemonsquare.distrilitemposv2;

import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Toast;


import es.dmoral.toasty.Toasty;

import static com.lemonsquare.distrilitemposv2.CustomerListExpandableListview.DialogCustomerSelecttransaction;
import static com.lemonsquare.distrilitemposv2.CustomerListExpandableListview.request_checkin;


public class CustomerList2Activity extends Activity {

    DBController controller = new DBController(this);
    CustomerListExpandableListview laCLExpandable;
    ExpandableListView lvCLDetail;
    int PPosition = -1;
    EditText etCLSearch;
    List<HashMap<String, String>> CLViewCList;
    BottomNavigationView menu;
    ImageView ivCLCancel;
    boolean isTwiceClicked;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customerlist2);

        lvCLDetail = (ExpandableListView) findViewById(R.id.lvCLDetail);
        etCLSearch = (EditText) findViewById(R.id.etCLSearch);
        menu = (BottomNavigationView) findViewById(R.id.btCLNavigation);
        ivCLCancel = (ImageView) findViewById(R.id.ivCLCancel);

        controller.PIsCustomer = 0;

        isTwiceClicked = false;

        CLViewCList = controller.fetchCustomerList();
        laCLExpandable = new CustomerListExpandableListview(CustomerList2Activity.this, CLViewCList);
        lvCLDetail.setAdapter(laCLExpandable);

        ivCLCancel.setVisibility(View.GONE);

        etCLSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {


            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                if (arg0.length() == 0){

                    ivCLCancel.setVisibility(View.GONE);

                    CLViewCList = controller.fetchCustomerList();
                    laCLExpandable = new CustomerListExpandableListview(CustomerList2Activity.this, CLViewCList);
                    lvCLDetail.setAdapter(laCLExpandable);
                }else{

                    ivCLCancel.setVisibility(View.VISIBLE);

                    lvCLDetail.invalidateViews();
                    controller.PSCName = etCLSearch.getText().toString();
                    CLViewCList = controller.searchCustomerList();
                    laCLExpandable = new CustomerListExpandableListview(CustomerList2Activity.this, CLViewCList);
                    lvCLDetail.setAdapter(laCLExpandable);

                }
            }

        });

        ivCLCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                etCLSearch.getText().clear();
            }
        });

        lvCLDetail.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            public void onGroupExpand(int groupPosition_e) {
                if (PPosition != -1 && groupPosition_e != PPosition) {
                    lvCLDetail.collapseGroup(PPosition);
                }
                PPosition = groupPosition_e;

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etCLSearch.getWindowToken(), 0);

            }
        });


        menu.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.mc_add:

                                if (controller.fetchISSDR() == 2){
                                    Toasty.info(CustomerList2Activity.this,"please time in first", Toast.LENGTH_LONG).show();
                                }else{
                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                        return false;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();

                               /* Intent intentNewCustomerActivity = new Intent(CustomerList2Activity.this, NewCustomerActivity.class);
                                startActivity(intentNewCustomerActivity);*/

                                    Intent intentNewCustomerActivity2 = new Intent(CustomerList2Activity.this, NewCustomerActivity2.class);
                                    intentNewCustomerActivity2.putExtra("isAdd", "1");
                                    startActivity(intentNewCustomerActivity2);
                                }

                                break;

                        }
                        return true;
                    }
                });
}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == request_checkin){
            if (resultCode == RESULT_OK) {
                DialogCustomerSelecttransaction(this);
            }
        }

    }

    public void onBackPressed() {

        controller.PCNm = 0;

        Intent IntentMainActivity = new Intent(CustomerList2Activity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }

}
