package com.imber.shadowroller.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
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
import com.imber.shadowroller.sync.SignInActivity;

import java.util.ArrayList;
import java.util.Random;

public class CommonRollsFragment extends Fragment
        implements AddCommonRollDialogFragment.AddDialogCallbacks {
    private static final String TAG = "CommonRollsFragment";

    private CommonRollsAdapter mAdapter;
    private Random mRandom = new Random();
    public static final String NAME_KEY = "name";
    public static final String DICE_KEY = "dice";
    public static final String FIREBASE_ID_KEY = "id";
    private static final int DEFAULT_HIT_VALUE = -1;

    private Util.HistoryListener mHistoryListener;

    private FloatingActionButton mFab;

    private LinearLayout mSignInBar;
    public static final String SIGN_IN_BAR_ACTED_ON_KEY = "sign_in_bar_acted_on";
    private static String SIGNED_IN_KEY;
    private static final int SIGN_IN_ACTIVITY_CODE = 9002;

    public CommonRollsFragment() {}

    public static CommonRollsFragment newInstance() {
        return new CommonRollsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_common_rolls, container, false);

        SIGNED_IN_KEY = getString(R.string.signed_in_key);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.common_rolls_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new CommonRollsAdapter();
        RecyclerViewSwipeManager swipeManager = new RecyclerViewSwipeManager();
        recyclerView.setAdapter(swipeManager.createWrappedAdapter(mAdapter));
        swipeManager.attachRecyclerView(recyclerView);

        mSignInBar = (LinearLayout) rootView.findViewById(R.id.sign_in_bar);
        Button notNow = (Button) mSignInBar.findViewById(R.id.common_rolls_not_now);
        notNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI(false);
            }
        });
        Button signIn = (Button) mSignInBar.findViewById(R.id.common_rolls_sign_in);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(getContext(), SignInActivity.class);
                startActivityForResult(signInIntent, SIGN_IN_ACTIVITY_CODE);
            }
        });
        int visibility = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(SIGN_IN_BAR_ACTED_ON_KEY, false) ? View.GONE : View.VISIBLE;
        mSignInBar.setVisibility(visibility);

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab_common_rolls_add);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                revealAddDialog(null);
            }
        });
        visibility = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(SIGN_IN_BAR_ACTED_ON_KEY, false) ? View.VISIBLE : View.GONE;
        mFab.setVisibility(visibility);
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

    private void updateUI(boolean signedIn) {
        mSignInBar.setVisibility(View.GONE);
        mFab.setVisibility(View.VISIBLE);
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(SIGN_IN_BAR_ACTED_ON_KEY, true);
        editor.putBoolean(SIGNED_IN_KEY, signedIn);
        editor.apply();
    }

    private void registerFirebaseListener(boolean addListener) {
        String uid = Util.getFirebaseUid();
        if (addListener) {
            FirebaseDatabase.getInstance()
                    .getReference(DbContract.CommonRollsTable.FIREBASE_USERS + "/" + uid + "/")
                    .addChildEventListener(mAdapter);
            Log.d(TAG, "registerFirebaseListener: " + "child event listener added");
        } else {
            FirebaseDatabase.getInstance()
                    .getReference(DbContract.CommonRollsTable.FIREBASE_USERS + "/" + uid + "/")
                    .removeEventListener(mAdapter);
            Log.d(TAG, "registerFirebaseListener: " + "child event listener removed");
            int n = mAdapter.mData.size();
            mAdapter.mData.clear();
            mAdapter.notifyItemRangeRemoved(0, n);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                updateUI(true);
            } else {
                Log.d(TAG, "onActivityResult: " + "failed");
                updateUI(false);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        registerFirebaseListener(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerFirebaseListener(true);
    }

    @Override
    public void onPositiveClicked(String name, int dice, String firebaseId) {
        if (firebaseId != null) {
            Util.updateCommonRollsTable(getContext(), firebaseId, name, dice, DEFAULT_HIT_VALUE, Util.RollStatus.NORMAL.toInt());
        }
        else {
            Util.insertIntoCommonRollsTable(getContext(), name, dice);
        }
    }

    @Override
    public void onNegativeClicked() {

    }

    private class CommonRollsAdapter extends RecyclerView.Adapter<ViewHolder>
            implements SwipeableItemAdapter<ViewHolder>, ChildEventListener {

        private static final String TAG = "CommonRollsAdapter";
        ArrayList<Item> mData = new ArrayList<>(); // needed for fast swipe actions

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
            holder.resultTextView.setText(
                    item.hits == -1 ?
                    "" :
                    String.valueOf(item.hits));
            holder.resultTextView.setBackgroundResource(
                    Util.getResultCircleIdFromRollStatus(Util.RollStatus.fromInt(item.rollStatusInt)));
        }

        @Override
        public long getItemId(int position) {
            return mData.get(position).firebaseId.hashCode();
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

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Toast.makeText(getContext(), R.string.error_common_rolls_sync,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            mData.add(dataSnapshot.getValue(Item.class));
            notifyItemInserted(mData.size()-1);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            Item newItem = dataSnapshot.getValue(Item.class);
            for (int i = 0; i < mData.size(); i++) {
                if (mData.get(i).firebaseId.equals(newItem.firebaseId)) {
                    mData.set(i, newItem);
                    notifyItemChanged(i);
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

        public void deleteFromUI(int position) {
            mData.remove(position);
            notifyItemRemoved(position);
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
                String firebaseId = mData.get(mPosition).firebaseId;
                deleteFromUI(mPosition);
                new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        Util.deleteFromCommonRollsTable(getContext(), params[0]);
                        return null;
                    }
                }.execute(firebaseId);
            }
        }
    }

    private class ViewHolder extends AbstractSwipeableItemViewHolder {
        public TextView nameTextView, diceTextView, resultTextView;
        public View container;

        public ViewHolder(View v) {
            super(v);
            container = v;
            container.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Bundle args = new Bundle(2);
                    args.putString(NAME_KEY, nameTextView.getText().toString());
                    args.putInt(DICE_KEY, Integer.parseInt(diceTextView.getText().toString()));
                    String firebaseId = mAdapter.mData.get(getAdapterPosition()).firebaseId;
                    args.putString(FIREBASE_ID_KEY, firebaseId);
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

                    Util.insertIntoHistoryTable(getContext().getContentResolver(), dice, successes,
                            Util.resultToOutput(output), true, Util.TestType.SIMPLE_TEST,
                            Util.TestModifier.NONE, Util.getRollStatus(output.get(0)));
                    if (getResources().getBoolean(R.bool.is_large)) {
                        mHistoryListener.notifyItemInserted();
                    }
                    Item item = mAdapter.mData.get(getAdapterPosition());
                    Util.updateCommonRollsTable(getContext(), item.firebaseId, item.name, item.dice,
                            successes, Util.getRollStatus(output.get(0)).toInt());
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
        public String firebaseId;
        public String name;
        public int dice;
        public int hits;
        public int rollStatusInt;

        public Item() {

        }

        public Item(String firebaseId, String name, int dice) {
            this.firebaseId = firebaseId;
            this.name = name;
            this.dice = dice;
            hits = DEFAULT_HIT_VALUE;
            rollStatusInt = Util.RollStatus.NORMAL.toInt();
        }

        public Item(String firebaseId, String name, int dice, int hits, int rollStatusInt) {
            this.firebaseId = firebaseId;
            this.name = name;
            this.dice = dice;
            this.hits = hits;
            this.rollStatusInt = rollStatusInt;
        }
    }
}
