package com.CSGames.Sprites;

import java.util.Vector;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.CSGames.Position;
import com.CSGames.R;

public class SpriteEnemigo extends Sprite{
	
	// =========================================================== 
	// Constants
	// ===========================================================
	
	public final static int EVIL_FISH = 1;
	private final double ROTATION_SPEED = Math.PI/128;

	// =========================================================== 
	// Fields
	// ===========================================================
	
	Resources mRes;
	private boolean alive = true;
	private double innerAnimationFrameTime = 0;
	private double outerAnimationFrameTime = 0;
	
	//animation of evil fish
	private double radioRotacion;
	private Position centroRotacion;
	private double rotacionActual = Math.PI;
	
	// =========================================================== 
	// Constructors
	// ===========================================================
	
	public SpriteEnemigo (float x, float y, Resources res, int tipoEnemigo){

		mPos = new Position(x, y);
		mRes = res;

		switch (tipoEnemigo){
		case EVIL_FISH:
			mFrames = new Bitmap[8];
			mFrames[0]=BitmapFactory.decodeResource(res, R.drawable.pez);
			mFrames[1]=BitmapFactory.decodeResource(res, R.drawable.pez_cierra_ojos_1);
			mFrames[2]=BitmapFactory.decodeResource(res, R.drawable.pez_cierra_ojos_2);
			mFrames[3]=BitmapFactory.decodeResource(res, R.drawable.pez_cierra_ojos_3);
			mFrames[4]=BitmapFactory.decodeResource(res, R.drawable.pez_aleteo);
			mFrames[5]=BitmapFactory.decodeResource(res, R.drawable.pez_desaparezco_1);
			mFrames[6]=BitmapFactory.decodeResource(res, R.drawable.pez_desaparezco_2);
			mFrames[7]=BitmapFactory.decodeResource(res, R.drawable.pez_desaparezco_3);

			mWidth = mFrames[0].getWidth();
			mHeight = mFrames[0].getHeight();
			
			radioRotacion = Math.random()*50+30;
			centroRotacion = new Position(x + (float)radioRotacion, y);
		}
	}

	// =========================================================== 
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void action(Vector<Sprite> sprites, double elapsed) {	
		
		//Internal Animation
		innerAnimationFrameTime += elapsed;
		if (alive) {
			while (innerAnimationFrameTime > 0.1){
				mCurrentFrame++;
				innerAnimationFrameTime -= 0.1;
				if (mCurrentFrame == 5){
					mCurrentFrame = 0;
				}
			}
		} else {
			while (innerAnimationFrameTime > 0.05){
				mCurrentFrame++;
				innerAnimationFrameTime -= 0.05;
				if (mCurrentFrame == mFrames.length){
					mDone = true;
				}
			}
		}
		
		//External animation
		outerAnimationFrameTime += elapsed;
		if (alive){
			while (outerAnimationFrameTime > 0.016){
				rotacionActual += ROTATION_SPEED;
				if (rotacionActual > 2*Math.PI)
					rotacionActual -= 2*Math.PI;
				this.getPos().setX(centroRotacion.getX()+(float)(radioRotacion*Math.cos(rotacionActual)));
				this.getPos().setY(centroRotacion.getY()+(float)(radioRotacion*Math.sin(rotacionActual)));
				outerAnimationFrameTime -= 0.016;
			}
		}
		
		
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		// OUT OF BOUNDS	
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////

		if ((sprites.elementAt(0).getPos().getX()) - this.getPos().getX() > 300)
			mDone = true;
	}
	
	// =========================================================== 
	// Methods
	// ===========================================================
	
	public void killed(){
		alive = false;
		mCurrentFrame = 5;
	}

}
