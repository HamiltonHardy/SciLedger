
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Quorum Class: Creates the quorum to approve transactions. The quorum is randomly selected from the blockchain
 * network of nodes.
 */
public class Quorum {
    private final int SIZE;
    private final ArrayList<Node> NODES;
    private final ArrayList<Boolean> VOTES;

    /**
     * Constructor: initiates quorum selection, sets the votes for each node to false.
     * @param quorumSize The desired number of nodes in the quorum
     */

    public Quorum(int quorumSize) {
        this.SIZE = quorumSize;
        this.NODES = selectQuorum();
        this.VOTES = new ArrayList<>(Arrays.asList(new Boolean[this.SIZE]));
        Collections.fill(this.VOTES, Boolean.FALSE);
    }

    /**
     * Randomly selects the quorum members.
     * @return Arraylist containing the nodes in the quorum.
     */
    public ArrayList<Node> selectQuorum() {
        ArrayList<Node> quorum = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < this.SIZE; i++) {

            Node node = Main.Nodes.get(rand.nextInt(Main.Nodes.size()));
            //Ensure no duplicate nodes in list
            while (quorum.contains(node)) {
                node = Main.Nodes.get(rand.nextInt(Main.Nodes.size()));
            }
            quorum.add(node);
        }
        return quorum;
    }

    public ArrayList<Boolean> getVOTES() {
        return VOTES;
    }

    public ArrayList<Node> getNODES() {
        return NODES;
    }
    public int getSIZE(){
        return SIZE;
    }

}
