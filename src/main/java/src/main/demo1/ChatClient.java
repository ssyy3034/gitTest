package src.main.demo1;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient extends Application {
    private TextArea textArea;
    private TextField textField;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;

    public static void main(String[] args) {
        System.setProperty("prism.order", "sw"); // 소프트웨어 렌더링 모드 설정
        launch(args); // JavaFX 애플리케이션 시작
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Client");

        // UI 구성 요소 설정
        textArea = new TextArea();
        textArea.setEditable(false);

        textField = new TextField();
        textField.setPromptText("Enter your message");
        Button button = new Button("Send");

        // 전송 버튼 클릭 시 sendMessage 호출
        button.setOnAction(e -> sendMessage());

        VBox layout = new VBox(10, textArea, textField, button);
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();

        // 서버 연결 설정
        new Thread(this::setupNetwork).start();
    }

    private void setupNetwork() {
        try {
            // 서버에 연결
            socket = new Socket("localhost", 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 서버로부터 메시지를 수신하여 화면에 표시
            String message;
            while ((message = in.readLine()) != null) {
                String receivedMessage = message;
                Platform.runLater(() -> textArea.appendText(receivedMessage + "\n"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 전송 버튼을 통해 메시지를 서버로 전송하는 메서드
    private void sendMessage() {
        String message = textField.getText();
        if (!message.isEmpty()) {
            out.println(message);  // 서버로 메시지 전송
            textArea.appendText("You: " + message + "\n"); // 본인의 메시지 출력
            textField.clear();
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
