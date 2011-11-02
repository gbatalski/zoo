/**
 * 
 */
package cloud.node.cluster.utils;

import java.lang.reflect.Field;

import com.jcraft.jcterm.JCTermSwingFrame;
import com.jcraft.jcterm.JSchSession;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

/**
 * @author gena
 * 
 */
public class JCTermCloud {
	final private JCTermSwingFrame frame;

	public JCTermCloud(String username, String hostname, int port,
			byte[] keyMaterial) {
		super();
		JSch jSch = new JSch();
		if (keyMaterial != null)
			try {
				jSch.addIdentity(username, // String userName
									keyMaterial, // byte[] privateKey
									null, // byte[] publicKey
									new byte[0] // byte[] passPhrase
				);
			} catch (JSchException e) {
				throw new RuntimeException(e);
			}
		Field jschField;

		try {
			jschField = JSchSession.class.getDeclaredField("jsch");

			jschField.setAccessible(true);
			jschField.set(null, jSch);

			JSchSession.getSession(	username,
									null,
									hostname,
									port,
									new UserInfo() {

										@Override
										public void showMessage(String message) {

										}

										@Override
										public boolean promptYesNo(
												String message) {

											return true;
										}

										@Override
										public boolean promptPassword(
												String message) {

											return false;
										}

										@Override
										public boolean promptPassphrase(
												String message) {

											return false;
										}

										@Override
										public String getPassword() {

											return null;
										}

										@Override
										public String getPassphrase() {

											return null;
										}
									},
									null);

			frame = new JCTermSwingFrame(	username + "@" + hostname + ":"
													+ port,
											username + "@" + hostname + ":"
													+ port);
			frame.setVisible(true);
			frame.setResizable(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
