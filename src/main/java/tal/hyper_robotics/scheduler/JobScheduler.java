package tal.hyper_robotics.scheduler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import tal.hyper_robotics.entities.Job;
import tal.hyper_robotics.entities.JobState;

@Service
public class JobScheduler {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);

    private final MachineProcessor machineA = new MachineProcessor("A", 1, 10, logger);
    private final MachineProcessor machineB = new MachineProcessor("B", 2, 15, logger);
    private final MachineProcessor machineC = new MachineProcessor("C", 1, 20, logger);
    private final Queue<Job> doneJobs = new LinkedList<>();

    private final int NUM_OF_CONCURRENT_JOBS_PROCESSES = 2;
    private final ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_CONCURRENT_JOBS_PROCESSES);
    
     private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public JobScheduler() {
        machineA.start();
        machineB.start();
        machineC.start();
    }

    public void scheduleJobs() {
        executorService.submit(this::scheduleJobsInternal);
    }

    public static void sendJobChanged(Job job, SseEmitter emitter) {
        logger.info(
                "[JobScheduler.sendJobChanged] Sending jobUpdate for job " + job.getId() + " State: " + job.getState());
        try {

            ObjectMapper mapper = new ObjectMapper();
            String jobJson = mapper.writeValueAsString(job);
            logger.info("[JobScheduler.sendJobChanged] Sending JSON: {}", jobJson);           


            emitter.send(SseEmitter.event().name("jobUpdated").data(job));
            logger.info("[JobScheduler.sendJobChanged] emitted job change for job {} and state {}", job.getId(),
                    job.getState());
        } catch (IOException e) {
            logger.error("[JobScheduler.sendJobChanged] Error while sending jobChanged", e);
        }
    }

    public void handleUserCreatedEvent(Job job) {
        for (SseEmitter emitter : emitters) {
            sendJobChanged(job, emitter);
        }
    }

    private void scheduleJobsInternal() {
        Job job = new Job();

        machineA.addJob(job);
        job.setState(JobState.IN_A_QUEUE);
        handleUserCreatedEvent(job);
        Job machineADoneJob = machineA.getFinishedJob();
        logger.info("[JobScheduler.scheduleJobsInternal] Machine A finished processing job {}",
                machineADoneJob.getId());
        handleUserCreatedEvent(job);

        machineB.addJob(machineADoneJob);
        job.setState(JobState.IN_B_QUEUE);
        handleUserCreatedEvent(job);
        Job machineBDoneJob = machineB.getFinishedJob();
        handleUserCreatedEvent(job);
        logger.info("[JobScheduler.scheduleJobsInternal] Machine B finished processing job {}",
                machineADoneJob.getId());

        machineC.addJob(machineBDoneJob);
        job.setState(JobState.IN_C_QUEUE);
        handleUserCreatedEvent(job);
        Job machineCDoneJob = machineC.getFinishedJob();
        logger.info("[JobScheduler.scheduleJobsInternal] Machine C finished processing job {}",
                machineCDoneJob.getId());
        job.setState(JobState.FINISHED);
        doneJobs.add(machineCDoneJob);
        handleUserCreatedEvent(job);
    }

    public SseEmitter registerToJobUpdates() {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        emitters.add(emitter);
        logger.info("Added new emitter");
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            logger.info("SseEmitter completed. Total emitters: {}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            logger.warn("SseEmitter timed out. Total emitters: {}", emitters.size());
        });
        emitter.onError((e) -> {
            emitters.remove(emitter);
            logger.error("SseEmitter error: {}. Total emitters: {}", e.getMessage(), emitters.size());
        });
        return emitter;
    }
}