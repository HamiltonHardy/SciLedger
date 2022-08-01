
import java.io.*;
import java.util.ArrayList;

/**
 * Quorum Class: Creates the quorum to approve transactions. The quorum is randomly selected from the blockchain
 * network of nodes.
 */
public class Quorum {
    private final int SIZE;
    private final ArrayList<Node> NODES;

    /**
     * Constructor: initiates quorum selection, sets the votes for each node to false.
     * @param quorumSize The desired number of NETWORK in the quorum
     */

    public Quorum(int quorumSize) {
        this.SIZE = quorumSize;
        this.NODES = Main.NETWORK;
    }

    /**
     * Function simulates each member of the quorum signing the block and then every other member of the quorum
     * verifying the signature. This function is only used to factor in the time that this process takes, so
     * nothing is returned.
     */
    public void exchangeSignatures() throws Exception {
        for(int i = 0; i<this.SIZE; i++){
            //Quorum member signs block
            Node signer = this.NODES.get(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(Main.currentBlock);
            oos.flush();
            byte[] blockAsByteArray = bos.toByteArray();
            byte[] digitalSignature = signer.Create_Digital_Signature(blockAsByteArray, signer.getPRIVATE_KEY());

            //All other members verify signature
            for(int j = 0; j<this.SIZE; j++){
                //Quorum member should not verify its own signature
                if(i != j) {
                    Node verifier = this.NODES.get(i);
                    verifier.Verify_Digital_Signature(blockAsByteArray, digitalSignature, signer.getPUBLIC_KEY());
                }
            }
        }
    }

}
