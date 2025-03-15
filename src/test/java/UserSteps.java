import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class UserSteps {

    @Step("Создание пользователя: email={email}, name={name}")
    public static Response registerUser(String email, String password, String name) {
        return given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\", \"name\": \"%s\"}", email, password, name))
                .when()
                .post(Constants.AUTH_REGISTER_ENDPOINT);
    }

    @Step("Логин пользователя: email={email}")
    public static Response loginUser(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password))
                .when()
                .post(Constants.AUTH_LOGIN_ENDPOINT);
    }

    @Step("Обновление данных пользователя")
    public static Response updateUser(String accessToken, String newEmail, String newName) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(String.format("{\"email\": \"%s\", \"name\": \"%s\"}", newEmail, newName))
                .when()
                .patch(Constants.AUTH_USER_ENDPOINT);
    }

    @Step("Удаление пользователя")
    public static Response deleteUser(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .delete(Constants.AUTH_USER_ENDPOINT);
    }

    @Step("Разлогин пользователя")
    public static Response logoutUser(String refreshToken) {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"token\": \"" + refreshToken + "\"}")  // Тело запроса должно содержать token
                .when()
                .post(Constants.AUTH_LOGOUT_ENDPOINT);
    }
}
