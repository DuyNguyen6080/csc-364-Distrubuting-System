package remote;

import local.Outsourcer;
import java.util.Vector;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        int cpuCore = Runtime.getRuntime().availableProcessors();
        //System.out.println("cpu available: " + cpuCore );
        Vector<Thread> remoteWorkerThread = new Vector<>();
        for(int i = 0; i < 1; i++) {
            remoteWorkerThread.add(new Thread(new Remoteworker()));
        }
        for(int i = 0 ; i < remoteWorkerThread.size(); i++) {
            remoteWorkerThread.get(i).start();
        }





    }
}