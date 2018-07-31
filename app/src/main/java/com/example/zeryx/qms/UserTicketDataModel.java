package com.example.zeryx.qms;

public class UserTicketDataModel {
    Integer ticketID;
    Integer queueID;

    public UserTicketDataModel(Integer ticketID, Integer merchantID) {
        this.ticketID = ticketID;
        this.queueID = merchantID;
    }

    public Integer getTicketID() {
        return ticketID;
    }

    public Integer getQueueID() {
        return queueID;
    }
}
