/* Implement this class. */

import java.util.List;

public class MyDispatcher extends Dispatcher {
    int lastAllocatedRoundRobinHostIndex = -1;

    public MyDispatcher(SchedulingAlgorithm algorithm, List<Host> hosts) {
        super(algorithm, hosts);
    }

    @Override
    public void addTask(Task task) {
        Host selectedHost;

        switch (algorithm) {
            case ROUND_ROBIN: {
                int index = (lastAllocatedRoundRobinHostIndex + 1) % hosts.size();

                lastAllocatedRoundRobinHostIndex = index;
                selectedHost =  hosts.get(index);
                break;
            } case SHORTEST_QUEUE: {
                selectedHost = findHostByShortestQueue();
                break;
            } case SIZE_INTERVAL_TASK_ASSIGNMENT: {
                selectedHost = findHostByTaskType(task);
                break;
            } case LEAST_WORK_LEFT: {
                selectedHost = findHostByLeastWorkLeft();
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }

        selectedHost.addTask(task);
    }

    private Host findHostByShortestQueue() {
        Host shortestQueueHost = hosts.get(0);
        for (Host host : hosts) {
            if (host.getQueueSize() < shortestQueueHost.getQueueSize() ||
                    (host.getQueueSize() == shortestQueueHost.getQueueSize() && hosts.indexOf(host) < hosts.indexOf(shortestQueueHost))) {
                shortestQueueHost = host;
            }
        }
        return shortestQueueHost;
    }

    private Host findHostByTaskType(Task task) {
        return switch (task.getType()) {
            case SHORT -> hosts.get(0);
            case MEDIUM -> hosts.get(1);
            case LONG -> hosts.get(2);
        };
    }

    private Host findHostByLeastWorkLeft() {
        Host leastWorkHost = hosts.get(0);
        for (Host host : hosts) {
            if (host.getWorkLeft() < leastWorkHost.getWorkLeft() ||
                    (host.getWorkLeft() == leastWorkHost.getWorkLeft() && hosts.indexOf(host) < hosts.indexOf(leastWorkHost))) {
                leastWorkHost = host;
            }
        }

        return leastWorkHost;
    }
}
