package tal.hyper_robotics.entities;

import java.util.UUID;

public class Job {
    public String id;
    private JobState state;


    public Job() {
        this.id = UUID.randomUUID().toString();
    }

    public void setState(JobState state){
        this.state = state;
    }

    public String getId(){
        return id;
    }
}
