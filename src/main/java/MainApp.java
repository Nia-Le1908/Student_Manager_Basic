import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Thay đổi màn hình khởi động của ứng dụng thành LoginView.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
        primaryStage.setTitle("Đăng nhập - Quản lý điểm");
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

