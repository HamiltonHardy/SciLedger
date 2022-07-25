import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Block Class: Creates the blocks that are committed to the block chain
 */
public class Block implements Serializable {
    private final String HASH;
//    private final ArrayList<String> PARENT_HASHES;
//    private final Transaction TRANSACTION;
    private final ArrayList<String> PROVENANCE_RECORD;
    private final int USER_ID;
    private final Timestamp TIMESTAMP;
//    private final ArrayList<Boolean> VOTES;


    public Block(int userID, ArrayList<String> provenanceRecord) {
        this.USER_ID = userID;
        this.PROVENANCE_RECORD = provenanceRecord;
        this.TIMESTAMP = new Timestamp(new Date().getTime());
//        this.PARENT_HASHES = parentHashes;
//        this.VOTES = votes;
        this.HASH = calculateHash();

    }
    /**
     * TODO
     * @return the hash of the current block
     */
    public String calculateHash() {
        return crypt.sha256( this.USER_ID + this.PROVENANCE_RECORD.toString() + this.TIMESTAMP.toString());
//        return crypt.sha256( this.USER_ID + this.PROVENANCE_RECORD.toString() + this.TIMESTAMP.toString() + this.ID + this.PARENT_HASHES);
    }

    public String getHASH() {
        return HASH;
    }

//    public ArrayList<Boolean> getVOTES() {
//        return VOTES;
//    }
}

