package com.example.electricitybillestimator;

/**
 * BillRecord.java
 *
 * This is a MODEL class — it represents one single bill record.
 * Every row in our database maps to one BillRecord object.
 *
 * Think of it like a container or a box that holds:
 *   - id, month, unit, rebate, totalCharges, finalCost
 */
public class BillRecord {

    // ─── Fields (data stored in each record) ───────────────────
    private int id;               // Auto-generated unique ID
    private String month;         // e.g. "January"
    private double unit;          // e.g. 467.0 kWh
    private int rebate;           // e.g. 5 (meaning 5%)
    private double totalCharges;  // e.g. 163.17
    private double finalCost;     // e.g. 154.01

    // ─── Constructor 1: Used when CREATING a new record ────────
    // (no id needed — database auto-generates it)
    public BillRecord(String month, double unit,
                      int rebate, double totalCharges,
                      double finalCost) {
        this.month        = month;
        this.unit         = unit;
        this.rebate       = rebate;
        this.totalCharges = totalCharges;
        this.finalCost    = finalCost;
    }

    // ─── Constructor 2: Used when READING from database ────────
    // (includes id because it already exists in database)
    public BillRecord(int id, String month, double unit,
                      int rebate, double totalCharges,
                      double finalCost) {
        this.id           = id;
        this.month        = month;
        this.unit         = unit;
        this.rebate       = rebate;
        this.totalCharges = totalCharges;
        this.finalCost    = finalCost;
    }

    // ─── Getters (read the values) ──────────────────────────────
    public int getId()             { return id; }
    public String getMonth()       { return month; }
    public double getUnit()        { return unit; }
    public int getRebate()         { return rebate; }
    public double getTotalCharges(){ return totalCharges; }
    public double getFinalCost()   { return finalCost; }

    // ─── Setters (change the values) ───────────────────────────
    public void setId(int id)                    { this.id = id; }
    public void setMonth(String month)           { this.month = month; }
    public void setUnit(double unit)             { this.unit = unit; }
    public void setRebate(int rebate)            { this.rebate = rebate; }
    public void setTotalCharges(double t)        { this.totalCharges = t; }
    public void setFinalCost(double f)           { this.finalCost = f; }
}