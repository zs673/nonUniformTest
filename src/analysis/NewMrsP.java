package analysis;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;

public class NewMrsP {

	private boolean use_deadline_insteadof_Ri = false;
	long count = 0;
	SporadicTask problemtask = null;

	long oneMig = 0;
	long np_length = 0;
	boolean printDebug;

	public NewMrsP(long oneMig, long np_length, boolean printDebug) {
		this.oneMig = oneMig;
		this.np_length = np_length;
		this.printDebug = printDebug;
	}

	public long[][] schedulabilityTest(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources) {
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
					if (response_time[i][j] != response_time_plus[i][j]) {
						if (count > 10000) {
							System.out.println("task T" + tasks.get(i).get(j).id + " : " + response_time_plus[i][j]
									+ " " + response_time[i][j]);
							System.out.println("task T" + tasks.get(i).get(j).id + " : " + tasks.get(i).get(j).spin
									+ " " + tasks.get(i).get(j).local + " " + tasks.get(i).get(j).interference);
						}
						isEqual = false;
					}

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
				System.out.println("NewMrsPRTAWithMigration    after " + count
						+ " tims of recursion, the tasks miss the deadline.");
			else
				System.out.println(
						"NewMrsPRTAWithMigration    after " + count + " tims of recursion, we got the response time.");
			new Utils().printResponseTime(response_time, tasks);
		}

