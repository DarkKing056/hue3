package com.example.packflow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.packflow.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        newerBackCall();

        replaceFragment(new LoginFragment());
        if (savedInstanceState == null) {
        }

       binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    // Add this method for newer Android versions (API 33+)
    public void newerBackCall() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if the current fragment is LoginFragment
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof HomePageFragment || currentFragment instanceof LoginFragment) {
                    // Prevent back press if on LoginFragment (or take any custom action)
                    return; // Ignore the back press
                }

                // Allow the standard back action
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}