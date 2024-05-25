public class RunningTask {
    private final Task task;
    private Long timeLeft;

    public RunningTask(Task task, Long duration) {
        this.task = task;
        this.timeLeft = duration / 1000;
    }

    public synchronized Task getTask() {
        return task;
    }

    public Long getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(Long timeLeft) {
        this.timeLeft = timeLeft;
    }
}
