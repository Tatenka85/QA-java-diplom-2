import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;

public class UserLoginTests {

    private final String email = "test" + System.currentTimeMillis() + "@example.com";
    private final String password = "TestPassword123!";

    @Test
    @Description("Логин под существующим пользователем")
    public void testLoginUser() {
        // Регистрация пользователя
        String name = "Tata";
        UserSteps.registerUser(email, password, name);

        // Логин под созданным пользователем
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().assertThat().statusCode(200);

        String accessToken = loginResponse.then().extract().body().path("accessToken");
        String refreshToken = loginResponse.then().extract().body().path("refreshToken");

        Assert.assertNotNull("AccessToken не должен быть null", accessToken);
        Assert.assertNotNull("RefreshToken не должен быть null", refreshToken);
    }

    @Test
    @Description("Логин с неверным логином или паролем")
    public void testLoginInvalidUser() {
        // Логин с неправильным паролем
        Response loginResponse = UserSteps.loginUser(email, "wrongPassword");
        loginResponse.then().assertThat().statusCode(401);

        String message = loginResponse.then().extract().body().path("message");
        Assert.assertEquals("Неверное сообщение об ошибке при неверном пароле",
                "email or password are incorrect", message);

        // Логин с неправильным email
        loginResponse = UserSteps.loginUser("wrongEmail@example.com", password);
        loginResponse.then().assertThat().statusCode(401);

        message = loginResponse.then().extract().body().path("message");
        Assert.assertEquals("Неверное сообщение об ошибке при неверном email",
                "email or password are incorrect", message);
    }
}