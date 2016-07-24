package com.imber.shadowroller.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.imber.shadowroller.R;

public class HistoryActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear_history:
                HistoryFragment fragment = (HistoryFragment) getSupportFragmentManager().getFragments().get(0);
                fragment.clearHistory();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
