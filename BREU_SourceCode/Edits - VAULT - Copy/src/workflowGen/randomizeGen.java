package workflowGen;

import java.util.Random;
import java.util.*;

public class randomizeGen {
    final int MXWFS;
    final static int MAXWFSIZE = 5001;
    String startPoint;
    ArrayList<workflow> workflows = new ArrayList<>();


    public randomizeGen(int maxWorkflows) {
        MXWFS = maxWorkflows;
        workflows.add(new workflow(MAXWFSIZE,0,null,null));
        Random rand = new Random();
        for(int i = 1; i< MXWFS; i++) {
            workflow w = new workflow(MAXWFSIZE,i,startPoint,String.valueOf(rand.nextInt(i)));
            workflows.add(w);
            startPoint = w.forNextWf.getTaskID();
        }
//        System.out.println("Hash AVG runtime: " + getHashRuntimeAvg());
//        System.out.println("Merkle AVG runtime: " + getMerkleRuntimeAvg());
    }

    private long getHashRuntimes(){
        long hashRuntime=0;
        for (workflowGen.workflow workflow : workflows) {
            for (int j = 0; j < workflow.workflow.size(); j++) {
                hashRuntime += workflow.workflow.get(j).hashRuntime;
            }
        }
        return hashRuntime;
    }

    public long getHashRuntimeAvg(){ return getHashRuntimes()/(MXWFS * MAXWFSIZE); }

    private long getMerkleRuntimes(){
        long merkleRuntime =0;
        for (workflowGen.workflow workflow : workflows) {
            merkleRuntime += workflow.runtime;
        }
        return merkleRuntime;
    }

    public long getMerkleRuntimeAvg(){ return getMerkleRuntimes()/(MXWFS * MAXWFSIZE); }
    public ArrayList<workflow> getWorkflows(){
        return workflows;
    }


}

//verification
//exclude selecting a quorum
