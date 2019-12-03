package ru.otus.hw08;


import com.google.gson.Gson;

public class Main {

    public static void main(String[] args)  {

        System.out.println("hw08-json");

        Gson gson = new Gson();
        BagOfPrimitives obj = new BagOfPrimitives(22, "test", 10);
        System.out.println(obj);

        String json = gson.toJson(obj);
        System.out.println(json);

        BagOfPrimitives obj2 = gson.fromJson(json, BagOfPrimitives.class);
        System.out.println(obj.equals(obj2));
        System.out.println(obj2);

    }
}
