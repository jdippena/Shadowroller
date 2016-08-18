package com.imber.shadowroller.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;

import java.util.ArrayList;

public class ExtendedTestFragment extends Fragment implements Util.ExtendedTestDiceListener {
    public static final String TAG = ExtendedTestFragment.class.getCanonicalName();

    private TextView mResultCircle;
    private ResultAdapter mResultAdapter;

    private String mTotalSuccesses = "";
    private ArrayList<String> mDisplay = new ArrayList<>();
    private ArrayList<String> mSuccessesList = new ArrayList<>();

    private static final String TOTAL_SUCCESSES_KEY = "total_successes";
    private static final String DISPLAY_KEY = "display";
    private static final String SUCCESSES_LIST_KEY = "successes_list";

    public ExtendedTestFragment() {}

    public static ExtendedTestFragment newInstance() {
        return new ExtendedTestFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_test_extended, container, false);
        mResultCircle = (TextView) rootView.findViewById(R.id.test_extended_result_circle);
        ListView resultListView = (ListView) rootView.findViewById(R.id.test_extended_recycler_view);
        mResultAdapter = new ResultAdapter();
        resultListView.setAdapter(mResultAdapter);
        if (savedInstanceState != null) {
            mTotalSuccesses = savedInstanceState.getString(TOTAL_SUCCESSES_KEY);
            mDisplay = savedInstanceState.getStringArrayList(DISPLAY_KEY);
            mSuccessesList = savedInstanceState.getStringArrayList(SUCCESSES_LIST_KEY);
            populate();
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TOTAL_SUCCESSES_KEY, mTotalSuccesses);
        outState.putStringArrayList(DISPLAY_KEY, mDisplay);
        outState.putStringArrayList(SUCCESSES_LIST_KEY, mSuccessesList);
    }

    @Override
    public void onExtendedRollPerformed(ArrayList<int[]> output) {
        mDisplay.clear();
        mSuccessesList.clear();
        int totalSuccesses = 0;
        for (int[] result : output) {
            mDisplay.add(Util.resultToOutput(result));
            int successes = Util.countSuccesses(result);
            totalSuccesses += successes;
            mSuccessesList.add(String.valueOf(successes));
        }
        mTotalSuccesses = String.valueOf(totalSuccesses);
        populate();
    }

    private void populate() {
        mResultCircle.setText(mTotalSuccesses);
        mResultAdapter.notifyDataSetChanged();
    }

    private class ResultAdapter extends ArrayAdapter<int[]> {
        public ResultAdapter() {
            super(ExtendedTestFragment.this.getContext(), 0);
        }

        @Override
        public int getCount() {
            return mDisplay.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String label = String.valueOf(position + 1);
            String output = mDisplay.get(position);
            String successes = mSuccessesList.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_test_extended, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.list_item_test_extended_result_label)).setText(label);
            ((TextView) convertView.findViewById(R.id.list_item_test_extended_result_output)).setText(output);
            ((TextView) convertView.findViewById(R.id.list_item_test_extended_result_hits)).setText(successes);
            return convertView;
        }
    }
}
