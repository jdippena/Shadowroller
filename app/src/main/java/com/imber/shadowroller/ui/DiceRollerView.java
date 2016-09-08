package com.imber.shadowroller.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
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
    private CheckBox mCumulativeCheckbox;
    private RadioGroup mModifiers;

    private Util.SimpleTestDiceListener mSimpleTestDiceListener;
    private Util.ExtendedTestDiceListener mExtendedTestDiceListener;
    private Util.ProbabilityDiceListener mProbabilityDiceListener;
    private Util.HistoryListener mHistoryListener;

    private Random mRandom = new Random();
    private int mDice;
    private Util.TestType mTestType;
    private Util.TestModifier mModifierStatus = Util.TestModifier.NONE;

    private static final String DICE_KEY = "dice";
    private static final String STATE_KEY = "state";

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
        mDice = context.getResources().getInteger(R.integer.default_dice_number);

        LayoutInflater.from(context).inflate(R.layout.layout_dice_roller, this, true);
        mBigRedButton = (TextView) findViewById(R.id.big_red_button);
        mMinusButton = (TextView) findViewById(R.id.minus_button);
        mPlusButton = (TextView) findViewById(R.id.plus_button);
        mEdgeCheckbox = (CheckBox) findViewById(R.id.edge_check_box);
        mCumulativeCheckbox = (CheckBox) findViewById(R.id.cumulative_check_box);
        mModifiers = (RadioGroup) findViewById(R.id.radio_group_roll_modifiers);
        for (int i = 0; i < mModifiers.getChildCount(); i++) {
            mModifiers.getChildAt(i).setEnabled(false);
        }
        setType(Util.TestType.SIMPLE_TEST);

        mBigRedButton.setText(String.valueOf(mDice));
        mBigRedButton.setOnClickListener(new BigRedButtonClickListener());

        mModifiers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setModifierStatus(checkedId);
            }
        });
        mEdgeCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setModifiersEnabled(isChecked);
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
                setDice(mDice - 1);
            }
        });

        mPlusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setDice(mDice + 1);
            }
        });
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable state = super.onSaveInstanceState();
        Bundle out = new Bundle(2);
        out.putInt(DICE_KEY, mDice);
        out.putParcelable(STATE_KEY, state);
        return out;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mDice = bundle.getInt(DICE_KEY);
            mBigRedButton.setText(String.valueOf(mDice));
            state = bundle.getParcelable(STATE_KEY);
        }
        super.onRestoreInstanceState(state);
    }

    public void setType(Util.TestType type) {
        mTestType = type;
        switch (mTestType) {
            case SIMPLE_TEST:
                mEdgeCheckbox.setEnabled(true);
                mCumulativeCheckbox.setEnabled(false);
                setModifiersEnabled(true);
                break;
            case EXTENDED_TEST:
                mEdgeCheckbox.setEnabled(false);
                mCumulativeCheckbox.setEnabled(false);
                setModifiersEnabled(false);
                break;
            case PROBABILITY:
                mEdgeCheckbox.setEnabled(true);
                mCumulativeCheckbox.setEnabled(true);
                setModifiersEnabled(true);
                setDice(mDice);
                break;
        }
    }

    private void setModifiersEnabled(boolean enabled) {
        for (int i = 0; i < mModifiers.getChildCount(); i++) {
            mModifiers.getChildAt(i).setEnabled(enabled);
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

    public void setHistoryListener(Util.HistoryListener historyListener) {
        this.mHistoryListener = historyListener;
    }

    private void setDice(int dice) {
        mDice = Util.getBoundedDiceNumber(getResources(), dice, mTestType);
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

    private class BigRedButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ArrayList<int[]> result = new ArrayList<>();
            Util.RollStatus rollStatus = Util.RollStatus.NORMAL;
            switch (mTestType) {
                case SIMPLE_TEST:
                    result = Util.doSimpleRoll(mRandom, mDice, mModifierStatus);
                    mSimpleTestDiceListener.onRollPerformed(result, mModifierStatus);
                    rollStatus = Util.getRollStatus(result.get(0));
                    break;
                case EXTENDED_TEST:
                    result = Util.doExtendedRoll(mRandom, mDice);
                    mExtendedTestDiceListener.onExtendedRollPerformed(result);
                    break;
                case PROBABILITY:
                    mProbabilityDiceListener.onProbabilityQueried(
                            Util.buildProbabilityUri(mModifierStatus, mDice, mCumulativeCheckbox.isChecked()));
                    return;
            }
            boolean rtl = Util.isRTL(mContext);
            Util.insertIntoHistoryTable(getContext().getContentResolver(),
                    mDice, Util.countSuccesses(result), Util.resultToOutput(result, rtl), false,
                    mTestType, mModifierStatus, rollStatus);
            if (mContext.getResources().getBoolean(R.bool.is_large)) {
                mHistoryListener.notifyItemInserted();
            }
        }
    }
}
