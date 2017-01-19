package com.example.monjuri.planner;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Date.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Date#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Date extends Fragment implements Edit_Add.OnFragmentInteractionListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    Button add;
    TextView today;
    LinearLayout parent;
    ScrollView scr;

    private OnFragmentInteractionListener mListener;

    public Date() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Date.
     */
    // TODO: Rename and change types and number of parameters
    public static Date newInstance(String param1, String param2) {
        Date fragment = new Date();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_date, container, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    if (!((MainActivity)getActivity()).backClicked && getFragmentManager().getBackStackEntryCount()>0) {
                        getFragmentManager().popBackStack();
                        ((MainActivity)getActivity()).reload();
                        return true;
                    }
                    else
                        ((MainActivity)getActivity()).backClicked = false;

                }
                return false;
            }
        } );
        scr = (ScrollView)view.findViewById(R.id.scroll);
        add = (Button)view.findViewById(R.id.add);
        add.setBackgroundColor(Color.rgb(0,128,255));
        add.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("year", getArguments().getInt("year"));
                args.putInt("month", getArguments().getInt("month"));
                args.putInt("day", getArguments().getInt("day"));
                args.putBoolean("edit",false);
                Edit_Add fragment = new Edit_Add();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                fragment.setArguments(args);
                ft.replace(R.id.date, fragment);
                ft.addToBackStack(null);
                ft.commit();
                clearButtons();
            }
        });
        today = (TextView)view.findViewById(R.id.current);
        int day = getArguments().getInt("day");
        int month = getArguments().getInt("month")+1;
        int year = getArguments().getInt("year");
        String today_date = month + "/" + day + "/" + year;
        today.setText(today_date);
        parent = (LinearLayout)view.findViewById(R.id.eves);
        addButtons();

        //Helpful links
        //Too many events?

        //java.awt.Desktop.getDesktop().browse(theURI);
        //Use to open any web page
        return view;


    }

    public void addButtons() {
        String ev = "";
        SQLiteDatabase db = ((MainActivity) getActivity()).getDb();
        int day = getArguments().getInt("day");
        int month = getArguments().getInt("month")+1;
        int year = getArguments().getInt("year");
        Cursor c = db.rawQuery("SELECT * FROM calendar WHERE month = ? AND day = ? AND year = ? ORDER BY year ASC, month ASC, day ASC, hour ASC, minute ASC", new String[]{String.valueOf(month), String.valueOf(day), String.valueOf(year)});
        while (c.moveToNext()) {
            Button eve = new Button(getContext());
            String minute;
            ev = c.getString(c.getColumnIndex("category")) + "\n";
            minute = c.getInt(c.getColumnIndex("minute")) < 10 ?
                    "0" + c.getString(c.getColumnIndex("minute")) : c.getString(c.getColumnIndex("minute"));
            ev += c.getString(c.getColumnIndex("event_name")) + (!(c.getString(c.getColumnIndex("category")).equals("Holiday")) ? " at " + c.getString(c.getColumnIndex("hour")) + ": " + minute : "");
            eve.setText(ev);
            eve.setTag(c.getInt(c.getColumnIndex("event_index")));
            eve.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle args = new Bundle();
                    args.putInt("year", getArguments().getInt("year"));
                    args.putInt("month", getArguments().getInt("month"));
                    args.putInt("day", getArguments().getInt("day"));
                    Cursor c = ((MainActivity) getActivity()).getDb().rawQuery("SELECT * FROM calendar WHERE event_index=" + v.getTag(), null);
                    c.moveToFirst();
                    args.putString("color", c.getString(c.getColumnIndex("color")));
                    args.putString("category", c.getString(c.getColumnIndex("category")));
                    args.putString("name", c.getString(c.getColumnIndex("event_name")));
                    args.putInt("hour", c.getInt(c.getColumnIndex("hour")));
                    args.putInt("minute", c.getInt(c.getColumnIndex("minute")));
                    args.putBoolean("edit", true);
                    c.close();
                    Edit_Add fragment = new Edit_Add();
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    fragment.setArguments(args);
                    ft.replace(R.id.date, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                    clearButtons();
                    //Put event info into edit_add
                }
            });
            eve.setBackgroundColor(Color.WHITE);
            String col = c.getString(c.getColumnIndex("color"));
            if (col.equals("Red"))
                eve.setTextColor(Color.RED);
            else if (col.equals("Yellow"))
                eve.setTextColor(Color.YELLOW);
            else if (col.equals("Blue"))
                eve.setTextColor(Color.BLUE);
            else if (col.equals("Black"))
                eve.setTextColor(Color.BLACK);
            parent.addView(eve);
            String cat = c.getString(c.getColumnIndex("category"));
            if (cat.equals("Homework")){
                TextView link = new TextView(getContext());
                link.setMovementMethod(LinkMovementMethod.getInstance());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    link.setText(Html.fromHtml("Recommended Link<br><a href='http://www.docs.google.com'>Google Docs</a>", Html.FROM_HTML_MODE_LEGACY));
                else
                    link.setText(Html.fromHtml("Recommended Link<br><a href='http://www.docs.google.com'>Google Docs</a>"));
                parent.addView(link);

            } else if (cat.equals("Appointment")) {
                TextView link = new TextView(getContext());
                link.setMovementMethod(LinkMovementMethod.getInstance());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    link.setText(Html.fromHtml("Recommended Link<br><a href='http://www.maps.google.com'>Google Maps</a>", Html.FROM_HTML_MODE_LEGACY));
                else
                    link.setText(Html.fromHtml("Recommended Link<br><a href='http://www.maps.google.com'>Google Maps</a>"));
                parent.addView(link);

            }
            else if (cat.equals("Assessment")){
                TextView link = new TextView(getContext());
                link.setMovementMethod(LinkMovementMethod.getInstance());
                String topics = c.getString(c.getColumnIndex("event_name")).substring(c.getString(c.getColumnIndex("event_name")).indexOf("on") + 2);
                String[] topicsList = topics.split(",");
                String links = "";
                for (int i = 0; i < topicsList.length; i++) {
                    links += "<a href='https://quizlet.com/subject/"+topicsList[i]+"'>Quizlet topic "+(i+1)+"<br>"+
                            "<a href='https://www.youtube.com/results?search_query="+topicsList[i]+"'>Youtube topic "+(i+1)+"</a>";
                    if (i + 1 != topicsList.length)
                        links += "<br>";
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    link.setText(Html.fromHtml("Recommended Links<br>"+links, Html.FROM_HTML_MODE_LEGACY));
                else
                    link.setText(Html.fromHtml("Recommended Link<br>"+links));
                //FOR EACH TOPIC, INCLUDE LINK
                parent.addView(link);
            }else if (cat.equals("Birthday")){
                TextView link = new TextView(getContext());
                link.setMovementMethod(LinkMovementMethod.getInstance());
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N)
                    link.setText(Html.fromHtml("Recommended Link<br><a href='http://www.amazon.com'>Amazon</a>", Html.FROM_HTML_MODE_LEGACY));
                else
                    link.setText(Html.fromHtml("Recommended Link<br><a href='http://www.amazon.com'>Amazon</a>"));
                parent.addView(link);
            }

            //Homework - Google docs, Appointments - Google maps, Assessments - Quizlet + Youtube, Birthdays - Amazon

        }
        c.close();
    }

    public void closeFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
        ((MainActivity)getActivity()).reload();
    }

    public void clearButtons(){
        add.setVisibility(View.GONE);
        today.setVisibility(View.GONE);
        parent.setVisibility(View.GONE);
        scr.setVisibility(View.GONE);
    }

    public void reload(){
        add.setVisibility(View.VISIBLE);
        today.setVisibility(View.VISIBLE);
        parent.setVisibility(View.VISIBLE);
        scr.setVisibility(View.VISIBLE);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void onFragmentInteractionListener(Uri uri){

    }

    public void onFragmentInteraction(Uri uri){

    }
}
