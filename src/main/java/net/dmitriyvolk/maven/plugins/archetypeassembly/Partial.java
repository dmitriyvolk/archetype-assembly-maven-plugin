package net.dmitriyvolk.maven.plugins.archetypeassembly;

public enum Partial {
	YES {
		public boolean asBoolean() {return true ;}
	}, NO {
		public boolean asBoolean() {return false ;}
	};
	
	public abstract boolean asBoolean();
}
