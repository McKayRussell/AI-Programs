package finalproject.mrussell8.minesweeper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private Point pos;
    int width;
    int height;
    List<Paint> paints = new ArrayList<>();
    int selectedPaint = 0;

    public Cell(int selectedPaint, int width, int height, Point pos) {
        this.selectedPaint = selectedPaint;
        for(int i = 0; i < 2; i++){
            paints.add(new Paint());
        }
        paints.get(0).setARGB(255, 185, 185, 185);          // Dark Grey
        paints.get(1).setARGB(0, 0, 0, 0);                  // Clear

        this.width = width;
        this.height = height;
        this.pos = pos;
    }

    public void draw(Canvas canvas, Bitmap bmb, Bitmap explode, Bitmap flag, int counter,
                     int[][] grid){
        // Set text color and size
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(Board.textSize);

        // If cell has not been uncovered and isn't a flagged cell, draw cover
        // ][0] == 0 hasn't been clicked, ][2] == 0 isn't a flag
        if(grid[counter][0] == 0 && grid[counter][2] == 0){
            canvas.drawRect(pos.x, pos.y, pos.x + width,
                    pos.y + height, paints.get(selectedPaint));
        }
        // If the cell hasn't been uncovered and the flag value has been flipped, draw flag
        else if(grid[counter][0] == 0 && grid[counter][2] == 1){    // ][2] == 1 means flag
            canvas.drawBitmap(flag, pos.x, pos.y, null);
        }
        // If cell has been clicked and mine exploded, draw red and black image
        // ][0] == 1 is a clear cell, ][1] == 1 means mine, ][4] == 2 means exploded mine
        else if(grid[counter][0] == 1 && grid[counter][1] == 1 && grid[counter][4] == 2){
            canvas.drawBitmap(explode, pos.x, pos.y, null);
        }
        // If the cell is uncovered upon loss, but mine was not exploded, show black and white image
        // ][0] == 1 is a clear cell, ][1] == 1 means mine, ][4] == 2 means unexploded mine
        else if(grid[counter][0] == 1 && grid[counter][1] == 1 && grid[counter][4] != 2){
            canvas.drawBitmap(bmb, pos.x, pos.y, null);
        }
        // If the cell contains a number, draw it
        else if(grid[counter][1] == 2){                             // ][1] == 2 means number
            String val = Integer.toString(grid[counter][3]);        // ][3] is the number value
            canvas.drawText(val, pos.x + (width / 7f), pos.y + (height / 1.30f), paint);
        }
    }

    // Makes clickable area for each cell on the board
    public int isCellClicked(Point point, int[][] grid, int index, int flagMode){
        if(point.x >= pos.x && point.y >= pos.y && point.x <= pos.x + width
            && point.y <= pos.y + height){

            // If flagMode is on, a flag will be place on the cell
            if(flagMode == 1){
                grid[index][2] ^= 1; // Allows for toggle of flag on or off
            }
            // If the cell contains a mine, clear the cell cover and explode the mine
            else if(grid[index][2] != 1 && grid[index][1] == 1){
                grid[index][0] = 1; // ][0] == 1 means cell cover off
                grid[index][4] = 2; // ][4] == 2 means exploded mine
                selectedPaint = 1;
                return 3;           // return of 3 means game lost
            }
            // If the cell doesn't have a mine, clear it
            else if(grid[index][2] != 1){
                grid[index][0] = 1;
                selectedPaint = 1;
                return 2;           // return of 2 signifies an empty cell
            }
        }
        return 0;
    }
}
