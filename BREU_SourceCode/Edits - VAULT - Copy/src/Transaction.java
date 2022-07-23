import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Transaction Class: Creates the transactions that are submitted to the quorum. Each transaction corresponds to
 * an individual workflow task.
 */

public class Transaction {
    private final int userID;
    private final Timestamp timeStamp;
    private final String workflowID;
    private final String taskID;
    private final String validStatus;
    private final String parentTaskID;
    private final String merkleRoot;

    /**
     * Constructor
     * @param userID The ID for the node that ?
     * @param provenanceData An arraylist containing the provenance data for the given workflow task.
     */
    public Transaction(int userID, ArrayList<String> provenanceData) {
        this.userID = userID;
        this.timeStamp = new Timestamp(new Date().getTime());

        this.workflowID = provenanceData.get(0);
        this.taskID = provenanceData.get(1);
        this.validStatus = provenanceData.get(2);
        this.parentTaskID = provenanceData.get(3);
        this.merkleRoot = provenanceData.get(4);
    }

    public int getUserID() {
        return userID;
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