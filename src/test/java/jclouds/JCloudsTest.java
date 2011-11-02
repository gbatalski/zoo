/**
 *
 */
package jclouds;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.ComputeServiceContextFactory;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.domain.Credentials;
import org.jclouds.domain.Location;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.io.Payloads;
import org.jclouds.logging.log4j.config.Log4JLoggingModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.ssh.jsch.config.JschSshClientModule;
import org.junit.BeforeClass;
import org.junit.Test;

import util.AWSUtil;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Module;

/**
 * @author gbatalski
 * 
 */
public class JCloudsTest {

	private static String accesskeyid;

	private static String secretkey;

	@BeforeClass
	public static void init() throws IOException {
		accesskeyid = AWSUtil.getAWSCredentials()
								.getAWSAccessKeyId();
		secretkey = AWSUtil.getAWSCredentials()
							.getAWSSecretKey();

	}

	@Test
	public void firstTest() throws RunNodesException {

		// get a context with ec2 that offers the portable ComputeService api
		ComputeServiceContext context = new ComputeServiceContextFactory().createContext(	"aws-ec2",
																							accesskeyid,
																							secretkey,
																							ImmutableSet.<Module> of(	new SLF4JLoggingModule(),
																														new JschSshClientModule()));

		// here's an example of the portable api
		Set<? extends Location> locations = context.getComputeService()
													.listAssignableLocations();

		// Set<? extends Image> images =
		// context.getComputeService().listImages();

		// pick the highest version of the RightScale CentOs template
		Template template = context.getComputeService()
									.templateBuilder()
									.osFamily(OsFamily.UBUNTU)
									.os64Bit(true)
									.osVersionMatches("11.10")
									// .imageId("ami-b9c40ad0")
									.build();

		// specify your own groups which already have the correct rules applied

		template.getOptions()
				.inboundPorts(80, 22);
		template.getOptions()
				.runScript(Payloads.newStringPayload("apt-get update -y"))
				.runAsRoot(true);
		template.getOptions()
				.runScript(Payloads.newStringPayload("apt-get install -y apache2"))
				.runAsRoot(true);
		template.getOptions()
				.blockOnPort(22, 60)
				.blockOnPort(80, 180);

		// specify your own keypair for use in creating nodes
		//template.getOptions().as(EC2TemplateOptions.class).keyPair("default");
		
		context.getCredentialStore()
				.entrySet();

		// run a couple nodes accessible via group
		Set<? extends NodeMetadata> nodes = context.getComputeService()
													.createNodesInGroup("webserver",
																		1,
																		template);

		Collection<Credentials> creds =	context.getCredentialStore().values();
		

		
		// when you need access to very ec2-specific features, use the
		// provider-specific context
		EC2Client ec2Client = EC2Client.class.cast(context.getProviderSpecificContext()
															.getApi());

		for (NodeMetadata node : nodes) {

			context.getComputeService()
					.destroyNode(node.getId());
			

		}

		// ex. to get an ip and associate it with a node

		// String ip =
		// ec2Client.getElasticIPAddressServices().allocateAddressInRegion(node.getLocation().getParent().getId());
		// ec2Client.getElasticIPAddressServices().associateAddressInRegion(node.getLocation().getParent().getId(),ip,
		// node.getProviderId());

		context.close();
	}

}
