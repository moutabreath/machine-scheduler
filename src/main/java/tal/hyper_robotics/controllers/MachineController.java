package tal.hyper_robotics.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import tal.hyper_robotics.scheduler.JobScheduler;

@RestController
@RequestMapping("/machine")
@CrossOrigin(origins = "*")
public class MachineController {

    @Autowired
    private JobScheduler jobScheduler;

    @PostMapping("/startJob")
    public void startJob() {
        jobScheduler.scheduleJobs();
    }

    @GetMapping("/jobUpdated/register")
    public SseEmitter registerToJobUpdates() {
        return jobScheduler.registerToJobUpdates();
    }

}
