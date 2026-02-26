package local;
import java.util.Vector;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        int cpuCore = Runtime.getRuntime().availableProcessors();

        Thread producerThread = new Thread(new Producer(Buffer.getInstance()));


        Vector<Thread> LocalWorker = new Vector<Thread>();
        for(int i = 0; i < cpuCore/2; i++) {
            LocalWorker.add(new Thread(new LocalWorker(Buffer.getInstance())));

        }

        Thread outsourcerThread = new Thread(Outsourcer.getInstance());



        producerThread.start();

        outsourcerThread.start();
        for(int i = 0; i < LocalWorker.size(); i++) {
            LocalWorker.get(i).start();

        }
    }
}