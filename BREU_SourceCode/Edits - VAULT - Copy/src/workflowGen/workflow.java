package workflowGen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class workflow {

    final double PERINV = 0.3;
    int wfSize;
    task forNextWf;
    ArrayList<task> invTree = new ArrayList<>();
    ArrayList<task> workflow = new ArrayList<>();
    ArrayList<task> valTree = new ArrayList<>();
    public long runtime = 0;
    public workflow(int wfSize,int wfNum, String stpt, String spwf) {
        this.wfSize = wfSize;
        //make gen task
        //GENESIS BLOCK
        ArrayList<Integer> genesisParent = new ArrayList<>();
        genesisParent.add(-1);
        //String workflowID, String taskID, boolean invalidated, ArrayList<Integer> idxParent
        this.workflow.add(new task("w" + wfNum, "0", false, genesisParent));
        //make the other tasks
        genTasks(wfNum);
//        System.out.println("Merkle computation time: " + runtime);
    }

    public void addTask(task task){
        //if task is invalid add to invalid tree and copy valid tree from last task
        long stTime = System.currentTimeMillis();
//        System.out.println("START TIME " + stTime);
        if(task.isInvalidated()){
            this.invTree.add(task);
            task.setInvalidTree(this.genMerkleTree(this.invTree));
            task.setValidTree(workflow.get(workflow.size()-2).getValidTree());
        }
        //if task is valid add to valid tree and copy invalid tree from last task
        else{
            this.valTree.add(task);
            task.setValidTree(this.genMerkleTree(this.valTree));
            task.setInvalidTree(workflow.get(workflow.size()-2).getInvalidTree());
        }
        this.workflow.add(task);
        long stpTime = System.currentTimeMillis();
//        System.out.println("END TIME " + stpTime);
//        System.out.println("Gen Merkle Tree computation time" + (stpTime-stTime));
        runtime += (stpTime-stTime);
    }
    private void genTasks(int wf){
        Random rand = new Random();
        int wSize = (rand.nextInt(this.wfSize/2 -3) + 3);
        int counter = 1;
        int randIdx;

        this.workflow.add(new task("w" + wf, "t1", (rand.nextDouble() < PERINV), new ArrayList<>(Arrays.asList(0))));
        while (counter < wSize) {
            counter++;
            System.out.println(counter);
            addTask(new task("w" + wf, "t" + counter, (rand.nextDouble() < PERINV), new ArrayList<>(Arrays.asList(counter-1))));
        }

        //Add Branching Tasks
        //loop through remaining nonlinear tasks
        while(counter<this.wfSize) {
            randIdx = rand.nextInt(wSize - 2) + 1;
            counter++;
            System.out.println(counter);
            addTask(new task("w" + wf, "t" + counter, (rand.nextDouble() < PERINV), new ArrayList<>(Arrays.asList(randIdx))));
//            int branchLen = rand.nextInt(MAXWFSIZE-counter+1);
//            //for a new non linear task add a random number of linear tasks
//            for (int j = 0; j < branchLen; j++) {
//                counter++;
//                if(counter>=MAXWFSIZE) break;
//                addTask(new task("w" + wf, "t" + counter, (rand.nextDouble() < PERINV), new ArrayList<>(Arrays.asList(counter-1))));
//            }
            //merge any open tasks
            task merge = this.workflow.get(rand.nextInt(wSize - 1 - randIdx) + randIdx + 2);
            merge.addIdxParent(counter);
        }
        //get random task for next workflow branch
        forNextWf = this.workflow.get((int) (Math.random() * workflow.size()));

        //Remove genesis, sort, reinsert genesis
        task genesis = workflow.get(0);
        workflow.remove(genesis);
        workflow.sort(Comparator.comparing(o -> o.getIdxParent(0)));
        workflow.add(0, genesis);
    }



    public String genTreeHash(String hash){
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(digest.digest(hash.getBytes(StandardCharsets.UTF_8)));
    }

    public ArrayList<String> genMerkleTree(ArrayList<task> wf){
        ArrayList<String> tree = new ArrayList<>();
        // Start by adding all the hashes of the transactions as leaves of the
        // tree.
        for (workflowGen.task task : wf) {
//            System.out.println(task.hash());
            tree.add(task.hash());
        }
//        System.out.println();
        int levelOffset = 0; // Offset in the list where the currently processed
        // level starts.
        // Step through each level, stopping when we reach the root (levelSize
        // == 1).
        for (int levelSize = wf.size(); levelSize > 1; levelSize = (levelSize + 1) / 2) {
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

    public ArrayList<task> getWorkflow(){
        return workflow;
    }

}
