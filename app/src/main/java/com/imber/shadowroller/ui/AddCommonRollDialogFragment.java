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

public class AddCommonRollDialogFragment extends DialogFragment {
    private String mName = "";
    private int mDice = 10;
    private TextInputEditText mNameEditText;
    private TextInputEditText mDiceEditText;
    private TextView mMinusButton;
    private TextView mPlusButton;
    private AddDialogCallbacks mListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.layout_common_roll_add, null);
        mNameEditText = (TextInputEditText) rootView.findViewById(R.id.edit_text_common_rolls_add_name);
        mDiceEditText = (TextInputEditText) rootView.findViewById(R.id.edit_text_common_rolls_add_dice);
        mMinusButton = (TextView) rootView.findViewById(R.id.minus_button);
        mPlusButton = (TextView) rootView.findViewById(R.id.plus_button);

        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setDefaultNameValue(mNameEditText.getText().toString());
                }
            }
        });
        mNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setDefaultNameValue(mNameEditText.getText().toString());
                    mDiceEditText.requestFocus();
                }
                return false;
            }
        });

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
                    setDefaultDiceValue();
                }
            }
        });
        mDiceEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    setDefaultDiceValue();
                    processDone();
                    return true;
                }
                return false;
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementDice();
            }
        });
        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementDice();
            }
        });

        builder.setView(rootView)
                .setTitle(R.string.title_common_roll_add)
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

    private void setDefaultNameValue(String name) {
        if (name.equals("")) {
            mName = getString(R.string.untitled);
            mNameEditText.setText(mName);
        } else {
            mName = name;
        }
    }

    private void setDefaultDiceValue() {
        String value = mDiceEditText.getText().toString();
        value = value.replaceFirst("^0+(?!$)", ""); // remove leading zeros
        if (value.equals("") || Integer.valueOf(value) == 0) {
            mDice = 1;
            mDiceEditText.setText(String.valueOf(mDice));
        } else {
            mDice = Integer.valueOf(value);
            mDiceEditText.setText(String.valueOf(mDice));
        }
    }

    private void processDone() {
        mListener.onPositiveClicked(mName, mDice);
    }

    private void decrementDice() {
        mDice = Math.max(1, mDice - 1);
        mDiceEditText.setText(String.valueOf(mDice));
    }

    private void incrementDice() {
        mDice++;
        mDiceEditText.setText(String.valueOf(mDice));
    }

    public void setCallback(AddDialogCallbacks listener) {
        mListener = listener;
    }

    public interface AddDialogCallbacks {
        void onPositiveClicked(String name, int dice);
        void onNegativeClicked();
    }
}