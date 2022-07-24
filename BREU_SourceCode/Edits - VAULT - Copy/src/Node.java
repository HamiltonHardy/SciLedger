
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Node Class: Creates the nodes that make up the blockchain network.
 */

public class Node {

    private final int NODE_ID;
    private final ArrayList<Block> BLOCKCHAIN = new ArrayList<>();
    private final ArrayList<Node> PEERS = new ArrayList<>();
    private final ArrayList<Transaction> PENDING_TRANSACTIONS = new ArrayList<>();

    /**
     * Constructor: Assigns an ID to the node and creates the starting blockchain for the node
     * which consists of only the genesis block. The ID of the node is just what number it is in the network.
     * ex. 1st block = 1, 2nd block = 2...
     */
    public Node(){
        this.NODE_ID = Main.NETWORK.size() + 1;
        this.BLOCKCHAIN.add(Main.GENESIS_BLOCK);
    }

    /**
     * Takes the arraylist of provenance data and makes a transaction out of it
     * @param provenanceData The information about the workflow task
     * @return The resulting Transaction object
     */
    public Transaction createTransaction(ArrayList<String> provenanceData) {
        return new Transaction(this.NODE_ID, provenanceData);
    }

    /**
     *TODO: Is there a more efficient way to check the nodes in the network? Do we ever remove nodes, is it ok to just check the size and make sure Id is less?
     * Do we need to broadcast? And do we need to remove the first vote?
     */
    public void validateTransactions() {
        //Ensure node is a member of the quorum
        if (Main.quorum.getNODES().contains(this)) {
            for (Transaction transaction : this.PENDING_TRANSACTIONS) {
                //A transaction is valid if the node ID is a node in the network. Vote is true for valid, false for invalid.
                boolean vote = ((transaction.getUSER_ID() < Main.NETWORK.size()) && (transaction.getUSER_ID() > 0));
                //Check if the transaction is pending for the rest of the quorum, if not broadcast transaction
                for (Node quorumNode : Main.quorum.getNODES()) {
                    if (!quorumNode.getPENDING_TRANSACTIONS().contains(transaction)) {
                        quorumNode.getPENDING_TRANSACTIONS().add(transaction);
                    }
                }
                //?
                Main.quorum.getVOTES().remove(0);
                Main.quorum.getVOTES().add(vote);
            }
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
    public void validateBlock(double quorumThreshold) {
        //Tally votes rejecting the transaction and calculate percentage
        int transactionRejectionCount = 0;
        for (Boolean vote : Main.quorum.getVOTES()) {
            if (!vote) {
                transactionRejectionCount++;
            }
        }
        double percentRejected = (double)transactionRejectionCount / (double)Main.quorum.getSIZE();

        //If threshold is not met, remove all invalid transactions from pending transactions lists for all quorum members
        if (percentRejected >= quorumThreshold) {
            System.out.println("Block validation failed - Attempting to remove Bad TXs and rebroadcast for validaton\n Rejected " + percentRejected);

            //? should you check them all before calling validate
            for (int i = 0; i < this.PENDING_TRANSACTIONS.size(); i++) {
                if ((this.PENDING_TRANSACTIONS.get(i).getUSER_ID() > Main.NETWORK.size()) || (this.PENDING_TRANSACTIONS.get(i).getUSER_ID() < 1)) {
                    for (Node node : Main.quorum.getNODES()) {
                        node.getPENDING_TRANSACTIONS().remove(i);
                        node.validateTransactions();
                    }
                    this.validateBlock(quorumThreshold);
                }
            }
        }
        // If threshold is met, create a block and add to the blockchain
        else {
            for (Transaction transaction : this.PENDING_TRANSACTIONS) {
                ArrayList<String> hashes = new ArrayList<>();
                hashes.add(this.BLOCKCHAIN.get(this.BLOCKCHAIN.size() - 1).getHASH());
                this.BLOCKCHAIN.add(new Block(transaction, hashes , this.BLOCKCHAIN.size() + 1, Main.quorum.getVOTES()));
            }
            //? Should this be cleared?
            this.PENDING_TRANSACTIONS.clear();
            //Broadcast block to network (node now has longest chain) Nodes check if block in longest chain has valid Quorum Signature
            for (Node node : Main.NETWORK) {
                node.updateLocalLedger(quorumThreshold);
            }
            System.out.println("CURRENT BLOCKCHAIN SIZE " + this.BLOCKCHAIN.size());
        }
    }

    /**
     * Function to propagate the given transaction throughout the network
     * @param transaction The transaction to be propagated
     */
    public void propagateTransaction(Transaction transaction) {
        //Node adds transaction to its current pending transactions if it is not already there
        if (!this.PENDING_TRANSACTIONS.contains(transaction)) {
            this.PENDING_TRANSACTIONS.add(transaction);
        }
        //Node does the same for all peer nodes in the network
        for (Node peer : this.PEERS) {
            if (!peer.getPENDING_TRANSACTIONS().contains(transaction)) {
                peer.getPENDING_TRANSACTIONS().add(transaction);
                //Peers continue to propagate the transaction
                peer.propagateTransaction(transaction);
            }
        }
    }

    /**
     * Function updates all nodes in the network with the most recent version of the blockchain
     *
     * TODO: Issue, we check the votes on one block, but add all new blocks? Also, Last note seems to make this code no good
     */
    public void updateLocalLedger(double quorumThreshold) {
        Node mostCurrentNode = this;

        //Find the most up-to-date version of the blockchain (the longest)
        for (Node node : Main.NETWORK) {
            if (node.getBLOCKCHAIN().size() > this.BLOCKCHAIN.size()) {
                mostCurrentNode = node;
            }
        }

        //If the most current node is self, do nothing
        if (mostCurrentNode.NODE_ID != this.NODE_ID) {

            //Get the most current blockchain and block from the most current node
           ArrayList<Block> mostCurrentBlockChain = mostCurrentNode.getBLOCKCHAIN();
           Block mostCurrentBlock = mostCurrentBlockChain.get(mostCurrentBlockChain.size()-1);

            //Verify again that the number of approval votes reaches the threshold
            int transactionRejectionCount = 0;
            for (Boolean vote : mostCurrentBlock.getVOTES()) {
                if (!vote) {
                    transactionRejectionCount++;
                }
            }
            double percentRejected = (double)transactionRejectionCount / (double)Main.quorum.getSIZE();
            if (percentRejected < quorumThreshold) {

                //Add all blocks that longest blockchain has that are not on the local blockchain
                for (int i = this.BLOCKCHAIN.size(); i < mostCurrentBlockChain.size(); i++) {
                    this.BLOCKCHAIN.add(mostCurrentBlockChain.get(i));
                }

                //*note* in reality we want to remove only PENDING_TRANSACTIONS transactions that are already in the blockchain
                //for scalability experiment, PENDING_TRANSACTIONS will always match, so just clear PENDING_TRANSACTIONS.
                this.PENDING_TRANSACTIONS.clear();
            }


        }

    }

    public ArrayList<Transaction> getPENDING_TRANSACTIONS() {
        return PENDING_TRANSACTIONS;
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