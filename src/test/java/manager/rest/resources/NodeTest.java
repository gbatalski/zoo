package manager.rest.resources;

import java.io.IOException;

import manager.aws.ec2.EC2ClusterManager;
import manager.model.Node;
import manager.zk.ZooKeeperClient;

import org.apache.zookeeper.KeeperException;
import org.junit.BeforeClass;
import org.junit.Test;

public class NodeTest {

	Node node;

	static EC2ClusterManager ec2ClusterManager;

	static final String clusterResourceName = "testcluster1";

	static String nodeResourceName;

	@BeforeClass
	public static void doInit() throws KeeperException, IOException,
			InterruptedException {

		ZooKeeperClient zkClient = ZooKeeperClient.getInstance();
		ec2ClusterManager = new EC2ClusterManager(zkClient);

	}

	/**
	 * Handle POST requests: create a new node in a cluster.
	 * 
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	@Test
	public void createNode() throws KeeperException, InterruptedException {

		nodeResourceName = ec2ClusterManager.createEC2DefaultMicroNode(clusterResourceName);

	}

	/**
	 * Handle DELETE requests: delete a node in a cluster.
	 * 
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	@Test
	public void deleteNode() throws KeeperException, InterruptedException {

		ec2ClusterManager.destroyEC2Node(clusterResourceName, nodeResourceName);

	}

}
