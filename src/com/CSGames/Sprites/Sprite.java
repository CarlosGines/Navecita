package com.CSGames.Sprites;

import java.util.Vector;

import android.graphics.Bitmap;

import com.CSGames.Level;
import com.CSGames.NavecitaView;
import com.CSGames.Position;

public abstract class Sprite {

	// =========================================================== 
	// Constants
	// ===========================================================
	
	final int TILE_SIZE = NavecitaView.TILE_SIZE;
	
	// =========================================================== 
	// Fields
	// ===========================================================
	
	/** Top left point of the prite */
	Position mPos;
	
	int mWidth;
	int mHeight;
	
	Bitmap[] mFrames;
	int mCurrentFrame = 0;
	
	boolean mDone = false;
	
	// =========================================================== 
	// Constructors
	// ===========================================================
	
	// =========================================================== 
	// Getter & Setter
	// ===========================================================
	
	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public Position getPos() {
		return mPos;
	}
	
	// =========================================================== 
	// Methods
	// ===========================================================

	/** Get current frame to draw according to internal animation*/
	public Bitmap getFrame() {
		return mFrames[mCurrentFrame];
	}
	
	/**
	 * Tells us whether this Sprite can stop being processed
	 */
	public boolean isDone() {
		return mDone;
	}
	
	/**
	 * Tells us whether there is collision between this Sprite and another one.
	 * It takes into account the whole sprite for the collision (also transparencies).
	 * @param s The other sprite to check the collision.
	 * @param xOffset can be used to figure out if there would be collision with this sprite 
	 * moved this amount of pixels in the X axis. 0 for original position.
	 * @param yOffset can be used to figure out if there would be collision with this sprite 
	 * moved this amount of pixels in the Y axis. 0 for original position.
	 * @return whether there is collision 
	 */
	boolean collision(Sprite s, float xOffset, float yOffset){
		
		float w1,h1,w2,h2,x1,y1,x2,y2;
		w1 = this.getWidth(); // width sprite1
		h1 = this.getHeight(); // height sprite1
		w2 = s.getWidth(); // width sprite2
		h2 = s.getHeight(); // height sprite2
		x1 = this.getPos().getX() + xOffset; // pos. X from sprite1
		y1 = this.getPos().getY() + yOffset; // pos. Y from sprite1
		x2 = s.getPos().getX(); // pos. X from sprite2
		y2 = s.getPos().getY(); // pos. Y from sprite2
		
		if (((x1+w1)>x2) && ((y1+h1)>y2) && ((x2+w2)>x1) && ((y2+h2)>y1)) {
			return true;
		} else {
			return false;
		}
	}
	
	//Following methods tell us whether there is a collision 
	//between this Sprite and the backstage (tilemap)
	
	boolean collisionRight (Level level, float offset){
		
		float w,h,x,y;
		w = this.getWidth(); // width sprite1
		h = this.getHeight(); // height sprite1
		x = this.getPos().getX() + offset; // pos. X from sprite1
		y = this.getPos().getY(); // pos. Y from sprite1
		
		for (int yCurrent = (int)y; yCurrent < y+h; yCurrent++){
			if ( level.getTileAt(x+w-1, yCurrent) == Level.TILE_SOLIDO){
				return true;
			}
		}
		return false;
	}
	
	boolean collisionBottom (Level level, float offset){
		
		float w,h,x,y;
		w = this.getWidth(); // width sprite1
		h = this.getHeight(); // height sprite1
		x = this.getPos().getX(); // pos. X from sprite1
		y = this.getPos().getY() + offset; // pos. Y from sprite1
		
		for (int xCurrent = (int)x; xCurrent < x+w; xCurrent++){
			if (level.getTileAt(xCurrent, y+h-1) == Level.TILE_SOLIDO){
				return true;
			}
		}
		return false;
	}
	
	boolean collisionLeft (Level level, float offset){
		
		float h,x,y;
		h = this.getHeight(); // altura del sprite1
		x = this.getPos().getX() + offset; // pos. X del sprite1
		y = this.getPos().getY(); // pos. Y del sprite1
		
		for (int yCurrent = (int)y; yCurrent < y+h; yCurrent++){
			if ( level.getTileAt(x, yCurrent) == Level.TILE_SOLIDO){
				return true;
			}
		}
		return false;
	}
	
	boolean collisionTop (Level level, float offset){
		
		float w,x,y;
		w = this.getWidth(); // ancho del sprite1
		x = this.getPos().getX(); // pos. X del sprite1
		y = this.getPos().getY() + offset; // pos. Y del sprite1
		
		for (int xCurrent = (int)x; xCurrent < x+w; xCurrent++){
			if ( level.getTileAt(xCurrent, y) == Level.TILE_SOLIDO){
				return true;
			}
		}
		return false;

	}

	/**
	 * 
	 * @param sprites Vector with the current Sprites being processed
	 * @param elapsed time elapsed since last loop.
	 */
	public abstract void action(Vector<Sprite> sprites, double elapsed);
}
