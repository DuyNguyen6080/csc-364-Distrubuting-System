package local;

import org.eclipse.paho.client.mqttv3.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;


public class Outsourcer implements Runnable, MqttCallback {

    private final String BROKER_URL    = "tcp://test.mosquitto.org:1883";
    private final String topic_request = "works/request";
    private final String topic_assign  = "works/assign";

    private static Outsourcer outsourcer = null;

    private final Queue<String> workers = new LinkedList<>();
    private final Queue<Job>    jobQueue = new LinkedList<>();

    private final Map<String, Job>  pendingJobs      = new ConcurrentHashMap<>();
    private final Map<String, Long> pendingTimestamp = new ConcurrentHashMap<>();

    private static final long TIMEOUT_MS = 3000; // 5 seconds before a job is re-queued

    private MqttClient client = null;
    private ScheduledExecutorService timerService;

    private Outsourcer() {}

    public static Outsourcer getInstance() {
        if (outsourcer == null) {
            outsourcer = new Outsourcer();
        }
        return outsourcer;
    }

    @Override
    public void run() {
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(BROKER_URL, clientId);
            client.setCallback(this);
            client.connect();
            System.out.println("Outsourcer: " + clientId + "  Connected to " + BROKER_URL);

            client.subscribe(topic_request, 2);
            client.subscribe(topic_assign,  2);


            timerService = Executors.newSingleThreadScheduledExecutor();
            timerService.scheduleAtFixedRate(this::checkTimeouts, 1, 1, TimeUnit.SECONDS);

            while (true) {
                // Pull a job from the shared Buffer and hold it ready to assign
                if (jobQueue.isEmpty()) {
                    Job job = Buffer.getInstance().getJob();
                    if (job != null) {
                        jobQueue.add(job);
                        System.out.println("Outsourcer: picked up job -> " + job.getString());
                    }
                }
                Thread.sleep(500);
            }

        } catch (MqttException e) {
            System.out.println("Outsourcer MQTT error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (client != null && client.isConnected()) {
                try {
                    timerService.shutdown();
                    client.disconnect();
                    System.out.println("Outsourcer disconnected.");
                } catch (MqttException e) {
                    System.err.println("Outsourcer error disconnecting: " + e.getMessage());
                }
            }
        }
    }

    private void checkTimeouts() {
        long now = System.currentTimeMillis();

        List<String> timedOutWorkers = new ArrayList<>();

        for (Map.Entry<String, Long> entry : pendingTimestamp.entrySet()) {
            if (now - entry.getValue() > TIMEOUT_MS) {
                timedOutWorkers.add(entry.getKey());
            }
        }

        for (String workerId : timedOutWorkers) {
            Job timedOutJob = pendingJobs.remove(workerId);
            pendingTimestamp.remove(workerId);

            if (timedOutJob != null) {
                System.out.println("Outsourcer: TIMEOUT — worker " + workerId
                        + " went silent, re-queuing job: " + timedOutJob.getString());
                ((LinkedList<Job>) jobQueue).addFirst(timedOutJob);
            }
        }


        System.out.println("Outsourcer Timer: " + pendingJobs.size()
                + " jobs pending, " + jobQueue.size() + " jobs waiting to assign");
    }

    public void sendWork(String workerId, String encodedJob) {
        try {
            if (client != null && client.isConnected()) {
                String workerTopic = topic_assign + "/" + workerId;
                MqttMessage msg = new MqttMessage(encodedJob.getBytes());
                msg.setQos(2);
                client.publish(workerTopic, msg);
                System.out.println("Outsourcer: sent job to " + workerId + " -> " + encodedJob);
            }
        } catch (MqttException e) {
            System.out.println("Outsourcer MQTT error: " + e.getMessage());
        }
    }

    // Parses "key=value;key=value" strings into a Map
    public static Map<String, String> parseKV(String s) {
        Map<String, String> m = new HashMap<>();
        for (String part : s.split(";")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) m.put(kv[0].trim(), kv[1].trim());
        }
        return m;
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());

        if (topic.equals(topic_request)) {
            // A RemoteWorker is requesting work (Pull model)
            Map<String, String> m = parseKV(payload);
            String workerId = m.get("workerId");

            System.out.println("Outsourcer: worker available -> " + workerId);
            workers.add(workerId);

            // If we have a job ready, assign it immediately
            if (!jobQueue.isEmpty()) {
                String nextWorker = workers.poll();
                Job job = jobQueue.poll();


                pendingJobs.put(nextWorker, job);
                pendingTimestamp.put(nextWorker, System.currentTimeMillis());

                sendWork(nextWorker, job.getEncode());
            }
        }

        if (topic.equals(topic_assign)) {

            Map<String, String> m = parseKV(payload);
            String workerId = m.get("workerId");
            String result   = m.get("result");

            if (workerId != null && pendingJobs.containsKey(workerId)) {
                Job originalJob = pendingJobs.remove(workerId);
                pendingTimestamp.remove(workerId);
                System.out.println("Outsourcer: result for job ["
                        + originalJob.getString() + "] = " + result
                        + " (from worker " + workerId + ")");
            } else {
                System.out.println("Outsourcer: received result -> " + payload);
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("Outsourcer: connection lost -> " + cause.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}