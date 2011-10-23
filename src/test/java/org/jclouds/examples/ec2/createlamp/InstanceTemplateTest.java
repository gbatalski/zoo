/**
 *
 */
package org.jclouds.examples.ec2.createlamp;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.ec2.EC2AsyncClient;
import org.jclouds.ec2.EC2Client;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.rest.RestContext;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;

import util.AWSUtil;

/**
 * @author gena
 *
 */
public class InstanceTemplateTest {

	private static String name = "myjcloudtest";

	private static RestContext<EC2Client, EC2AsyncClient> context;

	private static EC2Client client;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// example of injecting a ssh implementation
		Iterable<Module> modules = ImmutableSet.<Module> of(new JschSshClientModule(),
															new SLF4JLoggingModule(),
															new EnterpriseConfigurationModule());

		// Init
		context = new ComputeServiceContextFactory().createContext(	"aws-ec2",
																	AWSUtil.getAWSCredentials()
																			.getAWSAccessKeyId(),
																	AWSUtil.getAWSCredentials()
																			.getAWSSecretKey(),
																	modules)
													.getProviderSpecificContext();

		// Get a synchronous client
		client = context.getApi();

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		context.close();
	}

	/**
	 * Test method for
	 * {@link org.jclouds.examples.ec2.createlamp.InstanceTemplate#run(org.jclouds.ec2.EC2Client)}
	 * .
	 *
	 * @throws TimeoutException
	 */
	@Test
	public void testRun() throws TimeoutException {
		InstanceTemplate it = new InstanceTemplate(name).withTcpPortAndTestPort(22)
														//.withTcpPortAndTestPort(80)
														.withTcpPortAndTestPort(7000)
														.withTcpPortAndTestPort(7199)
														.withTcpPortAndTestPort(8888)
														.withTcpPortAndTestPort(9160)
														.withScriptLine("add-apt-repository -y 'deb http://www.apache.org/dist/cassandra/debian 10x main'")
														.withScriptLine("gpg --keyserver pgp.mit.edu --recv-keys F758CE318D77295D")
														.withScriptLine("gpg --export --armor F758CE318D77295D | sudo apt-key add -")
														.withScriptLine("gpg --keyserver pgp.mit.edu --recv-keys 2B5C1B00")
														.withScriptLine("gpg --export --armor 2B5C1B00 | sudo apt-key add -")
														.withScriptLine("apt-get update")
														.withScriptLine("apt-get install -y cassandra");

		it.run(client);
		System.out.println(it.toYaml());
	}

	/**
	 * Test method for
	 * {@link org.jclouds.examples.ec2.createlamp.InstanceTemplate#terminate(org.jclouds.ec2.EC2Client)}
	 * .
	 */
	@Test
	public void testTerminate() {
		new InstanceTemplate(name).terminate(client);
	}

}
