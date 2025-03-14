package com.example.packflow;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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



  /*      binding.edtxtBarcode.requestFocus();
           hideKeyboard();

        binding.edtxtBarcode.setOnEditorActionListener((v, actionId, event) -> {
            // Check if it's the "Done" action or "Enter" key (KEYCODE_ENTER)
            if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                String barcode = binding.edtxtBarcode.getText().toString().trim();

                // Process barcode if it's not empty
                if (!barcode.isEmpty()) {
                    // Call the barcode processing function
                    scannerScanned(barcode);
                }
                // Hide keyboard only when action is performed
                return true;
            }
            return false;
        });
*/
        binding.edtxtBarcode.requestFocus();
        binding.edtxtBarcode.addTextChangedListener(new TextWatcher() {
            private static final long SCAN_DELAY = 300;  // Delay time (in milliseconds) to wait for full input

            private Handler handler = new Handler();
            private Runnable processBarcodeRunnable;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                // No need to handle this method for now
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Remove any pending previous runnable (to avoid multiple invocations)
                if (processBarcodeRunnable != null) {
                    handler.removeCallbacks(processBarcodeRunnable);
                }

                // Add a new runnable that processes the input after the delay
                processBarcodeRunnable = new Runnable() {
                    @Override
                    public void run() {
                        String barcode = charSequence.toString().trim();

                        if (!barcode.isEmpty()) {
                            // Call your barcode processing function here
                            scannerScanned(barcode);

                        }
                    }
                };

                // Post the runnable with a delay to process the text after a small timeout
                handler.postDelayed(processBarcodeRunnable, SCAN_DELAY);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No need to handle this method for now
            }
        });
        binding.cnstWholeLayout.setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("backgroundThema", "#8692f7")));
        return binding.getRoot();
    }

    // Hide the soft keyboard
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputMethodManager != null && getView() != null) {
            inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
    private void scannerScanned(String barcode) {

        String extractedData = extractFromYtoZ(barcode);

        if(extractedData!=null){

            String weight=getWeight(barcode);


            if (weight!=null) {
                sendGLSPost(extractedData,weight);
            } else {
                ShowPopUp("❌ FEHLER","Barcode enthält Fehler"+barcode);
            }
        }else{
            sendDHLPost(barcode);
        }

        binding.edtxtBarcode.setText("");
    }
    public double formatWeight(String weight) {

        int kg = Integer.parseInt(weight.substring(0, 3)); // Convert "020" → 20
        int g = Integer.parseInt(weight.substring(3, 5));  // Convert "00" → 0, "50" → 50

        // Combine as double
        return Double.parseDouble(kg +"."+ g); // Example: 20 + (00 / 100) → 20.00
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

        // Create a new request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {

                            if (response == null || response.length() == 0) {
                                ShowPopUp("✅ ERFOLGREICH", "Paket wurde erfolgreich hinzugefügt! (Keine Serverantwort)");
                            } else {
                                ShowPopUp("✅ ERFOLGREICH", "Paket wurde erfolgreich hinzugefügt!");
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMessage = error.getMessage(); // Get the error message

                            if (errorMessage != null) {
                                // Check if the error message contains "OK"
                                if (errorMessage.trim().contains("OK")) {
                                    ShowPopUp("✅ ERFOLGREICH", "Paket wurde erfolgreich hinzugefügt!");
                                    return;
                                }
                            }
                            if (error.networkResponse != null) {
                                int statusCode = error.networkResponse.statusCode;
                                String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);

                                ShowPopUp("❌ FEHLERCODE:"+statusCode,"Paket wurde nicht hinzugefügt! \n"+body);
                                return;
                            }

                            ShowPopUp("❌ FEHLERCODE", "Keine Netzwerkantwort erhalten.\nFehlermeldung: " + errorMessage);
                        }
                    });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }

    private void sendDHLPost(String barcode){
        String url = DataHolder.getInstance().getUrl()+"/Scanning/DHL/Parcel?scannedCode="+barcode+"&userId="+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response == null || response.trim().isEmpty()) {
                            ShowPopUp("✅ ERFOLGREICH", "Paket wurde erfolgreich hinzugefügt! (Keine Serverantwort)");
                        } else {
                            ShowPopUp("✅ ERFOLGREICH", "Paket wurde erfolgreich hinzugefügt!");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            ShowPopUp("❌ FEHLERCODE:"+statusCode,"Paket wurde nicht hinzugefügt! \n"+body);
                        }else {

                            ShowPopUp("❌ FEHLERCODE ","Keine Netzwerkantwort erhalten.\nFehlermeldung: " + error.getMessage());
                        }
                    }
                });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }


    // Method to extract the substring using regex and ensure it's 8 characters
    private String extractFromYtoZ(String barcode) {
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

    private void richtig(){
        binding.cnstWholeLayout.setBackgroundColor(Color.GREEN);

        // Reset the background color to original after 2 seconds (2000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.cnstWholeLayout.setBackgroundColor(Color.WHITE); // or your default color
            }
        }, 2000);
    }

    private void falsch(){
        binding.cnstWholeLayout.setBackgroundColor(Color.RED);


        ShowPopUp("Fehler","Dieses Paket wurde schon gescannt");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.cnstWholeLayout.setBackgroundColor(Color.WHITE); // or your default color
            }
        }, 2000);

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


        toolbar.getRoot().setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("toolbarThema", "#8692f7")));

        toolbar.profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ProfileFragment());
            }
        });
        toolbar.leftLogoutIcon.setOnClickListener(v -> {replaceFragment(new LoginFragment());});

        toolbar.settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new SettingsFragment());
            }
        });

        toolbar.statisticIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new StatisticFragment());
            }
        });
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