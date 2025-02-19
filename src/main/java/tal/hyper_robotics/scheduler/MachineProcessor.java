package tal.hyper_robotics.scheduler;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import tal.hyper_robotics.entities.Job;
import tal.hyper_robotics.entities.JobState;

public class MachineProcessor {
    private final String id;
    private final int sleepTime;
    private final Queue<Job> pendingJobs;
    private final ExecutorService executorService;
    private boolean running;
    private JobListener jobListener;
    private final BlockingQueue<Job> finishedJobs = new LinkedBlockingQueue<>();

    public MachineProcessor(String machineId, int parallelExecNum, int sleepTime){
        this.id = machineId;
        pendingJobs = new LinkedBlockingQueue<>();
        this.sleepTime = sleepTime;
        this.running = true;
        executorService = Executors.newFixedThreadPool(parallelExecNum); 
        startJobProcessor();
    }

    private void startJobProcessor() {
        executorService.submit(() -> {
            while (running) {
                try {
                    Job job = ((LinkedBlockingQueue<Job>) pendingJobs).take(); // Take job from queue (blocks if empty)
                    executeJob(job);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }



    public void addJob(Job job) {
        pendingJobs.add(job);
    }

    public void shutdown() {
        running = false;
        executorService.shutdown();
    }


    public void executeJob(Job job){
        try {
            Thread.sleep(sleepTime * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        job.setState(JobState.FINISHED);
        finishedJobs.add(job);
    }

    public Job getFinishedJob(){
        return finishedJobs.poll();
    }

    public String getId(){
        return id;
    }
    
}
