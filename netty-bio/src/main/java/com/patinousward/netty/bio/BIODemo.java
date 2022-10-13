package com.patinousward.netty.bio;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIODemo {

    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("server start");
        while (true) {
            System.out.println("wait to connect");
            Socket socket = serverSocket.accept(); //block method
            System.out.println("accept a connect");
            executorService.execute(() -> {
                handler(socket);
            });
        }
    }

    public static void handler(Socket socket) {
        System.out.println(String.format("currentThreadId => %s,currentThreadName => %s", Thread.currentThread().getId(), Thread.currentThread().getName()));
        try {
            byte[] bytes = new byte[1024];
            InputStream inputStream = socket.getInputStream();
            while (true) {
                System.out.println("wait to read");
                int read = inputStream.read(bytes);// block method
                if (read != -1) {
                    System.out.println(new String(bytes, 0, read));
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }
}
