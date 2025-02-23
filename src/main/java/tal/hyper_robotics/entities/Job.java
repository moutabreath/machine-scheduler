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

    public void advanceState() {
        JobState[] states = JobState.values();
        int nextOrdinal = (state.ordinal() + 1) % states.length;  // Wraps around to first state if at end
        this.state =  states[nextOrdinal];
    }

    public JobState getState(){
        return this.state;
    }

    public String getId(){
        return id;
    }
}
