package com.example.packflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.packflow.databinding.FragmentLoginBinding;
import com.example.packflow.databinding.FragmentProfileBinding;
import com.example.packflow.databinding.ToolbarBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {


    FragmentProfileBinding binding;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        binding= FragmentProfileBinding.inflate(getLayoutInflater());

        getInformation();
        binding.cnstWholeLayout.setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("backgroundThema", "#8692f7")));


        setToolBar();
        //title
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Profil");
            }

        }
        return binding.getRoot();
    }

    private void getInformation(){
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
                            SetEverything();

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
                            SetEverything();


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
                            SetEverything();

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
                            SetEverything();

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
        SetEverything();
    }
    private void ShowPopUp(String popUpCode, String message){
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(getContext());

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

        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void setToolBar(){
        ToolbarBinding toolbar=binding.profileToolbar;
        SetEverything();

        toolbar.leftIcon.setVisibility(View.VISIBLE);
        toolbar.profileIcon.setVisibility(View.INVISIBLE);
        toolbar.settingsIcon.setVisibility(View.INVISIBLE);
        toolbar.getRoot().setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("toolbarThema", "#8692f7")));

        toolbar.leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    public void SetEverything(){
        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
            binding.profileToolbar.title.setText("\uD83D\uDC64 Profil-Seite");
            binding.txtTitle.setText("Top-Leistungen");

            binding.txtDayP.setText("Bester Tag: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestDay", "0").split(";")[0]+" Punkte");
            binding.txtWeekP.setText("Beste Woche: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestWeek", "0").split(";")[0]+" Punkte");
            binding.txtMonthP.setText("Bester Monat: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestMonth", "0").split(";")[0]+" Punkte");
            binding.txtYearP.setText("Bestes Jahr: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestYear", "0").split(";")[0]+" Punkte");
        }else{
            binding.profileToolbar.title.setText("⚙\uFE0F Profile");
            binding.txtTitle.setText("Top Scores");

            binding.txtDayP.setText("Best Day: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestDay", "0").split(";")[0]+" Points");
            binding.txtWeekP.setText("Best Week: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestWeek", "0").split(";")[0]+" Points");
            binding.txtMonthP.setText("Best Month: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestMonth", "0").split(";")[0]+" Points");
            binding.txtYearP.setText("Best Year: "+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("bestYear", "0").split(";")[0]+" Points");
        }
    }


}