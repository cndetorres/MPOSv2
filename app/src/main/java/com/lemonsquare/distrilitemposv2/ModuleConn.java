package com.lemonsquare.distrilitemposv2;

import android.content.Context;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class ModuleConn {

    ConnectionClass connectionClass;
    private Context context;

    public ModuleConn(Context context) {
        this.context = context;
    }


    public String  SP_INS_ORDR_STAGING(String PONo,String DeliveryDate,String Loc,String Order) throws SQLException {

        String returnString;

        connectionClass = new ConnectionClass(context);
        Connection con = connectionClass.CONN();

        if (con == null) {
            returnString = "No internet connection";

        }else{
            String query = "{CALL sp_insert_ordering ?,?,?,?,?}";
            CallableStatement stmt = con.prepareCall(query);
               stmt.setString(1, PONo);
            stmt.setString(2, DeliveryDate);
            stmt.setString(3, Loc);
            stmt.setString(4, Order);
            stmt.registerOutParameter(5, Types.VARCHAR);
            stmt.executeUpdate();
            returnString = stmt.getString(5);
            stmt.close();
            con.close();

        }
        return returnString;

    }

    /*public String fetchOrderNo(String PONo,String Location) throws SQLException{

        connectionClass = new ConnectionClass();
        Connection con = connectionClass.CONN();

        String order = "";
        if (con == null) {
            order = "No internet connection";

        }else{
            String query = "{CALL SP_FTCH_ORDRDNO ?,?,?}";
            CallableStatement stmt = con.prepareCall(query);
            stmt.setString(1,PONo);
            stmt.setString(2,Location);
            stmt.registerOutParameter(3, Types.VARCHAR);
            stmt.executeUpdate();
            order = stmt.getString(3);
            stmt.close();
            con.close();
        }


        return order;


    }*/

    /*public void SP_UPDATE_ORDRDNO(String PONo,String remarks) throws SQLException {



        connectionClass = new ConnectionClass();
        Connection con = connectionClass.CONN();

        String query = "{CALL sp_insert_ordering ?,?}";
        CallableStatement stmt = con.prepareCall(query);
        stmt.setString(1,PONo);
        stmt.setString(2,remarks);
        stmt.executeUpdate();

        stmt.close();
        con.close();

    }*/


}
