package bugs.azure.storage;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareClientBuilder;
import com.azure.storage.file.share.ShareFileClientBuilder;

import java.util.UUID;

public class SelfCleaningAzureFilesTest {
	protected static final String ENDPOINT = "https://<ACCOUNT_NAME>.file.core.windows.net";
	protected static final String SAS_TOKEN = "<SAS_TOKEN>";
	private static final int FILENAME_LENGTH = 8;

	//utility methods
	protected static String generateRandomName() {
		return UUID.randomUUID().toString().substring(0, FILENAME_LENGTH);
	}

	protected ShareFileClientBuilder getClientBuilder(String shareName, String resourcePath) {
		return new ShareFileClientBuilder()
				.endpoint(ENDPOINT)
				.sasToken(SAS_TOKEN)
				.shareName(shareName)
				.resourcePath(resourcePath);
	}

	//self-cleaning bit
	protected void runTestAndCleanUp(ThrowingFunction<String> test) throws Exception {
		String shareName = "deleteme-" + AzureFilesTest.generateRandomName();
		ShareClient shareClient = new ShareClientBuilder()
				.endpoint(ENDPOINT)
				.sasToken(SAS_TOKEN)
				.shareName(shareName).buildClient();
		try {
			shareClient.create();

			test.execute(shareName);

		} finally {
			shareClient.delete();
		}
	}

	@FunctionalInterface
	interface ThrowingFunction<T> {
		void execute(T t) throws Exception;
	}
}
