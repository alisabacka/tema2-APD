import java.util.Comparator;

class RunningTaskComparator implements Comparator<RunningTask> {
    @Override
    public int compare(RunningTask rt1, RunningTask rt2) {
        return Integer.compare(rt1.getTask().getPriority(), rt2.getTask().getPriority());
    }
}