package com.example.packflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.packflow.databinding.FragmentProfileBinding;
import com.example.packflow.databinding.FragmentSettingsBinding;
import com.example.packflow.databinding.ToolbarBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {


    FragmentSettingsBinding binding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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
        binding= FragmentSettingsBinding.inflate(getLayoutInflater());

        setToolBar();

        binding.btnChangePas.setOnClickListener(v -> showPasswordChangeDialog());

        binding.imgThema1.setImageResource(R.drawable.pattern1);
        binding.imgThema2.setImageResource(R.drawable.pattern2);
        binding.imgThema3.setImageResource(R.drawable.pattern3);
        binding.imgThema4.setImageResource(R.drawable.pattern4);

        binding.cnstWholeLayout.setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("backgroundThema", "#8692f7")));
        binding.btnChangeLanguage.setOnClickListener(v -> {
            if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")) {
                // Switch to English
                SharedPreferences.Editor editor= requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).edit();

                editor.putString("language", "en");
                editor.apply();
            } else {
                // Switch to German
                SharedPreferences.Editor editor= requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).edit();

                editor.putString("language", "de");
                editor.apply();
            }
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        binding.imgThema1.setOnClickListener(v -> {changeThema(1);});
        binding.imgThema2.setOnClickListener(v -> {changeThema(2);});
        binding.imgThema3.setOnClickListener(v -> {changeThema(3);});
        binding.imgThema4.setOnClickListener(v -> {changeThema(4);});

        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Einstellungen");
            }

        }
        return binding.getRoot();
    }

    private void showPasswordChangeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        // Get references to UI elements

        EditText oldPassword = dialogView.findViewById(R.id.editOldPassword);
        EditText newPassword = dialogView.findViewById(R.id.editNewPassword);
        EditText confirmPassword = dialogView.findViewById(R.id.editConfirmPassword);
        TextView errorText = dialogView.findViewById(R.id.textError);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnAccept = dialogView.findViewById(R.id.btnAccept);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAccept.setOnClickListener(v -> {
            String oldPass = oldPassword.getText().toString();
            String newPass = newPassword.getText().toString();
            String confirmPass = confirmPassword.getText().toString();

            if (requireContext()
                    .getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de")
                    .equals("de")){
                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    errorText.setText("Alle Felder sind erforderlich.");
                    return;
                }
                if (!oldPass.equals(requireContext()
                        .getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                        .getString("pass", "password.toString()"))) {
                    errorText.setText("Das alte Passwort ist falsch.");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    errorText.setText("Die neuen Passwörter stimmen nicht überein.");
                    return;
                }
                if (newPass.length() < 6) {
                    errorText.setText("Das Passwort muss mindestens 6 Zeichen lang sein.");
                    return;
                }
            }else{
                if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    errorText.setText("All fields are required.");
                    return;
                }
                if (!oldPass.equals(requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("pass", "password.toString()"))) {
                    errorText.setText("The old password is incorrect.");
                    return;
                }
                if (!newPass.equals(confirmPass)) {
                    errorText.setText("New passwords do not match.");
                    return;
                }
                if (newPass.length() < 8) {
                    errorText.setText("Password must be at least 8 characters.");
                    return;
                }
            }
            changePassReq(newPass);
            dialog.dismiss();
        });
    }

    private void changePassReq(String newPass) {
        String url = DataHolder.getInstance().getUrl()+"/Users/"+requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getInt("userId", -1);// Replace with your API URL

        // Create JSON request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("username", requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).getString("username","defUsername"));
            requestBody.put("password", newPass);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Create a new request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //Volley thinks empty json is error
                        requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putString("pass", newPass).apply();
                        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                            ShowPopUp("✅ Erfolgreich","Passwort wurde geändert!");
                        }else{
                            ShowPopUp("✅ Successful","Password Changed!");
                        }                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String body = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                            if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                                ShowPopUp("❌ Fehler:"+statusCode,"! "+body);
                            }else{
                                ShowPopUp("❌ Error:"+statusCode,"! "+body);
                            }
                        }else{
                            //if correct response
                            if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                                ShowPopUp("✅ Erfolgreich","Passwort wurde geändert!");
                            }else{
                                ShowPopUp("✅ Successful","Password Changed!");
                            }
                            requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE).edit().putString("pass", newPass).apply();
                        }
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(request);

    }

    private void changeThema(int themaID) {
        SharedPreferences.Editor editor= requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).edit();

        switch (themaID) {
            case 1:
                editor.putInt("btnThema", R.drawable.custom_button_pattern1);
                editor.putString("toolbarThema", "#FF6B35");
                editor.putString("backgroundThema", "#EFEFD0");
                editor.putString("statisticThema", "#F7C59F");
                break;
            case 2:
                editor.putInt("btnThema", R.drawable.custom_button_pattern2);
                editor.putString("toolbarThema", "#EAE151");
                editor.putString("backgroundThema", "#FAFDF6");
                editor.putString("statisticThema", "#EEEFA8");
                break;
            case 3:
                editor.putInt("btnThema", R.drawable.custom_button_pattern3);
                editor.putString("toolbarThema", "#935ab0");
                editor.putString("backgroundThema", "#F2D1C9");
                editor.putString("statisticThema", "#BAD1CD");
                break;
            case 4:
                editor.putInt("btnThema", R.drawable.custom_button_pattern4);
                editor.putString("toolbarThema", "#62B6CB");
                editor.putString("backgroundThema", "#CAE9FF");
                editor.putString("statisticThema", "#5FA8D3");
                break;
        }

        editor.apply();

        requireActivity().getSupportFragmentManager().popBackStack();

    }


    private void setToolBar(){

        ToolbarBinding toolbar=binding.settingsToolBar;

        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
            toolbar.title.setText("⚙\uFE0F Einstellungen-Seite");
            binding.btnChangePas.setText("Passwort ändern");
            binding.btnChangeLanguage.setText("Deutsch");
        }else{
            toolbar.title.setText("⚙\uFE0F Settings");
            binding.btnChangePas.setText("change Password");
            binding.btnChangeLanguage.setText("English");
        }

        toolbar.leftIcon.setVisibility(View.VISIBLE);
        toolbar.profileIcon.setVisibility(View.INVISIBLE);
        toolbar.settingsIcon.setVisibility(View.INVISIBLE);


        binding.btnChangePas.setBackgroundResource(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getInt("btnThema",R.drawable.custom_button_pattern1));
        binding.btnChangeLanguage.setBackgroundResource(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getInt("btnThema",R.drawable.custom_button_pattern1));

        toolbar.getRoot().setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("toolbarThema", "#8692f7")));


        toolbar.leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
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

}