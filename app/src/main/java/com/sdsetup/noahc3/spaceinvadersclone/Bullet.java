package com.sdsetup.noahc3.spaceinvadersclone;

import android.graphics.RectF;

public class Bullet {

    //x and y position
    private float x, y;

    //bounding box
    private RectF rect;

    // bullet direction (up for player, down for invader)
    public final int UP = 0;
    public final int DOWN = 1;

    // direction the bullet moves
    int heading = -1;
    // speed in Px/s
    float speed =  350;

    private int width = 1;
    private int height;

    //if the bullet is alive or not
    private boolean isActive;

    //initializer for the bullet
    public Bullet(int screenY) {

        height = screenY / 20;
        isActive = false;

        rect = new RectF();
    }

    //getter for bounding box
    public RectF getRect(){
        return  rect;
    }

    public boolean getStatus(){
        return isActive;
    }

    //
    public void setInactive(){
        isActive = false;
    }

    public float getImpactPointY(){
        if (heading == DOWN){
            return y + height;
        }else{
            return  y;
        }

    }

    //called when the bullet is shot. inits the start x and y, and the direction it should move
    public boolean shoot(float startX, float startY, int direction) {
        if (!isActive) { //make sure the bullet isnt already active
            x = startX;
            y = startY;
            heading = direction;
            isActive = true;
            return true;
        }

        //return false if the bullet is already active
        return false;
    }

    //update to translate the bullet and bounding box
    public void update(long fps){

        // Just move up or down
        if(heading == UP){
            y = y - speed / fps;
        }else{
            y = y + speed / fps;
        }

        // Update rect
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;

    }

}
