import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserSteps {

    private static final Gson gson = new Gson();

    @Step("Создание пользователя: email={email}, name={name}")
    public static Response registerUser(String email, String password, String name) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);
        requestBody.put("name", name);

        String jsonBody = gson.toJson(requestBody);

        return given()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(Constants.AUTH_REGISTER_ENDPOINT);
    }

    @Step("Логин пользователя: email={email}")
    public static Response loginUser(String email, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", email);
        requestBody.put("password", password);

        String jsonBody = gson.toJson(requestBody);

        return given()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(Constants.AUTH_LOGIN_ENDPOINT);
    }

    @Step("Обновление email пользователя")
    public static Response updateUserEmail(String accessToken, String newEmail) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", newEmail);

        String jsonBody = gson.toJson(requestBody);

        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(jsonBody)
                .when()
                .patch(Constants.AUTH_USER_ENDPOINT);
    }

    @Step("Обновление name пользователя")
    public static Response updateUserName(String accessToken, String newName) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", newName);

        String jsonBody = gson.toJson(requestBody);

        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(jsonBody)
                .when()
                .patch(Constants.AUTH_USER_ENDPOINT);
    }

    @Step("Разлогин пользователя")
    public static Response logoutUser(String refreshToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("token", refreshToken);

        String jsonBody = gson.toJson(requestBody);

        return given()
                .contentType(ContentType.JSON)
                .body(jsonBody)
                .when()
                .post(Constants.AUTH_LOGOUT_ENDPOINT);
    }

    @Step("Удаление пользователя")
    public static Response deleteUser(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .delete(Constants.AUTH_USER_ENDPOINT);
    }
}