/* Implement this class. */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MyHost extends Host {
    final List<Task> taskQueue = new ArrayList<>();
    List<RunningTask> preemptedTasks = new ArrayList<>();
    RunningTask currentTask;
    private boolean shutdown = false;

    @Override
    public void run() {
        while (!shutdown) {
            if (getQueueSize() <= 0) {
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            if (currentTask == null) {
                Task task = taskQueue.remove(0);

                currentTask = new RunningTask(task, task.getDuration());
            }

            while (true) {
                if (currentTask.getTimeLeft() > 0) {
                    currentTask.setTimeLeft(currentTask.getTimeLeft() - 1);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (shouldPreemptCurrentTask()) {
                        preemptCurrentTask();
                    }
                } else {
                    currentTask.getTask().finish();
                    currentTask = getNextTaskOrNull();

                    break;
                }
            }
        }
    }

    @Override
    public void addTask(Task task) {
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.sort(Comparator.comparing(Task::getPriority).reversed());
        }
    }

    @Override
    public int getQueueSize() {
        return taskQueue.size() + preemptedTasks.size() + (currentTask != null ? 1 : 0);
    }

    @Override
    public long getWorkLeft() {
        return taskQueue.stream().mapToLong(Task::getDuration).sum() +
                calculateRunningTaskTotalDuration(preemptedTasks) * 1000 +
                (currentTask != null ? currentTask.getTimeLeft() * 1000 : 0);
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    private RunningTask getNextTaskOrNull() {
        if (!preemptedTasks.isEmpty()) {
            return preemptedTasks.remove(0);
        }

        if (taskQueue.size() <= 1) {
            return null;
        }

        Task nextTask;

        synchronized (taskQueue) {
            nextTask = taskQueue.remove(0);
        }

        return new RunningTask(nextTask, nextTask.getDuration());
    }

    private static long calculateRunningTaskTotalDuration(List<RunningTask> runningTasks) {
        long totalDuration = 0;

        for (RunningTask runningTask : runningTasks) {
            totalDuration += runningTask.getTimeLeft();
        }

        return totalDuration;
    }


    private boolean shouldPreemptCurrentTask() {
        return currentTask.getTask().isPreemptible() &&
                taskQueue.stream().anyMatch(obj -> obj.getPriority() > currentTask.getTask().getPriority());
    }

    private void preemptCurrentTask() {
        Optional<Task> highestPriorityTask = taskQueue.stream()
                .max(java.util.Comparator.comparingInt(Task::getPriority));

        highestPriorityTask.ifPresent(task -> {
            if (currentTask.getTimeLeft() <= 0) {
                currentTask.getTask().finish();
            } else {
                preemptedTasks.add(currentTask);
                preemptedTasks.sort(new RunningTaskComparator().reversed());
            }

            currentTask = new RunningTask(task, task.getDuration());

            synchronized (taskQueue) {
                taskQueue.remove(task);
            }
        });
    }
}