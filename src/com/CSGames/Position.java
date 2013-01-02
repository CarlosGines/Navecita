package com.CSGames;

public class Position {

	// =========================================================== 
	// Constants
	// ===========================================================
	
	private static final int TILE_SIZE = NavecitaView.TILE_SIZE;
	
	// =========================================================== 
	// Fields
	// ===========================================================
	
	private float x;
	private float y;
	
	// =========================================================== 
	// Constructors
	// ===========================================================

	public Position (float x, float y){
		this.x = x;
		this.y = y;
	}
	
	// =========================================================== 
	// Getter & Setter
	// ===========================================================
	
	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	public void setY(float y) {
		this.y = y;
	}
	
	// =========================================================== 
	// Methods
	// ===========================================================

	public int getXTile(){
		return (int)(x / TILE_SIZE);
	}
	public int getYTile(){
		return (int)(y / TILE_SIZE);
	}
	
	
	public float getXTileOffset(){
		return x % TILE_SIZE;
	}
	public float getYTileOffset(){
		return y % TILE_SIZE;
	}

	
	public void setXTile (float xTile){
		x = xTile * TILE_SIZE;
	}
	public void setYTile (float yTile){
		y = yTile * TILE_SIZE;
	}
	
	
	public void setXTileOffset (float offset){
		x = x - (x % TILE_SIZE) + offset;
	}
	public void setYTileOffset (float offset){
		y = y - (y % TILE_SIZE) + offset;
	}
	
	
	public void moveX (float px){
		x += px;
	}
	public void moveY (float px){
		y += px;
	}
	
	
	public void moveXTile (float tiles){
		x += tiles * TILE_SIZE;
	}
	public void moveYTile (float tiles){
		y += tiles * TILE_SIZE;
	}
}

