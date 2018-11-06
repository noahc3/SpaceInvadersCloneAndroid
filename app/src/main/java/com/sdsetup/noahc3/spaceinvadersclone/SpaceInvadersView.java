package com.sdsetup.noahc3.spaceinvadersclone;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

//this game needs an actual game loop so implement runnable
public class SpaceInvadersView extends SurfaceView implements Runnable{

    //app context
    Context context;

    // game thread
    private Thread gameThread = null;

    // Our SurfaceHolder to lock the surface before we draw our graphics
    private SurfaceHolder ourHolder;

    // determines whether the game is running
    // some code may be called from another thread (ex. when the game is suspended by a system call and the pause method is run) so this variable should
    // be volatile so it is never cached with the incorrect value.
    private volatile boolean playing;

    // whether the game is paused or not. the game will be paused at the start for initialization.
    private boolean paused = true;

    // canvas and paint objects for drawing stuff
    private Canvas canvas;
    private Paint paint;

    // framerate
    private long fps;

    // frame time delta
    private long timeThisFrame;

    // screen size (passed in from activity)
    private int screenX;
    private int screenY;

    // player ship
    private PlayerShip playerShip;

    // the player's bullet, which they can have only one at a time
    private Bullet bullet;

    // currently active bullets
    private Bullet[] invadersBullets = new Bullet[200];
    private int nextBullet;
    private int maxInvaderBullets = 10;

    //note: the bullets are kept separate based on who fired it so we dont need to do collision detection for them on objects they will never hit
    //(ex. if the invader fired a bullet that bullet can only ever hit the player, so it would be a waste of time to check them against the invaders).

    // list of all invaders
    Invader[] invaders = new Invader[60];
    int numInvaders = 0;

    // The player's shelters are built from bricks
    private DefenceBrick[] bricks = new DefenceBrick[400];
    private int numBricks;

    // create a soundpool and have reference id's for each sfx that will be used
    private SoundPool soundPool;
    private int playerExplodeID = -1;
    private int invaderExplodeID = -1;
    private int shootID = -1;
    private int damageShelterID = -1;
    private int uhID = -1;
    private int ohID = -1;

    // current score
    int score = 0;

    // hp
    private int lives = 3;

    // How menacing should the sound be?
    private long menaceInterval = 1000;

    // Which menace sound should play next
    private boolean uhOrOh;

    // When did we last play a menacing sound
    private long lastMenaceTime = System.currentTimeMillis();

    //if the player is currently moving the ship
    private boolean shipMovementTouch = false;
    //x pos of the touch
    private float shipMovementTouchPos;

    //constructor for the view, initializes the object
    public SpaceInvadersView(Context context, int x, int y) {

        //run the super constructor, the default initialization code for the class this extends (SurfaceView)
        super(context);

        //copy the context somewhere as it is normally restricted to the constructor otherwise
        this.context = context;

        // init holder and paint
        ourHolder = getHolder();
        paint = new Paint();

        //set the screen size to the values passed in
        screenX = x;
        screenY = y;

        // soundpool will hold references to all of the sound effects
        // soundpool is deprecated in newer versions of the SDK but i'm running an android 4 emulator so im using this for compatibility sake
        // (it also seems to prevent the popping issue i get with mediaplayer).
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);

        //asset loading requires you to catch IOExceptions which may be raised for whatever reasons
        try{
            //init some necessary objects
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            //load all of the sound effects into the soundpool
            descriptor = assetManager.openFd("shoot.ogg");
            shootID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("invaderexplode.ogg");
            invaderExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("playerexplode.ogg");
            playerExplodeID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("damageshelter.ogg");
            damageShelterID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("uh.ogg");
            uhID = soundPool.load(descriptor, 0);

            descriptor = assetManager.openFd("oh.ogg");
            ohID = soundPool.load(descriptor, 0);

        } catch(IOException e){
            // if an IOException is thrown, catch it and log a message
            Log.e("error", "Sound files failed to load\n" + e.getMessage());
        }

