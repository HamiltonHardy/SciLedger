
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
     *TODO: Is there a more efficient way to check the nodes in the network? Do we ever remove nodes, so could we just check the size and make sure Id is less?
     * Do we need to broadcast? And do we need to remove the first vote?
     */
    public void validateTransactions() {
        //Ensure node is a member of the quorum
        if (Main.quorum.getNODES().contains(this)) {
            for (Transaction transaction : this.PENDING_TRANSACTIONS) {
//                System.out.println("NODE ID: " + transaction.getUSER_ID());
                //A transaction is valid if the node ID is a node in the network. Vote is true for valid, false for invalid.
                boolean vote = false;
                for (Node node : Main.NETWORK) {
                    if(node.getNODE_ID() == transaction.getUSER_ID()) {
                        vote = true;
                        break;
                    }
                }
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
    public void proposeBlock(double quorumThreshold) {

        //If the node vote has a "false" ...
        int transactionRejectionCount = 0;
        for (Boolean vote : Main.quorum.getVOTES()) {
            if (!vote) {
                transactionRejectionCount++;
            }
        }
        System.out.println(transactionRejectionCount + " : " + Main.quorum.getSIZE());
        double percentRejected = (double)transactionRejectionCount / (double)Main.quorum.getSIZE();

        //Check whether threshold is met
        if (percentRejected > quorumThreshold) {
            System.out.println("Block validation failed - Attempting to remove Bad TXs and rebroadcast for validaton\n Rejected " + percentRejected);

            //search through mempool and check for invalid transactions (i.e. NETWORK not members of the network - invalid nodeID)
            for (int i = 0; i < this.PENDING_TRANSACTIONS.size(); i++) {
                if ((this.PENDING_TRANSACTIONS.get(i).getUSER_ID() > Main.NETWORK.size()) || (this.PENDING_TRANSACTIONS.get(i).getUSER_ID() < 1)) {
                    //bad transaction found, Call on quorum to remove bad transaction and revalidate new block
                    for (Node node : Main.quorum.getNODES()) {
                        node.getPENDING_TRANSACTIONS().remove(i);
                        node.validateTransactions();

                    }
                    this.proposeBlock(quorumThreshold);
                }
            }
        }
        // If node vote is good (block is good) create new block from the mempool
        else {  //Block is good, add Block to local ledger, clear MemPool
            System.out.println("(before) CURRENT BLOCKCHAIN SIZE " + this.BLOCKCHAIN.size());
            for (Transaction transaction : this.PENDING_TRANSACTIONS) {
                ArrayList<String> hashes = new ArrayList<>();
                hashes.add(this.BLOCKCHAIN.get(this.BLOCKCHAIN.size() - 1)
                        .getHASH());
                this.BLOCKCHAIN.add(new Block(transaction, hashes , this.BLOCKCHAIN.size() + 1, Main.quorum.getVOTES()));

//                System.out.println("Successfully added Block. Blockchain length: " + this.blockchain.size());
            }

            this.PENDING_TRANSACTIONS.clear();

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

        if (!this.PENDING_TRANSACTIONS.contains(tx)) {
            this.PENDING_TRANSACTIONS.add(tx);
        }
        //broadcast transaction to connected PEERS
        for (Node peer : this.PEERS) {
            if (!peer.getPENDING_TRANSACTIONS().contains(tx)) {
                peer.getPENDING_TRANSACTIONS().add(tx);
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
                            .getBLOCKCHAIN().size() - 1).getVOTES()
                    .contains(false)) {

                //Quorum Signature succesfully validated, clear PENDING_TRANSACTIONS and add latest blocks to local ledger
                //Start index at the size of the non-updated blockchain
                //End one before the size of the updated (larger) blockchain
                for (int i = this.BLOCKCHAIN.size(); i < Main.NETWORK.get(maxID - 1).getBLOCKCHAIN().size(); i++) {
                    this.BLOCKCHAIN.add(Main.NETWORK.get(maxID - 1)
                            .getBLOCKCHAIN().get(i));
                }

                //*note* in reality we want to remove only PENDING_TRANSACTIONS transactions that are already in the blockchain
                //for scalability experiment, PENDING_TRANSACTIONSs will always match, so just clear PENDING_TRANSACTIONS.
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