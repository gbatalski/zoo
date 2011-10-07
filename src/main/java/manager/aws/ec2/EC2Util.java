package manager.aws.ec2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import manager.model.EC2Node;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class EC2Util {

	static Logger logger = Logger.getLogger(EC2Util.class);

	private AmazonEC2Client ec2Client;

	private String regionName;

	final int PING_INTERVAL = 5000;

	int timeoutCountdown = 20; // #intervals

	private EC2Util(AmazonEC2Client ec2Client) {
		this.ec2Client = ec2Client;
	}

	public static EC2Util instance(AmazonEC2Client ec2Client) throws IOException {
		return new EC2Util(ec2Client);
	}

	enum State {
		PENDING("pending"),
		RUNNING("running"),
		SHUTTINGDOWN("shutting-down"),
		TERMINATING("terminated");
		private String name;

		private State(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static State value(String stringValue) {
			for (State s : State.values())
				if (s.name.equalsIgnoreCase(stringValue))
					return s;
			return null;
		}

	}

	public List<EC2Node> createEC2Nodes(String clusterName,
			String instanceType, String imageId, int maxCount) {

		List<EC2Node> nodes = new ArrayList<EC2Node>();

		// Try to create a new unique key pair.
		String uuidKeyPairName = UUID.randomUUID()
										.toString();
		CreateKeyPairRequest createKeypairReq = new CreateKeyPairRequest(uuidKeyPairName);

		CreateKeyPairResult createKeyPairResult = ec2Client.createKeyPair(createKeypairReq);
		final KeyPair keypair = createKeyPairResult.getKeyPair();

		RunInstancesRequest req = new RunInstancesRequest();
		req.setKeyName(keypair.getKeyName());
		// req.setSecurityGroups(securityGroups);
		req.setImageId(imageId);
		req.setInstanceType(instanceType);
		req.setMinCount(1);
		req.setMaxCount(maxCount);
		RunInstancesResult result = ec2Client.runInstances(req);

		List<Instance> instances = result.getReservation()
											.getInstances();

		for (Instance i : instances) {
			EC2Node node = new EC2Node();
			node.setImageId(imageId);
			node.setInstanceType(instanceType);
			node.setInstanceId(i.getInstanceId());
			node.setKeyPairId(uuidKeyPairName);
			node.setRegion(regionName);
			node.setKeyMaterial(keypair.getKeyMaterial()
										.getBytes());

			logger.info("Successfully created EC2 instance with id "
					+ i.getInstanceId());

			// Poll EC2 instance until it has been created
			boolean ec2InstanceCreated = false;

			DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
			ArrayList<String> instanceIds = new ArrayList<String>();
			instanceIds.add(i.getInstanceId());
			describeInstancesRequest.setInstanceIds(instanceIds);
			while (!ec2InstanceCreated && timeoutCountdown-- > 0) {
				try {
					Thread.sleep(PING_INTERVAL);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances(describeInstancesRequest);
				for (Reservation res : describeInstancesResult.getReservations()) {
					for (Instance resInstance : res.getInstances()) {
						InstanceState state = resInstance.getState();
						if (resInstance.getInstanceId()
										.equals(i.getInstanceId())
								&& State.RUNNING == State.value(state.getName())) {
							ec2InstanceCreated = true;

						}
					}
				}
			}
			if (ec2InstanceCreated)
				nodes.add(node);
			else
				logger.error("Failed to launch EC2 instance.");

		}

		return nodes;
	}

	public EC2Node createEC2MicroNode(String clusterName, String imageID)
			throws KeeperException, InterruptedException {
		return createEC2Nodes(	clusterName,
								InstanceType.T1Micro.toString(),
								imageID,
								1).get(0);
	}

	public EC2Node createEC2DefaultMicroNode(String clusterName)
			throws KeeperException, InterruptedException {
		return createEC2Nodes(	clusterName,
								InstanceType.T1Micro.toString(),
								"ami-359ea941",
								1).get(0);
	}

	public void destroyEC2Node(String clusterName, String nodeName) {

		TerminateInstancesRequest req = new TerminateInstancesRequest();
		List<String> instanceIds = new ArrayList<String>();
		instanceIds.add(nodeName);
		req.setInstanceIds(instanceIds);
		ec2Client.terminateInstances(req);

	}

}
