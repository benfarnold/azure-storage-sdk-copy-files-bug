package bugs.azure.storage;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.ShareClientBuilder;
import com.azure.storage.file.share.ShareDirectoryClient;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.ShareFileItem;
import org.junit.Test;

import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AzureFilesHiddenDirectoryTest extends SelfCleaningAzureFilesTest {
	private static final String MIXEDCASE_NAME = "SomeName";

	public ShareDirectoryClient getRootDirectoryClient(String shareName) {
		return new ShareClientBuilder()
				.endpoint(ENDPOINT)
				.sasToken(SAS_TOKEN)
				.shareName(shareName).buildClient().getRootDirectoryClient();
	}

	private void createDirectory(String shareName, String directoryName) {
		ShareDirectoryClient destFileClient = getClientBuilder(shareName, directoryName).buildDirectoryClient();
		destFileClient.create();
	}

	private void createHiddenDirectory(String shareName, String directoryName) {
		ShareDirectoryClient destFileClient = getClientBuilder(shareName, directoryName).buildDirectoryClient();
		EnumSet attributes = EnumSet.of(NtfsFileAttributes.HIDDEN, NtfsFileAttributes.DIRECTORY);
		FileSmbProperties smbProperties = new FileSmbProperties();
		smbProperties.setNtfsFileAttributes(attributes);
		destFileClient.createWithResponse(smbProperties, null, null, null, Context.NONE);
	}

	private PagedIterable<ShareFileItem> getRepositoryChildren(String shareName) {
		ShareDirectoryClient client = getRootDirectoryClient(shareName);
		return client.listFilesAndDirectories();
	}

	@Test
	public void createNormalDirectory() throws Exception {
		runTestAndCleanUp(shareName -> {
			createDirectory(shareName, MIXEDCASE_NAME);
			PagedIterable<ShareFileItem> items = getRepositoryChildren(shareName);
			int size = 0;
			for (ShareFileItem item: items) {
				size++;
			}
			assertThat(size, is(1));
		});
	}

	@Test
	public void createHiddenDirectory() throws Exception {
		runTestAndCleanUp(shareName -> {
			createHiddenDirectory(shareName, MIXEDCASE_NAME);
			PagedIterable<ShareFileItem> items = getRepositoryChildren(shareName);
			int size = 0;
			for (ShareFileItem item: items) {
				size++;
			}
			assertThat(size, is(1));
		});
	}
}
