package finalproject.mrussell8.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.WindowManager;

import com.google.gson.Gson;

import java.util.StringTokenizer;

public class GameActivity extends AppCompatActivity {
    private GameView gameView;

    // Flag to determine if old board should be loaded or a new one created
    public static int gameOver = 1;

    // Function to save board and game data on pause
    public void saveData(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("shared preferences",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Store values to prevent game breaking on reload
        editor.putString("gameOver", String.valueOf(gameOver));
        editor.putString("minesLeft", String.valueOf(Board.minesLeft));
        editor.putString("numMines", String.valueOf(MainActivity.dropNum));

        // Store grid in json format
        Gson gson = new Gson();
        String json = gson.toJson(MainActivity.grid);
        editor.putString("grid", json);
        editor.apply();
    }

    // Function to reload data on returning to the app
    private void loadData(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("shared preferences",
                Context.MODE_PRIVATE);

        String over = sharedPreferences.getString("gameOver", String.valueOf(1));
        gameOver = Integer.parseInt(over);

        if(gameOver == 1){
            gameOver = 0;
            Board.minesLeft = MainActivity.dropNum;
        } else {
            // Reload important values and game grid
            String left = sharedPreferences.getString("minesLeft", String.valueOf(0));
            Board.minesLeft = Integer.parseInt(left);
            String num = sharedPreferences.getString("numMines", String.valueOf(5));
            MainActivity.dropNum = Integer.parseInt(num);
            String json = sharedPreferences.getString("grid", null);
            json = json.replace("[", "");
            json = json.replace("]", "");
            StringTokenizer tokens = new StringTokenizer(json, ",");
            for(int i = 0; i < 100; i++) {
                for(int j = 0; j < 5; j++){
                    MainActivity.grid[i][j] = Integer.parseInt(tokens.nextToken());
                }
            }
        }
        setContentView(gameView);
    }

    // On reload of the app, check if data should be reloaded by calling loadData()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        gameView = new GameView(this, screenSize);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadData();
    }

    // If game is paused in any way, store data
    @Override
    protected void onPause(){
        super.onPause();
        saveData();
        gameView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameView.resume();
    }
}