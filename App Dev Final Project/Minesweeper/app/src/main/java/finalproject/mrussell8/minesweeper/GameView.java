package finalproject.mrussell8.minesweeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.google.gson.Gson;

public class GameView extends SurfaceView implements Runnable {
    private final int FPS = 1000/60;
    private Thread thread;
    private boolean isPlaying;
    private Board board;

    public GameView(Context context, Point screenSize) {
        super(context);
        board = new Board(screenSize, getResources(), (GameActivity)context);
    }

    // Functions for starting and stopping of the game and drawing initial canvas
    @Override
    public void run() {
        while(isPlaying) {
            draw();
            sleep();
        }
    }

    private void draw(){
        if(getHolder().getSurface().isValid()){
            Canvas canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.BLACK);
            board.draw(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void pause(){
        try {
            isPlaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume(){
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }

    public void sleep(){
        try {
            Thread.sleep(FPS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction() == MotionEvent.ACTION_UP){
            int x = Math.round(event.getX());
            int y = Math.round(event.getY());
            board.onClick(x, y);
        }
        return true;
    }

}
