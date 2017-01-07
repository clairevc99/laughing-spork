package com.example.monjuri.planner;

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
import android.widget.TextView;

import java.util.Arrays;


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
    Button add, back;
    TextView today, events;

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
        add = (Button)view.findViewById(R.id.add);
        add.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putInt("year", getArguments().getInt("year"));
                args.putInt("month", getArguments().getInt("month"));
                args.putInt("day", getArguments().getInt("day"));
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
        back = (Button)view.findViewById(R.id.back);
        back.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                closeFragment();
            }
        });
        events = (TextView)view.findViewById(R.id.events);
        String ev = "";
        SQLiteDatabase db = ((MainActivity)getActivity()).getDb();
        Cursor c = db.rawQuery("SELECT * FROM calendar WHERE month = ? AND day = ? AND year = ? ORDER BY year ASC, month ASC, day ASC, hour ASC, minute ASC", new String[]{String.valueOf(month), String.valueOf(day), String.valueOf(year)});
        while (c.moveToNext()) {
            String minute;
            minute = c.getInt(c.getColumnIndex("minute")) < 10 ?
                "0"+c.getString(c.getColumnIndex("minute")) : c.getString(c.getColumnIndex("minute"));
            ev += (c.getString(c.getColumnIndex("event_name")) + " at " + c.getString(c.getColumnIndex("hour")) + ": " + minute +
                    " under category " + c.getString(c.getColumnIndex("category"))) + "\n";
        }
        c.close();
        events.setText(ev);
        return view;


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
        back.setVisibility(View.GONE);
        today.setVisibility(View.GONE);
        events.setVisibility(View.GONE);
    }

    public void reload(){
        add.setVisibility(View.VISIBLE);
        back.setVisibility(View.VISIBLE);
        today.setVisibility(View.VISIBLE);
        events.setVisibility(View.VISIBLE);
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
