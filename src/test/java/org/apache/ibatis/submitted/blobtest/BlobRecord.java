package org.apache.ibatis.submitted.blobtest;

public class BlobRecord {
    private int id;
    private byte[] blob;
    
    public BlobRecord(int id, byte[] blob) {
        super();
        this.id = id;
        this.blob = blob;
    }

    public int getId() {
        return id;
    }
    
    public byte[] getBlob() {
        return blob;
    }
}
