
import java.util.ArrayList;

/**
 * Node Class: Creates the nodes that make up the blockchain network.
 */

public class Node {

    private final int NODE_ID;
    private final ArrayList<Block> BLOCKCHAIN = new ArrayList<>();
    private final ArrayList<Node> PEERS = new ArrayList<>();
    private final ArrayList<Transaction> MEM_POOL = new ArrayList<>();

    /**
     * Constructor: Assigns an ID to the node and creates the starting blockchain for the node
     * which consists of only the genesis block.
     */
    public Node(){
        this.NODE_ID = Main.NETWORK.size() + 1;
        this.BLOCKCHAIN.add(Main.GENESIS_BLOCK);
    }

    /**
     * Takes the arraylist of provenance data and makes a transaction out of it
     * @param provenanceData The information about the workflow task
     * @return A Transaction
     */
    public Transaction createTransaction(ArrayList<String> provenanceData) {
        return new Transaction(this.NODE_ID, provenanceData);
    }

//    /**
//     * If node is in the quorum, for each transaction in the mempool check the nodeID to validate. Vote true if all good and vote false if even one is bad. Make sure transactions are in all the mempools, if not sync.
//     */

    /**
     *
     */
    public void validateBlock() {
        boolean nodeVote = true;
        if (Main.quorum.getNETWORK().contains(this)) {
            for (Transaction tx : this.MEM_POOL) {
                boolean txIsFound = false;
                for (int j = 0; j < Main.NETWORK.size(); j++) {
                    if (tx.getUSER_ID() == Main.NETWORK.get(j).getNODE_ID()) {
                        txIsFound = true;
                    }

                }
                if (!txIsFound) {
                    nodeVote = false; //Bad transaction found! Do not vote for the block
                }
                //Check if transactions are in node mempools, if not mempools need to be synged, broadcast to other mempools
                for (Node node : Main.quorum.getNETWORK()) {
                    if (!node.getMEM_POOL().contains(tx)) {
                        node.getMEM_POOL().add(tx);
                    }
                }

            }
            //Set the node vote
//            System.out.println("I voted: " + nodeVote);
            Main.quorum.getVOTES().remove(0);
            Main.quorum.getVOTES().add(nodeVote);

        }

    }


//    /**
//     * Checks whether the votes overall have any "false". If yes, check the entire mempool for invalid transactions
//     * (nodeID no good) and when one is found remove it from the mempool of each quorum member. Call validate block
//     * on each node and then call propose block again
//     * <p>
//     * If no bad votes, for ever transaction in the mempool, add a new block for only that transaction. Clear the mempool
//     * For ever node, make sure they have the updated chain after adding all 60 blocks
//     */

