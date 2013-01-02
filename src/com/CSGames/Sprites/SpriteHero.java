package com.CSGames.Sprites;

import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;

import com.CSGames.GameInputs;
import com.CSGames.Level;
import com.CSGames.Position;
import com.CSGames.R;
import com.CSGames.World;

public class SpriteHero extends Sprite{

	// =========================================================== 
	// Constants
	// ===========================================================
	
	
	public static final int PHYS_DOWN_ACCEL_SEC = World.PHYS_DOWN_ACCEL_SEC;
    public static final int PHYS_JUMP_ACCEL_SEC = 500;
    public static final int PHYS_MOV_ACC_SEC = 900; 
    public static final int PHYS_MOV_MAX = 200; 
    
	// =========================================================== 
	// Fields
	// ===========================================================
	
    private SoundPool mSoundPool;
    
    Resources mRes;
    Context mContext;
    private int mIdShotSound;
    GameInputs mInputs;
    private float mDX;
    private float mDY;
    private int mHeading = 1;
    //private boolean motionBreak;
    public boolean alive = true;
    
	// =========================================================== 
	// Constructors
	// ===========================================================
	
	public SpriteHero (float x, float y, Context context, Handler handler){
		
		this.mPos = new Position(x, y);
		this.mFrames = new Bitmap[4];
		this.mRes = context.getResources();
		this.mContext = context;
		
		this.mFrames[0]=BitmapFactory.decodeResource(mRes, R.drawable.lander_plain_dcha);
		this.mFrames[1]=BitmapFactory.decodeResource(mRes, R.drawable.lander_firing_dcha);
		this.mFrames[2]=BitmapFactory.decodeResource(mRes, R.drawable.lander_plain_izda);
		this.mFrames[3]=BitmapFactory.decodeResource(mRes, R.drawable.lander_firing_izda);
		this.mWidth = mFrames[0].getWidth();
		this.mHeight = mFrames[0].getHeight();
		this.mInputs = GameInputs.getInstance();
	    this.mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
	    this.mIdShotSound = mSoundPool.load(mContext, R.raw.shot, 1);
	}

