package controller;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BaseController {
    
    private StackPane loadingOverlay;

    /**
     * Hiệu ứng Fade In cho Root Node (Dùng cho chuyển cảnh trong cùng 1 cửa sổ).
     */
    protected void playFadeInTransition(Node rootNode) {
        if (rootNode != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), rootNode);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();
        }
    }

    protected void playPopUpAnimation(Stage stage) {
        if (stage == null || stage.getScene() == null || stage.getScene().getRoot() == null) return;

        Node root = stage.getScene().getRoot();

        // 1. Bắt đầu từ nhỏ xíu (0.7) và mờ (0.0)
        root.setScaleX(0.7);
        root.setScaleY(0.7);
        root.setOpacity(0);

        // 2. Hiệu ứng Phóng to (Scale Up)
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), root);
        scaleTransition.setFromX(0.7);
        scaleTransition.setFromY(0.7);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        
        // 3. Hiệu ứng Hiện dần (Fade In)
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), root);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);

        // 4. Chạy song song
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
    }
    
    /**
     * Hiển thị lớp phủ loading lên trên một StackPane (thường là rootPane).
     */
    protected void showLoading(Node overlayTarget) {
        StackPane parentStackPane = null;
        if (overlayTarget instanceof StackPane) {
            parentStackPane = (StackPane) overlayTarget;
        } else if (overlayTarget != null && overlayTarget.getParent() instanceof StackPane) {
            parentStackPane = (StackPane) overlayTarget.getParent();
        } else if (overlayTarget != null && overlayTarget.getScene() != null && overlayTarget.getScene().getRoot() instanceof StackPane) {
             parentStackPane = (StackPane) overlayTarget.getScene().getRoot();
        }

        if (parentStackPane == null) return; 

        if (loadingOverlay == null) {
            loadingOverlay = new StackPane();
            loadingOverlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
            ProgressIndicator pi = new ProgressIndicator();
            pi.setStyle("-fx-progress-color: #3498db;");
            pi.setPrefSize(50, 50);
            
            VBox box = new VBox(10, pi, new Label("Đang xử lý..."));
            box.setAlignment(Pos.CENTER);
            loadingOverlay.getChildren().add(box);
        }
        
        if (!parentStackPane.getChildren().contains(loadingOverlay)) {
            parentStackPane.getChildren().add(loadingOverlay);
        }
        loadingOverlay.setVisible(true);
        loadingOverlay.toFront(); 
    }

    protected void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisible(false);
        }
    }
    
    protected void showNotification(String title, String message, boolean isError) {
        Platform.runLater(() -> {
            try {
                Notifications notify = Notifications.create()
                        .title(title)
                        .text(message)
                        .hideAfter(Duration.seconds(4))
                        .position(javafx.geometry.Pos.BOTTOM_RIGHT);

                if (isError) notify.showError(); else notify.showInformation();
            } catch (Exception e) { System.out.println((isError ? "[ERROR] " : "[INFO] ") + title + ": " + message); }
        });
    }

    protected void showSuccess(String message) { showNotification("Thành công", message, false); }
    protected void showError(String message) { showNotification("Lỗi", message, true); }

    protected <T> void runTaskWithOverlay(Supplier<T> backgroundTask, Node overlayTargetNode, 
                               Consumer<T> onSuccess, Consumer<Throwable> onError) {
        showLoading(overlayTargetNode); 
        Task<T> task = new Task<>() {
            @Override protected T call() throws Exception { return backgroundTask.get(); }
        };
        task.setOnSucceeded(e -> { hideLoading(); onSuccess.accept(task.getValue()); });
        task.setOnFailed(e -> { hideLoading(); if (onError != null) onError.accept(task.getException()); else showError("Lỗi hệ thống: " + task.getException().getMessage()); });
        new Thread(task).start();
    }
    
    protected <T> void runTask(Supplier<T> backgroundTask, Node onCursorNode, 
                               Consumer<T> onSuccess, Consumer<Throwable> onError) {
        if (onCursorNode != null && onCursorNode.getScene() != null) {
            onCursorNode.getScene().setCursor(Cursor.WAIT);
            onCursorNode.setDisable(true);
        }
        Task<T> task = new Task<>() { @Override protected T call() throws Exception { return backgroundTask.get(); } };
        task.setOnSucceeded(e -> {
            if (onCursorNode != null) { if (onCursorNode.getScene() != null) onCursorNode.getScene().setCursor(Cursor.DEFAULT); onCursorNode.setDisable(false); }
            onSuccess.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            if (onCursorNode != null) { if (onCursorNode.getScene() != null) onCursorNode.getScene().setCursor(Cursor.DEFAULT); onCursorNode.setDisable(false); }
            if (onError != null) onError.accept(task.getException()); else showError("Error: " + task.getException().getMessage());
        });
        new Thread(task).start();
    }

    protected void showStatus(Label lblStatus, String message, boolean isError) {
        if (lblStatus != null) {
            lblStatus.setText(message);
            if (isError) {
                lblStatus.getStyleClass().removeAll("status-label-success");
                if (!lblStatus.getStyleClass().contains("status-label-error")) lblStatus.getStyleClass().add("status-label-error");
            } else {
                lblStatus.getStyleClass().removeAll("status-label-error");
                if (!lblStatus.getStyleClass().contains("status-label-success")) lblStatus.getStyleClass().add("status-label-success");
            }
            lblStatus.setVisible(true); 
            lblStatus.setManaged(true);
        }
    }
    protected void showSuccess(Label lbl, String msg) { showStatus(lbl, msg, false); }
    protected void showError(Label lbl, String msg) { showStatus(lbl, msg, true); }
    protected void clearStatus(Label lblStatus) { if (lblStatus != null) { lblStatus.setText(""); lblStatus.setVisible(false); lblStatus.setManaged(false); } }
}