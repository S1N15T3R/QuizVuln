package com.team404bnf.quizvuln.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.team404bnf.quizvuln.R;

public class LoadingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        // NOTE: You should launch this LoadingActivity
        // when API call starts, and close it when data is ready.
    }
}
