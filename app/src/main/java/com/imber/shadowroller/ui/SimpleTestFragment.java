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
    private TextView mRollStatus;

    private String mSuccesses = "";
    private String mDisplay = "";
    private int mRollStatusInt = 0;

    private static final String SUCCESSES_KEY = "successes";
    private static final String DISPLAY_KEY = "display";
    private static final String ROLL_STATUS_KEY = "roll_status";

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
        mRollStatus = (TextView) rootView.findViewById(R.id.test_simple_roll_status);
        if (savedInstanceState != null) {
            mSuccesses = savedInstanceState.getString(SUCCESSES_KEY);
            mDisplay = savedInstanceState.getString(DISPLAY_KEY);
            mRollStatusInt = savedInstanceState.getInt(ROLL_STATUS_KEY, 0);
            populate();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SUCCESSES_KEY, mSuccesses);
        outState.putString(DISPLAY_KEY, mDisplay);
        outState.putInt(ROLL_STATUS_KEY, mRollStatusInt);
    }

    @Override
    public void onRollPerformed(ArrayList<int[]> output, Util.TestModifier modifier) {
        mSuccesses = String.valueOf(Util.countSuccesses(output));
        mRollStatusInt = Util.getRollStatus(output.get(0)).toInt();
        boolean rtl = Util.isRTL(getContext());
        String reroll = getString(R.string.re_roll);
        if (rtl) {
            mDisplay = "\n" + Util.resultToOutput(output.get(0), true);
        } else {
            mDisplay = Util.resultToOutput(output.get(0), false) + "\n";
        }
        for (int i = 1; i < output.size(); i++) {
            String diceOutput = Util.resultToOutput(output.get(i), rtl);
            mDisplay += rtl ? "\n" + diceOutput + " :" + reroll : reroll + ": " + diceOutput + "\n";
        }
        populate();
    }

    private void populate() {
        Util.RollStatus status = Util.RollStatus.fromInt(mRollStatusInt);
        mResultCircle.setBackgroundResource(
                Util.getResultCircleIdFromRollStatus(status));
        mResultCircle.setText(mSuccesses);
        if (status != Util.RollStatus.NORMAL) {
            mRollStatus.setTextColor(Util.getColorFromRollStatus(getResources(), status));
            mRollStatus.setText(Util.getNameFromRollStatus(getResources(), status));
            mRollStatus.setVisibility(View.VISIBLE);
        } else {
            mRollStatus.setVisibility(View.GONE);
        }
        mResultOutput.setText(mDisplay);
    }
}

