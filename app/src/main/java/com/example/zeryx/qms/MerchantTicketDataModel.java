package com.example.zeryx.qms;

public class MerchantTicketDataModel {
    Integer ticketID;
    Integer userID;

    public MerchantTicketDataModel(Integer ticketID, Integer userID) {
        this.ticketID = ticketID;
        this.userID = userID;
    }

    public Integer getTicketID() {
        return ticketID;
    }

    public Integer getUserID() {
        return userID;
    }
}
