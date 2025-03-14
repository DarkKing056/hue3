package com.example.packflow;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {
    private static final DataHolder instance = new DataHolder();
    private String myString = "http://192.168.31.173:5000"; // Your constant string

    private List<String> prefixList = new ArrayList<>();

    // Private constructor to prevent instantiation from outside
    private DataHolder() {}
    // Get the single instance of this class
    public static DataHolder getInstance() {
        return instance;
    }
    // Method to get the string
    public String getUrl() {
        return myString;
    }
    // Optional: If you ever need to change the string dynamically
    public void setMyString(String newString) {
        this.myString = newString;
    }


    // Method to get the prefix list
    public List<String> getPrefixList() {
        return prefixList;
    }

    // Method to set the prefix list
    public void setPrefixList(List<String> prefixes) {
        this.prefixList = prefixes;
    }
}