		return response_time;
	}

	private long[][] busyWindow(ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources,
			long[][] response_time) {
		long[][] response_time_plus = new long[tasks.size()][];

		for (int i = 0; i < response_time.length; i++) {
			response_time_plus[i] = new long[response_time[i].length];
		}

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.spin = resourceAccessingTime(task, tasks, resources, response_time, response_time[i][j], 0,
						task.deadline);
				task.interference = highPriorityInterference(task, tasks, response_time[i][j], response_time,
						resources);
				task.local = localBlocking(task, tasks, resources, response_time, response_time[i][j])
						+ (np_length > 0 && isTaskIncurNPSection(task, tasks.get(task.partition), resources) ? np_length
								: 0);
				response_time_plus[i][j] = task.Ri = task.WCET + task.spin + task.interference + task.local;

				if (task.Ri > task.deadline)
					return response_time_plus;

			}
		}
		return response_time_plus;
	}

	private boolean isTaskIncurNPSection(SporadicTask task, ArrayList<SporadicTask> tasksOnItsParititon,
			ArrayList<Resource> resources) {
		int partition = task.partition;
		int priority = task.priority;
		int minCeiling = 1000;

		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);
			if (resource.partitions.contains(partition)
					&& minCeiling > resource.ceiling.get(resource.partitions.indexOf(partition))) {
				minCeiling = resource.ceiling.get(resource.partitions.indexOf(partition));
			}
		}

		if (priority >= minCeiling)
			return true;
		else
			return false;
	}

	/*
	 * Calculate the local blocking for task t.
	 */
	private long localBlocking(SporadicTask t, ArrayList<ArrayList<SporadicTask>> tasks, ArrayList<Resource> resources,
			long[][] Ris, long time) {
		ArrayList<Resource> LocalBlockingResources = getLocalBlockingResources(t, resources);
		ArrayList<Long> local_blocking_each_resource = new ArrayList<>();

		for (int i = 0; i < LocalBlockingResources.size(); i++) {
			ArrayList<Integer> migration_targets = new ArrayList<>();

			Resource res = LocalBlockingResources.get(i);
			long local_blocking = 0;

			// get max local blocking
			for (int j = 0; j < tasks.get(t.partition).size(); j++) {
				SporadicTask llt = tasks.get(t.partition).get(j);
				if (llt.priority < t.priority && llt.resource_required_index.contains(res.id - 1)
						&& llt.resource_access_time.get(llt.resource_required_index.indexOf(res.id - 1))
								.get(0) > local_blocking) {
					local_blocking = llt.resource_access_time.get(llt.resource_required_index.indexOf(res.id - 1))
							.get(0);
				}
			}
			migration_targets.add(t.partition);
			long maxlocal = local_blocking;

			// get max remote blocking
			if (res.isGlobal) {
				for (int parition_index = 0; parition_index < res.partitions.size(); parition_index++) {
					int partition = res.partitions.get(parition_index);

					if (partition != t.partition) {
						int norHP = getNoRFromHP(res, t, tasks.get(t.partition), Ris[t.partition], time);
						int norT = t.resource_required_index.contains(res.id - 1)
								? t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(res.id - 1))
								: 0;

						if (getTheSpecificAccess(
								getAccessLinkFromProcessor(res, tasks.get(partition), Ris[partition], time),
								norHP + norT) > 0) {
							local_blocking += getTheSpecificAccess(
									getAccessLinkFromProcessor(res, tasks.get(partition), Ris[partition], time),
									norHP + norT);
							migration_targets.add(partition);
						}
					}

				}

				// migration cost
				if(oneMig > 0){
					local_blocking += migrationCost(maxlocal, migration_targets, res, t.partition, tasks, t.deadline);
					for (int parition_index = 0; parition_index < res.partitions.size(); parition_index++) {
						int partition = res.partitions.get(parition_index);

						if (partition != t.partition) {
							int norHP = getNoRFromHP(res, t, tasks.get(t.partition), Ris[t.partition], time);
							int norT = t.resource_required_index.contains(res.id - 1)
									? t.number_of_access_in_one_release.get(t.resource_required_index.indexOf(res.id - 1))
									: 0;

							if (getTheSpecificAccess(
									getAccessLinkFromProcessor(res, tasks.get(partition), Ris[partition], time),
									norHP + norT) > 0) {
								local_blocking += migrationCost(getTheSpecificAccess(
										getAccessLinkFromProcessor(res, tasks.get(partition), Ris[partition], time),
										norHP + norT), migration_targets, res, partition, tasks, t.deadline);
							}
						}

					}
				}

			}

			local_blocking_each_resource.add(local_blocking);
		}

		if (local_blocking_each_resource.size() > 1)
			local_blocking_each_resource.sort((l2, l1) -> Long.compare(l1, l2));

		return local_blocking_each_resource.size() > 0 ? local_blocking_each_resource.get(0) : 0;

	}

	/*
	 * Calculate the local high priority tasks' interference for a given task t.
	 * CI is a set of computation time of local tasks, including spin delay.
	 */
	private long highPriorityInterference(SporadicTask t, ArrayList<ArrayList<SporadicTask>> allTasks, long time,
			long[][] Ris, ArrayList<Resource> resources) {
		long interference = 0;
		int partition = t.partition;
		ArrayList<SporadicTask> tasks = allTasks.get(partition);

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).priority > t.priority) {
				SporadicTask hpTask = tasks.get(i);
				interference += Math.ceil((double) (time) / (double) hpTask.period) * (hpTask.WCET);
				interference += resourceAccessingTime(hpTask, allTasks, resources, Ris, time, Ris[partition][i],
						t.deadline);
			}
		}
		return interference;
	}

	private long resourceAccessingTime(SporadicTask task, ArrayList<ArrayList<SporadicTask>> tasks,
			ArrayList<Resource> resources, long[][] Ris, long time, long jitter, long deadline) {
		long resource_accessing_time = 0;

		// for each resource
		for (int i = 0; i < task.resource_required_index.size(); i++) {
			Resource resource = resources.get(task.resource_required_index.get(i));

			int number_of_request_with_btb = (int) Math.ceil((double) (time + jitter) / (double) task.period)
					* task.number_of_access_in_one_release.get(i);

			for (int j = 1; j < number_of_request_with_btb + 1; j++) {
				long oneAccess = 0;
				oneAccess += resourceAccessingTimeInOne(task, resource, tasks, Ris, time, jitter, j, deadline);

				resource_accessing_time += oneAccess;
			}
		}

		return resource_accessing_time;
	}

	private long resourceAccessingTimeInOne(SporadicTask task, Resource resource,
			ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris, long time, long jitter, int n, long deadline) {
		long access_length = 0;

		ArrayList<Integer> migration_targets = new ArrayList<>();

		// identify the migration targets
		migration_targets.add(task.partition);

		// account for the remote spin delay from each processor.
		for (int i = 0; i < tasks.size(); i++) {
			if (i != task.partition) {
				int getNoRFromHP = getNoRFromHP(resource, task, tasks.get(task.partition), Ris[task.partition], time);
				access_length += getTheSpecificAccess(getAccessLinkFromProcessor(resource, tasks.get(i), Ris[i], time),
						getNoRFromHP + n - 1);

				if (getTheSpecificAccess(getAccessLinkFromProcessor(resource, tasks.get(i), Ris[i], time),
						getNoRFromHP + n - 1) > 0) {
					migration_targets.add(i);
				}
			}
		}

		// account for the request of the task itself
		access_length += getTheSpecificAccess(getAccessLinkFromTask(resource, task, time, jitter), n - 1);

		if (oneMig > 0) {
			// account for the remote spin delay from each processor.
			for (int i = 0; i < tasks.size(); i++) {
				int getNoRFromHP = getNoRFromHP(resource, task, tasks.get(task.partition), Ris[task.partition], time);

				if (i != task.partition
						&& getTheSpecificAccess(getAccessLinkFromProcessor(resource, tasks.get(i), Ris[i], time),
								getNoRFromHP + n - 1) > 0) {

					access_length += migrationCost(
							getTheSpecificAccess(getAccessLinkFromProcessor(resource, tasks.get(i), Ris[i], time),
									getNoRFromHP + n - 1),
							migration_targets, resource, i, tasks, deadline);

				}
			}

			access_length += migrationCost(
					getTheSpecificAccess(getAccessLinkFromTask(resource, task, time, jitter), n - 1), migration_targets,
					resource, task.partition, tasks, deadline);
		}

		return access_length;
	}

	private long migrationCost(long access, ArrayList<Integer> migration_targets, Resource resource,
			int access_partition, ArrayList<ArrayList<SporadicTask>> tasks, long deadline) {
		ArrayList<Integer> migration_targets_with_P = new ArrayList<>();

		// identify the migration targets with preemptors
		for (int i = 0; i < migration_targets.size(); i++) {
			int partition = migration_targets.get(i);
			if (tasks.get(partition).get(0).priority > resource.ceiling.get(resource.partitions.indexOf(partition)))
				migration_targets_with_P.add(migration_targets.get(i));
		}

		// check
		if (!migration_targets.containsAll(migration_targets_with_P)) {
			System.out.println("migration targets error!");
			System.exit(0);
		}

		// now we compute the migration cost for one request
		long migration_cost_for_one_access = 0;

		// calculating migration cost
		// 1. If there is no preemptors on the task's partition OR there is
		// no
		// other migration targets
		if (!migration_targets_with_P.contains(access_partition)
				|| (migration_targets.size() == 1 && migration_targets.get(0) == access_partition))
			migration_cost_for_one_access = 0;

		// 2. If there is preemptors on the task's partition AND there are
		// no
		// preemptors on other migration targets
		else if (migration_targets_with_P.size() == 1 && migration_targets_with_P.get(0) == access_partition
				&& migration_targets.size() > 1)
			migration_cost_for_one_access = 2 * oneMig;

		// 3. If there exist multiple migration targets with preemptors.
		// With NP
		// section applied.
		else {
			long migCostWithHP = migrationCostBusyWindow(migration_targets_with_P, access, resource, tasks, deadline);
			long migCostWithNP = 0;

			if (np_length > 0)
				migCostWithNP = (long) (1 + Math.ceil((double) access / (double) np_length)) * oneMig;

			migration_cost_for_one_access = np_length > 0 ? Math.min(migCostWithHP, migCostWithNP) : migCostWithHP;
		}

		return migration_cost_for_one_access;
	}

	public long migrationCostBusyWindow(ArrayList<Integer> migration_targets_with_P, long access, Resource resource,
			ArrayList<ArrayList<SporadicTask>> tasks, long deadline) {
		long migCost = 0;

		long migCostWithNP = 0;
		if (np_length > 0)
			migCostWithNP = (long) (1 + Math.ceil((double) access / (double) np_length)) * oneMig;

		long newMigCost = migrationCostOneCal(migration_targets_with_P, access + migCost, resource, tasks);

		while (migCost != newMigCost) {
			migCost = newMigCost;
			newMigCost = migrationCostOneCal(migration_targets_with_P, access + migCost, resource, tasks);

			if (np_length > 0 && newMigCost >= migCostWithNP) {
				return newMigCost;
			}

			if (newMigCost > deadline)
				return newMigCost;
		}

		return migCost;
	}

	public long migrationCostOneCal(ArrayList<Integer> migration_targets_with_P, long duration, Resource resource,
			ArrayList<ArrayList<SporadicTask>> tasks) {
		long migCost = 0;

		for (int i = 0; i < migration_targets_with_P.size(); i++) {
			int partition_with_p = migration_targets_with_P.get(i);
			int ceiling_index = resource.partitions.indexOf(partition_with_p);

			for (int j = 0; j < tasks.get(partition_with_p).size(); j++) {
				SporadicTask hpTask = tasks.get(partition_with_p).get(j);

				if (hpTask.priority > resource.ceiling.get(ceiling_index))
					migCost += Math.ceil((double) (duration) / (double) hpTask.period) * oneMig;
			}
		}

		return migCost + oneMig;
	}

	/*
	 * gives a set of resources that can cause local blocking for a given task
	 */
	private ArrayList<Resource> getLocalBlockingResources(SporadicTask task, ArrayList<Resource> resources) {
		ArrayList<Resource> localBlockingResources = new ArrayList<>();
		int partition = task.partition;

		for (int i = 0; i < resources.size(); i++) {
			Resource resource = resources.get(i);

			if (resource.partitions.contains(partition)
					&& resource.ceiling.get(resource.partitions.indexOf(partition)) >= task.priority) {
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
				number_of_request_by_HP += Math.ceil(
						(double) (Ri + (use_deadline_insteadof_Ri ? hpTask.deadline : Ris[i])) / (double) hpTask.period)
						* hpTask.number_of_access_in_one_release.get(indexR);
			}
		}
		return number_of_request_by_HP;
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

	private long getTheSpecificAccess(ArrayList<Long> requests, int index) {
		if (index >= requests.size()) {
			return 0;
		} else
			return requests.get(index);
	}

	private ArrayList<Long> getAccessLinkFromTask(Resource resource, SporadicTask task, long time, long jitter) {
		ArrayList<Long> requests = new ArrayList<Long>();

		int number_of_release_with_btb = (int) Math.ceil((double) (time + jitter) / (double) task.period);

		for (int k = 0; k < number_of_release_with_btb; k++) {
			requests.addAll(task.resource_access_time.get(task.resource_required_index.indexOf(resource.id - 1)));
		}

		requests.sort((r2, r1) -> Long.compare(r1, r2));
		return requests;
	}

	private ArrayList<Long> getAccessLinkFromProcessor(Resource resource, ArrayList<SporadicTask> tasks, long[] Ris,
			long time) {
		ArrayList<Long> requests = new ArrayList<Long>();

		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).resource_required_index.contains(resource.id - 1)) {
				SporadicTask remote_task = tasks.get(i);
				int indexR = getIndexRInTask(remote_task, resource);
				int NoRelease = (int) Math
						.ceil((double) (time + (use_deadline_insteadof_Ri ? remote_task.deadline : Ris[i]))
								/ (double) remote_task.period);
				for (int j = 0; j < NoRelease; j++) {
					requests.addAll(remote_task.resource_access_time.get(indexR));
				}
			}
		}
		requests.sort((r2, r1) -> Long.compare(r1, r2));
		return requests;
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
}
