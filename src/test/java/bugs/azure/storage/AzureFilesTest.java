package bugs.azure.storage;

import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileProperties;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AzureFilesTest extends SelfCleaningAzureFilesTest {
	private static final long SOURCE_FILE_SIZE = 1024L;

	private void upload(ShareFileClient uploadClient) {
		byte[] data  = "Hello, file client sample!".getBytes(StandardCharsets.UTF_8);
		uploadClient.upload(new ByteArrayInputStream(data), data.length);
	}

	private void downloadFile(ShareFileClient downloadFileClient) {
		//if this works then files definitely exists
		ShareFileProperties sourceProperties = downloadFileClient.downloadToFile(generateRandomName());
		assertThat(sourceProperties.getContentLength(), is(SOURCE_FILE_SIZE));
	}

	private void copySourceToClient(String sourceURL, ShareFileClient dstFileClient) {
		// Copy the file from source file to destination file.
		//append SAS token to source URL to avoid a 404
		SyncPoller<ShareFileCopyInfo, Void> poller = dstFileClient.beginCopy(sourceURL + SAS_TOKEN, null, Duration.ofSeconds(2));
	}

	@Test
	public void uploadBytes() throws Exception {
		runTest(this::upload);
	}

	@Test
	public void copyFileDataInSource() throws Exception {
		runTest(srcFileClient -> {
			upload(srcFileClient);

			downloadFile(srcFileClient);

			ShareFileClient destFileClient = getClient(srcFileClient.getShareName());
			copySourceToClient(srcFileClient.getFileUrl(), destFileClient);//getFileUrl does not include the SAS token
		});
	}

	private ShareFileClient getClient(String shareName) {
		String fileName = generateRandomName();
		ShareFileClient destFileClient = new ShareFileClientBuilder()
				.endpoint(ENDPOINT)
				.sasToken(SAS_TOKEN)
				.shareName(shareName)
				.resourcePath(fileName).buildFileClient();
		destFileClient.create(SOURCE_FILE_SIZE);
		return destFileClient;
	}

	private void runTest(ThrowingFunction<ShareFileClient> test) throws Exception {
		runTestAndCleanUp((String shareName) -> {
			// Create a source file client
			ShareFileClient srcFileClient = new ShareFileClientBuilder()
					.endpoint(ENDPOINT)
					.sasToken(SAS_TOKEN)
					.shareName(shareName)
					.resourcePath(generateRandomName()).buildFileClient();

			// Create a source file
			srcFileClient.create(SOURCE_FILE_SIZE);

			test.execute(srcFileClient);
		});
	}

}
