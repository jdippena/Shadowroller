package com.imber.shadowroller.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.imber.shadowroller.R;
import com.imber.shadowroller.Util;

public class AddCommonRollDialogFragment extends DialogFragment {
    private String mName;
    private int mDice;
    private long mId;
    private TextInputEditText mNameEditText;
    private TextInputEditText mDiceEditText;
    private AddDialogCallbacks mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.layout_common_roll_add, null);
        mNameEditText = (TextInputEditText) rootView.findViewById(R.id.edit_text_common_rolls_add_name);
        mDiceEditText = (TextInputEditText) rootView.findViewById(R.id.edit_text_common_rolls_add_dice);
        TextView minusButton = (TextView) rootView.findViewById(R.id.minus_button);
        TextView plusButton = (TextView) rootView.findViewById(R.id.plus_button);

        Bundle args = getArguments();
        int titleId;
        if (args != null) {
            mName = args.getString(CommonRollsFragment.NAME_KEY);
            mDice = args.getInt(CommonRollsFragment.DICE_KEY);
            mId = args.getLong(CommonRollsFragment.ID_KEY);
            titleId = R.string.title_common_roll_add_edit;
        } else {
            mName = "";
            mDice = getResources().getInteger(R.integer.default_dice_number);
            mId = -1;
            titleId = R.string.title_common_roll_add;
        }

        mDiceEditText.setText(String.valueOf(mDice));
        mDiceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    mDice = 1;
                } else {
                    mDice = Integer.valueOf(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mDiceEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setDiceValWithDefault();
                }
            }
        });
        mDiceEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setDiceValWithDefault();
                    processDone();
                    return true;
                }
                return false;
            }
        });

        mNameEditText.setText(mName);

        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDice(mDice - 1);
            }
        });
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDice(mDice + 1);
            }
        });

        builder.setView(rootView)
                .setTitle(titleId)
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        processDone();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNegativeClicked();
                    }
                });
        mNameEditText.requestFocus();
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }

    private void setDiceValWithDefault() {
        String value = mDiceEditText.getText().toString();
        if (value.equals("") || Integer.valueOf(value) == 0) {
            value = "1";
        }
        setDice(Integer.valueOf(value));
    }

    private void processDone() {
        mListener.onPositiveClicked(mNameEditText.getText().toString(), mDice, mId);
    }

    private void setDice(int dice) {
        mDice = Util.getBoundedDiceNumber(getResources(), dice, Util.TestType.SIMPLE_TEST);
        mDiceEditText.setText(String.valueOf(mDice));
    }

    public void setCallback(AddDialogCallbacks listener) {
        mListener = listener;
    }

    public interface AddDialogCallbacks {
        void onPositiveClicked(String name, int dice, long id);
        void onNegativeClicked();
    }
}