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
import android.widget.CheckBox;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;
import com.imber.shadowroller.R;
import com.imber.shadowroller.data.DbContract;

import java.util.ArrayList;

public class CommonRollsFragment extends Fragment
        implements AddCommonRollDialogFragment.AddDialogCallbacks, LoaderManager.LoaderCallbacks<Cursor> {

    private CommonRollsAdapter mAdapter;
    private static final int LOADER_ID = 0;

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

        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_common_rolls_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealAddDialog();
            }
        });
        getLoaderManager().initLoader(LOADER_ID, null, this);
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
            holder.edgeCheckBox.setChecked(item.edge);
            holder.diceTextView.setText(String.valueOf(item.dice));
        }

        @Override
        public long getItemId(int position) {
            return mData.get(position).id;
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            if (!mViewEventHandled) {
                mData.clear();
                while (mCursor.moveToNext()) {
                    mData.add(new Item(cursor));
                }
                notifyDataSetChanged();
            }
            mViewEventHandled = false;
        }

        public void deleteItem(int position) {
            mData.remove(position);
            notifyItemRemoved(position);
            mViewEventHandled = true;
            new DeleteItemTask().execute(position);
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

        private class DeleteItemTask extends AsyncTask<Integer, Void, Void> {
            @Override
            protected Void doInBackground(Integer... params) {
                mCursor.moveToPosition(params[0]);
                long id  = mCursor.getLong(mCursor.getColumnIndex(DbContract.CommonRollsTable._ID));
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
        public CheckBox edgeCheckBox;
        public View container;

        public ViewHolder(View v) {
            super(v);
            container = v;
            nameTextView = (TextView) v.findViewById(R.id.name_text_view);
            diceTextView = (TextView) v.findViewById(R.id.dice_text_view);
            resultTextView = (TextView) v.findViewById(R.id.result_text_view);
            edgeCheckBox = (CheckBox) v.findViewById(R.id.edge_check_box);
        }

        @Override
        public View getSwipeableContainerView() {
            return container;
        }
    }

    private class Item {
        long id;
        String name;
        int dice;
        boolean edge;

        public Item(Cursor cursor) {
            id = cursor.getLong(cursor.getColumnIndex(DbContract.CommonRollsTable._ID));
            name = cursor.getString(cursor.getColumnIndex(DbContract.CommonRollsTable.NAME));
            edge = cursor.getInt(cursor.getColumnIndex(DbContract.CommonRollsTable.EDGE)) == 1;
            dice = cursor.getInt(cursor.getColumnIndex(DbContract.CommonRollsTable.DICE));
        }
    }
}
