package com.lemonsquare.distrilitemposv2.Model;

public class SettingsList {

    String status;
    String salesDistrict;
    String sLoc;
    String terminalID;
    String databasePassword;
    String databaseUsername;
    String databaseName;
    String serverAddress;
    String lastOdometer;
    String plant;
    String contactNumberOrders;
    String contactNumberHeader;
    String contactNumberCustomerService;
    String movingAverageBuffer;
    String defaultPriceList;
    String officeAddress;
    String paymentTermDays;
    String minimumAmount;
    String smsGatewayNo;

    public SettingsList(){

    }

    public SettingsList(String status,String salesDistrict,String sLoc,String terminalID,String databasePassword,String databaseUsername,
                        String databaseName,String serverAddress,String lastOdometer,String plant,String contactNumberOrders,String contactNumberCustomerService,
                        String movingAverageBuffer,String defaultPriceList, String officeAddress,String paymentTermDays,String minimumAmount,String smsGatewayNo){

                        this.status = status;
                        this.salesDistrict = salesDistrict;
                        this.sLoc = sLoc;
                        this.terminalID = terminalID;
                        this.databasePassword = databasePassword;
                        this.databaseUsername = databaseUsername;
                        this.databaseName = databaseName;
                        this.serverAddress = serverAddress;
                        this.lastOdometer = lastOdometer;
                        this.plant = plant;
                        this.contactNumberOrders = contactNumberOrders;
                        this.contactNumberCustomerService = contactNumberCustomerService;
                        this.movingAverageBuffer = movingAverageBuffer;
                        this.defaultPriceList = defaultPriceList;
                        this.officeAddress = officeAddress;
                        this.paymentTermDays = paymentTermDays;
                        this.minimumAmount = minimumAmount;
                        this.smsGatewayNo = smsGatewayNo;

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSalesDistrict() {
        return salesDistrict;
    }

    public void setSalesDistrict(String salesDistrict) {
        this.salesDistrict = salesDistrict;
    }

    public String getsLoc() {
        return sLoc;
    }

    public void setsLoc(String sLoc) {
        this.sLoc = sLoc;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getLastOdometer() {
        return lastOdometer;
    }

    public void setLastOdometer(String lastOdometer) {
        this.lastOdometer = lastOdometer;
    }

    public String getPlant() {
        return plant;
    }

    public void setPlant(String plant) {
        this.plant = plant;
    }

    public String getContactNumberOrders() {
        return contactNumberOrders;
    }

    public void setContactNumberOrders(String contactNumberOrders) {
        this.contactNumberOrders = contactNumberOrders;
    }

    public String getContactNumberHeader() {
        return contactNumberHeader;
    }

    public void setContactNumberHeader(String contactNumberHeader) {
        this.contactNumberHeader = contactNumberHeader;
    }

    public String getContactNumberCustomerService() {
        return contactNumberCustomerService;
    }

    public void setContactNumberCustomerService(String contactNumberCustomerService) {
        this.contactNumberCustomerService = contactNumberCustomerService;
    }

    public String getMovingAverageBuffer() {
        return movingAverageBuffer;
    }

    public void setMovingAverageBuffer(String movingAverageBuffer) {
        this.movingAverageBuffer = movingAverageBuffer;
    }

    public String getDefaultPriceList() {
        return defaultPriceList;
    }

    public void setDefaultPriceList(String defaultPriceList) {
        this.defaultPriceList = defaultPriceList;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public String getPaymentTermDays() {
        return paymentTermDays;
    }

    public void setPaymentTermDays(String paymentTermDays) {
        this.paymentTermDays = paymentTermDays;
    }

    public String getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(String minimumAmount) {
        this.minimumAmount = minimumAmount;
    }

    public String getSmsGatewayNo() {
        return smsGatewayNo;
    }

    public void setSmsGatewayNo(String smsGatewayNo) {
        this.smsGatewayNo = smsGatewayNo;
    }
}
