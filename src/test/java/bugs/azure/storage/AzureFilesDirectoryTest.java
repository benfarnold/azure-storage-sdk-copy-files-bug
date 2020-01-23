package bugs.azure.storage;

import com.azure.storage.file.share.ShareDirectoryClient;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class AzureFilesDirectoryTest extends SelfCleaningAzureFilesTest {
	private static final String INTEGER_NAME = "81233475";
	private static final String REAL_NAME = "8.1233475";
	private static final String VERSIONED_NAME = "8.12.3.3475";
	private static final String MIXEDCASE_NAME = "SomeName";
	private static final String UNDERLINED_NAME = "some_name";
	private static final String HYPHENED_NAME = "some-name";
	private static final String DATE_NAME = "2020-01-23";
	private static final String INVALID_NAME = "<somename>";

	private void validateStringIsValidURI(String directoryName) throws URISyntaxException {
		String complete = "http://test.com/" + directoryName;
		URI uri = new URI(complete);//throws URISyntaxException if not valid
	}

	private void createDirectory(String shareName, String directoryName) throws URISyntaxException {
		validateStringIsValidURI(directoryName);
		ShareDirectoryClient destFileClient = getClientBuilder(shareName, directoryName).buildDirectoryClient();
		destFileClient.create();
	}

	@Test(expected = URISyntaxException.class)
	public void createDirectoryInvalidName() throws Exception {
		validateStringIsValidURI(INVALID_NAME);
	}

	@Test
	public void createDirectoryIntegerName() throws Exception {
		runTestAndCleanUp(shareName -> createDirectory(shareName, INTEGER_NAME));
	}

	@Test
	public void createDirectoryRealName() throws Exception {
		runTestAndCleanUp(shareName -> createDirectory(shareName, REAL_NAME));
	}

	@Test
	public void createDirectoryVersionedName() throws Exception {
		runTestAndCleanUp(shareName -> createDirectory(shareName, VERSIONED_NAME));
	}

	@Test
	public void createDirectoryMixedCaseName() throws Exception {
		runTestAndCleanUp(shareName -> createDirectory(shareName, MIXEDCASE_NAME));
	}

	@Test
	public void createDirectoryUnderlinedName() throws Exception {
		runTestAndCleanUp(shareName -> createDirectory(shareName, UNDERLINED_NAME));
	}

	@Test
	public void createDirectoryHyphenedName() throws Exception {
		runTestAndCleanUp(shareName -> createDirectory(shareName, HYPHENED_NAME));
	}

	@Test
	public void createDirectoryDateName() throws Exception {
		runTestAndCleanUp(shareName -> createDirectory(shareName, DATE_NAME));
	}
}
