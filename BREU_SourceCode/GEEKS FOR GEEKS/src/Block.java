
// Java implementation for creating
// a block in a Blockchain

//Creating Blocks: To create a block, a Block class is implemented. In the class Block:
//        hash will contain the hash of the block and
//        previousHash will contain the hash of the previous block.
//        String data is used to store the data of the block and
//        “long timeStamp” is used to store the timestamp of the block.
//        Here long data type is used to store the number of milliseconds.
//        calculateHash() to generate the hash


import java.util.Date;

public class Block {

    // Every block contains
    // a hash, previous hash and
    // data of the transaction made
    public String hash;
    public String previousHash;
    private String data;
    private long timeStamp;

    // Constructor for the block
    public Block(String data,
                 String previousHash)
    {
        this.data = data;
        this.previousHash
                = previousHash;
        this.timeStamp
                = new Date().getTime();
        this.hash
                = calculateHash();
    }

    // Function to calculate the hash
    public String calculateHash()
    {
        // Calling the "crypt" class
        // to calculate the hash
        // by using the previous hash,
        // timestamp and the data
        String calculatedhash
                = crypt.sha256(
                previousHash
                        + Long.toString(timeStamp)
                        + data);

        return calculatedhash;
    }
}