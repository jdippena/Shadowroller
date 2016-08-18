package com.imber.shadowroller.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;
import com.imber.shadowroller.data.DbHelper;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private final static String TAG = MainActivity.class.getCanonicalName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private SimpleTestFragment mSimpleTestFragment;
    private ExtendedTestFragment mExtendedTestFragment;
    private ProbabilityFragment mProbabilityFragment;

    private static final String SIMPLE_TEST_FRAG_KEY = "simple_test";
    private static final String EXTENDED_TEST_FRAG_KEY = "extended_test";
    private static final String PROBABILITY_FRAG_KEY = "probability";

    public DiceRollerView mDiceRollerView;

    private static final String PROBABILITY_TABLES_INITIALIZED_KEY = "prob_initialized";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDiceRollerView = (DiceRollerView) findViewById(R.id.dice_roller_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(3);
        onPageSelected(1);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (!PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(PROBABILITY_TABLES_INITIALIZED_KEY, false)) {
            new InitializeProbabilityTables().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_history:
                Intent historyIntent = new Intent(this, HistoryActivity.class);
                startActivity(historyIntent);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, SIMPLE_TEST_FRAG_KEY, mSimpleTestFragment);
        getSupportFragmentManager().putFragment(outState, EXTENDED_TEST_FRAG_KEY, mExtendedTestFragment);
        getSupportFragmentManager().putFragment(outState, PROBABILITY_FRAG_KEY, mProbabilityFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSimpleTestFragment = (SimpleTestFragment)
                getSupportFragmentManager().getFragment(savedInstanceState, SIMPLE_TEST_FRAG_KEY);
        mExtendedTestFragment = (ExtendedTestFragment)
                getSupportFragmentManager().getFragment(savedInstanceState, EXTENDED_TEST_FRAG_KEY);
        mProbabilityFragment = (ProbabilityFragment)
                getSupportFragmentManager().getFragment(savedInstanceState, PROBABILITY_FRAG_KEY);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position == 0) {
            int width = mDiceRollerView.getWidth();
            mDiceRollerView.setTranslationX((1 - positionOffset) * width);
        }
    }

    @Override
    public void onPageSelected(int position) {
        Util.TestType type;
        switch (position) {
            case 1:
                type = Util.TestType.SIMPLE_TEST;
                break;
            case 2:
                type = Util.TestType.EXTENDED_TEST;
                break;
            case 3:
                type = Util.TestType.PROBABILITY;
                break;
            default:
                type = Util.TestType.SIMPLE_TEST;
        }
        mDiceRollerView.setType(type);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    public void setSimpleTestDiceListener(Util.SimpleTestDiceListener simpleTestDiceListener) {
        mDiceRollerView.setSimpleTestDiceListener(simpleTestDiceListener);
    }

    public void setExtendedTestDiceListener(Util.ExtendedTestDiceListener extendedTestDiceListener) {
        mDiceRollerView.setExtendedTestDiceListener(extendedTestDiceListener);
    }

    public void setProbabilityDiceListener(Util.ProbabilityDiceListener probabilityDiceListener) {
        mDiceRollerView.setProbabilityDiceListener(probabilityDiceListener);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return CommonRollsFragment.newInstance();
                case 1:
                    mSimpleTestFragment = SimpleTestFragment.newInstance();
                    mDiceRollerView.setSimpleTestDiceListener(mSimpleTestFragment);
                    return mSimpleTestFragment;
                case 2:
                    mExtendedTestFragment = ExtendedTestFragment.newInstance();
                    mDiceRollerView.setExtendedTestDiceListener(mExtendedTestFragment);
                    return mExtendedTestFragment;
                case 3:
                    mProbabilityFragment = ProbabilityFragment.newInstance();
                    mDiceRollerView.setProbabilityDiceListener(mProbabilityFragment);
                    return mProbabilityFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.name_saved_rolls);
                case 1:
                    return getString(R.string.name_test_simple);
                case 2:
                    return getString(R.string.name_test_extended);
                case 3:
                    return getString(R.string.name_probability);
                default:
                    return null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 1:
                    mDiceRollerView.setSimpleTestDiceListener(mSimpleTestFragment);
                    break;
                case 2:
                    mDiceRollerView.setExtendedTestDiceListener(mExtendedTestFragment);
                    break;
                case 3:
                    mDiceRollerView.setProbabilityDiceListener(mProbabilityFragment);
                    break;
            }
            return super.instantiateItem(container, position);
        }
    }

    public class InitializeProbabilityTables extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "initializeProbabilityTables: ");
            DbHelper.initializeProbabilityTables(getApplicationContext());
            SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putBoolean(PROBABILITY_TABLES_INITIALIZED_KEY, true);
            editor.apply();
            return null;
        }
    }
}
