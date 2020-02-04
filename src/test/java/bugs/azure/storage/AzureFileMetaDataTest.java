package bugs.azure.storage;

import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.ShareFileClient;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AzureFileMetaDataTest extends SelfCleaningAzureFilesTest {
	private static final String MD_KEY = "test_key";
	private static final String MD_VALUE = "test value";

	private Map<String, String> getMetaDataFromDirectory(String shareName, String directoryName) {
		ShareDirectoryClient shareDirectoryClient = getClientBuilder(shareName, directoryName).buildDirectoryClient();
		shareDirectoryClient.create();

		Map<String, String> metadata = new HashMap<>();
		metadata.put(MD_KEY, MD_VALUE);
		shareDirectoryClient.setMetadata(metadata);
		return shareDirectoryClient.getProperties().getMetadata();
	}

	private Map<String, String> getMetaDataFromFile(String shareName, String directoryName) {
		ShareFileClient shareFileClient = getClientBuilder(shareName, directoryName).buildFileClient();
		shareFileClient.create(1024);

		Map<String, String> metadata = new HashMap<>();
		metadata.put(MD_KEY, MD_VALUE);
		shareFileClient.setMetadata(metadata);
		return shareFileClient.getProperties().getMetadata();
	}

	@Test
	public void setDirectoryMetadata() throws Exception {
		runTestAndCleanUp(shareName -> {
			Map<String, String> metadata = getMetaDataFromDirectory(shareName, "test");
			assertThat(metadata.size(), is(1));
			assertThat(metadata.get(MD_KEY), is(MD_VALUE));
		});
	}

	@Test
	public void setFileMetadata() throws Exception {
		runTestAndCleanUp(shareName -> {
			Map<String, String> metadata = getMetaDataFromFile(shareName, "test");
			assertThat(metadata.size(), is(1));
			assertThat(metadata.get(MD_KEY), is(MD_VALUE));
		});
	}




}
