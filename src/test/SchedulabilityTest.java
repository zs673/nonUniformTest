package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import analysis.FIFONPLinearJava;
import analysis.NewMrsP;
import analysis.RTAWithoutBlocking;
import entity.Resource;
import entity.SporadicTask;
import generatorTools.SystemGenerator;
import generatorTools.SystemGenerator.CS_LENGTH_RANGE;
import generatorTools.SystemGenerator.RESOURCES_RANGE;

public class SchedulabilityTest {

	public static int TOTAL_NUMBER_OF_SYSTEMS = 1000;
	public static int TOTAL_PARTITIONS = 16;
	public static int MIN_PERIOD = 1;
	public static int MAX_PERIOD = 1000;

	public static void main(String[] args) throws InterruptedException {
		int experiment = 0;
		int bigSet = 0;
		int smallSet = 0;

		if (args.length == 3) {
			experiment = Integer.parseInt(args[0]);
			bigSet = Integer.parseInt(args[1]);
			smallSet = Integer.parseInt(args[2]);

			switch (experiment) {
			case 1:
				experimentIncreasingWorkLoad(bigSet, smallSet);
				break;
			case 2:
				experimentIncreasingCriticalSectionLength(bigSet, smallSet);
				break;
			case 3:
				experimentIncreasingContention(bigSet, smallSet);
				break;
			default:
				break;
			}

		} else
			System.err.println("wrong parameter.");

	}

	public static void experimentIncreasingWorkLoad(int bigSet, int smallSet) {
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 2;
		double RESOURCE_SHARING_FACTOR = 0.2 + 0.1 * (double) (bigSet - 1);

		int NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION = smallSet;

		long[][] Ris;
		NewMrsP new_mrsp = new NewMrsP(0, 0, false);

		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFONPLinearJava fnp = new FIFONPLinearJava();

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD,
				0.1 * (double) NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS,
				NUMBER_OF_MAX_TASKS_ON_EACH_PARTITION, true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN,
				RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);

		String result = "";
		int schedulableSystem_New_MrsP_Analysis2 = 0;
		int schedulableSystem_No_Blocking = 0;
		int sfnp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				schedulableSystem_No_Blocking++;

				Ris = new_mrsp.schedulabilityTest(tasks, resources);
				if (isSystemSchedulable(tasks, Ris))
					schedulableSystem_New_MrsP_Analysis2++;

				Ris = fnp.schedulabilityTest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					sfnp++;

			}

			System.out.println(1 + "" + bigSet + " " + smallSet + " times: " + i);

		}

		result += (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((1 + " " + bigSet + " " + smallSet), result);
	}

	public static void experimentIncreasingCriticalSectionLength(int tasksNumConfig, int csLenConfig) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 3;
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 3 + 1 * (tasksNumConfig - 1);

		CS_LENGTH_RANGE range = null;
		switch (csLenConfig) {
		case 1:
			range = CS_LENGTH_RANGE.VERY_SHORT_CS_LEN;
			break;
		case 2:
			range = CS_LENGTH_RANGE.SHORT_CS_LEN;
			break;
		case 3:
			range = CS_LENGTH_RANGE.MEDIUM_CS_LEN;
			break;
		case 4:
			range = CS_LENGTH_RANGE.LONG_CSLEN;
			break;
		case 5:
			range = CS_LENGTH_RANGE.VERY_LONG_CSLEN;
			break;
		default:
			break;
		}

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD,
				0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS, NUMBER_OF_TASKS_ON_EACH_PARTITION,
				true, range, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR, NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsP new_mrsp = new NewMrsP(0, 0, false);
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFONPLinearJava fnp = new FIFONPLinearJava();
		String result = "";

		int schedulableSystem_New_MrsP_Analysis2 = 0;
		int schedulableSystem_No_Blocking = 0;
		int sfnp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {
			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				schedulableSystem_No_Blocking++;

				Ris = new_mrsp.schedulabilityTest(tasks, resources);
				if (isSystemSchedulable(tasks, Ris))
					schedulableSystem_New_MrsP_Analysis2++;

				Ris = fnp.schedulabilityTest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					sfnp++;

			}

			System.out.println(2 + "" + tasksNumConfig + " " + csLenConfig + " times: " + i);
		}

		result += (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS  + "\n";

		writeSystem((2 + " " + tasksNumConfig + " " + csLenConfig), result);
	}

	public static void experimentIncreasingContention(int bigSet, int smallSet) {
		double RESOURCE_SHARING_FACTOR = 0.4;
		int NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE = 1 + 5 * (smallSet - 1);
		int NUMBER_OF_TASKS_ON_EACH_PARTITION = 4 + 2 * (bigSet - 1);

		SystemGenerator generator = new SystemGenerator(MIN_PERIOD, MAX_PERIOD,
				0.1 * (double) NUMBER_OF_TASKS_ON_EACH_PARTITION, TOTAL_PARTITIONS, NUMBER_OF_TASKS_ON_EACH_PARTITION,
				true, CS_LENGTH_RANGE.VERY_SHORT_CS_LEN, RESOURCES_RANGE.PARTITIONS, RESOURCE_SHARING_FACTOR,
				NUMBER_OF_MAX_ACCESS_TO_ONE_RESOURCE);
		long[][] Ris;

		NewMrsP new_mrsp = new NewMrsP(0, 0, false);
		RTAWithoutBlocking noblocking = new RTAWithoutBlocking();
		FIFONPLinearJava fnp = new FIFONPLinearJava();

		String result = "";

		int schedulableSystem_New_MrsP_Analysis2 = 0;
		int schedulableSystem_No_Blocking = 0;
		int sfnp = 0;

		for (int i = 0; i < TOTAL_NUMBER_OF_SYSTEMS; i++) {

			ArrayList<ArrayList<SporadicTask>> tasks = generator.generateTasks();
			ArrayList<Resource> resources = generator.generateResources();
			generator.generateResourceUsage(tasks, resources);

			Ris = noblocking.NewMrsPRTATest(tasks, resources, false);
			if (isSystemSchedulable(tasks, Ris)) {
				schedulableSystem_No_Blocking++;

				Ris = new_mrsp.schedulabilityTest(tasks, resources);
				if (isSystemSchedulable(tasks, Ris))
					schedulableSystem_New_MrsP_Analysis2++;

				Ris = fnp.schedulabilityTest(tasks, resources, false);
				if (isSystemSchedulable(tasks, Ris))
					sfnp++;

			}

			System.out.println(3 + "" + bigSet + " " + smallSet + " times: " + i);
		}

		result += (double) schedulableSystem_New_MrsP_Analysis2 / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) schedulableSystem_No_Blocking / (double) TOTAL_NUMBER_OF_SYSTEMS + " "
				+ (double) sfnp / (double) TOTAL_NUMBER_OF_SYSTEMS + "\n";

		writeSystem((3 + " " + bigSet + " " + smallSet), result);
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

	public static void writeSystem(String filename, String result) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(new File("result/" + filename + ".txt"), false));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		writer.println(result);
		writer.close();
	}
}
