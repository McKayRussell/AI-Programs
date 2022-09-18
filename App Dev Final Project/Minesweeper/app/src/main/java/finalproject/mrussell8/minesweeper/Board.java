package finalproject.mrussell8.minesweeper;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {
    private Paint paint = new Paint();
    private final List<Cell> cellList = new ArrayList<>();
    private final int boardXPos;
    private final int cellWidth;
    private final int cellHeight;
    private final int rowVertSpace;
    private final int rowYOffSet;
    private final GameActivity gameActivity;
    private Buttons buttons;
    private GameOver gameOver;

    // Bitmaps to be drawn
    private Bitmap background;
    private Bitmap bmb;
    private Bitmap explode;
    private Bitmap flag;

    public static int minesLeft;    // Stores number of mines left
    public int gameState = 0;       // Flag to store if game is in win, loss, or play
    public int flagFlag = 0;        // Flag for if flag mode is on

    // Variables to adjust canvas and text for different screen sizes
    public static int textSize;
    public static int offsetX;
    public static int offsetY;
    public static int mineMessageX;
    public static int mineMessageY;
    public static int flagOffsetX;
    public static int flagOffsetY;
    public static int flagOffsetX2;
    public static int flagOffsetY2;
    public static int mainMenuOffsetX;
    public static int mainMenuOffsetX2;
    public static int mainMenuOffsetY;
    public static int mainMenuOffsetY2;

    public Board(Point screenSize, Resources resources, GameActivity gameActivity){
        this.gameActivity = gameActivity;

        // Initialize bitmaps
        background = BitmapFactory.decodeResource(resources, R.drawable.board);
        bmb = BitmapFactory.decodeResource(resources, R.drawable.bomb);
        explode = BitmapFactory.decodeResource(resources, R.drawable.mine);
        flag = BitmapFactory.decodeResource(resources, R.drawable.flag);

        // Alter bitmap sizes and initialize text size
        if(screenSize.y * 0.65f > screenSize.x) {
            background = Bitmap.createScaledBitmap(background, screenSize.x, (int)(screenSize.x * 1.65f), true);
            bmb = Bitmap.createScaledBitmap(bmb, (int)(screenSize.x / 13f), (int)(screenSize.x / 11f), true);
            explode = Bitmap.createScaledBitmap(explode, (int)(screenSize.x / 12.8f), (int)(screenSize.x / 10.365f), true);
            flag = Bitmap.createScaledBitmap(flag, (int)(screenSize.x / 12.8f), (int)(screenSize.x / 10.365f), true);
            textSize = 80;
        } else {
            background = Bitmap.createScaledBitmap(background, (int)(screenSize.y * 0.75f), screenSize.y, true);
            bmb = Bitmap.createScaledBitmap(bmb, (int)(screenSize.x / 26f), (int)(screenSize.x / 27f), true);
            explode = Bitmap.createScaledBitmap(explode, (int)(screenSize.x / 24f), (int)(screenSize.x / 24f), true);
            flag = Bitmap.createScaledBitmap(flag, (int)(screenSize.x / 24f), (int)(screenSize.x / 24f), true);
            textSize = 30;
        }

        // Initialize variables for cell spacing and create cells
        boardXPos = screenSize.x / 2 - background.getWidth() / 2;
        cellWidth = flag.getWidth();
        cellHeight = flag.getHeight();
        rowYOffSet = Math.round(cellHeight + background.getHeight() / 30f);
        rowVertSpace = Math.round(cellHeight + background.getHeight() / 750f);
        generateCells();

        // Set initial grid values
        initializeGrid();

        // Make GameOver and Buttons instances to allow for cover, end game text, and clickable areas
        Point tmpCoverPos = new Point();
        tmpCoverPos.x = Math.round(boardXPos * 1.85f);
        tmpCoverPos.y = Math.round(boardXPos * 2.45f);
        int tmpWidth = Math.round(cellWidth * 2.95f);
        int tmpHeight = Math.round(cellHeight * 1.25f);
        gameOver = new GameOver(tmpCoverPos, tmpWidth, tmpHeight);
        buttons = new Buttons(tmpCoverPos, tmpWidth, tmpHeight);

        // Calculate offset for end game message for given screen size
        offsetX = Math.round(boardXPos + 0 * cellWidth * 1.035f + cellWidth * 1.245f);
        offsetX = Math.round(offsetX + cellWidth * 3.15f);
        offsetY = Math.round(rowYOffSet * 1.44f + 0 * cellHeight * 1.005f + cellHeight * 1.245f);
        offsetY = Math.round(offsetY - cellHeight * 1.25f);

        // Depending on screen size, initialize variables for:
        // - mine message, displays number of miens left
        // - flag offset, allows for correct clickable space and cover placement
        if(screenSize.y * 0.65f > screenSize.x) {
            mineMessageX = Math.round(offsetX/1.35f);
            mineMessageY = Math.round(offsetY * 1.35f);
            flagOffsetX = Math.round(boardXPos + 5.9f * cellWidth * 1.035f + cellWidth * 1.245f);
            flagOffsetY = Math.round(rowYOffSet * 1.44f + 12f * cellHeight * 1.005f + cellHeight * 1.245f);
            flagOffsetX2 = flagOffsetX + flag.getWidth();
            flagOffsetY2 = flagOffsetY + flag.getHeight();
        } else {
            mineMessageX = Math.round(boardXPos * 1.8f);
            mineMessageY = Math.round(boardXPos/1.8f);
            flagOffsetX = Math.round(boardXPos * 2.285f);
            flagOffsetY = Math.round(boardXPos * 2.71f);
            flagOffsetX2 = Math.round(boardXPos * 2.438f);
            flagOffsetY2 = Math.round(boardXPos * 2.86f);
        }

        // Calculate offset for main menu cover and clickable space
        mainMenuOffsetX = Math.round(boardXPos + 3.75f * cellWidth * 1.035f + cellWidth * 1.245f);
        mainMenuOffsetY = Math.round(rowYOffSet * 1.42f + 10.75f * cellHeight * 1.005f + cellHeight * 1.245f);
        mainMenuOffsetX2 = Math.round(boardXPos + 6.25f * cellWidth * 1.035f + cellWidth * 1.245f);
        mainMenuOffsetY2 = Math.round(rowYOffSet * 1.44f + 11.5f * cellHeight * 1.005f + cellHeight * 1.245f);
    }

    // Function to generate cells using previously calculated values and store them in a list
    private void generateCells() {
        for(int r = 0; r < 10; r++){
            for(int c = 0; c < 10; c++){
                int cellX = Math.round(boardXPos + c * cellWidth * 1.035f + cellWidth * 1.245f);
                int cellY = Math.round(rowYOffSet * 1.44f + r * cellHeight * 1.005f + cellHeight * 1.245f);
                Cell tmp = new Cell(0, cellWidth, cellHeight, new Point(cellX, cellY));
                cellList.add(tmp);
            }
        }
    }

    // Function to set initial grid values: randomly places mines and calculates numbers
    public static void initializeGrid(){
        int mineAmount = MainActivity.dropNum;
        ArrayList<Integer> nums = new ArrayList<>();

        // Initialize all grid values to 0 and create list of numbers 1-100
        for(int i = 0; i < 100; i++){
            MainActivity.grid[i][0] = 0;
            MainActivity.grid[i][1] = 0;
            MainActivity.grid[i][2] = 0;
            MainActivity.grid[i][3] = 0;
            MainActivity.grid[i][4] = 0;
            nums.add(i);
        }
        Collections.shuffle(nums); // Shuffle order of list, used to randomly place mines in grid

        // With the indexes in the list now shuffled, the list can be read up until the desired..
        // .. number of mines and those indexes in the grid can be given a mine value. This also..
        // .. means number values can be calculated, which is done in this loop.
        for(int i = 0; i < mineAmount; i++) {
            // Calculate number value if mine is on leftmost side of grid
            if (nums.get(i) > 0 && nums.get(i) % 10 == 0 && nums.get(i) < 90) {
                MainActivity.grid[nums.get(i) + 1][1] = 2;
                MainActivity.grid[nums.get(i) + 1][3] += 1;

                MainActivity.grid[nums.get(i) - 10][1] = 2;
                MainActivity.grid[nums.get(i) - 10][3] += 1;

                MainActivity.grid[nums.get(i) + 11][1] = 2;
                MainActivity.grid[nums.get(i) + 11][3] += 1;

                MainActivity.grid[nums.get(i) + 10][1] = 2;
                MainActivity.grid[nums.get(i) + 10][3] += 1;

                MainActivity.grid[nums.get(i) - 9][1] = 2;
                MainActivity.grid[nums.get(i) - 9][3] += 1;
            }
            // Calculate number value if mine is on the rightmost side of the grid
            else if (nums.get(i) > 9 && (nums.get(i) + 1) % 10 == 0 && nums.get(i) < 99) {
                MainActivity.grid[nums.get(i) - 1][1] = 2;
                MainActivity.grid[nums.get(i) - 1][3] += 1;

                MainActivity.grid[nums.get(i) - 10][1] = 2;
                MainActivity.grid[nums.get(i) - 10][3] += 1;

                MainActivity.grid[nums.get(i) - 11][1] = 2;
                MainActivity.grid[nums.get(i) - 11][3] += 1;

                MainActivity.grid[nums.get(i) + 10][1] = 2;
                MainActivity.grid[nums.get(i) + 10][3] += 1;

                MainActivity.grid[nums.get(i) + 9][1] = 2;
                MainActivity.grid[nums.get(i) + 9][3] += 1;
            }
            // Calculate number value if mine is on bottom row of grid
            else if (nums.get(i) > 90 && nums.get(i) < 99) {
                MainActivity.grid[nums.get(i) - 1][1] = 2;
                MainActivity.grid[nums.get(i) - 1][3] += 1;

                MainActivity.grid[nums.get(i) + 1][1] = 2;
                MainActivity.grid[nums.get(i) + 1][3] += 1;

                MainActivity.grid[nums.get(i) - 11][1] = 2;
                MainActivity.grid[nums.get(i) - 11][3] += 1;

                MainActivity.grid[nums.get(i) - 10][1] = 2;
                MainActivity.grid[nums.get(i) - 10][3] += 1;

                MainActivity.grid[nums.get(i) - 9][1] = 2;
                MainActivity.grid[nums.get(i) - 9][3] += 1;
            }
            // Calculate number value if mine is on top row of grid
            else if (nums.get(i) > 0 && nums.get(i) < 9) {
                MainActivity.grid[nums.get(i) - 1][1] = 2;
                MainActivity.grid[nums.get(i) - 1][3] += 1;

                MainActivity.grid[nums.get(i) + 1][1] = 2;
                MainActivity.grid[nums.get(i) + 1][3] += 1;

                MainActivity.grid[nums.get(i) + 11][1] = 2;
                MainActivity.grid[nums.get(i) + 11][3] += 1;

                MainActivity.grid[nums.get(i) + 10][1] = 2;
                MainActivity.grid[nums.get(i) + 10][3] += 1;

                MainActivity.grid[nums.get(i) + 9][1] = 2;
                MainActivity.grid[nums.get(i) + 9][3] += 1;
            }
            // Calculate number value if mine is top left corner of grid
            else if (nums.get(i) == 0) {
                MainActivity.grid[nums.get(i) + 1][1] = 2;
                MainActivity.grid[nums.get(i) + 1][3] += 1;

                MainActivity.grid[nums.get(i) + 11][1] = 2;
                MainActivity.grid[nums.get(i) + 11][3] += 1;

                MainActivity.grid[nums.get(i) + 10][1] = 2;
                MainActivity.grid[nums.get(i) + 10][3] += 1;
            }
            // Calculate number value if mine is top right corner of grid
            else if (nums.get(i) == 9) {
                MainActivity.grid[nums.get(i) - 1][1] = 2;
                MainActivity.grid[nums.get(i) - 1][3] += 1;

                MainActivity.grid[nums.get(i) + 10][1] = 2;
                MainActivity.grid[nums.get(i) + 10][3] += 1;

                MainActivity.grid[nums.get(i) + 9][1] = 2;
                MainActivity.grid[nums.get(i) + 9][3] += 1;
            }
            // Calculate number value if mine is bottom left corner of grid
            else if (nums.get(i) == 90) {
                MainActivity.grid[nums.get(i) + 1][1] = 2;
                MainActivity.grid[nums.get(i) + 1][3] += 1;

                MainActivity.grid[nums.get(i) - 10][1] = 2;
                MainActivity.grid[nums.get(i) - 10][3] += 1;

                MainActivity.grid[nums.get(i) - 9][1] = 2;
                MainActivity.grid[nums.get(i) - 9][3] += 1;
            }
            // Calculate number value if mine is bottom right corner of grid
            else if (nums.get(i) == 99) {
                MainActivity.grid[nums.get(i) - 1][1] = 2;
                MainActivity.grid[nums.get(i) - 1][3] += 1;

                MainActivity.grid[nums.get(i) - 10][1] = 2;
                MainActivity.grid[nums.get(i) - 10][3] += 1;

                MainActivity.grid[nums.get(i) - 11][1] = 2;
                MainActivity.grid[nums.get(i) - 11][3] += 1;
            }
            // Calculate number value if mine is not on the outer edges of the grid
            else {
                MainActivity.grid[nums.get(i) - 1][1] = 2;
                MainActivity.grid[nums.get(i) - 1][3] += 1;

                MainActivity.grid[nums.get(i) + 1][1] = 2;
                MainActivity.grid[nums.get(i) + 1][3] += 1;

                MainActivity.grid[nums.get(i) - 11][1] = 2;
                MainActivity.grid[nums.get(i) - 11][3] += 1;

                MainActivity.grid[nums.get(i) - 10][1] = 2;
                MainActivity.grid[nums.get(i) - 10][3] += 1;

                MainActivity.grid[nums.get(i) - 9][1] = 2;
                MainActivity.grid[nums.get(i) - 9][3] += 1;

                MainActivity.grid[nums.get(i) + 11][1] = 2;
                MainActivity.grid[nums.get(i) + 11][3] += 1;

                MainActivity.grid[nums.get(i) + 10][1] = 2;
                MainActivity.grid[nums.get(i) + 10][3] += 1;

                MainActivity.grid[nums.get(i) + 9][1] = 2;
                MainActivity.grid[nums.get(i) + 9][3] += 1;
            }
        }
        // Fill in the mines, give the cell at ][1] value of 1 to denote a mine
        for(int i = 0; i < mineAmount; i++){
            MainActivity.grid[nums.get(i)][1] = 1;
        }
    }

    public void draw(Canvas canvas){
        // Draw board
        canvas.drawBitmap(background, boardXPos, 0, paint);

        // Draw cells
        int counter = 0;
        for(Cell c : cellList){
            c.draw(canvas, bmb, explode, flag, counter, MainActivity.grid);
            counter += 1;
        }
        // If game over condition is met, clear the board except for the flags
        if(gameState > 0) {
            for (int i = 0; i < 100; i++) {
                if (MainActivity.grid[i][2] != 1) { //][2] == 1 is a flag
                    MainActivity.grid[i][0] = 1;
                }
            }
        }
        // Draw current game status, game over or still playing
        gameOver.draw(canvas, gameState);

        // If flag mode is on, cover the flag image
        if(flagFlag == 1){
            Paint paint = new Paint();
            paint.setARGB(255, 185, 185, 185);
            canvas.drawRect(flagOffsetX, flagOffsetY, flagOffsetX2, flagOffsetY2, paint);
        }

        // Print current number of mines and total
        paint.setColor(Color.BLACK);
        paint.setTextSize(textSize - 10);
        canvas.drawText( minesLeft + " of " + MainActivity.dropNum + " mines left",
                mineMessageX, mineMessageY, paint);
    }

    public void onClick(int x, int y) {
        Point point = new Point(x, y);

        // If position of flag button is clicked, toggle flag mode and evaluate the flags
        if (buttons.isFlagClicked(point)) {
            flagFlag ^= 1;
            evaluateFlags();
        }

        // Process cell clicks and determine if a game is lost, if empty space is clicked..
        // .. set values to clear neighboring empty spaces
        int loseFlag = 0;
        int emptyCell = 0;
        int emptyIndex = 0;
        for (int i = 0; i < cellList.size(); i++) {
            loseFlag = cellList.get(i).isCellClicked(point, MainActivity.grid, i, flagFlag);
            if (loseFlag == 3) {
                gameState = 2;
            } else if (loseFlag == 2) {
                emptyCell = 1;
                emptyIndex = i;
            }
        }

        // If previously clicked cell is empty or a number, clear cells
        if(emptyCell == 1 && MainActivity.grid[emptyIndex][1] == 2) {
            clearCells(emptyIndex, 1);
        } else if(emptyCell == 1 && MainActivity.grid[emptyIndex][1] != 2) {
            clearCells(emptyIndex, 0);
        }

        // If the main menu button is clicked end the game activity and go to main menu
        if (gameState > 0 && buttons.isMainMenuClicked(point)) {
            Intent intent = new Intent(gameActivity, MainActivity.class);
            gameActivity.startActivity(intent);
            gameActivity.finish();
        }
    }

    // Function to determine if flags are in correct spaces
    private void evaluateFlags(){
        for(int i = 0; i < 100; i++){
            // If mine has been flagged and this flag has not already been counted, decrease..
            // .. number of mines left and mark this flag as counted
            if(MainActivity.grid[i][1] == 1 && MainActivity.grid[i][2] == 1 && MainActivity.grid[i][4] != 1){
                minesLeft -= 1;
                MainActivity.grid[i][4] = 1;
            }
            // If previously counted flag is no longer, there mark it as not counted and increase..
            // number of mines
            else if(MainActivity.grid[i][2] == 0 && MainActivity.grid[i][4] == 1){
                minesLeft += 1;
                MainActivity.grid[i][4] = 0;
            }
        }

        // If all mines have been flagged, game is won and end game state will be triggered
        if(minesLeft == 0){
            gameState = 1;      // 1 means you won!
        }
    }

    // Recursive function that clears empty cells starting with the initially clicked cell
    // thresh is used to stop clearing number cells after one has been cleared
    // If the is met, the cell is cleared and the function is called again with the new index
    public static void clearCells(int index, int thresh){
        // If cell right of clicked cell is not a mine and current index not on the rightmost side
        if(index + 1 <= 99 && (index + 1) % 10 != 0 && MainActivity.grid[index + 1][1] != 1 &&
                MainActivity.grid[index + 1][0] != 1 && thresh < 1){
            MainActivity.grid[index + 1][0] = 1;
            if(MainActivity.grid[index][1] == 2){
                clearCells(index + 1, thresh + 1);
            } else {
                clearCells(index + 1, 0);
            }
        }
        // If cell left of the clicked cell is not a mine and current index not on the leftmost side
        if(index - 1 >= 0 && (index) % 10 != 0 && MainActivity.grid[index - 1][1] != 1 &&
                MainActivity.grid[index - 1][0] != 1 && thresh < 1){
            MainActivity.grid[index - 1][0] = 1;
            if(MainActivity.grid[index][1] == 2) {
                clearCells(index - 1, thresh + 1);
            } else {
                clearCells(index - 1, 0);
            }
        }
        // If cell below the clicked cell is not a mine and not out of grid bounds
        if(index + 10 < 99 && (index + 1) % 10 != 0 && MainActivity.grid[index + 10][1] != 1 &&
                MainActivity.grid[index + 10][0] != 1 && thresh < 1){
            MainActivity.grid[index + 10][0] = 1;
            if(MainActivity.grid[index][1] == 2) {
                clearCells(index + 10, thresh + 1);
            } else {
                clearCells(index + 10, 0);
            }
        }
        // If cell above the clicked cell is not a mine and not out of grid bounds
        if(index - 10 > 0 && (index) % 10 != 0 && MainActivity.grid[index - 10][1] != 1 &&
                MainActivity.grid[index - 10][0] != 1 && thresh < 1){
            MainActivity.grid[index - 10][0] = 1;
            if(MainActivity.grid[index][1] == 2) {
                clearCells(index - 10, thresh + 1);
            } else {
                clearCells(index - 10, 0);
            }
        }
        // If cell to upper right of clicked cell is not mine and current index not on leftmost side
        if(index - 9 > 0 && index % 10 != 0 && (index-9) % 10 != 0 &&
                MainActivity.grid[index - 9][1] != 1 && MainActivity.grid[index - 9][0] != 1 && thresh < 1) {
            MainActivity.grid[index - 9][0] = 1;
            if(MainActivity.grid[index][1] == 2) {
                clearCells(index - 9, thresh + 1);
            } else {
                clearCells(index - 9, 0);
            }
        }
        // If cell to upper left of clicked cell is not mine and current index not on leftmost side
        if(index - 11 > 0 && index % 10 != 0 && MainActivity.grid[index - 11][1] != 1 &&
                MainActivity.grid[index - 11][0] != 1 && thresh < 1) {
            MainActivity.grid[index - 11][0] = 1;
            if(MainActivity.grid[index][1] == 2) {
                clearCells(index - 11, thresh + 1);
            } else {
                clearCells(index - 11, 0);
            }
        }
        // If lower left of clicked cell is not mine and current index not on rightmost side
        if(index + 9 < 99 && (index + 1) % 10 != 0 && (index+9+1) % 10 != 0 &&
                MainActivity.grid[index + 9][1] != 1 && MainActivity.grid[index + 9][0] != 1 && thresh < 1) {
            MainActivity.grid[index + 9][0] = 1;
            if(MainActivity.grid[index][1] == 2) {
                clearCells(index + 9, thresh + 1);
            } else {
                clearCells(index + 9, 0);
            }
        }
        // If lower right of clicked cell is not mine and current index not on rightmost side
        if(index + 11 < 99 && (index + 1) % 10 != 0 && MainActivity.grid[index + 11][1] != 1 &&
                MainActivity.grid[index + 11][0] != 1 && thresh < 1) {
            MainActivity.grid[index + 11][0] = 1;
            if(MainActivity.grid[index][1] == 2) {
                clearCells(index + 11, thresh + 1);
            } else {
                clearCells(index + 11, 0);
            }
        }
    }
}
