import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserUpdateTests extends Base {

    @Test
    @Description("Изменение данных пользователя с авторизацией")
    public void testUpdateUserWithAuth() {
        String newEmail = "new" + System.currentTimeMillis() + "@example.com";
        String newName = "NewName";

        Response updateResponse = UserSteps.updateUser(accessToken, newEmail, newName);
        updateResponse.then().statusCode(200);
        assertEquals(newEmail, updateResponse.jsonPath().getString("user.email"));
        assertEquals(newName, updateResponse.jsonPath().getString("user.name"));
    }

    @Test
    @Description("Изменение данных пользователя без авторизации")
    public void testUpdateUserWithoutAuth() {
        Response updateResponse = UserSteps.updateUser("", "unauthorized@example.com", "NoAuthName");
        updateResponse.then().statusCode(401);
        assertEquals("You should be authorised", updateResponse.jsonPath().getString("message"));
    }
}