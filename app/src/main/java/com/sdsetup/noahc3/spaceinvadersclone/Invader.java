package com.sdsetup.noahc3.spaceinvadersclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;

import java.util.Random;

public class Invader {
    RectF rect;

    Random generator = new Random();

    // invader bitmaps
    private Bitmap bitmap1;
    private Bitmap bitmap2;

    // width and height of invader
    private float width, height;

    // x and y pos
    private float x, y;

    // speed in Px/s
    private float shipSpeed = 40;

    //movement states
    //the invaders will never be stopped
    public final int LEFT = 1;
    public final int RIGHT = 2;

    // current movement direction
    private int shipMoving = RIGHT;

    //if the invader is visible (ie. not dead)
    boolean isVisible;

    public Invader(Context context, int row, int column, int screenX, int screenY) {

        // create new rect
        rect = new RectF();

        //set width and height of invaders
        width = screenX / 20;
        height = screenY / 20;

        isVisible = true;

        //set padding between each invader
        int padding = screenX / 25;

        //set width and height of invader based on its column and row
        x = column * (width + padding);
        y = row * (width + padding/4);

        // init bitmaps
        bitmap1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader1);
        bitmap2 = BitmapFactory.decodeResource(context.getResources(), R.drawable.invader2);

        // scale the bitmaps proportional to the screen resolution
        bitmap1 = Bitmap.createScaledBitmap(bitmap1,
                (int) (width),
                (int) (height),
                false);

        bitmap2 = Bitmap.createScaledBitmap(bitmap2,
                (int) (width),
                (int) (height),
                false);

    }

    //tons of getters and setters

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }

    public RectF getRect(){
        return rect;
    }

    public Bitmap getBitmap(){
        return bitmap1;
    }

    public Bitmap getBitmap2(){
        return bitmap2;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getWidth(){
        return width;
    }

    //update method, like the rest of them
    public void update(long fps){
        if(shipMoving == LEFT){
            x = x - shipSpeed / fps;
        }

        if(shipMoving == RIGHT){
            x = x + shipSpeed / fps;
        }

        // Update rect which is used to detect hits
        rect.top = y;
        rect.bottom = y + height;
        rect.left = x;
        rect.right = x + width;

    }

    //method will move the invaders towards the player and reverse their movement direction
    public void dropDownAndReverse(){
        if (shipMoving == LEFT){
            shipMoving = RIGHT;
        } else{
            shipMoving = LEFT;
        }

        y = y + height;

        shipSpeed = shipSpeed * 1.18f;
    }

    public boolean takeAim(float playerShipX, float playerShipLength){

        int randomNumber = -1;

        // if the invader is aligned with the player:
        if((playerShipX + playerShipLength > x &&
                playerShipX + playerShipLength < x + width) || (playerShipX > x && playerShipX < x + width)) {

            // fire chance 1 in 150
            randomNumber = generator.nextInt(150);
            if(randomNumber == 0) {
                return true;
            }

        }

        // random fire chance, 1 in 2000
        randomNumber = generator.nextInt(2000);
        if(randomNumber == 0){
            return true;
        }

        return false;
    }
}
