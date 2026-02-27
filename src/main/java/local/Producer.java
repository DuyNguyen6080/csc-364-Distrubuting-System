package local;

import java.util.Random;

public class Producer implements Runnable {
    private Buffer instanceBuffer;
    private static final long PRODUCE_INTERVAL_MS = 1000;

    private static final int MIN_VALUE = 1;
    private static final int MAX_VALUE = 100;

    private final Random random = new Random();

    public Producer(Buffer buffer) {
        this.instanceBuffer = Buffer.getInstance();
    }

    private int randomOperand() {
        return random.nextInt(MAX_VALUE - MIN_VALUE + 1) + MIN_VALUE;
    }
    private String randomOperator() {
        String[] operators = {"+", "-", "*", "/"};
        return operators[random.nextInt(operators.length)];
    }

    @Override
    public void run() {
        try {
            while (true) {
                int operand1 = randomOperand();
                int operand2 = randomOperand();
                String operator = randomOperator();

                if (operator.equals("/") && operand2 == 0) {
                    operand2 = 1;
                }

                Job tempJob = new Job(
                        String.valueOf(operand1),
                        operator,
                        String.valueOf(operand2)
                );

                System.out.println("Producer: created job -> " + tempJob.getString());
                instanceBuffer.addJob(tempJob);
                Thread.sleep(PRODUCE_INTERVAL_MS);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}