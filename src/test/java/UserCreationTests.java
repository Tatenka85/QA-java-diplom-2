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
    private String email;
    private String password;
    private String name;

    @Before
    public void setUp() {
        // Генерация уникальных данных для пользователя
        email = faker.internet().emailAddress();
        password = faker.internet().password(8, 16, true, true, true);
        name = faker.name().firstName();

        // Регистрация пользователя
        Response registerResponse = UserSteps.registerUser(email, password, name);
        registerResponse.then().statusCode(200);
        System.out.println("Регистрация пользователя: " + registerResponse.asString());

        // Авторизация пользователя
        Response loginResponse = UserSteps.loginUser(email, password);
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
        String newEmail = faker.internet().emailAddress();
        String newPassword = faker.internet().password(8, 16, true, true, true);
        String newName = faker.name().firstName();

        Response response = UserSteps.registerUser(newEmail, newPassword, newName);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(200);
        assertTrue(response.jsonPath().getBoolean("success"));
    }

    @Test
    @Description("Создание пользователя, который уже зарегистрирован")
    public void testCreateDuplicateUser() {
        Response response = UserSteps.registerUser(email, password, name);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(403);
        assertEquals("User already exists", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        Response response = UserSteps.registerUser("", password, name);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без пароля")
    public void testCreateUserWithoutPassword() {
        Response response = UserSteps.registerUser(email, "", name);
        System.out.println("Ответ сервера: " + response.asString());
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без имени")
    public void testCreateUserWithoutName() {
        Response response = UserSteps.registerUser(email, password, "");
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