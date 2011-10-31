/**
 *
 */
package org.jclouds.examples.ec2.createlamp;

import static org.jclouds.ec2.domain.InstanceType.T1_MICRO;
import static org.jclouds.ec2.domain.IpProtocol.TCP;
import static org.jclouds.ec2.options.RunInstancesOptions.Builder.asType;
import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.InstanceState;
import org.jclouds.ec2.domain.IpProtocol;
import org.jclouds.ec2.domain.KeyPair;
import org.jclouds.ec2.domain.Reservation;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.ec2.predicates.InstanceStateRunning;
import org.jclouds.ec2.predicates.InstanceStateTerminated;
import org.jclouds.logging.Logger;
import org.jclouds.net.IPSocket;
import org.jclouds.predicates.InetSocketAddressConnect;
import org.jclouds.predicates.RetryablePredicate;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.yaml.snakeyaml.Yaml;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import static com.google.common.base.Strings.*;

/**
 * @author gena
 * 
 */
public class InstanceTemplate {

	public InstanceTemplate() {
		super();
	}

	@Inject
	private EC2Client client;

	@Resource
	protected Logger logger = Logger.CONSOLE;

	private String clusterName;
	
	private String name;

	private String region = null;

	private String zone = null;

	private List<String> script = Lists.newArrayList();

	private List<PortRange> portRanges = Lists.newArrayList();

	private List<Integer> testPorts = Lists.newArrayList();

	private String instanceType = T1_MICRO;

	private String osFamily = "UNIX";

	private String image = "ami-bbf539d2";

	private String instanceId;

	private NodeMetadata nodeMetadata;

	private String keyMaterial;

	public InstanceTemplate(String name) {
		this();
		this.name = name;
	}

	public InstanceTemplate withImage(String image) {
		this.image = image;
		return this;
	}

	public InstanceTemplate withinRegion(String region) {
		this.region = region;
		return this;
	}

	public InstanceTemplate withinZone(String zone) {
		this.zone = zone;
		return this;
	}

	public InstanceTemplate withTcpPort(int tcpPort) {
		portRanges.add(new PortRange(tcpPort));
		return this;
	}

	public InstanceTemplate withTcpPortAndTestPort(int tcpPort) {
		portRanges.add(new PortRange(tcpPort));

		return withTcpTestPort(tcpPort);
	}

	public InstanceTemplate withTcpTestPort(int tcpPort) {
		testPorts.add(tcpPort);
		return this;
	}

	public InstanceTemplate withUnixOsFamily() {
		this.osFamily = "UNIX";
		return this;
	}

	public InstanceTemplate withInstanceType(String instanceType) {
		this.instanceType = instanceType;
		return this;
	}

	public InstanceTemplate withScriptLine(String scriptLine) {
		this.script.add(scriptLine);
		return this;
	}

	public static class PortRange {
		public PortRange(int fromPort) {
			this(	TCP,
					fromPort,
					fromPort,
					"0.0.0.0/0");

		}

		public PortRange() {
			super();
		}

		public PortRange(IpProtocol ipProtocol, int fromPort, int toPort,
				String cidrIp) {
			this();
			this.ipProtocol = ipProtocol;
			this.fromPort = fromPort;
			this.toPort = toPort;
			this.cidrIp = cidrIp;
		}

		private IpProtocol ipProtocol = TCP;

		private int fromPort;

		private int toPort;

		private String cidrIp;

		public IpProtocol getIpProtocol() {
			return ipProtocol;
		}

		public void setIpProtocol(IpProtocol ipProtocol) {
			this.ipProtocol = ipProtocol;
		}

		public int getFromPort() {
			return fromPort;
		}

		public void setFromPort(int fromPort) {
			this.fromPort = fromPort;
		}

		public int getToPort() {
			return toPort;
		}

		public void setToPort(int toPort) {
			this.toPort = toPort;
		}

		public String getCidrIp() {
			return cidrIp;
		}

		public void setCidrIp(String cidrIp) {
			this.cidrIp = cidrIp;
		}

	}

	private String createSecurityGroupAndAuthorizePorts() {
		client.getSecurityGroupServices()
				.createSecurityGroupInRegion(region, name, name);
		for (PortRange portRange : portRanges)
			client.getSecurityGroupServices()
					.authorizeSecurityGroupIngressInRegion(	region,
															name,
															portRange.ipProtocol,
															portRange.fromPort,
															portRange.toPort,
															portRange.cidrIp);

		return name;
	}

	private KeyPair createKeyPair() {
		return client.getKeyPairServices()
						.createKeyPairInRegion(region, name);
	}

	private String buildScript() {
		ScriptBuilder scriptBuilder = new ScriptBuilder();
		for (String scriptLine : script) {
			scriptBuilder.addStatement(exec(scriptLine));
		}

		return scriptBuilder.render(OsFamily.valueOf(osFamily));
	}

	private RunningInstance runInstance() {
		KeyPair keyPair = createKeyPair();
		keyMaterial = keyPair.getKeyMaterial();
		Reservation<? extends RunningInstance> reservation = client.getInstanceServices()
																	.runInstancesInRegion(	region,
																							zone,
																							image,
																							1,
																							1,
																							asType(instanceType).withKeyName(keyPair.getKeyName())
																												.withSecurityGroup(createSecurityGroupAndAuthorizePorts())
																												.withUserData(buildScript().getBytes()));

		return Iterables.getOnlyElement(reservation);

	}

