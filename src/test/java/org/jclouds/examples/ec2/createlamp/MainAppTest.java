/**
 *
 * Copyright (C) 2011 Cloud Conscious, LLC. <info@cloudconscious.com>
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

package org.jclouds.examples.ec2.createlamp;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.Test;

import util.AWSUtil;

/**
 * This the Main class of an Application that demonstrates the use of the
 * EC2Client by creating a small lamp server.
 *
 * Usage is: java MainApp accesskeyid secretkey command name where command in
 * create destroy
 *
 * @author Adrian Cole
 */
public class MainAppTest {

	private static String accesskeyid;

	private static String secretkey;

	private static String name = "myjcloudtest";

	@BeforeClass
	public static void init() throws IOException {
		accesskeyid = AWSUtil.getAWSCredentials()
								.getAWSAccessKeyId();
		secretkey = AWSUtil.getAWSCredentials()
							.getAWSSecretKey();

	}

	@Test
	public void testMainAppStart() throws TimeoutException {
		MainApp.main(new String[] { accesskeyid, secretkey, "create", name });
	}

	@Test
	public void testMainAppStop() throws TimeoutException {
		MainApp.main(new String[] { accesskeyid, secretkey, "destroy", name });
	}

}
