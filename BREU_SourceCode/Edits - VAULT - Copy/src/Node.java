
import java.lang.reflect.Array;
import java.security.*;
import java.util.ArrayList;

/**
 * Node Class: Creates the nodes that make up the blockchain network.
 */

public class Node {

    private final int NODE_ID;
    private final ArrayList<Block> BLOCKCHAIN = new ArrayList<>();
    private final ArrayList<Node> PEERS = new ArrayList<>();
    private final PrivateKey PRIVATE_KEY;
    private final PublicKey PUBLIC_KEY;
//    private final ArrayList<Transaction> PENDING_TRANSACTIONS = new ArrayList<>();

    /**
     * Constructor: Assigns an ID to the node and creates the starting blockchain for the node
     * which consists of only the genesis block. The ID of the node is just what number it is in the network.
     * ex. 1st block = 1, 2nd block = 2...
     */
    public Node() throws Exception {
        this.NODE_ID = Main.NETWORK.size() + 1;
        this.BLOCKCHAIN.add(Main.genesisBlock);
        KeyPair keyPair = Generate_RSA_KeyPair();
        this.PRIVATE_KEY = keyPair.getPrivate();
        this.PUBLIC_KEY = keyPair.getPublic();

    }

    /**
     * Takes the arraylist of provenance data and makes a transaction out of it
     * @return The resulting Transaction object
     */
    public Block createBlock(ArrayList<String> provenanceRecord) {
        return new Block(this.NODE_ID, provenanceRecord);
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

    // Generating the asymmetric key pair
    // using SecureRandom class
    // functions and RSA algorithm.
    public static KeyPair Generate_RSA_KeyPair()
            throws Exception {
        SecureRandom secureRandom
                = new SecureRandom();
        KeyPairGenerator keyPairGenerator
                = KeyPairGenerator
                .getInstance("RSA");
        keyPairGenerator
                .initialize(
                        2048, secureRandom);
        return keyPairGenerator
                .generateKeyPair();
    }

    public byte[] Create_Digital_Signature(byte[] input, PrivateKey Key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(Key);
        signature.update(input);
        return signature.sign();
    }

    public boolean Verify_Digital_Signature(byte[] input, byte[] signatureToVerify, PublicKey key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(input);
        return signature.verify(signatureToVerify);
    }

    public PrivateKey getPRIVATE_KEY() {
        return PRIVATE_KEY;
    }

    public PublicKey getPUBLIC_KEY(){
        return PUBLIC_KEY;
    }
}