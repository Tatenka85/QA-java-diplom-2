import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserCreationTests extends Base {
    private final Faker faker = new Faker();
    private String accessToken;
    private String refreshToken;
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

        // Авторизация пользователя
        Response loginResponse = UserSteps.loginUser(user);
        loginResponse.then().statusCode(200);
        System.out.println("Авторизация пользователя: " + loginResponse.asString());

        // Сохраняем токены
        accessToken = loginResponse.jsonPath().getString("accessToken");
        refreshToken = loginResponse.jsonPath().getString("refreshToken");

        System.out.println("Access Token: " + accessToken);
        System.out.println("Refresh Token: " + refreshToken);
    }

    @Test
    @Description("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        UserModel newUser = new UserModel(
                faker.internet().emailAddress(),
                faker.internet().password(8, 16, true, true, true),
                faker.name().firstName()
        );

        Response response = UserSteps.registerUser(newUser);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(200);
        assertTrue(response.jsonPath().getBoolean("success"));
    }

    @Test
    @Description("Создание пользователя, который уже зарегистрирован")
    public void testCreateDuplicateUser() {
        Response response = UserSteps.registerUser(user);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(403);
        assertEquals("User already exists", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        UserModel invalidUser = new UserModel("", user.getPassword(), user.getName());
        Response response = UserSteps.registerUser(invalidUser); // Используем UserSteps
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без пароля")
    public void testCreateUserWithoutPassword() {
        UserModel invalidUser = new UserModel(user.getEmail(), "", user.getName());
        Response response = UserSteps.registerUser(invalidUser);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без имени")
    public void testCreateUserWithoutName() {
        UserModel invalidUser = new UserModel(user.getEmail(), user.getPassword(), "");
        Response response = UserSteps.registerUser(invalidUser);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @After
    public void cleanup() {
        // Разлогин пользователя перед удалением
        if (refreshToken != null) {
            System.out.println("Перед разлогином refreshToken: " + refreshToken);
            Response logoutResponse = UserSteps.logoutUser(refreshToken);
            System.out.println("Ответ сервера на разлогин: " + logoutResponse.asString());
            logoutResponse.then().statusCode(200);
        }

        // Удаление пользователя
        if (accessToken != null) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            System.out.println("Ответ сервера на удаление: " + deleteResponse.asString());
            deleteResponse.then().statusCode(202);
        }
    }
}