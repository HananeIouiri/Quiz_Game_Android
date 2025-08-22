package com.example.quizappculture;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("testConnexion").setValue("Hello Firebase!");

    }
}