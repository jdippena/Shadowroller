package com.imber.shadowroller.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.ViewTreeObserver;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;
import com.imber.shadowroller.data.DbHelper;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {
    private final static String TAG = MainActivity.class.getCanonicalName();

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private CommonRollsFragment mCommonRollsFragment;
    private SimpleTestFragment mSimpleTestFragment;
    private ExtendedTestFragment mExtendedTestFragment;
    private ProbabilityFragment mProbabilityFragment;
    private HistoryFragment mHistoryFragment;

    private static final String COMMON_ROLLS_FRAG_KEY = "common_rolls";
    private static final String SIMPLE_TEST_FRAG_KEY = "simple_test";
    private static final String EXTENDED_TEST_FRAG_KEY = "extended_test";
    private static final String PROBABILITY_FRAG_KEY = "probability";

    public DiceRollerView mDiceRollerView;

    private static final String PROBABILITY_TABLES_INITIALIZED_KEY = "prob_initialized";
    private static final String SELECTED_PAGE_KEY = "selected_page";
    private int mSelectedPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDiceRollerView = (DiceRollerView) findViewById(R.id.dice_roller_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        if (savedInstanceState != null) {
            mSelectedPage = savedInstanceState.getInt(SELECTED_PAGE_KEY, 1);
            if (mSelectedPage == 0) {
                mDiceRollerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int translation = Util.isRTL(getApplicationContext()) ?
                                -mDiceRollerView.getWidth() : mDiceRollerView.getWidth();
                        mDiceRollerView.setTranslationX(translation);
                    }
                });
            }
        }
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(mSelectedPage);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOffscreenPageLimit(3);
        onPageSelected(mSelectedPage);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if (!getSharedPreferences(getString(R.string.file_name_initialize_prob_tables_shared_pref),  MODE_PRIVATE)
                .getBoolean(PROBABILITY_TABLES_INITIALIZED_KEY, false)) {
            new InitializeProbabilityTables().execute();
        }

        if (getResources().getBoolean(R.bool.is_large)) {
            mHistoryFragment = HistoryFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.history_container, mHistoryFragment)
                    .commit();
            mDiceRollerView.setHistoryListener(mHistoryFragment);
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
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, COMMON_ROLLS_FRAG_KEY, mCommonRollsFragment);
        getSupportFragmentManager().putFragment(outState, SIMPLE_TEST_FRAG_KEY, mSimpleTestFragment);
        getSupportFragmentManager().putFragment(outState, EXTENDED_TEST_FRAG_KEY, mExtendedTestFragment);
        getSupportFragmentManager().putFragment(outState, PROBABILITY_FRAG_KEY, mProbabilityFragment);
        outState.putInt(SELECTED_PAGE_KEY, mSelectedPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCommonRollsFragment = (CommonRollsFragment)
                getSupportFragmentManager().getFragment(savedInstanceState, COMMON_ROLLS_FRAG_KEY);
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
            float offset = Util.isRTL(this) ? (positionOffset - 1) * width : (1 - positionOffset) * width;
            mDiceRollerView.setTranslationX(offset);
        } else {
            mDiceRollerView.setTranslationX(0);
        }
    }

    @Override
    public void onPageSelected(int position) {
        mSelectedPage = position;
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

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    mCommonRollsFragment = CommonRollsFragment.newInstance();
                    mCommonRollsFragment.setHistoryListener(mHistoryFragment);
                    return mCommonRollsFragment;
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
                case 0:
                    if (mCommonRollsFragment != null && getResources().getBoolean(R.bool.is_large)) {
                        mCommonRollsFragment.setHistoryListener(mHistoryFragment);
                    }
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
            SharedPreferences.Editor editor = getSharedPreferences(
                    getString(R.string.file_name_initialize_prob_tables_shared_pref), MODE_PRIVATE).edit();
            editor.putBoolean(PROBABILITY_TABLES_INITIALIZED_KEY, true);
            editor.apply();
            return null;
        }
    }
}
