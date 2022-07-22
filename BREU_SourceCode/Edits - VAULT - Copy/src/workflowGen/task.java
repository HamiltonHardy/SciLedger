package workflowGen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class task {
    private String workflowID;
    private String taskID;
    private boolean invalidated;
    private ArrayList<Integer> idxParent;
    private ArrayList<String> tree;
    public task(String workflowID, String taskID, boolean invalidated, ArrayList<Integer> idxParent){
        this.workflowID = workflowID;
        this.taskID = taskID;
        this.invalidated = invalidated;
        this.idxParent=idxParent;
    }

    public String getTaskID() {
        return taskID;
    }

    public ArrayList<Integer> getIdxParent() {
        return idxParent;
    }

    public void addIdxParent(int parent) {
        this.idxParent.add(parent);
    }

    public void setMerkleTree(ArrayList<String> tree){
        this.tree=tree;
    }
    public String hash(){
        String taskHash = this.toString();
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(digest.digest(taskHash.getBytes(StandardCharsets.UTF_8)));
    }
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(this.workflowID + "\n" + this.taskID + "\n" + this.invalidated + "\n");
        for (Integer integer : this.idxParent) {
            str.append(integer + ", ");
        }
        if(this.tree!=null){
            str.append("\n" + this.tree);
        }
        return str.toString();
    }
}
