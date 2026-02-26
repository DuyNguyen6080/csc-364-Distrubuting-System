package remote;

import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.Map;

public class Remoteworker implements Runnable, MqttCallback {

    private final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    private final String topic_request = "works/request";
    private final String topic_assign = "works/assign";
    private String workerId;
    private int cpuCores;
    MqttClient client = null;
    @Override
    public void run() {

        try {
            workerId = MqttClient.generateClientId();
            cpuCores = Runtime.getRuntime().availableProcessors();
            Map<String, Object> map = new HashMap<>();
            map.put("workerId", workerId);
            map.put("capacity", cpuCores);
            String request = "workerId=" + workerId + ";capacity=" + cpuCores;

            //String request = "{\"workerId\": \"" + workerId + "\",\"capacity\":" + cpuCores + "}";
            MqttMessage message = new MqttMessage(request.getBytes());
            message.setQos(2);
            client = new MqttClient(BROKER_URL, workerId);
            client.setCallback(this);
            client.connect();
            client.subscribe(topic_assign + "/" + client.getClientId());
            System.out.println("Remoteworker: " + workerId + " ↗️ Connected to broker: " + BROKER_URL);

            int counter;
            while (true){
                if(client.isConnected()) {
                    client.publish(topic_request, message);

                    System.out.println("Remoteworker: "+"↗️ published to " + topic_request + ": " + message);

                }
                Thread.sleep(1000);
            }
        } catch (MqttException e) {
            System.out.println("↗️Remoteworker MQTT error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("↗️Remoteworker Demo interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            if (client == null && client.isConnected()) {
                try {
                    client.disconnect();
                    System.out.println("↗️Remoteworker Disconnected from broker.");
                }catch (MqttException e) {
                    System.err.println("↗️Remoteworker Error disconnecting: " + e.getMessage());
                }
            }
        }
    }
    public Integer doWork(String work) {

        String[] words = work.split(" ");
        Integer operand1 = Integer.parseInt(words[0]);
        String operator = words[1];
        Integer operand2 = Integer.parseInt(words[2]);
        return switch (operator) {
            case "1" -> (operand1 + operand2);
            case "2" -> operand1 - operand2;
            case "3" -> operand1 * operand2;
            case "4" -> operand1 / operand2;
            default -> -999999999;
        };
    }
    public void sendresult(String result){
        try {
            if (client.isConnected()) {
                String temp_topic = topic_assign ;
                MqttMessage test_msg = new MqttMessage(result.getBytes());
                test_msg.setQos(2);
                client.publish(temp_topic, test_msg);
                System.out.println("\tRemoteworker Sending back result: " + new String(test_msg.getPayload()) + " from topic" + client.getClientId());
            }
        } catch (MqttException e) {
            System.out.println("↗️Remoteworker MQTT error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("📥Remoteworker Connection to broker lost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("Remoteworker receive " + " 📥 Delivery :: " + "[" + topic + " : " + message.getQos() + "] :: " + payload);
        if (topic.equals(topic_assign + "/" + client.getClientId())) {
            Integer Int_result = doWork(payload);
            String result = Int_result.toString();
            sendresult(result);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            //System.out.println("📥 Remoteworker Delivery complete for: " + token.getMessageId());
        } catch (Exception e) {
            System.out.println("📥 Remoteworker Delivery complete, but failed to get message ID.");
        }
    }


}