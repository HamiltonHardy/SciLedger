package workflowGen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import de.svenjacobs.loremipsum.LoremIpsum;

public class task {
    static int SIZELOREMIPSUM = 5000;
    private String workflowID;
    private String taskID;
    private boolean invalidated;
    private ArrayList<Integer> idxParent;
    private ArrayList<String> validTree;
    private ArrayList<String> invalidTree;
    private String inData;
    private String outData;
    public task(String workflowID, String taskID, boolean invalidated, ArrayList<Integer> idxParent){
        this.workflowID = workflowID;
        this.taskID = taskID;
        this.invalidated = invalidated;
        this.idxParent=idxParent;
        this.inData = getLoremHash(SIZELOREMIPSUM);
        this.outData = getLoremHash(SIZELOREMIPSUM + 1);
    }

    public int getIdxParent(int index) {
        if (idxParent==null){
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(-2);
            return list.get(0);
        }else{

            return idxParent.get(index);
        }
    }
    public ArrayList<Integer> getIdxParent() {
        if (idxParent==null){
            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(-2);
            return list;
        }else{

            return idxParent;
        }
    }
    public String getLoremHash(int size){
        return hash(new LoremIpsum().getWords(size));
    }
    public String getTaskID() {
        return taskID;
    }

    public void addIdxParent(int parent) {
        this.idxParent.add(parent);
    }

    public void setValidTree(ArrayList<String> validTree){
        this.validTree = validTree;
    }
    public void setInvalidTree(ArrayList<String> invalidTree){this.invalidTree = invalidTree;}

    public boolean isInvalidated(){
        return invalidated;
    }
    public ArrayList<String> getValidTree(){
        return this.validTree;
    }
    public ArrayList<String> getInvalidTree(){
        return this.invalidTree;
    }
    public String hash(String str){
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(digest.digest(str.getBytes(StandardCharsets.UTF_8)));
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
        StringBuilder str = new StringBuilder(this.workflowID + "\n" + this.taskID + "\n" + this.invalidated + "\n" + this.inData + "\n" + this.outData);
        str.append("Parents\n");
        for (Integer integer : this.idxParent) {
            str.append(integer + "\n");
        }
            str.append("Valid Tree" + this.validTree + "\n");

            str.append("Invalid Tree" + this.invalidTree);
        return str.toString();
    }


    public ArrayList<String> toProvenanceRecord(){
        ArrayList<String> provenanceRecord = new ArrayList<>();

        provenanceRecord.add(this.idxParent.toString());
        provenanceRecord.add(this.validTree.get(this.validTree.size()-1));
        provenanceRecord.add(this.invalidTree.get(this.invalidTree.size()-1));

        provenanceRecord.add(this.workflowID);
        provenanceRecord.add(this.taskID);
        provenanceRecord.add(Boolean.toString(this.invalidated));
        provenanceRecord.add(inData);
        provenanceRecord.add(outData);




        return provenanceRecord;
    }
}
