package net.dmitriyvolk.maven.plugins.archetypeassembly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codehaus.plexus.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ArchetypeMetadataXml {

	private static final String ARCHETYPE_DESCRIPTOR = "archetype-descriptor";
	private static final String MODULE = "module";
	private Document doc;
	private DocumentBuilder db;
	
	public ArchetypeMetadataXml(InputStream input) {
		try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = db.parse(input);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ArchetypeMetadataXml(File f) throws FileNotFoundException {
		this(new FileInputStream(f));
	}

	public ArchetypeMetadataXml absorbModule(File file) {
		try {
			return absorbModule(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	public ArchetypeMetadataXml absorbModule(InputStream input) {
		return absorbModule(input, null);
	}
	
	public ArchetypeMetadataXml absorbModule(InputStream input, ModuleDescription md) {
		try {
			return absorbModule(db.parse(input), md);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public ArchetypeMetadataXml absorbModule(Document childModuleMetadata) {
		return absorbModule(childModuleMetadata, null);
	}
	
	public ArchetypeMetadataXml absorbModule(Document childModuleMetadata, ModuleDescription md) {
		return absorbModule(childModuleMetadata.getDocumentElement(), md);
	}
	
	public ArchetypeMetadataXml absorbModule(ArchetypeMetadataXml metadata) {
		return absorbModule(metadata, null);
	}
	public ArchetypeMetadataXml absorbModule(ArchetypeMetadataXml metadata, ModuleDescription md) {
		return absorbModule(metadata.getDoc(), md);
	}

	public ArchetypeMetadataXml absorbModule(Element childModuleMetadata) {
		return absorbModule(childModuleMetadata, null);
	}
	public ArchetypeMetadataXml absorbModule(Element childModuleMetadata, ModuleDescription md) {
		String childMetadataTagName = childModuleMetadata.getTagName();
		if (!childMetadataTagName.equals(ARCHETYPE_DESCRIPTOR)) 
			throw new RuntimeException(String.format("Unknown element %s when expecting %s", childMetadataTagName, ARCHETYPE_DESCRIPTOR));
		Element module = absorb(childModuleMetadata, md);
		Node modules;
		modules = getOrMakeTopLevelNode("modules");
		modules.appendChild(module);
		return this;
	}

	private Node getOrMakeTopLevelNode(String tagName) {
		Node node;
		NodeList nl = doc.getDocumentElement().getElementsByTagName(tagName);
		if (nl == null || nl.getLength() == 0) {
			node = doc.createElement(tagName);
			doc.getDocumentElement().appendChild(node);
		} else {
			node = nl.item(0);
		}
		return node;
	}

	private Element absorb(Element childModuleMetadata, ModuleDescription md) {
		Element module = doc.createElement(MODULE);
		if (md == null) {
			String moduleName = childModuleMetadata.getAttribute("name");
			module.setAttribute("name", moduleName);
			module.setAttribute("id", moduleName);
			module.setAttribute("dir", moduleName);
		} else {
			module.setAttribute("id", md.getId());
			module.setAttribute("name", md.getName());
			module.setAttribute("dir", md.getDir());
		}
		NodeList children = childModuleMetadata.getChildNodes(); 
		for (int i = 0; i < children.getLength(); i++) {
			absorbChild(module, children.item(i));
		}
		return module;
	}

	private void absorbChild(Element module, Node newChild) {
		if (newChild.getNodeName().equals("requiredProperties")) {
			absorbProperties(newChild);
		} else {
			Node imported = doc.importNode(newChild, true);
			module.appendChild(imported);
		}
	}
	
	private void absorbProperties(Node moduleRequiredPropertiesNode) {
		Node requiredProperties = getOrMakeTopLevelNode("requiredProperties");
		NodeList existingProperties = requiredProperties.getChildNodes();
		NodeList moduleProperties = moduleRequiredPropertiesNode.getChildNodes();
		for (int i = 0; i < moduleProperties.getLength(); i++) {
			Node newProperty = moduleProperties.item(i);
			if (!alreadyExists(newProperty, existingProperties)) {
				Node imported = doc.importNode(newProperty, true);
				requiredProperties.appendChild(imported);
			}
		}
	}

	private boolean alreadyExists(Node newProperty, NodeList existingProperties) {
		for (int i = 0; i < existingProperties.getLength(); i++) {
			Node existingProperty = existingProperties.item(i);
			if (StringUtils.equals(getKeyAttr(existingProperty), getKeyAttr(newProperty))) {
				return true;
			}
		}
		return false;

	}
	
	private String getKeyAttr(Node node) {
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null) {
			Node keyAttr = attrs.getNamedItem("key");
			if (keyAttr != null) {
				return keyAttr.getNodeValue();
			}
		}
		return null;
	}
	/**
	 * Package-visible method used for testing
	 * @return the DOM document
	 */
	Document getDoc() {
		return doc;
	}
	
	public void prettyPrint(PrintStream out) throws Exception {
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(out));
	}
	
}
