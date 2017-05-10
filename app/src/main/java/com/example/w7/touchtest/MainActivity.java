package com.example.w7.touchtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.FileOutputStream;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
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