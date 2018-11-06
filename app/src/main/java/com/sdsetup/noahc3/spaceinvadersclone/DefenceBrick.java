package com.sdsetup.noahc3.spaceinvadersclone;

import android.graphics.RectF;

public class DefenceBrick {

    //bounding box
    private RectF rect;

    private boolean isVisible;

    public DefenceBrick(int row, int column, int shelterNumber, int screenX, int screenY){

        int width = screenX / 90;
        int height = screenY / 40;

        isVisible = true;

        // padding between each brick
        int brickPadding = 0;

        // number of shelters
        int shelterPadding = screenX / 9;
        int startHeight = screenY - (screenY /8 * 2);

        //set the rect position based on a number of factors
        rect = new RectF(column * width + brickPadding +
                (shelterPadding * shelterNumber) +
                shelterPadding + shelterPadding * shelterNumber,
                row * height + brickPadding + startHeight,
                column * width + width - brickPadding +
                        (shelterPadding * shelterNumber) +
                        shelterPadding + shelterPadding * shelterNumber,
                row * height + height - brickPadding + startHeight);
    }

    public RectF getRect(){
        return this.rect;
    }

    public void setInvisible(){
        isVisible = false;
    }

    public boolean getVisibility(){
        return isVisible;
    }

}
