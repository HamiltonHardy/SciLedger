
import workflowGen.randomizeGen;
import workflowGen.task;
import workflowGen.workflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Main Class:
 */
public class Main {
    public static final ArrayList<Node> NETWORK = new ArrayList<>();
    public static final ArrayList<Block> BLOCKCHAIN = new ArrayList<>();
    public static Block currentBlock;
    public static Quorum quorum;
    private final int NETWORK_SIZE = 20;
    private final int QUORUM_SIZE = 10;

    /**
     * Driver to run experiments
     */
    public static void main(String[] args) throws Exception {
        //Create a "dummy" arraylist to use as the provenance data for the genesis block
        ArrayList<String> dummyProvenanceRecord = new ArrayList<>();
        for(int i = 0; i< 5; i++){
            dummyProvenanceRecord.add("-1");
        }

        //Run Experiments
        Main main = new Main();
        main.scalability();

        main.merkleExperiment();
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

        randomizeGen randomizeGen = new randomizeGen();
        ArrayList<workflow> workflows = randomizeGen.getWorkflows();

//        System.out.println(workflows);

        for (int i = 0; i<workflows.size(); i++){
            ArrayList<task> workflow = workflows.get(i).getWorkflow();
            Block[] workflowBlocks = new Block[workflow.size()];
            for(int j = 0; j < workflow.size(); j++) {
//                System.out.println("Task: " + printCount);
                ArrayList<String> provenanceRecord = workflow.get(j).toProvenanceRecord();

                String parentTaskIDString = provenanceRecord.get(0);
                System.out.println("parent task id string" + parentTaskIDString);
                parentTaskIDString = parentTaskIDString.replace("[", "");
                parentTaskIDString = parentTaskIDString.replace("]", "");
                parentTaskIDString = parentTaskIDString;
                System.out.println("parent task id string #2" + parentTaskIDString);
                String[] parentTaskIDs = parentTaskIDString.split(",");

                System.out.println("Parent task IDs " + parentTaskIDs);

                Block[] parentBlocks = new Block[parentTaskIDs.length];
                for(int parentCount = 0; parentCount < parentTaskIDs.length; parentCount++){
                    if(Integer.parseInt(parentTaskIDs[parentCount]) != -1) {
                        parentBlocks[parentCount] = workflowBlocks[Integer.parseInt(parentTaskIDs[parentCount])];
                    }
                }

                System.out.println("Parent blocks " + parentBlocks);
                
                String validMerkleRoot = provenanceRecord.get(1);
                String invalidMerkleRoot = provenanceRecord.get(2);

                //Create the quorum
                this.quorum = new Quorum(this.QUORUM_SIZE);

                //Begin: Get start time
                long start = System.currentTimeMillis();

                //Step 2: Create the block
                currentBlock = NETWORK.get(0).createBlock(provenanceRecord);

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
        System.out.println();
        System.out.println("Total time to add blocks: " + totalTimeSum);
        System.out.println("Average time to add 1 block: " + avgTime);
        System.out.println("Blockchain size " + BLOCKCHAIN.size());
        System.out.println("Print count " + printCount);
        pw.println(avgTime);
        pw.close();
    }

    /**
     * TODO
     */
    public void merkleExperiment() throws IOException {
        File file = new File("Merkle.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File("AverageBlockAddTime.csv"), true));

    }

}
