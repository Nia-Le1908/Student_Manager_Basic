package model;

public enum Role {
    ADMIN("admin"),
    GIANG_VIEN("giangvien"),
    SINH_VIEN("sinhvien");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role fromString(String text) {
        for (Role r : Role.values()) {
            if (r.value.equalsIgnoreCase(text)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + text);
    }
}