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
    private final String workflowID;
    private String taskID;
    private boolean invalidated;
    private ArrayList<Integer> idxParent;
    private ArrayList<String> validTree;
    private ArrayList<String> invalidTree;
    private final String inData;
    private final String outData;
    public long hashRuntime =0;
    public task(String workflowID, String taskID, boolean invalidated, ArrayList<Integer> idxParent){
        this.workflowID = workflowID;
        this.taskID = taskID;
        this.invalidated = invalidated;
        this.idxParent=idxParent;

        this.inData = getLoremHash(SIZELOREMIPSUM);
        this.outData = getLoremHash(SIZELOREMIPSUM + 1);
        this.validTree = new ArrayList<>();
        validTree.add("-1");
        this.invalidTree = new ArrayList<>();
        invalidTree.add("-1");
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
            return idxParent;
    }

    public String getLoremHash(int size){
        LoremIpsum loremIpsum = new LoremIpsum();
        String str = loremIpsum.getParagraphs(size);
        long sTime = System.nanoTime();
        String hash = hash(str);
        long stpTime = System.nanoTime();
//        System.out.println("Input & Output Hash computation time: " + (stpTime-sTime));
        hashRuntime = (stpTime-sTime);
        return hash;
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

    public long getHashRuntime() {
        return hashRuntime;
    }

    public String hash(String str){
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(digest.digest(str.getBytes(StandardCharsets.UTF_8)));
    }
    public String hash(){
        String taskHash = this.toString();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return Base64.getEncoder().encodeToString(digest.digest(taskHash.getBytes(StandardCharsets.UTF_8)));
    }
//    @Override
//    public String toString() {
//        StringBuilder str = new StringBuilder(this.workflowID + "\n" + this.taskID + "\n" + this.invalidated + "\n" + this.inData + "\n" + this.outData);
//        str.append("Parents\n");
//        for (Integer integer : this.idxParent) {
//            str.append(integer + "\n");
//        }

//        return str.toString();
//    }


    public ArrayList<String> toProvenanceRecord(){
        ArrayList<String> provenanceRecord = new ArrayList<>();
        provenanceRecord.add(this.idxParent.toString());
//        int validTreeHeight = (int) Math.ceil(Math.log(this.validTree.size()) / Math.log(2) + 1);
//        int invalidTreeHeight = (int)Math.ceil(Math.log(this.invalidTree.size()) / Math.log(2) + 1);
//        provenanceRecord.add((int)Math.ceil(Math.log(this.validTree.size()) / Math.log(2)) + 1 + "");
//        provenanceRecord.add((int)Math.ceil(Math.log(this.invalidTree.size()) / Math.log(2)) + 1 + "");
        provenanceRecord.add(this.validTree.size() + "");
        provenanceRecord.add(this.invalidTree.size() + "");

//        System.out.println();
//        System.out.println(this.taskID);
//        System.out.println("Valid tree size: " + this.validTree.size());
//        System.out.println("Valid tree height: " + validTreeHeight);
//////
//        System.out.println("invalid tree size: " + this.invalidTree.size());
//        System.out.println("invalid tree height: " + invalidTreeHeight);
//        System.out.println();

        provenanceRecord.add(this.validTree.get(this.validTree.size()-1));
        provenanceRecord.add(this.invalidTree.get(this.invalidTree.size()-1));
        provenanceRecord.add(this.workflowID);
        provenanceRecord.add(this.taskID);
        provenanceRecord.add(Boolean.toString(this.invalidated));
        provenanceRecord.add(inData);
        provenanceRecord.add(outData);
//
//        System.out.println("Valid Tree" + this.validTree + "\n");
//
//        System.out.println("Invalid Tree" + this.invalidTree);

        return provenanceRecord;
    }
}
