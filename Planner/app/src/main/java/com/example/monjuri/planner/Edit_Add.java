package com.example.monjuri.planner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Edit_Add.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Edit_Add#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Edit_Add extends Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Spinner category, color;
    TimePicker time;
    EditText name;
    Button done, del;
    Bundle ar = null;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Edit_Add() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Edit_Add.
     */
    // TODO: Rename and change types and number of parameters
    public static Edit_Add newInstance(String param1, String param2) {
        Edit_Add fragment = new Edit_Add();
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
        View view = inflater.inflate(R.layout.fragment_edit__add, container, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener( new View.OnKeyListener()
        {
            @Override
            public boolean onKey( View v, int keyCode, KeyEvent event )
            {
                if( keyCode == KeyEvent.KEYCODE_BACK )
                {
                    ((MainActivity)getActivity()).backClicked = true;
                    if (getFragmentManager().getBackStackEntryCount()>0) {
                        getFragmentManager().popBackStack();
                    }
                    backPress();
                    return true;
                }
                return false;
            }
        } );
        category = (Spinner)view.findViewById(R.id.category);
        color = (Spinner)view.findViewById(R.id.color);
        time = (TimePicker)view.findViewById(R.id.time);
        name = (EditText)view.findViewById(R.id.name);
        if (getArguments().getBoolean("edit")){
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                time.setHour(getArguments().getInt("hour"));
                time.setMinute(getArguments().getInt("minute"));
            }
            else {
                time.setCurrentHour(getArguments().getInt("hour"));
                time.setCurrentMinute(getArguments().getInt("minute"));
            }
            name.setText(getArguments().getString("name"));
            String[] cats = getResources().getStringArray(R.array.categories);
            int i = 0;
            int j = 0;
            for (; i < cats.length; i++)
                if (cats[i].equals(getArguments().getString("category")))
                    break;
            String[] cols = getResources().getStringArray(R.array.colors);
            for (; j < cols.length; j++)
                if (cols[j].equals(getArguments().getString("color")))
                    break;

            category.setSelection(i);//Get array index from arrays.xml
            color.setSelection(j);

        }
        done = (Button)view.findViewById(R.id.done);
        done.setBackgroundColor(Color.GREEN);
        done.setBackgroundColor(Color.rgb(0,128,255));
        done.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                String cat_choice = category.getSelectedItem().toString();
                String col_choice = color.getSelectedItem().toString();
                String name_choice = name.getText().toString();
                int month = getArguments().getInt("month")+1;
                int day = getArguments().getInt("day");
                int year = getArguments().getInt("year");
                int hour;
                int minute;
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    hour = time.getHour();
                    minute = time.getMinute();
                }
                else {
                    hour = time.getCurrentHour();
                    minute = time.getCurrentMinute();
                }
                if (cat_choice.equals("Select") || col_choice.equals("Select") || name_choice.trim().equals("") || name_choice.contains("\""))
                    Toast.makeText(getContext(), "Invalid event", Toast.LENGTH_SHORT).show();
                else if (cat_choice.equals("Assessment") && name_choice.split("on").length != 2) {
                    Toast.makeText(getContext(), "Wrong number of 'on' statements\nShould be like 'Subject on topic 1, topic 2'", Toast.LENGTH_SHORT).show();
                }
                else{
                    SQLiteDatabase db = ((MainActivity) getActivity()).getDb();
                    if (getArguments().getBoolean("edit")){
                        //Name, color, category, time
                        if (!name_choice.equals(getArguments().getString("name")) || !col_choice.equals(getArguments().getString("color")) || !cat_choice.equals(getArguments().getString("category")) || hour != getArguments().getInt("hour") || minute != getArguments().getInt("minute")) {
                            db.delete("calendar","event_name=\""+getArguments().getString("name")+"\"",null);
                        }
                        else {
                            Toast.makeText(getContext(), "Event already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    else {
                        Cursor c = db.rawQuery("SELECT * FROM calendar WHERE event_name=? AND day=?", new String[]{name_choice, String.valueOf(day)});
                        if (c.moveToFirst()) {
                            Toast.makeText(getContext(), "Duplicate event", Toast.LENGTH_SHORT).show();
                            c.close();
                            return;
                        }
                        c.close();
                    }
                    ContentValues cv = new ContentValues();
                    cv.put("event_index", ((MainActivity)getActivity()).count++);
                    cv.put("event_name", name_choice);
                    cv.put("category", cat_choice);
                    cv.put("color", col_choice);
                    cv.put("month", month);
                    cv.put("day", day);
                    cv.put("year", year);
                    cv.put("hour", hour);
                    cv.put("minute", minute);
                    cv.put("notified", 0);
                    db.insert("calendar",null, cv);
                    if (getFragmentManager().getBackStackEntryCount()>0) {
                        getFragmentManager().popBackStack();
                    }
                    backPress();
                    Toast.makeText(getContext(), "Event entered", Toast.LENGTH_SHORT).show();

                }
                ((MainActivity)getActivity()).pushNotifications();
                    //add to database
            }
        });
        del = (Button)view.findViewById(R.id.Delete);
        if (!getArguments().getBoolean("edit"))
            del.setVisibility(View.INVISIBLE);
        del.setBackgroundColor(Color.rgb(102,178,255));
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = ((MainActivity)getActivity()).getDb();
                if (getArguments().getBoolean("edit")) {
                    db.delete("calendar", "event_name=\""+getArguments().getString("name")+"\"",null);
                    Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                }
                if (getFragmentManager().getBackStackEntryCount()>0) {
                        getFragmentManager().popBackStack();
                }
                backPress();
            }
        });
        return view;
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void backPress(){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        Date date = new Date();
        Bundle args = new Bundle();
        args.putInt("year", getArguments().getInt("year"));
        args.putInt("month", getArguments().getInt("month"));
        args.putInt("day", getArguments().getInt("day"));
        date.setArguments(args);
        ft.replace(R.id.date, date).commit();
    }

    public void clearButtons(){
        category.setVisibility(View.GONE);
        color.setVisibility(View.GONE);
        time.setVisibility(View.GONE);
        name.setVisibility(View.GONE);
        done.setVisibility(View.GONE);

    }
    public void closeFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
        ((MainActivity)getActivity()).reload();
    }

    public void onStop(){
        super.onStop();
        ar = new Bundle();
        ar.putString("name", name.getText().toString());
        ar.putString("category", category.getSelectedItem().toString());
        ar.putString("color", color.getSelectedItem().toString());
        ar.putInt("day", getArguments().getInt("day"));
        ar.putInt("year",getArguments().getInt("year"));
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            ar.putInt("hour",time.getHour());
            ar.putInt("minute",time.getMinute());
        }
        else {
            ar.putInt("hour",time.getCurrentHour());
            ar.putInt("minute",time.getCurrentMinute());
        }

    }

    public void onStart(){
        super.onStart();
        if (ar == null)
            return;
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            time.setHour(ar.getInt("hour"));
            time.setMinute(ar.getInt("minute"));
        }
        else {
            time.setCurrentHour(ar.getInt("hour"));
            time.setCurrentMinute(ar.getInt("minute"));
        }
        name.setText(ar.getString("name"));
        String[] cats = getResources().getStringArray(R.array.categories);
        int i = 0;
        int j = 0;
        for (; i < cats.length; i++)
            if (cats[i].equals(ar.getString("category")))
                break;
        String[] cols = getResources().getStringArray(R.array.colors);
        for (; j < cols.length; j++)
            if (cols[j].equals(ar.getString("color")))
                break;

        category.setSelection(i);//Get array index from arrays.xml
        color.setSelection(j);
        ar = null;

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
