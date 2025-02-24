package tal.hyper_robotics.scheduler;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;

import tal.hyper_robotics.entities.Job;

public class MachineProcessor {
    private final String id;
    private final int sleepTime;
    private final Queue<Job> pendingJobs;
    private final ExecutorService executorService;
    private boolean running;
    private final BlockingQueue<Job> finishedJobs = new LinkedBlockingQueue<>();
    private final Logger logger;

    public MachineProcessor(String machineId, int parallelExecNum, int sleepTime, Logger logger) {
        this.id = machineId;
        pendingJobs = new LinkedBlockingQueue<>();
        this.sleepTime = sleepTime;
        this.running = true;
        executorService = Executors.newFixedThreadPool(parallelExecNum);
        this.logger = logger;
        logger.info("[MachineProcessor.MachineProcessor] {} initialized with {} parallel executions and {} seconds sleep time.", machineId,
                parallelExecNum, sleepTime);
    }

    public void start() {
        executorService.submit(() -> {
            while (running) {
                try {
                    Job job = ((LinkedBlockingQueue<Job>) pendingJobs).take();
                    logger.info("[MachineProcessor.start] {} started processing job {}", id, job.getId());
                    executeJob(job);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("[MachineProcessor.start] {} was interrupted.", id, e);
                    break;
                }
            }
        });
    }

    public void addJob(Job job) {
        pendingJobs.add(job);
        logger.info("[MachineProcessor.addJob] {} added job {}", id, job.getId());
    }

    public void shutdown() {
        running = false;
        executorService.shutdown();
    }

    public void executeJob(Job job) {
        try {
            job.advanceState();
            Thread.sleep(sleepTime * 1000);
        } catch (InterruptedException e) {
            String message = String.format("[MachineProcessor.executeJob] {} was interrupted during job {} execution.",
                    id, job.id);
            logger.error(message, e);
            Thread.currentThread().interrupt();
        }
        logger.info("[MachineProcessor.executeJob] Job {} finished processing on MachineProcessor {}", job.getId(), id);
        job.advanceState();        
        finishedJobs.add(job);
    }

    public Job getFinishedJob() {
        Job job = null;
        try {
            job = finishedJobs.take();
        } catch (InterruptedException e) {
            String message = String.format(
                    "[MachineProcessor.getFinishedJob] {} was interrupted during job execution.", id);
            logger.error(message, e);
            Thread.currentThread().interrupt();
        }
        return job;
    }

    public String getId() {
        return id;
    }

}
