
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
    public Quorum quorum;
    private int NETWORK_SIZE;
//    private final int QUORUM_SIZE = 10;

    /**
     * Driver to run experiments
     */
    public static void main(String[] args) throws Exception {
        Main main = new Main();

//        //Vary the quorum size from 200-1000 (10% of 2-10k)
//        for(int i = 5; i<51; i+=5) {
//            //Create the network
//            for (int y = 0; y < i; y++) {
//                NETWORK.add(new Node());
//            }
//            //Number of trials
//            for (int j = 0; j < 50; j++) {
//                System.out.println("Quorum size: " + i + ", Trial: " + j);
//                main.scalability(i);
//            }
//        }
            main.merkleExperiment();
    }

    //----------Experiments----------//

    /**
     * TODO
     */
    public void scalability(int quorumSize) throws Exception {
        this.NETWORK_SIZE = quorumSize;
        BLOCKCHAIN = new ArrayList<>();
        String filename = "AverageBlockAddTime.csv";
        File file = new File(filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        PrintWriter pw = new PrintWriter(new FileOutputStream(new File(filename), true));

        long totalTimeSum = 0;
        long exchangeSum = 0;
        int printCount = 0;


        randomizeGen randomizeGen = new randomizeGen(10, 9);
        ArrayList<workflow> workflows = randomizeGen.getWorkflows();

        for (int i = 0; i<workflows.size(); i++){
            ArrayList<task> workflow = workflows.get(i).getWorkflow();
            Block[] workflowBlocks = new Block[workflow.size()];
            for(int j = 0; j < workflow.size(); j++) {
                ArrayList<String> provenanceRecord = workflow.get(j).toProvenanceRecord();
                String[] parentTaskIDs = provenanceRecord.get(0).replace("[", "").replace("]", "").strip().split(",");
                Block[] parentBlocks = new Block[parentTaskIDs.length];
                for(int parentCount = 0; parentCount < parentTaskIDs.length; parentCount++){
                    if(Integer.parseInt(parentTaskIDs[parentCount].strip()) != -1) {
                        parentBlocks[parentCount] = workflowBlocks[Integer.parseInt(parentTaskIDs[parentCount].strip())];
                    }
                }

                //Create the quorum
                this.quorum = new Quorum(this.NETWORK_SIZE);

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
                System.gc();

            }
        }
        long totalAverage = totalTimeSum/printCount + randomizeGen.getHashRuntimeAvg() + randomizeGen.getMerkleRuntimeAvg();
        long exchangeAverage = exchangeSum/printCount;
//        System.out.println();
//        System.out.println("Total time to add blocks: " + totalTimeSum);
//        System.out.println("Average time to add 1 block: " + totalAverage);
//        System.out.println("Blockchain size " + BLOCKCHAIN.size());
//        System.out.println("Print count " + printCount);

        String trialData = quorumSize + ", " +randomizeGen.getHashRuntimeAvg() + ", " + randomizeGen.getMerkleRuntimeAvg() + "," + exchangeAverage + "," + totalAverage;

        System.out.println(trialData);

        pw.println(trialData);
        pw.close();
    }

    /**
     * TODO
     */
    public void merkleExperiment() throws Exception {
        NETWORK.add(new Node());

        //Trial for 1, 2, 3, 4, 5k blocks + genesis
        for(int i = 1; i<6; i++) {
            int totalBlocksOnChain = i * 1000;
            //int totalBlocksOnChain = 20;
            //Creates one giant workflow
            randomizeGen randomizeGen = new randomizeGen(1, totalBlocksOnChain);
            ArrayList<task> workflow = randomizeGen.getWorkflows().get(0).getWorkflow();
            System.out.println(workflow.size());

            for(int j = 1; j < workflow.size(); j++) {
                ArrayList<String> provenanceRecord = workflow.get(j).toProvenanceRecord();
                Block[] parentBlocks = new Block[1];
                //Append block to blockchain
                this.BLOCKCHAIN.add(NETWORK.get(0).createBlock(provenanceRecord, parentBlocks));
            }


            //Creates a file for each workflow size
            String fileName = "Merkle2" + totalBlocksOnChain +".csv";
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(fileName), true));
            //Set values for random later
            int max = totalBlocksOnChain-1;
            int min = 1;
            int range = max - min + 1;

            System.out.println("Starting experiment with " + totalBlocksOnChain + " tasks-----------------------------------------------------------------");
            for (int j = 0; j < 50; j++) {
                int randomBlockIndex = (int)(Math.random() * range) + min;
                //get the block you want to verify and the last block within the workflow
                Block blockToVerify = this.BLOCKCHAIN.get(randomBlockIndex);
                Block lastBlock = this.BLOCKCHAIN.get(this.BLOCKCHAIN.size()-1);
                //get height of valid merkle tree for the block you are verifying
                int validMerkleSize = Integer.parseInt(blockToVerify.validGetTreeSize());

                //get the height of the valid and invalid merkle tree for the last block in the workflow
                int lastValidMerkleSize = Integer.parseInt(lastBlock.validGetTreeSize());
                int lastInvalidMerkleSize = Integer.parseInt(lastBlock.invalidGetTreeSize());

                System.out.println("Last valid " + lastValidMerkleSize + " last invalid " + lastInvalidMerkleSize + " size " + this.BLOCKCHAIN.size());

                //Chart A - Exists and Valid
                int siblingOrNot = (int) (Math.random() * 2 + 2);
                //Trial A1 - Check for existence in valid tree (self) and non-existence in invalid tree (last)
                int inSelfCount = (int) Math.ceil(Math.log(validMerkleSize) / Math.log(2) + 1);
                //***Should alt between 2 and 3
                int notInLastInvalidCount = (int) Math.ceil(Math.log(lastInvalidMerkleSize) / Math.log(2) + siblingOrNot);
                int a1Counts =  inSelfCount + notInLastInvalidCount;

                //Trial A2 - Check for existence in valid tree (last)
                int a2Counts = (int) Math.ceil(Math.log(lastValidMerkleSize) / Math.log(2) + 1);

                //Chart B - Exists
                //Trial B1 - Check for existence in valid tree (self) (first part of a1)
                int b1Counts = inSelfCount;

                //Trial B2 - Check for existence (brute force, find self)
                String b2Counts = "error";
                for(int y = 1; y < totalBlocksOnChain; y++){
                    if(blockToVerify == this.BLOCKCHAIN.get(y)){
                        b2Counts = y + "";
                        break;
                    }
                }

                //Chart C - Non-Existence
                siblingOrNot = (int) (Math.random() * 2 + 2);
                // Trial C1 - brute force, check all

                int c1Counts = totalBlocksOnChain;

                //Trial C2 - Check for non-existence in valid tree (last) and invalid tree (last)
                //*** Should alt between 2 and 3
                int notInLastValidCount = (int) Math.ceil(Math.log(lastValidMerkleSize) / Math.log(2) + siblingOrNot);
                int c2Counts = notInLastValidCount + notInLastInvalidCount;



                String taskID = blockToVerify.getPROVENANCE_RECORD().get(6);
                String verificationCounts = totalBlocksOnChain + ", " + taskID + ", " + a1Counts + ", " + a2Counts + ", " + b1Counts + ", " + b2Counts + ", " + c1Counts + ", " + c2Counts;
                System.out.println(verificationCounts);
                pw.println(verificationCounts);

            }

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
