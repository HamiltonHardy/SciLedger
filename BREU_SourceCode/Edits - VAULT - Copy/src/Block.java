import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

/**
 * Block Class: Creates the blocks that are committed to the block chain
 */
public class Block {
    private final String HASH;
    private final ArrayList<String> PARENT_HASHES;
    private final Transaction TRANSACTION;
    private final Timestamp TIMESTAMP;
    private final int ID;
    private final ArrayList<Boolean> VOTES;


    public Block(Transaction transaction, ArrayList<String> parentHashes, int ID, ArrayList<Boolean> votes) {
        this.TRANSACTION = transaction;
        this.TIMESTAMP = new Timestamp(new Date().getTime());
        this.PARENT_HASHES = parentHashes;
        this.ID = ID;
        this.VOTES = votes;
        this.HASH = calculateHash();

    }
    /**
     * TODO
     * @return the hash of the current block
     */
    public String calculateHash() {
        return crypt.sha256(this.TRANSACTION + this.TIMESTAMP.toString() + this.ID + this.PARENT_HASHES);
    }

    public String getHASH() {
        return HASH;
    }

    public ArrayList<Boolean> getVOTES() {
        return VOTES;
    }
}

