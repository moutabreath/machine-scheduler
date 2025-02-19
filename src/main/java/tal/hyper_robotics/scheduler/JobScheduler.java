package tal.hyper_robotics.scheduler;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public JobScheduler(){
        machineA = new MachineProcessor("A", 1, 10, logger);
        machineB = new MachineProcessor("B", 2, 15, logger);
        machineC = new MachineProcessor("C", 1, 20, logger);
        executorService = Executors.newFixedThreadPool(NUM_OF_CONCURRENT_JOBS_PROCESSES); 
        doneJobs = new LinkedList<>();
    }

    public void scheduleJobs() {
        executorService.submit(() -> {
            scheduleJobsInternal();
        });
    }

    private void scheduleJobsInternal() {
        Job job = new Job();
        machineA.addJob(job);
        logger.info("JobScheduler Machine A started processing job {}", job.getId());
        Job machineADoneJob = machineA.getFinishedJob();
        logger.info("JobScheduler Machine A finished processing job {}", machineADoneJob.getId());

        machineB.addJob(machineADoneJob);
        Job machineBDoneJob = machineB.getFinishedJob();
        logger.info("JobScheduler Machine B finished processing job {}", machineADoneJob.getId());

        machineC.addJob(machineBDoneJob);
        Job machineCDoneJob = machineC.getFinishedJob();
        logger.info("JobScheduler Machine C finished processing job {}", machineCDoneJob.getId());
        doneJobs.add(machineCDoneJob);
    }


}