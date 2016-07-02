package com.imber.shadowroller.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;

import java.util.ArrayList;

public class SimpleTestFragment extends Fragment implements Util.SimpleTestDiceListener {
    public static final String TAG = SimpleTestFragment.class.getCanonicalName();

    private TextView mResultCircle;
    private TextView mResultOutput;

    public SimpleTestFragment() {}

    public static SimpleTestFragment newInstance() {
        return new SimpleTestFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_simple, container, false);
        mResultCircle = (TextView) rootView.findViewById(R.id.test_simple_result_circle);
        mResultOutput = (TextView) rootView.findViewById(R.id.test_simple_result_output);
        return rootView;
    }

    @Override
    public void onRollPerformed(ArrayList<int[]> output, Util.TestModifier modifier) {
        int successes = Util.countSuccesses(output);
        mResultCircle.setText(String.valueOf(successes));
        String display = Util.resultToOutput(output.get(0));
        for (int i = 1; i < output.size(); i++) {
            display += "Re-roll: " + Util.resultToOutput(output.get(i));
        }
        mResultOutput.setText(display);
    }
}

