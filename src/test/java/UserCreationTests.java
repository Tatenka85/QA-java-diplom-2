import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserCreationTests extends Base {

    @Test
    @Description("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        String email = "test" + System.currentTimeMillis() + "@example.com";
        String password = "TestPassword123!";
        String name = "Tata";

        Response response = UserSteps.registerUser(email, password, name);
        System.out.println("Ответ сервера: " + response.asString()); // Логирование ответа
        response.then().statusCode(200);
        assertTrue(response.jsonPath().getBoolean("success"));
    }

    @Test
    @Description("Создание пользователя, который уже зарегистрирован")
    public void testCreateDuplicateUser() {
        Response response = UserSteps.registerUser(email, password, name);
        response.then().statusCode(403);
        assertEquals("User already exists", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без заполнения одного из обязательных полей")
    public void testCreateUserWithoutRequiredField() {
        Response response = UserSteps.registerUser("", password, name);
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }
}