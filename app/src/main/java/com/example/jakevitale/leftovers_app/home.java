package com.example.jakevitale.leftovers_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void goToStore (View view) {
        Intent intent = new Intent(this, store.class);
        startActivity(intent);
    }

    public void goToDressingRoom (View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToPastLookBooks (View view) {
        Intent intent = new Intent(this, past_lookbooks.class);
        startActivity(intent);
    }
}
