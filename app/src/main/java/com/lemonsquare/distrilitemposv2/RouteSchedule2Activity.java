package com.lemonsquare.distrilitemposv2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import static com.lemonsquare.distrilitemposv2.CustomerListExpandableListview.DialogCustomerSelecttransaction;
import static com.lemonsquare.distrilitemposv2.CustomerListExpandableListview.request_checkin;


public class RouteSchedule2Activity extends Activity {

    DBController controller = new DBController(this);
    ExpandableListView RSDetails;
    EditText etRSSearch;
    int PPosition = -1;
    RouteScheduleExpandableListview laRSExpandable;
    List<HashMap<String, String>> RSViewRSchedule;
    static final int READ_BLOCK_SIZE = 100;

    ImageView ivRSCancel;

    String routeschedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routeschedule2);

        RSDetails = (ExpandableListView) findViewById(R.id.lvRSDetail);
        etRSSearch = (EditText) findViewById(R.id.etRSSearch);
        ivRSCancel = (ImageView) findViewById(R.id.ivRSCancel);

        controller.PIsCustomer = 1;


        readrouteschedule();
        routesched();

        RSViewRSchedule = controller.fetchRouteSchedule();
        laRSExpandable = new RouteScheduleExpandableListview(RouteSchedule2Activity.this,RSViewRSchedule);
        RSDetails.setAdapter(laRSExpandable);

        controller.dbLReturns = 0.00;
        controller.PPayment = 0;
        controller.dbGAmt = 0.00;
        controller.dbNSales = 0.00;
        controller.dbCGiven = 0.00;
        controller.PDiscAmt = 0.00;
        controller.PDiscount = 0.00;

        ivRSCancel.setVisibility(View.GONE);

        etRSSearch.addTextChangedListener(new TextWatcher() {

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

                    ivRSCancel.setVisibility(View.GONE);

                    RSViewRSchedule = controller.fetchRouteSchedule();
                    laRSExpandable = new RouteScheduleExpandableListview(RouteSchedule2Activity.this,RSViewRSchedule);
                    RSDetails.setAdapter(laRSExpandable);
                }else{

                    ivRSCancel.setVisibility(View.VISIBLE);

                    RSDetails.invalidateViews();
                    controller.PSRSchedule = etRSSearch.getText().toString();
                    RSViewRSchedule = controller.searchRouteSchedule();
                    laRSExpandable = new RouteScheduleExpandableListview(RouteSchedule2Activity.this,RSViewRSchedule);
                    RSDetails.setAdapter(laRSExpandable);

                }
            }

        });



        RSDetails.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            public void onGroupExpand(int groupPosition_e) {
                if (PPosition != -1 && groupPosition_e != PPosition) {
                    RSDetails.collapseGroup(PPosition);
                }
                PPosition = groupPosition_e;

                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etRSSearch.getWindowToken(), 0);
            }
        });


        ivRSCancel.setOnClickListener(new OnSingleClickListener() {
            public void onSingleClick(View v) {
                etRSSearch.getText().clear();
            }
        });

    }



    void routesched(){
        String day = "";
        controller.days.clear();
        for (int i = 0;i<routeschedule.length();i++){
            if(routeschedule.charAt(i) == ','){
                controller.days.add(day);
                day = "";
            }else{
                day = day + routeschedule.charAt(i);
            }
        }
        controller.days.add(day);
    }

    void readrouteschedule() {

        try {
            FileInputStream fileIn = openFileInput("routeschedule.txt");
            InputStreamReader InputRead = new InputStreamReader(fileIn);

            char[] inputBuffer = new char[READ_BLOCK_SIZE];
            String s = "";
            int charRead;

            while ((charRead = InputRead.read(inputBuffer)) > 0) {
                // char to string conversion
                String readstring = String.copyValueOf(inputBuffer, 0, charRead);
                s += readstring;
            }
            InputRead.close();
            routeschedule = s;


        } catch (Exception e) {
            routeschedule = "";
        }
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

        Intent IntentMainActivity = new Intent(RouteSchedule2Activity.this, MainActivity.class);
        startActivity(IntentMainActivity);
        finish();
    }
}
