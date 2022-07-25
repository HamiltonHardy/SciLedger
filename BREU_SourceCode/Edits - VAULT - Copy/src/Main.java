
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
    public static final ArrayList<Node> NETWORK = new ArrayList<>();
    public static final ArrayList<Block> BLOCKCHAIN = new ArrayList<>();
    public static Block currentBlock;
    public static Block genesisBlock;
    public static Quorum quorum;
    private final int NETWORK_SIZE = 20;
    private final int QUORUM_SIZE = 10;
    private final double QUORUM_THRESHOLD = .8;

    /**
     * Driver to run experiments
     */
    public static void main(String[] args) throws Exception {
        //Create an empty arraylist to use as the parent hashes for the genesis block
//        ArrayList<String> genesisParentHashes = new ArrayList<>();
        //Create a "dummy" arraylist to use as the provenance data for the genesis block
        ArrayList<String> dummyProvenanceRecord = new ArrayList<>();
        for(int i = 0; i< 5; i++){
            dummyProvenanceRecord.add("-1");
        }

        //Create the genesis block
//        GENESIS_BLOCK = new Block(-1, dummyProvenanceRecord, genesisParentHashes, 1, genesisQuorum);
        genesisBlock = new Block(-1, dummyProvenanceRecord);

        //Run Experiments
        Main main = new Main();
        main.scalability();
    }

    //----------Experiments----------//

    /**
     * TODO
     */
    public void scalability() throws Exception {
        File file = new File("AverageBlockAddTime.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File("AverageBlockAddTime.csv"), true));



        long totalTimeSum = 0;
        int printCount = 0;
        //Create Nodes
        for (int i = 0; i < this.NETWORK_SIZE; i++) {
            NETWORK.add(new Node());
            System.out.println("Add node: " + i);
        }

        //?? I don't think we need this?
//        connectNetwork();

        //Number of Blocks to create for tests
        //create workflows
        randomizeGen randomizeGen = new randomizeGen();
        ArrayList<workflow> workflows = randomizeGen.getWorkflows();

        for (int i = 0; i<workflows.size(); i++){
            System.out.println("Workflow");
            ArrayList<task> workflow = workflows.get(i).getWorkflow();
            for(int j = 0; j < workflow.size(); j++) {
                System.out.println("Task");
                ArrayList<String> provenanceData = workflow.get(j).toProvenanceData();

                //Create the quorum
                this.quorum = new Quorum(this.QUORUM_SIZE);

                //Begin: Get start time
                long start = System.currentTimeMillis();

                //Step 2: Create the block
                currentBlock = NETWORK.get(0).createBlock(provenanceData);

                //Step 4: Quorum signs the block and xchange signatures
                quorum.exchangeSignatures();

                //Append block to blockchain
                this.BLOCKCHAIN.add(currentBlock);

                //Get end time
                long endTime = System.currentTimeMillis();

                long totalTime = (endTime - start);
                totalTimeSum += totalTime;
                System.out.println("Block Add Time: " + totalTime);
                printCount ++;

            }
        }
        long avgTime = totalTimeSum/printCount;
        System.out.println("Total time to add 1000 blocks: " + totalTimeSum);
        System.out.println("Average time to add 1 block: " + avgTime);
//        System.out.println("Blockchain size " + BLOCKCHAIN.size());
//        System.out.println("Print count " + printCount);
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
