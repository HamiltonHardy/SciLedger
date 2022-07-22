
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Justin Gazsi
 */
//Mimic behavior of a Node
public class Node {

    //Class variables
    private int nodeID;
    private ArrayList<Block> blockchain = new ArrayList<Block>();
    private ArrayList<Node> peers = new ArrayList<Node>();
    private ArrayList<Transaction> memPool = new ArrayList<Transaction>();

    //Constructor
    public Node() throws InterruptedException {
        //Inizalize local Blockchain ledger Genesis Block
        this.blockchain.add(DataStorage.GenBlock);

        //assign NodeID
        this.nodeID = DataStorage.Nodes.size() + 1;

    }

    // ***** FUNCTIONS *****//

    public Transaction createTransaction(ArrayList<String> provenanceData) {
        Transaction tx = new Transaction(this.nodeID, provenanceData);
        return tx;
    }

    /**
     * If node is in the quorum, for each transaction in the mempool check the nodeID to validate. Vote true if all good and vote false if even one is bad. Make sure transactions are in all the mempools, if not sync.
     */
    public void validateBlock() {
        boolean nodeVote = true;
        //Check if node is in Quorum
        if (DataStorage.Quorum.getQuroumGroup().contains(this)) {
            //System.out.print("I am node: " + this.nodeID + " \tI am in the quorum - ");
            //For each transaction in the mempool, if the transactionID is one of the nodeIDs then it is "found" meaning it is good
//            System.out.println("Validate Block Mempool: " + Arrays.toString(this.memPool.toArray()));
//            System.out.println("Validate Block MEMPOOL SIZE: " + this.memPool.size());
            for (Transaction tx : this.memPool) {
                boolean txIsFound = false;
                for (int j = 0; j < DataStorage.Nodes.size(); j++) {
                    if (tx.getuID() == DataStorage.Nodes.get(j).getNodeID()) {
                        txIsFound = true;
                    }

                }
                if (!txIsFound) {
                    nodeVote = false; //Bad transaction found! Do not vote for the block
                }
                //Check if transactions are in node mempools, if not mempools need to be synged, broadcast to other mempools
                for (Node node: DataStorage.Quorum.getQuroumGroup()) {
                    if (!node.getMemPool().contains(tx)) {
//                        System.out.println("PRINT TRANSACTION vs Mempool");
//                        System.out.println(tx);
//                        System.out.println(Arrays.toString(node.getMemPool().toArray()));
                        node.getMemPool().add(tx);
                    }
                }

            }
            //Set the node vote
//            System.out.println("I voted: " + nodeVote);
            DataStorage.Quorum.getVotes().remove(0);
            DataStorage.Quorum.getVotes().add(nodeVote);

        }

    }


// Original

//    public void proposeBlock() {
//        //If the node vote has a "false" ...
//        if (DataStorage.Quorum.getVotes().contains(false)) {
//            System.out.println("Block validation failed - Attempting to remove Bad TXs and rebroadcast for validaton\n");
//
//            //search through mempool and check for invalid transactions (i.e. nodes not members of the network - invalid nodeID)
//            for (int i = 0; i < this.memPool.size(); i++) {
//                if ((this.memPool.get(i).getNodeID() > DataStorage.Nodes.size()) || (this.memPool.get(i).getNodeID() < 1)) {
//                    //bad transaction found, Call on quorum to remove bad transaction and revalidate new block
//                    for (Node node : DataStorage.Quorum.getQuroumGroup()) {
//                        node.getMemPool().remove(i);
//                        node.validateBlock();
//
//                    }
//                    this.proposeBlock();
//                }
//            }
//        }
//        // If node vote is good (block is good) create new block from the mempool
//        else {  //Block is good, add Block to local ledger, clear MemPool
//            this.blockchain.add(new Block(this.memPool, this.blockchain.get(this.blockchain.size() - 1)
//                    .getHash(), this.blockchain.size() + 1, DataStorage.Quorum.getVotes()));
//
//            System.out.println("Successfully added Block. Blockchain length: " + blockchain.size());
//
//            this.memPool.clear();
//
//            //Broadcast block to network (node now has longest chain) Nodes check if block in longest chain has valid Quorum Signature
//            for (Node node : DataStorage.Nodes) {
//                node.getLongestChain();
//            }
//        }
//
//    }

////MINE #1 : 1 TXN per block
//
//    public void proposeBlock() {
//        //This value is 5. Why?
//        System.out.println("Quorum votes");
//        System.out.println(DataStorage.Quorum.getVotes().toString());
//
//        //If the node vote has a "false" ...
//        if (DataStorage.Quorum.getVotes().contains(false)) {
//            System.out.println("Block validation failed - Attempting to remove Bad TXs and rebroadcast for validaton\n");
//
//            //search through mempool and check for invalid transactions (i.e. nodes not members of the network - invalid nodeID)
//            for (int i = 0; i < this.memPool.size(); i++) {
//                if ((this.memPool.get(i).getNodeID() > DataStorage.Nodes.size()) || (this.memPool.get(i).getNodeID() < 1)) {
//                    //bad transaction found, Call on quorum to remove bad transaction and revalidate new block
//                    for (Node node : DataStorage.Quorum.getQuroumGroup()) {
//                        node.getMemPool().remove(i);
//                        node.validateBlock();
//
//                    }
//                    this.proposeBlock();
//                }
//            }
//        }
//        // If node vote is good (block is good) create new block from the mempool
//        else {  //Block is good, add Block to local ledger, clear MemPool
//            System.out.println("(before) CURRENT BLOCKCHAIN SIZE " + this.blockchain.size());
//            System.out.println("Propose Block MEMPOOL SIZE: " + memPool.size());
//            for (Transaction transaction : this.memPool){
//                //Create ArrayList<Transaction> for a single transaction
//                ArrayList<Transaction> singleTransaction = new ArrayList<>();
//                singleTransaction.add(transaction);
//
//                this.blockchain.add(new Block(singleTransaction, this.blockchain.get(this.blockchain.size() - 1)
//                        .getHash(), this.blockchain.size() + 1, DataStorage.Quorum.getVotes()));
//
////                System.out.println("Successfully added Block. Blockchain length: " + this.blockchain.size());
//            }
//
//            this.memPool.clear();
//
//            //Broadcast block to network (node now has longest chain) Nodes check if block in longest chain has valid Quorum Signature
//            for (Node node : DataStorage.Nodes) {
//                node.getLongestChain();
//            }
//            System.out.println("CURRENT BLOCKCHAIN SIZE " + this.blockchain.size());
//            System.out.println(this.blockchain.toString());
//
//        }
//    }

