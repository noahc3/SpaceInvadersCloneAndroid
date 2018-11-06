package com.sdsetup.noahc3.spaceinvadersclone;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

public class SpaceInvadersActivity extends Activity {

    //The game view, where draw is overridden to draw sprites
    SpaceInvadersView spaceInvadersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();

        //output display size to a point object for future use
        Point displaySize = new Point();
        display.getSize(displaySize);

        // init the custom view and tell it the screen dimensions.
        spaceInvadersView = new SpaceInvadersView(this, displaySize.x, displaySize.y);
        //set the view as current.
        setContentView(spaceInvadersView);

    }

    // runs when the game starts or is opened after being suspended (ex. game was previously launched and was reopened)
    @Override
    protected void onResume() {
        super.onResume();

        //run resume code on the view
        spaceInvadersView.resume();
    }

    // method runs when the game gets suspended (ex. user presses the home button)
    @Override
    protected void onPause() {
        super.onPause();

        // run pause code on the view
        spaceInvadersView.pause();
    }
}