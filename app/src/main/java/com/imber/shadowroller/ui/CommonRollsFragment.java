package com.imber.shadowroller.ui;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.imber.shadowroller.R;
import com.imber.shadowroller.data.DbContract;

public class CommonRollsFragment extends Fragment implements AddCommonRollDialogFragment.AddDialogCallbacks {

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
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_common_rolls_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealAddDialog();
            }
        });
        return rootView;
    }

    private void revealAddDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        AddCommonRollDialogFragment addDialog = new AddCommonRollDialogFragment();
        addDialog.setCallback(this);
        addDialog.show(fm, "addDialog");
    }

    @Override
    public void onPositiveClicked(String name, int dice) {
        ContentValues values = new ContentValues(3);
        values.put(DbContract.CommonRollsTable.NAME, name);
        values.put(DbContract.CommonRollsTable.DICE, dice);
        values.put(DbContract.CommonRollsTable.EDGE, Boolean.FALSE);
        getContext().getContentResolver().insert(DbContract.CommonRollsTable.CONTENT_URI, values);
    }

    @Override
    public void onNegativeClicked() {

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

    private class ViewHolder extends RecyclerView.ViewHolder {
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
