package util;

// Yêu cầu thư viện jBCrypt: https://mvnrepository.com/artifact/org.mindrot/jbcrypt
import org.mindrot.jbcrypt.BCrypt;

// Lớp tiện ích để xử lý mã hóa và kiểm tra mật khẩu
public class PasswordUtil {

    /**
     * Băm một mật khẩu sử dụng BCrypt.
     * @param plainPassword Mật khẩu gốc.
     * @return Chuỗi mật khẩu đã được băm.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Kiểm tra xem mật khẩu gốc có khớp với mật khẩu đã băm không.
     * @param plainPassword Mật khẩu gốc người dùng nhập vào.
     * @param hashedPassword Mật khẩu đã băm lưu trong CSDL.
     * @return true nếu khớp, false nếu không.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}