package com.example.w7.touchtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.FileOutputStream;
import static com.example.w7.touchtest.MyGlobals.counter;

public class MainActivity extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

    }

    public void setStartTouchtest(View view){
        Intent touchIntent = new Intent(this,TouchTest.class);
        startActivity(touchIntent);
    }

    public void setShowResultView(View view){
        Intent showResults = new Intent(this,ResultsActivity.class);
        startActivity(showResults);
    }

    public void deleteLogs(View view){
        writeToFile("");
        counter = 0;

        SharedPreferences sp = getSharedPreferences("savedCounter", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("savedCounter", counter);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void writeToFile(String string){
        String filename = "results.txt";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "All logs are deleted !!", Toast.LENGTH_SHORT).show();
    }
}