package local;

public class Producer implements Runnable{
    private String problem;
    private Buffer instanceBuffer;
    private static final long PRODUCE_INTERVAL_MS = 1000;
    public Producer(Buffer buffer) {
        this.instanceBuffer = Buffer.getInstance();

    }

    @Override
    public void run() {
        Integer i = 0;
        try {
            while (true) {
                String operand1 = i.toString();
                String operator = "+";
                String operand2 = i.toString();
                Job tempJob = new Job(operand1, operator, operand2);
                //System.out.println("Producer producing porblem: " + tempJob.getString() + " ENcode into: " + tempJob.getEncode());
                instanceBuffer.addJob(tempJob);
                i++;
                Thread.sleep(PRODUCE_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
