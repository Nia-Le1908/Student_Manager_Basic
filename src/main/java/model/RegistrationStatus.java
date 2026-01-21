package model;

public enum RegistrationStatus {
    CHO_DUYET("Chờ duyệt"),
    DA_DUYET("Đã duyệt"),
    DA_TU_CHOI("Đã từ chối");

    private final String displayName;

    RegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RegistrationStatus fromString(String text) {
        for (RegistrationStatus s : RegistrationStatus.values()) {
            if (s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + text);
    }
}