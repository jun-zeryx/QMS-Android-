package com.example.zeryx.qms;

public class UserTicketDataModel {
    Integer ticketID;
    Integer queueID;
    Integer waitingQueue;

    public UserTicketDataModel(Integer ticketID, Integer merchantID) {
        this.ticketID = ticketID;
        this.queueID = merchantID;
        this.waitingQueue = waitingQueue;
    }

    public Integer getTicketID() {
        return ticketID;
    }

    public Integer getQueueID() {
        return queueID;
    }

    public Integer getWaitingQueue() {
        return waitingQueue;
    }
}
