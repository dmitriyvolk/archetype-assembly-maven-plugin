package net.dmitriyvolk.maven.plugins.archetypeassembly;

public class ModuleDescription {
	public ModuleDescription(String archetype, String id, String name,
			String dir) {
		super();
		this.archetype = archetype;
		this.id = id;
		this.name = name;
		this.dir = dir;
	}
	public ModuleDescription() {
		super();
	}
	private String archetype;
	private String id;
	private String name;
	private String dir;
	public String getArchetype() {
		return archetype;
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getDir() {
		return dir;
	}
}
