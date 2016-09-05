package com.imber.shadowroller.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.database.FirebaseDatabase;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;
import com.imber.shadowroller.data.DbContract;
import com.imber.shadowroller.sync.SignInActivity;

import java.util.Random;

public class CommonRollsFragment extends Fragment
        implements AddCommonRollDialogFragment.AddDialogCallbacks {
    private static final String TAG = "CommonRollsFragment";

    private CommonRollsAdapter mAdapter;
    private Random mRandom = new Random();

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
        mAdapter = new CommonRollsAdapter(this);
        if (mHistoryListener != null) {
            mAdapter.setHistoryListener(mHistoryListener);
        }
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

    public void revealAddDialog(Bundle args) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        AddCommonRollDialogFragment addDialog = new AddCommonRollDialogFragment();
        addDialog.setCallback(this);
        addDialog.setArguments(args);
        addDialog.show(fm, "addDialog");
    }

    public void setHistoryListener(Util.HistoryListener historyListener) {
        mHistoryListener = historyListener;
        if (mAdapter != null) {
            mAdapter.setHistoryListener(mHistoryListener);
        }
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
        if (Util.isLoggedIn()) {
            if (addListener) {
                FirebaseDatabase.getInstance()
                        .getReference(DbContract.CommonRollsTable.FIREBASE_USERS + "/" + uid + "/")
                        .addChildEventListener(mAdapter);
            } else {
                FirebaseDatabase.getInstance()
                        .getReference(DbContract.CommonRollsTable.FIREBASE_USERS + "/" + uid + "/")
                        .removeEventListener(mAdapter);
                int n = mAdapter.mData.size();
                mAdapter.mData.clear();
                mAdapter.notifyItemRangeRemoved(0, n);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                updateUI(true);
            } else {
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
    public void onNewCommonRollCreated(String name, int dice) {
        Util.insertIntoCommonRollsTable(getContext(), name, dice, mAdapter);
    }

    @Override
    public void onCommonRollEdited(long id, String firebaseId, String name, int dice, int position) {
        Util.updateCommonRollsTable(getContext(), id, firebaseId, name, dice,
                CommonRollsAdapter.DEFAULT_HIT_VALUE, Util.RollStatus.NORMAL.toInt(),
                mAdapter, position);
    }
}
