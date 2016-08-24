package com.imber.shadowroller.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.imber.shadowroller.R;
import com.imber.shadowroller.sync.SignInActivity;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, FirebaseAuth.AuthStateListener {
    private static final int SIGN_IN_ACTIVITY_CODE = 9002;
    Preference mSignInPreference;
    FirebaseAuth mAuth;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
        setSummary(getString(R.string.pref_accuracy_key));

        mSignInPreference= new Preference(getActivity());
        setSignInPreferenceUI(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(R.string.signed_in_key), false));
        getPreferenceScreen().addPreference(mSignInPreference);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);
        mAuth.addAuthStateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(this);
        mAuth.removeAuthStateListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SIGN_IN_ACTIVITY_CODE) {
            setSignInPreferenceUI(resultCode == Activity.RESULT_OK);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setSummary(key);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        setSignInPreferenceUI(firebaseAuth.getCurrentUser() != null);
    }

    private void setSummary(String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference) pref).getValue());
        }
    }

    private void setSignInPreferenceUI(boolean signedIn) {
        if (signedIn) {
            mSignInPreference.setTitle(getString(R.string.sign_out));
            mSignInPreference.setSummary(getString(R.string.sign_out_purpose_text));
            mSignInPreference.setOnPreferenceClickListener(mSignOutPreferenceListener);
        } else {
            mSignInPreference.setTitle(getString(R.string.sign_in));
            mSignInPreference.setSummary(R.string.sign_in_purpose_text);
            mSignInPreference.setOnPreferenceClickListener(mSignInPreferenceListener);
        }
    }

    private Preference.OnPreferenceClickListener mSignInPreferenceListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent signInIntent = new Intent(getActivity(), SignInActivity.class);
            startActivityForResult(signInIntent, SIGN_IN_ACTIVITY_CODE);
            return true;
        }
    };

    private Preference.OnPreferenceClickListener mSignOutPreferenceListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            FirebaseAuth.getInstance().signOut();
            return true;
        }
    };
}
