package org.machanism.machai.process.tools;

import java.util.Map;

public class CommandResult {

	private String message;
	
	private int exitCode;

	private Map<String, Object> logReport;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getExitCode() {
		return exitCode;
	}

	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}

	public void setLog(Map<String, Object> logReport) {
		this.setLogReport(logReport);
	}

	public Map<String, Object> getLogReport() {
		return logReport;
	}

	public void setLogReport(Map<String, Object> logReport) {
		this.logReport = logReport;
	}

}
