package com.example.packflow;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.packflow.databinding.FragmentHomePageBinding;
import com.example.packflow.databinding.ToolbarBinding;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomePageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomePageFragment extends Fragment {


    FragmentHomePageBinding binding;

    private StringBuilder scanBuffer;
    private boolean processingBarcode = false;



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomePageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomePageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomePageFragment newInstance(String param1, String param2) {
        HomePageFragment fragment = new HomePageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding= FragmentHomePageBinding.inflate(getLayoutInflater());
        setToolBar();

        binding.cnstWholeLayout.setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("backgroundThema", "#EFEFD0")));

        // Initialize barcode scanning capability
        scanBuffer = new StringBuilder(128);

        // Critical tablet optimizations
        binding.edtxtBarcode.setFocusable(false);
        binding.edtxtBarcode.setFocusableInTouchMode(false);

        if (getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            getActivity().getWindow().setWindowAnimations(0);
            getActivity().getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        binding.cnstWholeLayout.setFocusable(true);
        binding.cnstWholeLayout.setFocusableInTouchMode(true);
        binding.cnstWholeLayout.requestFocus();
        binding.cnstWholeLayout.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (processingBarcode && scanBuffer.length() > 0) {
                        String barcode = scanBuffer.toString();
                        scannerScanned(barcode);
                        processingBarcode = false;
                    }
                    return true;
                }
                char c = (char) event.getUnicodeChar();
                if (c != 0) {
                    if (!processingBarcode) {
                        processingBarcode = true;
                        scanBuffer.setLength(0);
                    }
                    scanBuffer.append(c);
                    return true;
                }
            }
            return false;
        });

        return binding.getRoot();
    }

    private void scannerScanned(String barcode) {

        String extractedData = extractForGLS(barcode);

        if(extractedData!=null){

            String weight=getWeight(barcode);


            if (weight!=null) {
                sendGLSPost(extractedData,weight);
            } else {
                showFeedback(false,"❌ FEHLER","Barcode enthält Fehler");
            }
        }else if(extractForDHL(barcode)!=null){
            sendDHLPost(barcode);
        }else{
            showFeedback(false,"❌ FEHLER","Barcode enthält Fehler");
        }

        binding.edtxtBarcode.setText("");
    }


    private void showFeedback(boolean isSuccess, String message, String details) {
        playSound(isSuccess);
        // Change background color based on success/failure
        binding.cnstWholeLayout.setBackgroundColor(isSuccess ? Color.parseColor("#4CAF50") : Color.parseColor("#F44336"));

        // Show feedback in your UI
        TextView feedbackText = binding.feedbackText; // Main feedback text
        TextView detailsText = binding.detailsText;   // Details text (add this to your layout)

        feedbackText.setText(message);
        feedbackText.setBackgroundColor(isSuccess ? Color.parseColor("#388E3C") : Color.parseColor("#D32F2F"));
        feedbackText.setTextColor(Color.WHITE);
        feedbackText.setVisibility(View.VISIBLE);

        // Show details if available
        if (details != null && !details.isEmpty()) {
            detailsText.setText(details);
            detailsText.setVisibility(View.VISIBLE);
        } else {
            detailsText.setVisibility(View.GONE);
        }

        // Automatically hide feedback after delay
        new Handler().postDelayed(() -> {
            // Restore original background
            binding.cnstWholeLayout.setBackgroundColor(Color.parseColor(
                    requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE)
                            .getString("backgroundThema", "#EFEFD0")));

            // Hide feedback text
            feedbackText.setVisibility(View.GONE);
            detailsText.setVisibility(View.GONE);

            // Request focus again to be ready for next scan
            binding.cnstWholeLayout.requestFocus();
        }, 1500); // 1.5 seconds - enough to read but not too long
    }

    private void playSound(boolean success) {
        try {
            ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            toneGen.startTone(success ? ToneGenerator.TONE_CDMA_PIP : ToneGenerator.TONE_CDMA_ABBR_ALERT);
        } catch (Exception e) {
            // Silent failure if sound can't play
        }
    }
    private void sendGLSPost(String trackId,String weight){
            String url = DataHolder.getInstance().getUrl()+"/Scanning/GLSParcel";

            // Create JSON request body
            JSONObject requestBody = new JSONObject();
            try {
                requestBody.put("userID",requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1));
                requestBody.put("trackID", trackId);
                requestBody.put("weight", formatWeight(weight));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    // Success case - simplified
                    showFeedback(true, "✅ " + trackId ,"Erfolgreich Hinzugefügt");
                },
                error -> {
                    String errorMessage = error.getMessage();

                    // Check for "OK" in error message which indicates success
                    if (errorMessage != null && errorMessage.trim().contains("OK")) {
                        showFeedback(true, "✅ " + trackId ,"Erfolgreich Hinzugefügt");
                        return;
                    }

                    // Error case - simplified
                    String errorDetails = "";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        errorDetails = "Code: " + statusCode;
                    }
                    String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                    showFeedback(false, "❌ " + trackId + " " + errorDetails,body);
                });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void sendDHLPost(String barcode) {
        String url = DataHolder.getInstance().getUrl()+"/Scanning/DHL/Parcel?scannedCode="+barcode+"&userId="+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Success case - simplified
                    showFeedback(true, "✅ " + barcode, "Erfolgreich Hinzugefügt");
                },
                error -> {
                    String errorMessage = error.getMessage();
                    // Check for "OK" in error message which indicates success
                    if (errorMessage != null && errorMessage.trim().contains("OK")) {
                        showFeedback(true, "✅ " + barcode, "Erfolgreich Hinzugefügt");
                        return;
                    }

                    // Error case - simplified
                    String errorDetails = "";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        errorDetails = "Code: " + statusCode;
                        String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                        showFeedback(false, "❌ " + barcode + " " + errorDetails, body);
                    } else {
                        showFeedback(false, "❌ " + barcode, "Keine Netzwerkantwort erhalten.\nFehlermeldung: " + errorMessage);
                    }
                });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private String getWeight(String barcode) {
        // Define the regex pattern: 3 or more spaces followed by exactly 5 digits
        String regex = "\\s(\\d{5})(?!.{0,29}[a-zA-Z])";  // Matches 3 or more spaces, then 5 digits

        // Create a pattern and matcher
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(barcode);

        // Check if the pattern is found
        if (matcher.find()) {
            // Return the matched 5 digits (group 1 captures the digits)
            return matcher.group(1);
        }
        // Return null if no 5-digit pattern is found
        return null;
    }

    public double formatWeight(String weight) {

        int kg = Integer.parseInt(weight.substring(0, 3)); // Convert "020" → 20
        int g = Integer.parseInt(weight.substring(3, 5));  // Convert "00" → 0, "50" → 50

        // Combine as double
        return Double.parseDouble(kg +"."+ g); // Example: 20 + (00 / 100) → 20.00
    }


    // Method to extract the substring using regex and ensure it's 8 characters

    private String extractForGLS(String barcode) {
        // Create a pattern to match either "Y" at the front or the prefix at the front
        String regexForY = "Y([A-Za-z0-9]{6})(" + String.join("|", DataHolder.getInstance().getPrefixList()) + ")";
        String regexForPrefix = "(" + String.join("|", DataHolder.getInstance().getPrefixList()) + ")([A-Za-z0-9]{6})(Y)";

        Pattern patternY = Pattern.compile(regexForY);
        Matcher matcherY = patternY.matcher(barcode);

        Pattern patternZ = Pattern.compile(regexForPrefix);
        Matcher matcherZ = patternZ.matcher(barcode);

        // Check if "Y" is at the front
        if (matcherY.find()) {
            String start = matcherY.group(1);   // "Y"
            String middle = matcherY.group(2);  // 6-character section
            String prefix = matcherY.group(3);// Prefix
            return prefix + middle + start;
        }

        // Check if Prefix is at the front
        // If there's a match
        if (matcherZ.find()) {
            String prefix = matcherZ.group(1);  // Prefix
            String middle = matcherZ.group(2);  // 6-character section
            String end = matcherZ.group(3);
            return prefix + middle + end;  // Return "prefix + middle + Y"

        }

        // Return null if no match for both cases
        return null;
    }

    private String extractForDHL(String barcode) {
        // Regular expression to match any sequence of digits, parentheses, and plus signs
        String regex = "[\\d\\(\\)\\+]+";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(barcode);

        // If there's a match, return it
        if (matcher.matches()) {
            return matcher.group(0);
        }

        // Return null if no match
        return null;
    }
    private void setToolBar(){

        ToolbarBinding toolbar=binding.homepageToolBar;
        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
            toolbar.title.setText("\uD83C\uDFE0 Haupt-Seite");
        }else{
            toolbar.title.setText("\uD83C\uDFE0 Homepage");
        }


        toolbar.leftIcon.setVisibility(View.INVISIBLE);
        toolbar.leftLogoutIcon.setVisibility(View.VISIBLE);
        toolbar.profileIcon.setVisibility(View.VISIBLE);
        toolbar.settingsIcon.setVisibility(View.VISIBLE);
        toolbar.statisticIcon.setVisibility(View.VISIBLE);


        toolbar.getRoot().setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("toolbarThema", "#FF6B35")));


        toolbar.profileIcon.setOnClickListener(v -> replaceFragment(new ProfileFragment()));
        toolbar.leftLogoutIcon.setOnClickListener(v -> replaceFragment(new LoginFragment()));
        toolbar.settingsIcon.setOnClickListener(v -> replaceFragment(new SettingsFragment()));
        toolbar.statisticIcon.setOnClickListener(v -> replaceFragment(new StatisticFragment()));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ensure our layout has focus to capture keyboard events
        binding.cnstWholeLayout.requestFocus();

        // Force hardware acceleration
        if (getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
    }

    private void ShowPopUp(String popUpCode, String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

        switch (popUpCode){
            case "successful":

                builder.setTitle("Successful")
                        .setMessage(message)
                        .setPositiveButton("OK", ((dialog, which) -> dialog.dismiss()));


                break;
            case "not_successful":
                builder.setTitle("Error")
                        .setMessage(message)
                        .setPositiveButton("OK", ((dialog, which) -> dialog.dismiss()));
                break;
            default:
                builder.setTitle(popUpCode)
                        .setMessage(message)
                        .setPositiveButton("OK", ((dialog, which) -> dialog.dismiss()));
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void replaceFragment(Fragment fragment) {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}