	public void terminate() {
		try {
			String id = findInstanceByKeyName().getId();
			logger.info(String.format("%s terminating instance", id));
			RunningInstance instance = findInstanceByKeyName();
			client.getInstanceServices()
					.terminateInstancesInRegion(region, instance.getId());
			blockUntilInstanceTerminated(instance);
		} catch (NoSuchElementException e) {
		} catch (Exception e) {
			logger.error("", e);
		}

		try {
			logger.info(String.format("%s deleting keypair", name));
			client.getKeyPairServices()
					.deleteKeyPairInRegion(region, name);
		} catch (Exception e) {
			logger.error("", e);
		}

		try {
			logger.info(String.format("%s deleting group", name));
			client.getSecurityGroupServices()
					.deleteSecurityGroupInRegion(region, name);
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public RunningInstance run() throws TimeoutException {

		RunningInstance ri = blockUntilInstanceRunning(runInstance());
		instanceId = ri.getId();
		if (Strings.isNullOrEmpty(region))
			region = ri.getRegion();

		if (Strings.isNullOrEmpty(zone))
			zone = ri.getAvailabilityZone();

		return ri;
	}

	private RunningInstance blockUntilInstanceRunning(RunningInstance instance)
			throws TimeoutException {
		// create utilities that wait for the instance to finish
		RetryablePredicate<RunningInstance> runningTester = new RetryablePredicate<RunningInstance>(new InstanceStateRunning(client),
																									180,
																									5,
																									TimeUnit.SECONDS);

		logger.info(String.format(	"%s awaiting instance to run ",
									instance.getId()));

		if (!runningTester.apply(instance))
			throw new TimeoutException("timeout waiting for instance to run: "
					+ instance.getId());

		instance = findInstanceById(instance.getId());

		for (int testPort : testPorts) {
			RetryablePredicate<IPSocket> socketTester = new RetryablePredicate<IPSocket>(	new InetSocketAddressConnect(),
																							300,
																							1,
																							TimeUnit.SECONDS);

			logger.info(String.format(	"%s awaiting service to start listening on port %d",
										instance.getIpAddress(),
										testPort));
			if (!socketTester.apply(new IPSocket(	instance.getIpAddress(),
													testPort)))
				throw new TimeoutException(String.format(	"timeout waiting for service to start listening on  %s:%d: ",
															instance.getIpAddress(),
															testPort));

			logger.info(String.format(	"%s service started listening on port %d",
										instance.getIpAddress(),
										testPort));

		}
		return instance;
	}

	private void blockUntilInstanceTerminated(RunningInstance instance)
			throws TimeoutException {
		// create utilities that wait for the instance to finish
		RetryablePredicate<RunningInstance> terminatedTester = new RetryablePredicate<RunningInstance>(	new InstanceStateTerminated(client),
																										180,
																										5,
																										TimeUnit.SECONDS);
		logger.info(String.format(	"%s awaiting instance to terminate ",
									instance.getId()));
		if (!terminatedTester.apply(instance))
			throw new TimeoutException("timeout waiting for instance to terminate: "
					+ instance.getId());
	}

	private RunningInstance findInstanceById(String instanceId) {
		// search my account for the instance I just created
		Set<? extends Reservation<? extends RunningInstance>> reservations = client.getInstanceServices()
																					.describeInstancesInRegion(	null,
																												instanceId);
		// search
		// since we refined by instanceId there should only be one instance
		return Iterables.getOnlyElement(Iterables.getOnlyElement(reservations));
	}

	private RunningInstance findInstanceByKeyName() {
		// search my account for the instance I just created
		Set<? extends Reservation<? extends RunningInstance>> reservations = client.getInstanceServices()
																					.describeInstancesInRegion(null);

		// extract all the instances from all reservations
		Set<RunningInstance> allInstances = Sets.newHashSet();
		for (Reservation<? extends RunningInstance> reservation : reservations) {
			allInstances.addAll(reservation);
		}

		// get the first one that has a keyname matching what I just created
		return Iterables.find(allInstances, new Predicate<RunningInstance>() {

			public boolean apply(RunningInstance input) {
				return input.getKeyName()
							.equals(name)
						&& input.getInstanceState() != InstanceState.TERMINATED;
			}

		});
	}

	public String toYaml() {
		return new Yaml().dump(this);
	}

	public InstanceTemplate buildFromYaml(String yamlRepresentation) {
		return (InstanceTemplate) new Yaml().load(yamlRepresentation);
	}

	public String getName() {
		return name;
	}

	public String getRegion() {
		return region;
	}

	public String getZone() {
		return zone;
	}

	public List<String> getScript() {
		return script;
	}

	public List<PortRange> getPortRanges() {
		return portRanges;
	}

	public List<Integer> getTestPorts() {
		return testPorts;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public String getOsFamily() {
		return osFamily;
	}

	public String getImage() {
		return image;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getKeyMaterial() {
		return keyMaterial;
	}

	public NodeMetadata getNodeMetadata() {
		return nodeMetadata;
	}

	public void setNodeMetadata(NodeMetadata nodeMetadata) {
		this.nodeMetadata = nodeMetadata;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public void setScript(List<String> script) {
		this.script = script;
	}

	public void setPortRanges(List<PortRange> portRanges) {
		this.portRanges = portRanges;
	}

	public void setTestPorts(List<Integer> testPorts) {
		this.testPorts = testPorts;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public void setOsFamily(String osFamily) {
		this.osFamily = osFamily;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public void setKeyMaterial(String keyMaterial) {
		this.keyMaterial = keyMaterial;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}
}
