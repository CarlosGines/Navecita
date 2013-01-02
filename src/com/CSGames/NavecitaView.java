/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.CSGames;

import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.CSGames.Sprites.Sprite;
import com.CSGames.Sprites.SpriteEnemigo;
import com.CSGames.Sprites.SpriteHero;


/**
 * View that draws, takes keystrokes, etc. for a simple Navecita game.
 * 
 * Has a mode which RUNNING, PAUSED, etc. Has a x, y, dx, dy, ... capturing the
 * current ship physics. All x/y etc. are measured with (0,0) at the lower left.
 * updatePhysics() advances the physics based on realtime. draw() renders the
 * ship, and does an invalidate() to prompt another draw() as soon as possible
 * by the system.
 */
public class NavecitaView extends SurfaceView implements SurfaceHolder.Callback {
	
	// =========================================================== 
	// Constants
	// ===========================================================
	
	public static final int  TILE_SIZE = 16;
	
	// =========================================================== 
	// Fields
	// ===========================================================
	
	 /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

    /** Pointer to the text view to display "Paused.." etc. */
    private TextView mStatusText;

    /** The thread that actually draws the animation */
    private NavecitaThread mThread;
    
	// =========================================================== 
	// Constructors
	// ===========================================================
	
    public NavecitaView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        mThread = new NavecitaThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                mStatusText.setVisibility(m.getData().getInt("viz"));
                mStatusText.setText(m.getData().getString("text"));
            }
        });

        setFocusable(true); // make sure we get key events
    }
    
	// =========================================================== 
	// Getter & Setter
	// ===========================================================
	
    /**
     * Fetches the animation thread corresponding to this LunarView.
     * 
     * @return the animation thread
     */
    public NavecitaThread getThread() {
        return mThread;
    }
    
    /**
     * Installs a pointer to the text view used for messages.
     */
    public void setTextView(TextView textView) {
        mStatusText = textView;
    }
    
	// =========================================================== 
	// Methods for/from SuperClass/Interfaces
	// ===========================================================
	
    /**
     * Standard override to get key-press events.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        return mThread.doKeyDown(keyCode, msg);
    }

    /**
     * Standard override for key-up. We actually care about these, so we can
     * turn off the engine or stop rotating.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        return mThread.doKeyUp(keyCode, msg);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
    	return mThread.doTouchEvent (motionEvent);
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) mThread.pause();
    }
    
    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        mThread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        mThread.setRunning(true);
        mThread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        mThread.setRunning(false);
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
    
	// =========================================================== 
	// Inner and Anonymous Classes
	// ===========================================================
	
    class NavecitaThread extends Thread {
    	
    	// =========================================================== 
    	// Constants
    	// ===========================================================
    	    	
    	public static final int INVALID_POINTER_ID = -1;
    	public static final int SOUND_SHOT = 1;

        /*
         * State-tracking constants
         */
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;
        
        
    	// =========================================================== 
    	// Fields
    	// ===========================================================

        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;       

        /** Used to figure out elapsed time between frames */
        private long mLastTime;

        /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
        private int mMode;
        
        private GameInputs mInputs = GameInputs.getInstance();
        
        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;

        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;
              
        private World mWorld = World.getInstance();
        
        private SpriteHero mHero;
        
        private Vector<Sprite> mSprites;
        
        private Bitmap tiles[];
        
        /** Variables para controlar los gestos en la pantalla*/        
//        private int mLPointerID = INVALID_POINTER_ID;
        private Position mLPointerStartPos;
