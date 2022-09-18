package finalproject.mrussell8.minesweeper;

import android.graphics.Paint;
import android.graphics.Point;

public class Buttons {
    private Point pos;
    private int height;
    private int width;
    private Paint paint = new Paint();

    public Buttons(Point pos, int width, int height) {
        this.pos = pos;
        this.height = height;
        this.width = width;
        this.paint.setARGB(255, 0, 255, 0);
    }

    // Makes clickable area for flag
    public boolean isFlagClicked(Point point){
        return (point.x >= Board.flagOffsetX && point.y >= Board.flagOffsetY &&
                point.x <= Board.flagOffsetX2  && point.y <= Board.flagOffsetY2);
    }

    // Makes clickable area for main menu once game is over
    public boolean isMainMenuClicked(Point point){
        return (point.x >= Board.mainMenuOffsetX && point.y >= Board.mainMenuOffsetY &&
                point.x <= Board.mainMenuOffsetX2 && point.y <= Board.mainMenuOffsetY2);
    }
}
