package com.sdsetup.noahc3.spaceinvadersclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

public class PlayerShip {

    //bounding box for collision
    //shapes are pretty handy for collision detection cause they have intersect detection methods
    private RectF rect;

    //player ship image
    private Bitmap bitmap;

    // width and height of the ship
    private float width, height;

    // x and y position of the ship
    private float x, y;

    // ship movement speed in Px/s
    private float shipSpeed = 350;

    // ship movement values
    //TODO: make this an enum
    public final int STOPPED = 0;
    public final int LEFT = 1;
    public final int RIGHT = 2;

    //current ship movement direction
    //TODO: make this use an enum
    private int shipMoving = STOPPED;

    // constructor, initializes ship object
    public PlayerShip(Context context, int screenX, int screenY){

        // init bounding box rect
        rect = new RectF();

        //set width and height of the ship, proportional to screen size
        width = screenX/10;
        height = screenY/10;

        // start the ship in the middle of the screen
        x = Math.round((screenX / 2) - (width / 2));
        y = screenY - height;

        // init the ship bitmap
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.playership);

        // scale the ship bitmap proportional to the width and height of the screen using the vars determined earlier
        bitmap = Bitmap.createScaledBitmap(bitmap, (int) (width),(int) (height),false);
    }

    //let other classes get the ship bounding box
    public RectF getRect(){
        return rect;
    }


    public Bitmap getBitmap(){
        return bitmap;
    }

    public float getX(){
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth(){
        return width;
    }


    // lets other classes (the view) set the movement state of the ship
    public void setMovementState(int state){
        shipMoving = state;
    }

    // update method called from view
    // runs movement logic and updates the bounding rect
    public void update(long fps){
        if(shipMoving == LEFT){
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT){
            x = x + shipSpeed / fps;
        }

        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + width;

    }

}
