package remote;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Outsourcer outsourcer = new Outsourcer();
        Remoteworker remoteworker1 = new Remoteworker();
        Remoteworker remoteworker2 = new Remoteworker();
        Thread sourcer = new Thread(outsourcer);
        Thread woker1 = new Thread(remoteworker1);
        Thread woker2 = new Thread(remoteworker2);
        woker1.start();
        woker2.start();
        sourcer.start();


    }
}