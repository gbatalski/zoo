package util;

import java.io.IOException;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class AWSUtil {
	
	public static final String PATH_TO_PROPERT_FILE="aws_conf.properties";
	public static final String AWS_ID_PROPERTY = "AWS_ID";
	public static final String AWS_SECRET_KEY_PROPERTY = "AWS_SECRET_KEY";
	
	public static AWSCredentials getAWSCredentials() throws IOException {
		Properties awsConf = new Properties();
		awsConf.load(AWSCredentials.class.getResourceAsStream("/"+PATH_TO_PROPERT_FILE));
		final String awsID = awsConf.getProperty(AWS_ID_PROPERTY);
		final String awsSecretKey =  awsConf.getProperty(AWS_SECRET_KEY_PROPERTY);
		
		return new BasicAWSCredentials(awsID,awsSecretKey);
	}

}
