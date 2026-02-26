package local;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Buffer {

    private static  Buffer buffer = null;
    private Semaphore semaphore = new Semaphore(10);
    private final ReentrantLock lock = new ReentrantLock();
    private Queue<Job> problem = new LinkedList<>();
    private Buffer() {}
    public static Buffer getInstance() {
        if (buffer == null) {
            buffer = new Buffer();
        }
        return buffer;
    }
    public boolean isEmpty(){
        return problem.isEmpty();
    }
    public void addJob(Job job) {
        lock.lock();
        try {
            problem.add(job);
           // System.out.println("Buffer add " + job.getString());
        }catch (Exception e) {
            //System.out.println("Buffer error addJob" + job.getString() );
        }
        lock.unlock();
    }
    public Job getJob() {
        Job job = null;
        try {
            semaphore.acquire();
            job = problem.poll();
            //System.out.println("Buffert getJob: " + job.getString());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.out.println("getJob(): " + e);
        }finally {
            semaphore.release();
        }
        return job;
    }
}
