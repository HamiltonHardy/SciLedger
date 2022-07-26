package workflowGen;

import java.util.Random;
import java.util.*;


public class randomizeGen {
    static int maxWorkflows = 10;

    static String startPoint;
    static ArrayList<workflow> workflows = new ArrayList<>();


    public randomizeGen() {
        workflows.add(new workflow(0,null,null));
        Random rand = new Random();
        for(int i=1; i<maxWorkflows; i++) {
            workflow w = new workflow(i,startPoint,String.valueOf(rand.nextInt(i)));
            workflows.add(w);
            startPoint = w.forNextWf.getTaskID();
        }
    }



    public ArrayList<workflow> getWorkflows(){
        return workflows;
    }


}

//verification
//exclude selecting a quorum
