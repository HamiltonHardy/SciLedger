import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Block Class: Creates the blocks that are committed to the block chain
 */
public class Block implements Serializable {
    private final String HASH;
    private final ArrayList<String> PARENT_HASHES;
    private final ArrayList<String> PROVENANCE_RECORD;
    private final int USER_ID;
    private final Timestamp TIMESTAMP;


    public Block(int userID, ArrayList<String> provenanceRecord, Block[] parentBlocks) {
        this.USER_ID = userID;
        this.PROVENANCE_RECORD = provenanceRecord;
        this.TIMESTAMP = new Timestamp(new Date().getTime());

        this.PARENT_HASHES = new ArrayList<>();
        for(int i = 0; i<parentBlocks.length; i++){
            Block block = parentBlocks[i];
            if(block != null) {
                this.PARENT_HASHES.add(block.getHASH());
            }
        }
        this.HASH = calculateHash();

    }
    /**
     * TODO
     * @return the hash of the current block
     */
    public String calculateHash() {
        return crypt.sha256( this.USER_ID + this.PROVENANCE_RECORD.toString() + this.TIMESTAMP.toString() + this.PARENT_HASHES.toString());
    }

    public String getHASH() {
        return HASH;
    }

    public ArrayList<String> getPROVENANCE_RECORD() {
        return PROVENANCE_RECORD;
    }
}

