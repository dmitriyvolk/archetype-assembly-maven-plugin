package net.dmitriyvolk.maven.plugins.archetypeassembly;

public class NotAnArchetypeException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotAnArchetypeException(ModuleDescription md) {
		super(String.format("%s doesn't seem to be an archetype", md.getArchetype()));
	}


}
