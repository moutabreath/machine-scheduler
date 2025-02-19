package tal.hyper_robotics.scheduler;

import java.util.LinkedList;
import java.util.Queue;

import org.springframework.stereotype.Service;

import tal.hyper_robotics.entities.Job;

@Service
public class JobScheduler {

    private final MachineProcessor machineA;
    private final MachineProcessor machineB;
    private final MachineProcessor machineC;
    private final Queue<Job> doneJobs;

    public JobScheduler(){
        machineA = new MachineProcessor("A", 1, 10);
        machineB = new MachineProcessor("B", 2, 15);
        machineC = new MachineProcessor("C", 1, 20);
        doneJobs = new LinkedList<>();
    }

    public void scheduleJobs() {
        Job job = new Job();
        machineA.addJob(job);
        Job machineADoneJob = machineA.getFinishedJob();

        machineB.addJob(machineADoneJob);
        Job machineBDoneJob = machineB.getFinishedJob();

        machineC.addJob(machineBDoneJob);
        Job machineCDoneJob = machineC.getFinishedJob();
        doneJobs.add(machineCDoneJob);
        
        
        
    }


}