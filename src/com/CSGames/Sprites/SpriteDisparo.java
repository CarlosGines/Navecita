package com.CSGames.Sprites;

import java.util.Vector;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.CSGames.Level;
import com.CSGames.Position;
import com.CSGames.R;
import com.CSGames.World;


public class SpriteDisparo extends Sprite {

	// =========================================================== 
	// Constants
	// ===========================================================
	
	private final static int SPEED = 700;
	
	// =========================================================== 
	// Fields
	// ===========================================================
	
	private double mCosDir;
	private double mSenDir;
	private Resources mRes;
	
	// =========================================================== 
	// Constructors
	// ===========================================================
	
	public SpriteDisparo (float x, float y, Resources res, Position goal){

		this.mFrames = new Bitmap[1];
		mRes = res;
		
		Position camPos = World.getInstance().camPos;
		float dX = camPos.getX() + goal.getX() - x;
		float dY = camPos.getY() + goal.getY() - y;
		double hipotenusa = Math.sqrt(dX*dX+dY*dY);
		mCosDir = dX/hipotenusa;
		mSenDir = dY/hipotenusa;
		
		this.mFrames[0]=BitmapFactory.decodeResource(res, R.drawable.disparo_dcha);
		this.mWidth = mFrames[0].getWidth();
		this.mHeight = mFrames[0].getHeight();
		
		mPos = new Position(x-mWidth/2, y-mHeight/2);
	}

	// =========================================================== 
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void action(Vector<Sprite> sprites, double elapsed) {

		Level level = World.getInstance().currentLevel;
		
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		// MOVE BULLET	
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////

		int xMove = (int)(mCosDir*elapsed*SPEED);
		int yMove = (int)(mSenDir*elapsed*SPEED);


		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		// DETECT COLLISIONS	
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////


		//collision right
		boolean collision = false;
		
		if (!collision && xMove > 0){
			for (int i = 1; i <= xMove; i++){
				for (int posSprite = 1; posSprite < sprites.size(); posSprite++ ){
					Sprite s = sprites.elementAt(posSprite);
					if (s instanceof SpriteEnemigo && this.collision(s, i, 0)){
						xMove = i - 1;
						((SpriteEnemigo)s).killed();
						collision = true;
						break;
					}
				}
				if (!collision && collisionRight(level, i)) {
					xMove = i - 1;
					collision = true;
					break;
				}
			}
		}

		this.getPos().moveX(xMove);

		
		// Collision ground
		if (!collision && yMove > 0){
			for (int i = 1; i <= yMove; i++){
				for (int posSprite = 1; posSprite < sprites.size(); posSprite++ ){
					Sprite s = sprites.elementAt(posSprite);
					if (s instanceof SpriteEnemigo && this.collision(s, 0, i)){
						yMove = i - 1;
						((SpriteEnemigo)s).killed();
						collision = true;
						break;
					}
				}
				if (!collision && collisionBottom(level, i)) {
					yMove = i - 1;
					collision = true;
					break;
				}
			}

		//collision top
		} else if (!collision && yMove < 0){
			for (int i = -1; i >= yMove; i--){
				for (int posSprite = 1; posSprite < sprites.size(); posSprite++ ){
					Sprite s = sprites.elementAt(posSprite);
					if (s instanceof SpriteEnemigo && this.collision(s, 0, i)){
						yMove = i + 1;
						((SpriteEnemigo)s).killed();
						collision = true;
						break;
					}
				}
				if (collisionTop(level, i)) {
					yMove = i + 1;
					collision = true;
					break;
				}
			}
		}
		
		this.getPos().moveY(yMove);
		
		
		//TODO coordinates for the new explosion
		if (collision == true){
			mDone = true;
			sprites.add(new SpriteExplosion(getPos().getX() + getWidth(), 
					getPos().getY() + getHeight()/2, mRes, SpriteExplosion.EXPLOSION_SMALL));
		}
		
		
		
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		// OUT OF BOUNDS	
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////

		if ((this.getPos().getX() - sprites.elementAt(0).getPos().getX()) > 450)
			mDone = true;
	}



}
