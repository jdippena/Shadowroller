package com.imber.shadowroller.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;
import com.imber.shadowroller.data.DbContract;

import java.util.ArrayList;
import java.util.Random;

public class CommonRollsFragment extends Fragment
        implements AddCommonRollDialogFragment.AddDialogCallbacks, LoaderManager.LoaderCallbacks<Cursor> {

    private CommonRollsAdapter mAdapter;
    private static final int LOADER_ID = 0;
    private Random mRandom = new Random();
    public static final String NAME_KEY = "name";
    public static final String DICE_KEY = "dice";
    public static final String ID_KEY = "id";

    private ArrayList<Integer> mSavedDiceResults;
    private static final String SAVED_DICE_RESULTS_KEY = "saved_dice_results";
    private boolean mRestoredFromState;

    private Util.HistoryListener mHistoryListener;

    public CommonRollsFragment() {}

    public static CommonRollsFragment newInstance() {
        return new CommonRollsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_common_rolls, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.common_rolls_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new CommonRollsAdapter();
        RecyclerViewSwipeManager swipeManager = new RecyclerViewSwipeManager();
        recyclerView.setAdapter(swipeManager.createWrappedAdapter(mAdapter));
        swipeManager.attachRecyclerView(recyclerView);

        if (savedInstanceState != null) {
            mSavedDiceResults = savedInstanceState.getIntegerArrayList(SAVED_DICE_RESULTS_KEY);
            mRestoredFromState = true;
        } else {
            mSavedDiceResults = new ArrayList<>();
            mRestoredFromState = false;
        }

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_common_rolls_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealAddDialog(null);
            }
        });
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        return rootView;
    }

    private void revealAddDialog(Bundle args) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        AddCommonRollDialogFragment addDialog = new AddCommonRollDialogFragment();
        addDialog.setCallback(this);
        addDialog.setArguments(args);
        addDialog.show(fm, "addDialog");
    }

    public void setHistoryListener(Util.HistoryListener historyListener) {
        this.mHistoryListener = historyListener;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(SAVED_DICE_RESULTS_KEY, mSavedDiceResults);
    }

    @Override
    public void onPositiveClicked(String name, int dice, long id) {
        ContentValues values = new ContentValues(2);
        values.put(DbContract.CommonRollsTable.NAME, name);
        values.put(DbContract.CommonRollsTable.DICE, dice);
        if (id != -1) {
            getContext().getContentResolver().update(
                    DbContract.CommonRollsTable.buildUriFromId(id),
                    values, null, null);
        } else {
            getContext().getContentResolver().insert(DbContract.CommonRollsTable.CONTENT_URI, values);
        }
        mSavedDiceResults.add(-1);
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onNegativeClicked() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getContext(),
                DbContract.CommonRollsTable.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private class CommonRollsAdapter extends RecyclerView.Adapter<ViewHolder>
            implements SwipeableItemAdapter<ViewHolder> {

        private static final String TAG = "CommonRollsAdapter";
        Cursor mCursor;
        ArrayList<Item> mData = new ArrayList<>(); // needed for fast swipe actions
        private boolean mViewEventHandled = false;

        public CommonRollsAdapter() {
            setHasStableIds(true);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.list_item_common_rolls, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Item item = mData.get(position);
            holder.nameTextView.setText(item.name);
            holder.diceTextView.setText(String.valueOf(item.dice));
            holder.resultTextView.setText(mSavedDiceResults.get(position) == -1
                    ? ""
                    : String.valueOf(mSavedDiceResults.get(position)));
            holder.setId(item.id);
        }

        @Override
        public long getItemId(int position) {
            return mData.get(position).id;
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            if (!mViewEventHandled) {
                mData.clear();
                if (!mRestoredFromState) {
                    mSavedDiceResults.clear();
                }
                if (mCursor != null) {
                    while (mCursor.moveToNext()) {
                        mData.add(new Item(cursor));
                        if (!mRestoredFromState) {
                            mSavedDiceResults.add(-1);
                        }
                    }
                }
                notifyDataSetChanged();
            }
            mViewEventHandled = false;
        }

        public void deleteItem(int position) {
            long id = mData.get(position).id;
            mData.remove(position);
            mSavedDiceResults.remove(position);
            notifyItemRemoved(position);
            mViewEventHandled = true;
            new DeleteItemTask().execute(id);
        }

        @Override
        public SwipeResultAction onSwipeItem(ViewHolder holder, int position, int result) {
            switch (result) {
                case SwipeableItemConstants.RESULT_SWIPED_RIGHT:
                    return new SwipeRemoveAction(position);
                default:
                    return new SwipeResultActionDefault();
            }
        }

        @Override
        public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
            return SwipeableItemConstants.REACTION_CAN_SWIPE_RIGHT;
        }

        @Override
        public void onSetSwipeBackground(ViewHolder holder, int position, int type) {

        }

        private class SwipeRemoveAction extends SwipeResultActionRemoveItem {
            private int mPosition;

            public SwipeRemoveAction(int position) {
                super();
                mPosition = position;
            }

            @Override
            protected void onPerformAction() {
                super.onPerformAction();
                CommonRollsAdapter.this.deleteItem(mPosition);
            }
        }

        private class DeleteItemTask extends AsyncTask<Long, Void, Void> {
            @Override
            protected Void doInBackground(Long... params) {
                long id  = params[0];
                ContentResolver resolver = getContext().getContentResolver();
                resolver.delete(
                        DbContract.CommonRollsTable.CONTENT_URI,
                        DbContract.CommonRollsTable._ID + " = ?",
                        new String[] {String.valueOf(id)}
                );
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getLoaderManager().restartLoader(LOADER_ID, null, CommonRollsFragment.this);
            }
        }
    }

    private class ViewHolder extends AbstractSwipeableItemViewHolder {
        public TextView nameTextView, diceTextView, resultTextView;
        public View container;
        public long id;

        public ViewHolder(View v) {
            super(v);
            container = v;
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Bundle args = new Bundle(2);
                    args.putString(NAME_KEY, nameTextView.getText().toString());
                    args.putInt(DICE_KEY, Integer.parseInt(diceTextView.getText().toString()));
                    args.putLong(ID_KEY, id);
                    revealAddDialog(args);
                    return true;
                }
            });
            nameTextView = (TextView) v.findViewById(R.id.name_text_view);
            diceTextView = (TextView) v.findViewById(R.id.dice_text_view);
            diceTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int dice = Integer.valueOf(diceTextView.getText().toString());
                    ArrayList<int[]> output = Util.doSimpleRoll(mRandom, dice, Util.TestModifier.NONE);
                    int successes = Util.countSuccesses(output);
                    mSavedDiceResults.set(getLayoutPosition(), successes);
                    resultTextView.setBackgroundResource(
                            Util.getResultCircleIdFromRollStatus(Util.getRollStatus(output.get(0))));
                    resultTextView.setText(String.valueOf(successes));
                    Util.insertIntoHistoryTable(getContext().getContentResolver(), dice, successes,
                            Util.resultToOutput(output), true, Util.TestType.SIMPLE_TEST,
                            Util.TestModifier.NONE, Util.getRollStatus(output.get(0)));
                    if (getResources().getBoolean(R.bool.is_large)) {
                        mHistoryListener.notifyItemInserted();
                    }
                }
            });
            resultTextView = (TextView) v.findViewById(R.id.result_text_view);
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }

        public void setId(long id) {
            this.id = id;
        }
    }

    private class Item {
        long id;
        String name;
        int dice;

        public Item(Cursor cursor) {
            id = cursor.getLong(cursor.getColumnIndex(DbContract.CommonRollsTable._ID));
            name = cursor.getString(cursor.getColumnIndex(DbContract.CommonRollsTable.NAME));
            dice = cursor.getInt(cursor.getColumnIndex(DbContract.CommonRollsTable.DICE));
        }
    }
}
