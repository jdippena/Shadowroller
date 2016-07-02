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

    private ArrayList<int[]> mResults = new ArrayList<>();

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
        return rootView;
    }

    @Override
    public void onExtendedRollPerformed(ArrayList<int[]> output) {
        mResults = output;
        int totalSuccesses = Util.countSuccesses(output);
        mResultCircle.setText(String.valueOf(totalSuccesses));
        mResultAdapter.notifyDataSetChanged();
    }

    private class ResultAdapter extends ArrayAdapter<int[]> {
        public ResultAdapter() {
            super(ExtendedTestFragment.this.getContext(), 0);
        }

        @Override
        public int getCount() {
            return mResults.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int [] result = mResults.get(position);
            String label = String.valueOf(position + 1);
            String output = Util.resultToOutput(result);
            String successes = String.valueOf(Util.countSuccesses(result));
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
