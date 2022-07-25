
import java.security.*;
import java.util.ArrayList;

/**
 * Node Class: Creates the nodes that make up the blockchain network.
 */

public class Node {

    private final int NODE_ID;
    private final PrivateKey PRIVATE_KEY;
    private final PublicKey PUBLIC_KEY;

    /**
     * Constructor: Assigns an ID to the node and creates the starting blockchain for the node
     * which consists of only the genesis block. The ID of the node is just what number it is in the network.
     * ex. 1st block = 1, 2nd block = 2...
     */
    public Node() throws Exception {
        this.NODE_ID = Main.NETWORK.size() + 1;
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

    public PrivateKey getPRIVATE_KEY() {
        return PRIVATE_KEY;
    }

    public PublicKey getPUBLIC_KEY(){
        return PUBLIC_KEY;
    }

    //-----------------------------Following code from Geeks for Geeks--------------------------------------
    //https://www.geeksforgeeks.org/java-implementation-of-digital-signatures-in-cryptography/

    /**
     * Generating the asymmetric key pair using SecureRandom class functions and RSA algorithm.
     * @return A public and private key pair
v     */
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

    /**
     *Function to implement Digital signature using SHA256 and RSA algorithm by passing private key.
     * @param input What is to be signed (as a byte array)
     * @param Key The singer private key
     * @return The digital signature (a byte array)
v     */
    public byte[] Create_Digital_Signature(byte[] input, PrivateKey Key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(Key);
        signature.update(input);
        return signature.sign();
    }

    /**
     * Function for Verification of the digital signature by using the public key
     * @param input What was signed (as a byte array)
     * @param signatureToVerify The digital signature (as a byte array)
     * @param key The signer public key
     */
    public void Verify_Digital_Signature(byte[] input, byte[] signatureToVerify, PublicKey key) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(key);
        signature.update(input);
        signature.verify(signatureToVerify);
    }

}