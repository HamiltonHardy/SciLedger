
import workflowGen.randomizeGen;
import workflowGen.task;
import workflowGen.workflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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


//            main.merkleExperiment();
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
                        parentBlocks[parentCount] = workflowBlocks[Integer.parseInt(parentTaskIDs[parentCount].strip())];
                    }
                }


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

        //Create Nodes
//        for (int i = 0; i < this.NETWORK_SIZE; i++) {
//            NETWORK.add(new Node());
//            System.out.println("Add node: " + i);
//        }

        //Creates one giant workflow
        randomizeGen randomizeGen = new randomizeGen(1);
        ArrayList<task> workflow = randomizeGen.getWorkflows().get(0).getWorkflow();
        String verificationTimes;

        //-------------------------------------------------
        Block[] workflowBlocks = new Block[workflow.size()];
        int printCount = 0;
        for(int j = 0; j < workflow.size(); j++) {
//            System.out.println("Task: " + printCount);
            ArrayList<String> provenanceRecord = workflow.get(j).toProvenanceRecord();

//            String parentTaskIDString = provenanceRecord.get(0);
//            parentTaskIDString = parentTaskIDString.replace("[", "");
//            parentTaskIDString = parentTaskIDString.replace("]", "");
//            parentTaskIDString = parentTaskIDString.strip();
//            String[] parentTaskIDs = parentTaskIDString.split(",");
            Block[] parentBlocks = new Block[1];
//            for(int parentCount = 0; parentCount < parentTaskIDs.length; parentCount++){
//                if(Integer.parseInt(parentTaskIDs[parentCount].strip()) != -1) {
//                    Block parentBlock = workflowBlocks[Integer.parseInt(parentTaskIDs[parentCount].strip())];
//                    parentBlocks[parentCount] = parentBlock;                }
//            }
//
//
//            //Create the quorum
//            this.quorum = new Quorum(this.QUORUM_SIZE);
//            //Step 2: Create the block
            currentBlock = NETWORK.get(0).createBlock(provenanceRecord, parentBlocks);
//            //Step 4: Quorum signs the block and xchange signatures
//            quorum.exchangeSignatures();
//            //Append block to blockchain
            this.BLOCKCHAIN.add(currentBlock);
//            workflowBlocks[j] = currentBlock;

            //------Begin merkle-----------
            printCount ++;
        }
        //Trial for 2, 4, 6, 8, 10k blocks + genesis
        for(int i = 1; i<6; i++) {
//            int totalBlocksOnChain = i * 2000 + 1;
            int totalBlocksOnChain = i * 1000 + 1;
            String fileName = "Merkle-" + totalBlocksOnChain + "-blocks.csv";
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(fileName), true));

            //Set values for random later
            int max = totalBlocksOnChain;
            int min = 1;
            int range = max - min + 1;

            long trialOneSum = 0;
            long trialTwoSum = 0;
            long trialThreeSum = 0;
            long trialFourSum = 0;

            System.out.println("Starting experiment:");
            for (int j = 1; j < totalBlocksOnChain; j++) {
                Block blockToVerify = this.BLOCKCHAIN.get(j);
                int validMerkleHeight = Integer.parseInt(blockToVerify.validGetTreeHeight());
                int invalidMerkleHeight = Integer.parseInt(blockToVerify.invalidGetTreeHeight());
                Block lastBlock = this.BLOCKCHAIN.get(totalBlocksOnChain-1);
                int lastValidMerkleHeight = Integer.parseInt(lastBlock.validGetTreeHeight());
                int lastInvalidMerkleHeight = Integer.parseInt(lastBlock.invalidGetTreeHeight());
                String validMerkleRoot = blockToVerify.getValidMerkleRoot();
                String invalidMerkleRoot = blockToVerify.getInvalidMerkleRoot();
                Boolean valid;

                int rand = (int)(Math.random() * range) + min;
                task randomTask = workflow.get(rand);

                //Trial Time 1 - validAndLastInvalid
                long trialOneStart = System.currentTimeMillis();
                //valid(self)
                String totalHash = randomTask.hash();
                for(int level = 1; level < validMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }
                valid = totalHash.equals(validMerkleRoot);

                //invalid(last)
                totalHash = randomTask.hash();
                for(int level = 1; level < lastInvalidMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }
                valid = totalHash.equals(invalidMerkleRoot);

                long trialOneEnd = System.currentTimeMillis();
                long trialOneDifference = trialOneEnd - trialOneStart;
                trialOneSum += trialOneDifference;

                //Trial Time 2 - lastValid
                long trialTwoStart = System.currentTimeMillis();

                //valid(last)
                totalHash = randomTask.hash();
                for(int level = 1; level < lastValidMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }

                valid = totalHash.equals(validMerkleRoot);

                long trialTwoEnd = System.currentTimeMillis();
                long trialTwoDifference = trialTwoEnd - trialTwoStart;
                trialTwoSum += trialTwoDifference;

                //Trial Time 3 - selfValid
                long trialThreeStart = System.currentTimeMillis();

                //valid(self)

                totalHash = randomTask.hash();
                for(int level = 1; level < validMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }
                valid = totalHash.equals(invalidMerkleRoot);

                long trialThreeEnd = System.currentTimeMillis();
                long trialThreeDifference = trialThreeEnd - trialThreeStart;
                trialThreeSum += trialThreeDifference;

                //Trial Time 4 - bruteForce
                long trialFourStart = System.currentTimeMillis();
                for(int index = 1; index < j + 1; index ++){
                    if(blockToVerify == this.BLOCKCHAIN.get(index)){
                        break;
                    }
                }
                valid = totalHash.equals(invalidMerkleRoot);
                long trialFourEnd = System.currentTimeMillis();
                long trialFourDifference = trialFourEnd - trialFourStart;
                //End of four is the same as 3, so just append
                trialFourSum += (trialFourDifference + trialThreeDifference);


            }
            long trialOneAvg = trialOneSum/(totalBlocksOnChain - 1);
            long trialTwoAvg = trialTwoSum/(totalBlocksOnChain - 1);
            long trialThreeAvg = trialThreeSum/(totalBlocksOnChain - 1);
            long trialFourAvg = trialFourSum/(totalBlocksOnChain - 1);

            System.out.println(trialOneSum + "-" + trialTwoSum + "-" + trialThreeSum + "-" + trialFourSum);
            verificationTimes = trialOneAvg + ", " + trialTwoAvg + ", " + trialThreeAvg + ", " + trialFourAvg;
            System.out.println(verificationTimes);
            pw.println(verificationTimes);
            pw.close();
        }
    }

    public String hash(String hashes){
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(digest.digest(hashes.getBytes(StandardCharsets.UTF_8)));
    }
}
