package com.imber.shadowroller.ui;

import android.content.Context;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;

import java.util.ArrayList;
import java.util.Random;

public class DiceRollerView extends RelativeLayout {
    private static final String TAG = DiceRollerView.class.getCanonicalName();

    private Context mContext;
    private TextView mBigRedButton;
    private TextView mMinusButton;
    private TextView mPlusButton;
    private CheckBox mEdgeCheckbox;
    private RadioGroup mModifiers;

    private Util.SimpleTestDiceListener mSimpleTestDiceListener;
    private Util.ExtendedTestDiceListener mExtendedTestDiceListener;
    private Util.ProbabilityDiceListener mProbabilityDiceListener;

    private Random mRandom = new Random();
    private int mDice = 100;
    private Util.TestModifier mModifierStatus = Util.TestModifier.NONE;

    public DiceRollerView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public DiceRollerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public DiceRollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_dice_roller, this, true);
        mBigRedButton = (TextView) findViewById(R.id.big_red_button);
        mMinusButton = (TextView) findViewById(R.id.minus_button);
        mPlusButton = (TextView) findViewById(R.id.plus_button);
        mEdgeCheckbox = (CheckBox) findViewById(R.id.edge_check_box);
        mModifiers = (RadioGroup) findViewById(R.id.radio_group_roll_modifiers);
        for (int i = 0; i < mModifiers.getChildCount(); i++) {
            mModifiers.getChildAt(i).setEnabled(false);
        }

        mBigRedButton.setText(String.valueOf(mDice));
        mModifiers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setModifierStatus(checkedId);
            }
        });
        mEdgeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (int i = 0; i < mModifiers.getChildCount(); i++) {
                    mModifiers.getChildAt(i).setEnabled(isChecked);
                }
                if (isChecked) {
                      setModifierStatus(mModifiers.getCheckedRadioButtonId());
                } else {
                    mModifierStatus = Util.TestModifier.NONE;
                }
            }
        });

        mMinusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementDice();
            }
        });

        mPlusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementDice();
            }
        });
    }

    public void setType(Util.TestType type) {
        switch (type) {
            case SIMPLE_TEST:
                mBigRedButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<int[]> result = doSimpleRoll();
                        mSimpleTestDiceListener.onRollPerformed(result, mModifierStatus);
                    }
                });
                mEdgeCheckbox.setVisibility(VISIBLE);
                mModifiers.setVisibility(VISIBLE);
                break;
            case EXTENDED_TEST:
                mBigRedButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<int[]> result = doExtendedRoll();
                        mExtendedTestDiceListener.onExtendedRollPerformed(result);
                    }
                });
                mEdgeCheckbox.setVisibility(GONE);
                mModifiers.setVisibility(GONE);
                break;
            case PROBABILITY:
                mBigRedButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        float[] probs = doProbabilityRoll();
                        mProbabilityDiceListener.onProbabilityQueried(probs, mModifierStatus);
                    }
                });
                mEdgeCheckbox.setVisibility(VISIBLE);
                mModifiers.setVisibility(VISIBLE);
                break;
        }
    }

    public void setSimpleTestDiceListener(Util.SimpleTestDiceListener listener) {
        mSimpleTestDiceListener = listener;
    }

    public void setExtendedTestDiceListener(Util.ExtendedTestDiceListener listener) {
        mExtendedTestDiceListener = listener;
    }

    public void setProbabilityDiceListener(Util.ProbabilityDiceListener listener) {
        mProbabilityDiceListener = listener;
    }

    private ArrayList<int[]> doSimpleRoll() {
        ArrayList<int[]> result = new ArrayList<>();
        switch (mModifierStatus) {
            case NONE:
                result.add(Util.doSingleRoll(mRandom, mDice));
                break;
            case RULE_OF_SIX:
                int numSixes = mDice;
                do {
                    int[] roll = Util.doSingleRoll(mRandom, numSixes);
                    result.add(roll);
                    numSixes = Util.countSixes(roll);
                } while (numSixes > 0);
                break;
            case PUSH_THE_LIMIT:
                int[] roll = Util.doSingleRoll(mRandom, mDice);
                result.add(roll);
                int successes = Util.countSuccesses(result);
                if (successes != mDice) {
                    int[] reRoll = Util.doSingleRoll(mRandom, mDice - successes);
                    result.add(reRoll);
                }
                break;
        }
        return result;
    }

    private ArrayList<int[]> doExtendedRoll() {
        ArrayList<int[]> result = new ArrayList<>(mDice);
        for (int i = 0; i < mDice; i++) {
            result.add(Util.doSingleRoll(mRandom, mDice - i));
        }
        return result;
    }

    private float[] doProbabilityRoll() {
        // TODO: get things from ContentResolver
        return null;
    }

    private void decrementDice() {
        mDice = Math.max(1, mDice - 1);
        mBigRedButton.setText(String.valueOf(mDice));
    }

    private void incrementDice() {
        mDice++;
        mBigRedButton.setText(String.valueOf(mDice));
    }

    private void setModifierStatus(@IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radio_button_modifiers_rule_of_six:
                mModifierStatus = Util.TestModifier.RULE_OF_SIX;
                break;
            case R.id.radio_button_modifiers_push_the_limit:
                mModifierStatus = Util.TestModifier.PUSH_THE_LIMIT;
                break;
        }
    }
}
