package com.example.monjuri.planner;


import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CalendarView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements Date.OnFragmentInteractionListener, Edit_Add.OnFragmentInteractionListener{
    private CalendarView dates;
    SQLiteDatabase db;
    int count;
    Timer notifs;
    boolean backClicked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dates = (CalendarView)findViewById(R.id.cal);
        dates.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                dates.setVisibility(View.GONE);
                Fragment myFragment = new Date();
                Bundle args = new Bundle();
                args.putInt("year", year);
                args.putInt("month", month);
                args.putInt("day", dayOfMonth);
                myFragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.activity_main, myFragment).addToBackStack("Main").commit();

            }
        });
        db=openOrCreateDatabase("CalendarDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS calendar (event_index int, event_name VARCHAR, category VARCHAR, color VARCHAR, month int, day int, year int, hour int, minute int, notified int);");
        //db.execSQL("DROP TABLE IF EXISTS calendar");
        //db.delete("calendar",null,null);
        enableStrictMode();
        getDOEEvents();
        getFederalHolidays();
        startTimer();
    }

    public void startTimer(){
        notifs = new Timer();
        notifs.schedule(new TimerTask() {
            @Override
            public void run() {
                pushNotifications();
            }
        },0,60000);
        pushNotifications();
    }

    public void pushNotifications(){
        if (!db.isOpen())
            return;
        //Push notifications 3 hours prior to event
        GregorianCalendar notifDate = new GregorianCalendar();
        notifDate.add(Calendar.HOUR,3);
        GregorianCalendar now = new GregorianCalendar();

        Cursor c = db.rawQuery("SELECT * FROM calendar",null);
        while (c.moveToNext()) {
            //Check if date of event is before notifDate but after now
            GregorianCalendar event_time = new GregorianCalendar(c.getInt(c.getColumnIndex("year")),c.getInt(c.getColumnIndex("month"))-1,c.getInt(c.getColumnIndex("day")),
                    c.getInt(c.getColumnIndex("hour")),c.getInt(c.getColumnIndex("minute")));
            if (event_time.before(notifDate)) {
                NotificationCompat.Builder mBuilder;
                if (c.getInt(c.getColumnIndex("notified")) == 0)
                     mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.notif)
                                .setContentTitle("Upcoming event in less than 3 hours")
                                .setContentText(c.getString(c.getColumnIndex("event_name")));
                else if (event_time.before(now) && c.getInt(c.getColumnIndex("notified")) == 1)
                    mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.notif)
                                    .setContentTitle("Event now")
                                    .setContentText(c.getString(c.getColumnIndex("event_name")));
                else
                    continue;
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(0, mBuilder.build());
                ContentValues cv = new ContentValues();
                cv.put("event_index", c.getInt(c.getColumnIndex("event_index")));
                cv.put("event_name", c.getString(c.getColumnIndex("event_name")));
                cv.put("category", c.getString(c.getColumnIndex("category")));
                cv.put("color", c.getString(c.getColumnIndex("color")));
                cv.put("month", c.getInt(c.getColumnIndex("month")));
                cv.put("day", c.getInt(c.getColumnIndex("day")));
                cv.put("year", c.getInt(c.getColumnIndex("year")));
                cv.put("hour", c.getInt(c.getColumnIndex("hour")));
                cv.put("minute", c.getInt(c.getColumnIndex("minute")));
                cv.put("notified",c.getInt(c.getColumnIndex("notified"))+1);
                db.update("calendar",cv,"event_index="+c.getInt(c.getColumnIndex("event_index")),null);
            }
        }
        c.close();

    }

    public void getFederalHolidays() {

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    public void onStop(){
        super.onStop();
        db.close();
        notifs.cancel();
        notifs = null;

    }
    public void onStart(){
        super.onStart();
        db = openOrCreateDatabase("CalendarDB", Context.MODE_PRIVATE, null);
        startTimer();
    }

    public void getDOEEvents(){
        String li;
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line = "";

        try {
            url = new URL("http://schools.nyc.gov/calendar/default.htm?start_date=1%2f10%2f2017");
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((li = br.readLine()) != null) {
                line += li + "\n";
            }
            int year1 = Integer.parseInt(line.substring(line.indexOf("September") + 10, line.indexOf("September") + 14));
            int year2 = year1 + 1;
            line = line.substring(line.indexOf("September"));
            line = stripHTML(line);
            line = line.substring(14);
            line = line.substring(0, line.indexOf("/*"));
            String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] lines = line.split("\n");
            ArrayList<String> events = new ArrayList<String>();
            for (String l: lines)
                if (!l.equals(""))
                    events.add(l);
            //for (String e: events)
                //System.out.println("****" + e);

            for (int i = 0; i < events.size(); i+=2){
                for (int j = 0; j < months.length; j++)
                    if (events.get(i).equals(months[j]) || events.get(i).equals(months[j] + " " + year2)) {
                        i ++;
                        break;
                    }
                int month = -1;
                int[] days = new int[25];
                String description;
                for (int j = 0; j < months.length; j++)
                    if (events.get(i).contains(months[j])) {
                        month = j;
                        if (events.get(i).substring(months[j].length() + 1).contains("-")) {
                            int start = Integer.parseInt(events.get(i).substring(months[j].length() + 1, events.get(i).indexOf("-")));
                            int end = Integer.parseInt(events.get(i).substring(events.get(i).indexOf("-") + 1));
                            for (int k = 0; k <= end - start; k++)
                                days[k] = start + k;
                        }
                        else
                            days[0] = Integer.parseInt(events.get(i).substring(months[j].length() + 1));
                        break;
                    }
                if (month == -1)
                    break;
                description = events.get(i + 1);

                int k = 0;
                //while (days[k] != 0)
                    //System.out.println(description + " on month " + month + " and day " + days[k++]);
                Cursor c = db.rawQuery("SELECT * FROM calendar WHERE event_name=? AND day=?", new String[]{description, String.valueOf(days[0])});
                while (days[k] != 0) {
                    ContentValues cv = new ContentValues();
                    cv.put("event_index", count++);
                    cv.put("event_name", description);
                    cv.put("category", "School");
                    cv.put("color", "Black");
                    cv.put("month", month+1);
                    cv.put("day", days[k]);
                    if (month >= 8)
                        cv.put("year", year1);
                    else
                        cv.put("year", year2);
                    cv.put("hour", 0);
                    cv.put("minute", 0);
                    cv.put("notified",0);
                    if (c.getCount() == 0)
                        db.insert("calendar", null, cv);
                    k++;
                }
                c.close();

            }


        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }

    }

    public void enableStrictMode(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public String stripHTML(String text){
        return text.replaceAll("\\<.*?>","");
    }

    public void reload(){
        dates.setVisibility(View.VISIBLE);
        backClicked = false;
    }
    public SQLiteDatabase getDb(){
        return db;
    }
    public void onFragmentInteractionListener(Uri uri){

    }

    public void onFragmentInteraction(Uri uri){

    }
}
