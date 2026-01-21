package model;

public enum AssignmentStatus {
    CHUA_XAC_NHAN("Chưa xác nhận"),
    DA_NHAN("Đã nhận"),
    DA_TU_CHOI("Đã từ chối");

    private final String displayName;

    AssignmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AssignmentStatus fromString(String text) {
        for (AssignmentStatus s : AssignmentStatus.values()) {
            if (s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + text);
    }
}