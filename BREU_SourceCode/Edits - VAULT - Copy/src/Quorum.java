
import javax.xml.crypto.NodeSetData;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

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
        this.NODES = selectQuorum();
    }

    /**
     * Randomly selects the quorum members.
     * @return Arraylist containing the nodes selected for the quorum.
     */
    public ArrayList<Node> selectQuorum() {
        ArrayList<Node> quorum = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < this.SIZE; i++) {
            Node node = Main.NETWORK.get(rand.nextInt(Main.NETWORK.size()));
            //Chooses a new node if the originally selected node is already in the quorum
            while (quorum.contains(node)) {
                node = Main.NETWORK.get(rand.nextInt(Main.NETWORK.size()));
            }
            quorum.add(node);
        }
        return quorum;
    }

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
