package ru.otus.hw04;

public class Main {

    public static void main(String[] args) {
        DemoInterface demo = IoC.createMyClass();
        demo.action();
    }

}