package local;

public class LocalWorker implements Runnable {
    private Buffer instanceBuffer = null;
    public LocalWorker(Buffer buffer) {
        instanceBuffer = Buffer.getInstance();
    }

    Job getWork(){
        return instanceBuffer.getJob();
    }

    int doWork() {
        Job job = getWork();

        Integer operand1 = job.getIntOperand1();
        String operator = job.getOperator();
        Integer operand2 = job.getIntOperand2();
        switch (operator) {
            case "+":
                return operand1 + operand2;
            case "-":
                return operand1 - operand2;
            case "*":
                return operand1 * operand2;
            case "/":
                return  operand1 / operand2;
            default:
                return -999999999;
        }
    }

    @Override
    public void run() {

        while (!instanceBuffer.isEmpty()) {
            int result = doWork();
            System.out.println("Local worker result: " + result);
        }


    }
}
