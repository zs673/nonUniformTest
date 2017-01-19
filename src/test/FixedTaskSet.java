package test;

import java.util.ArrayList;

import analysis.NewMrsP;
import entity.Resource;
import entity.SporadicTask;

public class FixedTaskSet {

	public static void main(String args[]) {
		fixed_system_1();
//		fixed_system_2();
//		hard_system1();
	}

	public static void fixed_system_1() {
		/* Generate tasks */
		ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();

		ArrayList<SporadicTask> ts = new ArrayList<>();
		ts.add(new SporadicTask(999, 1000, 1, 0, 1));
		ts.add(new SporadicTask(900, 1000, 1, 0, 2));
		ts.add(new SporadicTask(800, 1000, 1, 0, 3));
		tasks.add(ts);

		ArrayList<SporadicTask> ts1 = new ArrayList<>();
		ts1.add(new SporadicTask(988, 1000, 1, 1, 4));
		tasks.add(ts1);

		/* Generate resource */
		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(new Resource(1));
		// resources.add(new Resource(2, 10));
		// resources.add(new Resource(3, 5));

		tasks.get(0).get(0).resource_required_index.add(0);
		tasks.get(0).get(0).number_of_access_in_one_release.add(2);
		tasks.get(0).get(0).pure_resource_execution_time = 2 * 2;
		tasks.get(0).get(0).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(0).get(0).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(0).resource_access_time.get(0).add((long) 2);
		tasks.get(0).get(0).resource_access_time.get(0).add((long) 2);
		

		tasks.get(0).get(1).resource_required_index.add(0);
		tasks.get(0).get(1).number_of_access_in_one_release.add(3);
		tasks.get(0).get(1).pure_resource_execution_time = 2 * 3;
		tasks.get(0).get(1).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(0).get(1).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(1).resource_access_time.get(0).add((long) 2);
		tasks.get(0).get(1).resource_access_time.get(0).add((long) 2);
		tasks.get(0).get(1).resource_access_time.get(0).add((long) 2);

		tasks.get(0).get(2).resource_required_index.add(0);
		tasks.get(0).get(2).number_of_access_in_one_release.add(3);
		tasks.get(0).get(2).pure_resource_execution_time = 2 * 3;
		tasks.get(0).get(2).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(0).get(2).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(2).resource_access_time.get(0).add((long) 2);
		tasks.get(0).get(2).resource_access_time.get(0).add((long) 2);
		tasks.get(0).get(2).resource_access_time.get(0).add((long) 2);

		tasks.get(1).get(0).resource_required_index.add(0);
		tasks.get(1).get(0).number_of_access_in_one_release.add(5);
		tasks.get(1).get(0).pure_resource_execution_time = 2 * 5;
		tasks.get(1).get(0).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(1).get(0).resource_access_time.add(new ArrayList<Long>());
		tasks.get(1).get(0).resource_access_time.get(0).add((long) 2);
		tasks.get(1).get(0).resource_access_time.get(0).add((long) 2);
		tasks.get(1).get(0).resource_access_time.get(0).add((long) 2);
		tasks.get(1).get(0).resource_access_time.get(0).add((long) 2);
		tasks.get(1).get(0).resource_access_time.get(0).add((long) 2);

		resources.get(0).requested_tasks.add(tasks.get(0).get(0));
		resources.get(0).requested_tasks.add(tasks.get(0).get(1));
		resources.get(0).requested_tasks.add(tasks.get(0).get(2));
		resources.get(0).requested_tasks.add(tasks.get(1).get(0));

		resources.get(0).ceiling.add(999);
		resources.get(0).ceiling.add(999);

		resources.get(0).partitions.add(0);
		resources.get(0).partitions.add(1);
		resources.get(0).isGlobal = true;

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.hasResource = 1;
				task.resource_required_index_cpoy = new int[task.resource_required_index.size()];
				task.number_of_access_in_one_release_copy = new int[task.resource_required_index.size()];
				for (int resource_index = 0; resource_index < task.resource_required_index.size(); resource_index++) {
					task.resource_required_index_cpoy[resource_index] = task.resource_required_index.get(resource_index);
					task.number_of_access_in_one_release_copy[resource_index] = task.number_of_access_in_one_release.get(resource_index);
				}
			}
		}

		tasks.get(0).get(0).Ri = 1 + 4;
		tasks.get(0).get(1).Ri = 1 + 4;
		tasks.get(0).get(2).Ri = 1 + 6;
		tasks.get(1).get(0).Ri = 1 + 10;
		
		access(tasks);

		new NewMrsP(0, 0, true).schedulabilityTest(tasks, resources);
		new NewMrsP(1, 2, true).schedulabilityTest(tasks, resources);
		System.out.println();
	}
	
	public static void access(ArrayList<ArrayList<SporadicTask>> tasks){
		for (int i = 0; i < tasks.size(); i++) {
			for (int l = 0; l < tasks.get(i).size(); l++) {
				SporadicTask task = tasks.get(i).get(l);
				
				int totalaccess = 0;
				for (int k = 0; k < task.resource_access_time.size(); k++) {
					for (int j = 0; j < task.resource_access_time.get(k).size(); j++) {
						totalaccess++;
					}
				}
				task.resource_access_time_copy = new long[totalaccess];
				int index = 0;
				for (int k = 0; k < task.resource_access_time.size(); k++) {
					for (int j = 0; j < task.resource_access_time.get(k).size(); j++) {
						task.resource_access_time_copy[index] = task.resource_access_time.get(k).get(j);
						//System.out.println(task.resource_access_time.get(k).get(j));
						index++;
					}
				}
			}
		}
	}

	public static void fixed_system_2() {
		/* Generate tasks */
		ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();

		ArrayList<SporadicTask> ts = new ArrayList<>();
		ts.add(new SporadicTask(999, 1000, 1, 0, 1));
		ts.add(new SporadicTask(900, 1000, 1, 0, 2));
		ts.add(new SporadicTask(800, 1000, 1, 0, 3));
		ts.add(new SporadicTask(700, 1000, 1, 0, 4));
		tasks.add(ts);

		ArrayList<SporadicTask> ts1 = new ArrayList<>();
		ts1.add(new SporadicTask(999, 1000, 1, 1, 5));
		tasks.add(ts1);

		/* Generate resource */
		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(new Resource(1));
		resources.add(new Resource(2));

		tasks.get(0).get(0).resource_required_index.add(0);
		tasks.get(0).get(0).resource_required_index.add(1);
		tasks.get(0).get(0).number_of_access_in_one_release.add(1);
		tasks.get(0).get(0).number_of_access_in_one_release.add(1);
		tasks.get(0).get(0).pure_resource_execution_time = 2 + 1;
		tasks.get(0).get(0).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(0).get(0).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(0).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(0).resource_access_time.get(0).add((long) 2);
		tasks.get(0).get(0).resource_access_time.get(1).add((long) 1);

		tasks.get(0).get(1).resource_required_index.add(0);
		tasks.get(0).get(1).number_of_access_in_one_release.add(1);
		tasks.get(0).get(1).pure_resource_execution_time = 2;
		tasks.get(0).get(1).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(0).get(1).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(1).resource_access_time.get(0).add((long) 2);

		tasks.get(0).get(2).resource_required_index.add(0);
		tasks.get(0).get(2).number_of_access_in_one_release.add(1);
		tasks.get(0).get(2).pure_resource_execution_time = 2;
		tasks.get(0).get(2).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(0).get(2).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(2).resource_access_time.get(0).add((long) 2);

		tasks.get(0).get(3).resource_required_index.add(0);
		tasks.get(0).get(3).resource_required_index.add(1);
		tasks.get(0).get(3).number_of_access_in_one_release.add(1);
		tasks.get(0).get(3).number_of_access_in_one_release.add(1);
		tasks.get(0).get(3).pure_resource_execution_time = 2 + 1;
		tasks.get(0).get(3).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(0).get(3).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(3).resource_access_time.add(new ArrayList<Long>());
		tasks.get(0).get(3).resource_access_time.get(0).add((long) 2);
		tasks.get(0).get(3).resource_access_time.get(1).add((long) 1);
		

		tasks.get(1).get(0).resource_required_index.add(0);
		tasks.get(1).get(0).number_of_access_in_one_release.add(1);
		tasks.get(1).get(0).pure_resource_execution_time = 2;
		tasks.get(1).get(0).resource_access_time = new ArrayList<ArrayList<Long>>();
		tasks.get(1).get(0).resource_access_time.add(new ArrayList<Long>());
		tasks.get(1).get(0).resource_access_time.get(0).add((long) 2);
		
		resources.get(0).requested_tasks.add(tasks.get(0).get(0));
		resources.get(0).requested_tasks.add(tasks.get(0).get(1));
		resources.get(0).requested_tasks.add(tasks.get(0).get(2));
		resources.get(0).requested_tasks.add(tasks.get(0).get(3));
		resources.get(0).requested_tasks.add(tasks.get(1).get(0));

		resources.get(0).ceiling.add(999);
		resources.get(0).ceiling.add(999);

		resources.get(0).partitions.add(0);
		resources.get(0).partitions.add(1);
		resources.get(0).isGlobal = true;

		resources.get(1).requested_tasks.add(tasks.get(0).get(0));
		resources.get(1).requested_tasks.add(tasks.get(0).get(3));
		resources.get(1).ceiling.add(999);
		resources.get(1).partitions.add(0);

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.hasResource = 1;
				task.resource_required_index_cpoy = new int[task.resource_required_index.size()];
				task.number_of_access_in_one_release_copy = new int[task.resource_required_index.size()];
				for (int resource_index = 0; resource_index < task.resource_required_index.size(); resource_index++) {
					task.resource_required_index_cpoy[resource_index] = task.resource_required_index.get(resource_index);
					task.number_of_access_in_one_release_copy[resource_index] = task.number_of_access_in_one_release.get(resource_index);
				}
			}
		}
		
		access(tasks);

		new NewMrsP(0, 0, true).schedulabilityTest(tasks, resources);
		new NewMrsP(1, 2, true).schedulabilityTest(tasks, resources);
		System.out.println();
	}

	public static void hard_system1() {
		/* Generate tasks */
		ArrayList<ArrayList<SporadicTask>> tasks = new ArrayList<>();

		ArrayList<SporadicTask> ts = new ArrayList<>();
		ts.add(new SporadicTask(998, 56000, 6558, 0, 1));
		ts.add(new SporadicTask(996, 522000, 62869, 0, 2));
		ts.add(new SporadicTask(994, 653000, 106070, 0, 3));
		tasks.add(ts);

		ArrayList<SporadicTask> ts1 = new ArrayList<>();
		ts1.add(new SporadicTask(998, 174000, 50173, 1, 4));
		ts1.add(new SporadicTask(996, 658000, 73465, 1, 5));
		tasks.add(ts1);

		/* Generate resource */
		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(new Resource(1));
		resources.add(new Resource(2));
		resources.add(new Resource(3));
		resources.add(new Resource(4));

		SporadicTask t2 = tasks.get(0).get(1);
		t2.resource_required_index.add(1);
		t2.number_of_access_in_one_release.add(4);
		t2.resource_required_index.add(2);
		t2.number_of_access_in_one_release.add(3);
		t2.resource_required_index.add(3);
		t2.number_of_access_in_one_release.add(3);
		t2.pure_resource_execution_time = 137;
		t2.WCET = t2.WCET - 137;
		
		t2.resource_access_time = new ArrayList<ArrayList<Long>>();
		t2.resource_access_time.add(new ArrayList<Long>());
		t2.resource_access_time.get(0).add((long) 14);
		t2.resource_access_time.get(0).add((long) 14);
		t2.resource_access_time.get(0).add((long) 14);
		t2.resource_access_time.get(0).add((long) 14);
		
		t2.resource_access_time.add(new ArrayList<Long>());
		t2.resource_access_time.get(1).add((long) 15);
		t2.resource_access_time.get(1).add((long) 15);
		t2.resource_access_time.get(1).add((long) 15);
		
		t2.resource_access_time.add(new ArrayList<Long>());
		t2.resource_access_time.get(2).add((long) 12);
		t2.resource_access_time.get(2).add((long) 12);
		t2.resource_access_time.get(2).add((long) 12);

		SporadicTask t3 = tasks.get(0).get(2);
		t3.resource_required_index.add(0);
		t3.resource_required_index.add(1);
		t3.resource_required_index.add(2);
		t3.resource_required_index.add(3);
		t3.number_of_access_in_one_release.add(1);
		t3.number_of_access_in_one_release.add(4);
		t3.number_of_access_in_one_release.add(3);
		t3.number_of_access_in_one_release.add(2);
		t3.pure_resource_execution_time = 135;
		t3.WCET = t3.WCET - 135;
		
		t3.resource_access_time = new ArrayList<ArrayList<Long>>();
		t3.resource_access_time.add(new ArrayList<Long>());
		t3.resource_access_time.get(0).add((long) 10);
		
		t3.resource_access_time.add(new ArrayList<Long>());
		t3.resource_access_time.get(1).add((long) 14);
		t3.resource_access_time.get(1).add((long) 14);
		t3.resource_access_time.get(1).add((long) 14);
		t3.resource_access_time.get(1).add((long) 14);
		
		t3.resource_access_time.add(new ArrayList<Long>());
		t3.resource_access_time.get(2).add((long) 15);
		t3.resource_access_time.get(2).add((long) 15);
		t3.resource_access_time.get(2).add((long) 15);
		
		t3.resource_access_time.add(new ArrayList<Long>());
		t3.resource_access_time.get(3).add((long) 12);
		t3.resource_access_time.get(3).add((long) 12);

		SporadicTask t4 = tasks.get(1).get(0);
		t4.resource_required_index.add(2);
		t4.number_of_access_in_one_release.add(5);
		t4.pure_resource_execution_time = 75;
		t4.WCET = t4.WCET - 75;
		t4.resource_access_time = new ArrayList<ArrayList<Long>>();
		t4.resource_access_time.add(new ArrayList<Long>());
		t4.resource_access_time.get(0).add((long) 15);
		t4.resource_access_time.get(0).add((long) 15);
		t4.resource_access_time.get(0).add((long) 15);
		t4.resource_access_time.get(0).add((long) 15);
		t4.resource_access_time.get(0).add((long) 15);

		SporadicTask t5 = tasks.get(1).get(1);
		t5.resource_required_index.add(2);
		t5.number_of_access_in_one_release.add(1);
		t5.pure_resource_execution_time = 15;
		t5.WCET = t5.WCET - 15;
		t5.resource_access_time = new ArrayList<ArrayList<Long>>();
		t5.resource_access_time.add(new ArrayList<Long>());
		t5.resource_access_time.get(0).add((long) 15);

		resources.get(0).requested_tasks.add(t3);
		resources.get(0).ceiling.add(994);
		resources.get(0).partitions.add(0);

		resources.get(1).requested_tasks.add(t2);
		resources.get(1).requested_tasks.add(t3);
		resources.get(1).ceiling.add(996);
		resources.get(1).partitions.add(0);

		resources.get(2).requested_tasks.add(t2);
		resources.get(2).requested_tasks.add(t3);
		resources.get(2).requested_tasks.add(t4);
		resources.get(2).requested_tasks.add(t5);
		resources.get(2).ceiling.add(996);
		resources.get(2).ceiling.add(998);
		resources.get(2).partitions.add(0);
		resources.get(2).partitions.add(1);
		resources.get(2).isGlobal = true;

		resources.get(3).requested_tasks.add(t2);
		resources.get(3).requested_tasks.add(t3);
		resources.get(3).ceiling.add(996);
		resources.get(3).partitions.add(0);

		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				SporadicTask task = tasks.get(i).get(j);
				task.hasResource = 1;
				task.resource_required_index_cpoy = new int[task.resource_required_index.size()];
				task.number_of_access_in_one_release_copy = new int[task.resource_required_index.size()];
				for (int resource_index = 0; resource_index < task.resource_required_index.size(); resource_index++) {
					task.resource_required_index_cpoy[resource_index] = task.resource_required_index.get(resource_index);
					task.number_of_access_in_one_release_copy[resource_index] = task.number_of_access_in_one_release.get(resource_index);
				}
			}
		}
		
		access(tasks);

		new NewMrsP(0,0,true).schedulabilityTest(tasks, resources);
		new NewMrsP(1,2,true).schedulabilityTest(tasks, resources);
		System.out.println();
	}
}
