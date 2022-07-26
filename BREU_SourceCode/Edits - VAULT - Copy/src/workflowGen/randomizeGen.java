package workflowGen;

import java.util.Random;
import java.util.*;

public class randomizeGen {

    final static int MXWFS = 1;
    final static int MAXWFSIZE = 1000;
    static String startPoint;
    static ArrayList<workflow> workflows = new ArrayList<>();


    public randomizeGen() {
        workflows.add(new workflow(MAXWFSIZE,0,null,null));
        Random rand = new Random();
        for(int i = 1; i< MXWFS; i++) {
            workflow w = new workflow(MAXWFSIZE,i,startPoint,String.valueOf(rand.nextInt(i)));
            workflows.add(w);
            startPoint = w.forNextWf.getTaskID();
        }
        System.out.println("Hash AVG runtime: " + getHashRuntimeAvg());
        System.out.println("Merkle AVG runtime: " + getMerkleRuntimeAvg());
    }

    private double getHashRuntimes(){
        double hashRuntime=0;
        for (workflowGen.workflow workflow : workflows) {
            for (int j = 0; j < workflow.workflow.size(); j++) {
                hashRuntime += workflow.workflow.get(j).hashRuntime;
            }
        }
        return hashRuntime;
    }

    public double getHashRuntimeAvg(){ return getHashRuntimes()/(MXWFS * MAXWFSIZE); }

    private double getMerkleRuntimes(){
        double merkleRuntime =0;
        for (workflowGen.workflow workflow : workflows) {
            merkleRuntime += workflow.runtime;
        }
        return merkleRuntime;
    }

    public double getMerkleRuntimeAvg(){ return getMerkleRuntimes()/(MXWFS * MAXWFSIZE); }
    public ArrayList<workflow> getWorkflows(){
        return workflows;
    }


}

//verification
//exclude selecting a quorum
