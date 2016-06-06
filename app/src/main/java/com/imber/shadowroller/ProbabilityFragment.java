package com.imber.shadowroller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ProbabilityFragment extends Fragment {

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
}

