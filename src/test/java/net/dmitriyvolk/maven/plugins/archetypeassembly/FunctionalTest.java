package net.dmitriyvolk.maven.plugins.archetypeassembly;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

public class FunctionalTest {

	File testDir;
	Verifier verifier;
	
//	@Before
	public void setUp() throws Exception {
		testDir = ResourceExtractor.simpleExtractResources(getClass(), "/testarchetype");
		verifier = new Verifier(testDir.getAbsolutePath());
		verifier.deleteArtifact("net.dmitriyvolk.testing.testarchetype", "parent", "1.0", "pom");
		verifier.deleteArtifact("net.dmitriyvolk.testing.testarchetype", "prebuilt-archetype", "1.0", "jar");
		verifier.deleteArtifact("net.dmitriyvolk.testing.testarchetype", "archetype", "1.0", "jar");
	}
	
//	@Test
	public void test() throws Exception {
		install();
		ArchetypeMetadataXml metadata = readMetadata();
		metadata.prettyPrint(System.out);
	}

	private ArchetypeMetadataXml readMetadata() throws FileNotFoundException {
		return new ArchetypeMetadataXml(new File(testDir, "archetype/target/classes/META-INF/maven/archetype-metadata.xml"));
	}

	private void install() throws VerificationException {
		verifier.executeGoal("install");
		verifier.verifyErrorFreeLog();
		verifier.resetStreams();
	}


}
