import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserUpdateTests extends Base {

    private String accessToken;
    private final Gson gson = new Gson();
    private final Faker faker = new Faker();

    @Before
    public void setUp() {
        // Генерация уникальных данных для пользователя
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16, true, true, true);
        String name = faker.name().firstName();

        // Регистрация пользователя
        Response registerResponse = UserSteps.registerUser(email, password, name);
        registerResponse.then().statusCode(200);
        System.out.println("Регистрация пользователя: " + registerResponse.asString());

        // Логин пользователя и получение токена
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().statusCode(200);
        accessToken = loginResponse.jsonPath().getString("accessToken");
    }

    @Test
    @Description("Обновление email пользователя")
    public void testUpdateUserEmail() {
        // Генерация уникального email для обновления
        String newEmail = faker.internet().emailAddress();

        // Обновляем email
        Response updateResponse = UserSteps.updateUserEmail(accessToken, newEmail);
        System.out.println("Ответ сервера: " + updateResponse.asString());

        // Проверяем статус и успешность обновления
        updateResponse.then().statusCode(200);
        JsonObject responseJson = gson.fromJson(updateResponse.asString(), JsonObject.class);
        assertTrue(responseJson.get("success").getAsBoolean());
        assertEquals(newEmail, responseJson.getAsJsonObject("user").get("email").getAsString());
    }

    @Test
    @Description("Обновление name пользователя")
    public void testUpdateUserName() {
        // Обновляем name
        Response updateResponse = UserSteps.updateUserName(accessToken, "New Name");
        System.out.println("Ответ сервера: " + updateResponse.asString());

        // Проверяем статус и успешность обновления
        updateResponse.then().statusCode(200);
        JsonObject responseJson = gson.fromJson(updateResponse.asString(), JsonObject.class);
        assertTrue(responseJson.get("success").getAsBoolean());
        assertEquals("New Name", responseJson.getAsJsonObject("user").get("name").getAsString());
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