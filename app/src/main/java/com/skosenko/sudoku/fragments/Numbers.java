package com.skosenko.sudoku.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.skosenko.sudoku.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Numbers#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Numbers extends Fragment {
    View view;
    private OnNumberInteractionListener mListener;

    public Numbers() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Numbers.
     */
    // TODO: Rename and change types and number of parameters
    public static Numbers newInstance(String param1, String param2) {
        Numbers fragment = new Numbers();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_numbers, container, false);

        int numbers[] = new int[]{R.id.clear, R.id.keypad_1, R.id.keypad_2, R.id.keypad_3, R.id.keypad_4,
                R.id.keypad_5, R.id.keypad_6, R.id.keypad_7, R.id.keypad_8, R.id.keypad_9};
        for (int number : numbers) {
            final Button buttonNumber = view.findViewById(number);
            buttonNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onFragmentInteraction(buttonNumber.getText().toString());
                }
            });
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CellFragment.OnFragmentInteractionListener) {
            mListener = (OnNumberInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNumberInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnNumberInteractionListener {
        void onFragmentInteraction(String value);
    }
}