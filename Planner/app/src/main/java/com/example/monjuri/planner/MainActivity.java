package com.example.monjuri.planner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;

import static android.drm.DrmStore.DrmObjectType.CONTENT;

public class MainActivity extends AppCompatActivity implements Date.OnFragmentInteractionListener{
    private CalendarView dates;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dates = (CalendarView)findViewById(R.id.cal);
        dates.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                Fragment myFragment = new Date();
                Bundle args = new Bundle();
                args.putInt("year", year);
                args.putInt("month", month);
                args.putInt("day", dayOfMonth);
                myFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main, myFragment).commit();

            }
        });
        db=openOrCreateDatabase("CalendarDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS calendar(index int, name VARCHAR, category VARCHAR, color VARCHAR, time int);");




    }
    public void onFragmentInteractionListener(Uri uri){

    }

    public void onFragmentInteraction(Uri uri){

    }
}
