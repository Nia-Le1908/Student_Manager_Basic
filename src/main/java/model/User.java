package model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User {
    
    private final StringProperty username;
    private final StringProperty passwordHash; 
    private final StringProperty role;
    private final StringProperty maSV;
    private final StringProperty maGV; 

    public User(String username, String passwordHash, String role, String maSV, String maGV) {
        this.username = new SimpleStringProperty(username);
        this.passwordHash = new SimpleStringProperty(passwordHash);
        this.role = new SimpleStringProperty(role);
        
        this.maSV = new SimpleStringProperty(maSV == null ? "" : maSV);
        this.maGV = new SimpleStringProperty(maGV == null ? "" : maGV);
    }

    // --- Getters ---
    public String getUsername() { return username.get(); }
    /**
     * Get the hashed password string.
     * @return The password hash string.
     */
    public String getPasswordHash() {
        return passwordHash.get(); // Ensure this method returns the actual value
    }
    public String getRole() { return role.get(); }
    public String getMaSV() { return maSV.get(); }
    public String getMaGV() { return maGV.get(); }

    // --- Property methods ---
    public StringProperty usernameProperty() { return username; }
    public StringProperty passwordHashProperty() { return passwordHash; }
    public StringProperty roleProperty() { return role; }
    public StringProperty maSVProperty() { return maSV; }
    public StringProperty maGVProperty() { return maGV; }
}