package com.questengine;

public class ObjectiveProgress {
    private final String type;
    private final String item;
    private final int targetAmount;
    private int currentAmount;

    public ObjectiveProgress(String type, String item, int targetAmount) {
        this.type = type;
        this.item = item;
        this.targetAmount = targetAmount;
        this.currentAmount = 0;
    }

    public String getType() {
        return type;
    }

    public String getItem() {
        return item;
    }

    public int getTargetAmount() {
        return targetAmount;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(int amount) {
        this.currentAmount = amount;
    }

    public void increment() {
        if (currentAmount < targetAmount) {
            currentAmount++;
        }
    }

    public boolean isComplete() {
        return currentAmount >= targetAmount;
    }
}
