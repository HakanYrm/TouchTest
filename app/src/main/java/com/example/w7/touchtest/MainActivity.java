package com.example.w7.touchtest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.icu.text.DateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import static android.content.ContentValues.TAG;
import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends Activity {

    private static final boolean mDebug = false;
    private static final int SUCCESS = 1;
    private static final int FAIL = 0;
    private ArrayList<Rect> rectList;
    public TPTestView tpView;
    public int mDisplayWidth;
    public int mDisplayHeight;
    public int numX;
    public int numY;
    public String result;
    public static final String SHOW_RESULT = "com.example.w7.touchtest.ResultsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        int uiOptions = fullScreen();
        decorView.setSystemUiVisibility(uiOptions);
        initTouchPanelTest();
        tpView = new TPTestView(this);
        setContentView(tpView);
        writeToFile(this,"        " + logTime() + "         :" + result + "\n" );

    }

    private void initTouchPanelTest(){

        Display myDisplay = getWindowManager().getDefaultDisplay();
        Point size = new Point(); myDisplay.getSize(size);
        mDisplayWidth =size.x;
        if (mDisplayWidth == 720) mDisplayHeight = 1280;
                else if (mDisplayWidth==1080) mDisplayHeight = 1920;
                else mDisplayHeight = size.y;

        numY = mDisplayHeight/100; //her kare 100*100 pixel
        numX = mDisplayWidth/100;

        rectList = new ArrayList<Rect>(numX * numY);

        for(int i = 0; i < numX; i++){
            for(int j = 0; j < numY; j++){
                Rect r = new Rect(i*mDisplayWidth/numX,j*mDisplayHeight/numY,mDisplayWidth/numX,mDisplayHeight/numY);
                rectList.add(r);
            }
        }
    }

    private void viewResult(View view){
        Intent intent = new Intent(this, ResultsActivity.class);
        String message = readFromFile(this);
        intent.putExtra(SHOW_RESULT, message);
        startActivity(intent);
    }

    private class TPTestView extends View {
        private Bitmap bitmap;
        private Canvas canvas;
        private Paint paint;
        private Rect tempRect;

        public TPTestView(Context context){
            super(context);
            paint=new Paint(Paint.DITHER_FLAG);
            bitmap = Bitmap.createBitmap(mDisplayWidth, mDisplayHeight, Bitmap.Config.ARGB_8888);
            canvas=new Canvas();
            canvas.setBitmap(bitmap);

            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
            paint.setColor(Color.WHITE);
            paint.setAntiAlias(true);

            Iterator<Rect> iterator = rectList.iterator();
            while(iterator.hasNext()){
                Rect rect = iterator.next();
                canvas.drawRect(rect.x+1, rect.y+1, rect.x+rect.dx-1, rect.y+rect.dy-1, paint);
            }
            invalidate();
            paint.setColor(Color.BLUE);
        }

        protected void onDraw(Canvas canvas){
            canvas.drawBitmap(bitmap,0,0,null);
        }

        public boolean onTouchEvent(MotionEvent event){
            int action = event.getAction();
            float x = event.getX();
            float y = event.getY();
            if(mDebug) Log.v(TAG, "onTouchEvent" + " x=" + x + " y=" + y);

            if( action== MotionEvent.ACTION_DOWN || action== MotionEvent.ACTION_MOVE ){
                if (rectList.size() == 0) {
                    doAfterTest();
                }
                boolean bNeedFindRect = false;
                if(tempRect==null){
                    bNeedFindRect = true;
                }
                else{
                    if(!tempRect.isInRect((int)x, (int)y)){
                        bNeedFindRect = true;
                    }else {
                        bNeedFindRect = false;
                    }
                }
                if(mDebug) Log.v(TAG, "onTouchEvent" + " bNeedFindRect=" + bNeedFindRect);
                if(bNeedFindRect){
                    Iterator<Rect> iterator = rectList.iterator();
                    while(iterator.hasNext()){
                        Rect rect = iterator.next();
                        if(rect.isInRect((int)x, (int)y)){
                            tempRect = rect;
                            canvas.drawRect(rect.x, rect.y, rect.x+rect.dx, rect.y+rect.dy, paint);
                            invalidate();
                            rectList.remove(rect);
                            break;
                        }
                    }
                }
            }
            return true;
        }
    }

    private class Rect{
        int x,y,dx,dy;
        Rect(int x, int y, int dx, int dy)	{this.x = x;this.y = y;this.dx = dx;this.dy = dy;}

        public boolean isInRect(int x, int y){
            if(mDebug)
                Log.v(TAG, "isInRect" + " this.x=" + this.x + " this.y=" + this.y + " this.x+dx=" + (this.x + this.dx) + " this.y+dy=" + (this.y + this.dy));
            if(x>=this.x && (x<=this.x+this.dx) && y>=this.y && (y<=this.y+this.dy)){
                return true;
            }else{
                return false;
            }
        }
    }

