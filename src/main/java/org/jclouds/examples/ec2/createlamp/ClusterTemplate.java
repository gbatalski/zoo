/**
 * 
 */
package org.jclouds.examples.ec2.createlamp;

import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.logging.Logger;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * @author gena
 * 
 */
public class ClusterTemplate {

	public ClusterTemplate() {
		super();		
	}

	
	@Resource
	protected Logger logger = Logger.CONSOLE;

	private String name;

	private List<InstanceTemplate> instances = Lists.newArrayList();

	public ClusterTemplate(String name) {
		this();
		this.name = name;
	}

	public ClusterTemplate withInstance(InstanceTemplate instanceTemplate) {
		instances.add(instanceTemplate);
		return this;
	}

	public List<RunningInstance> run() {
		List<RunningInstance> runningInstances = Lists.newArrayList();
		for (InstanceTemplate instanceTemplate : instances) {
			try {
				runningInstances.add(instanceTemplate.run());
			} catch (TimeoutException e) {
				logger.error("", e);
			}
		}
		return runningInstances;
	}

	public void terminate() {
		for (InstanceTemplate instanceTemplate : instances) {
			instanceTemplate.terminate();
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<InstanceTemplate> getInstances() {
		return instances;
	}

	public void setInstances(List<InstanceTemplate> instances) {
		this.instances = instances;
	}

	public String toYaml() {
		return new Yaml().dump(this);
	}

	public static ClusterTemplate buildFromYaml(String yaml) {
		return (ClusterTemplate) new Yaml().load(yaml);
	}

}
