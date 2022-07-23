import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Transaction Class: Creates the transactions that are submitted to the blockchain. Each transaction represents
 * a single workflow task.
 */

public class Transaction {
    private final int uID;
    private final Timestamp timeStamp;
    private final String workflowID;
    private final String taskID;
    private final String validStatus;
    private final String parentTaskID;
    private final String merkleRoot;

    public Transaction(int uID, ArrayList<String> provenanceData) {
        this.uID = uID;
        this.timeStamp = new Timestamp(new Date().getTime());

        this.workflowID = provenanceData.get(0);
        this.taskID = provenanceData.get(1);
        this.validStatus = provenanceData.get(2);
        this.parentTaskID = provenanceData.get(3);
        this.merkleRoot = provenanceData.get(4);
    }

    public int getuID() {
        return uID;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public String getworkflowID() {
        return workflowID;
    }

    public String gettaskID() {
        return taskID;
    }

    public String getValidStatus() {
        return validStatus;
    }

    public String getparentTaskID() {
        return parentTaskID;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }
}