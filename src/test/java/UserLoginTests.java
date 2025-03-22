import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserLoginTests extends Base {

    private final Faker faker = new Faker();
    private UserModel user;

    @Before
    public void setUp() {
        // Генерация уникальных данных для пользователя
        user = new UserModel(
                faker.internet().emailAddress(),
                faker.internet().password(8, 16, true, true, true),
                faker.name().firstName()
        );

        // Регистрация пользователя
        Response registerResponse = UserSteps.registerUser(user);
        registerResponse.then().statusCode(200);
        System.out.println("Регистрация пользователя: " + registerResponse.asString());

        // Логин пользователя и получение токенов
        Response loginResponse = UserSteps.loginUser(user);
        loginResponse.then().statusCode(200);
        System.out.println("Авторизация пользователя: " + loginResponse.asString());

        // Сохраняем токены
        String accessToken = loginResponse.jsonPath().getString("accessToken");
        String refreshToken = loginResponse.jsonPath().getString("refreshToken");

        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);
    }

    @Test
    @Description("Логин под существующим пользователем")
    public void testLoginWithValidCredentials() {
        Response loginResponse = UserSteps.loginUser(user);
        loginResponse.then().statusCode(200);
        assertNotNull(loginResponse.jsonPath().getString("accessToken"));
        assertNotNull(loginResponse.jsonPath().getString("refreshToken"));
    }

    @Test
    @Description("Логин с неверным email")
    public void testLoginWithInvalidEmail() {
        UserModel invalidUser = new UserModel("wrongEmail@example.com", user.getPassword(), user.getName());
        Response loginResponse = UserSteps.loginUser(invalidUser);
        loginResponse.then().statusCode(401);
        assertEquals("email or password are incorrect", loginResponse.jsonPath().getString("message"));
    }

    @Test
    @Description("Логин с неверным паролем")
    public void testLoginWithInvalidPassword() {
        UserModel invalidUser = new UserModel(user.getEmail(), "wrongPassword", user.getName());
        Response loginResponse = UserSteps.loginUser(invalidUser);
        loginResponse.then().statusCode(401);
        assertEquals("email or password are incorrect", loginResponse.jsonPath().getString("message"));
    }
}