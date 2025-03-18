import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Test;
import static org.junit.Assert.*;
import com.github.javafaker.Faker;

public class UserCreationTests extends Base {
    private final Faker faker = new Faker();

    @Test
    @Description("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        String email = faker.internet().emailAddress();
        String password = faker.internet().password(8, 16, true, true, true);
        String name = faker.name().firstName();

        Response response = UserSteps.registerUser(email, password, name);
        System.out.println("Ответ сервера: " + response.asString());
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
    @Description("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        Response response = UserSteps.registerUser("", password, name);
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без имени")
    public void testCreateUserWithoutName() {
        Response response = UserSteps.registerUser(email, password, "");
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание пользователя без пароля")
    public void testCreateUserWithoutPassword() {
        Response response = UserSteps.registerUser(email, "", name);
        response.then().statusCode(403);
        assertEquals("Email, password and name are required fields", response.jsonPath().getString("message"));
    }
}