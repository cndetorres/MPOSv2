package com.lemonsquare.distrilitemposv2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;

import com.lemonsquare.distrilitemposv2.Model.ItemList;
import com.lemonsquare.distrilitemposv2.Model.SettingsList;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class DBController extends SQLiteOpenHelper {

    private static final String databasename = "mpos";
    private static final int versioncode = 1;

    public static String PPlist,PCCode,PIType,PCName,PMNumber,PUName,PPassword,PCLName,PIiD,PTName,PNCCode,PMName,PCHKName,PCHKPassword,PUiD,PICName,PSTiD,
            PSCName,PINVNumber,PSRSchedule,PMatName,PSTSLoc,PCADBank,PVROdometer,PVVehicleNo,PRType,PTerms,PBillDt,dbPCoins,RICCode,PDCode,SIDCode,imports,exports,transact,videos,productinfo,PInvoiceNo,PRemarks;
    public static int PUType,Prscl,PIndicator,PCashExist;
    public static int PSTSLocNum = 0;
    public static int PVRNum = 0;
    public static int PINum = 0;
    public static int PCNm = 0;
    public  static int PRItem = 0 ;
    public  static int PPayment = 0 ;
    public static int PIsSOrder = 0;
    public static int PIsWlk = 0;
    public static int Pposition = 0;
    public static double PValidatedTotal;
    public static double PCashTotal;
    public static double dbTotalAmt = 0.00;
    public static double dbAmtDue = 0.00;
    public static double dbAmtPd = 0.00;
    public static double dbBalance = 0.00;
    public static double dbLReturns = 0.00;
    public static double dbNSales = 0.00;
    public static double dbCGiven = 0.00;
    public static double dbGAmt = 0.00;
    public static int PLVposition = 0;
    public static double Plong = 0;
    public static double Plat = 0;
    public static double PLimit = 0.00;
    public static double PDiscount = 0.00;
    public static double PDiscAmt = 0.00;
    public static double lesslimit = 0.00;
    public static double TotalShortage = 0.00;
    public static byte[] bArray;
    public static int PIsCustomer = 0;
    public static String PCashierName = "";
    public static String PDefaultPricelist = "";

    final Calendar myCalendar = Calendar.getInstance();



    public static ArrayList<String> alSTItems = new ArrayList<String>();
    public static ArrayList<String> alSTQty = new ArrayList<String>();
    public static ArrayList<String> alSTUnit = new ArrayList<String>();
    public static ArrayList<String> days = new ArrayList<String>();
    public static ArrayList<String> alCheckNumber = new ArrayList<String>();

    public static List<HashMap<String, String>> VRValidateReturns;
    public static List<HashMap<String, String>> IViewInventory;
    public static List<HashMap<String, String>> CDViewCDeposits;
    public static List<HashMap<String, String>> CASCViewChecks;
    public static List<HashMap<String, String>> CASVUCViewVariance;
    public static List<HashMap<String, String>> RIViewRItems;
    public static List<HashMap<String, String>> hmPDDetails;
    public static ArrayList<HashMap<String, String>> hmSODetails;
    public static ArrayList<HashMap<String, String>> hmCOHDetails;
    public static ArrayList<HashMap<String, String>> hmCASSDetails;
    public static ArrayList<HashMap<String, String>> hmCASVCVDetail;

    public static File backupDB;


    DecimalFormat ARAmt = new DecimalFormat("######0.00");


    DateFormat defaultDateFormat = new SimpleDateFormat("yyMMdd");
    DateFormat defaultDateFormat2 = new SimpleDateFormat("yyyyMMdd");
    Calendar defaultDate = Calendar.getInstance();
    String todayDate = defaultDateFormat.format(defaultDate.getTime());
    String todayDate2 = defaultDateFormat2.format(defaultDate.getTime());


    public DBController(Context context) {
        super(context, databasename, null, versioncode);

    }

    @Override
    public void onCreate(SQLiteDatabase database) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old,
                          int current_version) {

    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            db.disableWriteAheadLogging();
        }

    }


    //ArrayList for Settings
    public ArrayList<String> fetchdbSettings(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT CASE Status WHEN 1 THEN 'For Salesman' WHEN 2 THEN 'Ended Transaction' WHEN 3 THEN 'Done with PID. For Recon' WHEN 4 THEN 'For Upload' ELSE 'For Download' END," +
                "TerminalID,ServerAddress,DatabaseName,DatabaseUsername,DatabasePassword,SalesDistrict," +
                "Sloc,Plant,PaymentTermDays,ContactNumberHeader,ContactNumberOrders,ContactNumberCustomerService,OfficeAddress,DefaultPricelist," +
                "MovingAverageBuffer,MinimumAmount,LastOdometer,SMSGatewayNo,CompanyName,Website FROM Settings", null);
        cursor.moveToFirst();
        ArrayList<String> alSettings = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alSettings.add(cursor.getString(0));
            alSettings.add(cursor.getString(1));
            alSettings.add(cursor.getString(2));
            alSettings.add(cursor.getString(3));
            alSettings.add(cursor.getString(4));
            alSettings.add(cursor.getString(5));
            alSettings.add(cursor.getString(6));
            alSettings.add(cursor.getString(7));
            alSettings.add(cursor.getString(8));
            alSettings.add(cursor.getString(9));
            alSettings.add(cursor.getString(10));
            alSettings.add(cursor.getString(11));
            alSettings.add(cursor.getString(12));
            alSettings.add(cursor.getString(13));
            alSettings.add(cursor.getString(14));
            alSettings.add(cursor.getString(15));
            alSettings.add(cursor.getString(16));
            alSettings.add(cursor.getString(17));
            alSettings.add(cursor.getString(18));
            alSettings.add(cursor.getString(19));
            alSettings.add(cursor.getString(20));
            cursor.moveToNext();
        }
        cursor.close();
        return alSettings;
    }

    //DB Update For Settings
    public void updateSettings(String ColumnName,String ColumnValue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ColumnName, ColumnValue);
        db.update("Settings", cv, null, null);
        db.close();
    }

    //Display Data In Inventory Acceptance
    public ArrayList<HashMap<String, String>> fetchInventoryAcceptance() {
        ArrayList<HashMap<String, String>> ftchIAcceptance;
        ftchIAcceptance = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.Name,B.Unit,A.Qty,A.QtyInTransit,A.QtyInTransit + A.Qty " +
                "FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode ORDER BY A.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("Unit", cursor.getString(1));
                map.put("Prev", cursor.getString(2));
                map.put("New", cursor.getString(3));
                map.put("Total", cursor.getString(4));
                ftchIAcceptance.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIAcceptance;
    }

    //Display Data In RouteSchedule
    public ArrayList<HashMap<String, String>> fetchRouteSchedule() {
        ArrayList<HashMap<String, String>> ftchRSchedule;
        ftchRSchedule = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustomerCode,A.CustomerName,B.RouteDate ,A.SAPStreet, A.SAPCity,A.CreditLimit,A.PaymentTerms  " +
                "FROM Customer A INNER JOIN RouteSchedule B ON A.CustomerCode = B.CustomerCode WHERE A.CustomerCode NOT IN(SELECT CustomerCode FROM Customer WHERE SUBSTR(CustomerCode,1,2) ='CD' AND SUBSTR(CustomerCode,5,4) = '"+ fetchSDstSettings()+"' ) ORDER BY B.RouteDate,B.RouteArrangement";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();

                final long date = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd");
                final String dateString = sdf.format(date);

                Date RSDate = new Date(cursor.getLong(2));
                SimpleDateFormat RSWkDay = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());


                DateFormat format = new SimpleDateFormat("EEE, MMM dd");
                Calendar calendar = Calendar.getInstance();
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                String[] daysofweek = new String[6];
                for (int i = 0; i < 6; i++)
                {
                    daysofweek[i] = format.format(calendar.getTime());
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                List<String> weekofdays = Arrays.asList(daysofweek);

                if (days.contains(RSWkDay.format(RSDate).substring(0, 3)) || dateString.substring(0,3).equals(RSWkDay.format(RSDate).substring(0,3))){
                    if (weekofdays.contains(RSWkDay.format(RSDate))){

                        if (cursor.getString(0).equals("CAS" + fetchdbSettings().get(6))){
                            if(!fetchcountnwcust().equals("0")) {
                                map.put("CustomerCode", cursor.getString(0));
                                map.put("Customer", cursor.getString(1));
                                map.put("Schedule", "Scheduled: " + RSWkDay.format(RSDate));
                                map.put("Address", cursor.getString(3) + " , " + cursor.getString(4));
                                map.put("Limit", cursor.getString(5));
                                map.put("Terms", cursor.getString(6));
                                ftchRSchedule.add(map);
                            }
                        }else{
                            map.put("CustomerCode", cursor.getString(0));
                            map.put("Customer", cursor.getString(1));
                            map.put("Schedule", "Scheduled: " + RSWkDay.format(RSDate));
                            map.put("Address", cursor.getString(3) + " , " + cursor.getString(4));
                            map.put("Limit", cursor.getString(5));
                            map.put("Terms", cursor.getString(6));
                            ftchRSchedule.add(map);
                        }



                    }
                }


                /*for (int a= 0;a < days.size();a++) {
                    if (days.get(a).equals(RSWkDay.format(RSDate).substring(0, 3))) {

                        if (weekofdays.contains(RSWkDay.format(RSDate))){
                            map.put("CustomerCode", cursor.getString(0));
                            map.put("Customer", cursor.getString(1));
                            map.put("Schedule", "Scheduled: " + RSWkDay.format(RSDate));
                            map.put("Address", cursor.getString(3) + " , " + cursor.getString(3));
                            ftchRSchedule.add(map);
                        }
                    }
                }*/

            } while (cursor.moveToNext());
        }
        return ftchRSchedule;
    }

    //Display Data In RouteSchedule
    public ArrayList<HashMap<String, String>> searchRouteSchedule() {
        ArrayList<HashMap<String, String>> ftchRSchedule;
        ftchRSchedule = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustomerCode,A.CustomerName,B.RouteDate ,A.Street, A.City,A.CreditLimit,A.PaymentTerms  " +
                "FROM Customer A INNER JOIN RouteSchedule B ON A.CustomerCode = B.CustomerCode WHERE A.CustomerCode NOT IN (SELECT CustomerCode FROM Customer WHERE SUBSTR(CustomerCode,1,2) ='CD' AND SUBSTR(CustomerCode,5,4) = '"+ fetchSDstSettings()+"' )AND A.CustomerName LIKE '%" + PSRSchedule + "%' ORDER BY B.RouteDate,B.RouteArrangement";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();

                final long date = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd");
                final String dateString = sdf.format(date);

                Date RSDate = new Date(cursor.getLong(2));
                SimpleDateFormat RSWkDay = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());

                DateFormat format = new SimpleDateFormat("EEE, MMM dd");
                Calendar calendar = Calendar.getInstance();
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

                String[] daysofweek = new String[6];
                for (int i = 0; i < 6; i++)
                {
                    daysofweek[i] = format.format(calendar.getTime());
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }

                List<String> weekofdays = Arrays.asList(daysofweek);

                if (days.contains(RSWkDay.format(RSDate).substring(0, 3)) || dateString.substring(0,3).equals(RSWkDay.format(RSDate).substring(0,3))){
                    if (weekofdays.contains(RSWkDay.format(RSDate))){

                        if (cursor.getString(0).equals("CAS" + fetchdbSettings().get(6))){
                            if(!fetchcountnwcust().equals("0")) {
                                map.put("CustomerCode", cursor.getString(0));
                                map.put("Customer", cursor.getString(1));
                                map.put("Schedule", "Scheduled: " + RSWkDay.format(RSDate));
                                map.put("Address", cursor.getString(3) + " , " + cursor.getString(4));
                                map.put("Limit", cursor.getString(5));
                                map.put("Terms", cursor.getString(6));
                                ftchRSchedule.add(map);
                            }
                        }else{
                            map.put("CustomerCode", cursor.getString(0));
                            map.put("Customer", cursor.getString(1));
                            map.put("Schedule", "Scheduled: " + RSWkDay.format(RSDate));
                            map.put("Address", cursor.getString(3) + " , " + cursor.getString(4));
                            map.put("Limit", cursor.getString(5));
                            map.put("Terms", cursor.getString(6));
                            ftchRSchedule.add(map);
                        }

                    }
                }


                /*for (int a= 0;a < days.size();a++){
                    if (days.get(a).equals(RSWkDay.format(RSDate).substring(0,3)) || dateString.equals(RSWkDay.format(RSDate).substring(0,3))){
                        map.put("CustomerCode", cursor.getString(0));
                        map.put("Customer", cursor.getString(1));
                        map.put("Schedule", "Scheduled: " + RSWkDay.format(RSDate));
                        map.put("Address",  cursor.getString(3) + " , " + cursor.getString(3));
                        ftchRSchedule.add(map);
                    }
                }*/


            } while (cursor.moveToNext());
        }
        return ftchRSchedule;
    }

    //Display Data In Customer List
    public ArrayList<HashMap<String, String>> fetchCustomerList() {
        ArrayList<HashMap<String, String>> ftchCList;
        ftchCList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustomerCode,A.CustomerName,A.CreditLimit,A.PaymentTerms  FROM Customer A WHERE A.PaymentTerms <> 'CAS' AND A.CustomerCode NOT IN(SELECT CustomerCode FROM Customer WHERE SUBSTR(CustomerCode,1,2) ='CD' AND SUBSTR(CustomerCode,5,4) = '"+ fetchSDstSettings()+"' ) ORDER BY A.CustomerName";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();


                if (StringUtils.substringAfter(cursor.getString(1), "-").equals("CASH SALES")){
                    if(!fetchcountnwcust().equals("0")) {
                        map.put("CustomerCode", cursor.getString(0));
                        map.put("Customer", cursor.getString(1));
                        String CLLimit;
                        if (cursor.getString(2) == null) {
                            CLLimit = "";
                        } else {
                            CLLimit = cursor.getString(2);
                        }

                        map.put("Limit", "Limit: " + CLLimit);
                        map.put("Terms", "Terms:" + cursor.getString(3));
                        ftchCList.add(map);
                    }
                }else{
                    map.put("CustomerCode", cursor.getString(0));
                    map.put("Customer", cursor.getString(1));
                    String CLLimit;
                    if (cursor.getString(2) == null ){
                        CLLimit = "";
                    }else{
                        CLLimit = cursor.getString(2);
                    }

                    map.put("Limit", "Limit: " + CLLimit);
                    map.put("Terms",  "Terms:" + cursor.getString(3));
                    ftchCList.add(map);
                }

            } while (cursor.moveToNext());
        }
        return ftchCList;
    }


    //Search Data In Customer List
    public ArrayList<HashMap<String, String>> searchCustomerList() {
        ArrayList<HashMap<String, String>> ftchCList;
        ftchCList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustomerCode,A.CustomerName,A.CreditLimit,A.PaymentTerms  FROM Customer A WHERE A.PaymentTerms <> 'CAS' AND A.CustomerCode NOT IN (SELECT CustomerCode FROM Customer WHERE SUBSTR(CustomerCode,1,2) ='CD' AND SUBSTR(CustomerCode,5,4) = '"+ fetchSDstSettings()+"' ) AND A.CustomerName LIKE '%" + PSCName + "%' ORDER BY A.CustomerName";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();
                if (StringUtils.substringAfter(cursor.getString(1), "-").equals("CASH SALES")){
                    if(!fetchcountnwcust().equals("0")) {
                        map.put("CustomerCode", cursor.getString(0));
                        map.put("Customer", cursor.getString(1));
                        String CLLimit;
                        if (cursor.getString(2) == null) {
                            CLLimit = "";
                        } else {
                            CLLimit = cursor.getString(2);
                        }

                        map.put("Limit", "Limit: " + CLLimit);
                        map.put("Terms", "Terms:" + cursor.getString(3));
                        ftchCList.add(map);
                    }
                }else{
                    map.put("CustomerCode", cursor.getString(0));
                    map.put("Customer", cursor.getString(1));
                    String CLLimit;
                    if (cursor.getString(2) == null ){
                        CLLimit = "";
                    }else{
                        CLLimit = cursor.getString(2);
                    }

                    map.put("Limit", "Limit: " + CLLimit);
                    map.put("Terms",  "Terms:" + cursor.getString(3));
                    ftchCList.add(map);
                }

            } while (cursor.moveToNext());
        }
        return ftchCList;
    }

    //Display Data In Inventory List
    public ArrayList<HashMap<String, String>> fetchInventoryList() {
        ArrayList<HashMap<String, String>> ftchIList;
        ftchIList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.Name,A.Qty + A.QtyInTransit + A.ReturnQty - A.SoldQty - A.TransferredQty + A.ReceivedQty,B.Unit " +
                "FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode ORDER BY A.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                map.put("Unit", cursor.getString(2));
                ftchIList.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIList;
    }

    public ArrayList<HashMap<String, String>> fetchInventoryList(String Matname) {
        ArrayList<HashMap<String, String>> ftchIList;
        ftchIList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.Name,A.Qty + A.QtyInTransit + A.ReturnQty - A.SoldQty - A.TransferredQty + A.ReceivedQty,B.Unit " +
                "FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode WHERE B.Name LIKE '%" + Matname + "%' ORDER BY A.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                map.put("Unit", cursor.getString(2));
                ftchIList.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIList;
    }

    //Display Data In View Returns
    public ArrayList<HashMap<String, String>> fetchViewReturns() {
        ArrayList<HashMap<String, String>> ftchVReturns;
        ftchVReturns = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.RetType, B.Name, A.Qty, B.Unit,D.CustomerName,A.Remarks FROM ReturnItem A " +
                "INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode INNER JOIN Returns C ON A.RetId = C.RetId " +
                "INNER JOIN Customer D ON C.CustomerCode = D.CustomerCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Type", cursor.getString(0));
                map.put("Item", cursor.getString(1));
                map.put("Qty", cursor.getString(2) + ' ' + cursor.getString(3));
                map.put("Customer", cursor.getString(4));
                map.put("Remarks", cursor.getString(5));
                ftchVReturns.add(map);
            } while (cursor.moveToNext());
        }
        return ftchVReturns;
    }

    //Display Data In Transactions For The Day
    public ArrayList<HashMap<String, String>> fetchTransactionsfortheDay() {
        ArrayList<HashMap<String, String>> ftchTDay;
        ftchTDay = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.ID,A.Category,A.CustomerName,A.Date,A.Amount FROM Transactions A ORDER BY A.Date ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID", cursor.getString(0));
                map.put("Type", cursor.getString(1));
                map.put("Customer", cursor.getString(2));
                Date TDDate = new Date(cursor.getLong(3));
                SimpleDateFormat TDTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                map.put("Time", TDTime.format(TDDate));
                DecimalFormat TDAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", TDAmt.format(cursor.getDouble(4)));
                ftchTDay.add(map);
            } while (cursor.moveToNext());
        }
        return ftchTDay;
    }

    public ArrayList<HashMap<String, String>> fetchTransactionsfortheDay(String ID) {
        ArrayList<HashMap<String, String>> ftchTDay;
        ftchTDay = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.ID,A.Category,A.CustomerName,A.Date,A.Amount FROM Transactions A WHERE A.ID = '" + ID + "' ORDER BY A.Date ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID", cursor.getString(0));
                map.put("Type", cursor.getString(1));
                map.put("Customer", cursor.getString(2));
                Date TDDate = new Date(cursor.getLong(3));
                SimpleDateFormat TDTime = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault());
                map.put("Time", TDTime.format(TDDate));
                DecimalFormat TDAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", TDAmt.format(cursor.getDouble(4)));
                ftchTDay.add(map);
            } while (cursor.moveToNext());
        }
        return ftchTDay;
    }

    //Display Data In Accounts Receivable
    public ArrayList<HashMap<String, String>> fetchAccountsReceivable() {
        ArrayList<HashMap<String, String>> ftchAReceivable;
        ftchAReceivable = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.SalesID,A.BillingDate,A.Amount,A.Payment,A.Balance FROM ARBalances A WHERE  A.CustomerCode = '" + PCCode + "' ORDER BY A.BillingDate";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("SID", cursor.getString(0));
                Date ARDate = new Date(cursor.getLong(1));
                SimpleDateFormat ARWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                map.put("BillDt", ARWkDate.format(ARDate));
                DecimalFormat ARAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", ARAmt.format(cursor.getDouble(2)));
                map.put("Payment", ARAmt.format(cursor.getDouble(3)));
                map.put("Balance", ARAmt.format(cursor.getDouble(4)));
                ftchAReceivable.add(map);
            } while (cursor.moveToNext());
        }
        return ftchAReceivable;
    }

    //Display Data SUM Amount In Accounts Receivable
    public double fetchSUMARBalances() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Balance) FROM ARBalances A  WHERE  A.CustomerCode = '" + PCCode + "'" , null);
        cursor.moveToFirst();

        Double fetchSUMARBalances = Double.valueOf(ARAmt.format(cursor.getDouble(0)));
        return  fetchSUMARBalances;
    }

    public double fetchCreditExpo() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CreditExposure FROM Customer A  WHERE  A.CustomerCode = '" + PCCode + "'" , null);
        cursor.moveToFirst();

        Double fetchCreditExpo = Double.valueOf(ARAmt.format(cursor.getDouble(0)));
        return  fetchCreditExpo;
    }

    public int fetchcountchecks(String CheckNumber) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Checks WHERE CheckNumber = '" + CheckNumber + "'" , null);
        cursor.moveToFirst();
        int countchecks = cursor.getInt(0);
        return countchecks ;
    }

    public int fetchId(String Id) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM OdometerReading WHERE Id = '" + Id + "'" , null);
        cursor.moveToFirst();
        int countid = cursor.getInt(0);
        return countid;
    }

    public void updateCreditExposure(Double CreditExpo){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE Customer  SET CreditExposure =  " + CreditExpo + " WHERE  CustomerCode = '" + PCCode + "'");
        db.close();

    }

    public double fetchSUMChecks() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.TotalCheckPayments - A.AmountPayableExcess) FROM PaymentSummary A  WHERE  A.CustomerCode = '" + PCCode + "' AND A.TransactionType = 'AR'" , null);
        cursor.moveToFirst();

        Double ftchSUMChecks = Double.valueOf(ARAmt.format(cursor.getDouble(0)));
        return  ftchSUMChecks;
    }
    public double fetchSUMAmtARBalances() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Balance) FROM ARBalances A  WHERE  A.CustomerCode = '" + PCCode + "'" , null);
        cursor.moveToFirst();
        Double fetchSUMARBalances = Double.valueOf(ARAmt.format(cursor.getDouble(0)));
        return  fetchSUMARBalances;
    }

    public double fetchSUMAmtARBalancesCOD() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Balance) FROM ARBalances A  WHERE  A.CustomerCode = '" + PCCode + "' AND A.Balance < 0" , null);
        cursor.moveToFirst();
        Double fetchSUMARBalances = Double.valueOf(ARAmt.format(cursor.getDouble(0)));
        return  fetchSUMARBalances;
    }

    public int fetchCountARBalances() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Balance) FROM ARBalances A  WHERE  A.CustomerCode = '" + PCCode + "'" , null);
        cursor.moveToFirst();
        int fetchSUMARBalances = cursor.getInt(0);
        return  fetchSUMARBalances;
    }

    //Display Data In Price List
    public ArrayList<HashMap<String, String>> fetchPriceList() {
        ArrayList<HashMap<String, String>> ftchPList;
        ftchPList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.Name,A.PriceList,A.Amount,B.Unit FROM PricingList A " +
                "INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode WHERE A.PriceList = '" + PPlist + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("List", cursor.getString(1));
                DecimalFormat PLAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", PLAmt.format(cursor.getDouble(2)));
                map.put("Unit", cursor.getString(3));
                ftchPList.add(map);
            } while (cursor.moveToNext());
        }
        return ftchPList;
    }

    //Fetch List Data Default In Price List
    public String fetchDefaultPListList() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.DefaultPricelist FROM Settings A", null);
        cursor.moveToFirst();
        String fetchPLList = cursor.getString(0);
        return  fetchPLList;
    }

    //Fetch List Data Distinct In Price List
    public String[] fetchDistinctPListList() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.PriceList FROM PricingList A GROUP BY A.PriceList", null);
        cursor.moveToFirst();
        ArrayList<String> alPLList = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alPLList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alPLList.toArray(new String[alPLList.size()]);
    }

    //ArrayList for Customer Information
    public ArrayList<String> fetchCustomerInfo(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerName,A.ContactPerson,A.CustomerCode,A.CusType,A.CreditLimit,A.CreditExposure,A.VisitDays," +
                "A.ContactNumber,A.MobileNumber,A.Street, A.City,A.Region, A.Postal," +
                "A.StreetH,A.CityH,A.RegionH,A.PostalH, A.Remarks FROM Customer A WHERE A.CustomerCode = '" + PCCode + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alCInfo = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alCInfo.add(cursor.getString(0));
            alCInfo.add(cursor.getString(1));
            alCInfo.add(cursor.getString(2));
            alCInfo.add(cursor.getString(3));
            alCInfo.add(cursor.getString(4));
            DecimalFormat FRAmt = new DecimalFormat("#,##0.00");
            alCInfo.add( FRAmt.format(cursor.getDouble(5)));
            alCInfo.add(cursor.getString(6));
            alCInfo.add(cursor.getString(7));
            alCInfo.add(cursor.getString(8));
            alCInfo.add(cursor.getString(9) + "," + cursor.getString(10) + ", " + cursor.getString(11) + ", " + cursor.getString(12));
            alCInfo.add(cursor.getString(13) + "," + cursor.getString(14) + ", " + cursor.getString(15) + ", " + cursor.getString(16));
            alCInfo.add(cursor.getString(17));
            cursor.moveToNext();
        }
        cursor.close();
        return alCInfo;
    }

    //Update For CustomerInfo
    public void updateCustomerInfo(String ColumnName,String ColumnValue,int status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ColumnName, ColumnValue);
        cv.put("Status",status);
        db.update("Customer", cv, "CustomerCode = '" + PCCode + "'", null);
        db.close();
    }

    //Fetch Data For Customer Info - BusinessAddress
    public ArrayList<String > fetchCustomerInfoBAddress(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Street, A.City,A.Region, A.Postal FROM Customer A WHERE A.CustomerCode = '" + PCCode + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alCInfoBAddress = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alCInfoBAddress.add(cursor.getString(0));
            alCInfoBAddress.add(cursor.getString(1));
            alCInfoBAddress.add(cursor.getString(2));
            alCInfoBAddress.add(cursor.getString(3));
            cursor.moveToNext();
        }
        cursor.close();
        return alCInfoBAddress;
    }

    //Fetch Data For Customer Info - HomeAddress
    public ArrayList<String > fetchCustomerInfoHAddress(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.StreetH,A.CityH,A.RegionH,A.PostalH FROM Customer A WHERE A.CustomerCode = '" + PCCode + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alCInfoHAddress = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alCInfoHAddress.add(cursor.getString(0));
            alCInfoHAddress.add(cursor.getString(1));
            alCInfoHAddress.add(cursor.getString(2));
            alCInfoHAddress.add(cursor.getString(3));
            cursor.moveToNext();
        }
        cursor.close();
        return alCInfoHAddress;
    }

    //Fetch List Data IncidentyType - Name In Incident
    public String[] fetchITypeIncidentTypeName() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.IncidentType,A.Name FROM IncidentType A ORDER BY A._id", null);
        cursor.moveToFirst();
        ArrayList<String> alITypeIncidentTypeName = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alITypeIncidentTypeName.add(cursor.getString(0) + "-" + cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return alITypeIncidentTypeName.toArray(new String[alITypeIncidentTypeName.size()]);
    }

    //Fetch Data _id For IncidentType
    public String fetchIDIType() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A._id FROM IncidentType A WHERE A.IncidentType = '" + PIType + "'" , null);
        cursor.moveToFirst();
        String fetchITid = cursor.getString(0);
        return  fetchITid;
    }

       //Insert Data For Incident Report
    public void insertIncidentReport(String IncidentID,String IncidentTypeId,Long IncidentDate, String Details,String ReportedBy,String Reference) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("IncidentID", IncidentID);
        cv.put("IncidentTypeId", IncidentTypeId);
        cv.put("CustomerCode", " ");
        cv.put("IncidentDate", IncidentDate);
        cv.put("Details", Details);
        cv.put("ReportedBy", ReportedBy);
        cv.put("Reference", Reference);
        cv.put("Status", 0);
        db.insert("IncidentReport", null, cv);
        db.close();
    }

    //Fetch Data For Incident Report
    public ArrayList<HashMap<String, String>> fetchIncidentReport() {
        ArrayList<HashMap<String, String>> ftchIReport;
        ftchIReport = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.IncidentID,B.IncidentType,B.Name,A.Details FROM IncidentReport A " +
                "INNER JOIN IncidentType B ON A.IncidentTypeId = B._id";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Id", cursor.getString(0));
                map.put("Incident Name", cursor.getString(1) + "-" + cursor.getString(2));
                map.put("Details", cursor.getString(3));
                ftchIReport.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIReport;
    }

    //Fetch Max Number For TableSequence - IncidentType
    public String fetchMaxNumTSequence() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.AUTOINC_NEXT FROM TableSequence A WHERE A.COLUMN_NAME ='" + PCName + "'", null);
        cursor.moveToFirst();
        String maxTSequence = cursor.getString(0);
        return maxTSequence;
    }

    public String fetchMaxNumTCTSequence() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.AUTOINC_NEXT FROM TableSequence A WHERE A.COLUMN_NAME ='" + PCName + "' AND A.TABLE_NAME = '" + PTName +"'", null);
        cursor.moveToFirst();
        String maxTSequence = cursor.getString(0);
        return maxTSequence;
    }

    public String fetchMaxNumTCTSequence(String Columnname,String Tablename) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.AUTOINC_NEXT FROM TableSequence A WHERE A.COLUMN_NAME ='" + Columnname + "' AND A.TABLE_NAME = '" + Tablename +"'", null);
        cursor.moveToFirst();
        String maxTSequence = cursor.getString(0);
        return maxTSequence;
    }

    //Update For TableSequence
    public void updateTableSequence(int ColumnValue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AUTOINC_NEXT", ColumnValue);
        db.update("TableSequence", cv, "COLUMN_NAME = '" + PCName + "'", null);
        db.close();
    }

    public void updateTCTableSequence(int ColumnValue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AUTOINC_NEXT", ColumnValue);
        db.update("TableSequence", cv, "COLUMN_NAME = '" + PCName + "' AND TABLE_NAME = '" + PTName + "'", null);
        db.close();
    }

    public void updateTCTableSequence(String Columnname,String Tablename,int ColumnValue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AUTOINC_NEXT", ColumnValue);
        db.update("TableSequence", cv, "COLUMN_NAME = '" + Columnname + "' AND TABLE_NAME = '" + Tablename + "'", null);
        db.close();
    }

    //Fetch SalesDistrict For Settings
    public String fetchSDstSettings() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.SalesDistrict FROM Settings A", null);
        cursor.moveToFirst();
        String ftchSDst = cursor.getString(0);
        return ftchSDst;
    }

    //Update For Users
    public void updateUsers(int ColumnValue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Status", ColumnValue);
        db.update("Users", cv, "Username = '" + PUName + "'", null);
        db.close();
    }

    //Fetch Count Status For Users
    public String fetchCountStatusUsers() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(Status) FROM Users WHERE Status = 1", null);
        cursor.moveToFirst();
        String countSUsers = cursor.getString(0);
        return countSUsers;
    }

    public void updateStatus() {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Status", 0);
        db.update("Users", cv, null, null);
        db.close();
    }



    //Fetch Active Data For Users
    public ArrayList<String> fetchRIDNmUsers(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT RoleId,Name,Username,UserID,[Password] FROM Users WHERE Status = 1" , null);
        cursor.moveToFirst();
        ArrayList<String> ftchRIDNmUsers = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            ftchRIDNmUsers.add(cursor.getString(0));
            ftchRIDNmUsers.add(cursor.getString(1));
            ftchRIDNmUsers.add(cursor.getString(2));
            ftchRIDNmUsers.add(cursor.getString(3));
            ftchRIDNmUsers.add(cursor.getString(4));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchRIDNmUsers;
    }

    //Fetch Validated For Users
    public String validateUser() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(A.Username) FROM Users A WHERE A.Username ='" + PUName + "' AND A.Password='" + PPassword + "'", null);
        cursor.moveToFirst();
        String countUsers = cursor.getString(0);
        return countUsers;
    }

    //Fetch Role For Users
    public ArrayList<String> FetchRoleUser(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.RoleId FROM Users A WHERE A.Username ='" + PUName + "' AND A.Password='" + PPassword + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchRoleUser = new ArrayList<String>();

        if (cursor.isAfterLast()){
            alftchRoleUser.add("");
            cursor.moveToNext();

        }else{
            while(!cursor.isAfterLast()) {
                alftchRoleUser.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return alftchRoleUser;
    }
    public ArrayList<String> FetchSalesDistrict(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.SalesDistrict FROM Users A WHERE A.Username ='" + PUName + "' AND A.Password='" + PPassword + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchSalesDistrict = new ArrayList<String>();

        if (cursor.isAfterLast()){
            alftchSalesDistrict.add("");
            cursor.moveToNext();

        }else{
            while(!cursor.isAfterLast()) {
                alftchSalesDistrict.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return alftchSalesDistrict;
    }

    //Fetch Customer Code For Customers
    /*public String fetchCCodeCustomers() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerCode FROM Customer A WHERE REPLACE(A.CustomerName,'''','') = '" + PCLName + "'", null);
        cursor.moveToFirst();
        String ftchCCode = cursor.getString(0);
        return ftchCCode;
    }*/

    //Fetch Incident Report Info For Incident Report
    public ArrayList<String> fetchIRInfo(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.IncidentDate,B.Name,A.ReportedBy,A.Details,A.Reference FROM IncidentReport A " +
                "INNER JOIN IncidentType B ON A.IncidentTypeId = B._id WHERE A.IncidentID = '" + PIiD + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> ftchIRInfo = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            Date IRDate = new Date(cursor.getLong(0));
            SimpleDateFormat IRDTime = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault());
            ftchIRInfo.add(IRDTime.format(IRDate));
            ftchIRInfo.add(cursor.getString(1));
            ftchIRInfo.add(cursor.getString(2));
            ftchIRInfo.add(cursor.getString(3));
            ftchIRInfo.add(cursor.getString(4));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchIRInfo;
    }



    //Display Data In Fuel Reports
    public ArrayList<HashMap<String, String>> fetchFuelReports() {
        ArrayList<HashMap<String, String>> ftchFReports;
        ftchFReports = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.FuelReportId,A.GasStation,A.OdometerReading,A.Liters,A.RefillDate,A.Amount FROM FuelReport A";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID", cursor.getString(0));
                map.put("Station", cursor.getString(1));
                map.put("Odometer",  cursor.getString(2));
                map.put("Liters",  cursor.getString(3));
                Date FRDate = new Date(cursor.getLong(4));
                SimpleDateFormat FRTime = new SimpleDateFormat(" hh:mm a", Locale.getDefault());
                map.put("Time", FRTime.format(FRDate));
                DecimalFormat FRAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", FRAmt.format(cursor.getDouble(5)));
                ftchFReports.add(map);
            } while (cursor.moveToNext());
        }
        return ftchFReports;
    }
    public ArrayList<HashMap<String, String>> fetchFuelReports(String FuelReportId) {
        ArrayList<HashMap<String, String>> ftchFReports;
        ftchFReports = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.RefillDate,A.GasStation,A.Amount,A.Liters,A.OdometerReading,A.PONum,A.InvoiceNum FROM FuelReport A WHERE A.FuelReportId = '" + FuelReportId + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();

                Date FRDate = new Date(cursor.getLong(0));
                SimpleDateFormat FRTime = new SimpleDateFormat("MM/dd/yy hh:mm a", Locale.getDefault());
                map.put("Time", FRTime.format(FRDate));
                map.put("Station", cursor.getString(1));
                DecimalFormat FRAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", FRAmt.format(cursor.getDouble(2)));
                map.put("Liters",  cursor.getString(3));
                map.put("Odometer",  cursor.getString(4));
                map.put("PONum",  cursor.getString(5));
                map.put("InvoiceNum",  cursor.getString(6));
                ftchFReports.add(map);
            } while (cursor.moveToNext());
        }
        return ftchFReports;
    }

    //Insert Data For Fuel Report
    public void insertFuelReport(String FuelReportId,Long RefillDate , String Liters,String Amount,String GasStation,String OdometerReading,String PONum,String Vehicle,String UserId,String InvoiceNum ) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("FuelReportId", FuelReportId);
        cv.put("RefillDate ", RefillDate );
        cv.put("Liters", Liters);
        cv.put("Amount", Amount);
        cv.put("GasStation ", GasStation);
        cv.put("OdometerReading", OdometerReading);
        cv.put("PONum", PONum);
        cv.put("Vehicle", Vehicle);
        cv.put("UserId",UserId);
        cv.put("Status", 0);
        cv.put("InvoiceNum",InvoiceNum);
        db.insert("FuelReport", null, cv);
        db.close();
    }

    //Fetch UserId-VehicleNo In OdometerReading
    public ArrayList<String> fetchUiDVNoOReading(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.UserId,A.VehicleNo,A.OdoReading FROM OdometerReading A WHERE _id = 2" , null);
        cursor.moveToFirst();
        ArrayList<String> ftchUiDVNoOReading = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            ftchUiDVNoOReading .add(cursor.getString(0));
            ftchUiDVNoOReading .add(cursor.getString(1));
            ftchUiDVNoOReading .add(cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchUiDVNoOReading ;
    }

    //Fetch CusType-Description In Customer Type
    public String[] fetchCusTypeDescCustomerType() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CusType,A.Description FROM CustomerType A WHERE A.CusType NOT IN ('CSW') ORDER BY A._id", null);
        cursor.moveToFirst();
        ArrayList<String> alCusTypeDescCustomerType = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alCusTypeDescCustomerType.add(cursor.getString(0) + "-" + cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return alCusTypeDescCustomerType.toArray(new String[alCusTypeDescCustomerType.size()]);
    }

    //Insert Data For Customer
    public void insertCustomer(String CustomerCode,String CustomerName , String ContactPerson,String ContactNumber,String VisitDays,String PriceList,String SalesDistrict,
                               String SAPCity,String SAPStreet,String UnitNo,String Street,String Subdv,String Barangay,String City,String Province,String Postal,String Region,
                               String CusType,String MobileNumber,String UnitNoH,String StreetH,String SubdvH,String BarangayH,String CityH,String ProvinceH,
                               String SAPStreetH,String PostalH,String SAPCityH,String RegionH,String Id,String PaymentTermDays,String Remarks,String Location,byte[] Attachment,String Freq,String OrderDay,String DeliveyDay,String DelWTimeFrom,String DelWTimeTo) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("CustomerName", CustomerName);
        cv.put("ContactPerson", ContactPerson);
        cv.put("ContactNumber", ContactNumber);
        cv.put("VisitDays", VisitDays);
        cv.put("PriceList", PriceList);
        cv.put("PaymentTerms", "");
        cv.put("SalesDistrict", SalesDistrict);
        cv.put("SAPCity", SAPCity);
        cv.put("SAPStreet", SAPStreet);
        cv.put("UnitNo", UnitNo);
        cv.put("Street", Street);
        cv.put("Subdv", Subdv);
        cv.put("Barangay", Barangay);
        cv.put("City", City);
        cv.put("Province", Province);
        cv.put("Postal",Postal);
        cv.put("Region",Region);
        cv.put("CusType", CusType);
        cv.put("MobileNumber",MobileNumber);
        cv.put("UnitNoH", UnitNoH);
        cv.put("StreetH", StreetH);
        cv.put("SubdvH", SubdvH);
        cv.put("BarangayH", BarangayH);
        cv.put("CityH", CityH);
        cv.put("ProvinceH", ProvinceH);
        cv.put("SAPStreetH",SAPStreetH);
        cv.put("PostalH",PostalH);
        cv.put("SAPCityH",SAPCityH);
        cv.put("RegionH",RegionH);
        cv.put("Id",Id);
        cv.put("PaymentTermDays",PaymentTermDays);
        cv.put("Status",6);
        cv.put("Remarks",Remarks);
        cv.put("PaymentTerms","COD");
        cv.put("CreditLimit","0.00");
        cv.put("CreditExposure","0.00");
        cv.put("Discount","0.00");
        cv.put("TaxClass","0.00");
        cv.put("Location",Location);
        cv.put("Attachment", Attachment);
        cv.put("Freq", Freq);
        cv.put("OrderDay", OrderDay);
        cv.put("DelDay", DeliveyDay);
        cv.put("DelWTimeFrom", DelWTimeFrom);
        cv.put("DelWTimeTo", DelWTimeTo);
        db.insert("Customer", null, cv);
        db.close();
    }



    //Fetch New Customer In Customer
    public ArrayList<String> fetchNewCustomer(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerCode,A.CustomerName FROM Customer A WHERE SUBSTR(A.CustomerCode,1,2) = 'NW'", null);
        cursor.moveToFirst();
        ArrayList<String> alftchNewCustomer = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alftchNewCustomer.add(cursor.getString(0) + "-" + cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchNewCustomer;
    }

    //Fetch Material Name In Materials
    public String[] fetchMNameMaterials() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM Materials A WHERE A.MaterialCode NOT IN (SELECT A.MatCode FROM PromoD A INNER JOIN PromoH B ON A.PromoID = B.PromoID WHERE B.PromoType = 'FREE')", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchCINameMaterials() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM Materials A", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchMNameMaterialsNoPromo() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM Materials A", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }


    public String[] fetchMNameMaterialsPacks() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM Materials A INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode WHERE A.Unit = 'PAK' AND B.PriceList = '" + fetchDefaultPListList() + "' AND SUBSTR(A.MaterialCode,1,7) <> 'FG-4005' AND (A.ExtMatGrp <> ' ' AND A.ExtMatGrp <> '')" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    //Fetch Unit-Qty-Price In Material
    public ArrayList<String> fetchUnitQtyPriceMaterial (){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Unit, C.Qty + C.QtyInTransit + C.ReturnQty - C.SoldQty - C.TransferredQty + C.ReceivedQty, B.Amount FROM Materials A " +
                "INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode LEFT JOIN Inventory C ON A.MaterialCode = C.MaterialCode WHERE A.Name ='" + PMName + "' AND B.PriceList ='" + PDefaultPricelist + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alftchQtyPriceMaterial = new ArrayList<String>();

            while(!cursor.isAfterLast()) {

                alftchQtyPriceMaterial.add(cursor.getString(0));

                String strQty;
                if (cursor.getString(1) == null ){
                    strQty = "0";
                }else{
                    strQty = cursor.getString(1);
                }

                alftchQtyPriceMaterial.add(strQty);
                DecimalFormat MAmt = new DecimalFormat("#,##0.00");
                alftchQtyPriceMaterial.add(MAmt.format(cursor.getDouble(2)));
                cursor.moveToNext();

        }

        cursor.close();
        return alftchQtyPriceMaterial;
    }



    public ArrayList<String> fetchUnitQtyPriceMaterialwithDisc (){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Unit, C.Qty + C.QtyInTransit + C.ReturnQty - C.SoldQty - C.TransferredQty + C.ReceivedQty, B.Amount-D.Disc FROM Materials A " +
                "INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode LEFT JOIN Inventory C ON A.MaterialCode = C.MaterialCode INNER JOIN PromoD D ON A.MaterialCode = D.MatCode WHERE A.Name ='" + PMName + "' AND B.PriceList ='" + PDefaultPricelist + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alftchQtyPriceMaterial = new ArrayList<String>();

        while(!cursor.isAfterLast()) {

            alftchQtyPriceMaterial.add(cursor.getString(0));

            String strQty;
            if (cursor.getString(1) == null ){
                strQty = "0";
            }else{
                strQty = cursor.getString(1);
            }

            alftchQtyPriceMaterial.add(strQty);
            DecimalFormat MAmt = new DecimalFormat("#,##0.00");
            alftchQtyPriceMaterial.add(MAmt.format(cursor.getDouble(2)));
            cursor.moveToNext();

        }

        cursor.close();
        return alftchQtyPriceMaterial;
    }

    public ArrayList<String> fetchUnitQtyPriceMaterialInventory(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Unit, C.Qty + C.QtyInTransit + C.ReturnQty - C.SoldQty - C.TransferredQty + C.ReceivedQty, B.Amount FROM Materials A " +
                "INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode LEFT JOIN Inventory C ON A.MaterialCode = C.MaterialCode WHERE A.Name ='" + PMName + "' AND B.PriceList ='" + fetchDefaultPListList()+ "'", null);
        cursor.moveToFirst();
        ArrayList<String> alftchQtyPriceMaterial = new ArrayList<String>();

        while(!cursor.isAfterLast()) {

            alftchQtyPriceMaterial.add(cursor.getString(0));

            String strQty;
            if (cursor.getString(1) == null ){
                strQty = "0";
            }else{
                strQty = cursor.getString(1);
            }

            alftchQtyPriceMaterial.add(strQty);
            DecimalFormat MAmt = new DecimalFormat("#,##0.00");
            alftchQtyPriceMaterial.add(MAmt.format(cursor.getDouble(2)));
            cursor.moveToNext();

        }

        cursor.close();
        return alftchQtyPriceMaterial;
    }

    //Insert Data For Stock Receiving Header
    public void insertStockReceivingH(String StockReceivingId,String Id , Long TransferDate,String Sloc,double Amount,String CheckerUserId,String AgentUserId) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("StockReceivingId", StockReceivingId);
        cv.put("Id", Id);
        cv.put("TransferDate", TransferDate);
        cv.put("Sloc", Sloc);
        cv.put("Status", 0);
        cv.put("Amount", Amount);
        cv.put("CheckerUserId", CheckerUserId);
        cv.put("AgentUserId", AgentUserId);
        db.insert("StockReceiving", null, cv);
        db.close();
    }

    //Insert Data For Stock Receiving Detail
    public void insertStockReceivingD(String StockReceivingId,String Item , String MaterialCode,String Qty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("StockReceivingId", StockReceivingId);
        cv.put("Item", Item);
        cv.put("MaterialCode", MaterialCode);
        cv.put("Qty", Qty);
        cv.put("Status", 0);
        db.insert("StockReceivingItem", null, cv);
        db.close();
    }

    //Fetch CheckerID In User
    public ArrayList<String> fetchChkIDUser(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.UserID,A.RoleID FROM Users A WHERE A.[Username] = '" + PCHKName + "' AND A.[Password] = '" + PCHKPassword + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchChkIDUser = new ArrayList<String>();

        if (cursor.isAfterLast()){
            String strUserID,strCheckRID;
            strUserID = "0";
            strCheckRID = " ";
            alftchChkIDUser.add(strUserID);
            alftchChkIDUser.add(strCheckRID);
            cursor.moveToNext();

        }else{
            while(!cursor.isAfterLast()) {

                String strUserID,strCheckRID;
                if (cursor.getString(0) == null){
                    strUserID = "0";
                }else{
                    strUserID = cursor.getString(0);
                }

                if (cursor.getString(1) == null){
                    strCheckRID = " ";
                }else{
                    strCheckRID = cursor.getString(1);
                }

                alftchChkIDUser.add(strUserID);
                alftchChkIDUser.add(strCheckRID);
                cursor.moveToNext();

            }
        }
        cursor.close();
        return alftchChkIDUser;
    }

    //Fetch Material Code In Materials
    public String fetchMCodeMaterials() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.MaterialCode FROM Materials A WHERE A.Name = '" + PMName + "'", null);

        cursor.moveToFirst();
        String ftchMCodeMaterials = cursor.getString(0);
        return  ftchMCodeMaterials;
    }

    //Fetch Count Material Code In Inventory
    public int fetchCountMaterialInventory() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(A.MaterialCode) FROM Inventory A WHERE A.MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        cursor.moveToFirst();
        int ftchCountMaterialInventory = cursor.getInt(0);
        return  ftchCountMaterialInventory;

    }

    //Fetch Count Material Code In Inventory
    public String fetchMaxNumInvtTSequence() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.AUTOINC_NEXT FROM TableSequence A WHERE A.COLUMN_NAME ='" + PICName + "'", null);
        cursor.moveToFirst();
        String maxTSequence = cursor.getString(0);
        return maxTSequence;
    }

    public void updateInvtTableSequence(int ColumnValue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AUTOINC_NEXT", ColumnValue);
        db.update("TableSequence", cv, "COLUMN_NAME = '" + PICName + "'", null);
        db.close();
    }

    //Insert Data For Stock Receiving Detail
    public void insertInventory(String MaterialCode,String Sloc,String InvId,String ReceivedQty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("MaterialCode", MaterialCode);
        cv.put("Qty", 0);
        cv.put("QtyInTransit", 0);
        cv.put("Sloc", Sloc);
        cv.put("EndingQty", 0);
        cv.put("ReturnQty", 0);
        cv.put("InvId", InvId);
        cv.put("SoldQty", 0);
        cv.put("TransferredQty", 0);
        cv.put("ReceivedQty", ReceivedQty);
        db.insert("Inventory", null, cv);
        db.close();
    }

    //FETCH Data For Stock Receiving Detail
    public int fetcReceivedQtyInventory() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.ReceivedQty FROM Inventory A WHERE A.MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        cursor.moveToFirst();
        int ftcReceivedQtyInventory = cursor.getInt(0);
        return  ftcReceivedQtyInventory ;

    }

    public void updateReceivedInventory(int Qty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ReceivedQty", fetcReceivedQtyInventory() + Qty);
        db.update("Inventory", cv, "MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        db.close();
    }

    public String[] fetchSTMaterials() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM Materials A INNER JOIN Inventory B ON A.MaterialCode = B.MaterialCode WHERE B.Qty + B.QtyInTransit + B.ReturnQty - B.SoldQty - B.TransferredQty + B.ReceivedQty > 0 AND A.MaterialCode NOT IN (SELECT A.MatCode FROM PromoD A INNER JOIN PromoH B ON A.PromoID = B.PromoID WHERE B.PromoType = 'FREE')", null);
        cursor.moveToFirst();
        ArrayList<String> alftchSTMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchSTMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchSTMaterials.toArray(new String[alftchSTMaterials.size()]);
    }



    //Insert Data For Stock Transfer Header
    public void insertStockTransferH(String StockReceivingId,Long TransferDate,String Id,byte[] Signature , String SenderUserId,String ReceiverSloc,String SenderSloc,String ReceiverName,Double Amount) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("StockTransferId", StockReceivingId);
        cv.put("TransferDate", TransferDate);
        cv.put("Status", 0);
        cv.put("Id", Id);
        cv.put("Signature", Signature);
        cv.put("SenderUserId", SenderUserId);
        cv.put("ReceiverSloc", ReceiverSloc);
        cv.put("SenderSloc", SenderSloc);
        cv.put("ReceiverName", ReceiverName);
        cv.put("Amount", Amount);
        db.insert("StockTransfer", null, cv);
        db.close();
    }

    //Insert Data For Stock Transfer Detail
    public void insertStockTransferD(String StockTransferId , String MaterialCode,String Qty,String Item) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("StockTransferId", StockTransferId);
        cv.put("MaterialCode", MaterialCode);
        cv.put("Qty", Qty);
        cv.put("Status", 0);
        cv.put("Item", Item);
        db.insert("StockTransferItem", null, cv);
        db.close();
    }

    //FETCH Data For Stock Receiving Detail
    public int fetcTransferredQtyInventory() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.TransferredQty FROM Inventory A WHERE A.MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        cursor.moveToFirst();
        int ftcReceivedQtyInventory = cursor.getInt(0);
        return  ftcReceivedQtyInventory ;

    }

    public void updateTransferredQtyInventory(int Qty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("TransferredQty", fetcTransferredQtyInventory() + Qty);
        db.update("Inventory", cv, "MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        db.close();
    }

    //Display Data In Cash Deposited
    public ArrayList<HashMap<String, String>> fetchCashDeposited() {
        ArrayList<HashMap<String, String>> ftchCashDeposited;
        ftchCashDeposited = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.Bank,A.AcctNo,A.BranchCode,A.Amt,A.DateDeposited FROM CashDeposited A";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Bank", cursor.getString(0));
                map.put("AcctNo", cursor.getString(1));
                map.put("Branch",  cursor.getString(2));
                DecimalFormat FRAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", FRAmt.format(cursor.getDouble(3)));
                map.put("DateDeposited", String.valueOf(cursor.getLong(4)));
                ftchCashDeposited.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCashDeposited;
    }

    public ArrayList<HashMap<String, String>> fetchCashDepositedCashier() {
        ArrayList<HashMap<String, String>> ftchCashDeposited;
        ftchCashDeposited = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.Bank,A.AcctNo,A.BranchCode,A.Amt,A.DateDeposited FROM CashDepositedCashier A";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Bank", cursor.getString(0));
                map.put("AcctNo", cursor.getString(1));
                map.put("Branch",  cursor.getString(2));
                DecimalFormat FRAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", FRAmt.format(cursor.getDouble(3)));
                map.put("DateDeposited", String.valueOf(cursor.getLong(4)));
                ftchCashDeposited.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCashDeposited;
    }

    //Display Data In Cash Deposits
    public ArrayList<HashMap<String, String>> fetchCashDeposits() {
        ArrayList<HashMap<String, String>> ftchCashDeposited;
        ftchCashDeposited = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A._id,A.Bank,A.AcctNo,A.BranchCode,A.Amt,A.DateDeposited FROM CashDeposited A";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID", cursor.getString(0));
                map.put("Bank", cursor.getString(1));
                map.put("AcctNo", cursor.getString(2));
                map.put("Branch",  cursor.getString(3));
                DecimalFormat FRAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", FRAmt.format(cursor.getDouble(4)));
                map.put("DateDeposited", String.valueOf(cursor.getLong(5)));
                ftchCashDeposited.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCashDeposited;
    }

    //Display Data SUM Amount In Cash Deposited
    public double fetchSUMCashDeposited() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Amt) FROM CashDeposited A ", null);
        cursor.moveToFirst();
        Double fetchSUMCashDeposited = cursor.getDouble(0);
        return  fetchSUMCashDeposited;
    }

    public double fetchSUMCashDepositedSDR() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Amt,A.DateDeposited FROM CashDeposited A ", null);

        Double fetchSUMCashDeposited = 0.00;
        int rowcount = 0;
        rowcount = cursor.getCount();

        if (rowcount > 0) {
            cursor.moveToFirst();

            for(int i = 0;i <rowcount;i++){
                cursor.moveToPosition(i);
                Date Date = new Date(cursor.getLong(1));
                SimpleDateFormat sdfTodayDate = new SimpleDateFormat("yyMMdd", Locale.getDefault());
                if (sdfTodayDate.format(Date).equals(todayDate)){
                    fetchSUMCashDeposited = fetchSUMCashDeposited + cursor.getDouble(0);
                }
            }

        }



        return  fetchSUMCashDeposited;
    }

    //Display Data SUM Amount In Transactions
    public double fetchSUMPaymentItem() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A WHERE A.PaymentTypeId = '0001'", null);
        cursor.moveToFirst();
        Double ftchSUMPaymentItem = cursor.getDouble(0);
        return  ftchSUMPaymentItem;
    }

    public double fetchSUMPaymentItemSDR() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A WHERE A.PaymentTypeId = '0001' AND SUBSTR(PaymentId,1,6) = '" + todayDate + "'", null);
        cursor.moveToFirst();
        Double ftchSUMPaymentItem = cursor.getDouble(0);
        return  ftchSUMPaymentItem;
    }

    public double fetchSUMChecksSDR() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A WHERE A.PaymentTypeId = '0002' AND SUBSTR(PaymentId,1,6) = '" + todayDate + "'", null);
        cursor.moveToFirst();
        Double ftchSUMPaymentItem = cursor.getDouble(0);
        return  ftchSUMPaymentItem;
    }

    public int fetchCountCalls() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(DISTINCT CustomerCode) FROM Sales WHERE SUBSTR(SalesId,1,6) = '" + todayDate + "'", null);
        cursor.moveToFirst();

        int ftchCountCalls;
        if (cursor.getCount() > 0){
            ftchCountCalls = cursor.getInt(0);
        }else{
            ftchCountCalls = 0;
        }

        return  ftchCountCalls;
    }

    public int fetchCountChecks() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(A.AmtPaid) FROM PaymentItem A WHERE A.PaymentTypeId = '0002' AND SUBSTR(PaymentId,1,6) = '" + todayDate + "'", null);
        cursor.moveToFirst();

        int ftchCountCalls;
        if (cursor.getCount() > 0){
            ftchCountCalls = cursor.getInt(0);
        }else{
            ftchCountCalls = 0;
        }

        return  ftchCountCalls;
    }


    //Fetch Name in Material
    public ArrayList<String> FetchNameMaterials(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM Materials A WHERE A.MatEanUpc ='" + PMatName + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchNMaterials = new ArrayList<String>();

        if (cursor.isAfterLast()){
            alftchNMaterials.add("");
            cursor.moveToNext();

        }else{
            while(!cursor.isAfterLast()) {
                alftchNMaterials.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return alftchNMaterials;
    }


    //Fetch Name-Unit-Qty-Price In Material
    public ArrayList<String> fetchNameUnitQtyPriceMaterial(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name,A.Unit,B.Qty + B.QtyInTransit + B.ReturnQty - B.SoldQty - B.TransferredQty + B.ReceivedQty,C.Amount FROM Materials A LEFT JOIN Inventory B ON A.MaterialCode = B.MaterialCode " +
                "LEFT JOIN PricingList C ON B.MaterialCode = C.MaterialCode WHERE A.MatEanUpc ='" + PMName + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchNMaterials = new ArrayList<String>();

        if (cursor.isAfterLast()){
            alftchNMaterials.add("");
            cursor.moveToNext();

        }else{
            while(!cursor.isAfterLast()) {
                alftchNMaterials.add(cursor.getString(0));
                alftchNMaterials.add(cursor.getString(1));
                alftchNMaterials.add(cursor.getString(2));
                DecimalFormat MAmt = new DecimalFormat("#,##0.00");
                alftchNMaterials.add(MAmt.format(cursor.getDouble(3)));
                alftchNMaterials.add(cursor.getString(3));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return alftchNMaterials;
    }

    //Fetch Amount-Material In PricingList
    public double fetchAmtPricingList() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT Amount FROM PricingList WHERE MaterialCode ='" + fetchMCodeMaterials() + "' AND PriceList ='" + fetchDPListSettings() + "'" , null);
        cursor.moveToFirst();
        double countSLocUsers = cursor.getDouble(0);
        return countSLocUsers;
    }

    //Insert Data In Transaction
    public void insertTransaction(String ID,String Category,String CustomerName,String CustomerCode , Long Date,Double Amount) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ID", ID);
        cv.put("Category", Category);
        cv.put("CustomerName", CustomerName);
        cv.put("CustomerCode", CustomerCode);
        cv.put("Date", Date);
        cv.put("Status", 0);
        cv.put("Amount", Amount);
        db.insert("Transactions", null, cv);
        db.close();
    }

    //Fetch Amount-Material In PricingList
    public String fetchDPListSettings() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT DefaultPricelist FROM Settings" , null);
        cursor.moveToFirst();
        String ftchDPListSettings = cursor.getString(0);
        return ftchDPListSettings;
    }

    //Fetch List Bank In Bank Accounts
    public String[] fetchBankBAccounts() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Bank FROM BankAccounts A", null);
        cursor.moveToFirst();
        ArrayList<String> alBAccounts= new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alBAccounts.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alBAccounts.toArray(new String[alBAccounts.size()]);
    }

    //Fetch Bank Account In BankAccounts
    public ArrayList<String> fetchBABankAccounts(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.BankAccount,A.BranchCode FROM BankAccounts A WHERE Bank = '" + PCADBank + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alftchBABankAccounts = new ArrayList<String>();

            while(!cursor.isAfterLast()) {
                alftchBABankAccounts.add(cursor.getString(0));
                alftchBABankAccounts.add(cursor.getString(1));
                cursor.moveToNext();
        }
        cursor.close();
        return alftchBABankAccounts;
    }

    //Display Data In View Returns
    public ArrayList<HashMap<String, String>> fetchValidateReturns() {
        ArrayList<HashMap<String, String>> ftchVReturns;
        ftchVReturns = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A._id,A.RetType, B.Name, A.Qty, B.Unit,D.CustomerName,A.Remarks FROM ReturnItem A " +
                "INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode INNER JOIN Returns C ON A.RetId = C.RetId " +
                "INNER JOIN Customer D ON C.CustomerCode = D.CustomerCode WHERE A.RetType <> 'RE'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("_id", cursor.getString(0));
                map.put("Type", cursor.getString(1));
                map.put("Item", cursor.getString(2));
                map.put("Qty", cursor.getString(3) + ' ' + cursor.getString(4));
                map.put("Customer", cursor.getString(5));
                map.put("Remarks", cursor.getString(6));
                ftchVReturns.add(map);
            } while (cursor.moveToNext());
        }
        return ftchVReturns;
    }

    //Display Data In Inventory
    public ArrayList<HashMap<String, String>> fetchInventory() {
        ArrayList<HashMap<String, String>> ftchIList;
        ftchIList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.Name,0,B.Unit " +
                "FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode ORDER BY A.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                map.put("Unit", cursor.getString(2));
                ftchIList.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIList;
    }

    //Fetch Material Name In Inventory
    public String[] fetchMNameInventory() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.Name FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode ORDER BY A.MaterialCode", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameInventory = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameInventory.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameInventory.toArray(new String[alftchMNameInventory.size()]);
    }

    //Fetch Bank Account In BankAccounts
    public ArrayList<String> fetchCustRTOdometer(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerCode,A.ReadTime,UserId FROM OdometerReading A WHERE _id = 2", null);
        cursor.moveToFirst();
        ArrayList<String> alftchCustRTOdometer = new ArrayList<String>();

        while(!cursor.isAfterLast()) {
            alftchCustRTOdometer.add(cursor.getString(0));
            Date ORDate = new Date(cursor.getLong(1));
            SimpleDateFormat ORWkDate = new SimpleDateFormat("MM/dd/yy hh:mm:ss a", Locale.getDefault());
            alftchCustRTOdometer.add(ORWkDate.format(ORDate));
            alftchCustRTOdometer.add(cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchCustRTOdometer;
    }

    //Insert Data In PhysicalInventory
    public void insertPhysicalInventory(String AgentId,String CheckerId,String AcceptDate, String CheckDate,byte[] Signature,String Id,String PIID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AgentId", AgentId);
        cv.put("CheckerId", CheckerId);
        cv.put("AcceptDate", AcceptDate);
        cv.put("CheckDate", CheckDate);
        cv.put("Signature", Signature);
        cv.put("Id", Id);
        cv.put("PIID", PIID);
        cv.put("Status", 0);
        db.insert("PhysicalInventory", null, cv);
        db.close();
    }

    //Update In PhysicalInventory
    public void updatePhysicalInventory(String AgentId,String CheckerId,String AcceptDate, String CheckDate,byte[] Signature,String Id,String PIID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AgentId", AgentId);
        cv.put("CheckerId", CheckerId);
        cv.put("AcceptDate", AcceptDate);
        cv.put("CheckDate", CheckDate);
        cv.put("Signature", Signature);
        cv.put("Id", Id);
        cv.put("PIID", PIID);
        cv.put("Status", 0);
        db.update("PhysicalInventory", cv, null, null);
        db.close();
    }

    //Fetch Count Status For Users
    public String fetchCountPhysicalInventory() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM PhysicalInventory", null);
        cursor.moveToFirst();
        String countfetchCountPhysicalInventory = cursor.getString(0);
        return countfetchCountPhysicalInventory;
    }

    //Fetch Return Variance In ReturnItem
    public ArrayList<HashMap<String, String>> fetchReturnVariance() {
        ArrayList<HashMap<String, String>> ftchRVariance;
        ftchRVariance = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.Name,A.ValidatedQty - A.Qty,B.Unit,C.CustomerCode,A.RetType,A.Qty,A.ValidatedQty,A.Remarks FROM ReturnItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode INNER JOIN Returns C ON A.RetId = C.RetId ORDER BY A.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                map.put("Unit", cursor.getString(2));
                map.put("CustCode", cursor.getString(3));
                map.put("RetType", cursor.getString(4));
                map.put("Sys", cursor.getString(5));
                map.put("Val", cursor.getString(6));
                map.put("Remarks", cursor.getString(7));
                ftchRVariance.add(map);
            } while (cursor.moveToNext());
        }
        return ftchRVariance;
    }

    //Fetch Return Variance In ReturnItem
    public ArrayList<HashMap<String, String>> fetchInventoryVariance() {
        ArrayList<HashMap<String, String>> ftchRVariance;
        ftchRVariance = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.Name,(A.EndingQty) - (A.Qty + A.QtyInTransit + A.ReturnQty + A.ReceivedQty - A.SoldQty- A.TransferredQty),B.Unit,(A.Qty + A.QtyInTransit + A.ReturnQty + A.ReceivedQty - A.SoldQty- A.TransferredQty),A.EndingQty FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode ORDER BY A.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                map.put("Unit", cursor.getString(2));
                map.put("Sys", cursor.getString(3));
                map.put("Val", cursor.getString(4));
                ftchRVariance.add(map);
            } while (cursor.moveToNext());
        }
        return ftchRVariance;
    }

    //Update For Settings
    public void updateSettings(int ColumnValue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Status", ColumnValue);
        db.update("Settings", cv, null, null);
        db.close();
    }


    //Update Ending Qty In Inventory
    public void updateEndingQtyInventory(int Qty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("EndingQty", Qty);
        db.update("Inventory", cv, "MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        db.close();
    }

    //Update ValidatedQty In ReturnItem
    public void updateValidatedQtyRItem(int Qty,String RetID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ValidatedQty", Qty);
        db.update("ReturnItem", cv, "MaterialCode = '" + fetchMCodeMaterials() + "' AND _id = '" + RetID + "'", null);
        db.close();
    }

    //Delete CashDeposited
    public void deleteCashDeposited() {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.delete("CashDeposited", null, null);
        db.close();
    }


    //Delete CashDeposited
    public void deleteCashDepositedCashier() {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.delete("CashDepositedCashier", null, null);
        db.close();
    }



    //Insert Data In PhysicalInventory
    public void insertCashDeposited(String Bank,String AcctNo,String BranchCode, String Id,String Amt,String DateDeposited,String DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Bank", Bank);
        cv.put("AcctNo", AcctNo);
        cv.put("BranchCode", BranchCode);
        cv.put("Id", Id);
        cv.put("Amt", Amt);
        cv.put("DateDeposited", DateDeposited);
        cv.put("DateChecked", DateChecked);
        db.insert("CashDeposited", null, cv);
        db.close();
    }

    //Insert Data In PhysicalInventory
    public void updateCashDeposited(String Bank,String AcctNo,String BranchCode, String Id,String Amt,String DateDeposited,String DateChecked,String ID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Bank", Bank);
        cv.put("AcctNo", AcctNo);
        cv.put("BranchCode", BranchCode);
        cv.put("Id", Id);
        cv.put("Amt", Amt);
        cv.put("DateDeposited", DateDeposited);
        cv.put("DateChecked", DateChecked);
        db.update("CashDeposited", cv, "_id = '" + ID + "'",null);
        db.close();
    }

    public void updateCashDepositedDate(Long DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("DateChecked", DateChecked);
        db.update("CashDeposited", cv, null,null);
        db.close();
    }

    //Insert Data In PhysicalInventory
    public void updateCashDepositedCashier(String Bank,String AcctNo,String BranchCode, String Id,String Amt,String DateDeposited,String DateChecked,String ID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Bank", Bank);
        cv.put("AcctNo", AcctNo);
        cv.put("BranchCode", BranchCode);
        cv.put("Id", Id);
        cv.put("Amt", Amt);
        cv.put("DateDeposited", DateDeposited);
        cv.put("DateChecked", DateChecked);
        db.update("CashDepositedCashier", cv, "_id = '" + ID + "'",null);
        db.close();

    }

    //Insert Data In PhysicalInventory
    public void insertCashDepositedCashier(String Bank,String AcctNo,String BranchCode, String Id,String Amt,String DateDeposited,Long DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Bank", Bank);
        cv.put("AcctNo", AcctNo);
        cv.put("BranchCode", BranchCode);
        cv.put("Id", Id);
        cv.put("Amt", Amt);
        cv.put("DateDeposited", DateDeposited);
        cv.put("DateChecked", DateChecked);
        db.insert("CashDepositedCashier", null, cv);
        db.close();
    }

    //Fetch Count CashOnHand
    public String fetchCountCashOnHand() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM CashOnHand", null);
        cursor.moveToFirst();
        String countfetchCashOnHand = cursor.getString(0);
        return countfetchCashOnHand;
    }

    //Insert Data In CashOnHand
    public void insertCashOnHand(String AgentId,String CashierId,String M1000,String M500, String M200,String M100,String M50,String M20,String Coins,int CashShortage,String DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AgentId", AgentId);
        cv.put("CashierId", CashierId);
        cv.put("[1000]", M1000);
        cv.put("[500]", M500);
        cv.put("[200]", M200);
        cv.put("[100]", M100);
        cv.put("[50]", M50);
        cv.put("[20]", M20);
        cv.put("Coins", Coins);
        cv.put("CashShortage", CashShortage);
        cv.put("DateChecked", DateChecked);
        db.insert("CashOnHand",  null, cv);
        db.close();

    }

    //Update Data In CashOnHandCashier
    public void updateCashOnHand(String AgentId,String CashierId,String M1000,String M500, String M200,String M100,String M50,String M20,String Coins,int CashShortage,String DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AgentId", AgentId);
        cv.put("CashierId", CashierId);
        cv.put("[1000]", M1000);
        cv.put("[500]", M500);
        cv.put("[200]", M200);
        cv.put("[100]", M100);
        cv.put("[50]", M50);
        cv.put("[20]", M20);
        cv.put("Coins", Coins);
        cv.put("CashShortage", CashShortage);
        cv.put("DateChecked", DateChecked);
        db.update("CashOnHand", cv, null, null);
        db.close();
    }

    public void updateCashOnHandDate(String DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("DateChecked", DateChecked);
        db.update("CashOnHand", cv, null, null);
        db.close();
    }

    //Update Data In CashOnHandCashier without Agent
    public void updateCashOnHandWOAgent(String CashierId,String M1000,String M500, String M200,String M100,String M50,String M20,String Coins,Double CashShortage,String DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CashierId", CashierId);
        cv.put("[1000]", M1000);
        cv.put("[500]", M500);
        cv.put("[200]", M200);
        cv.put("[100]", M100);
        cv.put("[50]", M50);
        cv.put("[20]", M20);
        cv.put("Coins", Coins);
        cv.put("CashShortage", CashShortage);
        cv.put("DateChecked", DateChecked);
        db.update("CashOnHandCashier", cv, null, null);
        db.close();
    }

    public void updateCashOnHandCShortage(Double CashShortage) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CashShortage", CashShortage);
        db.update("CashOnHandCashier", cv, null, null);
        db.close();
    }

    public Double fetchCashShortageCashOnHand() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT CashShortage FROM CashOnHandCashier", null);
        cursor.moveToFirst();
        DecimalFormat CSCOH = new DecimalFormat("###0.00");
        Double countfetchCashOnHand = cursor.getDouble(0);
        return countfetchCashOnHand;
    }

    //Insert Data In CashOnHand
    public void insertCashOnHandCashier(String AgentId,String CashierId,String M1000,String M500, String M200,String M100,String M50,String M20,String Coins,int CashShortage,String DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AgentId", AgentId);
        cv.put("CashierId", CashierId);
        cv.put("[1000]", M1000);
        cv.put("[500]", M500);
        cv.put("[200]", M200);
        cv.put("[100]", M100);
        cv.put("[50]", M50);
        cv.put("[20]", M20);
        cv.put("Coins", Coins);
        cv.put("CashShortage", CashShortage);
        cv.put("DateChecked", DateChecked);
        db.insert("CashOnHandCashier",  null, cv);
        db.close();

    }

    //Update Data In CashOnHand
    public void updateCashOnHandCashier(String AgentId,String CashierId,String M1000,String M500, String M200,String M100,String M50,String M20,String Coins,int CashShortage,String DateChecked) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("AgentId", AgentId);
        cv.put("CashierId", CashierId);
        cv.put("[1000]", M1000);
        cv.put("[500]", M500);
        cv.put("[200]", M200);
        cv.put("[100]", M100);
        cv.put("[50]", M50);
        cv.put("[20]", M20);
        cv.put("Coins", Coins);
        cv.put("CashShortage", CashShortage);
        cv.put("DateChecked", DateChecked);
        db.update("CashOnHandCashier", cv, null, null);
        db.close();
    }

    //Fetch Data in CashOnHand
    public ArrayList<String> fetchCashOnHand(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.[1000],A.[500],A.[200],A.[100],A.[50],A.[20],A.Coins FROM CashOnHand A", null);
        cursor.moveToFirst();
        ArrayList<String> alCashOnHand = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alCashOnHand.add(cursor.getString(0));
            alCashOnHand.add(cursor.getString(1));
            alCashOnHand.add(cursor.getString(2));
            alCashOnHand.add(cursor.getString(3));
            alCashOnHand.add(cursor.getString(4));
            alCashOnHand.add(cursor.getString(5));
            alCashOnHand.add(cursor.getString(6));
            cursor.moveToNext();
        }
        cursor.close();
        return alCashOnHand;
    }

    //Fetch Data in CashOnHand
    public ArrayList<String> fetchCashOnHandCashier(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.[1000],A.[500],A.[200],A.[100],A.[50],A.[20],A.Coins FROM CashOnHandCashier A", null);
        cursor.moveToFirst();
        ArrayList<String> alCashOnHand = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alCashOnHand.add(cursor.getString(0));
            alCashOnHand.add(cursor.getString(1));
            alCashOnHand.add(cursor.getString(2));
            alCashOnHand.add(cursor.getString(3));
            alCashOnHand.add(cursor.getString(4));
            alCashOnHand.add(cursor.getString(5));
            alCashOnHand.add(cursor.getString(6));
            cursor.moveToNext();
        }
        cursor.close();
        return alCashOnHand;
    }


    //Display Data In Inventory Acceptance
    public ArrayList<HashMap<String, String>> fetchChecks() {
        ArrayList<HashMap<String, String>> ftchIAcceptance;
        ftchIAcceptance = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A._id,A.CheckDate,A.Bank,A.CheckNumber,A.Amount,A.Liquidated FROM CHECKS A";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID",cursor.getString(0));
                Date CDate = new Date(cursor.getLong(1));
                SimpleDateFormat CWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                map.put("CheckDt", CWkDate.format(CDate));
                map.put("Bank", cursor.getString(2));
                map.put("CheckNo", cursor.getString(3));
                DecimalFormat CAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", CAmt.format(cursor.getDouble(4)));
                String isLiq;
                if (cursor.getString(5).equals("0")){
                    isLiq = "NO";
                }else{
                    isLiq = "YES";
                }
                map.put("Liq", isLiq);
                ftchIAcceptance.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIAcceptance;
    }

    //Display Data In Inventory Acceptance
    public ArrayList<HashMap<String, String>> fetchChecksOriginal(String _id) {
        ArrayList<HashMap<String, String>> ftchIAcceptance;
        ftchIAcceptance = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A._id,A.CheckDate,A.Bank,A.OriginalCheckNumber,A.OriginalAmount FROM CHECKS A WHERE A._id ='" + _id + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID",cursor.getString(0));
                Date CDate = new Date(cursor.getLong(1));
                SimpleDateFormat CWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                map.put("CheckDt", CWkDate.format(CDate));
                map.put("Bank", cursor.getString(2));
                map.put("CheckNo", cursor.getString(3));
                DecimalFormat CAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", CAmt.format(cursor.getDouble(4)));
                ftchIAcceptance.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIAcceptance;
    }

    public ArrayList<HashMap<String, String>> fetchChecksUnliq() {
        ArrayList<HashMap<String, String>> ftchIAcceptance;
        ftchIAcceptance = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A._id,A.CheckDate,A.Bank,A.CheckNumber,A.Amount,A.Liquidated FROM CHECKS A WHERE A.Liquidated = 0";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {

                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID",cursor.getString(0));
                Date CDate = new Date(cursor.getLong(1));
                SimpleDateFormat CWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                map.put("CheckDt", CWkDate.format(CDate));
                map.put("Bank", cursor.getString(2));
                map.put("CheckNo", cursor.getString(3));
                DecimalFormat CAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", CAmt.format(cursor.getDouble(4)));
                String isLiq;
                if (cursor.getString(5).equals("0")){
                    isLiq = "NO";
                }else{
                    isLiq = "YES";
                }
                map.put("Liq", isLiq);
                ftchIAcceptance.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIAcceptance;
    }

    //Display Data SUM Stock Shortage In Inventory
    public double fetchSUMReturnsShortage()  {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM((A.ValidatedQty - A.Qty)*B.Amount) - (((A.ValidatedQty - A.Qty)*B.Amount)*(D.Discount * -1))" +
                " FROM ReturnItem A INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode INNER JOIN Returns C ON A.RetId = C.RetId INNER JOIN Customer D ON C.CustomerCode = D.CustomerCode  WHERE B.PriceList ='" + fetchDPListSettings() + "' AND (A.ValidatedQty - A.Qty) < 0", null);
        cursor.moveToFirst();
        Double ftchSUMStockShortage = cursor.getDouble(0);
        return  ftchSUMStockShortage;
    }

    //Display Data SUM Stock Shortage In Inventory
    public double fetchSUMStockShortage() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(((A.EndingQty) - (A.Qty + A.QtyInTransit + A.ReturnQty + A.ReceivedQty - A.SoldQty- A.TransferredQty))*B.Amount)" +
                " FROM Inventory A INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode WHERE B.PriceList ='" + fetchDPListSettings() + "' AND A.EndingQty - (A.Qty + A.QtyInTransit + A.ReturnQty + A.ReceivedQty - A.SoldQty- A.TransferredQty) < 0", null);
        cursor.moveToFirst();
        Double ftchSUMStockShortage = cursor.getDouble(0);
        return  ftchSUMStockShortage;
    }

    //Display Data SUM System Generated Amount In PaymentItem
    public double fetchSUMCheckPItem() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A WHERE A.PaymentTypeId = '0002'", null);
        cursor.moveToFirst();
        Double ftchSUMCheckPItem = cursor.getDouble(0);
        return  ftchSUMCheckPItem;
    }

    //Fetch Count Cust In Odometer Reading
    public String fetchCountCustOdometer() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(A.CustomerCode) FROM OdometerReading A WHERE A.CustomerCode = '" + PCCode + "'", null);
        cursor.moveToFirst();
        String ftchCountCustOdometer = cursor.getString(0);
        return  ftchCountCustOdometer;

    }

    //Insert Data For Odometer Reading
    public void insertOdometerReading(String CustomerCode,String OdoReading,Long ReadTime, String Remarks,String UserId,String VehicleNo,String Id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("OdoReading", OdoReading);
        cv.put("ReadTime", ReadTime);
        cv.put("Remarks", Remarks);
        cv.put("UserId", UserId);
        cv.put("VehicleNo", VehicleNo);
        cv.put("Id", Id);
        cv.put("Status", 0);
        db.insert("OdometerReading", null, cv);
        db.close();
    }

    //Fetch ReturnType
    public String[] fetchNameRetTypeReturnType() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.RetType,A.Name FROM ReturnType A GROUP BY A._id", null);
        cursor.moveToFirst();
        ArrayList<String> alPLList = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alPLList.add(cursor.getString(0) + "-" + cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return alPLList.toArray(new String[alPLList.size()]);
    }

    //Fetch Count Odometer Reading
    public String fetchCountOdometerReading() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM OdometerReading A WHERE _id = 2", null);
        cursor.moveToFirst();
        String ftchCountCustOdometer = cursor.getString(0);
        return  ftchCountCustOdometer;

    }

    //Fetch Count Odometer Reading
    public String fetchCountVehicleNo() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Vehicle A WHERE VehicleNo = '" + PVVehicleNo + "'", null);
        cursor.moveToFirst();
        String ftchCountCustOdometer = cursor.getString(0);
        return  ftchCountCustOdometer;

    }

    //Display Data In Cash Deposited
    public ArrayList<HashMap<String, String>> fetchNull() {
        ArrayList<HashMap<String, String>> ftchNull;
        ftchNull = new ArrayList<HashMap<String, String>>();
        return ftchNull;
    }

    public void insertReturns(String CustomerCode,String ReturnDate,byte[] Signature, String Id,String RetId,String Retby,String TotalAmount) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("ReturnDate", ReturnDate);
        cv.put("Signature", Signature);
        cv.put("Id", Id);
        cv.put("RetId", RetId);
        cv.put("Status", 0);
        cv.put("Retby", Retby);
        cv.put("TotalAmount", TotalAmount);
        db.insert("Returns", null, cv);
        db.close();
    }

    public void insertReturnItem(String RetId,String MaterialCode,int Qty,int ValidatedQty,Float Total,String RetType,String Remarks,int Item) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("RetId", RetId);
        cv.put("MaterialCode", MaterialCode);
        cv.put("Qty", Qty);
        cv.put("ValidatedQty", ValidatedQty);
        cv.put("Total", Total);
        cv.put("RetType", RetType);
        cv.put("Status", 0);
        cv.put("Remarks", Remarks);
        cv.put("Item", Item);
        db.insert("ReturnItem", null, cv);
        db.close();
    }

    //Insert Data For Stock Receiving Detail
    public void insertReturnInventory(String MaterialCode,String Sloc,String InvId,String ReturnQty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("MaterialCode", MaterialCode);
        cv.put("Qty", 0);
        cv.put("QtyInTransit", 0);
        cv.put("Sloc", Sloc);
        cv.put("EndingQty", 0);
        cv.put("ReturnQty", ReturnQty);
        cv.put("InvId", InvId);
        cv.put("SoldQty", 0);
        cv.put("TransferredQty", 0);
        cv.put("ReceivedQty", 0);
        db.insert("Inventory", null, cv);
        db.close();
    }

    public int fetchReturnQtyInventory() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.ReturnQty FROM Inventory A WHERE A.MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        cursor.moveToFirst();
        int ftcReceivedQtyInventory = cursor.getInt(0);
        return  ftcReceivedQtyInventory ;

    }

    public void updateReturnedInventory(int Qty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ReturnQty", fetchReturnQtyInventory() + Qty);
        db.update("Inventory", cv, "MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        db.close();
    }

    public ArrayList<String> fetchCreditTerms(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.NOD,A.PMODE FROM CreditTerms A WHERE CTCODE = '" + PTerms + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> ftchPTerms = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            ftchPTerms.add(cursor.getString(0));
            ftchPTerms.add(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchPTerms;
    }

    public void insertARBalances(String CustomerCode,Long BillingDate,Double Balance, String SalesId,Double Amount) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("BillingDate", BillingDate);
        cv.put("Balance", Balance);
        cv.put("SalesId", SalesId);
        cv.put("Amount", Amount);
        cv.put("Payment", 0);
        db.insert("ARBalances", null, cv);
        db.close();
    }

    public void insertPayment(String Remarks,double TotalAmtPaid,byte[] Signature,Long PaymentDate, String Id,String PaymentId) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Remarks", Remarks);
        cv.put("TotalAmtPaid", TotalAmtPaid);
        cv.put("Signature", Signature);
        cv.put("PaymentDate", PaymentDate);
        cv.put("Status", 0);
        cv.put("Id", Id);
        cv.put("PaymentId", PaymentId);
        db.insert("Payment", null, cv);
        db.close();
    }

    public void insertPaymentItem(String PaymentId,String SalesId,String PaymentTypeId,double AmtPaid,String CheckNumber,Long CheckDate, String Bank,int Item) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("PaymentId", PaymentId);
        cv.put("SalesId", SalesId);
        cv.put("PaymentTypeId", PaymentTypeId);
        cv.put("AmtPaid", AmtPaid);
        cv.put("CheckNumber", CheckNumber);
        cv.put("CheckDate", CheckDate);
        cv.put("Bank", Bank);
        cv.put("Status", 0);
        cv.put("Item", Item);
        db.insert("PaymentItem", null, cv);
        db.close();
    }

    public void insertChecks(String Bank,Long CheckDate,String CheckNumber,double Amount,String PaymentId,int Liquidated,String OriginalCheckNumber,double OriginalAmount) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Bank", Bank);
        cv.put("CheckDate", CheckDate);
        cv.put("Status", 0);
        cv.put("CheckNumber", CheckNumber);
        cv.put("Amount", Amount);
        cv.put("Id", PaymentId);
        cv.put("Liquidated", Liquidated);
        cv.put("OriginalCheckNumber", OriginalCheckNumber);
        cv.put("OriginalAmount", OriginalAmount);
        cv.put("DateChecked", "");
        db.insert("Checks", null, cv);
        db.close();
    }

    /*public void insertARBalancesHistory(String CustomerCode,Long BillingDate,double Balance,String SalesId,double Amount,double Payment) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("BillingDate", BillingDate);
        cv.put("Balance", Balance);
        cv.put("SalesId", SalesId);
        cv.put("Amount", Amount);
        cv.put("Payment", Payment);
        db.insert("ARBalancesHistory", null, cv);
        db.close();
    }*/


    public ArrayList<String > fetchARBalanceSIDFloat(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.SalesID FROM ARBalances A WHERE  A.CustomerCode = '" + PCCode + "' AND A.Balance < 0 ORDER BY A.BillingDate", null);
        cursor.moveToFirst();
        ArrayList<String> ftchARBalanceFloat = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            ftchARBalanceFloat.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchARBalanceFloat;
    }

    /*public ArrayList<String > fetchARBalanceSID(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.SalesID FROM ARBalances A WHERE  A.CustomerCode = '" + PCCode + "' AND A.Balance > 0 ORDER BY A.BillingDate", null);
        cursor.moveToFirst();
        ArrayList<String> ftchARBalanceFloat = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            ftchARBalanceFloat.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchARBalanceFloat;
    }*/

    public String fetchARBalanceSID() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.SalesID FROM ARBalances A WHERE  A.CustomerCode = '" + PCCode + "' AND A.Balance > 0 ORDER BY A.BillingDate LIMIT 1", null);
        cursor.moveToFirst();
        String ftchARBalanceSID = cursor.getString(0);
        return  ftchARBalanceSID ;

    }


    public ArrayList<Double> fetchARBalanceAmtPmentBal(String SalesID){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Amount,A.Payment,A.Balance FROM ARBalances A WHERE  A.SalesID = '" + SalesID + "' AND A.CustomerCode = '" + PCCode + "'", null);
        cursor.moveToFirst();
        ArrayList<Double> ftchARBalanceAmtPmentBal = new ArrayList<Double>();
        while(!cursor.isAfterLast()) {

            ftchARBalanceAmtPmentBal.add(cursor.getDouble(0));
            ftchARBalanceAmtPmentBal.add(cursor.getDouble(1));
            ftchARBalanceAmtPmentBal.add(cursor.getDouble(2));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchARBalanceAmtPmentBal;
    }

    public void updateARBalance(double Payment,double Balance,String SalesID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Payment", Payment);
        cv.put("Balance", Balance);
        db.update("ARBalances", cv, "SalesID = '" + SalesID + "'", null);
        db.close();


    }

    public void updateFloatARBalance(){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE ARBalances  SET Payment = Amount,Balance = 0 WHERE  CustomerCode = '" + PCCode + "' AND Balance < 0");
        db.close();

    }

    public void updateARPayment(String PaymentId,int Item,Double AmtPaid){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE PaymentItem  SET AmtPaid = AmtPaid + " + AmtPaid + " WHERE  PaymentId = '" + PaymentId + "' AND Item =" + Item);
        db.close();

    }

    public void insertPaymentSummary(String PaymentId,String TransactionType,String Username,String CustomerCode,double GrossAmountDue,double PaymentsToDate,double TotalDiscount,double TotalCashReturns,
                                     double NetAmountDue,double TotalCashPayments,double TotalCheckPayments,double TotalOvrCredits,double TotalChargedPayments,double NetCashPayments,double TotalPaymentsReceived,double NetPaymentsReceived,double ChangeGiven,double AmountPayableExcess,int IsFreebie) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("PaymentId", PaymentId);
        cv.put("TransactionType", TransactionType);
        cv.put("Username", Username);
        cv.put("CustomerCode", CustomerCode);
        cv.put("GrossAmountDue",  GrossAmountDue);
        cv.put("PaymentsToDate", PaymentsToDate);
        cv.put("TotalDiscount", TotalDiscount);
        cv.put("TotalCashReturns", TotalCashReturns);
        cv.put("NetAmountDue", NetAmountDue);
        cv.put("TotalCashPayments", TotalCashPayments);
        cv.put("TotalCheckPayments", TotalCheckPayments);
        cv.put("TotalOvrCredits", TotalOvrCredits);
        cv.put("TotalChargedPayments", TotalChargedPayments);
        cv.put("NetCashPayments", NetCashPayments);
        cv.put("TotalOvrCredits", TotalOvrCredits);
        cv.put("TotalPaymentsReceived", TotalPaymentsReceived);
        cv.put("NetPaymentsReceived", NetPaymentsReceived);
        cv.put("ChangeGiven", ChangeGiven);
        cv.put("AmountPayableExcess", AmountPayableExcess);
        cv.put("IsFreebie", IsFreebie);
        db.insert("PaymentSummary", null, cv);
        db.close();
    }

    //Fetch Count Customer
    public int fetchCountCustomer(String CustCode) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Customer WHERE CustomerCode = '" + CustCode + "'", null);
        cursor.moveToFirst();
        int countfetchCustomer = cursor.getInt(0);
        return countfetchCustomer;
    }

    public int fetchCountCustomerName(String CustName) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Customer WHERE CustomerName = '" + CustName + "'", null);
        cursor.moveToFirst();
        int countfetchCustomerName = cursor.getInt(0);
        return countfetchCustomerName;
    }

    public int fetchCountCustomerAddress(String barangay, String city, String province, String region) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Customer WHERE Region = '" + region + "'" +
                "AND Province = '" + province + "'" +
                "AND City = '" + city + "'" +
                "AND Barangay = '" + barangay + "'", null);
        cursor.moveToFirst();
        int countfetchCustomerAddress = cursor.getInt(0);
        return countfetchCustomerAddress;
    }

    public int fetchCountCustomerContactPerson(String ContactPerson) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Customer WHERE ContactPerson = '" + ContactPerson + "'", null);
        cursor.moveToFirst();
        int countfetchCustomerContactPerson = cursor.getInt(0);
        return countfetchCustomerContactPerson;
    }

    public String fetchLastOdometerCustomer() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT CustomerCode FROM OdometerReading ORDER BY _id DESC LIMIT 1", null);
        cursor.moveToFirst();
        String ftchLastOdometerCustomer = cursor.getString(0);
        return ftchLastOdometerCustomer;
    }

    public ArrayList<String> fetchCustomer(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerName,A.PaymentTerms,A.CreditLimit,A.Discount,A.PriceList,A.Status FROM Customer A WHERE CustomerCode='" + PCCode  + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alfetchCustomer = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alfetchCustomer.add(cursor.getString(0));
            alfetchCustomer.add(cursor.getString(1));
            alfetchCustomer.add(cursor.getString(2));
            alfetchCustomer.add(cursor.getString(3));
            alfetchCustomer.add(cursor.getString(4));
            alfetchCustomer.add(cursor.getString(5));
            cursor.moveToNext();
        }
        cursor.close();
        return alfetchCustomer;
    }

    public String fetchBalanceARBal(String CustomerCode) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT Balance FROM ARBalances WHERE CustomerCode = '" + CustomerCode + "'", null);
        cursor.moveToFirst();
        String ftchBalanceARBal;
        try {
            ftchBalanceARBal = cursor.getString(0);

        } catch (Exception e) {
            ftchBalanceARBal = "0.00";
        }

        return ftchBalanceARBal;
    }

    public void updateCheck(String Bank,Long CheckDate,String CheckNumber,String Amount,int Liquidated,String DateChecked,String _id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Bank", Bank);
        cv.put("CheckDate", CheckDate);
        cv.put("CheckNumber", CheckNumber);
        cv.put("Amount", Amount);
        cv.put("Liquidated", Liquidated);
        cv.put("DateChecked", DateChecked);
        db.update("Checks", cv, "_id = '" + _id + "'", null);
        db.close();


    }

    public void insertSales(String CustomerCode,Double Discount,Double AmtDue, String SalesDateTime,String SignedBy,byte[] Signature,String InvoiceNo,String Remarks,String Id,String SalesId) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("Discount", Discount);
        cv.put("AmtDue", AmtDue);
        cv.put("SalesDateTime", SalesDateTime);
        cv.put("Status", 0);
        cv.put("SignedBy",SignedBy);
        cv.put("Signature",Signature);
        cv.put("InvoiceNo",InvoiceNo);
        cv.put("Remarks",Remarks);
        cv.put("Id", Id);
        cv.put("SalesId", SalesId);
        db.insert("Sales", null, cv);
        db.close();
    }

    public void insertSalesItem(String SalesId,String MaterialCode, String Qty,String UnitPrice,Double TotalPrice,int Item) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("SalesId", SalesId);
        cv.put("MaterialCode", MaterialCode);
        cv.put("Qty", Qty );
        cv.put("UnitPrice", UnitPrice);
        cv.put("TotalPrice", TotalPrice);
        cv.put("Status", 0);
        cv.put("Item", Item);

        db.insert("SalesItem", null, cv);
        db.close();
    }

    public int fetchSoldQtyInventory() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.SoldQty FROM Inventory A WHERE A.MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        cursor.moveToFirst();
        int fetchSoldQtyInventory = cursor.getInt(0);
        return  fetchSoldQtyInventory;

    }

    public void updateSoldQtyInventory(int Qty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("SoldQty", fetchSoldQtyInventory() + Qty);
        db.update("Inventory", cv, "MaterialCode = '" + fetchMCodeMaterials() + "'", null);
        db.close();
    }

    public ArrayList<String > fetchSalesPerf(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.SalesQuota,A.Sales,A.SalesPerf,A.Collection,A.CollectionPerf,A.BO,A.BOPerf,A.Balance,A.SDst FROM SalesPerf A", null);
        cursor.moveToFirst();
        ArrayList<String> ftchSalesPerf = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            DecimalFormat SPAmt = new DecimalFormat("#,##0.00");
            ftchSalesPerf.add(SPAmt.format(cursor.getDouble(0)));
            ftchSalesPerf.add(SPAmt.format(cursor.getDouble(1)));
            ftchSalesPerf.add(cursor.getString(2));
            ftchSalesPerf.add(SPAmt.format(cursor.getDouble(3)));
            ftchSalesPerf.add(cursor.getString(4));
            ftchSalesPerf.add(SPAmt.format(cursor.getDouble(5)));
            ftchSalesPerf.add(cursor.getString(6));
            ftchSalesPerf.add(SPAmt.format(cursor.getDouble(7)));
            ftchSalesPerf.add(cursor.getString(8));
            cursor.moveToNext();
        }
        cursor.close();
        return ftchSalesPerf;
    }

    public String fetchLateAR() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT BillingDate FROM ARBalances WHERE CustomerCode ='" + PCCode + "' AND Amount > 0.00 ORDER BY BillingDate LIMIT 1", null);
        cursor.moveToFirst();
        String ftchLateAR;

        try {
            Date ARDate = new Date(cursor.getLong(0));
            SimpleDateFormat ARkDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            ftchLateAR = ARkDate.format(ARDate);

        } catch (Exception e) {
            ftchLateAR = "";
        }
        return ftchLateAR;

    }

    public String fetchCountPayment() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(B.AmtPaid) FROM ARBalances A INNER JOIN PaymentItem B ON A.SalesId = B.SalesId WHERE A.CustomerCode ='" +  PCCode + "' AND B.PaymentTypeId <> '0001'", null);
        cursor.moveToFirst();
        String ftchCountPayment;
        try{
            ftchCountPayment = cursor.getString(0);
        } catch (Exception e) {
            ftchCountPayment = "";
        }

        return ftchCountPayment;
    }

    public String fetchSalesToday() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A INNER JOIN PaymentSummary B ON A.PaymentId = B.PaymentId  WHERE A.PaymentTypeId IN('0001','0002','0003') AND B.TransactionType = 'SALES'", null);//,'005'
        cursor.moveToFirst();
        DecimalFormat SPAmt = new DecimalFormat("#,##0.00");
        String ftchSalesToday;
        try {
            ftchSalesToday = SPAmt.format(cursor.getDouble(0));

        } catch (Exception e) {
            ftchSalesToday = "0.00";
        }
        return ftchSalesToday;

    }

    public String fetchSalesTodayDSR() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A INNER JOIN PaymentSummary B ON A.PaymentId = B.PaymentId WHERE A.PaymentTypeId IN('0001','0002','0003') AND B.TransactionType = 'SALES' AND SUBSTR(B.PaymentId,1,6) = '" + todayDate + "'", null);//,'005'
        cursor.moveToFirst();
        DecimalFormat SPAmt = new DecimalFormat("#,##0.00");
        String ftchSalesToday;
        try {
            ftchSalesToday = SPAmt.format(cursor.getDouble(0));

        } catch (Exception e) {
            ftchSalesToday = "0.00";
        }
        return ftchSalesToday;

    }

    public Double fetchCollToday() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A INNER JOIN PaymentSummary B ON A.PaymentId = B.PaymentId  WHERE A.PaymentTypeId IN('0001','0002') AND B.TransactionType IN ('SALES','AR') ", null);
        cursor.moveToFirst();
        Double ftchCollToday;

        try {
            ftchCollToday = cursor.getDouble(0);

        } catch (Exception e) {
            ftchCollToday = 0.00;
        }
        return ftchCollToday;

    }

    public Double fetchCollTodaySDR() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtPaid) FROM PaymentItem A INNER JOIN PaymentSummary B ON A.PaymentId = B.PaymentId  WHERE A.PaymentTypeId IN('0001','0002') AND B.TransactionType IN ('SALES','AR') AND SUBSTR(B.PaymentId,1,6) = '" + todayDate + "'", null);
        cursor.moveToFirst();
        Double ftchCollToday;

        try {
            ftchCollToday = cursor.getDouble(0);

        } catch (Exception e) {
            ftchCollToday = 0.00;
        }
        return ftchCollToday;

    }

    public Double fetchCollCheckToday() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(AmtPaid) FROM PaymentItem WHERE PaymentTypeId IN ('0002') AND SalesId NOT IN (SELECT SalesId FROM Sales)", null);
        cursor.moveToFirst();
        Double ftchCollToday;
        try {
            ftchCollToday = cursor.getDouble(0);

        } catch (Exception e) {
            ftchCollToday = 0.00;
        }
        return ftchCollToday;

    }

    public String fetchBOToday() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(Total) FROM ReturnItem WHERE RetType IN ('ZCR','RE')", null);
        cursor.moveToFirst();
        DecimalFormat SPAmt = new DecimalFormat("#,##0.00");
        String ftchBOToday;
        try {
            ftchBOToday = SPAmt.format(cursor.getDouble(0));

        } catch (Exception e) {
            ftchBOToday = "0.00";
        }
        return ftchBOToday;

    }

    public String fetchBOTodayDSR() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(Total) FROM ReturnItem WHERE RetType IN ('ZCR','RE') AND SUBSTR(RetId,1,6) = '" + todayDate + "'", null);
        cursor.moveToFirst();
        DecimalFormat SPAmt = new DecimalFormat("#,##0.00");
        String ftchBOToday;
        try {
            ftchBOToday = SPAmt.format(cursor.getDouble(0));

        } catch (Exception e) {
            ftchBOToday = "0.00";
        }
        return ftchBOToday;

    }

    public void insertLocationLog(String LastCustomer,String ODOID,Double Longitude, Double Latitude,String Timestamp) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("LastCustomer", LastCustomer);
        cv.put("ODOID", ODOID);
        cv.put("Longitude", Longitude);
        cv.put("Latitude", Latitude);
        cv.put("Timestamp", Timestamp);
        db.insert("LocationLog", null, cv);
        db.close();
    }

    public byte[] fetchSignature(String StockTransferId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT Signature FROM StockTransfer WHERE StockTransferId = '" + StockTransferId + "'", null);
        cursor.moveToFirst();
        byte[] bSignature = cursor.getBlob(0);

        return bSignature;

    }

    public byte[] fetchSignatureRItem(String ReturnId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT Signature FROM Returns WHERE RetId = '" + ReturnId + "'", null);
        cursor.moveToFirst();

        byte[] bSignature = cursor.getBlob(0);

        return bSignature;

    }
    public byte[] fetchSignatureSItem(String SalesId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT Signature FROM Sales WHERE SalesId = '" + SalesId + "'", null);
        cursor.moveToFirst();

        byte[] bSignature = cursor.getBlob(0);

        return bSignature;

    }


    public ArrayList<String> printStockTransferH(String StockTransferId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.ReceiverSloc,A.TransferDate,A.SenderSloc,B.Name,A.ReceiverName FROM StockTransfer A INNER JOIN Users B ON A.SenderUserId = B.UserID WHERE StockTransferId = '" + StockTransferId + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alpSTH = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpSTH.add(cursor.getString(0));
            Date STDate = new Date(cursor.getLong(1));
            SimpleDateFormat STDTime = new SimpleDateFormat("MM/dd/yy hh:mm:ss a", Locale.getDefault());
            alpSTH.add(STDTime.format(STDate));
            alpSTH.add(cursor.getString(2));
            alpSTH.add(cursor.getString(3));
            alpSTH.add(cursor.getString(4));
            cursor.moveToNext();
        }
        cursor.close();
        return alpSTH;
    }

    //Fetch Count Status For Users
    public int fetchCountItem(String Id,String TableName,String ColumnName) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + TableName + " WHERE " + ColumnName  + " = '" + Id + "'", null);
        cursor.moveToFirst();
        int countSUsers = cursor.getInt(0);
        return countSUsers;
    }

    public int fetchCountInventoryItem() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Inventory", null);
        cursor.moveToFirst();
        int countSUsers = cursor.getInt(0);
        return countSUsers;
    }

    public ArrayList<String> printStockTransferD(String StockTransferId,int Item ){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.Name,B.Unit,A.Qty FROM StockTransferItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode WHERE A.StockTransferId = '" + StockTransferId + "' AND A.Item =" + Item + "", null);
        cursor.moveToFirst();
        ArrayList<String> alpSTD = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpSTD.add(cursor.getString(0));
            alpSTD.add(cursor.getString(1));
            alpSTD.add(cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        return alpSTD;
    }

    public ArrayList<String> printStockReceivingH(String StockTransferId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.TransferDate,A.Sloc,B.Name FROM StockReceiving A INNER JOIN Users B ON A.CheckerUserId = B.UserID WHERE StockReceivingId = '" + StockTransferId + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alpSRH = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            Date SRDate = new Date(cursor.getLong(0));
            SimpleDateFormat SRDTime = new SimpleDateFormat("MM/dd/yy hh:mm:ss a", Locale.getDefault());
            alpSRH.add(SRDTime.format(SRDate));
            alpSRH.add(cursor.getString(1));
            alpSRH.add(cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        return alpSRH;
    }

    public ArrayList<String> printStockReceivingD(String StockReceivingId,int Item ){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Item,B.Name,B.Unit,A.Qty FROM StockReceivingItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode WHERE A.StockReceivingId = '" + StockReceivingId + "' AND A.Item =" + Item + "", null);
        cursor.moveToFirst();
        ArrayList<String> alpSTD = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpSTD.add(cursor.getString(0));
            alpSTD.add(cursor.getString(1));
            alpSTD.add(cursor.getString(2));
            alpSTD.add(cursor.getString(3));
            cursor.moveToNext();
        }
        cursor.close();
        return alpSTD;
    }

    public ArrayList<String> printReturnsH(String RetId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerCode,A.ReturnDate,B.CustomerName,A.TotalAmount,D.Name,A.Retby FROM Returns A INNER JOIN Customer B ON A.CustomerCode = B.CustomerCode INNER JOIN ReturnItem C ON A.RetID = C.RetID  INNER JOIN ReturnType D ON C.RetType = D.RetType WHERE A.RetId = '" + RetId + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alpRIH = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpRIH.add(cursor.getString(0));
            alpRIH.add(cursor.getString(1));
            alpRIH.add(cursor.getString(2));
            DecimalFormat TDAmt = new DecimalFormat("###,##0.00");
            alpRIH.add(TDAmt.format(cursor.getDouble(3)));
            alpRIH.add(cursor.getString(4));
            alpRIH.add(cursor.getString(5));
            cursor.moveToNext();
        }
        cursor.close();
        return alpRIH;
    }

    public ArrayList<String> printReturnsD(String RetId,int Item ){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Qty,B.Name,C.Amount,A.Total FROM ReturnItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode INNER JOIN PricingList C ON A.MaterialCode = C.MaterialCode INNER JOIN Returns D ON A.RetId = D.RetId INNER JOIN Customer E ON D.CustomerCode = E.CustomerCode  WHERE A.RetId = '" + RetId + "' AND A.Item =" + Item + " AND C.PriceList = E.PriceList", null);
        cursor.moveToFirst();
        ArrayList<String> alpRID = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpRID.add(cursor.getString(0));
            alpRID.add(cursor.getString(1));
            DecimalFormat RIAmt = new DecimalFormat("###,##0.00");
            alpRID.add(RIAmt.format(cursor.getDouble(2)));
            alpRID.add(RIAmt.format(cursor.getDouble(3)));
            cursor.moveToNext();
        }
        cursor.close();
        return alpRID;
    }

    public ArrayList<String> printInventoryAcceptanceH(int _id){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.Name,B.Unit,A.Qty,A.QtyInTransit,A.QtyInTransit + A.Qty " +
                " FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode WHERE A._id = " + _id +" ORDER BY A.MaterialCode", null);
        cursor.moveToFirst();
        ArrayList<String> alpIAH = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpIAH.add(cursor.getString(0));
            alpIAH.add(cursor.getString(1));
            alpIAH.add(cursor.getString(2));
            alpIAH.add(cursor.getString(3));
            alpIAH.add(cursor.getString(4));
            cursor.moveToNext();
        }
        cursor.close();
        return alpIAH;
    }

    public int fetchCountARBalancesItem() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM ARBalances WHERE  CustomerCode = '" + PCCode + "'", null);
        cursor.moveToFirst();
        int countSUsers = cursor.getInt(0);
        return countSUsers;
    }

    public ArrayList<String> printARPaymentH(String PaymentId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerCode,B.PaymentDate,C.CustomerName,A.NetAmountDue,A.TotalOvrCredits,A.GrossAmountDue,A.TotalCashPayments,A.TotalCheckPayments,A.ChangeGiven,A.NetPaymentsReceived FROM PaymentSummary A INNER JOIN Payment B ON A.PaymentId = B.PaymentId INNER JOIN Customer C ON A.CustomerCode = C.CustomerCode WHERE A.PaymentId = '" + PaymentId + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alpARPH = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpARPH.add(cursor.getString(0));
            Date ARPDate = new Date(cursor.getLong(1));
            SimpleDateFormat ARPDTime = new SimpleDateFormat("MM/dd/yy hh:mm:ss a", Locale.getDefault());
            alpARPH.add(ARPDTime.format(ARPDate));
            alpARPH.add(cursor.getString(2));

            DecimalFormat ARPAmt = new DecimalFormat("#,##0.00");

            alpARPH.add(ARPAmt.format(cursor.getDouble(3)));
            alpARPH.add(ARPAmt.format(cursor.getDouble(4)* -1));
            alpARPH.add(ARPAmt.format(cursor.getDouble(5)));
            alpARPH.add(ARPAmt.format(cursor.getDouble(6)));
            alpARPH.add(ARPAmt.format(cursor.getDouble(7)));
            alpARPH.add(ARPAmt.format(cursor.getDouble(8)));
            alpARPH.add(ARPAmt.format(cursor.getDouble(9)));
            cursor.moveToNext();
        }
        cursor.close();
        return alpARPH;
    }

    public ArrayList<HashMap<String, String>> fetchCheck(String PaymentID) {
        ArrayList<HashMap<String, String>> ftchCheck;
        ftchCheck = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT Bank,CheckDate,CheckNumber,Amount FROM Checks WHERE Id = '" + PaymentID +"'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                Date CDate = new Date(cursor.getLong(1));
                SimpleDateFormat CWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                map.put("Bank", cursor.getString(0));
                map.put("CheckDt", CWkDate.format(CDate));

                map.put("CheckNo", cursor.getString(2));
                DecimalFormat CAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", CAmt.format(cursor.getDouble(3)));
                ftchCheck.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCheck;
    }

    public ArrayList<String> printSOrderH(String SalesId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerCode,C.SalesDateTime,D.CustomerName,A.PaymentId,A.GrossAmountDue,A.TotalCashReturns,A.NetAmountDue,A.TotalCashPayments,A.TotalCheckPayments,A.TotalChargedPayments,A.ChangeGiven,C.SignedBy,A.TotalDiscount,C.InvoiceNo FROM PaymentSummary A INNER JOIN PaymentItem B ON A.PaymentId = B.PaymentId INNER JOIN Sales C ON B.SalesId = C.SalesId INNER JOIN Customer D ON A.CustomerCode = D.CustomerCode WHERE C.SalesId = '" + SalesId + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alSOH = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alSOH.add(cursor.getString(0));
            alSOH.add(cursor.getString(1));
            alSOH.add(cursor.getString(2));
            alSOH.add(cursor.getString(3));

            DecimalFormat SOAmt = new DecimalFormat("#,##0.00");

            alSOH.add(SOAmt.format(cursor.getDouble(4)));
            alSOH.add(SOAmt.format(cursor.getDouble(5)));
            alSOH.add(SOAmt.format(cursor.getDouble(6)));
            alSOH.add(SOAmt.format(cursor.getDouble(7)));
            alSOH.add(SOAmt.format(cursor.getDouble(8)));
            alSOH.add(SOAmt.format(cursor.getDouble(9)));
            alSOH.add(SOAmt.format(cursor.getDouble(10)));
            alSOH.add(cursor.getString(11));
            alSOH.add(SOAmt.format(cursor.getDouble(12)));
            alSOH.add(cursor.getString(13));
            cursor.moveToNext();
        }
        cursor.close();
        return alSOH;
    }

    /*public ArrayList<String>  printSOrderD(String SalesId){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Qty,B.Name,C.Amount,A.Total FROM SalesItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode INNER JOIN PricingList C ON A.MaterialCode = C.MaterialCode WHERE A.RetId = '" + RetId + "' AND A.Item =" + Item + " AND C.PriceList = '" + fetchDPListSettings() + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alpRID = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alpRID.add(cursor.getString(0));
            alpRID.add(cursor.getString(1));
            DecimalFormat RIAmt = new DecimalFormat("###0.00");
            alpRID.add(RIAmt.format(cursor.getDouble(2)));
            alpRID.add(RIAmt.format(cursor.getDouble(3)));
            cursor.moveToNext();
        }
        cursor.close();
        return alpRID;
    }*/

    public ArrayList<HashMap<String, String>> printSOrderD(String SalesId) {
        ArrayList<HashMap<String, String>> ftchCheck;
        ftchCheck = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.Qty,B.Name,A.UnitPrice,A.TotalPrice FROM SalesItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode  WHERE A.SalesId = '" + SalesId + "' AND A.MaterialCode NOT IN (SELECT A.MatCode FROM PromoD A INNER JOIN PromoH B ON A.PromoID = B.PromoID WHERE B.PromoType = 'FREE')";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Qty", cursor.getString(0));
                map.put("MatDesc", cursor.getString(1));
                DecimalFormat CAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", CAmt.format(cursor.getDouble(2)));
                map.put("Total", CAmt.format(cursor.getDouble(3)));
                ftchCheck.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCheck;
    }

    public ArrayList<HashMap<String, String>> printSOrderPromo(String SalesId) {
        ArrayList<HashMap<String, String>> ftchCheck;
        ftchCheck = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.Qty,B.Name,A.UnitPrice,A.TotalPrice FROM SalesItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode  WHERE A.SalesId = '" + SalesId + "' AND A.MaterialCode IN (SELECT A.MatCode FROM PromoD A INNER JOIN PromoH B ON A.PromoID = B.PromoID WHERE B.PromoType = 'FREE')";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Qty", cursor.getString(0));
                map.put("MatDesc", cursor.getString(1));
                DecimalFormat CAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", CAmt.format(cursor.getDouble(2)));
                map.put("Total", CAmt.format(cursor.getDouble(3)));
                ftchCheck.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCheck;
    }

    public ArrayList<HashMap<String, String>> printPaymentItemD(String SalesId) {
        ArrayList<HashMap<String, String>> ftchCheck;
        ftchCheck = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT CASE A.PaymentTypeId WHEN '0001' THEN 'CASH SALES' WHEN '0002' THEN 'CHECK SALES' WHEN '0003' THEN 'CHARGED SALES' WHEN '0004' THEN 'TMS' ELSE 'RETURNS' END FROM PaymentItem A  WHERE A.SalesId = '" + SalesId + "' GROUP BY A.PaymentTypeId ";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("PaymentMode", cursor.getString(0));
                ftchCheck.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCheck;
    }

    public ArrayList<HashMap<String, String>> printCashDepositsH() {
        ArrayList<HashMap<String, String>> ftchCashDeposited;
        ftchCashDeposited = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.Bank,A.BranchCode,A.AcctNo,A.DateDeposited,A.Amt FROM CashDeposited A";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Bank", cursor.getString(0));
                map.put("Branch",  cursor.getString(1));
                map.put("AcctNo", cursor.getString(2));



                Date CDDate = new Date(cursor.getLong(3));
                SimpleDateFormat CDWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
                map.put("BillDt", CDWkDate.format(CDDate));

                DecimalFormat FRAmt = new DecimalFormat("#,##0.00");
                map.put("Amt", FRAmt.format(cursor.getDouble(4)));

                ftchCashDeposited.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCashDeposited;
    }

    public ArrayList<String> printVReportH(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.Name,A.CheckDate,A.PIID FROM PhysicalInventory A INNER JOIN Users B ON A.AgentId = B.UserID", null);
        cursor.moveToFirst();
        ArrayList<String> alVRH = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alVRH.add(cursor.getString(0));
            alVRH.add(cursor.getString(1));
            alVRH.add(cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        return alVRH;
    }

    public double fetchTotalCOHand() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT (A.[1000] * 1000) + (A.[500] * 500) + (A.[200] * 200) + (A.[100] * 100) + (A.[50] * 50) + (A.[20] * 20) + A.Coins FROM CashOnHand A", null);
        cursor.moveToFirst();
        double ftchTotalCOHand = cursor.getDouble(0);
        return ftchTotalCOHand;
    }

    public double fetchTotalCOHandCashier() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT (A.[1000] * 1000) + (A.[500] * 500) + (A.[200] * 200) + (A.[100] * 100) + (A.[50] * 50) + (A.[20] * 20) + A.Coins FROM CashOnHandCashier A", null);
        cursor.moveToFirst();
        double ftchTotalCOHand = cursor.getDouble(0);
        return ftchTotalCOHand;
    }

    public double fetchSUMCashDepositedCashier() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Amt) FROM CashDepositedCashier A ", null);
        cursor.moveToFirst();
        Double fetchSUMCashDeposited = cursor.getDouble(0);
        return  fetchSUMCashDeposited;
    }

    public double fetchTotalOAmtChecks() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.OriginalAmount) FROM Checks A WHERE A.Liquidated = 1 ", null);
        cursor.moveToFirst();
        double ftchTotalOAmtChecks = cursor.getDouble(0);
        return ftchTotalOAmtChecks;
    }

    public double fetchTotalAmtChecks() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Amount) FROM Checks A WHERE A.Liquidated = 1 ", null);
        cursor.moveToFirst();
        double ftchTotalAmtChecks = cursor.getDouble(0);
        return ftchTotalAmtChecks;
    }

    public double fetchTotalOAmtChecksUL() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.OriginalAmount) FROM Checks A WHERE A.Liquidated = 0 ", null);
        cursor.moveToFirst();
        double ftchTotalOAmtChecks = cursor.getDouble(0);
        return ftchTotalOAmtChecks;
    }

    public double fetchTotalBalARBalances() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Balance) FROM ARBalances A WHERE A.CustomerCode = '" + PCCode + "'" , null);
        cursor.moveToFirst();
        double ftchTotalBalARBalances = cursor.getDouble(0);
        return ftchTotalBalARBalances;
    }

    public double fetchTotalSales() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.AmtDue) FROM Sales A WHERE A.CustomerCode = '" + PCCode + "'" , null);
        cursor.moveToFirst();
        double ftchTotalBalARBalances = cursor.getDouble(0);
        return ftchTotalBalARBalances;
    }

    public double fetchOAmtChecks() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.OriginalAmount) FROM Checks A" , null);
        cursor.moveToFirst();
        double ftchOAmtChecks = cursor.getDouble(0);
        return ftchOAmtChecks;
    }

    public String fetchReturnTypeName(String RetType) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM ReturnType A  WHERE  A.RetType = '" + RetType + "'" , null);
        cursor.moveToFirst();
        String ftchReturnTypeName = cursor.getString(0);
        return  ftchReturnTypeName;
    }

    public String fetchSalesH() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Sales A  WHERE  A.CustomerCode = '" + PCCode + "'" , null);
        cursor.moveToFirst();
        String ftchSalesH = cursor.getString(0);
        return  ftchSalesH;
    }

    public String fetchcountnwcust() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(A.CustomerName) FROM Customer A WHERE SUBSTR(A.CustomerCode,1,2) = 'NW'" , null);
        cursor.moveToFirst();
        return  cursor.getString(0);
    }

    public double fetchSUMAmtPdPaymentCash() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(B.AmtPaid) FROM ARBalances A INNER JOIN PaymentItem B ON A.SalesId = B.SalesId WHERE A.CustomerCode ='" +  PCCode + "' AND B.PaymentTypeId = '0001'", null); //
        cursor.moveToFirst();
        Double ftchSUMAmtPdPaymentCash = cursor.getDouble(0);
        return ftchSUMAmtPdPaymentCash;
    }

    public double fetchSUMAmtPdPaymentCheck() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(B.AmtPaid) FROM ARBalances A INNER JOIN PaymentItem B ON A.SalesId = B.SalesId WHERE A.CustomerCode ='" +  PCCode + "' AND B.PaymentTypeId = '0002'", null); //
        cursor.moveToFirst();
        Double ftchSUMAmtPdPaymentCash = cursor.getDouble(0);
        return ftchSUMAmtPdPaymentCash;
    }
    public double fetchSUMAmtPdPaymentCashOD() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(B.AmtPaid) FROM ARBalances A INNER JOIN PaymentItem B ON A.SalesId = B.SalesId WHERE A.CustomerCode ='" +  PCCode + "' AND B.PaymentTypeId = '0001'", null); //
        cursor.moveToFirst();
        Double ftchSUMAmtPdPaymentCash = cursor.getDouble(0);
        return ftchSUMAmtPdPaymentCash;
    }
    public double fetchSUMAmtPdPayment() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.NetPaymentsReceived) FROM PaymentSummary A  WHERE A.CustomerCode ='" +  PCCode + "' AND A.TransactionType = 'AR'", null); //AND B.PaymentTypeId = '0001'
        cursor.moveToFirst();
        Double ftchSUMAmtPdPayment = cursor.getDouble(0);
        return ftchSUMAmtPdPayment;
    }

    public void updateCreditExpo(Double CreditExposure) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CreditExposure", CreditExposure);
        db.update("Customer", cv, "CustomerCode = '" + PCCode + "'", null);
        db.close();
    }

    public ArrayList<String> fetchReturnUnitQtyPriceMaterial(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Unit, C.Qty + C.QtyInTransit + C.ReturnQty - C.SoldQty - C.TransferredQty + C.ReceivedQty, B.BOPrice FROM Materials A " +
                "INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode LEFT JOIN Inventory C ON A.MaterialCode = C.MaterialCode WHERE A.Name ='" + PMName + "' AND B.PriceList ='" + PDefaultPricelist + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alftchQtyPriceMaterial = new ArrayList<String>();

        while(!cursor.isAfterLast()) {

            alftchQtyPriceMaterial.add(cursor.getString(0));

            String strQty;
            if (cursor.getString(1) == null ){
                strQty = "0";
            }else{
                strQty = cursor.getString(1);
            }

            alftchQtyPriceMaterial.add(strQty);
            DecimalFormat MAmt = new DecimalFormat("#,##0.00");
            alftchQtyPriceMaterial.add(MAmt.format(cursor.getDouble(2)));
            cursor.moveToNext();

        }

        cursor.close();
        return alftchQtyPriceMaterial;
    }

    public int fetchBegInventory() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.QtyInTransit + A.Qty) FROM Inventory A" , null);
        cursor.moveToFirst();
        int fetchBegInventory = cursor.getInt(0);
        return  fetchBegInventory;
    }

    /*public ArrayList<String> fetchChkDtAmt(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CheckDate,A.Amount FROM Checks A INNER JOIN PaymentSummary B ON A.Id = B.PaymentId WHERE B.CustomerCode ='" + PCCode + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alfetchChkDtAmt = new ArrayList<String>();
        while(!cursor.isAfterLast()) {



            Date ChkDtAmtDate = new Date(cursor.getLong(0));
            SimpleDateFormat ChkDtAmtWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
            alfetchChkDtAmt.add(ChkDtAmtWkDate.format(ChkDtAmtDate));
            alfetchChkDtAmt.add(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();
        return alfetchChkDtAmt;
    }*/

    public ArrayList<HashMap<String, String>> fetchChkDtAmt() {
        ArrayList<HashMap<String, String>> fetchChkDtAmt;
        fetchChkDtAmt = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CheckDate,A.Amount FROM Checks A INNER JOIN PaymentSummary B ON A.Id = B.PaymentId WHERE B.CustomerCode ='" + PCCode + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();

                Date ChkDtAmtDate = new Date(cursor.getLong(0));
                SimpleDateFormat ChkDtAmtWkDate = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());

                map.put("ChkDt", ChkDtAmtWkDate.format(ChkDtAmtDate));
                map.put("Amt",  cursor.getString(1));

                fetchChkDtAmt.add(map);
            } while (cursor.moveToNext());
        }
        return fetchChkDtAmt;
    }

    public int fetchcountpayment(String PaymentID) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Payment WHERE PaymentId = '" + PaymentID + "'" , null);
        cursor.moveToFirst();
        int countpayment = cursor.getInt(0);
        return countpayment ;
    }

    public int fetchcountpaymentitem(String PaymentID) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM PaymentItem WHERE PaymentId = '" + PaymentID + "'" , null);
        cursor.moveToFirst();
        int countpayment = cursor.getInt(0);
        return countpayment ;
    }

    public int fetchcountpaymentsummary(String PaymentID) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM PaymentSummary WHERE PaymentId = '" + PaymentID + "'" , null);
        cursor.moveToFirst();
        int countpayment = cursor.getInt(0);
        return countpayment ;
    }

    public ArrayList<HashMap<String, String>> fetchStockTransferItem(String StockTransferID) {
        ArrayList<HashMap<String, String>> ftchStockTransferItem;
        ftchStockTransferItem = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.MaterialCode,A.Qty FROM StockTransferItem A WHERE A.StockTransferId = '" + StockTransferID + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("MaterialCode", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                ftchStockTransferItem.add(map);
            } while (cursor.moveToNext());
        }
        return ftchStockTransferItem;
    }

    public void insertPurchaseOrder(String ID,String AgentCode,String PODate,String DeliveryDate) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ID", ID);
        cv.put("AgentCode", AgentCode);
        cv.put("PODate", PODate);
        cv.put("DeliveryDate", DeliveryDate);
        db.insert("PurchaseOrder", null, cv);
        db.close();
    }

    public void insertPurchaseOrderItem(String ID,int item,String MaterialCode,int qty) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ID", ID);
        cv.put("Item", item);
        cv.put("MaterialCode", MaterialCode);
        cv.put("Qty", qty);
        cv.put("Status", 0);
        db.insert("PurchaseOrderItem", null, cv);
        db.close();
    }

    public void updatePurchaseOrderItemAll(String ID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Status", 1);
        db.update("PurchaseOrderItem", cv, "ID = '" + ID + "'", null);
        db.close();
    }

    public void updatePurchaseOrderItem(String ID,String MaterialCode) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Status", 1);
        db.update("PurchaseOrderItem", cv, "ID = '" + ID + "' AND MaterialCode='" + MaterialCode + "'", null);
        db.close();

    }


    public ArrayList<HashMap<String, String>> fetchPurchaseOrder(String PurchaseOrderID) {
        ArrayList<HashMap<String, String>> ftchPurchaseOrder;
        ftchPurchaseOrder = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.ID,A.AgentCode,A.PODate,A.DeliveryDate,B.MaterialCode,B.Qty FROM PurchaseOrder A INNER JOIN PurchaseOrderItem B ON A.ID = B.ID WHERE A.ID = '" + PurchaseOrderID + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ID", cursor.getString(0));
                map.put("AgentCode", cursor.getString(1));
                map.put("PODate", cursor.getString(2));
                map.put("DeliveryDate", cursor.getString(3));
                map.put("MaterialCode", cursor.getString(4));
                map.put("Qty", cursor.getString(5));
                ftchPurchaseOrder.add(map);
            } while (cursor.moveToNext());
        }
        return ftchPurchaseOrder;
    }



    // ADDRESS

    public String[] fetchRegioncat() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT DISTINCT UPPER(RGNNM) FROM AddressRegion ORDER BY RGNNM", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchProvinceCat(String Region) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT UPPER(B.PRVDSC) FROM AddressRegion A INNER JOIN AddressProvince B ON A.RGNCD = B.REGCD WHERE A.RGNNM = '" + Region + "' ORDER BY B.PRVDSC", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchCityMunicipalityCat(String Province) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT UPPER(B.CITYMUNDSC) FROM AddressProvince A INNER JOIN AddressCityMunicipality B ON A.PRVCD = B.PRVCD WHERE A.PRVDSC = '" + Province +"' ORDER BY B.CITYMUNDSC", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchBrgy(String CityMun) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT UPPER(B.BRGYDSC) FROM AddressCityMunicipality A INNER JOIN AddressBarangay B ON A.CITYMUNCD = B.CITYMUNCD WHERE CITYMUNDSC ='" + CityMun + "' ORDER BY BRGYDSC", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchBarangay() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT DISTINCT UPPER(BRGYDSC) FROM AddressBarangay ORDER BY BRGYDSC", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchMunicipalityCity(String Barangay) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.CITYMUNDSC FROM AddressBarangay A INNER JOIN AddressCityMunicipality B ON A.CITYMUNCD = B.CITYMUNCD WHERE UPPER(A.BRGYDSC) = '" + Barangay + "' ORDER BY B.CITYMUNDSC", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String[] fetchProvince(String CityMun) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT DISTINCT B.PRVDSC FROM AddressCityMunicipality A INNER JOIN AddressProvince B ON A.PRVCD = B.PRVCD WHERE UPPER(A.CITYMUNDSC) = '" + CityMun + "' ORDER BY A.CITYMUNDSC", null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String fetchregion(String Province) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.RGNNM FROM AddressRegion A INNER JOIN AddressProvince B ON A.RGNCD = B.REGCD WHERE B.PRVDSC = '" + Province + "'" , null);
        cursor.moveToFirst();
        return cursor.getString(0) ;
    }

    public String fetchpostal(String MunCity) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT ZIPCD FROM AddressZipCode WHERE MUNCITY = '" + MunCity + "'" , null);
        cursor.moveToFirst();
        String ZipCode;
        if (cursor.getCount()> 0) {
            ZipCode = cursor.getString(0);
        } else {
            ZipCode = "";
        }

        return ZipCode ;
    }




   /* public void insertCustomerAttachment(String CustomerCode,byte[] Attachment , String Description,String DateCreated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("Attachment", Attachment);
        cv.put("Description", Description);
        cv.put("DateCreated", DateCreated);
        db.insert("CustomerAttachment", null, cv);
        db.close();
    }*/
    public void insertCustomerBuying(String CustomerCode, String BuyerName,String Gender,String ContactNo,String Category,String Favorite,String Birthday,String Address) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("BuyerName", BuyerName);
        cv.put("Gender", Gender);
        cv.put("ContactNo", ContactNo);
        cv.put("Category", Category);
        cv.put("Favorite", Favorite);
        cv.put("Birthday", Birthday);
        cv.put("Address", Address);
        db.insert("CustomerBuying", null, cv);
        db.close();
    }

    public void insertCustomerSequence(String CustomerCode, String WkNum, String CallDay, String CallSeq) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode", CustomerCode);
        cv.put("WkNum", WkNum);
        cv.put("CallDay", CallDay);
        cv.put("CallSeq", CallSeq);
        db.insert("Frequency", null, cv);
        db.close();
    }

    public void deleteCustomerFrequency(String CustCode) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.delete("Frequency", "CustomerCode = '" + CustCode +"'", null);
        db.close();
    }

    public String fetchExtMatGrp() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.ExtMatGrp FROM Materials A WHERE A.Name = '" + PMName + "'", null);

        cursor.moveToFirst();
        return  cursor.getString(0);
    }

    public ArrayList<String> FetchNameMaterialsPacks(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Name FROM Materials A INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode WHERE A.Unit = 'PAK' AND B.PriceList = '" + fetchDefaultPListList() + "' AND SUBSTR(A.MaterialCode,1,7) <> 'FG-4005' AND (A.ExtMatGrp <> ' ' AND A.ExtMatGrp <> '') A.MatEanUpc ='" + PMatName + "'" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchNMaterials = new ArrayList<String>();

        if (cursor.isAfterLast()){
            alftchNMaterials.add("");
            cursor.moveToNext();

        }else{
            while(!cursor.isAfterLast()) {
                alftchNMaterials.add(cursor.getString(0));
                cursor.moveToNext();
            }
        }
        cursor.close();
        return alftchNMaterials;
    }

    public String[] fetchMNameMaterialsPacksPromo() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.Name FROM PromoCustomers A INNER JOIN Materials B ON A.ItemCode = B.MaterialCode WHERE A.Balance > 0" , null);
        cursor.moveToFirst();
        ArrayList<String> alftchMNameMaterials = new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alftchMNameMaterials.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alftchMNameMaterials.toArray(new String[alftchMNameMaterials.size()]);
    }

    public String fetchBalance(String name) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Balance FROM PromoCustomers A INNER JOIN Materials B ON A.ItemCode = B.MaterialCode WHERE B.Name = '" + name + "'", null);

        cursor.moveToFirst();
        return  cursor.getString(0);
    }

    public ArrayList<HashMap<String, String>> fetchCustomerInfo(String customerCode) {
        ArrayList<HashMap<String, String>> ftchCustomerInfo;
        ftchCustomerInfo = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.*,B.Description FROM Customer A INNER JOIN CustomerType B ON A.CusType = B.CusType WHERE A.CustomerCode = '" + customerCode + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("CustomerName", cursor.getString(3));
                map.put("CustomerType", cursor.getString(39) + "-" + cursor.getString(49));
                map.put("OwnerName", cursor.getString(28));
                map.put("Limit", cursor.getString(5));
                map.put("CreditExpo", cursor.getString(6));
                map.put("PaymentTerms", cursor.getString(32));
                map.put("Route", cursor.getString(30));
                map.put("ContactNumber", cursor.getString(29));
                map.put("MobileNumber", cursor.getString(40));
                map.put("SAPAddress", cursor.getString(12) + " " + cursor.getString(20));
                map.put("UnitNo", cursor.getString(8) );
                map.put("Street", cursor.getString(10) );
                map.put("Subdv", cursor.getString(14) );
                map.put("Barangay", cursor.getString(16) );
                map.put("City", cursor.getString(18) );
                map.put("Province", cursor.getString(22));
                map.put("Region", cursor.getString(26));
                map.put("Postal", cursor.getString(24));

                map.put("SAPHAddress", cursor.getString(13) + " " + cursor.getString(21));
                map.put("UnitNoH", cursor.getString(9) );
                map.put("StreetH", cursor.getString(11) );
                map.put("SubdvH", cursor.getString(15) );
                map.put("BarangayH", cursor.getString(17) );
                map.put("CityH", cursor.getString(19) );
                map.put("ProvinceH", cursor.getString(23));
                map.put("RegionH", cursor.getString(27));
                map.put("PostalH", cursor.getString(25));

                map.put("OrderDay", cursor.getString(45));
                map.put("DelDay", cursor.getString(46));
                map.put("DelWTimeFrom", cursor.getString(47));
                map.put("DelWTimeTo", cursor.getString(48));

                ftchCustomerInfo.add(map);
            } while (cursor.moveToNext());
        }
        return ftchCustomerInfo;
    }

    public ArrayList<HashMap<String, String>> fetchBuyingCustomer(String CustomerCode) {
        ArrayList<HashMap<String, String>> ftchBuyingCustomer;
        ftchBuyingCustomer = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM CustomerBuying WHERE CustomerCode = '" + CustomerCode + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("BuyerName", cursor.getString(2));
                map.put("Gender", cursor.getString(3));
                map.put("ContactNo", cursor.getString(4));
                map.put("Category", cursor.getString(5));
                map.put("Favorite", cursor.getString(6));
                map.put("Birthday", cursor.getString(7));
                map.put("Address", cursor.getString(8));
                ftchBuyingCustomer.add(map);
            } while (cursor.moveToNext());
        }
        return ftchBuyingCustomer;
    }

    public ArrayList<HashMap<String, String>> fetchFrequencyCustomer(String CustomerCode) {
        ArrayList<HashMap<String, String>> ftchBuyingCustomer;
        ftchBuyingCustomer = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT * FROM Frequency WHERE CustomerCode = '" + CustomerCode + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("WkNum", cursor.getString(2));
                map.put("CallDay", cursor.getString(3));
                map.put("CallSeq", cursor.getString(4));
                ftchBuyingCustomer.add(map);
            } while (cursor.moveToNext());
        }
        return ftchBuyingCustomer;
    }

    public int fetchSDR(String DateToday) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT ISDSR FROM Attendance WHERE DTTMIN = '" + DateToday + "'" , null);
        cursor.moveToFirst();
        int SDR;
        if (cursor.getCount() > 0){
            SDR = cursor.getInt(0);
        }else{
            SDR = 2;
        }
        return SDR ;
    }

    public int fetchISSDR() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT ISDSR FROM Attendance WHERE DTTMIN = '" + todayDate2 + "'" , null);
        cursor.moveToFirst();
        int SDR;
        if (cursor.getCount() > 0){
            SDR = cursor.getInt(0);
        }else{
            SDR = 2;
        }
        return SDR ;
    }


    public int fetchNotTimeOutLate() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Attendance WHERE DTTMOUT IS NULL AND DTTMIN <> '" + todayDate2 + "'" , null);
        cursor.moveToFirst();
        int DateTime;
        if (cursor.getCount() > 0){
            DateTime = cursor.getInt(0);
        }else{
            DateTime = 0;
        }
        return DateTime ;
    }

    public int fetchNotTimeIn() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Attendance WHERE DTTMIN = '" + todayDate2 + "'" , null);
        cursor.moveToFirst();
        int DateTime;
        if (cursor.getCount() > 0){
            DateTime = cursor.getInt(0);
        }else{
            DateTime = 0;
        }
        return DateTime ;
    }

    public int fetchNotTimeOut() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Attendance WHERE DTTMIN = '" + todayDate2 + "' AND DTTMOUT IS NULL" , null);
        cursor.moveToFirst();
        int DateTime;
        if (cursor.getCount() > 0){
            DateTime = cursor.getInt(0);
        }else{
            DateTime = 0;
        }
        return DateTime;
    }

    public int fetchTimeInOutComplete() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM Attendance WHERE DTTMIN = '" + todayDate2 + "' AND DTTMOUT IS NOT NULL" , null);
        cursor.moveToFirst();
        int DateTime;
        if (cursor.getCount() > 0){
            DateTime = cursor.getInt(0);
        }else{
            DateTime = 0;
        }
        return DateTime;
    }

    public void updateAttendance(String DateTimeOut,String TimeOut,String Location,int ISDSR) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("DTTMOUT", DateTimeOut);
        cv.put("TMOUT", TimeOut);
        cv.put("TMOUTLOC", Location);
        cv.put("ISDSR", ISDSR);
        db.update("Attendance", cv, "DTTMOUT IS NULL", null);
        db.close();
    }

    public void updateAttendanceDSR() {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ISDSR", 1);
        db.update("Attendance", cv, "DTTMOUT IS NULL", null);
        db.close();
    }

    public String[] fetchReliever() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.RelieverID,Reliever FROM Reliever A", null);
        cursor.moveToFirst();
        ArrayList<String> alBAccounts= new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alBAccounts.add(cursor.getString(0) + "-" + cursor.getString(1) );
            cursor.moveToNext();
        }
        cursor.close();
        return alBAccounts.toArray(new String[alBAccounts.size()]);
    }

    public void insertAttendance(String DateTimeIn,String TimeIn,String Location,String RelieverID) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("DTTMIN", DateTimeIn);
        cv.put("TMIN", TimeIn);
        cv.put("TMINLOC", Location);
        cv.put("ISDSR", 0);
        cv.put("RelieverID", RelieverID);
        db.insert("Attendance", null, cv);
        db.close();
    }

    public byte[] fetchImage(String CustCode){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT Attachment FROM Customer WHERE CustomerCode = '" + CustCode + "'", null);
        cursor.moveToFirst();
        byte[] bAttachment = cursor.getBlob(0);

        return bAttachment;

    }
    public void deleteCustomerBuying(String CustCode) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        db.delete("CustomerBuying", "CustomerCode = '" + CustCode +"'", null);
        db.close();
    }

    public void updateCustomer(String CustomerCode, String ContactNumber,
                               String SAPCity,String SAPStreet,String UnitNo,String Street,String Subdv,String Barangay,String City,String Province,String Postal,String Region,
                               String MobileNumber,String UnitNoH,String StreetH,String SubdvH,String BarangayH,String CityH,String ProvinceH,
                               String SAPStreetH,String PostalH,String SAPCityH,String RegionH,String Location,byte[] Attachment,int Status,
                               String Freq,String OrderDay,String DeliveyDay,String DelWTimeFrom,String DelWTimeTo) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ContactNumber", ContactNumber);
        cv.put("SAPCity", SAPCity);
        cv.put("SAPStreet", SAPStreet);
        cv.put("UnitNo", UnitNo);
        cv.put("Street", Street);
        cv.put("Subdv", Subdv);
        cv.put("Barangay", Barangay);
        cv.put("City", City);
        cv.put("Province", Province);
        cv.put("Postal",Postal);
        cv.put("Region",Region);
        cv.put("MobileNumber",MobileNumber);
        cv.put("UnitNoH", UnitNoH);
        cv.put("StreetH", StreetH);
        cv.put("SubdvH", SubdvH);
        cv.put("BarangayH", BarangayH);
        cv.put("CityH", CityH);
        cv.put("ProvinceH", ProvinceH);
        cv.put("SAPStreetH",SAPStreetH);
        cv.put("PostalH",PostalH);
        cv.put("SAPCityH",SAPCityH);
        cv.put("RegionH",RegionH);
        cv.put("Status",Status);
        cv.put("Location",Location);
        cv.put("Attachment", Attachment);
        cv.put("Freq", Freq);
        cv.put("OrderDay", OrderDay);
        cv.put("DelDay", DeliveyDay);
        cv.put("DelWTimeFrom", DelWTimeFrom);
        cv.put("DelWTimeTo", DelWTimeTo);
        db.update("Customer", cv, "CustomerCode = '" + CustomerCode + "'", null);
        db.close();
    }

    public int fetchLastOdoToday() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.OdoReading FROM OdometerReading A INNER JOIN LocationLog B ON A.Id = B.ODOID WHERE B.TimeStamp = '" + todayDate + "' ORDER BY A.Id DESC LIMIT 1", null);
        cursor.moveToFirst();

        int odometerReading;
        if (cursor.getCount() > 0){
            odometerReading = cursor.getInt(0);
        }else{
            odometerReading = 0;
        }

        return odometerReading;
    }

    public int fetchFirstOdoToday() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.OdoReading FROM OdometerReading A INNER JOIN LocationLog B ON A.Id = B.ODOID WHERE B.TimeStamp = '" + todayDate + "' ORDER BY A.Id LIMIT 1", null);
        cursor.moveToFirst();
        int odometerReading;
        if (cursor.getCount() > 0){
            odometerReading = cursor.getInt(0);
        }else{
            odometerReading = 0;
        }

        return odometerReading;
    }

    public ArrayList<HashMap<String, String>> fetchSalesItemToday() {
        ArrayList<HashMap<String, String>> ftchSalesItemToday;
        ftchSalesItemToday = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.ExtMatGrp,SUM(A.Qty) FROM SalesItem A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode WHERE SUBSTR(A.SalesId,1,6) = '" + todayDate +"'  GROUP BY B.ExtMatGrp";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ExtMatGrp", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                ftchSalesItemToday.add(map);
            } while (cursor.moveToNext());
        }
        return ftchSalesItemToday;
    }

    public ArrayList<HashMap<String, String>> fetchInventoryAcceptanceToday() {
        ArrayList<HashMap<String, String>> ftchInventoryAcceptanceToday;
        ftchInventoryAcceptanceToday = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.ExtMatGrp,A.QtyInTransit + A.Qty AS Total FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode ORDER BY A.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ExtMatGrp", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                ftchInventoryAcceptanceToday.add(map);
            } while (cursor.moveToNext());
        }
        return ftchInventoryAcceptanceToday;
    }

    public ArrayList<HashMap<String, String>> fetchEndingInventoryToday() {
        ArrayList<HashMap<String, String>> ftchEndingInventoryToday;
        ftchEndingInventoryToday = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT B.ExtMatGrp,A.Qty + A.QtyInTransit + A.ReturnQty + A.ReceivedQty - A.SoldQty- A.TransferredQty AS Total FROM Inventory A INNER JOIN Materials B ON A.MaterialCode = B.MaterialCode";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("ExtMatGrp", cursor.getString(0));
                map.put("Qty", cursor.getString(1));
                ftchEndingInventoryToday.add(map);
            } while (cursor.moveToNext());
        }
        return ftchEndingInventoryToday;
    }

    public ArrayList<HashMap<String, String>> fetchBankRemittance() {
        ArrayList<HashMap<String, String>> ftchBankRemittance;
        ftchBankRemittance = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT Bank,AcctNo,SUM(Amt) FROM CashDeposited WHERE SUBSTR(STRFTIME('%Y%m%d',SUBSTR(DATETIME(DateDeposited/1000, 'unixepoch', 'localtime'),1,10)),3,8) = '" + todayDate +"' GROUP BY Bank,AcctNo,SUBSTR(DATETIME(DateDeposited/1000, 'unixepoch', 'localtime'),1,10)";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Bank", cursor.getString(0));
                map.put("AcctNo", cursor.getString(1));
                map.put("Amt", cursor.getString(2));
                ftchBankRemittance.add(map);
            } while (cursor.moveToNext());
        }
        return ftchBankRemittance;
    }

    public void updatePromoCustomers(String materialCode,int Qty){

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE PromoCustomers  SET Ordered =  Ordered + " + Qty + ", Balance = Balance -  " + Qty + " WHERE  ItemCode = '" + materialCode + "'");
        db.close();

    }

    public int fetchCountPurchaseOrder(String isRegular) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*)  FROM PurchaseOrder WHERE SUBSTR(ID,3,4) = '" + todayDate.substring(2,6) + "' AND SUBSTR(ID,1,1) = '" + isRegular + "'", null);
        cursor.moveToFirst();

        int count;
        if (cursor.getCount() > 0){
            count = cursor.getInt(0);
        }else{
            count = 0;
        }

        return  count;
    }

    public int fetchCountCustomerCode(String CustomerCode) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*)  FROM Customer WHERE CustomerCode = '" + CustomerCode + "'", null);
        cursor.moveToFirst();

        int count;
        if (cursor.getCount() > 0){
            count = cursor.getInt(0);
        }else{
            count = 0;
        }

        return  count;
    }

    public ArrayList<HashMap<String, String>> fetchMustCarry() {
        ArrayList<HashMap<String, String>> ftcMustCarry;
        ftcMustCarry = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustCode,B.CustomerName,A.CC,A.WCH,A.LAC,A.LAB,A.IA FROM MustCarry A\n" +
                "INNER JOIN Customer B ON A.CustCode = B.CustomerCode ORDER BY B.CustomerName";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("CustCode", cursor.getString(0));
                map.put("CustName", cursor.getString(1));
                map.put("CC",  Utils.isMustCarry(cursor.getString(2)));
                map.put("WCH", Utils.isMustCarry(cursor.getString(3)));
                map.put("LAC", Utils.isMustCarry(cursor.getString(4)));
                map.put("LAB", Utils.isMustCarry(cursor.getString(5)));
                map.put("IA", Utils.isMustCarry(cursor.getString(6)));
                ftcMustCarry.add(map);
            } while (cursor.moveToNext());
        }
        return ftcMustCarry;
    }

    public ArrayList<HashMap<String, String>> fetchMustCarry(int Status) {
        ArrayList<HashMap<String, String>> ftcMustCarry;
        ftcMustCarry = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustCode,B.CustomerName,A.CC,A.WCH,A.LAC,A.LAB,A.IA FROM MustCarry A\n" +
                "INNER JOIN Customer B ON A.CustCode = B.CustomerCode AND A.MCStatus =" + Status + " ORDER BY B.CustomerName";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("CustCode", cursor.getString(0));
                map.put("CustName", cursor.getString(1));
                map.put("CC",  Utils.isMustCarry(cursor.getString(2)));
                map.put("WCH", Utils.isMustCarry(cursor.getString(3)));
                map.put("LAC", Utils.isMustCarry(cursor.getString(4)));
                map.put("LAB", Utils.isMustCarry(cursor.getString(5)));
                map.put("IA", Utils.isMustCarry(cursor.getString(6)));
                ftcMustCarry.add(map);
            } while (cursor.moveToNext());
        }
        return ftcMustCarry;
    }

    public ArrayList<HashMap<String, String>> fetchNotBuyingCustomer() {
        ArrayList<HashMap<String, String>> ftchNotBuyingCustomer;
        ftchNotBuyingCustomer = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustCode,B.CustomerName,A.PrevMon,A.[2MonsAgo],A.[3MonsAgo] FROM NotBuyingCustomer A\n" +
                "INNER JOIN Customer B ON A.CustCode = B.CustomerCode ORDER BY A.PrevMon";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("CustCode", cursor.getString(0));
                map.put("CustName", cursor.getString(1));
                map.put("PrevMon",  Utils.convertToDecimal(cursor.getString(2)));
                map.put("2MonsAgo", Utils.convertToDecimal(cursor.getString(3)));
                map.put("3MonsAgo", Utils.convertToDecimal(cursor.getString(4)));
                ftchNotBuyingCustomer.add(map);
            } while (cursor.moveToNext());
        }
        return ftchNotBuyingCustomer;
    }



    public void export(){

        DateFormat df = new SimpleDateFormat("MMddyyHHmm");
        String date = df.format(Calendar.getInstance().getTime());

        try {
            //File sd = Environment.getExternalStorageDirectory();
            File sd = new File(exports);//"/storage/sdcard1/Android/data/com.lemonsquare.mpos/files/backup"
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/com.lemonsquare.distrilitemposv2/databases/mpos";
                String backupDBPath ="Database_" + fetchdbSettings().get(6)+"_"+ date +".db";
                File currentDB = new File(data, currentDBPath);
                backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
    }

    public ArrayList<HashMap<String, String>> fetchPriceSurveyList(String unit) {
        ArrayList<HashMap<String, String>> ftchIList;
        ftchIList = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT PRODDSC,'0.00' AS PRC,BSUNT FROM PriceSurvey WHERE BSUNT = '" + unit + "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {

            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Item", cursor.getString(0));
                map.put("Price", cursor.getString(1));
                map.put("Unit", cursor.getString(2));
                ftchIList.add(map);
            } while (cursor.moveToNext());
        }
        return ftchIList;
    }

    public String fetchSegNm(String custCode) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT CASE CusType WHEN 'SSS' THEN 'PC' ELSE 'PAC' END FROM Customer WHERE CustomerCode = '" + custCode + "'" , null);
        cursor.moveToFirst();

        return  cursor.getString(0);
    }

    public String fetchProdCd(String prodDesc) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT PRODCD FROM PriceSurvey WHERE PRODDSC = '" + prodDesc + "'" , null);
        cursor.moveToFirst();

        return  cursor.getString(0);
    }

    public int fetchCountPriceSurvey() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM PriceSurvey", null);
        cursor.moveToFirst();
        int countPSurvey;

        try {
            countPSurvey = cursor.getInt(0);

        } catch (Exception e) {
            countPSurvey = 0;
        }
        return countPSurvey;

    }

    public void requestDeactivation(String customerCode,String remarks) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("Remarks", remarks);
        cv.put("Status",8);
        db.update("Customer", cv, "CustomerCode = '" + customerCode + "'", null);
        db.close();
    }

    public void export(String export){

        DateFormat df = new SimpleDateFormat("MMddyyHHmm");
        String date = df.format(Calendar.getInstance().getTime());

        try {
            //File sd = Environment.getExternalStorageDirectory();
            File sd = new File(export);//"/storage/sdcard1/Android/data/com.lemonsquare.mpos/files/backup"
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/com.lemonsquare.distrilitemposv2/databases/mpos";
                String backupDBPath ="Database_" + fetchdbSettings().get(6)+"_"+ date +".db";
                File currentDB = new File(data, currentDBPath);
                backupDB = new File(sd, backupDBPath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {

        }
    }

    //MVC Pattern

    public List<ItemList> fetchMaterialsOrdering()
    {
        List<ItemList> itemList =new ArrayList<ItemList>();
        String selectQuery = "SELECT MaterialCode,Name,Unit,MatEanUpc FROM Materials";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                itemList.add(new ItemList(cursor.getString(0),cursor.getString(1),
                        cursor.getString(2),cursor.getString(3)));
            } while (cursor.moveToNext());
        }
        return itemList;

    }

    public List<SettingsList> fetchSettings()
    {
        List<SettingsList> settingsList = new ArrayList<SettingsList>();
        String selectQuery = "SELECT Status,SalesDistrict,Sloc,TerminalId,DatabasePassword,DatabaseUsername,DatabaseName,ServerAddress," +
                "LastOdometer,Plant,ContactNumberOrders,ContactNumberHeader,ContactNumberCustomerService,MovingAverageBuffer,DefaultPriceList," +
                "OfficeAddress,PaymentTermDays,MinimumAmount,SMSGatewayNo FROM Settings";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                settingsList.add(new SettingsList(cursor.getString(0),cursor.getString(1),
                        cursor.getString(2),cursor.getString(3),cursor.getString(4),
                        cursor.getString(5),cursor.getString(6),cursor.getString(7),
                        cursor.getString(8),cursor.getString(9),cursor.getString(10),
                        cursor.getString(11),cursor.getString(12),cursor.getString(13),
                        cursor.getString(14),cursor.getString(15),cursor.getString(16),
                        cursor.getString(17)));
            } while (cursor.moveToNext());
        }
        return settingsList;

    }

    public double fetchARBalance(String customerCode) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(A.Balance) FROM ARBalances A  WHERE  A.CustomerCode = '" + customerCode + "' AND A.Balance < 0" , null);
        cursor.moveToFirst();
        Double fetchSUMARBalances = Double.valueOf(ARAmt.format(cursor.getDouble(0)));
        return  fetchSUMARBalances;
    }
    /*public ArrayList<String> fetchCustInfo(String customerCode){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerName,A.PaymentTerms,A.CreditLimit,A.Discount*-1,A.PriceList,A.Status FROM Customer A WHERE CustomerCode='" + PCCode  + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alfetchCustomer = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alfetchCustomer.add(cursor.getString(0));
            alfetchCustomer.add(cursor.getString(1));
            alfetchCustomer.add(cursor.getString(2));
            alfetchCustomer.add(cursor.getString(3));
            alfetchCustomer.add(cursor.getString(4));
            alfetchCustomer.add(cursor.getString(5));
            cursor.moveToNext();
        }
        cursor.close();
        return alfetchCustomer;RF
    }
*/


    public ArrayList<String> fetchPromoAvailable(String materialName){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.QtyToAvail,B.FreeQty,D.Name,C.Amount,D.Unit FROM PromoH A INNER JOIN PromoD B ON A.PromoID = B.PromoID INNER JOIN PricingList C ON B.MatCode = C.MaterialCode INNER JOIN Materials D ON B.MatCode = D.MaterialCode INNER JOIN Inventory E ON B.MatCode = E.MaterialCode WHERE E.Qty + E.QtyInTransit + E.ReturnQty - E.SoldQty - E.TransferredQty + E.ReceivedQty > 0 AND A.PromoDesc = '" + materialName + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alfetchPromoAvailable = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alfetchPromoAvailable.add(cursor.getString(0));
            alfetchPromoAvailable.add(cursor.getString(1));
            alfetchPromoAvailable.add(cursor.getString(2));
            alfetchPromoAvailable.add(cursor.getString(3));
            alfetchPromoAvailable.add(cursor.getString(4));

            cursor.moveToNext();
        }
        cursor.close();
        return alfetchPromoAvailable;
    }

    public int fetchCntPromoD(String matdesc) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(A.MatDesc) FROM PromoD A INNER JOIN Materials B ON A.MatCode = B.MaterialCode WHERE B.Name =  '" + matdesc + "'" , null);
        cursor.moveToFirst();
        return  cursor.getInt(0);
    }

    public ArrayList<String> MustCarry(){
        Cursor cursor = getReadableDatabase().rawQuery("SELECT CC,WCH,LAC,LAB,IA,MCStatus FROM MustCarry WHERE CustCode = '" + PCCode + "'", null);
        cursor.moveToFirst();
        ArrayList<String> alfetchPromoAvailable = new ArrayList<String>();
        while(!cursor.isAfterLast()) {
            alfetchPromoAvailable.add(cursor.getString(0));
            alfetchPromoAvailable.add(cursor.getString(1));
            alfetchPromoAvailable.add(cursor.getString(2));
            alfetchPromoAvailable.add(cursor.getString(3));
            alfetchPromoAvailable.add(cursor.getString(4));
            alfetchPromoAvailable.add(cursor.getString(5));
            cursor.moveToNext();
        }
        cursor.close();
        return alfetchPromoAvailable;
    }

    public void updateMustCarry(String ColumnName) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(ColumnName, 1);
        db.update("MustCarry", cv, "CustCode = '" + PCCode + "'", null);
        db.close();
    }


    public void  updateMustCarryStatus() {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE MustCarry SET MCStatus = CASE WHEN CC = 1 AND WCH = 1 AND LAC = 1 AND LAB = 1 AND IA = 1 THEN 1 ELSE 0 END  WHERE  CustCode = '" + PCCode + "'");
        db.close();

    }

    public String[] fetchCheckInRemarks() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT Reason FROM Reason WHERE Category IN (1,3)", null);
        cursor.moveToFirst();
        ArrayList<String> alBAccounts= new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alBAccounts.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alBAccounts.toArray(new String[alBAccounts.size()]);
    }

    public String[] fetchNotBuyingRemarks() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT Reason FROM Reason WHERE Category IN (2,3)", null);
        cursor.moveToFirst();
        ArrayList<String> alBAccounts= new ArrayList<String>();
        while (!cursor.isAfterLast()) {
            alBAccounts.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return alBAccounts.toArray(new String[alBAccounts.size()]);
    }

    public String fetchPromoType() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT PromoType FROM PromoH", null);
        cursor.moveToFirst();
        String promoType;

        try {
            promoType = cursor.getString(0);

        } catch (Exception e) {
            promoType = "";
        }
        return promoType;

    }

    public String fetchPromoItem() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.Name FROM PromoH A INNER JOIN Materials B ON RTRIM(A.MatCode) = B.MaterialCode WHERE A.PromoType = \"DISC\"", null);
        cursor.moveToFirst();
        String promoItem;

        try {
            promoItem = cursor.getString(0);

        } catch (Exception e) {
            promoItem = "";
        }
        return promoItem;

    }

    public String fetchPromoBundleItem() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT B.Name FROM PromoD A INNER JOIN Materials B ON RTRIM(A.MatCode) = B.MaterialCode", null);
        cursor.moveToFirst();
        String promoItem;

        try {
            promoItem = cursor.getString(0);

        } catch (Exception e) {
            promoItem = "";
        }
        return promoItem;

    }

    public int fetchCountMPOSPromoCustomer() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM MPOSPromoCustomer WHERE CustCode = '" + PCCode + "'", null);
        cursor.moveToFirst();

        int ftchCountCalls;
        if (cursor.getCount() > 0){
            ftchCountCalls = cursor.getInt(0);
        }else{
            ftchCountCalls = 0;
        }

        return  ftchCountCalls;
    }

    public void insertCustomerLogs(String LogID,String CustomerCode,String LogDateTime,int isFinish) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("LogID", LogID);
        cv.put("CustomerCode", CustomerCode);
        cv.put("LogDateTime", LogDateTime);
        cv.put("IsFinish", isFinish);
        db.insert("CustomerLogs", null, cv);
    }

    public void insertCustomerLogsItem(String LogID,int TXN,String LogDateTime,int IsSkip) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("LogID", LogID);
        cv.put("TXN", TXN);
        cv.put("LogDateTime", LogDateTime);
        cv.put("IsSkip", IsSkip);
        db.insert("CustomerLogsItem", null, cv);
    }

    public int fetchActiveLogs() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM CustomerLogs WHERE IsFinish = 0", null);
        cursor.moveToFirst();
        int count= cursor.getInt(0);
        cursor.close();
        return count;

    }

    /*public String fetchActiveCustomerLogs() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.CustomerCode , B.CustomerName FROM CustomerLogs A INNER JOIN Customer B ON A.CustomerCode = B.CustomerCode WHERE IsFinish = 0", null);
        cursor.moveToFirst();
        String customerCode = ("(" + cursor.getString(0) + ") - " + cursor.getString(1));
        cursor.close();
        return customerCode;

    }*/

    public String fetchLogID() {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.LogID FROM CustomerLogs A WHERE A.IsFinish = 0", null);
        cursor.moveToFirst();
        String LogID ;
        try {
            LogID = cursor.getString(0);

        } catch (Exception e) {
            LogID = "";
        }

        cursor.close();
        return LogID;

    }

    public String fetchUnit(String productName) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Unit FROM Materials A WHERE A.Name ='" + productName + "'", null);
        cursor.moveToFirst();
        String customerCode = (cursor.getString(0));
        cursor.close();
        return customerCode;

    }



    public ArrayList<HashMap<String, String>> fetchActiveCustomerLogs() {
        ArrayList<HashMap<String, String>> fetch_activeCustomerLogs;
        fetch_activeCustomerLogs = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.CustomerCode , B.CustomerName ,C.TXN FROM CustomerLogs A INNER JOIN Customer B ON A.CustomerCode = B.CustomerCode INNER JOIN CustomerLogsItem C ON A.LogID = C.LogID WHERE IsFinish = 0 ORDER BY C.TXN DESC LIMIT 1";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("CustomerCode", cursor.getString(0));
                map.put("CustomerName", cursor.getString(1));
                map.put("Txn", cursor.getString(2));
                fetch_activeCustomerLogs.add(map);
            } while (cursor.moveToNext());
        }
        return fetch_activeCustomerLogs;

    }
    public void insertCustomerCheckin(String CheckInDt,String CheckInTm,String CheckInLoc,String CustomerCode,String Remarks) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CHECKINDT", CheckInDt);
        cv.put("CHECKINTM", CheckInTm);
        cv.put("CHECKINLOC", CheckInLoc);
        cv.put("CustomerCode", CustomerCode);
        cv.put("Remarks", Remarks);
        db.insert("CheckInOut", null, cv);

    }

    public void updateCheckOut(String CheckOutDate,String CheckOutTm,String CheckOutLoc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CHECKOUTDT", CheckOutDate);
        cv.put("CHECKOUTTM", CheckOutTm);
        cv.put("CHECKOUTLOC", CheckOutLoc);
        db.update("CheckInOut", cv, "CustomerCode = '" + PCCode + "'", null);
    }

    public void updateCustomerLogs() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("IsFinish", 1);
        db.update("CustomerLogs", cv, "LogID = '" + fetchLogID()  + "'", null);
    }

        public int fetchCustomerExists(String CustomerCode) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM CustomerLogs A WHERE A.CustomerCode = '" + CustomerCode + "'", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;

    }

    public void insertCustomerInventory(String CustomerCode,String InventoryDate,String ID,String InventoryBy) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode",CustomerCode );
        cv.put("InventoryDate", InventoryDate);
        cv.put("ID", ID);
        cv.put("InventoryBy", InventoryBy);
        db.insert("CustomerInventory", null, cv);

    }

    public void insertCustomerInventoryItem(String ID,String MaterialCode,String Qty,String ExpDate) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ID", ID);
        cv.put("MaterialCode",MaterialCode );
        cv.put("Qty", Qty);
        cv.put("ExpDate", ExpDate);
        db.insert("CustomerInventoryItem", null, cv);

    }

    public void insertCheckDisplay(String CustomerCode,String ID,String TransactionDate,String TransactionBy) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("CustomerCode",CustomerCode );
        cv.put("ID", ID);
        cv.put("TransactionDate", TransactionDate);
        cv.put("TransactionBy", TransactionBy);
        db.insert("CheckDisplayH", null, cv);

    }

    public void insertCheckDisplayItem(String ID,String DisplayType,String AssetNo,byte[] Image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ID", ID);
        cv.put("DisplayType", DisplayType);
        cv.put("AssetNo", AssetNo);
        cv.put("Image", Image);
        db.insert("CheckDisplayD", null, cv);

    }

    public ArrayList<HashMap<String, String>> fetchNameUnitMatQR(String matcode) {
        ArrayList<HashMap<String, String>> ftchMat;
        ftchMat = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT A.Name,A.Unit FROM Materials A INNER JOIN PricingList B ON A.MaterialCode = B.MaterialCode WHERE A.MaterialCode='" + matcode +"' AND B.PriceList ='" + fetchDefaultPListList()+ "'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("Name", cursor.getString(0));
                map.put("Unit", cursor.getString(1));
                ftchMat.add(map);
            } while (cursor.moveToNext());
        }
        return ftchMat;
    }

    public int fetchStockTransfeID(String StockTransferId) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM StockReceiving A WHERE A.Id = '" + StockTransferId + "'", null);
        cursor.moveToFirst();
        return  cursor.getInt(0);

    }

    public ArrayList<HashMap<String, String>> printSCollectionTurnOver() {
        ArrayList<HashMap<String, String>> fetchCollectionTurnOver;
        fetchCollectionTurnOver = new ArrayList<HashMap<String, String>>();
        String selectQuery = "SELECT SUBSTR(A.SalesDateTime,1,10)  AS SalesDateTime,SUBSTR(A.InvoiceNo,1,10) AS Invoice ,substr(B.CustomerName, instr(B.CustomerName, '-') + 1) AS CustomerName,A.AmtDue FROM Sales A  INNER JOIN Customer B ON B.CustomerCode =  CASE WHEN A.Remarks = '' THEN   A.CustomerCode ELSE A.Remarks END ORDER BY A.SalesDateTime";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("SalesDateTime", cursor.getString(0));
                map.put("Invoice", cursor.getString(1));
                map.put("CustomerName", cursor.getString(2));
                DecimalFormat SOAmt = new DecimalFormat("#,##0.00");
                map.put("AmtDue", SOAmt.format(cursor.getDouble(3)));
                fetchCollectionTurnOver.add(map);
            } while (cursor.moveToNext());
        }
        return fetchCollectionTurnOver;
    }

    public String fetchLocation(String customerCode) {

        Cursor cursor = getReadableDatabase().rawQuery("SELECT A.Location FROM Customer A WHERE A.CustomerCode ='" + customerCode + "'", null);
        cursor.moveToFirst();
        String location = (cursor.getString(0));
        cursor.close();
        return location;

    }


}