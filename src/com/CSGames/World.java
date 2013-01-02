package com.CSGames;

public class World {

	// =========================================================== 
	// Constants
	// ===========================================================
	
	public static final int PHYS_DOWN_ACCEL_SEC = 1000;
	
	// =========================================================== 
	// Fields
	// ===========================================================
	
	public Level currentLevel;
	public Position camPos;

	private static World INSTANCE = new World();
	
	// =========================================================== 
	// Constructors
	// ===========================================================
	
	private World() {
		currentLevel = new Level();
	}
	
	// =========================================================== 
	// Methods
	// ===========================================================
	
	public static World getInstance() {
		return INSTANCE;
	}	
}
