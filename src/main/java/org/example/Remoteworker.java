package org.example;

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
            System.out.println("Remote workker: " + workerId + " ↗️ Connected to broker: " + BROKER_URL);

            int counter;
            {
                if(client.isConnected()) {
                    client.publish(topic_request, message);

                    System.out.println("Remote workker: "+"↗️ published to " + topic_request + ": " + message);

                }
                Thread.sleep(1000);
            }
        } catch (MqttException e) {
            System.out.println("↗️ MQTT error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("↗️ Demo interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            if (client == null && client.isConnected()) {
                try {
                    client.disconnect();
                    System.out.println("↗️ Disconnected from broker.");
                }catch (MqttException e) {
                    System.err.println("↗️ Error disconnecting: " + e.getMessage());
                }
            }
        }
    }
    public String doWork(String work) {
        return "1999";
    }
    public void sendresult(String result){
        try {
            if (client.isConnected()) {
                String temp_topic = topic_assign ;
                MqttMessage test_msg = new MqttMessage(result.getBytes());
                test_msg.setQos(2);
                client.publish(temp_topic, test_msg);
                System.out.println("Sending result from worker: " + new String(test_msg.getPayload()));
            }
        } catch (MqttException e) {
            System.out.println("↗️ MQTT error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("📥 Connection to broker lost: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        System.out.println("Remote worker got " + " 📥 Delivery :: " + "[" + topic + " : " + message.getQos() + "] :: " + payload);
        if (topic.equals(topic_assign + "/" + client.getClientId())) {
            String result = doWork(payload);
            sendresult(result);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            System.out.println("📥 Remoteworker Delivery complete for: " + token.getMessageId());
        } catch (Exception e) {
            System.out.println("📥 Delivery complete, but failed to get message ID.");
        }
    }


}