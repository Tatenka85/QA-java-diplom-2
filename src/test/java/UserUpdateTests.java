import com.github.javafaker.Faker;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;

public class UserUpdateTests extends Base {

    private String accessToken;
    private final Gson gson = new Gson();
    private final Faker faker = new Faker();

    @Before
    public void setUp() {
        // Генерация уникальных данных для пользователя
        UserModel user = new UserModel(
                faker.internet().emailAddress(),
                faker.internet().password(8, 16, true, true, true),
                faker.name().firstName()
        );

        // Регистрация пользователя
        Response registerResponse = UserSteps.registerUser(user);
        registerResponse.then().statusCode(200);
        System.out.println("Регистрация пользователя: " + registerResponse.asString());

        // Логин пользователя и получение токена
        Response loginResponse = UserSteps.loginUser(user);
        loginResponse.then().statusCode(200);
        accessToken = loginResponse.jsonPath().getString("accessToken");
    }

    @Test
    @Description("Обновление email пользователя")
    public void testUpdateUserEmail() {
        String newEmail = faker.internet().emailAddress();
        Response updateResponse = UserSteps.updateUserEmail(accessToken, newEmail);
        System.out.println("Ответ сервера: " + updateResponse.asString());
        updateResponse.then().statusCode(200);
        JsonObject responseJson = gson.fromJson(updateResponse.asString(), JsonObject.class);
        assertTrue(responseJson.get("success").getAsBoolean());
        assertEquals(newEmail, responseJson.getAsJsonObject("user").get("email").getAsString());
    }

    @Test
    @Description("Обновление name пользователя")
    public void testUpdateUserName() {
        Response updateResponse = UserSteps.updateUserName(accessToken, "New Name");
        System.out.println("Ответ сервера: " + updateResponse.asString());
        updateResponse.then().statusCode(200);
        JsonObject responseJson = gson.fromJson(updateResponse.asString(), JsonObject.class);
        assertTrue(responseJson.get("success").getAsBoolean());
        assertEquals("New Name", responseJson.getAsJsonObject("user").get("name").getAsString());
    }

    @Test
    @Description("Обновление email пользователя без авторизации")
    public void testUpdateUserEmailWithoutAuth() {
        String newEmail = faker.internet().emailAddress();

        // Отправляем запрос без токена авторизации
        Response updateResponse = given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(new UserModel(newEmail, null, null))) // Сериализация через UserModel
                .when()
                .patch(Constants.AUTH_USER_ENDPOINT);

        System.out.println("Ответ сервера: " + updateResponse.asString());

        // Проверяем статус и сообщение об ошибке
        updateResponse.then().statusCode(401);
        assertEquals("You should be authorised", updateResponse.jsonPath().getString("message"));
    }

    @Test
    @Description("Обновление name пользователя без авторизации")
    public void testUpdateUserNameWithoutAuth() {
        // Отправляем запрос без токена авторизации
        Response updateResponse = given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(new UserModel(null, null, "New Name"))) // Сериализация через UserModel
                .when()
                .patch(Constants.AUTH_USER_ENDPOINT);

        System.out.println("Ответ сервера: " + updateResponse.asString());

        // Проверяем статус и сообщение об ошибке
        updateResponse.then().statusCode(401);
        assertEquals("You should be authorised", updateResponse.jsonPath().getString("message"));
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