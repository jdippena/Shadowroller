package com.imber.shadowroller.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;

public class ProbabilityFragment extends Fragment implements Util.ProbabilityDiceListener {
    private static final String TAG = "ProbabilityFragment";

    public ProbabilityFragment() {}

    public static ProbabilityFragment newInstance() {
        return new ProbabilityFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_probability, container, false);
        return rootView;
    }

    @Override
    public void onProbabilityQueried(float[] probabilities, Util.TestModifier modifier) {
        String debug = "";
        for (float prob : probabilities) {
            debug += String.valueOf(prob) + "\t";
        }
        Log.i(TAG, "onProbabilityQueried:" + debug);
    }
}

