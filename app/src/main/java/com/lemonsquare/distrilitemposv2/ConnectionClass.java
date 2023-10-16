package com.lemonsquare.distrilitemposv2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectionClass  {



    //String ip = "210.213.199.246";
    String classs = "net.sourceforge.jtds.jdbc.Driver";
    //String db = "SMSGATEWAY";
    //String un = "sa";
    //String password = "Distri91$";
    private  String ip;
    private String db;
    private String un;
    private String password;

    private Context context;
    public ConnectionClass(Context context) {
        this.context = context;
        DBController controller = new DBController(context);
        ip = controller.fetchdbSettings().get(2);
        db = controller.fetchdbSettings().get(3);
        un = controller.fetchdbSettings().get(4);
        password = controller.fetchdbSettings().get(5);

    }

    @SuppressLint("NewApi")
    public Connection CONN() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {

            Class.forName(classs);
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";user=" + un + ";password="
                    + password + ";";
            DriverManager.setLoginTimeout(5);
            conn = DriverManager.getConnection(ConnURL);
        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
        }
        return conn;
    }

}
