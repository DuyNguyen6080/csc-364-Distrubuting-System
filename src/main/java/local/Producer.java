package local;

public class Producer implements Runnable{
    private String problem;
    private Buffer instanceBuffer;
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
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
