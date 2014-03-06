package edu.uiowa.datacollection.tests;

import java.io.PrintStream;

public class TestResult
{
	private int passRuns;
	private int totalRuns;
	private String testName;
	private PrintStream log;
	
	private static final String DIVIDER_LINE = 
"--------------------------------------------------------------------------------";
	
	public TestResult(String testName, PrintStream log)
	{
		this.testName = testName;
		this.log = log;
	}
	
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
	
	public int getPassRuns()
	{
		return passRuns;
	}
	
	public int getTotalRuns()
	{
		return totalRuns;
	}
	
	public int getPassPercentage()
	{
		return (int)((double)passRuns / (double)totalRuns * 100);
	}
	
	public String toString()
	{
		return DIVIDER_LINE + "\n" + "----------    " + testName + "\n\n" + 
				"              Passed: " + passRuns + "\n" + 
				"              Total: " + totalRuns + "\n" +
				"              Percentage: " + getPassPercentage() + "\n" + 
				DIVIDER_LINE;
	}

	public void begin()
	{
		log.println(DIVIDER_LINE + "\n**** BEGINNING TEST '" + testName + "'\n" + 
					DIVIDER_LINE + "\n");
		
	}
}
