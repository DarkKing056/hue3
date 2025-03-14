package com.example.packflow.HelperClasses;

public class Package {
    private String pieceCode;
    private String countryCode;
    private double weight;
    private int productId;
    private String date;

    public Package(String pieceCode, String countryCode, double weight, int productId, String date) {
        this.pieceCode = pieceCode;
        this.countryCode = countryCode;
        this.weight = weight;
        this.productId = productId;
        this.date = date;
    }

    // Getters
    public String getPieceCode() {
        return pieceCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getWeight() {
        return weight;
    }

    public int getProductId() {
        return productId;
    }

    public String getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "com.example.packflow.HelperClasses.Package{" +
                "pieceCode='" + pieceCode + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", weight=" + weight +
                ", productId=" + productId +
                ", date='" + date + '\'' +
                '}';
    }
}