//        
//        private int mRPointerID = INVALID_POINTER_ID;
        private Position mRPointerStartPos;
        
        
    	// =========================================================== 
    	// Constructors
    	// ===========================================================

        public NavecitaThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;
            
            //Initialize level and camera and ship position
            mWorld.currentLevel.loadLevel(1, context.getResources());
            mWorld.currentLevel.loadEnemigos(1, context.getResources());

            mSprites = new Vector<Sprite>();
            
            mHero = new SpriteHero(mWorld.currentLevel.getStartX()*TILE_SIZE, 
            		mWorld.currentLevel.getStartY()*TILE_SIZE, context, mHandler);
            //TODO remove handler
            
            mSprites.add(mHero);

            mWorld.camPos = new Position(0, 16);
            
            //Load tileset
            tiles = new Bitmap[Level.TILES.length];
            for (int i = 1; i < Level.TILES.length; i++) {
            	tiles[i] = BitmapFactory.decodeResource(mContext.getResources(),
                                Level.TILES[i]);
            }		
        }

    	// =========================================================== 
    	// Methods for/from SuperClass/Interfaces
    	// ===========================================================
        
        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        if (mMode == STATE_RUNNING) updateWorld();
                        doDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
        
    	// =========================================================== 
    	// Methods
    	// ===========================================================
        
        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            synchronized (mSurfaceHolder) {

            	mSprites.clear();
            	mSprites.add(mHero);
            	
                mHero.getPos().setXTile(mWorld.currentLevel.getStartX());
                mHero.getPos().setXTileOffset(TILE_SIZE/2);
                mHero.getPos().setYTile(mWorld.currentLevel.getStartY());
                mHero.getPos().setYTileOffset(TILE_SIZE-1);
                mHero.alive = true;
                
                mWorld.camPos = new Position(0, 16);
                mWorld.currentLevel.loadEnemigos(1, mContext.getResources());
                

                mLastTime = System.currentTimeMillis() + 100;
                setState(STATE_RUNNING);
            }
        }

        /**
         * Pauses the physics update & animation.
         */
        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        /**
         * Restores game state from the indicated Bundle. Typically called when
         * the Activity is being restored after having been previously
         * destroyed.
         * 
         * @param savedState Bundle containing the game state
         */
        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);

//                mX = savedState.getDouble(KEY_X);
            }
        }


        /**
         * Dump game state to the provided Bundle. Typically called when the
         * Activity is being suspended.
         * 
         * @return Bundle with this view's state
         */
        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
