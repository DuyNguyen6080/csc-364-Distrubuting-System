package local;
import java.util.Vector;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        int cpuCore = Runtime.getRuntime().availableProcessors();

        Thread producerThread = new Thread(new Producer(Buffer.getInstance()));
        cpuCore--;

        Vector<Thread> LocalWorker = new Vector<Thread>();
        for(int i = 0; i < cpuCore; i++) {
            LocalWorker.add(new Thread(new LocalWorker(Buffer.getInstance())));
            cpuCore--;
        }

        Thread outsourcerThread = new Thread(Outsourcer.getInstance());
        cpuCore--;


        producerThread.start();
        outsourcerThread.start();
        for(int i = 0; i < LocalWorker.size(); i++) {
            LocalWorker.get(i).start();
            cpuCore--;
        }
    }
}