package com.CSGames.Sprites;

import java.util.Vector;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.CSGames.Position;
import com.CSGames.R;

public class SpriteExplosion extends Sprite {
	
	// =========================================================== 
	// Constants
	// ===========================================================
	
	public final static int EXPLOSION_SMALL = 1;

	// =========================================================== 
	// Fields
	// ===========================================================
	
	Resources mRes;
	double animationFrameTime = 0;
	
	// =========================================================== 
	// Constructors
	// ===========================================================
	
	public SpriteExplosion (float x, float y, Resources res, int tipoExplosion){
		
		mRes = res;

		switch (tipoExplosion){
		case EXPLOSION_SMALL:
			mFrames = new Bitmap[3];
			mFrames[0]=BitmapFactory.decodeResource(res, R.drawable.expl_disp_big);
			mFrames[1]=BitmapFactory.decodeResource(res, R.drawable.expl_disp_med);
			mFrames[2]=BitmapFactory.decodeResource(res, R.drawable.expl_disp_small);
			mWidth = mFrames[0].getWidth();
			mHeight = mFrames[0].getHeight();
			mPos = new Position(x-mWidth/2, y-mHeight/2);
		}
	}
	
	// =========================================================== 
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public void action(Vector<Sprite> sprites, double elapsed) {

		animationFrameTime += elapsed;
		if (animationFrameTime > 0.1){
			mCurrentFrame++;
			animationFrameTime -= 0.1;
			if (mCurrentFrame < mFrames.length){
				mWidth = mFrames[mCurrentFrame].getWidth();
				mHeight = mFrames[mCurrentFrame].getHeight();
			} else {
				mDone = true;		
			}
		}
	}
}
