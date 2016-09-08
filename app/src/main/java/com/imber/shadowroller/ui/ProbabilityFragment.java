package com.imber.shadowroller.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;
import com.imber.shadowroller.data.DbContract;

import java.util.Locale;

public class ProbabilityFragment extends Fragment implements Util.ProbabilityDiceListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ProbabilityFragment";

    private ProbabilityAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private static final int LOADER_ID = 0;
    private String mUri = "";
    private static final String URI_KEY = "query_uri";

    public ProbabilityFragment() {}

    public static ProbabilityFragment newInstance() {
        return new ProbabilityFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_probability, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.probability_recycler_view);
        mAdapter = new ProbabilityAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (savedInstanceState != null) {
            mUri = savedInstanceState.getString(URI_KEY);
            if (mUri != null && !mUri.equals("")) {
                getLoaderManager().restartLoader(LOADER_ID, null, this);
            }
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(URI_KEY, mUri);
    }

    @Override
    public void onProbabilityQueried(Uri uri) {
        mUri = uri.toString();
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse(mUri);
        String columnName = DbContract.getColumnNameFromDiceNumber(DbContract.getDiceNumberFromUri(uri));
        String[] projection = new String[] {columnName};
        String selection = columnName + " >= ? ";
        String[] selectionArgs = new String[] {String.valueOf(Math.pow(10, -(getAccuracy() + 2))/2)};
        return new CursorLoader(getContext(), uri, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private int getAccuracy() {
        String defaultAccuracy = String.valueOf(getResources().getInteger(R.integer.default_accuracy));
        String accuracy = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.pref_accuracy_key), defaultAccuracy);
        return Integer.parseInt(accuracy);
    }

    private class ProbabilityAdapter extends RecyclerView.Adapter<ProbabilityViewHolder> {
        Cursor mCursor;
        public static final int HEADER = 0;
        public static final int NOT_HEADER = 1; // <- creative genius this
        private int mProbabilityBarWidth = 0;

        @Override
        public int getItemCount() {
            return mCursor == null ? 0 : mCursor.getCount() + 1;
        }

        @Override
        public ProbabilityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutId = viewType == HEADER ?
                    R.layout.layout_probability_header :
                    R.layout.list_item_probability;

            return new ProbabilityViewHolder(
                    getActivity().getLayoutInflater().inflate(layoutId, parent, false),
                    viewType);
        }

        @Override
        public void onBindViewHolder(ProbabilityViewHolder holder, int position) {
            if (position == 0) return;
            mCursor.moveToPosition(position - 1);
            double prob = mCursor.getDouble(0);
            holder.hitsLabel.setText(String.valueOf(position - 1));
            holder.probability.setText(String.format(
                    Locale.getDefault(), "%." + String.valueOf(getAccuracy()) + "f", prob * 100));
            if (mProbabilityBarWidth == 0) {
                LinearLayout parent = (LinearLayout) holder.probability.getParent();
                mProbabilityBarWidth = (mRecyclerView.getWidth() -
                        mRecyclerView.getPaddingRight() -
                        mRecyclerView.getPaddingLeft() -
                        parent.getPaddingRight() -
                        parent.getPaddingLeft()) / 3;
            }
            if (Util.isRTL(getContext())) {
                holder.probabilityBar.setPadding((int) (mProbabilityBarWidth * (1 - (float) prob)),
                        0, 0, 0);
            } else {
                holder.probabilityBar.setPadding(0, 0,
                        (int) (mProbabilityBarWidth * (1 - (float) prob)), 0);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? HEADER : NOT_HEADER;
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }


    private class ProbabilityViewHolder extends RecyclerView.ViewHolder {
        TextView hitsLabel, probability;
        FrameLayout probabilityBar;

        public ProbabilityViewHolder(View v, int type) {
            super(v);
            switch (type) {
                case ProbabilityAdapter.HEADER:
                    hitsLabel = (TextView) v.findViewById(R.id.probability_header_hits_label);
                    probability = (TextView) v.findViewById(R.id.probability_header_probability);
                    break;
                case ProbabilityAdapter.NOT_HEADER:
                    hitsLabel = (TextView) v.findViewById(R.id.list_item_probability_hits_label);
                    probability = (TextView) v.findViewById(R.id.list_item_probability_probability);
                    probabilityBar = (FrameLayout) v.findViewById(R.id.list_item_probability_bar);
                    break;
            }
        }
    }
}