//                if (map != null) {
//                    map.putInt(KEY_DIFFICULTY, Integer.valueOf(mDifficulty));
//                }
            }
            return map;
        }


        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         * 
         * @param b true to run, false to shut down
         */
        public void setRunning(boolean b) {
            mRun = b;
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @see #setState(int, CharSequence)
         * @param mode one of the STATE_* constants
         */
        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        /**
         * Sets the game mode. That is, whether we are running, paused, in the
         * failure state, in the victory state, etc.
         * 
         * @param mode one of the STATE_* constants
         * @param message string to add to screen or null
         */
        public void setState(int mode, CharSequence message) {
            /*
             * This method optionally can cause a text message to be displayed
             * to the user when the mode changes. Since the View that actually
             * renders that text is part of the main View hierarchy and not
             * owned by this thread, we can't touch the state of that View.
             * Instead we use a Message + Handler to relay commands to the main
             * thread, which updates the user-text View.
             */
            synchronized (mSurfaceHolder) {
                mMode = mode;

                if (mMode == STATE_RUNNING) {
                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                } else {

                    Resources res = mContext.getResources();
                    CharSequence str = "";
                    if (mMode == STATE_READY)
                        str = res.getText(R.string.mode_ready);
                    else if (mMode == STATE_PAUSE)
                        str = res.getText(R.string.mode_pause);
                    else if (mMode == STATE_LOSE)
                        str = res.getText(R.string.mode_lose);
                    else if (mMode == STATE_WIN)
                        str = res.getString(R.string.mode_win_prefix)
                                + res.getString(R.string.mode_win_suffix);

                    if (message != null) {
                        str = message + "\n" + str;
                    }

                    Message msg = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    mHandler.sendMessage(msg);
                }
            }
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mInputs.canvasWidth = width;
                mInputs.canvasHeight = height;

                // don't forget to resize the background image
//                mBackgroundImage = Bitmap.createScaledBitmap(
//                        mBackgroundImage, width, height, true);
                
            }
        }

        /**
         * Resumes from a pause.
         */
        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            setState(STATE_RUNNING);
        }

        /**
         * Handles a key-down event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true
         */
        boolean doKeyDown(int keyCode, KeyEvent msg) {
            synchronized (mSurfaceHolder) {
                boolean okStart = false;
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) okStart = true;
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) okStart = true;
                if (keyCode == KeyEvent.KEYCODE_S) okStart = true;


                if (okStart
                        && (mMode == STATE_READY || mMode == STATE_LOSE || mMode == STATE_WIN)) {
                    // ready-to-start -> start
                    doStart();
                    return true;
                } else if (mMode == STATE_PAUSE && okStart) {
                    // paused -> running
                    unpause();
                    return true;
                } else if (mMode == STATE_RUNNING) {
                   	// center/space -> jump
                	if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                			|| keyCode == KeyEvent.KEYCODE_SPACE) {
                		mInputs.teclaEspacio = true;                		
                		return true;
                
                	}else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                			|| keyCode == KeyEvent.KEYCODE_Q) {
                		mInputs.teclaIzda = true;
                		return true;
                	
                	} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                			|| keyCode == KeyEvent.KEYCODE_W) {
                		mInputs.teclaDcha = true;
                		return true;
                	
                	} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                		mInputs.teclaArr = true;
                		return true;
                	} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                		doStart();
                		return true;
                	}
                }

                return false;
            }
        }

        /**
         * Handles a key-up event.
         * 
         * @param keyCode the key that was pressed
         * @param msg the original event object
         * @return true if the key was handled and consumed, or else false
         */
        boolean doKeyUp(int keyCode, KeyEvent msg) {
            boolean handled = false;

            synchronized (mSurfaceHolder) {
            	if (mMode == STATE_RUNNING) {
            		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_Q){
            			mInputs.teclaIzda = false;
            			handled = true;
            		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT || keyCode == KeyEvent.KEYCODE_W) {
            			mInputs.teclaDcha = false;
            			handled = true;
            		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_SPACE) {
            			mInputs.teclaEspacio = false;
            			handled = true;
            		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            			mInputs.teclaArr = false;
            			handled = true;
            		}
            		
            	}
            }

            return handled;
        }
        
        boolean doTouchEvent (MotionEvent e){
        	synchronized (mSurfaceHolder) {

        		int action = e.getAction();

        		switch (action & MotionEvent.ACTION_MASK){ 

        		case MotionEvent.ACTION_DOWN:

        			//Log.v("YEAH", "DOWN" + " -> X=" + mStrokes.getX() + " Y=" +  mStrokes.getY() + " HistorySize=" + mStrokes.getHistorySize());
        			if (e.getX()>mInputs.canvasWidth/4){        				
        				mInputs.punteroD = true;       
        				mInputs.punteroDPos.setX(e.getX());
        				mInputs.punteroDPos.setY(e.getY());
        				//        				mRPointerID = e.getPointerId(0);
        				mRPointerStartPos = new Position(e.getX(), e.getY());

        			} else {
        				mInputs.punteroI = true;
        				mInputs.punteroIPos.setX(e.getX());
        				mInputs.punteroIPos.setY(e.getY());        				
        				//        				mLPointerID = e.getPointerId(0);
        				mLPointerStartPos = new Position(e.getX(), e.getY());
        			}        	
        			break;
        			// -> Commented code on this method is for multitouch, now disabled
        			//        		case MotionEvent.ACTION_POINTER_DOWN:
        			//
        			//        			int pointerIndex = (action & MotionEvent.ACTION_POINTER_ID_MASK) 
        			//        			>> MotionEvent.ACTION_POINTER_ID_SHIFT;
        			//
        			//        			//Log.v("YEAH", "DOWN" + " -> X=" + mStrokes.getX() + " Y=" +  mStrokes.getY() + " HistorySize=" + mStrokes.getHistorySize());
        			//        			if (e.getX(pointerIndex)>mCanvasWidth/4){        				
        			//        				mInputs.punteroD = true;
        			//        				mInputs.punteroDPos = new Position(e.getX(pointerIndex), e.getY(pointerIndex));
        			//        				mRPointerID = e.getPointerId(pointerIndex);
        			//        				mRPointerStartPos = new Position(e.getX(pointerIndex), e.getY(pointerIndex));
        			//        			} else {
        			//        				mInputs.punteroI = true;
        			//        				mInputs.punteroIPos = new Position(e.getX(pointerIndex), e.getY(pointerIndex));
        			//        				mLPointerID = e.getPointerId(pointerIndex);
        			//        				mLPointerStartPos = new Position(e.getX(pointerIndex), e.getY(pointerIndex));
        			//        			}        	
        			//        			break;

        		case MotionEvent.ACTION_MOVE:

        			detectStrokes(e);
        			if (mInputs.punteroD){
        				Log.v("INPUTS", ""+mInputs);
        				Log.v("punteroPos", ""+mInputs.punteroDPos);
        				mInputs.punteroDPos.setX(e.getX());
        				mInputs.punteroDPos.setY(e.getY());
        			} else {
        				mInputs.punteroIPos.setX(e.getX());
        				mInputs.punteroIPos.setY(e.getY());
        			}

        			//        			int pointerIndex;
        			//        			if (mRPointerID != INVALID_POINTER_ID) {
        			//        				pointerIndex = e.findPointerIndex(mRPointerID);
        			//        				mInputs.punteroDPos.setX(e.getX());
        			//        				mInputs.punteroDPos.setY(e.getY());
        			//        			}
        			//        			if (mLPointerID != INVALID_POINTER_ID) {
        			//        				pointerIndex = e.findPointerIndex(mLPointerID);
        			//        				mInputs.punteroIPos.setX(e.getX());
        			//        				mInputs.punteroIPos.setY(e.getY());
        			//        			}
        			break;

        			//        		case MotionEvent.ACTION_POINTER_UP:
        			//
        			//        			detectStrokes(e);
        			//        			pointerIndex = (action & MotionEvent.ACTION_POINTER_ID_MASK) 
        			//        					>> MotionEvent.ACTION_POINTER_ID_SHIFT;
        			//        			if (e.getPointerId(pointerIndex) == mRPointerID){
        			//        				mRPointerID = INVALID_POINTER_ID;
        			//        				mRPointerStartPos = null;
        			//        				mInputs.punteroD = false;
        			//        				mInputs.punteroDFin = true;
        			//        				mInputs.punteroDPos.setX(e.getX(pointerIndex));
        			//        				mInputs.punteroDPos.setY(e.getY(pointerIndex));
        			//       				} else {
        			//      					mLPointerID = INVALID_POINTER_ID;
        			//      					mLPointerStartPos = null;
        			//      					mInputs.punteroI = false;
        			//      					mInputs.punteroIFin = true;
        			//      					mInputs.punteroIPos.setX(e.getX(pointerIndex));
        			//        				mInputs.punteroIPos.setY(e.getY(pointerIndex));
        			//   					}
        			//       				break;
        		case MotionEvent.ACTION_CANCEL:
        		case MotionEvent.ACTION_UP:


        			detectStrokes(e);
        			//        			if (mRPointerID != INVALID_POINTER_ID) {
        			//        				mRPointerID = INVALID_POINTER_ID;
        			//        				mRPointerStartPos = null;
        			//        				mInputs.punteroD = false;
        			//        				mInputs.punteroDFin = true;
        			//        				mInputs.punteroDPos.setX(e.getX());
        			//        				mInputs.punteroDPos.setY(e.getY());
        			//        			} else {
        			//        				mLPointerID = INVALID_POINTER_ID;
        			//        				mLPointerStartPos = null;
        			//        				mInputs.punteroI = false;
        			//        				mInputs.punteroIFin = true;
        			//        				mInputs.punteroIPos.setX(e.getX());
        			//        				mInputs.punteroIPos.setY(e.getY());
        			//        			}  
        			if (mInputs.punteroD) {
        				mInputs.punteroD = false;
        				mInputs.punteroDFin = true;
        				mInputs.punteroDPos.setX(e.getX());
        				mInputs.punteroDPos.setY(e.getY());
        			} else {
        				mInputs.punteroI = false;
        				mInputs.punteroIFin = true;
        				mInputs.punteroIPos.setX(e.getX());
        				mInputs.punteroIPos.setY(e.getY());
        			}  


        			break;
        		}                
        		return true;
        	}
        }
        
        private void detectStrokes (MotionEvent e){
        	final int STROKE_SENSITIVITY = 30;
        	// -> Commented code on this method is for multitouch, now disabled
        	//        	int pointerIndex;
        	//        	if (mRPointerID != INVALID_POINTER_ID) {
        	//				pointerIndex = e.findPointerIndex(mRPointerID);
        	//				if (e.getY(pointerIndex) - mRPointerStartPos.getY() < -STROKE_SENSITIVITY){
        	//					mInputs.punteroDArr = true;
        	//					mRPointerStartPos.setX(e.getX(pointerIndex));
        	//					mRPointerStartPos.setY(e.getY(pointerIndex));
        	//				} else if (e.getY(pointerIndex) - mRPointerStartPos.getY() > STROKE_SENSITIVITY){
        	//					mInputs.punteroDAbj = true;
        	//					mRPointerStartPos.setX(e.getX(pointerIndex));
        	//					mRPointerStartPos.setY(e.getY(pointerIndex));     
        	//				} else if (e.getX(pointerIndex) - mRPointerStartPos.getX() > STROKE_SENSITIVITY){
        	//					mInputs.punteroDDcha = true;
        	//					mRPointerStartPos.setX(e.getX(pointerIndex));
        	//					mRPointerStartPos.setY(e.getY(pointerIndex)); 
        	//				} else if (e.getX(pointerIndex) - mRPointerStartPos.getX() < -STROKE_SENSITIVITY){
        	//					mInputs.punteroDIzda = true;
        	//					mRPointerStartPos.setX(e.getX(pointerIndex));
        	//					mRPointerStartPos.setY(e.getY(pointerIndex));
        	//				} 
        	//			} 
        	//			if (mLPointerID != INVALID_POINTER_ID) {
        	//				pointerIndex = e.findPointerIndex(mLPointerID);
        	//				if (e.getY(pointerIndex) - mLPointerStartPos.getY() < -STROKE_SENSITIVITY){
        	//					mInputs.punteroIArr = true;
        	//					mLPointerStartPos.setX(e.getX(pointerIndex));
        	//					mLPointerStartPos.setY(e.getY(pointerIndex));
        	//				} else if (e.getY(pointerIndex) - mLPointerStartPos.getY() > STROKE_SENSITIVITY){
        	//					mInputs.punteroIAbj = true;
        	//					mLPointerStartPos.setX(e.getX(pointerIndex));
        	//					mLPointerStartPos.setY(e.getY(pointerIndex)); 
        	//				} else if (e.getX(pointerIndex) - mLPointerStartPos.getX() > STROKE_SENSITIVITY){
        	//					mInputs.punteroIDcha = true;
        	//					mLPointerStartPos.setX(e.getX(pointerIndex));
        	//					mLPointerStartPos.setY(e.getY(pointerIndex));     
        	//				} else if (e.getX(pointerIndex) - mLPointerStartPos.getX() < -STROKE_SENSITIVITY){
        	//					mInputs.punteroIIzda = true;
        	//					mLPointerStartPos.setX(e.getX(pointerIndex));
        	//					mLPointerStartPos.setY(e.getY(pointerIndex));
        	//				} 	
        	//			} 

        	if (mInputs.punteroD){
        		if (e.getY() - mRPointerStartPos.getY() < -STROKE_SENSITIVITY){
        			mInputs.punteroDArr = true;
        			mRPointerStartPos.setX(e.getX());
        			mRPointerStartPos.setY(e.getY());
        		} else if (e.getY() - mRPointerStartPos.getY() > STROKE_SENSITIVITY){
        			mInputs.punteroDAbj = true;
        			mRPointerStartPos.setX(e.getX());
        			mRPointerStartPos.setY(e.getY());     
        		} else if (e.getX() - mRPointerStartPos.getX() > STROKE_SENSITIVITY){
        			mInputs.punteroDDcha = true;
        			mRPointerStartPos.setX(e.getX());
        			mRPointerStartPos.setY(e.getY()); 
        		} else if (e.getX() - mRPointerStartPos.getX() < -STROKE_SENSITIVITY){
        			mInputs.punteroDIzda = true;
        			mRPointerStartPos.setX(e.getX());
        			mRPointerStartPos.setY(e.getY());
        		} 
        	} else {
            	if (e.getY() - mLPointerStartPos.getY() < -STROKE_SENSITIVITY){
            		mInputs.punteroIArr = true;
            		mLPointerStartPos.setX(e.getX());
            		mLPointerStartPos.setY(e.getY());
            	} else if (e.getY() - mLPointerStartPos.getY() > STROKE_SENSITIVITY){
            		mInputs.punteroIAbj = true;
            		mLPointerStartPos.setX(e.getX());
            		mLPointerStartPos.setY(e.getY());     
            	} else if (e.getX() - mLPointerStartPos.getX() > STROKE_SENSITIVITY){
            		mInputs.punteroIDcha = true;
            		mLPointerStartPos.setX(e.getX());
            		mLPointerStartPos.setY(e.getY()); 
            	} else if (e.getX() - mLPointerStartPos.getX() < -STROKE_SENSITIVITY){
            		mInputs.punteroIIzda = true;
            		mLPointerStartPos.setX(e.getX());
            		mLPointerStartPos.setY(e.getY());
            	} 
        	}
        }

        /**
         * Draws the ship, fuel/speed bars, and background to the provided
         * Canvas.
         */
        private void doDraw(Canvas canvas) {
        	
        	// the screen is built with a matrix of tiles.
        	//Screen size can change
        	
        	// It is hard that camera is in the center of what I draw.
        	// I need to calculate the offset for the first tile.
        	
        	// Es jodido que la camara estŽ en el centro de lo que dibujo.
        	// Tengo que calcular el offset del primer tile
        	
        	//TODO what I mention above. Now it could not work for every screen.
        	//TODO lo de arriba. Esta solo apa–ado. Muy cutre. Solo v‡lido para mi movil
        	
        	
        	for (int i=0; i<(mInputs.canvasWidth/TILE_SIZE + 2); i++)
        		for (int j=0; j<(mInputs.canvasHeight/TILE_SIZE + 2); j++) {
        			int x;
        			int y;
        			int tileCode;

        			x=i+mWorld.camPos.getXTile();
        			y=j+mWorld.camPos.getYTile();
        			tileCode = mWorld.currentLevel.getTile(x, y);
        			
        			if (tileCode != Level.TILE_OUT_OF_BOUNDS)
        				canvas.drawBitmap(tiles[tileCode], i*TILE_SIZE-mWorld.camPos.getXTileOffset(), 
        					j*TILE_SIZE-mWorld.camPos.getYTileOffset(), null);
        			
        			if (mWorld.currentLevel.devueveEnemigo(x, y)){
        				mSprites.add(new SpriteEnemigo(x*TILE_SIZE, y*TILE_SIZE, mContext.getResources(), SpriteEnemigo.EVIL_FISH));
        			}
        		}

            // Draw tal the sprites
      
            Sprite s;
            for (int i = mSprites.size()-1; i >= 0; i--) {
            	s = mSprites.elementAt(i);
            	canvas.drawBitmap(s.getFrame(), s.getPos().getX() - mWorld.camPos.getX(), 
            			s.getPos().getY() - mWorld.camPos.getY(), null);
            }
        }

        /**
         * Figures the lander state (x, y, fuel, ...) based on the passage of
         * realtime. Does not invalidate(). Called at the start of draw().
         * Detects the end-of-game and sets the UI to the next state.
         */
        private void updateWorld() {
            long now = System.currentTimeMillis();

            // Do nothing if mLastTime is in the future.
            // This allows the game-start to delay the start of the physics
            // by 100ms or whatever.
            if (mLastTime > now) return;

            double elapsed = (now - mLastTime) / 1000.0;
            
           
            
            Sprite s;
            for (int i = mSprites.size() -1; i >= 0; i--) {
            	s = mSprites.elementAt(i);
            	s.action(mSprites, elapsed);
              	if (s.isDone()){
                	mSprites.removeElementAt(i);
            	}
            }
            mInputs.processed();
            
            Sprite hero = mSprites.elementAt(0);
            if (hero.getPos().getY() > 380) {
            	setState(STATE_LOSE);
            }
            else if (hero.getPos().getX() > mWorld.currentLevel.getSizeX()*TILE_SIZE - 400) {
            	setState(STATE_WIN);
            }
            if (!mHero.alive){
            	setState(STATE_LOSE);
            }
            
            
            mWorld.camPos.setX(mHero.getPos().getX() - mInputs.canvasWidth/4 + mHero.getWidth()/2);
            
            mLastTime = now;

           
            
//            Message msg = mHandler.obtainMessage();
//            Bundle b = new Bundle();
//            b.putString("text", "mDY = " + mDY + "  mY = " + mY);
//            b.putInt("viz", View.VISIBLE);
//            msg.setData(b);
//            mHandler.sendMessage(msg);
            
//            int result = STATE_RUNNING;
//            CharSequence message = "";
//            setState(result, message);
        }
    }

   
}
