import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UserUpdateTests {
    private String email;
    private final String name = "Tata";
    private String accessToken;

    @Before
    @Description("Регистрация пользователя перед тестом")
    public void setUp() {
        email = "test" + System.currentTimeMillis() + "@example.com";
        String password = "TestPassword123!";

        Response registerResponse = UserSteps.registerUser(email, password, name);
        registerResponse.then().assertThat().statusCode(200);

        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().assertThat().statusCode(200);
        accessToken = loginResponse.then().extract().body().path("accessToken");
    }

    @Test
    @Description("Изменение email пользователя с авторизацией")
    public void testUpdateEmailWithAuth() {
        String newEmail = "new" + System.currentTimeMillis() + "@example.com";

        Response updateResponse = UserSteps.updateUser(accessToken, newEmail, name);
        updateResponse.then().assertThat().statusCode(200);

        String updatedEmail = updateResponse.then().extract().body().path("user.email");
        String updatedName = updateResponse.then().extract().body().path("user.name");

        Assert.assertEquals("Email не обновился", newEmail, updatedEmail);
        Assert.assertEquals("Имя изменилось, хотя не должно", name, updatedName);
    }

    @Test
    @Description("Изменение имени пользователя с авторизацией")
    public void testUpdateNameWithAuth() {
        String newName = "NewName";

        Response updateResponse = UserSteps.updateUser(accessToken, email, newName);
        updateResponse.then().assertThat().statusCode(200);

        String updatedName = updateResponse.then().extract().body().path("user.name");
        String updatedEmail = updateResponse.then().extract().body().path("user.email");

        Assert.assertEquals("Имя не обновилось", newName, updatedName);
        Assert.assertEquals("Email изменился, хотя не должен", email, updatedEmail);
    }

    @Test
    @Description("Изменение данных пользователя без авторизации")
    public void testUpdateUserWithoutAuth() {
        Response updateResponse = UserSteps.updateUser("", "unauthorized@example.com", "NoAuthName");
        updateResponse.then().assertThat().statusCode(401);

        String message = updateResponse.then().extract().body().path("message");
        Assert.assertEquals("Некорректное сообщение об ошибке", "You should be authorised", message);
    }

    @Test
    @Description("Попытка изменить email на уже существующий")
    public void testUpdateUserWithDuplicateEmail() {
        String duplicateEmail = "existinguser@example.com";

        Response updateResponse = UserSteps.updateUser(accessToken, duplicateEmail, name);
        updateResponse.then().assertThat().statusCode(403);

        String message = updateResponse.then().extract().body().path("message");
        Assert.assertEquals("Некорректное сообщение об ошибке", "User with such email already exists", message);
    }

    @After
    @Description("Удаление пользователя после тестов")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            deleteResponse.then().assertThat().statusCode(202);
        }
    }
}