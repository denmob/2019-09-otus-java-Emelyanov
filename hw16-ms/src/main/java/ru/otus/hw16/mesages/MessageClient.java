package ru.otus.hw16.mesages;

public interface MessageClient {
    void accept(Message msg) throws InterruptedException;

    void init();
}