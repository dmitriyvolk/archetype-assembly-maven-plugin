package net.dmitriyvolk.maven.plugins.archetypeassembly;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(name = "add-modules", requiresDependencyResolution = ResolutionScope.COMPILE)
public class AddModulesMojo extends AbstractMojo {

	@Component
	MavenProject project;

	@Parameter
	List<ModuleDescription> additionalModules;

	ArchetypeMetadataXml metadata;

	public void execute() throws MojoExecutionException, MojoFailureException {
		File archetypeMetadataFile = getArchetypeMetadataFile();
		// Current implementation assumes that it's already in target/classes,
		// so we'll fail if that's not the case
		assert archetypeMetadataFile.exists();
		try {
			metadata = new ArchetypeMetadataXml(archetypeMetadataFile);
			@SuppressWarnings("unchecked")
			Set<Artifact> dependencies = project.getDependencyArtifacts();
			for (ModuleDescription md : additionalModules) {
				handleModule(md, findArtifact(md, dependencies));
			}
			writeMetadata();
		} catch (Exception e) {
			throw new MojoExecutionException("Error: ", e);
		}
	}

	private File getArchetypeMetadataFile() {
		File archetypeMetadataFile = new File(project.getBuild()
				.getOutputDirectory(), "META-INF/maven/archetype-metadata.xml");
		return archetypeMetadataFile;
	}

	private void writeMetadata() throws Exception {
		metadata.prettyPrint(new PrintStream(getArchetypeMetadataFile()));
	}

	private Artifact findArtifact(ModuleDescription md,
			Set<Artifact> dependencies) {
		getLog().debug(String.format("looking for %s", md.getArchetype()));
		for (Artifact a : dependencies) {
			String shortCoordinates = String.format("%s:%s", a.getGroupId(),
					a.getArtifactId());
			getLog().debug("checking " + shortCoordinates);
			if (shortCoordinates.equals(md.getArchetype())) {
				getLog().debug(String.format("Yay, found it. Is it resolved? %s What's its type? %s", a.isResolved(), a.getType()));
				if (a.isResolved() && a.getType().equals("jar")) {
					return a;
				}
			}
		}
		getLog().debug("Nope");
		return null;
	}

	private void handleModule(ModuleDescription md, Artifact artifact) {
		try {
			getLog().info(
					String.format("Adding module %s from %s", md.getName(),
							md.getArchetype()));
			Archetype a = new Archetype(md, artifact);
			metadata.absorbModule(a.getMetadata(), md);
			File moduleDirectory = makeModuleDir(md);
			a.copyArchetypeResources(moduleDirectory);
		} catch (NotAnArchetypeException e) {
			getLog().warn(String.format("Unfortunately, %s doesn't seem to be an archetype", artifact.getFile().getName()));
		} catch (ArtifactNotFoundException e) {
			throw new RuntimeException(String.format(
					"Can't find artifact for %s", md.getArchetype()));
		}
	}

	private File makeModuleDir(ModuleDescription md) {
		File moduleDirectory = new File(project.getBuild().getOutputDirectory(), String.format("archetype-resources/%s", md.getDir()));
		moduleDirectory.mkdir();
		return moduleDirectory;
	}

}
