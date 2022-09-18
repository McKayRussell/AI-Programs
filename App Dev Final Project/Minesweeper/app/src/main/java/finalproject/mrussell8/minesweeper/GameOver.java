package finalproject.mrussell8.minesweeper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

public class GameOver {
    private Point pos;
    private final Paint paint = new Paint();
    private int width, height;
    public int alpha = 255;

    public GameOver(Point pos, int width, int height) {
        this.pos = pos;
        this.width = width;
        this.height = height;
    }

    // Canvas changes for game over state
    public void draw(Canvas canvas, int gameState){
        if(gameState == 0){
            paint.setARGB(alpha, 191, 203, 204);
            canvas.drawRect(Board.mainMenuOffsetX, Board.mainMenuOffsetY,
                    Board.mainMenuOffsetX2, Board.mainMenuOffsetY2, paint);
        }
        else if(gameState == 1){ // Stops drawing cover and prints message
            paint.setARGB(255, 93, 173, 88);
            paint.setTextSize(Board.textSize + 5);
            canvas.drawText("You Win!", Board.offsetX, Board.offsetY, paint);

            GameActivity.gameOver = 1;
        }
        else if(gameState == 2){ // Stops drawing cover and prints message
            paint.setColor(Color.RED);
            paint.setTextSize(Board.textSize + 5);
            canvas.drawText("You Lose :(", Board.offsetX, Board.offsetY, paint);

            GameActivity.gameOver = 1;
        }
    }
}
