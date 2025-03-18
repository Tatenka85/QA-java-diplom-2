import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserLoginTests extends Base {

    @Test
    @Description("Логин под существующим пользователем")
    public void testLoginUser() {
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().statusCode(200);
        assertNotNull(loginResponse.jsonPath().getString("accessToken"));
        assertNotNull(loginResponse.jsonPath().getString("refreshToken"));
    }

    @Test
    @Description("Логин с неверным логином и паролем")
    public void testLoginInvalidUser() {
        Response loginResponse = UserSteps.loginUser("wrongEmail@example.com", "wrongPassword");
        loginResponse.then().statusCode(401);
        assertEquals("email or password are incorrect", loginResponse.jsonPath().getString("message"));
    }
}