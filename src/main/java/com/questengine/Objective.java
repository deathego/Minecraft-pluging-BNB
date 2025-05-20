package com.questengine;

public class Objective {
    private String type;    // "collect" or "kill"
    private String item;    // "COBBLESTONE" or "ZOMBIE"
    private int amount;     // How many to collect/kill
    private int progress;   // How many done so far

    public Objective(String type, String item, int amount) {
        this.type = type.toLowerCase();   // e.g., "collect"
        this.item = item.toUpperCase();   // e.g., "COBBLESTONE"
        this.amount = amount;
        this.progress = 0;
    }

    public String getType() { return type; }
    public String getItem() { return item; }
    public int getAmount() { return amount; }
    public int getProgress() { return progress; }

    public void incrementProgress() {
        if (progress < amount) progress++;
    }

    public boolean isComplete() {
        return progress >= amount;
    }
}
