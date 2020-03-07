package com.example.ajarisuploader;

public class Base {
    private int number;
    private String name;

    public Base() {
        this.setNumber(-1);
        this.setName("");
    }

    public Base(int number, String name) {
        this.setNumber(number);
        this.setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return this.number;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return Integer.toString(this.getNumber()) + "&" + this.getName();
    }

    public static Base stringToBase(String base) {
        String[] baseString = base.split("&");
        if(baseString.length != 2) {
            return new Base();
        }
        return new Base(Integer.parseInt(baseString[0]), baseString[1]);
    }

    public boolean equals(Base base) {
        return this.getName().equals(base.getName()) && this.getNumber() == base.getNumber();
    }

    public boolean isEmpty() {
        return this.getNumber() < 0 && this.getName().equals("");
    }
}
