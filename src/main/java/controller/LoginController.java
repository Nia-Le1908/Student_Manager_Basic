package controller;

import dao.UserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert; 
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML private TextField tfUsername;
    @FXML private PasswordField pfPassword;
    @FXML private Label lblStatus;

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = tfUsername.getText();
        String password = pfPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        User authenticatedUser = UserDAO.authenticateUser(username, password);

        if (authenticatedUser != null) {
            clearStatus();
            Stage currentStage = (Stage) tfUsername.getScene().getWindow();
            currentStage.close();

            try {
                switch (authenticatedUser.getRole()) {
                    case "admin":
                    case "giangvien":
                        openMainDashboard(authenticatedUser);
                        break;
                    case "sinhvien":
                        openStudentDashboard(authenticatedUser);
                        break;
                    default:
                        showError("Vai trò người dùng không xác định.");
                         openLoginWindowAgain();
                }
            } catch (IOException e) {
                 e.printStackTrace();
                 showError("Lỗi nghiêm trọng khi mở giao diện. Vui lòng kiểm tra Console.");
                 Alert alert = new Alert(Alert.AlertType.ERROR);
                 alert.setTitle("Lỗi Tải Giao Diện");
                 alert.setHeaderText("Không thể tải giao diện cần thiết.");
                 alert.setContentText("Chi tiết lỗi: " + e.getMessage() + "\nVui lòng kiểm tra đường dẫn tệp FXML và thử lại.");
                 alert.showAndWait();
            } catch (Exception e) {
                 e.printStackTrace();
                 showError("Lỗi không xác định: " + e.getMessage());
                 openLoginWindowAgain();
            }
        } else {
            showError("Tên đăng nhập hoặc mật khẩu không đúng.");
        }
    }

    private void openMainDashboard(User loggedInUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
         if (loader.getLocation() == null) {
            throw new IOException("Không tìm thấy tệp /view/MainView.fxml");
        }
        Parent root = loader.load();
        MainController mainController = loader.getController();
        mainController.initializeData(loggedInUser);
        Stage mainStage = new Stage();
        mainStage.setTitle("Quản lý Điểm - " + loggedInUser.getRole());
        Scene scene = new Scene(root);
         URL cssUrl = getClass().getResource("/view/styles.css");
         if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
        mainStage.setScene(scene);
        mainStage.show();
    }

    private void openStudentDashboard(User loggedInUser) throws IOException {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StudentDashboardView.fxml"));
          if (loader.getLocation() == null) {
             throw new IOException("Không tìm thấy tệp /view/StudentDashboardView.fxml");
         }
         Parent root = loader.load();
         StudentDashboardController dashboardController = loader.getController();
         dashboardController.initializeData(loggedInUser);

         Stage studentStage = new Stage();
         studentStage.setTitle("Bảng điều khiển - SV: " + loggedInUser.getMaSV());
         Scene scene = new Scene(root);
          URL cssUrl = getClass().getResource("/view/styles.css");
          if (cssUrl != null) scene.getStylesheets().add(cssUrl.toExternalForm());
         studentStage.setScene(scene);
         studentStage.show();
     }


     public void openLoginWindowAgain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Đăng nhập - Quản lý Điểm");
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getResource("/view/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            loginStage.setScene(scene);
            loginStage.setResizable(false);
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
             Alert alert = new Alert(Alert.AlertType.ERROR);
             alert.setTitle("Lỗi Hệ Thống");
             alert.setHeaderText("Không thể mở lại cửa sổ đăng nhập.");
             alert.setContentText("Chi tiết lỗi: " + e.getMessage());
             alert.showAndWait();
        }
    }

    private void showError(String message) { lblStatus.setText(message); lblStatus.setStyle("-fx-text-fill: red;"); }
    private void clearStatus() { lblStatus.setText(""); }
}