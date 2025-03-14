package com.example.packflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.packflow.databinding.FragmentStatisticBinding;
import com.example.packflow.databinding.ToolbarBinding;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.example.packflow.HelperClasses.Package;
import com.github.mikephil.charting.formatter.ValueFormatter;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticFragment extends Fragment {

    FragmentStatisticBinding binding;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StatisticFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatisticFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatisticFragment newInstance(String param1, String param2) {
        StatisticFragment fragment = new StatisticFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        binding= FragmentStatisticBinding.inflate(getLayoutInflater());
        setToolBar();


        binding.btnStartDate.setOnClickListener(v -> showDatePickerDialog(binding.btnStartDate));

        binding.btnEndDate.setOnClickListener(v -> showDatePickerDialog(binding.btnEndDate));


        binding.cnstWholeLayout.setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("backgroundThema", "#8692f7")));


        getUserPackagesBetweenDate(binding.btnStartDate.getText().toString(),binding.btnEndDate.getText().toString());

        return binding.getRoot();
    }

    private void showDatePickerDialog(Button button) {
        String currentDate = button.getText().toString();


        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        if (!currentDate.isEmpty()) {

            String[] dateParts = currentDate.split("-");
            if (dateParts.length == 3) {
                year = Integer.parseInt(dateParts[0]);
                month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based in Calendar
                day = Integer.parseInt(dateParts[2]);
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format selected date
                    String selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    button.setText(selectedDate);

                    getUserPackagesBetweenDate(binding.btnStartDate.getText().toString(), binding.btnEndDate.getText().toString());

                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void getUserPackagesBetweenDate(String dateStart, String dateEnd) {
        SharedPreferences sp=requireContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        int userId=sp.getInt("userId",0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());

        Date dateToStart=null;
        Date dateToEnd=null;
        String url="";

        if (Objects.equals(dateStart, "Start Datum") || Objects.equals(dateEnd, "End Datum")) {
            Date currentDate = new Date();
            dateToStart = getMonday(currentDate);
            dateToEnd = getSunday(currentDate);

            binding.btnStartDate.setText(sdf.format(dateToStart));
            binding.btnEndDate.setText(sdf.format(dateToEnd));
            // Format the start and end dates properly
            String formattedStartDate = sdf.format(dateToStart) + ";00:00";
            String formattedEndDate = sdf.format(dateToEnd) + ";23:59:59";


            url=DataHolder.getInstance().getUrl()+"/Scanning/Parcels?dateBefore="+formattedStartDate+"&dateAfter="+formattedEndDate+"&userId="+userId;

            dateToStart=setTimeToMidnight(dateToStart);
            dateToEnd=setTimeToEndOfDay(dateToEnd);

        }else{
            try {
                dateToStart=setTimeToMidnight(sdf.parse(dateStart));
                dateToEnd=setTimeToEndOfDay(sdf.parse(dateEnd));
                if (dateToEnd.before(dateToStart)) {


                    binding.txtErrorMessage.setVisibility(View.VISIBLE);
                    if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                        binding.txtErrorMessage.setText("Das Enddatum muss nach dem Startdatum liegen!");
                    }else{
                        binding.txtErrorMessage.setText("The end date must be after the start date!");
                    }
                    return;
                } else {

                    binding.txtErrorMessage.setVisibility(View.INVISIBLE);
                    dateToEnd = setTimeToEndOfDay(dateToEnd);
                }

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            url=DataHolder.getInstance().getUrl()+"/Scanning/Parcels?dateBefore="+dateStart+";00:00"+"&dateAfter="+dateEnd+";23:59:59"+"&userId="+userId;

        }

        Date finalDateStart=dateToStart;
        Date finalDateEnd=dateToEnd;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            List<Package> packageList = new ArrayList<>();

                            for (int i = 0; i < response.length(); i++) {
                                JSONObject packageObject = response.getJSONObject(i);

                                String pieceCode = packageObject.getString("pieceCode");
                                String countryCode = packageObject.getString("countryCode");
                                double weight = packageObject.getDouble("weight");
                                int productId = packageObject.getInt("productId");
                                String date = packageObject.getString("date");

                                // Create a new Package object
                                Package newPackage = new Package(pieceCode, countryCode, weight, productId, date);
                                packageList.add(newPackage);
                            }

                            createChart(packageList,finalDateStart,finalDateEnd);

                        } catch (JSONException e) {
                            ShowPopUp("not_successful", "Error parsing the response JSON.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ShowPopUp("not_successful", "Error getting the UserPackages from the server.");
                    }
                });

        VolleySingleton.getInstance(requireContext()).addToRequestQueue(request);
    }
    private void createChart(List<Package> packages,Date startDate,Date endDate) {
        long diffInMillis = endDate.getTime() - startDate.getTime();
        int numberOfDays = (int) (diffInMillis / (1000 * 60 * 60 * 24)); // Number of days between the two dates


        //Wenn größer als eine woche
        if (6>numberOfDays || 7<numberOfDays ) {
            binding.txtErrorMessage.setVisibility(View.VISIBLE);
            if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
                binding.txtErrorMessage.setText("Start- und Enddatum müssen 7 Tage auseinander liegen!");
            }else{
                binding.txtErrorMessage.setText("Start- and EndDate must be 7 days apart!");
            }
            return;
        } else {
            binding.txtErrorMessage.setVisibility(View.INVISIBLE);
        }

        int[] dailyPackageCounts = new int[numberOfDays+1];
        final int[] completedRequests = {0};

        String url="";
        // Loop through the packages and count packages for each day between startDate and endDate
        if(packages.isEmpty()){
            updateChart(startDate,numberOfDays,dailyPackageCounts); // Call method to update the chart
        }
        for (Package pkg : packages) {

            url=DataHolder.getInstance().getUrl()+"/Settings/Product/"+pkg.getProductId();

            // Create a request using Volley
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Get the "points" from the JSON object
                                int points = response.getInt("points");
                                // Do something with the points

                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                                    Date packageDate = sdf.parse(pkg.getDate());

                                    // Calculate the difference in days between the package's date and startDate
                                    long diff = packageDate.getTime() - startDate.getTime();
                                    int dayIndex = (int) (diff / (1000 * 60 * 60 * 24)); // Get the day index

                                    // Increment the package count for that day
                                    dailyPackageCounts[dayIndex]+=points;

                                    completedRequests[0]++;

                                    // Once all requests have been completed, update the chart
                                    if (completedRequests[0] == packages.size()) {
                                        updateChart(startDate,numberOfDays,dailyPackageCounts); // Call method to update the chart
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e);
                                }


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Handle error
                            error.printStackTrace();
                        }
                    });

            VolleySingleton.getInstance(requireContext()).addToRequestQueue(jsonObjectRequest);

        }


    }
    private void updateChart(Date startDate, int numberOfDays,int[] dailyPackageCounts){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Initialize the BarChart and disable touch interaction
        BarChart barChart = binding.barChart;
        LineChart lineChart = binding.lineChart;

        ArrayList<String> xAxisLabels = new ArrayList<>();
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Entry> lineEntries = new ArrayList<>();
        for (int i = 0; i <= numberOfDays; i++) {

            String dayOfMonth = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

            xAxisLabels.add(dayOfMonth);

            if (numberOfDays <= 7) {
                barEntries.add(new BarEntry(i, dailyPackageCounts[i]));
            } else {
                lineEntries.add(new Entry(i, dailyPackageCounts[i]));
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (numberOfDays <= 7) {
            // BarChart visible, LineChart hidden
            binding.barChart.setVisibility(View.VISIBLE);
            binding.lineChart.setVisibility(View.GONE);
            createBarChart(barChart, barEntries, xAxisLabels, dailyPackageCounts);
        } else {
            /*
            // LineChart visible, BarChart hidden
            binding.barChart.setVisibility(View.GONE);
            binding.lineChart.setVisibility(View.VISIBLE);
            createLineChart(lineChart, lineEntries, xAxisLabels, dailyPackageCounts);
             */
        }
    }

    private void createBarChart(BarChart barChart, ArrayList<BarEntry> barEntries, ArrayList<String> xAxisLabels, int[] dailyPackageCounts) {
        barChart.setTouchEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        BarDataSet barDataSet;
        // Create the BarDataSet and BarData
        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
             barDataSet= new BarDataSet(barEntries, "Punkte pro Tag");

        }else{
                barDataSet = new BarDataSet(barEntries, "Points per Day");
        }
        // Set the bar data to the chart
        barDataSet.setValueTextSize(16f);
        barDataSet.setColor(Color.parseColor("#FF6200EE"));
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        barDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%d", (int) value); // Show whole numbers
            }
        });

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setTextSize(14f);
        barChart.setExtraBottomOffset(20f);
        barChart.getLegend().setYOffset(10f);
        // Customize the X-axis
        XAxis xAxis = barChart.getXAxis();
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(Arrays.stream(dailyPackageCounts).max().orElse(5) + 1);
        // Set the X-axis labels (using the dates of the week)
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // Animate the chart
        barChart.animateY(1000);

        // Customize axis text size and remove grid lines
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(14f);
        yAxis.setTextSize(14f);
        yAxis.setGranularity(1f); // Set granularity to 1


        if (barEntries.isEmpty()) {
            barChart.setNoDataText("No chart data available");
            barChart.setNoDataTextColor(Color.BLACK);  // Ensure the text color is black
        }

        // Invalidate to make sure the changes take effect
        barChart.invalidate();
    }

    private void createLineChart(LineChart lineChart, ArrayList<Entry> lineEntries, ArrayList<String> xAxisLabels, int[] dailyPackageCounts) {
        // Create the LineDataSet and LineData

        lineChart.getAxisRight().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setClickable(true);

        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Paketen pro Tag");
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setColor(Color.parseColor("#FF6200EE"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setCircleRadius(5f);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setTextSize(14f);

        // Customize X and Y axis for LineChart
        XAxis xAxis = lineChart.getXAxis();
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(Arrays.stream(dailyPackageCounts).max().orElse(5) + 1);


        lineDataSet.setDrawValues(false);


        lineDataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%d", (int) value); // Show whole numbers
            }
        });
        yAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%d", (int) value); // Show whole numbers
            }
        });
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Smooth line


        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(dailyPackageCounts.length / 2, true);

        lineChart.setVisibility(View.VISIBLE);
        lineChart.animateY(1000);
    }


    public static Date setTimeToEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
    public static Date setTimeToMidnight(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }


    public static Date getMonday(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);  // Set the calendar to the given date

        // Get the day of the week (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Calculate the difference to get the previous Monday (if the current day is Sunday, it will go back 6 days, otherwise to the previous Monday)
        int diff = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;

        // Adjust the calendar to the Monday of the current week
        calendar.add(Calendar.DAY_OF_MONTH, diff);

        return calendar.getTime();  // Return the Monday date as a Date object
    }

    public static Date getSunday(Date d) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);  // Set the calendar to the given date

        // Get the day of the week (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        // Calculate the difference to get the next Sunday
        int diff = (dayOfWeek == Calendar.SUNDAY) ? 0 : Calendar.SUNDAY - dayOfWeek + 7; // If today is Sunday, keep it; else move to the next Sunday

        // Add the difference to the current date to get the next Sunday
        calendar.add(Calendar.DAY_OF_MONTH, diff);

        return calendar.getTime();  // Return the Sunday date as a Date object
    }


    private void ShowPopUp(String popUpCode, String message){
        // Check if the fragment is attached to an activity
        if (getContext() != null && isAdded()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

            switch (popUpCode) {
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
    }
    private void setToolBar(){
        ToolbarBinding toolbar=binding.statisticToolBar;
        toolbar.getRoot().setBackgroundColor(Color.parseColor(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("toolbarThema", "#f78692")));

        binding.btnStartDate.setBackgroundResource(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getInt("btnThema", R.drawable.custom_button_pattern1));
        binding.btnEndDate.setBackgroundResource(requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getInt("btnThema", R.drawable.custom_button_pattern1));
        if (requireContext().getSharedPreferences("themaSP", Context.MODE_PRIVATE).getString("language", "de").equals("de")){
            toolbar.title.setText("\uD83D\uDCCA Statistik-Seite");
            binding.txtEndInfo.setText("Ende");
        }else{
            toolbar.title.setText("⚙\uFE0F Statistics");
            binding.txtEndInfo.setText("End");
        }

        toolbar.leftIcon.setVisibility(View.VISIBLE);
        toolbar.profileIcon.setVisibility(View.INVISIBLE);
        toolbar.settingsIcon.setVisibility(View.INVISIBLE);
        toolbar.statisticIcon.setVisibility(View.INVISIBLE);

        toolbar.leftIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}