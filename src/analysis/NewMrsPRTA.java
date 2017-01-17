package analysis;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;

public class NewMrsPRTA {

	private boolean use_deadline_insteadof_Ri = false;
	long count = 0;
	SporadicTask problemtask = null;
	int isTestPrint = 0;

	public long[][] NewMrsPRTATest(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, boolean printDebug) {
		long[][] init_Ri = new Utils().initResponseTime(tasks);

		long[][] response_time = new long[tasks.size()][];
		boolean isEqual = false, missDeadline = false;
		count = 0;

		for (int i = 0; i < init_Ri.length; i++) {
			response_time[i] = new long[init_Ri[i].length];
		}

		new Utils().cloneList(init_Ri, response_time);

		/* a huge busy window to get a fixed Ri */
		while (!isEqual) {
			isEqual = true;
			long[][] response_time_plus = busyWindow(tasks, resources, response_time);

			for (int i = 0; i < response_time_plus.length; i++) {
				for (int j = 0; j < response_time_plus[i].length; j++) {
					if (response_time[i][j] != response_time_plus[i][j])
						isEqual = false;

					if (response_time_plus[i][j] > tasks.get(i).get(j).deadline)
						missDeadline = true;
				}
			}

			count++;
			new Utils().cloneList(response_time_plus, response_time);

			if (missDeadline)
				break;
		}

		if (printDebug) {
			if (missDeadline)
				System.out.println("NewMrsPRTA    after " + count + " tims of recursion, the tasks miss the deadline.");
			else
				System.out.println("NewMrsPRTA    after " + count + " tims of recursion, we got the response time.");

			new Utils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

	public boolean isResponseTimeEqual(long[][] oldRi, long[][] newRi, ArrayList<ArrayList<SporadicTask>> tasks) {
		boolean is_equal = true;

		for (int i = 0; i < oldRi.length; i++) {
			for (int j = 0; j < oldRi[i].length; j++) {
				if (oldRi[i][j] != newRi[i][j]) {
					is_equal = false;
					System.out.println("not equal: " + oldRi[i][j] + " vs " + newRi[i][j]);
					System.out.println("T" + tasks.get(i).get(j).id + " old: S = " + tasks.get(i).get(j).spin + ", I = "
							+ tasks.get(i).get(j).interference + ", Local =" + tasks.get(i).get(j).local);
					problemtask = tasks.get(i).get(j);
				}
			}
		}
		System.out.println();
		return is_equal;
	}

	private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] response_time) {
		long[][] response_time_plus = new long[tasks.size()][];

		for (int i = 0; i < response_time.length; i++) {
			response_time_plus[i] = new long[response_time[i].length];
		}

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.spin = directRemoteDelay(task, tasks, resources, response_time, response_time[i][j]);
				task.interference = highPriorityInterference(task, tasks, response_time[i][j], response_time, resources);
				task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j]);
				response_time_plus[i][j] = task.Ri = task.WCET + task.pure_resource_execution_time + task.spin + task.interference + task.local;

				if (task.Ri > task.deadline)
					return response_time_plus;

			}
		}
		return response_time_plus;
	}

	/*
	 * Calculate the local blocking for task t.
	 */
	private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri) {
		ArrayList<Resource> LocalBlockingResources = getLocalBlockingResources(t, resources);
		ArrayList<Long> local_blocking_each_resource = new ArrayList<>();

		for (int i = 0; i < LocalBlockingResources.size(); i++) {
			Resource res = LocalBlockingResources.get(i);

			long max_lb = 0;

			// get max local blocking
			for (int j = 0; j < tasks.get(t.partition).size(); j++) {
				SporadicTask llt = tasks.get(t.partition).get(j);
				if (llt.priority < t.priority && llt.resource_required_index.contains(res.id - 1)
						&& llt.resource_access_time.get(llt.resource_required_index.indexOf(res.id - 1)).get(0) > max_lb) {
					max_lb = llt.resource_access_time.get(llt.resource_required_index.indexOf(res.id - 1)).get(0);
				}
			}

			// get max remote blocking
			if (res.isGlobal) {
				for (int parition_index = 0; parition_index < res.partitions.size(); parition_index++) {
					int partition = res.partitions.get(parition_index);
					int norHP = getNoRFromHP(res, t, tasks.get(t.partition), Ris[t.partition], Ri);
					int norT = t.resource_required_index.contains(res.id - 1)
							? t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(res.id - 1)) : 0;

					if (partition != t.partition) {
						max_lb += getTheSpecificAccess(getDeLinkFromPartition(res, tasks.get(partition), Ris[partition], Ri), norHP + norT);
					}
				}
			}
			local_blocking_each_resource.add(max_lb);
		}

		if (local_blocking_each_resource.size() > 1)
			local_blocking_each_resource.sort((l2, l1) -> Long.compare(l1, l2));

		return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;

	}

	/*
	 * gives a set of resources that can cause local blocking for a given task
	 */
	private ArrayList<Resource> getLocalBlockingResources(SporadicTask task, ArrayList<Resource> resources) {
		ArrayList<Resource> localBlockingResources = new ArrayList<>();
		int partition = task.partition;

		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);

			if (resource.partitions.contains(partition) && resource.ceiling.get(resource.partitions.indexOf(partition)) >= task.priority) {
				for (int j = 0; j < resource.requested_tasks.size(); j++) {
					SporadicTask LP_task = resource.requested_tasks.get(j);
					if (LP_task.partition == partition && LP_task.priority < task.priority) {
						localBlockingResources.add(resource);
						break;
					}
				}
			}
		}

		return localBlockingResources;
	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long Ri, long[][] Ris,
			ArrayList<Resource> resources) {
		long interference = 0;
		int partition = t.partition;
		ArrayList<SporadicTask> tasks = allTasks.get(partition);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				SporadicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (Ri) / (double) hpTask.period) * (hpTask.WCET);

				long btb_interference = getIndirectSpinDelay(hpTask, Ri, Ris[partition][i], Ris, allTasks, resources);
				interference += btb_interference;
			}
		}
		return interference;
	}

	/*
	 * for a high priority task hpTask, return its back to back hit time when
	 * the given task is pending
	 */
	private long getIndirectSpinDelay(SporadicTask hpTask, long Ri, long Rihp, long[][] Ris, ArrayList<ArrayList<SporadicTask>> allTasks,
			ArrayList<Resource> resources) {
		long BTBhit = 0;

		for (int i = 0; i < hpTask.resource_required_index.size(); i++) {
			/* for each resource that a high priority task request */
			Resource resource = resources.get(hpTask.resource_required_index.get(i));

			int number_of_higher_request = getNoRFromHP(resource, hpTask, allTasks.get(hpTask.partition), Ris[hpTask.partition], Ri);
			int number_of_release_with_btb = (int) Math
					.ceil((double) (Ri + (use_deadline_insteadof_Ri ? hpTask.deadline : Rihp)) / (double) hpTask.period)
					;
			
			ArrayList<Long> hp_access = new ArrayList<>();
			for(int k=0;k<number_of_release_with_btb;k++){
				hp_access.addAll(hpTask.resource_access_time.get(hpTask.resource_access_time.indexOf(resource.id-1)));
			}
			hp_access.sort((r2, r1) -> Long.compare(r1, r2));

			for(int k=0; k<hp_access.size();k++){
				BTBhit+= hp_access.get(k);
			}

			for (int j = 0; j < resource.partitions.size(); j++) {
				if (resource.partitions.get(j) != hpTask.partition) {
					int remote_partition = resource.partitions.get(j);
//					int number_of_remote_request = getNoRRemote(resource, allTasks.get(remote_partition), Ris[remote_partition], Ri);
//
//					int possible_spin_delay = number_of_remote_request - number_of_higher_request < 0 ? 0
//							: number_of_remote_request - number_of_higher_request;
//
//					int spin_delay_with_btb = Integer.min(possible_spin_delay, number_of_request_with_btb);
					
					

					BTBhit += 
				}
			}
		}
		return BTBhit;
	}

	/*
	 * Calculate the spin delay for a given task t.
	 */
	private long directRemoteDelay(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources, long[][] Ris, long Ri) {
		long spin_delay = 0;
		for (int k = 0; k < t.resource_required_index.size(); k++) {
			Resource resource = resources.get(t.resource_required_index.get(k));
			spin_delay += getDirectSpinDelay(t, resource, tasks, Ris, Ri);
		}
		return spin_delay;
	}

	/*
	 * gives the number of requests from remote partitions for a resource that
	 * is required by the given task.
	 */
	private int getDirectSpinDelay(SporadicTask task, Resource resource, ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris, long Ri) {
		int spin_dealy = 0;

		for (int i = 0; i < tasks.size(); i++) {
			if (i != task.partition) {
				ArrayList<Long> remoteAccess = getDeLinkFromPartition(resource, tasks.get(i), Ris[i], Ri);

				int getNoRFromHP = getNoRFromHP(resource, task, tasks.get(task.partition), Ris[task.partition], Ri);
				int NoRFromT = task.number_of_access_in_one_release.get(getIndexRInTask(task, resource));

				for (int start = getNoRFromHP; start < getNoRFromHP + NoRFromT; start++) {
					spin_dealy += getTheSpecificAccess(remoteAccess, start);
				}

			}
		}
		return spin_dealy;
	}

	private int getNoRRemote(Resource resource, ArrayList<SporadicTask> tasks, long[] Ris, long Ri) {
		int number_of_request_by_Remote_P = 0;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask remote_task = tasks.get(i);
				int indexR = getIndexRInTask(remote_task, resource);
				number_of_request_by_Remote_P += Math
						.ceil((double) (Ri + (use_deadline_insteadof_Ri ? remote_task.deadline : Ris[i])) / (double) remote_task.period)
						* remote_task.number_of_access_in_one_release.get(indexR);
			}
		}
		return number_of_request_by_Remote_P;
	}

	/*
	 * gives that number of requests from HP local tasks for a resource that is
	 * required by the given task.
	 */
	private int getNoRFromHP(Resource resource, SporadicTask task, ArrayList<SporadicTask> tasks, long[] Ris, long Ri) {
		int number_of_request_by_HP = 0;
		int priority = task.priority;

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > priority && tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask hpTask = tasks.get(i);
				int indexR = getIndexRInTask(hpTask, resource);
				number_of_request_by_HP += Math.ceil((double) (Ri + (use_deadline_insteadof_Ri ? hpTask.deadline : Ris[i])) / (double) hpTask.period)
						* hpTask.number_of_access_in_one_release.get(indexR);
			}
		}
		return number_of_request_by_HP;
	}

	private long getTheSpecificAccess(ArrayList<Long> requests, int index) {
		if (index >= requests.size()) {
			return 0;
		} else
			return requests.get(index);
	}

	private ArrayList<Long> getDeLinkFromPartition(Resource resource, ArrayList<SporadicTask> tasks, long[] Ris, long Ri) {
		ArrayList<Long> requests = new ArrayList<Long>();

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask remote_task = tasks.get(i);
				int indexR = getIndexRInTask(remote_task, resource);
				int NoRelease = (int) Math
						.ceil((double) (Ri + (use_deadline_insteadof_Ri ? remote_task.deadline : Ris[i])) / (double) remote_task.period);
				for (int j = 0; j < NoRelease; j++) {
					requests.addAll(remote_task.resource_access_time.get(indexR));
				}
			}
		}
		requests.sort((r2, r1) -> Long.compare(r1, r2));
		return requests;
	}

	/*
	 * Return the index of a given resource in stored in a task.
	 */
	private int getIndexRInTask(SporadicTask task, Resource resource) {
		int indexR = -1;
		if (task.resource_required_index.contains(resource.id - 1)) {
			for (int j = 0; j < task.resource_required_index.size(); j++) {
				if (resource.id - 1 == task.resource_required_index.get(j)) {
					indexR = j;
					break;
				}
			}
		}
		return indexR;
	}
}
