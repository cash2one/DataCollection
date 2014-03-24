package edu.uiowa.datacollection.tests;

import java.io.PrintStream;

public class TestResult
{
	private int passRuns;
	private int totalRuns;
	private String testName;
	private PrintStream log;

	private static final String DIVIDER_LINE = "--------------------------------------------------------------------------------";

	/**
	 * Constructor for TestResult
	 * @param testName The name of the test
	 * @param log The output manner, System.out prints test data to the console,
	 * a different printstream allows writing to file.
	 */
	public TestResult(String testName, PrintStream log)
	{
		this.testName = testName;
		this.log = log;
	}

	/**
	 * This function is called to add a test case to the test.
	 * 
	 * @param runName
	 *            The name of the test case
	 * @param passed
	 *            A boolean value indicating if the test case passed
	 * @param printResult
	 *            A boolean value indicating if the test case should be printed
	 */
	public void addResult(String runName, boolean passed, boolean printResult)
	{
		totalRuns++;
		if (passed)
			passRuns++;

		if (printResult)
		{
			log.println(DIVIDER_LINE);
			log.println("----------    " + runName);
			log.println();
			if (passed)
				log.println("    PASS: ( X )    FAIL: (   )");
			else
				log.println("    PASS: (   )    FAIL: ( X )");
			log.println();
			log.println(DIVIDER_LINE);
		}
	}

	public String getTestName()
	{
		return testName;
	}

	/**
	 * 
	 * @return The number of passing test cases
	 */
	public int getPassRuns()
	{
		return passRuns;
	}

	/**
	 * 
	 * @return The number of test cases
	 */
	public int getTotalRuns()
	{
		return totalRuns;
	}

	/**
	 * 
	 * @return the percentage of tests that passed
	 */
	public int getPassPercentage()
	{
		return (int) ((double) passRuns / (double) totalRuns * 100);
	}

	public String toString()
	{
		return DIVIDER_LINE + "\n" + "----------    " + testName + "\n\n"
				+ "              Passed: " + passRuns + "\n"
				+ "              Total: " + totalRuns + "\n"
				+ "              Percentage: " + getPassPercentage() + "\n"
				+ DIVIDER_LINE;
	}

	/**
	 * This function is called to create a test header
	 */
	public void begin()
	{
		log.println(DIVIDER_LINE + "\n**** BEGINNING TEST '" + testName + "'\n"
				+ DIVIDER_LINE + "\n");

	}
}
