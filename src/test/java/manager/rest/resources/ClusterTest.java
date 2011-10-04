package manager.rest.resources;

import java.io.IOException;

import manager.model.Cluster;
import manager.zk.ClusterManager;
import manager.zk.ZooKeeperClient;

import org.apache.zookeeper.KeeperException;
import org.junit.BeforeClass;
import org.junit.Test;

public class ClusterTest {

	Cluster cluster;

	static ClusterManager clusterManager;

	static final String clusterResourceName = "testcluster1";

	@BeforeClass
	public static void doInit() throws KeeperException, IOException,
			InterruptedException {

		ZooKeeperClient zkClient = ZooKeeperClient.getInstance();
		clusterManager = new ClusterManager(zkClient);

	}

	/**
	 * Handle POST requests: create a new cluster.
	 * 
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	@Test
	public void createCluster() throws KeeperException, InterruptedException {

		clusterManager.createCluster(clusterResourceName);

	}

}
