
import workflowGen.randomizeGen;
import workflowGen.task;
import workflowGen.workflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

/**
 * Main Class:
 */
public class Main {
    public static ArrayList<Node> NETWORK = new ArrayList<>();
    public static Block GENESIS_BLOCK;
    public static Quorum quorum;
    public static ArrayList<Boolean> genesisQuorum = new ArrayList<Boolean>();
    private final int NETWORK_SIZE = 1000;
    private final int QUORUM_SIZE = 10;
    private final double QUORUM_THRESHOLD = .8;

    /**
     * Driver to run experiments
     */
    public static void main(String[] args) throws IOException {
        //Create an empty arraylist to use as the parent hashes for the genesis block
        ArrayList<String> genesisParentHashes = new ArrayList<>();
        //Create a "dummy" arraylist to use as the provenance data for the genesis block
        ArrayList<String> dummyProvenanceData = new ArrayList<>();
        for(int i = 0; i< 5; i++){
            dummyProvenanceData.add("-1");
        }

        //Create the genesis block
        GENESIS_BLOCK = new Block(new Transaction(-1, dummyProvenanceData), genesisParentHashes, 1, genesisQuorum);

        //Run Experiments
        Main main = new Main();
        main.scalability();
    }

    //----------Experiments----------//

    /**
     * TODO
     */
    public void scalability() throws IOException {
        File file = new File("QuorumCounts.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File("QuorumCounts.csv"), true));



        long totalTimeSum = 0;
        int printCount = 0;
        //Create Nodes
        for (int i = 0; i < this.NETWORK_SIZE; i++) {
            NETWORK.add(new Node());
        }
        connectNetwork();

        //Number of Blocks to create for tests
        //create workflows
        randomizeGen randomizeGen = new randomizeGen();
        ArrayList<workflow> workflows = randomizeGen.getWorkflows();

        for (int i = 0; i<workflows.size(); i++){
            ArrayList<task> workflow = workflows.get(i).getWorkflow();
            for(int j = 0; j < workflow.size(); j++) {
                ArrayList<String> provenanceData = workflow.get(j).toProvenanceData();

                long start = System.currentTimeMillis();
                this.quorum = new Quorum(this.QUORUM_SIZE);          //1.Create Quroum
//                long QCreation = System.currentTimeMillis();
//                long qDuration = (QCreation - start);  //divide by 1000000 to get milliseconds.
//                //Print 1
//                System.out.println("Begin---------------------");
//                System.out.print("Quorum creation duration: " + qDuration);
//                //broadcast #tps for #seconds
//
//                long broadcastStart = System.currentTimeMillis();

                NETWORK.get(0).propagateTransaction(NETWORK.get(0).createTransaction(provenanceData));

//                long broadcastEnd = System.currentTimeMillis();
//                long bDuration = (broadcastEnd - broadcastStart);
//                //Print 2
//                System.out.println(", broadcast transactions duration: " + bDuration);
//
//                long validationStart = System.currentTimeMillis();

                for (Node node : NETWORK) {  //3. Validate Transactions

                    node.validateTransactions();
                }
//                long validationEnd = System.currentTimeMillis();
//                long vDuration = (validationEnd - validationStart);
//                //Print 3
//                System.out.println(", validation duration: " + vDuration);
//                System.out.println("Middle---------------------");

                //Propse block and append all NETWORK' ledgers
//                long blockStart = System.currentTimeMillis();

                quorum.getNODES().get(0).validateBlock(this.QUORUM_THRESHOLD);  //4. Broadcast Block and propogate ledgers
//                long blockEnd = System.currentTimeMillis();
//                long blockDuration = (blockEnd - blockStart);
//                //Print 4
//                System.out.print(", Add block duration: " + blockDuration);

                //total time
                long endTime = System.currentTimeMillis();
                long totalTime = (endTime - start);
                totalTimeSum += totalTime;
                printCount ++;

            }
        }
        long avgTime = totalTimeSum/printCount;
//        System.out.println("Avg Time " + avgTime);
//        System.out.println("Blockchain size " + NETWORK.get(1).getBLOCKCHAIN().size());
        pw.println(avgTime);
        pw.close();
    }

    /**
     * TODO
     */
    static void merkleExperiment(){

    }

    /**
     * TODO
     */
    static void connectNetwork() {
        Random rand = new Random();

        for (Node node : NETWORK) {
            int count = rand.nextInt(2) + 9;  //Random connection to peers
            //int count = 10;  //Random connection to peers
            //System.out.println(count);

            //While # of peers is less than desired size, get a random peer to connect to
            while (node.getPEERS().size() < count) {
                Node peer = NETWORK.get(rand.nextInt(NETWORK.size()));

                //Peer must not be in list already and peer must not equal itself, or must try to get new peer
                while (node.getPEERS().contains(peer) || (node.getNODE_ID() == peer.getNODE_ID())) {
                    peer = NETWORK.get(rand.nextInt(NETWORK.size()));
                }
                //Connect to listen to peers
                node.addPeer(peer);
                //peer.addPeer(node);  //for two-way connection
            }
        }
    }


}
