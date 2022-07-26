
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
    public ArrayList<Block> BLOCKCHAIN = new ArrayList<>();
    public static Block currentBlock;
    public static Quorum quorum;
    private static final int NETWORK_SIZE = 10;
    private final int QUORUM_SIZE = 10;

    /**
     * Driver to run experiments
     */
    public static void main(String[] args) throws Exception {
        //Run Experiments
        Main main = new Main();

        //Create Nodes
        for (int i = 0; i < NETWORK_SIZE; i++) {
            NETWORK.add(new Node());
//            System.out.println("Add node: " + i);
        }

        for(int i = 0; i<5; i++) {
            System.out.println(i);
            main.scalability();
        }

//        main.merkleExperiment();
    }

    //----------Experiments----------//

    /**
     * TODO
     */
    public void scalability() throws Exception {
        BLOCKCHAIN = new ArrayList<>();
//        System.out.println("Blockchain size at start " + BLOCKCHAIN.size());
        File file = new File("AverageBlockAddTime.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File("AverageBlockAddTime.csv"), true));

        long totalTimeSum = 0;
        long exchangeSum = 0;
        int printCount = 0;


        randomizeGen randomizeGen = new randomizeGen(10);
        ArrayList<workflow> workflows = randomizeGen.getWorkflows();

//        System.out.println("Workflows.size " + workflows.size());

        for (int i = 0; i<workflows.size(); i++){
            ArrayList<task> workflow = workflows.get(i).getWorkflow();
            Block[] workflowBlocks = new Block[workflow.size()];
            for(int j = 0; j < workflow.size(); j++) {
//                System.out.println("Task: " + printCount);
                ArrayList<String> provenanceRecord = workflow.get(j).toProvenanceRecord();

                String parentTaskIDString = provenanceRecord.get(0);
                parentTaskIDString = parentTaskIDString.replace("[", "");
                parentTaskIDString = parentTaskIDString.replace("]", "");
                parentTaskIDString = parentTaskIDString.strip();
                String[] parentTaskIDs = parentTaskIDString.split(",");


                Block[] parentBlocks = new Block[parentTaskIDs.length];
                for(int parentCount = 0; parentCount < parentTaskIDs.length; parentCount++){
                    if(Integer.parseInt(parentTaskIDs[parentCount].strip()) != -1) {
                        Block parentBlock = workflowBlocks[Integer.parseInt(parentTaskIDs[parentCount].strip())];
                        parentBlocks[parentCount] = parentBlock;
                    }
                }

//                String validMerkleRoot = provenanceRecord.get(1);
//                String invalidMerkleRoot = provenanceRecord.get(2);

                //Create the quorum
                this.quorum = new Quorum(this.QUORUM_SIZE);

                //Begin: Get start time
                long start = System.currentTimeMillis();

                //Step 2: Create the block
                currentBlock = NETWORK.get(0).createBlock(provenanceRecord, parentBlocks);

                //Step 4: Quorum signs the block and xchange signatures
                long beginExchange = System.currentTimeMillis();
                quorum.exchangeSignatures();
                long endExchange = System.currentTimeMillis();
                long exchange = endExchange - beginExchange;

                //Append block to blockchain
                this.BLOCKCHAIN.add(currentBlock);
                workflowBlocks[j] = currentBlock;
                //Get end time
                long endTime = System.currentTimeMillis();

                long totalTime = (endTime - start);
                totalTimeSum += totalTime;
                exchangeSum += exchange;
//                System.out.println("Block Add Time: " + totalTime);
                printCount ++;

            }
        }
        long totalAverage = totalTimeSum/printCount;
        long exchangeAverage = exchangeSum/printCount;
//        System.out.println();
//        System.out.println("Total time to add blocks: " + totalTimeSum);
//        System.out.println("Average time to add 1 block: " + avgTime);
//        System.out.println("Blockchain size " + BLOCKCHAIN.size());
//        System.out.println("Print count " + printCount);
        String trialData = exchangeAverage + ", " + totalAverage;
        pw.println(trialData);
        pw.close();
    }

    /**
     * TODO
     */
    public void merkleExperiment() throws Exception {
        File file = new File("Merkle.csv");
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File("Merkle.csv"), true));

        //Create Nodes
        for (int i = 0; i < this.NETWORK_SIZE; i++) {
            NETWORK.add(new Node());
            System.out.println("Add node: " + i);
        }

        //Creates one giant workflow
        randomizeGen randomizeGen = new randomizeGen(1);
        ArrayList<task> workflow = randomizeGen.getWorkflows().get(0).getWorkflow();
        String verificationTimes;
        long validAndLastInvalid = 0;
        long lastInvalid = 0;
        long selfValid = 0;
        long bruteForce = 0;
        //-------------------------------------------------
        Block[] workflowBlocks = new Block[workflow.size()];
        int printCount = 0;
        for(int j = 0; j < workflow.size(); j++) {
            System.out.println("Task: " + printCount);
            ArrayList<String> provenanceRecord = workflow.get(j).toProvenanceRecord();

            String parentTaskIDString = provenanceRecord.get(0);
            parentTaskIDString = parentTaskIDString.replace("[", "");
            parentTaskIDString = parentTaskIDString.replace("]", "");
            parentTaskIDString = parentTaskIDString.strip();
            String[] parentTaskIDs = parentTaskIDString.split(",");
            Block[] parentBlocks = new Block[parentTaskIDs.length];
            for(int parentCount = 0; parentCount < parentTaskIDs.length; parentCount++){
                if(Integer.parseInt(parentTaskIDs[parentCount].strip()) != -1) {
                    Block parentBlock = workflowBlocks[Integer.parseInt(parentTaskIDs[parentCount].strip())];
                    parentBlocks[parentCount] = parentBlock;                }
            }
//            String validMerkleRoot = provenanceRecord.get(1);
//            String invalidMerkleRoot = provenanceRecord.get(2);

            //Create the quorum
            this.quorum = new Quorum(this.QUORUM_SIZE);
            //Step 2: Create the block
            currentBlock = NETWORK.get(0).createBlock(provenanceRecord, parentBlocks);
            //Step 4: Quorum signs the block and xchange signatures
            quorum.exchangeSignatures();
            //Append block to blockchain
            this.BLOCKCHAIN.add(currentBlock);
            workflowBlocks[j] = currentBlock;

            //------Begin merkle-----------
            printCount ++;
        }
//        for(int i = 1; i<6; i++) {
//            int totalBlocksOnChain = i * 2000 + 1;
//            for (int j = 1; j < totalBlocksOnChain; j++) {
//                Block blockToVerify = this.BLOCKCHAIN.get(j);
//                Block lastBlock = this.BLOCKCHAIN.get(totalBlocksOnChain-1);
//
//                //Trial Time 1 - validAndLastInvalid
//
//                //Trial Time 2 - lastInvalid
//
//                //Trial Time 3 - selfValid
//
//                //Trial Time 4 - bruteForce
//
//            }
//        }


        //--------------------------------------------------

        verificationTimes = validAndLastInvalid + ", " + lastInvalid + ", " + selfValid + ", " + bruteForce;
        System.out.println(verificationTimes);
        pw.println(verificationTimes);
        pw.close();
    }


}