    /**
     *
     * @param quorumThreshold The percentage of the quorum that needs to approve a block in order for it to be committed
     *                        to the blockchainn.
     */
    public void proposeBlock(double quorumThreshold) {

        //If the node vote has a "false" ...
        int badVoteCount = 0;
        for (Boolean vote : Main.quorum.getVOTES()) {
            if (!vote) {
                badVoteCount++;
            }
        }
        double percentBadVotes = badVoteCount / Main.quorum.getSIZE();

        //Check whether threshold is met
        if (percentBadVotes > quorumThreshold) {
            System.out.println("Block validation failed - Attempting to remove Bad TXs and rebroadcast for validaton\n");

            //search through mempool and check for invalid transactions (i.e. NETWORK not members of the network - invalid nodeID)
            for (int i = 0; i < this.MEM_POOL.size(); i++) {
                if ((this.MEM_POOL.get(i).getUSER_ID() > Main.NETWORK.size()) || (this.MEM_POOL.get(i).getUSER_ID() < 1)) {
                    //bad transaction found, Call on quorum to remove bad transaction and revalidate new block
                    for (Node node : Main.quorum.getNETWORK()) {
                        node.getMEM_POOL().remove(i);
                        node.validateBlock();

                    }
                    this.proposeBlock(quorumThreshold);
                }
            }
        }
        // If node vote is good (block is good) create new block from the mempool
        else {  //Block is good, add Block to local ledger, clear MemPool
            System.out.println("(before) CURRENT BLOCKCHAIN SIZE " + this.BLOCKCHAIN.size());
            for (Transaction transaction : this.MEM_POOL) {
                //Create ArrayList<Transaction> for a single transaction
                ArrayList<Transaction> singleTransaction = new ArrayList<>();
                singleTransaction.add(transaction);

                System.out.println("Transaction Provenance tID, workflowID: " + transaction.getTASK_ID() + ", " + transaction.getWORKFLOW_ID());

                this.BLOCKCHAIN.add(new Block(singleTransaction, this.BLOCKCHAIN.get(this.BLOCKCHAIN.size() - 1)
                        .getHash(), this.BLOCKCHAIN.size() + 1, Main.quorum.getVOTES()));

//                System.out.println("Successfully added Block. Blockchain length: " + this.blockchain.size());
            }

            this.MEM_POOL.clear();

            //Broadcast block to network (node now has longest chain) Nodes check if block in longest chain has valid Quorum Signature
            for (Node node : Main.NETWORK) {
                node.getLongestChain();
            }
            System.out.println("CURRENT BLOCKCHAIN SIZE " + this.BLOCKCHAIN.size());
//            System.out.println(this.BLOCKCHAIN.toString());

        }
    }


    //Function to broadcast transaction through network

    /**
     *
     * @param tx
     */
    public void broadcastTransaction(Transaction tx) {

        if (!this.MEM_POOL.contains(tx)) {
            this.MEM_POOL.add(tx);
        }
        //broadcast transaction to connected PEERS
        for (Node peer : this.PEERS) {
            if (!peer.getMEM_POOL().contains(tx)) {
                peer.getMEM_POOL().add(tx);
                peer.broadcastTransaction(tx); //peers recursively propogate through network
            }
        }

    }

    /**
     *
     */
    public void getLongestChain() {


        int maxID = this.NODE_ID;
        for (Node node : Main.NETWORK) {  //Find node with longest BLOCKCHAIN
            if (node.getBLOCKCHAIN().size() > this.BLOCKCHAIN.size()) {
                maxID = node.getNODE_ID();
            }
        }

        if (maxID != this.NODE_ID) { //Make sure longest chain is not self
            //check that quorum voted true
            if (!Main.NETWORK.get(maxID - 1)
                    .getBLOCKCHAIN().get(Main.NETWORK.get(maxID - 1)
                            .getBLOCKCHAIN().size() - 1).getQVotes()
                    .contains(false)) {

                //Quorum Signature succesfully validated, clear MEM_POOL and add latest blocks to local ledger
                //Start index at the size of the non-updated blockchain
                //End one before the size of the updated (larger) blockchain
                for (int i = this.BLOCKCHAIN.size(); i < Main.NETWORK.get(maxID - 1).getBLOCKCHAIN().size(); i++) {
                    this.BLOCKCHAIN.add(Main.NETWORK.get(maxID - 1)
                            .getBLOCKCHAIN().get(i));
                }

                //*note* in reality we want to remove only MEM_POOL transactions that are already in the blockchain
                //for scalability experiment, MEM_POOLs will always match, so just clear MEM_POOL.
                this.MEM_POOL.clear();
            }


        }

    }

    public ArrayList<Transaction> getMEM_POOL() {
        return MEM_POOL;
    }

    public int getNODE_ID() {
        return NODE_ID;
    }

    public ArrayList<Node> getPEERS() {
        return PEERS;
    }

    public void addPeer(Node node) {
        this.PEERS.add(node);
    }

    public ArrayList<Block> getBLOCKCHAIN() {
        return BLOCKCHAIN;
    }

}