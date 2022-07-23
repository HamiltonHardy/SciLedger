
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * Node Class: Creates the nodes that make up the blockchain network.
 */

public class Node {

    private final int nodeID;
    private final ArrayList<Block> blockchain = new ArrayList<>();
    private final ArrayList<Node> peers = new ArrayList<>();
    private final ArrayList<Transaction> memPool = new ArrayList<>();

    /**
     * Constructor: Assigns an ID to the node and creates the starting blockchain for the node
     * which consists of only the genesis block.
     */
    public Node(){
        this.nodeID = Main.Nodes.size() + 1;
        this.blockchain.add(Main.genesisBlock);
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
        if (Main.quorum.getNODES().contains(this)) {
            //System.out.print("I am node: " + this.nodeID + " \tI am in the quorum - ");
            //For each transaction in the mempool, if the transactionID is one of the nodeIDs then it is "found" meaning it is good
//            System.out.println("Validate Block Mempool: " + Arrays.toString(this.memPool.toArray()));
//            System.out.println("Validate Block MEMPOOL SIZE: " + this.memPool.size());
            for (Transaction tx : this.memPool) {
                boolean txIsFound = false;
                for (int j = 0; j < Main.Nodes.size(); j++) {
                    if (tx.getUSER_ID() == Main.Nodes.get(j).getNodeID()) {
                        txIsFound = true;
                    }

                }
                if (!txIsFound) {
                    nodeVote = false; //Bad transaction found! Do not vote for the block
                }
                //Check if transactions are in node mempools, if not mempools need to be synged, broadcast to other mempools
                for (Node node : Main.quorum.getNODES()) {
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
            Main.quorum.getVOTES().remove(0);
            Main.quorum.getVOTES().add(nodeVote);

        }

    }


    /**
     * Checks whether the votes overall have any "false". If yes, check the entire mempool for invalid transactions
     * (nodeID no good) and when one is found remove it from the mempool of each quorum member. Call validate block
     * on each node and then call propose block again
     * <p>
     * If no bad votes, for ever transaction in the mempool, add a new block for only that transaction. Clear the mempool
     * For ever node, make sure they have the updated chain after adding all 60 blocks
     */
    //MINE #2 : 1 TXN per block AND ...
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

            //search through mempool and check for invalid transactions (i.e. nodes not members of the network - invalid nodeID)
            for (int i = 0; i < this.memPool.size(); i++) {
                if ((this.memPool.get(i).getUSER_ID() > Main.Nodes.size()) || (this.memPool.get(i).getUSER_ID() < 1)) {
                    //bad transaction found, Call on quorum to remove bad transaction and revalidate new block
                    for (Node node : Main.quorum.getNODES()) {
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
            for (Transaction transaction : this.memPool) {
                //Create ArrayList<Transaction> for a single transaction
                ArrayList<Transaction> singleTransaction = new ArrayList<>();
                singleTransaction.add(transaction);

                System.out.println("Transaction Provenance tID, workflowID: " + transaction.getTASK_ID() + ", " + transaction.getWORKFLOW_ID());

                this.blockchain.add(new Block(singleTransaction, this.blockchain.get(this.blockchain.size() - 1)
                        .getHash(), this.blockchain.size() + 1, Main.quorum.getVOTES()));

//                System.out.println("Successfully added Block. Blockchain length: " + this.blockchain.size());
            }

            this.memPool.clear();

            //Broadcast block to network (node now has longest chain) Nodes check if block in longest chain has valid Quorum Signature
            for (Node node : Main.Nodes) {
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


//Mine: propagates all new blocks (1 per transaction (60) to the chain)

    public void getLongestChain() {


        int maxID = this.nodeID;
        for (Node node : Main.Nodes) {  //Find node with longest blockchain
            if (node.getBlockchain().size() > this.blockchain.size()) {
                maxID = node.getNodeID();
            }
        }

        if (maxID != this.nodeID) { //Make sure longest chain is not self
            //check that quorum voted true
            if (!Main.Nodes.get(maxID - 1)
                    .getBlockchain().get(Main.Nodes.get(maxID - 1)
                            .getBlockchain().size() - 1).getQVotes()
                    .contains(false)) {

//                int blockchainSizeDifference = Main.Nodes.get(maxID - 1).getBlockchain().size() - this.blockchain.size();
//                System.out.println("Current node BC size " + this.blockchain.size());
//                System.out.println("Longest BC size " + Main.Nodes.get(maxID - 1).getBlockchain().size());
//                System.out.println("Difference " + blockchainSizeDifference);

                //Quorum Signature succesfully validated, clear mempool and add latest blocks to local ledger
                //Start index at the size of the non-updated blockchain
                //End one before the size of the updated (larger) blockchain
                for (int i = this.blockchain.size(); i < Main.Nodes.get(maxID - 1).getBlockchain().size(); i++) {
                    this.blockchain.add(Main.Nodes.get(maxID - 1)
                            .getBlockchain().get(i));
                }

                //*note* in reality we want to remove only mempool transactions that are already in the blockchain
                //for scalability experiment, mempools will always match, so just clear mempool.
                this.memPool.clear();
            }


        }

    }

    public ArrayList<Transaction> getMemPool() {
        return memPool;
    }

    public int getNodeID() {
        return nodeID;
    }

    public ArrayList<Node> getPeers() {
        return peers;
    }

    public void addPeer(Node node) {
        this.peers.add(node);
    }

    public ArrayList<Block> getBlockchain() {
        return blockchain;
    }

}