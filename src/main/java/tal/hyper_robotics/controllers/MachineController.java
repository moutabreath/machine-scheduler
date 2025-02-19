package tal.hyper_robotics.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tal.hyper_robotics.scheduler.JobScheduler;

@RestController
@RequestMapping("/machine")
public class MachineController {

    @Autowired
    private JobScheduler jobScheduler;
    
    @PostMapping("/startJobs")
    public void startJobs(){
        jobScheduler.scheduleJobs();
    }
}
