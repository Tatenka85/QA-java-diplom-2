import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserModel {
    // Геттеры и сеттеры
    private String email;
    private String password;
    private String name;

    public UserModel(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

}