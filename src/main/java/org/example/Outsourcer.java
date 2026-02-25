package org.example;

import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Outsourcer implements Runnable, MqttCallback {

    private final String BROKER_URL = "tcp://test.mosquitto.org:1883";

    private final String topic_request = "works/request";
    private final String topic_assign = "works/assign";
    private Queue<String> workers = new LinkedList<>();
    private Queue<String> result = new LinkedList<>();
    MqttClient client = null;
    @Override
    public void run() {

        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(BROKER_URL, clientId);
            client.setCallback(this);
            client.connect();
            System.out.println("Oursourcer: "+ clientId + " ↗️ Connected to broker: " + BROKER_URL);

            client.subscribe(topic_request, 2);
            int counter;
            {

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
    public static Map<String, String> parseKV(String s) {
        Map<String, String> m = new HashMap<>();
        for (String part : s.split(";")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) m.put(kv[0].trim(), kv[1].trim());
        }
        return m;
    }
    public void sendWork(String CurrentWorkerId, String works) {
        try {
            if (client.isConnected()) {
                String temp_topic = topic_assign + "/" + CurrentWorkerId;
                MqttMessage test_msg = new MqttMessage(works.getBytes());
                test_msg.setQos(2);
                client.publish(temp_topic, test_msg);
                System.out.println("Sending work: " + new String(test_msg.getPayload()) + " to " + CurrentWorkerId);
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


        if (topic.equals(this.topic_assign)) {
            System.out.println("sourcer get result: " + payload);
            //  System.out.println("📥 Delivery :: " + "[" + topic + " : " + message.getQos() + "] :: " + payload);

        }
        if (topic.equals(this.topic_request)) {
            Map<String, String> m = parseKV(payload);
            String workerId = m.get("workerId");
            int capacity = Integer.parseInt(m.get("capacity"));

            System.out.println("Delivery: " + " topic : " + topic + " workerId " + workerId + " capacity: " + capacity);
            //  System.out.println("📥 Delivery :: " + "[" + topic + " : " + message.getQos() + "] :: " + payload);
            workers.add(workerId);
            String temp_worker_Id = workers.poll();
            sendWork(temp_worker_Id,"123 testing");
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            System.out.println("📥 outsourcer Delivery complete for: " + token.getMessageId());
        } catch (Exception e) {
            System.out.println("📥 Delivery complete, but failed to get message ID.");
        }
    }


}