/**
 * 
 */
package org.jclouds.examples.ec2.createlamp;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.ec2.EC2AsyncClient;
import org.jclouds.ec2.EC2Client;
import org.jclouds.rest.RestContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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

		// Init
		context = new ComputeServiceContextFactory().createContext(	"aws-ec2",
																	AWSUtil.getAWSCredentials()
																			.getAWSAccessKeyId(),
																	AWSUtil.getAWSCredentials()
																			.getAWSSecretKey())
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
														.withTcpPortAndTestPort(80)
														.withScriptLine("apt-get update")
														.withScriptLine("apt-get install -y apache2");

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
