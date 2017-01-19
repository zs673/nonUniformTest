package test;

import java.util.ArrayList;

import analysis.NewMrsP;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;
import generatorTools.SystemGenerator.CS_LENGTH_RANGE;
import generatorTools.SystemGenerator.RESOURCES_RANGE;

public class test {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1;
	public static int TOTAL_PARTITIONS = 5;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;
	public static int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = 3;
	public static int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
	public static double RESOURCE_SHARING_FACTOR = .4;

	public static void main(String[] args) {

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD,
				0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN,
				RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		int a = 0, b = 0, c = 0;
		long[][] Ris;
		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			System.out.println(i);

			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);
			generator.testifyGeneratedTasksetAndResource(tasks, resources);

			NewMrsP MrsP_mig = new NewMrsP(0, 0, false);
			Ris = MrsP_mig.schedulabilityTest(tasks, resources);
			if (isSystemSchedulable(tasks, Ris))
				a++;

			MrsP_mig = new NewMrsP(1, 0, false);
			Ris = MrsP_mig.schedulabilityTest(tasks, resources);
			if (isSystemSchedulable(tasks, Ris))
				b++;

			MrsP_mig = new NewMrsP(1, 20, false);
			Ris = MrsP_mig.schedulabilityTest(tasks, resources);
			if (isSystemSchedulable(tasks, Ris))
				c++;

			

			// generator.testifyGeneratedTasksetAndResource(tasks, resources);
		}

		System.out.println(a + "	" + b + "	" + c);
	}

	public static boolean isSystemSchedulable(ArrayList<ArrayList<SporadicTask>> tasks, long[][] Ris) {
		for (int i = 0; i < tasks.size(); i++) {
			for (int j = 0; j < tasks.get(i).size(); j++) {
				if (tasks.get(i).get(j).deadline < Ris[i][j])
					return false;
			}
		}
		return true;
	}

}
