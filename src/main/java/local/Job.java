package local;

public class Job {
    String operand1;
    String operator;
    String operand2;
    public Job(String operand1, String operator, String operand2) {
        this.operand1 = operand1;
        this.operator = operator;
        this.operand2 = operand2;
    }
    public String getString() {
        return operand1 + operator + operand2;
    }

    public String getEncode() {
        String  op_number;
        switch (operator) {
            case "+":
                op_number = " 1 ";
                break;
            case "-":
                op_number = " 2 ";
                break;
            case "*":
                op_number = " 3 ";
                break;
            case "/":
                op_number = " 4 ";
                break;
            default:
                op_number = " -1 ";
        }
        return operand1 + op_number + operand2;
    }
    public String getDecode() {
        String  op_number;
        switch (operator) {
            case "1":
                op_number = "+";
                break;
            case "2":
                op_number = "-";
                break;
            case "3":
                op_number = "*";
                break;
            case "4":
                op_number = "/";
                break;
            default:
                op_number = "-1";
        }
        return operand1 + op_number + operand2;
    }

    public String getOperand1() {
        return operand1;
    }
    public int getIntOperand1() {
        return Integer.parseInt(operand1);
    }

    public String getOperand2() {
        return operand2;
    }
    public int getIntOperand2() {
        return Integer.parseInt(operand2);
    }
    public String getOperator() {
        return operand1;
    }
    public int getIntOperator() {
        return Integer.parseInt(operator);
    }
}
