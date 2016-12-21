package com.example.monjuri.planner;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;

public class MainActivity extends AppCompatActivity implements Date.OnFragmentInteractionListener{
    private CalendarView dates;

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



    }
    public void onFragmentInteractionListener(Uri uri){

    }

    public void onFragmentInteraction(Uri uri){

    }
}
