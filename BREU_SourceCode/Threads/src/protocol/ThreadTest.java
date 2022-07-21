package protocol;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

public class ThreadTest {
    Socket[] sockets;
    ObjectInputStream[] in;
    ObjectOutputStream[] out;
    public static int counter = 0;
    public static void main(String args[]) {
        Thread blah = new Thread(new ThreadThing(0, "", 0));
        Thread blah2 = new Thread(new ThreadThing(0, "", 1));
        blah.start();
        blah2.start();
        //other code here

        try {
            blah.join();
            blah2.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //all threads are done and sockets, in, and out should be populated.
        System.out.println(counter);
    }


}

class ThreadThing implements Runnable{

    int port;
    String ip;
    int index;

    ThreadThing(int port, String ip, int index){
        this.port = port;
        this.ip = ip;
        this.index = index;
    }
    //Code to execute thread must be in run()
    @Override
    public void run() {
        System.out.println("Thread is running");
        //Connect to ip and port
        //save socket to sockets[index]
        //same with input output
    }

}

class DataTransmit implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -1166758634559758301L;

    String data;
    transient String privateData;
}
