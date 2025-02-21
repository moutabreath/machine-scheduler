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
import tal.hyper_robotics.entities.Job;

@Service
public class JobScheduler {

    private final MachineProcessor machineA;
    private final MachineProcessor machineB;
    private final MachineProcessor machineC;
    private final Queue<Job> doneJobs;
    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(JobScheduler.class);
    private final int NUM_OF_CONCURRENT_JOBS_PROCESSES = 2;
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public JobScheduler(){
        machineA = new MachineProcessor("A", 1, 10, logger);
        machineB = new MachineProcessor("B", 2, 15, logger);
        machineC = new MachineProcessor("C", 1, 20, logger);
        executorService = Executors.newFixedThreadPool(NUM_OF_CONCURRENT_JOBS_PROCESSES); 
        doneJobs = new LinkedList<>();
    }

    public void scheduleJobs() {
        executorService.submit(this::scheduleJobsInternal);
    }

    private void handleUserCreatedEvent(Job job) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("jobUpdated").data(job));
                logger.info("emitted job change for {}", job.getId());
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    private void scheduleJobsInternal() {
        Job job = new Job();

        machineA.addJob(job);
        handleUserCreatedEvent(job);
        logger.info("JobScheduler Machine A started processing job {}", job.getId());
        Job machineADoneJob = machineA.getFinishedJob();
        handleUserCreatedEvent(machineADoneJob);
        logger.info("JobScheduler Machine A finished processing job {}", machineADoneJob.getId());

        machineB.addJob(machineADoneJob);
        handleUserCreatedEvent(machineADoneJob);
        Job machineBDoneJob = machineB.getFinishedJob();
        handleUserCreatedEvent(machineBDoneJob);
        logger.info("JobScheduler Machine B finished processing job {}", machineADoneJob.getId());

        machineC.addJob(machineBDoneJob);
        handleUserCreatedEvent(machineBDoneJob);
        Job machineCDoneJob = machineC.getFinishedJob();
        logger.info("JobScheduler Machine C finished processing job {}", machineCDoneJob.getId());
        doneJobs.add(machineCDoneJob);
        handleUserCreatedEvent(machineCDoneJob);
    }


    public SseEmitter streamJobsAdded() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));
        return emitter;
    }
}