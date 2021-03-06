package com.example.monjuri.planner;


import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;

/*import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlFieldSet;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;*/

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements Date.OnFragmentInteractionListener, Edit_Add.OnFragmentInteractionListener{
    private CalendarView dates;
    private DatePicker oldDates;
    SQLiteDatabase db;
    Timer notifs;
    boolean backClicked = false;
    Button oldDatesSelect;
    String OSIS, PASSWD;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dates = (CalendarView) findViewById(R.id.cal);
        oldDates = (DatePicker) findViewById(R.id.oldDates);
        oldDatesSelect = (Button) findViewById(R.id.oldDatesSelect);
        oldDatesSelect.setBackgroundColor(Color.rgb(0,128,255));
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            oldDates.setVisibility(View.GONE);
            oldDatesSelect.setVisibility(View.GONE);
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
        }
        else {
            dates.setVisibility(View.GONE);
            oldDatesSelect.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v){
                    oldDates.setVisibility(View.GONE);
                    oldDatesSelect.setVisibility(View.GONE);
                    Fragment myFragment = new Date();
                    int year = oldDates.getYear();
                    int month = oldDates.getMonth();
                    int dayOfMonth = oldDates.getDayOfMonth();
                    Bundle args = new Bundle();
                    args.putInt("year", year);
                    args.putInt("month", month);
                    args.putInt("day", dayOfMonth);
                    myFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction().replace(R.id.activity_main, myFragment).addToBackStack("Main").commit();
                }
            });

        }

        db=openOrCreateDatabase("CalendarDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS calendar (event_index INTEGER PRIMARY KEY AUTOINCREMENT, event_name VARCHAR, category VARCHAR, color VARCHAR, month int, day int, year int, hour int, minute int, notified int);");
        //db.execSQL("DROP TABLE IF EXISTS calendar");
        //db.delete("calendar",null,null);
        enableStrictMode();
        getDOEEvents();
        getFederalHolidays();
        startTimer();
        //printBXEvents();
        //connectToPP();
    }

    public void printBXEvents(){
        Document doc = null;
        try {
            int month = Calendar.MONTH - 1;
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            doc = Jsoup.connect("http://bxscience.edu/apps/events2/view_calendar.jsp?id=0&m="+month+"&y="+year).get();

        } catch (IOException e) {

            e.printStackTrace();
        }
        for (Element table : doc.select("table")) {
            for (Element row : table.select("tr")) {
                Elements tds = row.select("td");
                if (tds.size() > 0) {
                    for (int i = 0; i < 6; i++)
                        System.out.println(tds.get(i).text());
                }
            }
        }
    }

    /*public void connectToPP(){
        String strURL = "https://pupilpath.com/homework";
        String strUserId = "";   //enter user name
        String strPasword = ""; // enter password

        String authString = strUserId + ":" + strPasword;

        //String encodedString =
                //new String( Base64.encode(authString.getBytes(), Base64.DEFAULT) );
        OSIS = strUserId;
        PASSWD = strPasword;
        try {
            siftHTML(getRawData());
        }
        catch(Exception e){

        }
    }*/

    /*public String getRawData() {
        System.setProperty("webdriver.gecko.driver", "geckodriver");
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("general.useragent.override", "Mozilla/5.0 (X11; Linux x86_64; rv:48.0) Gecko/20100101 Firefox/48.0"); //set user-agent
        WebDriver driver = new FirefoxDriver(profile);
        try {
            driver.get("http://www.pupilpath.com");
            driver.navigate().to("https://pupilpath.skedula.com/redirectToAuth.aspx");
            driver.findElement(By.xpath("/html/body/div/div[2]/div/div[2]/div[1]/form/input[3]")).sendKeys(OSIS);
            driver.findElement(By.xpath("/html/body/div/div[2]/div/div[2]/div[1]/form/input[4]")).sendKeys(PASSWD);
            driver.findElement(By.xpath("/html/body/div/div[2]/div/div[2]/div[1]/form/button")).click();
            Thread.sleep(2000);
            driver.findElement(By.xpath("/html/body/div[1]/div[2]/div[3]/div[2]/div/button")).click();
            Thread.sleep(2000); //allowing page to load
            String rawHTML = driver.getPageSource();
            driver.close();
            driver.quit();
            return (rawHTML); //page html
        } catch (Exception e) {
            driver.close();
            driver.quit();
            System.out.println(e);
            return ("Error");
        }
    }*/

    /*public String getRawData() throws FailingHttpStatusCodeException, MalformedURLException, InterruptedException {
        final WebClient driver = new WebClient(BrowserVersion.FIREFOX_45);
        try {
            driver.getOptions().setRedirectEnabled(true);
            driver.getOptions().setJavaScriptEnabled(true);
            driver.getOptions().setUseInsecureSSL(true);
            final HtmlPage page2 = driver.getPage("https://pupilpath.skedula.com/redirectToAuth.aspx");
            final HtmlForm form = page2.getForms().get(0);

            final HtmlTextInput username = form.getInputByName("user[username]");
            final HtmlPasswordInput password = form.getInputByName("user[password]");
            final HtmlButton button = (HtmlButton) form.getElementsByTagName("button").get(0);
            username.setValueAttribute(OSIS);
            password.setValueAttribute(PASSWD);
            final HtmlPage page3 = button.click();
            final HtmlButton button2 = (HtmlButton) page3.getElementsByIdAndOrName("loginSKD").get(0);
            Thread.sleep(1000); //page loading
            final HtmlPage page4 = button2.click();
            Thread.sleep(1000); //page loading
            String rawHTML = page4.asXml();
            driver.close();
            return rawHTML;
        } catch (Exception e) {
            driver.close();
            return ("Error");
        }
    }

    public void siftHTML(String rawHTML) {
        double sum = 0;
        Document parsedHTML = Jsoup.parse(rawHTML);
        LinkedList<String> classes = new LinkedList<String>();
        LinkedList<String> grades = new LinkedList<String>();
        for (Element row : parsedHTML.getElementById("progress-card").select("tbody").select("tr")) {
            if((!(row.getElementsByTag("td").get(1).text().contains("LUNCH")) && !(row.getElementsByTag("td").get(1).text().contains("EDUCATION")))) {
                classes.add(row.getElementsByTag("td").get(1).text()); //index 1 is the name of the course
                grades.add(row.getElementsByTag("td").get(4).text().substring(4)); //index 4 is the average
            }
        }
        for (int i=0; i<=5;i++) {
            System.out.print(classes.get(i) + ": ");
            System.out.println(grades.get(i));
        }
        for (String i : grades) {
            sum += Double.parseDouble(i);
        }
        System.out.println("Average = " + sum/grades.size());
    }*/


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
        boolean toBeClosed = false;
        if (!db.isOpen()) {
            db = openOrCreateDatabase("CalendarDB", Context.MODE_PRIVATE, null);
            toBeClosed = true;
        }
        //Push notifications 3 hours prior to event
        GregorianCalendar notifDate = new GregorianCalendar();
        notifDate.add(Calendar.HOUR,3);
        GregorianCalendar now = new GregorianCalendar();
        GregorianCalendar expiredDate = new GregorianCalendar();
        expiredDate.add(Calendar.HOUR,-1);

        Cursor c = db.rawQuery("SELECT * FROM calendar",null);
        while (c.moveToNext()) {
            //Check if date of event is before notifDate but after now
            GregorianCalendar event_time = new GregorianCalendar(c.getInt(c.getColumnIndex("year")),c.getInt(c.getColumnIndex("month"))-1,c.getInt(c.getColumnIndex("day")),
                    c.getInt(c.getColumnIndex("hour")),c.getInt(c.getColumnIndex("minute")));
            if (event_time.before(expiredDate))
                continue;
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
        if (toBeClosed)
            db.close();

    }

    public void getFederalHolidays() {
        String[] holidays = {"Valentine's Day", "Christmas Eve", "Christmas Day","New Years Eve", "New Years Day", "Groundhog Day",
        "Saint Patrick's Day","April Fools","Earth Day","Flag Day","Independence Day","Halloween","Veterans Day"};
        int[] holidays_dates = {2,14,12,24,12,25,12,31,1,1,2,2,3,17,4,1,4,22,6,14,7,4,10,31,11,11};
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int finalYear = currentYear + 10;
        for (int i = 0; i < holidays.length; i++) {
            Cursor c = db.rawQuery("SELECT * FROM calendar WHERE event_name = \""+holidays[i]+"\" AND year = "+finalYear, null);
            if (!c.moveToFirst())
                for (int j = currentYear; j <= finalYear; j++) {
                    ContentValues cv = new ContentValues();
                    cv.put("event_name", holidays[i]);
                    cv.put("category", "Holiday");
                    cv.put("color", "Black");
                    cv.put("month", holidays_dates[2*i]);
                    cv.put("day", holidays_dates[2*i+1]);
                    cv.put("year", j);
                    cv.put("hour", 0);
                    cv.put("minute", 0);
                    cv.put("notified",0);
                    db.insert("calendar",null,cv);
                }
        }

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    public void onStop(){
        super.onStop();
        db.close();
        //notifs.cancel();
        //notifs = null;

    }
    public void onStart(){
        super.onStart();
        db = openOrCreateDatabase("CalendarDB", Context.MODE_PRIVATE, null);
        //startTimer();
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
        if (android.os.Build.VERSION.SDK_INT >= 23)
            dates.setVisibility(View.VISIBLE);
        else{
            oldDatesSelect.setVisibility(View.VISIBLE);
            oldDates.setVisibility(View.VISIBLE);
        }
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