/*    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case SUCCESS:
                finishTest(true);
                return true;
            case FAIL:
                finishTest(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void finishTest(boolean result){
        Bundle bundle = new Bundle();
        Intent intent = new Intent();

        bundle.putInt("test_result",result?1:0);
        intent.putExtras(bundle);
        //setResult(RESULT_OK,intent);

        ////////////PASS GÖNDERME///////////////////
        //try {
        //    TestSonuc.read_and_write(TestAdi, "PASS");
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        ////////////////////////////////////////////

        finish();

    }*/

    private void doAfterTest(){
        Bundle b = new Bundle();
        Intent intent = new Intent();
        b.putInt("test_result", 1);
        intent.putExtras(b);

      /*  setResult(RESULT_OK, intent);

        Log.e(TAG,"--------------------------------------------PROTOCOL FORMAT----------------------------------------\n");
        Log.e(TAG,"TEST NUMBER "+Test_Number+"\n");
        Log.e(TAG,"TEST NAME "+Test_Name+"\n");
        Log.e(TAG,"DATA-1 "+null+"\n");
        Log.e(TAG,"DATA-2 "+null+"\n");
        Log.e(TAG,"DATA-3 "+null+"\n");
        Log.e(TAG,"RESULT PASS"+"\n");
        Log.e(TAG,"---------------------------------TOUCH PANEL TEST COMPLETED SUCCESFULLY ---------------------------------\n");*/

        ////////////PASS GÖNDERME///////////////////
        //try {
        //    TestSonuc.read_and_write(TestAdi, "PASS");
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
        ////////////////////////////////////////////
        finish();
    }

    private void writeToFile(Context context,String string){

        String filename = "results.txt";
        File file = new File(context.getFilesDir(), filename);
        FileOutputStream outputStream = null;

        if(file.exists()){
            try {
                outputStream = new FileOutputStream(file,true);
                outputStream.write(string.getBytes());
                //outputStream.write(System.lineSeparator());
                outputStream.close();
                }catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            try {
                outputStream = openFileOutput(filename,Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("results.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString).append("\n");
                }

                inputStream.close();
                ret = stringBuilder.toString();
                //Toast.makeText(this, ret, Toast.LENGTH_LONG).show();
            }

            else Toast.makeText(this, "olmadı buuuuuu", Toast.LENGTH_LONG).show();
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ////////////FAIL GÖNDERME///////////////////
        //try {
        //TestSonuc.read_and_write(TestAdi, "FAIL");
        //} catch (IOException e) {
        //e.printStackTrace();
        //}
        ////////////////////////////////////////////
        viewResult(tpView);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

//		menu.add(0,SUCCESS,0,R.string.success);
//		menu.add(0,FAIL,0,R.string.fail);
        //menu.add(0,SUCCESS,0,R.string.success);
        //menu.add(0,FAIL,0,R.string.fail);

        return true;
    }

    public String logTime(){
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        return currentDateTimeString;
    }

    public int fullScreen(){
        int fullScreenInt = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        return fullScreenInt;
    }


}