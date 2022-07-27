
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
        Main main = new Main();

        //Create the network
        for (int i = 0; i < NETWORK_SIZE; i++) {
            NETWORK.add(new Node());
        }

//        for(int i = 0; i<5; i++) {
//            System.out.println("Trial  " + i);
//            main.scalability();
//        }
            main.merkleExperiment();
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
                long start = System.nanoTime();

                //Step 2: Create the block
                currentBlock = NETWORK.get(0).createBlock(provenanceRecord, parentBlocks);

                //Step 4: Quorum signs the block and xchange signatures
                long beginExchange = System.nanoTime();
                quorum.exchangeSignatures();
                long endExchange = System.nanoTime();
                long exchange = endExchange - beginExchange;

                //Append block to blockchain
                this.BLOCKCHAIN.add(currentBlock);
                workflowBlocks[j] = currentBlock;
                //Get end time
                long endTime = System.nanoTime();

                long totalTime = (endTime - start);
                totalTimeSum += totalTime;
                exchangeSum += exchange;
//                System.out.println("Block Add Time: " + totalTime);
                printCount ++;

            }
        }
        long totalAverage = totalTimeSum/printCount;
        long exchangeAverage = exchangeSum/printCount;
        System.out.println();
        System.out.println("Total time to add blocks: " + totalTimeSum);
        System.out.println("Average time to add 1 block: " + totalAverage);
        System.out.println("Blockchain size " + BLOCKCHAIN.size());
        System.out.println("Print count " + printCount);
        String trialData = exchangeAverage + ", " + totalAverage;
        pw.println(trialData);
        pw.close();
    }

    /**
     * TODO
     */
    public void merkleExperiment() throws Exception {
        //Creates one giant workflow
        randomizeGen randomizeGen = new randomizeGen(1);
        ArrayList<task> workflow = randomizeGen.getWorkflows().get(0).getWorkflow();
        String verificationTimes;

        for(int j = 0; j < workflow.size(); j++) {
            ArrayList<String> provenanceRecord = workflow.get(j).toProvenanceRecord();
            Block[] parentBlocks = new Block[1];
            //Step 2: Create the block
            currentBlock = NETWORK.get(0).createBlock(provenanceRecord, parentBlocks);
            //Append block to blockchain
            this.BLOCKCHAIN.add(currentBlock);
        }

        //Trial for 1, 2, 3, 4, 5k blocks + genesis
        for(int i = 1; i<6; i++) {
            int totalBlocksOnChain = i * 100 + 1;
            //Creates a file for each workflow size
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

            System.out.println("Starting experiment with " + i + " thousand tasks");
            for (int j = 1; j < 50; j++) {

                int randomBlockIndex = (int)(Math.random() * range) + min;
                //get the block you want to verify and the last block within the workflow
                Block blockToVerify = this.BLOCKCHAIN.get(randomBlockIndex);
                Block lastBlock = this.BLOCKCHAIN.get(totalBlocksOnChain-1);
                //get height of valid merkle tree for the block you are verifying
                int validMerkleHeight = Integer.parseInt(blockToVerify.validGetTreeHeight());

                //get the height of the valid and invalid merkle tree for the last block in the workflow
                int lastValidMerkleHeight = Integer.parseInt(lastBlock.validGetTreeHeight());
                int lastInvalidMerkleHeight = Integer.parseInt(lastBlock.invalidGetTreeHeight());

                //get the valid and invalid merkle roots for the block you are trying to verify
                String validMerkleRoot = blockToVerify.getValidMerkleRoot();
                String invalidMerkleRoot = blockToVerify.getInvalidMerkleRoot();

                Boolean valid;

                //Gets a random task from the workflow to use as the comparison hash (what the database would provide)
                int randomTaskIndex = (int)(Math.random() * range) + min;
                task randomTask = workflow.get(randomTaskIndex);

                //Trial Time 1 - Check for existence in valid tree (self) and non-existence in invalid tree (last)
                long trialOneStart = System.nanoTime();
                //valid(self)
                String totalHash = randomTask.hash();

                //These loops hash a task, concatenate the hash to itself, then hash that. Repeats the
                // number of the height of the tree
                for(int level = 1; level < validMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }
                //This represents checking if the given merkle root matches the hashed one
                valid = totalHash.equals(validMerkleRoot);

                //invalid(last)
                totalHash = randomTask.hash();
                for(int level = 1; level < lastInvalidMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }
                valid = totalHash.equals(invalidMerkleRoot);

                long trialOneEnd = System.nanoTime();
                long trialOneDifference = trialOneEnd - trialOneStart;
                trialOneSum += trialOneDifference;

                //Trial Time 2 - Check for existence in valid tree (last)
                long trialTwoStart = System.nanoTime();

                //valid(last)
                totalHash = randomTask.hash();
                for(int level = 1; level < lastValidMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }

                valid = totalHash.equals(validMerkleRoot);

                long trialTwoEnd = System.nanoTime();
                long trialTwoDifference = trialTwoEnd - trialTwoStart;
                trialTwoSum += trialTwoDifference;

                //Trial Time 3 - Checks to see if block existed at some point (may have since been invalidated)
                //So check for existence in valid tree (self)
                long trialThreeStart = System.nanoTime();

                //valid(self)
                totalHash = randomTask.hash();
                for(int level = 1; level < validMerkleHeight; level++){
                    totalHash = totalHash + totalHash;
                    totalHash = hash(totalHash);
                }
                valid = totalHash.equals(validMerkleRoot);

                long trialThreeEnd = System.nanoTime();
                long trialThreeDifference = trialThreeEnd - trialThreeStart;
                trialThreeSum += trialThreeDifference;

                //Trial Time 4 - bruteForce: linear search of the blockchain for the block. Once found, check for existence in valid tree (last) which is the same as trial two.
                long trialFourStart = System.nanoTime();
                for(int index = 1; index < j + 1; index ++){
                    if(blockToVerify == this.BLOCKCHAIN.get(index)){
                        break;
                    }
                }
                valid = totalHash.equals(invalidMerkleRoot);
                long trialFourEnd = System.nanoTime();
                long trialFourDifference = trialFourEnd - trialFourStart;
                trialFourSum += (trialFourDifference);

//                System.out.println(trialOneDifference + " " + trialTwoDifference + " " + trialThreeDifference + " " + trialFourDifference);
            }
            long trialOneAvg = trialOneSum/(totalBlocksOnChain - 1);
            long trialTwoAvg = trialTwoSum/(totalBlocksOnChain - 1);
            long trialThreeAvg = trialThreeSum/(totalBlocksOnChain - 1);
            long trialFourAvg = trialFourSum/(totalBlocksOnChain - 1);

//            System.out.println(trialOneSum + "-" + trialTwoSum + "-" + trialThreeSum + "-" + trialFourSum);
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