        //init the level
        prepareLevel();
    }

    private void prepareLevel(){

        //reset menace level
        menaceInterval = 1000;

        // init the player ship
        playerShip = new PlayerShip(context, screenX, screenY);

        // create the player's bullet
        bullet = new Bullet(screenY);

        // fill the array with bullets
        for(int i = 0; i < invadersBullets.length; i++){
            invadersBullets[i] = new Bullet(screenY);
        }

        // generate all of the invaders
        numInvaders = 0;
        for(int column = 0; column < 6; column ++ ){
            for(int row = 0; row < 5; row ++ ){
                invaders[numInvaders] = new Invader(context, row, column, screenX, screenY);
                numInvaders ++;
            }
        }

        // create the shelters
        numBricks = 0;
        for(int shelterNumber = 0; shelterNumber < 4; shelterNumber++){
            for(int column = 0; column < 10; column ++ ) {
                for (int row = 0; row < 5; row++) {
                    bricks[numBricks] = new DefenceBrick(row, column, shelterNumber, screenX, screenY);
                    numBricks++;
                }
            }
        }

    }

    @Override
    //game loop
    public void run() {
        while (playing) {

            // save the current system time to use to calculate deltatime
            long startFrameTime = System.currentTimeMillis();

            // run game code if the game isnt paused
            if (!paused) {
                update();
            }

            // draw the frame
            draw();

            // calculate fps for use with animations and stuff
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }

            // play a sound effect based on the menace level
            if(!paused) {
                if ((startFrameTime - lastMenaceTime) > menaceInterval) {
                    if (uhOrOh) {
                        // play uh
                        soundPool.play(uhID, 1, 1, 0, 0, 1);

                    } else {
                        // play oh
                        soundPool.play(ohID, 1, 1, 0, 0, 1);
                    }

                    // reset the last menace time
                    lastMenaceTime = System.currentTimeMillis();
                    // update uhOrOh
                    uhOrOh = !uhOrOh;
                }
            }

        }
    }

    private void update(){

        //determine ship movement states
        if (shipMovementTouch) {
            //if the touch was to the right of the ship, set the ship to move right
            if (shipMovementTouchPos > Math.floor(playerShip.getX() + playerShip.getWidth()/2)) {
                playerShip.setMovementState(playerShip.RIGHT);
            } else if (shipMovementTouchPos < Math.floor(playerShip.getX() + playerShip.getWidth()/2)) { //if the touch was to the left, set the ship to move left
                playerShip.setMovementState(playerShip.LEFT);
            } else {
                //otherwise stop moving the ship
                playerShip.setMovementState(playerShip.STOPPED);
            }
        }

        // Did an invader bump into the side of the screen
        boolean bumped = false;

        // Has the player lost
        boolean lost = false;

        // call playerShip update to move the ship
        playerShip.update(fps);

        // update any visible invaders
        for(int i = 0; i < numInvaders; i++){

            //if the current invader is visible::
            if(invaders[i].getVisibility()) {
                // call its update method to move it
                invaders[i].update(fps);

                // run the fire method
                if(invaders[i].takeAim(playerShip.getX(),
                        playerShip.getWidth())){

                    // if the invader wants to fire, try and fire a bullet
                    if(invadersBullets[nextBullet].shoot(invaders[i].getX()
                                    + invaders[i].getWidth() / 2,
                            invaders[i].getY(), bullet.DOWN)) {

                        // update the bullet index
                        nextBullet++;

                        // reset bullet index to 0 if it is larger than the array size
                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0;
                        }
                    }
                }

                // if the move reached the edge of the screen, flag invaders to be bumped down
                if (invaders[i].getX() > screenX - invaders[i].getWidth()
                        || invaders[i].getX() < 0){

                    bumped = true;

                }
            }

        }

        // update all of the invader bullets if they are active
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getStatus()) {
                invadersBullets[i].update(fps);
            }
        }

        // if an invader bumped the screen bounds
        if(bumped){

            // drop and reverse invaders
            for(int i = 0; i < numInvaders; i++){
                invaders[i].dropDownAndReverse();
                // if the invaders have hit the bottom 10th of the screen, the player has lost
                if(invaders[i].getY() > screenY - screenY / 10){
                    lost = true;
                }
            }

            // increase menace level
            menaceInterval = menaceInterval - 80;
        }

        //if the player lost, reset the level
        if(lost){
            prepareLevel();
        }

        // move the players bullet if it is active
        if(bullet.getStatus()){
            bullet.update(fps);
        }

        // check if the players bullet is outside of the screen bounds
        if(bullet.getImpactPointY() < 0){
            bullet.setInactive();
        }

        // check if any invader bullets are outside of the screen bounds
        for(int i = 0; i < invadersBullets.length; i++){
            if(invadersBullets[i].getImpactPointY() > screenY){
                invadersBullets[i].setInactive();
            }
        }

        //check if the players bullet hit any invaders
        if(bullet.getStatus()) {
            for (int i = 0; i < numInvaders; i++) { //for each invader
                if (invaders[i].getVisibility()) { //check if the invader is visible
                    if (RectF.intersects(bullet.getRect(), invaders[i].getRect())) { //check if the bullet bounding box intersects with the invader bounding box

                        //play some sounds, kill the invader, deactivate the bullet, increase the score
                        invaders[i].setInvisible();
                        soundPool.play(invaderExplodeID, 1, 1, 0, 0, 1);
                        bullet.setInactive();
                        score = score + 10;

                        // use the score times the number of invaders to determine if the player has won
                        if(score == numInvaders * 10){
                            paused = true;
                            score = 0;
                            lives = 3;
                            prepareLevel();
                        }
                    }
                }
            }
        }

        // check if an invader bullet hit a shelter brick
        for(int i = 0; i < invadersBullets.length; i++){ //for each invader bullet
            if(invadersBullets[i].getStatus()){ //check if the bullet is active
                for(int j = 0; j < numBricks; j++){ //for each brick
                    if(bricks[j].getVisibility()){ //check if the brick is visible
                        if(RectF.intersects(invadersBullets[i].getRect(), bricks[j].getRect())){ //check if the brick bounding box intersects the bullet bounding box
                            // deactivate the bullet, destroy the brick, play a sound
                            invadersBullets[i].setInactive();
                            bricks[j].setInvisible();
                            soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                        }
                    }
                }
            }

        }

        // check if the player bullet hit a shelter brick
        if(bullet.getStatus()){ // if the bullet is active
            for(int i = 0; i < numBricks; i++){ //for each brick
                if(bricks[i].getVisibility()){ //check if it is visible
                    if(RectF.intersects(bullet.getRect(), bricks[i].getRect())){ //check if the brick intersects the bullet
                        // deactivate the bullet, destroy the brick,  play a sound
                        bullet.setInactive();
                        bricks[i].setInvisible();
                        soundPool.play(damageShelterID, 1, 1, 0, 0, 1);
                    }
                }
            }
        }

        // check if an invader bullet has hit the player ship
        for(int i = 0; i < invadersBullets.length; i++) { //foreach bullet
            if (invadersBullets[i].getStatus()) { //if the bullet is active
                if (RectF.intersects(playerShip.getRect(), invadersBullets[i].getRect())) { //check if the bullet intersects the player
                    //deactivate the bullet, decrease lives, play a sound
                    invadersBullets[i].setInactive();
                    lives--;
                    soundPool.play(playerExplodeID, 1, 1, 0, 0, 1);

                    // check if lives are 0 and reset the game if so
                    if (lives == 0) {
                        paused = true;
                        lives = 3;
                        score = 0;
                        prepareLevel();

                    }
                }
            }
        }
    }

    private void draw(){
        //make sure the drawing surface is valid and initialized
        if (ourHolder.getSurface().isValid()) {
            // by locking the canvas, resources arent wasted drawing incomplete frame buffers.
            canvas = ourHolder.lockCanvas();

            // draw background color
            canvas.drawColor(Color.argb(255, 7, 7, 28));

            // set the brush to FFFFFFFF for drawing images correctly
            paint.setColor(Color.argb(255,  255, 255, 255));

            // draw the spaceship
            canvas.drawBitmap(playerShip.getBitmap(), playerShip.getX(), playerShip.getY(), paint);

            // draw invaders
            for(int i = 0; i < numInvaders; i++){
                if(invaders[i].getVisibility()) {
                    if(uhOrOh) {
                        canvas.drawBitmap(invaders[i].getBitmap(), invaders[i].getX(), invaders[i].getY(), paint);
                    }else{
                        canvas.drawBitmap(invaders[i].getBitmap2(), invaders[i].getX(), invaders[i].getY(), paint);
                    }
                }
            }

            // draw shelters
            for(int i = 0; i < numBricks; i++){
                if(bricks[i].getVisibility()) {
                    canvas.drawRect(bricks[i].getRect(), paint);
                }
            }

            // draw player bullet if active
            if(bullet.getStatus()){
                canvas.drawRect(bullet.getRect(), paint);
            }

            // draw invader bullets
            for(int i = 0; i < invadersBullets.length; i++){
                if(invadersBullets[i].getStatus()) {
                    canvas.drawRect(invadersBullets[i].getRect(), paint);
                }
            }

            // draw score
            paint.setColor(Color.argb(255,  255, 232, 132)); // change brush color
            paint.setTextSize(50); //set text size to 40px
            canvas.drawText("Score: " + score + "   Lives: " + lives, 10,60, paint); //actually draw the text

            // unlock canvas and flush it
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    // if the activity gets paused by a system event, stop the game thread.
    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            //this is a common exception because the game thread will probably be in the middle of execution when the game is suspended
            Log.e("Error:", "joining thread");
        }

    }

    // start thread when the game is resumed from suspension or the game is launched
    public void resume() {
        playing = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    // use this event to detect touch input for moving the ship
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                paused = false;

                //if the touch was in the bottom 8th of the screen
                if(motionEvent.getY() > screenY - screenY / 8) {
                    //enable touch checking in the update loop
                    shipMovementTouch = true;
                    shipMovementTouchPos = motionEvent.getX();

                } else {
                    //try and fire a bullet. this will return true if there is currently no bullet on the screen.
                    if(bullet.shoot(playerShip.getX() +
                            playerShip.getWidth()/2,screenY,bullet.UP)){
                        soundPool.play(shootID, 1, 1, 0, 0, 1); //if the bullet was fired successfully, play a fire sound
                    }
                }
                break;
            // player touched the screen this frame
            case MotionEvent.ACTION_MOVE:

                //if the touch was in the bottom 8th of the screen
                if(motionEvent.getY() > screenY - screenY / 8) {
                    //enable touch checking in the update loop
                    shipMovementTouch = true;
                    shipMovementTouchPos = motionEvent.getX();

                }

                break;


            // player has stopped touching the screen this frame
            case MotionEvent.ACTION_UP:

                if(motionEvent.getY() > screenY - screenY / 8) { //if the player stopped touching in the bottom 8th of the screen, stop moving the ship
                    playerShip.setMovementState(playerShip.STOPPED);
                    shipMovementTouch = false;
                }

                break;
        }

        return true;
    }
}
