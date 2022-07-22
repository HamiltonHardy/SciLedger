
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author Justin Gazsi
 */
public class Quorum {

    //Determine number of Quorum members desired
//    private static final int NUM = 10;

    //Original Votes array is size 5. Why?
    //I changed to 10 to match the quorum size
    private ArrayList<Boolean> votes;
    private ArrayList<Node> QuroumGroup = new ArrayList<>();
    private int quorumSize;

    //Constructor
    public Quorum(int quorumSize) {
        this.quorumSize = quorumSize;
        getRandomQuorum();
        this.votes = new ArrayList<Boolean>(Arrays.asList(new Boolean[this.quorumSize]));
        Collections.fill(votes, Boolean.FALSE);
    }

    //This function returns an ArrayList of Quorum members from the Network Node list
    public void getRandomQuorum() {
        ArrayList<Node> quorum = new ArrayList<>();

        //long seed = stringToSeed(DataStorage.GenBlock.getHash());
        //long seed = stringToSeed(hash);
        Random rand = new Random();

        //Add specified number of random nodes to Quorum group
        for (int i = 0; i < this.quorumSize; i++) {
            Node node = DataStorage.Nodes.get(rand.nextInt(DataStorage.Nodes.size()));
            //Ensure no duplicate nodes in list
            while (quorum.contains(node)) {
                node = DataStorage.Nodes.get(rand.nextInt(DataStorage.Nodes.size()));
            }
            quorum.add(node);
        }
        this.QuroumGroup = quorum;
        //return quorum;
    }




//    //This function returns an ArrayList of Quorum members from the Network Node list
//    public void getHashQuorum(String hash) {
//
//        ArrayList<Node> quorum = new ArrayList<>();
//        long seed = stringToSeed(hash); //convert hash string to long
//        Random rand = new Random(seed); //seed with converted hash string
//
//        //long seed = stringToSeed(DataStorage.GenBlock.getHash()); //Test with consistent seed to check results
//
//        //Add specified number of random nodes to Quorum group
//        for (int i = 0; i < NUM; i++) {
//            Node node = DataStorage.Nodes.get(rand.nextInt(DataStorage.Nodes.size()));
//            //Ensure no duplicate nodes in list
//            while (quorum.contains(node)) {
//                node = DataStorage.Nodes.get(rand.nextInt(DataStorage.Nodes.size()));
//            }
//            quorum.add(node);
//        }
//        this.QuroumGroup = quorum;
//        //return quorum;
//    }


    //Getters and Setters
    public ArrayList<Boolean> getVotes() {
        return votes;
    }

    public void setVotes(ArrayList<Boolean> votes) {
        this.votes = votes;
    }

    public ArrayList<Node> getQuroumGroup() {
        return QuroumGroup;
    }

    public void setQuroumGroup(ArrayList<Node> QuroumGroup) {
        this.QuroumGroup = QuroumGroup;
    }

    //Function to take a hash and convert it to a long for Random seed
    static long stringToSeed(String s) {
        if (s == null) {
            return 0;
        }
        long hash = 0;
        for (char c : s.toCharArray()) {
            hash = 31L * hash + c;
        }
        return hash;
    }

}
