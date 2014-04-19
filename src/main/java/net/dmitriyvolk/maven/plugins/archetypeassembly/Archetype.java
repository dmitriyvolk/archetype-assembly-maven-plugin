package net.dmitriyvolk.maven.plugins.archetypeassembly;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.codehaus.plexus.util.IOUtil;

public class Archetype {
	private static final String ARCHETYPE_RESOURCES = "archetype-resources/";
	private ModuleDescription md;
	private ArchetypeMetadataXml metadata;
	private File archetypeFile;
	
	public Archetype(ModuleDescription md, Artifact artifact) throws NotAnArchetypeException, ArtifactNotFoundException {
		this.md = md;
		if (artifact == null) {
			throw new ArtifactNotFoundException(md.getArchetype(), artifact);
		}
		load(artifact);
	}

	private void load(Artifact artifact) throws NotAnArchetypeException {
		archetypeFile = artifact.getFile();
		JarFile jar = null;
		try {
			jar = new JarFile(archetypeFile);
			for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
				JarEntry e = entries.nextElement();
				if (e.getName().startsWith("META-INF/maven/archetype-metadata.xml")) {
					metadata = new ArchetypeMetadataXml(jar.getInputStream(e));
					return;
				}
			}
			throw new NotAnArchetypeException(md);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
				}
			}
		}
		
	}
	
	public void copyArchetypeResources(File destinationDirectory) {
		JarFile jar = null; 
		try {
			jar = new JarFile(archetypeFile);
			for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
				JarEntry e = entries.nextElement();
				if (e.getName().startsWith(ARCHETYPE_RESOURCES) && !e.getName().equals(ARCHETYPE_RESOURCES)) {
					String destName = e.getName().substring(ARCHETYPE_RESOURCES.length());
					File destinationFile = new File(destinationDirectory, destName);
					destinationFile.getParentFile().mkdirs();
					IOUtil.copy(jar.getInputStream(e), new FileOutputStream(destinationFile));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (jar != null) {
				try {
					jar.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public ArchetypeMetadataXml getMetadata() {
		return metadata;
	}

}
