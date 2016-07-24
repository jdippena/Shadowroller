package com.imber.shadowroller.ui;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;
import com.imber.shadowroller.data.DbContract;

public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    HistoryAdapter mAdapter;

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }

    public HistoryFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_history, container, true);
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
        parentActivity.setSupportActionBar(toolbar);
        ActionBar actionBar = parentActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.history_recyler_view);
        mAdapter = new HistoryAdapter();
        if (recyclerView != null) {
            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        getLoaderManager().initLoader(LOADER_ID, null, this);
        return rootView;
    }

    public void clearHistory() {
        getContext().getContentResolver().delete(DbContract.HistoryTable.CONTENT_URI, null, null);
        mAdapter.notifyItemRangeRemoved(0, mAdapter.getItemCount());
        mAdapter.swapCursor(null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(), DbContract.HistoryTable.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private class HistoryAdapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor mCursor;

        @Override
        public int getItemCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(getActivity().getLayoutInflater().inflate(R.layout.list_item_history, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            boolean commonRoll = mCursor.getInt(mCursor.getColumnIndex(DbContract.HistoryTable.COMMON_ROLL)) == 1;
            Util.TestType type = Util.TestType.fromInt(
                    mCursor.getInt(mCursor.getColumnIndex(DbContract.HistoryTable.TEST_TYPE))
            );
            int dice = mCursor.getInt(mCursor.getColumnIndex(DbContract.HistoryTable.DICE));
            Util.TestModifier modifier = Util.TestModifier.fromInt(
                    mCursor.getInt(mCursor.getColumnIndex(DbContract.HistoryTable.MODIFIER))
            );
            int hits = mCursor.getInt(mCursor.getColumnIndex(DbContract.HistoryTable.HITS));
            Util.RollStatus status = Util.RollStatus.fromInt(
                    mCursor.getInt(mCursor.getColumnIndex(DbContract.HistoryTable.STATUS))
            );
            String output = mCursor.getString(mCursor.getColumnIndex(DbContract.HistoryTable.OUTPUT));
            long date = mCursor.getLong(mCursor.getColumnIndex(DbContract.HistoryTable.DATE));

            Resources res = getResources();
            holder.star.setVisibility(commonRoll ? View.VISIBLE : View.INVISIBLE);
            holder.testType.setText(Util.getNameFromTestType(res, type));
            holder.dice.setText(String.valueOf(dice));
            holder.edge.setText(Util.getNameFromTestModifier(res, modifier));
            holder.hits.setText(String.valueOf(hits));
            holder.hits.setBackgroundResource(Util.getResultCircleIdFromRollStatus(status));
            if (status != Util.RollStatus.NORMAL) {
                holder.status.setTextColor(Util.getColorFromRollStatus(res, status));
                holder.status.setText(Util.getNameFromRollStatus(res, status));
                holder.status.setVisibility(View.VISIBLE);
            } else {
                holder.status.setVisibility(View.GONE);
            }
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView star;
        public TextView testType, dice, edge, hits, status;

        public ViewHolder(View itemView) {
            super(itemView);
            star = (ImageView) itemView.findViewById(R.id.list_item_history_star);
            testType = (TextView) itemView.findViewById(R.id.list_item_history_test_type);
            dice = (TextView) itemView.findViewById(R.id.list_item_history_dice);
            edge = (TextView) itemView.findViewById(R.id.list_item_history_edge);
            hits = (TextView) itemView.findViewById(R.id.list_item_history_hits);
            status = (TextView) itemView.findViewById(R.id.list_item_history_status);
        }
    }
}
