package src.main.demo1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.lang.Thread.sleep;

public class ChatServerWithThreadPool {
    private static final int PORT = 12345;
    private static final int MAX_CLIENTS = 10; // 최대 클라이언트 수
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static ExecutorService pool; // 고정 스레드풀 생성

    static {
        pool = Executors.newFixedThreadPool(MAX_CLIENTS);
    }

    public static void main(String[] args) {
        System.out.println("Chat server started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // 클라이언트 연결 요청을 기다리며 수락
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");

                // 스레드풀을 통해 클라이언트 연결 처리
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        } finally {
            pool.shutdown(); // 서버가 종료될 때 스레드풀도 종료
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // 클라이언트와의 입출력 스트림 생성
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 클라이언트 출력 스트림을 공유 리스트에 추가
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                // 클라이언트로부터 메시지를 수신하고 모든 클라이언트에 전송
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    broadcast(message); // 모든 클라이언트에 메시지 전송
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                // 클라이언트 연결 종료 시 공유 리스트에서 제거
                if (out != null) {
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Client disconnected");
            }
        }

        private void broadcast(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
