package io.github.togar2.blocks;

public enum ToolTier {
	WOOD(0, 2.0),
	STONE(1, 4.0),
	IRON(2, 6.0),
	DIAMOND(3, 8.0),
	GOLD(0, 12.0),
	NETHERITE(4, 9.0);
	
	private final int miningLevel;
	private final double miningSpeed;
	
	ToolTier(int miningLevel, double miningSpeed) {
		this.miningLevel = miningLevel;
		this.miningSpeed = miningSpeed;
	}
	
	public int getMiningLevel() {
		return miningLevel;
	}
	
	public double getMiningSpeed() {
		return miningSpeed;
	}
}
