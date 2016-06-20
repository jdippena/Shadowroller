package com.imber.shadowroller.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.imber.shadowroller.R;

public class CommonRollsFragment extends Fragment {

    public CommonRollsFragment() {}

    public static CommonRollsFragment newInstance() {
        return new CommonRollsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_common_rolls, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.common_rolls_recycler_view);
        recyclerView.setAdapter(new CommonRollsAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }

    private class CommonRollsAdapter extends RecyclerView.Adapter<ViewHolder> {
        @Override
        public int getItemCount() {
            return 3;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.list_item_common_rolls, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            switch (position) {
                case 0:
                    holder.nameTextView.setText("Initiative");
                    holder.edgeCheckBox.setChecked(false);
                    holder.diceTextView.setText("13");
                    holder.resultTextView.setText("4");
                    break;
                case 1:
                    holder.nameTextView.setText("Dodge");
                    holder.edgeCheckBox.setChecked(true);
                    holder.diceTextView.setText("9");
                    holder.resultTextView.setText("2");
                    break;
                case 2:
                    holder.nameTextView.setText("Hack on the Fly");
                    holder.edgeCheckBox.setChecked(false);
                    holder.diceTextView.setText("14");
                    holder.resultTextView.setText("5");
                    break;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView, diceTextView, resultTextView;
        public CheckBox edgeCheckBox;

        public ViewHolder(View v) {
            super(v);
            nameTextView = (TextView) v.findViewById(R.id.name_text_view);
            diceTextView = (TextView) v.findViewById(R.id.dice_text_view);
            resultTextView = (TextView) v.findViewById(R.id.result_text_view);
            edgeCheckBox = (CheckBox) v.findViewById(R.id.edge_check_box);
        }

    }
}
