import java.util.ArrayList;
import java.util.Date;

/**
 * Block Class: Creates the blocks that are committed to the block chain
 */
public class Block {

    //Blocks contain previous hash and data of transaction
    private String hash;
    private String previousHash;
    private Transaction transaction;
    private ArrayList<Transaction> txList = new ArrayList<Transaction>();
    private long timeStamp;
    private int blockNum;
    private ArrayList<Boolean> QVotes;

    /**
     *
     * @param TXs
     * @param prevHash
     * @param blockNum
     * @param QVotes
     */
    public Block(ArrayList<Transaction> TXs, String prevHash, int blockNum, ArrayList<Boolean> QVotes) {
        //this.transaction = tx;
        for (Transaction tx: TXs) {
            this.txList.add(tx);
        }
        this.timeStamp = new Date().getTime();

        if (TXs.get(0).getUserID() == -1) {
            this.previousHash = "0";
            //tx.setData("GENESIS BLOCK");// = "Genesis Block";
        } else {
            //this.previousHash = DataStorage.publicBlockchain.(size() - 1).hash;
            this.previousHash = prevHash;
        }
        this.QVotes = QVotes;
        this.blockNum = blockNum;
        this.hash = calculateHash();

    }

    public ArrayList<Boolean> getQVotes() {
        return QVotes;
    }


    public ArrayList<Transaction> getTxList() {
        return txList;
    }


    public int getBlockNum() {
        return blockNum;
    }

    public String getHash() {
        return hash;
    }


    public String getPreviousHash() {
        return previousHash;
    }

    public Transaction getTransaction() {
        return transaction;
    }


    public long getTimeStamp() {
        return timeStamp;
    }


    //Function to calculate Hash
    public String calculateHash() {
        //Calling crypt class
        String hash = crypt.sha256(txList + Long.toString(timeStamp) + blockNum + previousHash );
        return hash;
    }

}

