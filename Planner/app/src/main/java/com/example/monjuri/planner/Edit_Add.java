package com.example.monjuri.planner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
    Button done, back;


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
        category = (Spinner)view.findViewById(R.id.category);
        color = (Spinner)view.findViewById(R.id.color);
        time = (TimePicker)view.findViewById(R.id.time);
        name = (EditText)view.findViewById(R.id.name);
        done = (Button)view.findViewById(R.id.done);
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
                if (cat_choice.equals("Select") || col_choice.equals("Select") || name_choice.trim().equals(""))
                    Toast.makeText(getContext(), "Invalid event", Toast.LENGTH_SHORT).show();
                else{
                    SQLiteDatabase db = ((MainActivity) getActivity()).getDb();
                    Cursor c = db.rawQuery("SELECT * FROM calendar WHERE event_name=? AND day=?", new String[]{name_choice, String.valueOf(day)});
                    if (c.moveToFirst()) {
                        Toast.makeText(getContext(), "Duplicate event", Toast.LENGTH_SHORT).show();
                        return;
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
                    db.insert("calendar",null, cv);
                    if (getFragmentManager().getBackStackEntryCount()>0) {
                        getFragmentManager().popBackStack();
                    }
                    backPress();
                    Toast.makeText(getContext(), "Event entered", Toast.LENGTH_SHORT).show();

                }
                    //add to database
            }
        });
        back = (Button)view.findViewById(R.id.back);
        back.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getFragmentManager().getBackStackEntryCount()>0) {
                    getFragmentManager().popBackStack();
                }
                backPress();
            }
        }
        );
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
        back.setVisibility(View.GONE);

    }
    public void closeFragment(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.remove(this);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        ft.commit();
        ((MainActivity)getActivity()).reload();
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
