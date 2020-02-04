package ru.otus.hw16.sockets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Client {
  private static Logger logger = LoggerFactory.getLogger(Client.class);

  private static final int PORT = 8080;
  private static final String HOST = "localhost";

  public static void main(String[] args) {
    new Client().go();
  }

  private void go() {
    try {
      try (Socket clientSocket = new Socket(HOST, PORT)) {
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        logger.info("sending to server");
        out.println("backEndService");
        String resp = in.readLine();
        logger.info("server response: {}", resp);
        sleep();

        logger.info("stop communication");
        out.println("stop");
      }

    } catch (Exception ex) {
      logger.error("error", ex);
    }
  }

  private static void sleep() {
    try {
      Thread.sleep(TimeUnit.SECONDS.toMillis(3));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
