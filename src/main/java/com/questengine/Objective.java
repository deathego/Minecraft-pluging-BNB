package com.questengine;

public class Objective {
    private String type;
    private String item;
    private int amount;
    private int progress;

    public Objective(String type, String item, int amount) {
        this.type = type.toLowerCase();
        this.item = item.toUpperCase();
        this.amount = amount;
        this.progress = 0;
    }

    public String getType() { return type; }
    public String getItem() { return item; } 
    public int getAmount() { return amount; }
    public int getProgress() { return progress; }

    public void incrementProgress() {
        if (progress < amount) {
            progress++;
        }
    }

    public boolean isComplete() {
        return progress >= amount;
    }

   
    public boolean matches(String actionType, String target) {
        return this.getType().equalsIgnoreCase(actionType) &&
               this.getItem().equalsIgnoreCase(target);
    }

    @Override
    public String toString() {
        return "Objective{" +
                "type='" + type + '\'' +
                ", item='" + item + '\'' +
                ", amount=" + amount +
                ", progress=" + progress +
                '}';
    }
}
