// Java implementation to store
// blocks in an ArrayList

//Storing the blocks: Now, let us store the blocks in the ArrayList of Block type,
// along with their hash values by calling the constructor of the Block Class.

import java.util.ArrayList;



public class GFG {

    // ArrayList to store the blocks
    public static ArrayList<Block> blockchain
            = new ArrayList<Block>();

    // Driver code
    public static void main(String[] args)
    {
        // Adding the data to the ArrayList
        blockchain.add(new Block(
                "First block", "0"));
        blockchain.add(new Block(
                "Second block",
                blockchain
                        .get(blockchain.size() - 1)
                        .hash));

        blockchain.add(new Block(
                "Third block",
                blockchain
                        .get(blockchain.size() - 1)
                        .hash));

        blockchain.add(new Block(
                "Fourth block",
                blockchain
                        .get(blockchain.size() - 1)
                        .hash));

        blockchain.add(new Block(
                "Fifth block",
                blockchain
                        .get(blockchain.size() - 1)
                        .hash));

    }


    // Java implementation to check
// validity of the blockchain

//    Blockchain Validity: Finally, we need to check the validity of the BlockChain by
//    creating a boolean method to check the validity. This method will be implemented
//    in the “Main” class and checks whether the hash is equal to the calculated hash
//    or not. If all the hashes are equal to the calculated hashes, then the block is valid.
//    Below is the implementation of the validity:

    // Function to check
// validity of the blockchain
    public static Boolean isChainValid()
    {
        Block currentBlock;
        Block previousBlock;

        // Iterating through
        // all the blocks
        for (int i = 1;
             i < blockchain.size();
             i++) {

            // Storing the current block
            // and the previous block
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // Checking if the current hash
            // is equal to the
            // calculated hash or not
            if (!currentBlock.hash
                    .equals(
                            currentBlock
                                    .calculateHash())) {
                System.out.println(
                        "Hashes are not equal");
                return false;
            }

            // Checking of the previous hash
            // is equal to the calculated
            // previous hash or not
            if (!previousBlock
                    .hash
                    .equals(
                            currentBlock
                                    .previousHash)) {
                System.out.println(
                        "Previous Hashes are not equal");
                return false;
            }
        }

        // If all the hashes are equal
        // to the calculated hashes,
        // then the blockchain is valid
        return true;
    }

}

