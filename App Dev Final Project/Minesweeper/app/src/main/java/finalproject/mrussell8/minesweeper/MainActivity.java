package finalproject.mrussell8.minesweeper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static int[][] grid = new int[100][5];       // declare grid to be used in other files
    public static int dropNum = 5;                      // initialize value of drop down
    Spinner numDropDown;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set view to be fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        dropDown();
    }

    // Dropdown function for the spinner, allows user to pick number of mines
    // Value saved in dropNum
    public void dropDown(){
        numDropDown = findViewById(R.id.numDropDown);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.numbers, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numDropDown.setAdapter(adapter);
        numDropDown.setOnItemSelectedListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        dropNum = Integer.parseInt(parent.getItemAtPosition(position).toString());
    }

    public void onNothingSelected(AdapterView<?> parent) { }

    public void onPlayClicked(View view){
        startActivity(new Intent(MainActivity.this, GameActivity.class));
    }
}