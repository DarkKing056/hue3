package com.example.packflow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.packflow.databinding.FragmentLoginBinding;
import com.example.packflow.databinding.ToolbarBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {


    private FragmentLoginBinding binding;




    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding= FragmentLoginBinding.inflate(getLayoutInflater());

        setToolBar();

        binding.edtxUsername.requestFocus();
        //btn anmelden
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryLogin(binding.edtxUsername.getText(),binding.edtxtPassword.getText());
            }
        });

        return binding.getRoot();
    }


    private void setToolBar(){
        ToolbarBinding toolbar=binding.loginToolBar;
        binding.cnstWholeLayout.setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("backgroundThema", "#f78692")));
        binding.btnLogin.setBackgroundResource(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getInt("btnThema",R.drawable.custom_button_pattern1));

        toolbar.getRoot().setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("toolbarThema", "#E6E6FA")));
        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
            toolbar.title.setText("Anmelde-Seite");
            binding.edtxUsername.setHint("Benutzername");
            binding.edtxtPassword.setHint("Passwort");
            binding.btnLogin.setText("ANMELDEN");
        }else{
            toolbar.title.setText("Login");
            binding.edtxUsername.setHint("Username");
            binding.edtxtPassword.setHint("Password");
            binding.btnLogin.setText("LOGIN");
        }
        toolbar.leftIcon.setVisibility(View.INVISIBLE);
        toolbar.profileIcon.setVisibility(View.INVISIBLE);
        toolbar.settingsIcon.setVisibility(View.INVISIBLE);
    }

    private void tryLogin(Editable username, Editable password) {

        String url=DataHolder.getInstance().getUrl()+"/Users/Authentication?username="+username.toString()+"&password="+password.toString();


        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // If we receive a 200 OK, the server sends no response body
                        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                            ShowPopUp("✅ Erfolgreich", "Wilkommen!");
                        }else{
                            ShowPopUp("✅ Successful", "Welcome!");
                        }
                        requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putString("pass", password.toString()).apply();

                        getUserInfo(username.toString());

                        replaceFragment(new HomePageFragment());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String errorMessage = new String(error.networkResponse.data);

                            if (statusCode == 400) {
                                // Handle Bad Request (e.g., input validation failure)
                                if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                                    ShowPopUp("❌ Fehler", "Benutzername oder Passwort ist falsch!");
                                }else{
                                    ShowPopUp("❌ Error", "Username or Password is Wrong!");
                                }
                            } else if (statusCode == 401) {
                                // Handle Unauthorized (e.g., incorrect credentials)
                                if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                                    ShowPopUp("❌ Fehler", "Benutzername oder Passwort ist falsch!");
                                }else{
                                    ShowPopUp("❌ Error", "Username or Password is Wrong!");
                                }
                            } else {
                                // Handle other unexpected errors
                                ShowPopUp("login_error", "Unexpected error occurred: " + errorMessage);
                            }
                        } else {
                            ShowPopUp("login_error", "No response from server.");
                        }
                    }
                });
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);

    }

    private void getUserInfo(String username) {

        String url=DataHolder.getInstance().getUrl()+"/Users/"+username;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                            SharedPreferences sp= requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor= sp.edit();

                            try {
                                // Save new user data
                                editor.putInt("userId", response.getInt("userId"));
                                editor.putString("username", response.getString("username"));
                                editor.putInt("points", response.getInt("points"));
                                editor.putInt("personId", response.getInt("personId"));


                                editor.apply();
                                getPrefixes();
                                getUserBestTimes();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                ShowPopUp("not_successful", "Error parsing the response JSON.");
                            }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ShowPopUp("not_successful", "Error getting the UserInfo from the server.");
                    }
                });
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }
    private void getPrefixes(){
        String url = DataHolder.getInstance().getUrl()+"/Settings/Products";
        // Create a JSON request
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<String> prefixList = new ArrayList<>();

                        // Loop through the JSON array
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);

                            // Extract "referenceName" attribute
                            if (jsonObject.has("referenceName")) {
                                prefixList.add(jsonObject.getString("referenceName"));
                            }
                        }

                        // Store the prefix list in DataHolder
                        DataHolder.getInstance().setPrefixList(prefixList);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Handle error
                    error.printStackTrace();
                });

        // Add the request to the queue
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void getUserBestTimes(){
        if(requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1)==-1) return;

        String urlDay=DataHolder.getInstance().getUrl()+"/Users/"+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1)+"/BestPoints/Day";
        String urlWeek=DataHolder.getInstance().getUrl()+"/Users/"+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1)+"/BestPoints/Week";
        String urlMonth=DataHolder.getInstance().getUrl()+"/Users/"+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1)+"/BestPoints/Month";
        String urlYear=DataHolder.getInstance().getUrl()+"/Users/"+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId",-1)+"/BestPoints/Year";

        SharedPreferences.Editor editor= requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit();

        JsonObjectRequest requestDay = new JsonObjectRequest(Request.Method.GET, urlDay, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            editor.putString("bestDay", response.getInt("points")+";"+response.getString("date"));

                            editor.apply();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ShowPopUp("not_successful", "Error parsing the response JSON. BestDay");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 400) {
                            editor.putString("bestDay", 0+";"+"noDate");

                            editor.apply();

                            return;
                        }
                        if (error.networkResponse != null) {
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            ShowPopUp("code:"+statusCode, body);
                        }
                    }
                });
        JsonObjectRequest requestWeek = new JsonObjectRequest(Request.Method.GET, urlWeek, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            editor.putString("bestWeek", response.getInt("points")+";"+response.getString("date"));

                            editor.apply();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ShowPopUp("Best Week Response", e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 400) {
                            editor.putString("bestWeek", 0+";"+"noDate");
                            editor.apply();
                            return;
                        }
                        if (error.networkResponse != null) {
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            ShowPopUp("code:"+statusCode, body);
                        }
                    }
                });
        JsonObjectRequest requestMonth = new JsonObjectRequest(Request.Method.GET, urlMonth, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            editor.putString("bestMonth", response.getInt("points")+";"+response.getString("date"));

                            editor.apply();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ShowPopUp("Best Month Error", "Error parsing the response JSON.BestMonth");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 400) {
                            editor.putString("bestMonth", 0+";"+"noDate");

                            editor.apply();
                            return;
                        }
                        if (error.networkResponse != null) {
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            ShowPopUp("code:"+statusCode, body);
                        }
                    }
                });

        JsonObjectRequest requestYear = new JsonObjectRequest(Request.Method.GET, urlYear, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            editor.putString("bestYear", response.getInt("points")+";"+response.getString("date"));

                            editor.apply();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            ShowPopUp("Best Year Error", "Error parsing the response JSON. BestYear");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        int statusCode = error.networkResponse.statusCode;
                        if (statusCode == 400) {
                            editor.putString("bestYear", 0+";"+"noDate");

                            editor.apply();
                            return;
                        }
                        if (error.networkResponse != null) {
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            ShowPopUp("code:"+statusCode, body);
                        }
                    }
                });
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(requestDay);
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(requestWeek);
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(requestMonth);
        VolleySingleton.getInstance(requireContext()).addToRequestQueue(requestYear);


    }

    private void ShowPopUp(String popUpCode, String message){
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

        switch (popUpCode){
            case "login_successful":

                builder.setTitle("Erfolgreich")
                        .setMessage(message)
                        .setPositiveButton("OK", ((dialog, which) -> dialog.dismiss()));


                break;
            case "login_not_successful":
                builder.setTitle("❌ Fehler")
                        .setMessage("Benutzername oder Passwort ist falsch!")
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
    @Override
    public void onResume() {
        super.onResume();
        // Clear the input fields when navigating back to this fragment
        if (binding.edtxUsername.getText() != null) binding.edtxUsername.setText("");
        if (binding.edtxtPassword.getText() != null) binding.edtxtPassword.setText("");
    }
}