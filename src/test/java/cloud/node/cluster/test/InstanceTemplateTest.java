/**
 *
 */
package cloud.node.cluster.test;

import static org.jclouds.ec2.domain.InstanceType.M1_LARGE;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.ec2.EC2AsyncClient;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.domain.RunningInstance;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.rest.RestContext;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import util.AWSUtil;
import cloud.node.cluster.InstanceTemplate;
import cloud.node.cluster.utils.JCTermCloud;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * @author gena
 * 
 */
public class InstanceTemplateTest {

	private static String name = "myjcloudtest";

	private static RestContext<EC2Client, EC2AsyncClient> context;

	private static Injector injector;

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
		final EC2Client client = context.getApi();

		class TestModule extends AbstractModule {

			@Override
			protected void configure() {
				bind(EC2Client.class).toInstance(client);
			}

		}
		injector = Guice.createInjector(new TestModule());

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
	 * {@link cloud.node.cluster.test.InstanceTemplate#run(org.jclouds.ec2.EC2Client)}
	 * .
	 * 
	 * @throws TimeoutException
	 */
	@Test
	public void testRun() throws TimeoutException {

		InstanceTemplate it = new InstanceTemplate(name).withInstanceType(M1_LARGE)
														.withTcpPortAndTestPort(22)
														// .withTcpPortAndTestPort(80)
														.withTcpPort(7000)
														.withTcpPort(7199)
														.withTcpPort(8888)
														.withTcpPort(9160)
														.withScriptLine("add-apt-repository -y 'deb http://www.apache.org/dist/cassandra/debian 10x main'")
														.withScriptLine("gpg --keyserver pgp.mit.edu --recv-keys F758CE318D77295D")
														.withScriptLine("gpg --export --armor F758CE318D77295D | sudo apt-key add -")
														.withScriptLine("gpg --keyserver pgp.mit.edu --recv-keys 2B5C1B00")
														.withScriptLine("gpg --export --armor 2B5C1B00 | sudo apt-key add -")
														.withScriptLine("apt-get update")
														.withScriptLine("apt-get remove -y byobu")
														.withScriptLine("apt-get install -y libmx4j-java")
														.withScriptLine("apt-get install -y cassandra")
														.withScriptLine("echo EXTRA_CLASSPATH=\\\"/usr/share/java/mx4j-tools.jar\\\" >> /etc/default/cassandra")
														.withScriptLine("service cassandra restart");

		injector.injectMembers(it);
		RunningInstance ri = it.run();
		new JCTermCloud("ubuntu",
						ri.getDnsName(),
						22,
						it.getKeyMaterial()
							.getBytes());

		System.out.println(it.toYaml());
	}

	/**
	 * Test method for
	 * {@link cloud.node.cluster.test.InstanceTemplate#terminate(org.jclouds.ec2.EC2Client)}
	 * .
	 */
	@Test
	public void testTerminate() {
		InstanceTemplate it = new InstanceTemplate(name);
		injector.injectMembers(it);
		it.terminate();
	}

	// @Test
	public void testMyJCTerm() throws FileNotFoundException, IOException {
		new JCTermCloud("ubuntu",
						"ec2-50-16-24-185.compute-1.amazonaws.com",
						22,
						ByteStreams.toByteArray(new FileInputStream("/home/gena/aws/defaultkey.pem")));

	}

}
