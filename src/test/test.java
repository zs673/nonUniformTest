package test;

import java.util.ArrayList;

import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;

public class test {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 99999999;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 8;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 30;
	public static double RESOURCE_SHARING_FACTOR = .6;

	public static void main(String[] args) {

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			System.out.println(i);

			SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD, 0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION,
					TOTAL_PARTITIONS, NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, SchedulabilityTest.CS_LENGTH_RANGE.VERY_LONG_CSLEN,
					SchedulabilityTest.RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

//			 generator.testifyGeneratedTasksetAndResource(tasks, resources);
		}

	}

}
