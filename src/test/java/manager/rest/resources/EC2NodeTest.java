/**
 *
 */
package manager.rest.resources;

import java.io.IOException;

import manager.aws.ec2.EC2Util;
import manager.model.EC2Node;

import org.apache.zookeeper.KeeperException;
import org.junit.Test;

import util.AWSUtil;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.InstanceType;

/**
 * @author gbatalski
 *
 */
public class EC2NodeTest {
	@Test
	public void testYmal() {
		EC2Node ec2Node = new EC2Node();
		ec2Node.setImageId("imageId");
		ec2Node.setInstanceId("instanceId");
		ec2Node.setInstanceType(InstanceType.T1Micro.name());
		String yaml = ec2Node.dumpYaml();
		ec2Node = EC2Node.fromYaml(yaml);
	}

	@Test
	public void testEC2Util() throws IOException, KeeperException,
			InterruptedException {
		AWSCredentials awsCredentials = AWSUtil.getAWSCredentials();
		AmazonEC2Client amazonEC2 = new AmazonEC2Client(awsCredentials);

		amazonEC2.setEndpoint(amazonEC2.describeRegions()
										.getRegions()
										.get(0)
										.getEndpoint());
		EC2Util ec2Util = EC2Util.instance(amazonEC2);
		EC2Node ec2Node = ec2Util.createEC2DefaultMicroNode("cluster1");

		System.out.println(ec2Node.dumpYaml());
		ec2Util.destroyEC2Node("cluster1", ec2Node.getInstanceId());
	}

}
