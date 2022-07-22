package workflowGen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class workflow {
    ArrayList<task> workflow = new ArrayList<>();
    final int maxWSize = 10;
    final double PERBRANCH = 0.3;
    task forNextWf;

    public workflow(int wfNum, String stpt, String spwf) {
        genRandWorkflow(wfNum, stpt, spwf);
    }

    public void addTask(task task){
        this.workflow.add(task);
        workflow.get(workflow.size()-1).setMerkleTree(this.genMerkleTree());
    }
    public void genRandWorkflow(int wf, String startPoint, String startPointWorkFlow) {

        Random rand = new Random();
        int wSize = rand.nextInt(maxWSize / 2) + 3;
        int branchCount = (int) (wSize * PERBRANCH) + 1;
        int counter = 1;
        int randIdx;


        addTask(new gentask("w" + wf, "gen", startPointWorkFlow, startPoint));
        addTask(new task("w" + wf, "t1", false, new ArrayList<>(Arrays.asList(0))));
        while (counter < wSize) {
            addTask(new task("w" + wf, "t" + (counter + 1), false, new ArrayList<>(Arrays.asList(counter))));
            counter++;
        }
        int linear = counter;

        for (int i = 0; i < branchCount; i++) {
            randIdx = rand.nextInt(linear - 2) + 1;
            addTask(new task("w" + wf, "t" + (counter + 1), false, new ArrayList<>(Arrays.asList(randIdx))));
            counter++;
            int branchLen = rand.nextInt(4);
            for (int j = 0; j < branchLen; j++) {
                addTask(new task("w" + wf, "t" + (counter + 1), false, new ArrayList<>(Arrays.asList(counter))));
                counter++;
            }
            task merge = this.workflow.get(rand.nextInt(linear - 1 - randIdx) + randIdx + 2);
            merge.addIdxParent(counter);
        }
        forNextWf = this.workflow.get((int) (Math.random() * workflow.size()));
    }


    public String genTreeHash(String hash){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(digest.digest(hash.getBytes(StandardCharsets.UTF_8)));
    }

    public ArrayList<String> genMerkleTree(){
        ArrayList<String> tree = new ArrayList<>();
        // Start by adding all the hashes of the transactions as leaves of the
        // tree.
        for (workflowGen.task task : this.workflow) {
            tree.add(task.hash());
        }
        int levelOffset = 0; // Offset in the list where the currently processed
        // level starts.
        // Step through each level, stopping when we reach the root (levelSize
        // == 1).
        for (int levelSize = this.workflow.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
            // For each pair of nodes on that level:
            for (int left = 0; left < levelSize; left += 2) {
                // The right hand node can be the same as the left hand, in the
                // case where we don't have enough
                // transactions.
                int right = Math.min(left + 1, levelSize - 1);
                String tleft = tree.get(levelOffset + left);
                String tright = tree.get(levelOffset + right);
                tree.add(genTreeHash(tleft + tright));
            }
            // Move to the next level.
            levelOffset += levelSize;
        }
        return tree;
    }

    public String toString(){
        StringBuilder str = new StringBuilder();
        for (workflowGen.task task : this.workflow) {
            str.append(task + "\n");
        }
        return str.toString();
    }
}
