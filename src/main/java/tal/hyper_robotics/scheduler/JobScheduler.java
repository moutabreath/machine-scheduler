package tal.hyper_robotics.scheduler;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    private final ExecutorService  executorService = Executors.newFixedThreadPool(NUM_OF_CONCURRENT_JOBS_PROCESSES);
    private final SseEmitter sseEmitter = new SseEmitter();

    public JobScheduler(){
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
            emitter.send(SseEmitter.event().name("jobUpdated").data(job));
            logger.info("[JobScheduler.sendJobChanged] emitted job change for job {} and state {}", job.getId(), job.getState());
        } catch (IOException e) {
            logger.error("[JobScheduler.sendJobChanged] Error while sending jobChanged", e);
        }
    }

    private void scheduleJobsInternal() {
        Job job = new Job();

        machineA.addJob(job);
        job.setState(JobState.IN_A_QUEUE);
        sendJobChanged(job, sseEmitter);
        Job machineADoneJob = machineA.getFinishedJob();
        logger.info("[JobScheduler.scheduleJobsInternal] Machine A finished processing job {}", machineADoneJob.getId());
        sendJobChanged(machineADoneJob, sseEmitter);        

        machineB.addJob(machineADoneJob);
        job.setState(JobState.IN_B_QUEUE);
        sendJobChanged(machineADoneJob, sseEmitter);
        Job machineBDoneJob = machineB.getFinishedJob();
        sendJobChanged(machineBDoneJob, sseEmitter);
        logger.info("[JobScheduler.scheduleJobsInternal] Machine B finished processing job {}", machineADoneJob.getId());

        machineC.addJob(machineBDoneJob);
        job.setState(JobState.IN_C_QUEUE);
        sendJobChanged(machineBDoneJob, sseEmitter);
        Job machineCDoneJob = machineC.getFinishedJob();
        logger.info("[JobScheduler.scheduleJobsInternal] Machine C finished processing job {}", machineCDoneJob.getId());
        job.setState(JobState.FINISHED);
        doneJobs.add(machineCDoneJob);
        sendJobChanged(machineCDoneJob, sseEmitter);
    }


    public SseEmitter registerToJobUpdates() {
      return this.sseEmitter;
    }
}