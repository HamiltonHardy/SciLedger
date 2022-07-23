import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Transaction Class: Creates the transactions that are submitted to the quorum. Each transaction corresponds to
 * an individual workflow task.
 */

public class Transaction {
    private final int USER_ID;
    private final Timestamp TIMESTAMP;
    private final String WORKFLOW_ID;
    private final String TASK_ID;
    private final String VALID_STATUS;
    private final String PARENT_TASK_ID;
    private final String MERKLE_ROOT;

    /**
     * Constructor
     * @param userID The ID for the node that ?
     * @param provenanceData An arraylist containing the provenance data for the given workflow task.
     */
    public Transaction(int userID, ArrayList<String> provenanceData) {
        this.USER_ID = userID;
        this.TIMESTAMP = new Timestamp(new Date().getTime());
        this.WORKFLOW_ID = provenanceData.get(0);
        this.TASK_ID = provenanceData.get(1);
        this.VALID_STATUS = provenanceData.get(2);
        this.PARENT_TASK_ID = provenanceData.get(3);
        this.MERKLE_ROOT = provenanceData.get(4);
    }

    public int getUSER_ID() {
        return USER_ID;
    }

    public Timestamp getTIMESTAMP() {
        return TIMESTAMP;
    }

    public String getWORKFLOW_ID() {
        return WORKFLOW_ID;
    }

    public String getTASK_ID() {
        return TASK_ID;
    }

    public String getVALID_STATUS() {
        return VALID_STATUS;
    }

    public String getPARENT_TASK_ID() {
        return PARENT_TASK_ID;
    }

    public String getMERKLE_ROOT() {
        return MERKLE_ROOT;
    }
}