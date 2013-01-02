package com.CSGames;

import java.io.IOException;
import java.io.InputStream;

import android.content.res.Resources;

public class Level {

	// =========================================================== 
	// Constants
	// ===========================================================
	
	public static final int TILE_SIZE = 16;
	public static final int TILE_ESPACIO = 1;
	public static final int TILE_SOLIDO = 2;
	public static final int TILE_OUT_OF_BOUNDS = 0;
	public static final int[] TILES = {0, R.drawable.tile_espacio, R.drawable.tile_solido};
	
	// =========================================================== 
	// Fields
	// ===========================================================
	
	private int mSizeX;
	private int mSizeY;
	private int mStartX;
	private int mStartY;

	private int [] mLevel; // Stores the tile codes to print the background
	private int [] mEnemigos; // Stores the tile codes to place the enemies	

	// =========================================================== 
	// Getter & Setter
	// ===========================================================
	
	public int getSizeX () {
		return mSizeX;
	}
	public int getSizeY () {
		return mSizeY;
	}
	public int getStartX(){
		return mStartX;
	}
	public int getStartY() {
		return mStartY;
	}
	
	// =========================================================== 
	// Methods
	// ===========================================================

	public boolean devueveEnemigo (int x, int y){
		if (x >= 0 && x < mSizeX && y >= 0 && y < mSizeY && mEnemigos[x + y*mSizeX] == 3) {
				mEnemigos[x + y*mSizeX] = 0;
				return true;
		} else
			return false;
	}

	/**
	 * 
	 * @param x in tiles
	 * @param y in tiles
	 * @return tile code
	 */
	public int getTile(int x, int y){
		if (x >= 0 && x < mSizeX && y >= 0 & y < mSizeY)
			return mLevel[y*mSizeX + x];
		else 
			return TILE_OUT_OF_BOUNDS; 
	}
	
	/**
	 * 
	 * @param x in pixel
	 * @param y in pixel
	 * @return tile code for a position on the background layer
	 */
	public int getTileAt(float x, float y){
		int xTile = (int)(x/TILE_SIZE);
		int yTile = (int)(y/TILE_SIZE);
		if (xTile >= 0 && xTile < mSizeX && yTile >= 0 & yTile < mSizeY){
			return mLevel[yTile*mSizeX + xTile];
		} else {
			return TILE_OUT_OF_BOUNDS;
		}
	}


	public boolean loadLevel (int pos, Resources res) {
		switch (pos) {

		case 1:
			mSizeX = 620;
			mSizeY = 21;
			mStartX = 15;
			mStartY = 14;
			mLevel = new int[mSizeX*mSizeY];
			
			InputStream levelData = res.openRawResource(R.raw.level1);
			int i = 0;
			int data;
			try {
				do {
					data = levelData.read();
					if (data > 48){
						mLevel[i] = data - 48;
						i++;
					}
				} while (data != -1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

		default:
			return false;
		}
	}
	
	public boolean loadEnemigos (int pos, Resources res) {
		switch (pos) {

		case 1:
			
			mEnemigos = new int[mSizeX*mSizeY];

			InputStream enemiesData = res.openRawResource(R.raw.level1_enemigos);
			int i = 0;
			int data;
			try {
				do {
					data = enemiesData.read();
					if (data >= 48){
						mEnemigos[i] = data - 48;
						i++;
					}
				} while (data != -1);
			} catch (IOException e) {
				e.printStackTrace();
			}	

		default:
			return false;
		}
	}
}
