package com.example.zeryx.qms;

public class QueueDataModel {
    Integer queueID;
    String queueName;

    public QueueDataModel(Integer queueID, String queueName) {
        this.queueID = queueID;
        this.queueName = queueName;
    }

    public Integer getQueueID() {
        return queueID;
    }

    public String getQueueName() {
        return queueName;
    }
}
