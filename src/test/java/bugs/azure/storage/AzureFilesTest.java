package bugs.azure.storage;

import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareClientBuilder;
import com.azure.storage.file.share.ShareFileClient;
import com.azure.storage.file.share.ShareFileClientBuilder;
import com.azure.storage.file.share.models.ShareFileCopyInfo;
import com.azure.storage.file.share.models.ShareFileProperties;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AzureFilesTest {
	private static final String ENDPOINT = "<File Service SAS URL>";
	private static final int FILENAME_LENGTH = 8;
	private static final long SOURCE_FILE_SIZE = 1024L;

	// This is the helper method to generate random name.
	private static String generateRandomName() {
		return UUID.randomUUID().toString().substring(0, FILENAME_LENGTH);
	}

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
		//blows up at this line
		SyncPoller<ShareFileCopyInfo, Void> poller = dstFileClient.beginCopy(sourceURL, null, Duration.ofSeconds(2));
	}

	//This test passes
	@Test
	public void uploadBytes() throws Exception {
		runTestAndCleanUp(this::upload);
	}

	//this test fails on copy
	@Test
	public void copyFileDataInSource() throws Exception {
		runTestAndCleanUp(srcFileClient -> {
			upload(srcFileClient);

			downloadFile(srcFileClient);

			ShareFileClient destFileClient = getClient(srcFileClient.getShareName());
			copySourceToClient(srcFileClient.getFileUrl(), destFileClient);
		});
	}

	//this test fails on copy
	@Test
	public void copyFileDataInDestination() throws Exception {
		runTestAndCleanUp(destFileClient -> {
			byte[] data  = "Hello, file client sample!".getBytes(StandardCharsets.UTF_8);
			destFileClient.upload(new ByteArrayInputStream(data), data.length);

			downloadFile(destFileClient);

			ShareFileClient srcFileClient = getClient(destFileClient.getShareName());
			copySourceToClient(srcFileClient.getFileUrl(), destFileClient);
		});
	}

	//this test fails on copy
	@Test
	public void copyFileAfterUploadingToBoth() throws Exception {
		runTestAndCleanUp(srcFileClient -> {
			//upload to source
			upload(srcFileClient);

			ShareFileClient destFileClient = getClient(srcFileClient.getShareName());
			//upload to destination
			upload(destFileClient);

			downloadFile(srcFileClient);
			downloadFile(destFileClient);

			copySourceToClient(srcFileClient.getFileUrl(), destFileClient);
		});
	}

	private ShareFileClient getClient(String shareName) {
		String fileName = generateRandomName();
		ShareFileClient destFileClient = new ShareFileClientBuilder()
				.endpoint(ENDPOINT)
				.shareName(shareName)
				.resourcePath(fileName).buildFileClient();
		destFileClient.create(SOURCE_FILE_SIZE);
		return destFileClient;
	}

	private void runTestAndCleanUp(ThrowingFunction<ShareFileClient> test) throws Exception {
		String shareName = "deleteme-" + generateRandomName();
		ShareClient shareClient = new ShareClientBuilder()
				.endpoint(ENDPOINT)
				.shareName(shareName).buildClient();
		try {
			shareClient.create();

			// Create a source file client
			ShareFileClient srcFileClient = new ShareFileClientBuilder()
					.endpoint(ENDPOINT)
					.shareName(shareName)
					.resourcePath(generateRandomName()).buildFileClient();

			// Create a source file
			srcFileClient.create(SOURCE_FILE_SIZE);

			test.execute(srcFileClient);

		} finally {
			shareClient.delete();
		}
	}

	@FunctionalInterface
	interface ThrowingFunction<T> {
		void execute(T t) throws Exception;
	}

}