	// =========================================================== 
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
	@Override
	public void action (Vector<Sprite> sprites, double elapsed) {

		Level level = World.getInstance().currentLevel;
		
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		// MOVE HERO	
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////

		double ddx = 0.0;
		double ddy = 0.0;
		boolean landed = collisionBottom(level, 1);

		
		//TODO
		
		//commented code here beacuse previously ship moved in every direction
		//then I decided to make advance for itself
        
//		//cambio direccion a la dcha
//		if (mInputs.punteroIDcha || mInputs.teclaDcha) {
//			mHeading = 1;
//			this.mCurrentFrame = 0;
//			motionBreak = true;
//		}	
//		//cambio direcci—n a la izda
//		if (mInputs.punteroIIzda || mInputs.teclaIzda) {
//			mHeading = -1; 
//			this.mCurrentFrame = 2;
//			motionBreak = true;
//		} 

		ddx = PHYS_MOV_ACC_SEC * elapsed;
		
		//on land
		if (landed) {			
			//motionBreak = false;

			//Jump
			if (mInputs.punteroIArr || mInputs.teclaArr) {          		
				ddy = -PHYS_JUMP_ACCEL_SEC;
				if (mDX < 100 && mDX > -100) 
					mDX = 0;
			} //else 
//				//avance
//				if (mInputs.punteroI || mInputs.teclaDcha || mInputs.teclaIzda) { 
//					ddx = mHeading * PHYS_MOV_ACC_SEC * elapsed;
//					mCurrentFrame = 2 - mHeading;
//				} else {
//					mCurrentFrame = 1 - mHeading;
//					if (mDX != 0) //frenada
//						ddx = -Math.signum(mDX) * PHYS_MOV_ACC_SEC * elapsed;
//				}


			//On air	
		} else {
//			//avance si no se cambia la direcci—n, se avanza con la inercia inicial
//			if (motionBreak){        		 
//				if (mInputs.punteroI || mInputs.teclaDcha || mInputs.teclaIzda) { 
//					ddx = mHeading * PHYS_MOV_ACC_SEC * elapsed;
//					mCurrentFrame = 2 - mHeading;
//				} else {
//					mCurrentFrame = 1 - mHeading;
//					if (mDX != 0) //frenada
//					ddx = -Math.signum(mDX) * PHYS_MOV_ACC_SEC * elapsed;
//				}
//			}
			//caida por gravedad
			ddy = PHYS_DOWN_ACCEL_SEC * elapsed;

		}


		double dxOld = mDX;
		double dyOld = mDY;

		// figure speeds for the end of the period
		mDX += ddx;

		// block at max speed
		if (Math.abs(mDX) > PHYS_MOV_MAX)
			mDX = mHeading * PHYS_MOV_MAX;

//		//no puedo acelerar de espaldas (limitar el freno por roce con el suelo)
//		if (mDX * ddx > 0 && mHeading * mDX < 0) 
//			mDX = 0;

		mDY += ddy;

		float xMove = (int) (elapsed * (mDX + dxOld) / 2);
		float yMove = (int) (elapsed * (mDY + dyOld) / 2);         



		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		// DETECT COLLISIONS	
		///////////////////////////////////////////////////
		///////////////////////////////////////////////////
		
		
		//collision right
		if (xMove > 0){
			for (int i = 1; i <= xMove; i++){
				for (int posSprite = 1; posSprite < sprites.size(); posSprite++ ){
					Sprite s = sprites.elementAt(posSprite);
					if (s instanceof SpriteEnemigo && this.collision(s, 0, i)){
						xMove = i - 1;
						this.killed();
						break;
					}
				}
				if (collisionRight(level, i)) {
					xMove = i - 1;
					mDX = 0;
					break;
				}
			}
		//collision left
		} else if (xMove < 0){
			for (int i = -1; i >= xMove; i--){
				for (int posSprite = 1; posSprite < sprites.size(); posSprite++ ){
					Sprite s = sprites.elementAt(posSprite);
					if (s instanceof SpriteEnemigo && this.collision(s, 0, i)){
						xMove = i + 1;
						this.killed();
						break;
					}
				}
				if (collisionLeft(level, i)) {
					xMove = i + 1;
					mDX = 0;
					break;
				}
			} 
		}
		
		this.getPos().moveX(xMove);

		// landing
		if (yMove > 0){
			for (int i = 1; i <= yMove; i++){
				for (int posSprite = 1; posSprite < sprites.size(); posSprite++ ){
					Sprite s = sprites.elementAt(posSprite);
					if (s instanceof SpriteEnemigo && this.collision(s, 0, i)){
						xMove = i - 1;
						this.killed();
						break;
					}
				}
				if (collisionBottom(level, i)) {
					yMove = i - 1;
					mDY = 0;
					break;
				}
			}

		// collision top
		} else if (yMove < 0){
			for (int i = -1; i >= yMove; i--){
				for (int posSprite = 1; posSprite < sprites.size(); posSprite++ ){
					Sprite s = sprites.elementAt(posSprite);
					if (s instanceof SpriteEnemigo && this.collision(s, 0, i)){
						xMove = i + 1;
						this.killed();
						break;
					}
				}
				if (collisionTop(level, i)) {
					yMove = i + 1;
					mDY = 0;
					break;
				}
			}
		}
		
		this.getPos().moveY(yMove);

		//////////////////
		//////////////////
		// SHOOTING
		//////////////////
		//////////////////

		if ((mInputs.punteroDFin && mInputs.punteroDPos.getX()>mInputs.canvasWidth/4) 
				|| mInputs.teclaEspacio){
			
			if (mHeading == 1) {
				sprites.add(new SpriteDisparo(this.getPos().getX() + this.mWidth, 
						this.getPos().getY() + this.getHeight()/2, mRes, mInputs.punteroDPos));
			} else{
				sprites.add(new SpriteDisparo(this.getPos().getX() , 
						this.getPos().getY() + this.getHeight()/2, mRes, mInputs.punteroDPos));
			}
			mSoundPool.play(mIdShotSound, 1, 1, 1, 0, 1f);
		}  
	}
	
	// =========================================================== 
	// Methods
	// ===========================================================
	
	void killed() {
		alive = false;
	}
}
