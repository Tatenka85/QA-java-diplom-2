import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class UserSteps {

    private static final Gson gson = new Gson();

    @Step("Создание пользователя: email={user.email}, name={user.name}")
    public static Response registerUser(UserModel user) {
        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(user))
                .when()
                .post(Constants.AUTH_REGISTER_ENDPOINT);
    }

    @Step("Логин пользователя: email={user.email}")
    public static Response loginUser(UserModel user) {
        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(user))
                .when()
                .post(Constants.AUTH_LOGIN_ENDPOINT);
    }

    @Step("Обновление email пользователя")
    public static Response updateUserEmail(String accessToken, String newEmail) {
        UserModel user = new UserModel(newEmail, null, null);
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(gson.toJson(user))
                .when()
                .patch(Constants.AUTH_USER_ENDPOINT);
    }

    @Step("Обновление name пользователя")
    public static Response updateUserName(String accessToken, String newName) {
        UserModel user = new UserModel(null, null, newName);
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(gson.toJson(user))
                .when()
                .patch(Constants.AUTH_USER_ENDPOINT);
    }

    @Step("Разлогин пользователя")
    public static Response logoutUser(String refreshToken) {
        return given()
                .contentType(ContentType.JSON)
                .body(gson.toJson(Map.of("token", refreshToken)))
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