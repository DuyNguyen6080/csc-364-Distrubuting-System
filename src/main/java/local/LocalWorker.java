package local;

public class LocalWorker implements Runnable {
    private Buffer instanceBuffer = null;
    public LocalWorker(Buffer buffer) {
        System.out.println("Localworker Initialize");

        instanceBuffer = Buffer.getInstance();
    }

    Job getWork(){
        return instanceBuffer.getJob();
    }

    int doWork(Job job) {

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
        System.out.println("Localworker running");
        while (true) {
            Job temp_Job = getWork();
            if(temp_Job != null) {
                int result = doWork(temp_Job);
                System.out.println("\tLocalworker Job: " + temp_Job.getString());
                System.out.println("\t Localworker result: " + result);
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }


    }
}
