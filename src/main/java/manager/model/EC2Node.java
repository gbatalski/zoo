package manager.model;

import java.io.StringReader;

import org.yaml.snakeyaml.Yaml;

public class EC2Node {

	private String imageId;

	private String instanceId;

	private String keyPairId;

	private byte[] keyMaterial;

	private String instanceType;

	private String region;

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getKeyPairId() {
		return keyPairId;
	}

	public void setKeyPairId(String keyPairId) {
		this.keyPairId = keyPairId;
	}

	public String getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(String instanceType) {
		this.instanceType = instanceType;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public byte[] getKeyMaterial() {
		return keyMaterial;
	}

	public void setKeyMaterial(byte[] keyMaterial) {
		this.keyMaterial = keyMaterial;
	}

	public String dumpYaml() {
		return new Yaml().dump(this);
	}

	public static EC2Node fromYaml(String yaml) {
		return (EC2Node) new Yaml().load(new StringReader(yaml));
	}

}
