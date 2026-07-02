package com.turbochinczyk.gameserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpGameServer {
    public static void main(String[] args) throws IOException {
        int port = 9000;
        ExecutorService pool = Executors.newFixedThreadPool(4);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Serwer: Listening:" + port);

            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client));
            }
        }
    }

    private static void handleClient(Socket socket) {
        String remote = String.valueOf(socket.getRemoteSocketAddress());

        try (
            socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))
        ) {
            out.write("Welcom to jamrock");
            out.newLine();
            out.flush();

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Serwer: " + remote + " -> " + line);
                out.write("Klient: " + line);
                out.newLine();
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Serwer: Client error: " + e.getMessage());
        }
    }
}