package com.example.woco;

public class wocoNames {

    private String name;

    private int syncStatus;

    public wocoNames(){}

    public wocoNames(String names, int syncStatus){
        this.name=names;
        this.syncStatus=syncStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
    }

}
