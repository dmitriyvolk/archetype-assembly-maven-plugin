package net.dmitriyvolk.maven.plugins.archetypeassembly;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.PrintStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.NodeList;

public class ArchetypeMetadataXmlTest {
	XPath xpath;
	XPathExpression modules; 

	@Before
	public void setUp() throws Exception {
		xpath = XPathFactory.newInstance().newXPath();
		modules = xpath.compile("/archetype-descriptor/modules/module");
	}
	
	@Test
	public void canInsertModuleIntoEmptyMetadata() throws Exception {
		importModuleOneIntoEmptyMetadata();
	}

	private ArchetypeMetadataXml importModuleOneIntoEmptyMetadata()
			throws XPathExpressionException {
		ArchetypeMetadataXml metadata = new ArchetypeMetadataXml(getClass().getResourceAsStream("/metadata/empty.xml"));
		assertModulesCount(0, metadata);
		assertNodesCount(0, "/archetype-descriptor/fileSets", metadata);
		assertNodesCount(0, "/archetype-descriptor/requiredProperties", metadata);
		metadata.absorbModule(getClass().getResourceAsStream("/metadata/module1.xml"));
		assertModulesCount(1, metadata);
		return metadata;
	}

	@Test
	public void moduleFileSetsGetImported() throws Exception {
		ArchetypeMetadataXml metadata = importModuleOneIntoEmptyMetadata();
		assertNodesCount(1, "/archetype-descriptor/modules/module[@name=\"module1\"]/fileSets/fileSet", metadata);
	}

	private void assertNodesCount(int expected, String xPathExpression,
			ArchetypeMetadataXml metadata) throws XPathExpressionException {
		NodeList module1Fileset = (NodeList) xpath.evaluate(xPathExpression, metadata.getDoc(), XPathConstants.NODESET);
		int actual = module1Fileset.getLength();
		assertEquals(String.format("Expected to find %d nodes, but got %d", expected, actual), expected, actual);
	}
	
	@Test
	public void moduleRequiredPropertiesGetMovedIntoRoot() throws Exception {
		ArchetypeMetadataXml metadata = importModuleOneIntoEmptyMetadata();
		assertNodesCount(0, "/archetype-descriptor/modules/module/requiredProperties", metadata);
		assertNodesCount(1, "/archetype-descriptor/requiredProperties", metadata);
		assertEquals("propertyOneValue", xpath.evaluate("/archetype-descriptor/requiredProperties/requiredProperty[@key=\"propertyOne\"]/defaultValue", metadata.getDoc()));
		assertNodesCount(1, "/archetype-descriptor/requiredProperties/requiredProperty[@key=\"propertyTwo\"]", metadata);
	}

	@Test
	public void requiredPropertiesNotDuplicated() throws Exception {
		ArchetypeMetadataXml metadata = new ArchetypeMetadataXml(getClass().getResourceAsStream("/metadata/nonempty.xml"));
		assertNodesCount(1, "/archetype-descriptor/requiredProperties/requiredProperty", metadata);
		assertNodesCount(1, "/archetype-descriptor/requiredProperties/requiredProperty[@key=\"propertyOne\"]", metadata);
		assertNodesCount(0, "/archetype-descriptor/requiredProperties/requiredProperty/defaultValue", metadata);
		metadata.absorbModule(getClass().getResourceAsStream("/metadata/module1.xml"));
		assertNodesCount(2, "/archetype-descriptor/requiredProperties/requiredProperty", metadata);
		assertNodesCount(1, "/archetype-descriptor/requiredProperties/requiredProperty[@key=\"propertyOne\"]", metadata);
		//even though module1 contains a default value for propertyOne, we drop it since the original (nonempty) has no default value
		assertNodesCount(0, "/archetype-descriptor/requiredProperties/requiredProperty[@key=\"propertyOne\"]/defaultValue", metadata);
		assertNodesCount(1, "/archetype-descriptor/requiredProperties/requiredProperty[@key=\"propertyTwo\"]", metadata);
		assertNodesCount(0, "/archetype-descriptor/requiredProperties/requiredProperty[@key=\"propertyTwo\"]/defaultValue", metadata);
	}
	
	
	@Test
	public void metadataGetsImported() throws Exception {
		ArchetypeMetadataXml metadata = new ArchetypeMetadataXml(getClass().getResourceAsStream("/testarchetype/archetype/src/main/resources/META-INF/maven/archetype-metadata.xml"));
		assertCount(1, (NodeList) xpath.evaluate("/archetype-descriptor/fileSets/fileSet", metadata.getDoc(), XPathConstants.NODESET));
		assertCount(0, (NodeList) xpath.evaluate("/archetype-descriptor/modules/module", metadata.getDoc(), XPathConstants.NODESET));
		ModuleDescription md = new ModuleDescription("group:id", "submoduleid", "submodulename", "subdir");
		metadata.absorbModule(new ArchetypeMetadataXml(getClass().getResourceAsStream("/testarchetype/prebuilt-archetype/src/main/resources/META-INF/maven/archetype-metadata.xml")), md);
//		metadata.prettyPrint(System.out);
		assertCount(1, (NodeList) xpath.evaluate("/archetype-descriptor/fileSets/fileSet", metadata.getDoc(), XPathConstants.NODESET));
		assertCount(1, (NodeList) xpath.evaluate("/archetype-descriptor/modules/module[@id=\"submoduleid\"]/fileSets/fileSet", metadata.getDoc(), XPathConstants.NODESET));
		assertEquals("submodulename", xpath.evaluate("/archetype-descriptor/modules/module[@id=\"submoduleid\"]/@name", metadata.getDoc()));
		assertEquals("subdir", xpath.evaluate("/archetype-descriptor/modules/module[@id=\"submoduleid\"]/@dir", metadata.getDoc()));
	}
	private void assertModulesCount(int expected, ArchetypeMetadataXml metadata)
			throws XPathExpressionException {
		NodeList nl = (NodeList) modules.evaluate(metadata.getDoc(), XPathConstants.NODESET);
		assertEquals(String.format("Expected %d modules but got %d", expected, nl.getLength()), expected, nl.getLength());
	}
	

	private void assertCount(int expected, NodeList nl) {
		int actual = nl == null ? 0 : nl.getLength();
		assertEquals(expected, actual);
	}
}
