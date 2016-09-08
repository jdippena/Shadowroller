package com.imber.shadowroller.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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

public class CommonRollsAdapter extends RecyclerView.Adapter<CommonRollsAdapter.ViewHolder>
        implements SwipeableItemAdapter<CommonRollsAdapter.ViewHolder>, ChildEventListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "CommonRollsAdapter";

    private Context mContext;
    public CommonRollsFragment mFragment;
    private Util.HistoryListener mHistoryListener;

    private Cursor mCursor;
    public static final int LOADER_ID = 1;
    private Random mRandom = new Random();
    ArrayList<Item> mData = new ArrayList<>(); // needed for fast swipe actions
    private boolean mViewEventHandled = false;
    public static final String ID_KEY = "id";
    public static final String FIREBASE_ID_KEY = "firebase_id";
    public static final String NAME_KEY = "name";
    public static final String DICE_KEY = "dice";
    public static final String POSITION_KEY = "pos";
    public static final int DEFAULT_ID_VALUE = -1;
    public static final int DEFAULT_HIT_VALUE = -1;

    public CommonRollsAdapter(CommonRollsFragment commonRollsFragment) {
        mContext = commonRollsFragment.getActivity().getApplicationContext();
        mFragment = commonRollsFragment;
        setHasStableIds(true);
        mViewEventHandled = false;
        if (!Util.isLoggedIn()) {
            mFragment.getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_common_rolls, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = mData.get(position);
        holder.nameTextView.setText(item.name);
        holder.diceTextView.setText(String.valueOf(item.dice));
        holder.resultTextView.setText(
                item.hits == DEFAULT_HIT_VALUE ?
                        "" :
                        String.valueOf(item.hits));
        holder.resultTextView.setBackgroundResource(
                Util.getResultCircleIdFromRollStatus(Util.RollStatus.fromInt(item.rollStatusInt)));
    }

    @Override
    public long getItemId(int position) {
        if (Util.isLoggedIn()) {
            return mData.get(position).firebaseId.hashCode();
        } else  {
            return mData.get(position).id;
        }
    }

    @Override
    public SwipeResultAction onSwipeItem(ViewHolder holder, int position, int result) {
        switch (result) {
            case SwipeableItemConstants.RESULT_SWIPED_RIGHT:
                if (!Util.isRTL(mContext)) {
                    return new SwipeRemoveAction(position);
                } else {
                    return new SwipeResultActionDefault();
                }
            case SwipeableItemConstants.RESULT_SWIPED_LEFT:
                if (Util.isRTL(mContext)) {
                    return new SwipeRemoveAction(position);
                } else {
                    return new SwipeResultActionDefault();
                }
            default:
                return new SwipeResultActionDefault();
        }
    }

    @Override
    public int onGetSwipeReactionType(ViewHolder holder, int position, int x, int y) {
        if (Util.isRTL(mContext)) {
            return SwipeableItemConstants.REACTION_CAN_SWIPE_LEFT;
        } else {
            return SwipeableItemConstants.REACTION_CAN_SWIPE_RIGHT;
        }
    }

    @Override
    public void onSetSwipeBackground(ViewHolder holder, int position, int type) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, DbContract.CommonRollsTable.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        if (!mViewEventHandled) {
            mCursor.moveToFirst();
            mData.clear();
            while (mCursor.moveToNext()) {
                Item newItem = new Item(mCursor.getLong(mCursor.getColumnIndex(DbContract.CommonRollsTable._ID)),
                        mCursor.getString(mCursor.getColumnIndex(DbContract.CommonRollsTable.NAME)),
                        mCursor.getInt(mCursor.getColumnIndex(DbContract.CommonRollsTable.DICE)),
                        mCursor.getInt(mCursor.getColumnIndex(DbContract.CommonRollsTable.HITS)),
                        mCursor.getInt(mCursor.getColumnIndex(DbContract.CommonRollsTable.ROLL_STATUS)));
                mData.add(newItem);
            }
            notifyDataSetChanged();
        }
        mViewEventHandled = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        addToUI(dataSnapshot.getValue(Item.class));
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
        Item newItem = dataSnapshot.getValue(Item.class);
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).firebaseId.equals(newItem.firebaseId)) {
                updateUI(i, newItem);
                return;
            }
        }
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).firebaseId.equals(key)) {
                deleteFromUI(i);
                return;
            }
        }
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(mContext, R.string.error_common_rolls_sync,
                Toast.LENGTH_SHORT).show();
    }

    public void addToUI(long id, String name, int dice, int hits, int rollStatus) {
        mData.add(new Item(id, name, dice, hits, rollStatus));
        notifyItemInserted(mData.size()-1);
        mViewEventHandled = true;
    }

    public void addToUI(Item newItem) {
        mData.add(newItem);
        notifyItemInserted(mData.size()-1);
        mViewEventHandled = true;
    }

    public void updateUI(int position, String name, int dice, int hits, int rollStatus) {
        long id = mData.get(position).id;
        mData.set(position, new Item(id, name, dice, hits, rollStatus));
        notifyItemChanged(position);
        mViewEventHandled = true;
    }

    public void updateUI(int position, Item newItem) {
        mData.set(position, newItem);
        notifyItemChanged(position);
        mViewEventHandled = true;
    }

    public void deleteFromUI(int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        mViewEventHandled = true;
    }

    public void setHistoryListener(Util.HistoryListener historyListener) {
        mHistoryListener = historyListener;
    }

    public class SwipeRemoveAction extends SwipeResultActionRemoveItem {
        private int mPosition;

        public SwipeRemoveAction(int position) {
            super();
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();
            long id = mData.get(mPosition).id;
            String firebaseId = mData.get(mPosition).firebaseId;
            deleteFromUI(mPosition);
            new AsyncTask<String, Void, Void>() {
                @Override
                protected Void doInBackground(String... params) {
                    Util.deleteFromCommonRollsTable(mContext, Long.parseLong(params[0]), params[1]);
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    if (!Util.isLoggedIn()) {
                        mFragment.getLoaderManager().restartLoader(LOADER_ID, null, CommonRollsAdapter.this);
                    }
                }
            }.execute(String.valueOf(id), firebaseId);
        }
    }

    public class ViewHolder extends AbstractSwipeableItemViewHolder {
        public TextView nameTextView, diceTextView, resultTextView;
        public View container;

        public ViewHolder(View v) {
            super(v);
            container = v;
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Bundle args = new Bundle(4);
                    Item item = mData.get(getAdapterPosition());
                    args.putLong(ID_KEY, item.id);
                    args.putString(FIREBASE_ID_KEY, item.firebaseId);
                    args.putString(NAME_KEY, item.name);
                    args.putInt(DICE_KEY, item.dice);
                    args.putInt(POSITION_KEY, getAdapterPosition());
                    mFragment.revealAddDialog(args);
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

                    boolean rtl = Util.isRTL(mContext);
                    Item item = mData.get(getAdapterPosition());
                    Util.updateCommonRollsTable(mContext, item.id, item.firebaseId, item.name, item.dice,
                            successes, Util.getRollStatus(output.get(0)).toInt(), CommonRollsAdapter.this, getAdapterPosition());
                    Util.insertIntoHistoryTable(mContext.getContentResolver(), dice, successes,
                            Util.resultToOutput(output, rtl), true, Util.TestType.SIMPLE_TEST,
                            Util.TestModifier.NONE, Util.getRollStatus(output.get(0)));
                    if (mContext.getResources().getBoolean(R.bool.is_large)) {
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
    }

    public static class Item {
        long id;
        public String firebaseId;
        public String name;
        public int dice;
        public int hits;
        public int rollStatusInt;

        public Item() {

        }

        private Item(String name, int dice, int hits, int rollStatusInt) {
            this.name = name;
            this.dice = dice;
            this.hits = hits;
            this.rollStatusInt = rollStatusInt;
        }

        public Item(long id, String name, int dice, int hits, int rollStatusInt) {
            this(name, dice, hits, rollStatusInt);
            this.id = id;
            this.firebaseId = null;
        }

        public Item(String firebaseId, String name, int dice, int hits, int rollStatusInt) {
            this(name, dice, hits, rollStatusInt);
            id = DEFAULT_ID_VALUE;
            this.firebaseId = firebaseId;
        }
    }

}
