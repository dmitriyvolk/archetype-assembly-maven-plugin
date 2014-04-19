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
		ArchetypeMetadataXml metadata = new ArchetypeMetadataXml(getClass().getResourceAsStream("/metadata/empty.xml"));
		assertModulesCount(0, metadata);
		metadata.absorbModule(getClass().getResourceAsStream("/metadata/module1.xml"));
		assertModulesCount(1, metadata);
	}

	@Test
	public void moduleChildrenGetImported() throws Exception {
		ArchetypeMetadataXml metadata = new ArchetypeMetadataXml(getClass().getResourceAsStream("/metadata/empty.xml"));
		assertModulesCount(0, metadata);
		metadata.absorbModule(getClass().getResourceAsStream("/metadata/module1.xml"));
		assertModulesCount(1, metadata);
		NodeList module1Fileset = (NodeList) xpath.evaluate("/archetype-descriptor/modules/module[@name=\"module1\"]/fileSets/fileSet", metadata.getDoc(), XPathConstants.NODESET);
		int expected = 1;
		int actual = module1Fileset.getLength();
		assertEquals(String.format("Expected to find %d fileSet tags, but got %d", expected, actual), expected, actual);
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
