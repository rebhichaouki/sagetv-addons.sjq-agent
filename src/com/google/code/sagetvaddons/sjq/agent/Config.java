/*
 *      Copyright 2010 Battams, Derek
 *       
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 */
package com.google.code.sagetvaddons.sjq.agent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.code.sagetvaddons.sjq.shared.Task;

public final class Config {
	static private final Logger LOG = Logger.getLogger(Config.class);
	static private final String DEFAULT_PROPS = "sjqagent.properties";
	static private final String REFERENCE_PROPS = "sjqagent.properties.ref";
	static private final String TASK_PREFIX = "TASK.";
	static private final int DEFAULT_PORT = 23344;
	static private final String DEFAULT_SCHED = "* * * * *";
	static private final int DEFAULT_RESOURCES = 100;
	
	static private Config INSTANCE = null;
	static public final Config get(String propsPath) {
		if(INSTANCE == null)
			INSTANCE = new Config(propsPath);
		return INSTANCE; 
	}
	static public final Config get() {
		return get(DEFAULT_PROPS);
	}
	
	private Properties props;
	private int port;
	private String schedule;
	private int totalResources;
	private ClassLoader clsLoader;
	
	private Map<String, Task> tasks;
	
	private Config(String propsPath) {
		port = DEFAULT_PORT;
		schedule = DEFAULT_SCHED;
		totalResources = DEFAULT_RESOURCES;
		clsLoader = null;
		
		File propsFile = new File(propsPath);
		if(!propsFile.exists()) {
			LOG.warn("Unable to find specified props file! [" + propsFile.getAbsolutePath() + "]");
			LOG.warn("Checking for default props file...");
			propsFile = new File(DEFAULT_PROPS);
			if(!propsFile.exists()) {
				LOG.warn("Unable to find default props file! [" + propsFile.getAbsolutePath() + "]");
				LOG.warn("Creating default props file...");
				File refFile = new File(REFERENCE_PROPS);
				if(!refFile.exists())
					throw new RuntimeException("Reference props file missing!  Your installation appears to be corrupted!");
				try {
					FileUtils.copyFile(refFile, propsFile);
				} catch (IOException e) {
					throw new RuntimeException("Unable to create default props file!", e);
				}
			}
		}
		props = new Properties();
		try {
			props.load(new FileReader(propsFile));
		} catch (IOException e) {
			throw new RuntimeException("Cannot read props file! [" + propsFile.getAbsolutePath() + "]", e);
		}
		tasks = new HashMap<String, Task>();
		parseProps();
	}
	
	private void parseProps() {
		for(Object k : props.keySet()) {
			if(k.toString().toUpperCase().startsWith(TASK_PREFIX)) {
				String[] parts = k.toString().split("\\.");
				if(parts.length != 3) {
					LOG.warn("Invalid task option; skipping! [" + k + "]");
					continue;
				}
				Task t = tasks.get(parts[1].toUpperCase());
				if(t == null) {
					t = new Task();
					t.setId(parts[1].toUpperCase());
					tasks.put(parts[1].toUpperCase(), t);
				}
				String option = parts[2].toUpperCase();
				if(option.equals("EXE"))
					t.setExecutable(props.getProperty(k.toString()));
				else if(option.equals("ARGS"))
					t.setExeArguments(props.getProperty(k.toString()));
				else if(option.equals("SCHEDULE"))
					t.setSchedule(props.getProperty(k.toString()));
				else if(option.equals("RESOURCES"))
					t.setRequiredResources(Integer.parseInt(props.getProperty(k.toString())));
				else if(option.equals("MAXPROCS"))
					t.setMaxInstances(Integer.parseInt(props.getProperty(k.toString())));
				else if(option.equals("MAXTIME"))
					t.setMaxTime(Integer.parseInt(props.getProperty(k.toString())));
				else if(option.equals("MAXTIMERATIO"))
					t.setMaxTimeRatio(Float.parseFloat(props.getProperty(k.toString())));
				else if(option.equals("RCMIN"))
					t.setMinReturnCode(Integer.parseInt(props.getProperty(k.toString())));
				else if(option.equals("RCMAX"))
					t.setMaxReturnCode(Integer.parseInt(props.getProperty(k.toString())));
				else if(option.equals("TEST"))
					t.setTest(props.getProperty(k.toString()));
				else if(option.equals("TESTARGS"))
					t.setTestArgs(props.getProperty(k.toString()));
				else
					LOG.warn("Unknown property '" + option + "' defined for task '" + parts[1].toUpperCase() + "', skipping!");
			} else if(k.toString().toUpperCase().equals("AGENT.PORT"))
				port = Integer.parseInt(props.get(k).toString());
			else if(k.toString().toUpperCase().equals("AGENT.SCHEDULE"))
				schedule = props.getProperty(k.toString());
			else if(k.toString().toUpperCase().equals("AGENT.RESOURCES"))
				totalResources = Integer.parseInt(props.getProperty(k.toString()));
			else
				LOG.warn("Unrecognized property skipped! [" + k + "]");
		}
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	/**
	 * @return the schedule
	 */
	public String getSchedule() {
		return schedule;
	}
	/**
	 * @return the totalResources
	 */
	public int getTotalResources() {
		return totalResources;
	}
	
	public Task[] getTasks() {
		return tasks.values().toArray(new Task[0]);
	}
	
	public String[] getTaskIds() {
		return tasks.keySet().toArray(new String[0]);
	}
	/**
	 * @return the clsLoader
	 */
	public ClassLoader getClsLoader() {
		return clsLoader;
	}
	/**
	 * @param clsLoader the clsLoader to set
	 */
	public void setClsLoader(ClassLoader clsLoader) {
		this.clsLoader = clsLoader;
	}
}