    /**
     * Checks whether the votes overall have any "false". If yes, check the entire mempool for invalid transactions
     * (nodeID no good) and when one is found remove it from the mempool of each quorum member. Call validate block
     * on each node and then call propose block again
     *
     * If no bad votes, for ever transaction in the mempool, add a new block for only that transaction. Clear the mempool
     * For ever node, make sure they have the updated chain after adding all 60 blocks
     */
    //MINE #2 : 1 TXN per block AND ...
    public void proposeBlock(double quorumThreshold) {

        //If the node vote has a "false" ...
        int badVoteCount = 0;
        for(Boolean vote : DataStorage.Quorum.getVotes()){
            if(!vote){
                badVoteCount ++;
            }
        }
        double percentBadVotes = badVoteCount/DataStorage.Quorum.getVotes().size();

        //Check whether threshold is met
        if (percentBadVotes > quorumThreshold) {
            System.out.println("Block validation failed - Attempting to remove Bad TXs and rebroadcast for validaton\n");

            //search through mempool and check for invalid transactions (i.e. nodes not members of the network - invalid nodeID)
            for (int i = 0; i < this.memPool.size(); i++) {
                if ((this.memPool.get(i).getuID() > DataStorage.Nodes.size()) || (this.memPool.get(i).getuID() < 1)) {
                    //bad transaction found, Call on quorum to remove bad transaction and revalidate new block
                    for (Node node : DataStorage.Quorum.getQuroumGroup()) {
                        node.getMemPool().remove(i);
                        node.validateBlock();

                    }
                    this.proposeBlock(quorumThreshold);
                }
            }
        }
        // If node vote is good (block is good) create new block from the mempool
        else {  //Block is good, add Block to local ledger, clear MemPool
            System.out.println("(before) CURRENT BLOCKCHAIN SIZE " + this.blockchain.size());
//            System.out.println("Propose Block MEMPOOL SIZE: " + memPool.size());
            for (Transaction transaction : this.memPool){
                //Create ArrayList<Transaction> for a single transaction
                ArrayList<Transaction> singleTransaction = new ArrayList<>();
                singleTransaction.add(transaction);

                System.out.println("Transaction Provenance tID, wID: " + transaction.gettID() + ", " + transaction.getwID() );

                this.blockchain.add(new Block(singleTransaction, this.blockchain.get(this.blockchain.size() - 1)
                        .getHash(), this.blockchain.size() + 1, DataStorage.Quorum.getVotes()));

//                System.out.println("Successfully added Block. Blockchain length: " + this.blockchain.size());
            }

            this.memPool.clear();

            //Broadcast block to network (node now has longest chain) Nodes check if block in longest chain has valid Quorum Signature
            for (Node node : DataStorage.Nodes) {
                node.getLongestChain();
            }
            System.out.println("CURRENT BLOCKCHAIN SIZE " + this.blockchain.size());
//            System.out.println(this.blockchain.toString());

        }
    }


