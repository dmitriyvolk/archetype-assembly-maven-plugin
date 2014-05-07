import javax.xml.xpath.*
import javax.xml.parsers.DocumentBuilderFactory
import java.io.*

def verifyModulesInArchetypeMetadata() {
	File metadataFile = new File(basedir, "archetype/target/classes/META-INF/maven/archetype-metadata.xml")

	def doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(metadataFile)
	
	def xpath = XPathFactory.newInstance().newXPath()
	
	def moduleNodes = xpath.evaluate("/archetype-descriptor/modules/module", doc, XPathConstants.NODESET)
	assert moduleNodes.length==1, "There must be only one module"
}

def verifyArtifactResources() {
	File fromPrebuiltArchetype = new File(basedir, "archetype/target/classes/archetype-resources/loaded-module/src/main/resources/from-prebuilt-archetype.txt")
	assert fromPrebuiltArchetype.isFile(), "Expected to find from-prebuilt-archetype.txt in archetype's resources"
}

verifyModulesInArchetypeMetadata()