    //Function to broadcast transaction through network
    public void broadcastTransaction(Transaction tx) {

        if (!this.memPool.contains(tx)) {
            this.memPool.add(tx);
        }
        //broadcast transaction to connected peers
        for (Node peer : this.peers) {
            if (!peer.getMemPool().contains(tx)) {
                peer.getMemPool().add(tx);
                peer.broadcastTransaction(tx); //peers recursively propogate through network
            }
        }

    }


//Original: only propagated the last new block to the network

//    public void getLongestChain() {
//
//
//        int maxID = this.nodeID;
//        for (Node node : DataStorage.Nodes) {  //Find node with longest blockchain
//            if (node.getBlockchain().size() > this.blockchain.size()) {
//                maxID = node.getNodeID();
//            }
//        }
//
//        if (maxID != this.nodeID) { //Make sure longest chain is not self
//            //check that quorum voted true
//            if(!DataStorage.Nodes.get(maxID - 1)
//                    .getBlockchain().get(DataStorage.Nodes.get(maxID - 1)
//                            .getBlockchain().size() - 1).getQVotes()
//                    .contains(false))
//            {
//                //Quorum Signature succesfully validated, clear mempool and add latest block to local ledger
//                this.blockchain.add(DataStorage.Nodes.get(maxID - 1)
//                        .getBlockchain().get(DataStorage.Nodes.get(maxID - 1).getBlockchain().size() - 1));
//
//                //*note* in reality we want to remove only mempool transactions that are already in the blockchain
//                //for scalability experiment, mempools will always match, so just clear mempool.
//                this.memPool.clear();
//            }
//
//
//
//        }
//
//    }

//Mine: propagates all new blocks (1 per transaction (60) to the chain)

    public void getLongestChain() {


        int maxID = this.nodeID;
        for (Node node : DataStorage.Nodes) {  //Find node with longest blockchain
            if (node.getBlockchain().size() > this.blockchain.size()) {
                maxID = node.getNodeID();
            }
        }

        if (maxID != this.nodeID) { //Make sure longest chain is not self
            //check that quorum voted true
            if(!DataStorage.Nodes.get(maxID - 1)
                    .getBlockchain().get(DataStorage.Nodes.get(maxID - 1)
                            .getBlockchain().size() - 1).getQVotes()
                    .contains(false))
            {

//                int blockchainSizeDifference = DataStorage.Nodes.get(maxID - 1).getBlockchain().size() - this.blockchain.size();
//                System.out.println("Current node BC size " + this.blockchain.size());
//                System.out.println("Longest BC size " + DataStorage.Nodes.get(maxID - 1).getBlockchain().size());
//                System.out.println("Difference " + blockchainSizeDifference);

                //Quorum Signature succesfully validated, clear mempool and add latest blocks to local ledger
                //Start index at the size of the non-updated blockchain
                //End one before the size of the updated (larger) blockchain
                for(int i = this.blockchain.size(); i<DataStorage.Nodes.get(maxID - 1).getBlockchain().size(); i++){
                    this.blockchain.add(DataStorage.Nodes.get(maxID - 1)
                            .getBlockchain().get(i));
                }

                //*note* in reality we want to remove only mempool transactions that are already in the blockchain
                //for scalability experiment, mempools will always match, so just clear mempool.
                this.memPool.clear();
            }



        }

    }

    //***** Getters and Setters ****//
    public ArrayList<Transaction> getMemPool() {
        return memPool;
    }

    public void setMemPool(ArrayList<Transaction> memPool) {
        this.memPool = memPool;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public ArrayList<Node> getPeers() {
        return peers;
    }

    public void setPeers(ArrayList<Node> peers) {
        this.peers = peers;
    }

    public void addPeer(Node node) {
        this.peers.add(node);
    }

    public ArrayList<Block> getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(ArrayList<Block> blockchain) {
        this.blockchain = blockchain;
    }

//    public int getQuorumID() {
//        return quorumID;
//    }
//
//    public void setQuorumID(int quorumID) {
//        this.quorumID = quorumID;
//    }
    // Function to check validity of the blockchain
//    public static Boolean isChainValid() {
//
//        Block currentBlock;
//        Block previousBlock;
//
//        // Iterating through all the blocks
//        for (int i = 1; i < blockchain.size(); i++) {
//
//            // Storing the current block and the previous block
//            currentBlock = blockchain.get(i);
//            previousBlock = blockchain.get(i - 1);
//
//            // Checking if the current hash is equal to the calculated hash or not
//            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
//                System.out.println("Hashes are not equal");
//                return false;
//            }
//
//            // Checking of the previous hash is equal to the calculated previous hash or not
//            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
//                System.out.println("Previous Hashes are not equal");
//                return false;
//            }
//        }
//
//        // If all the hashes are equal to the calculated hashes, then the blockchain is valid
//        return true;
//    }
}
