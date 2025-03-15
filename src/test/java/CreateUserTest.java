import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class CreateUserTest {

    private String accessToken;
    private String refreshToken;
    private final String email = "test" + System.currentTimeMillis() + "@example.com";
    private final String password = "TestPassword123!";
    private final String name = "Tata";

    @Test
    @Description("Создание пользователя без email")
    public void testCreateUserWithoutEmail() {
        Response response = UserSteps.registerUser("", password, name);
        response.then().statusCode(403);
        Assert.assertEquals(response.jsonPath().getString("message"), "Email, password and name are required fields");
    }

    @Test
    @Description("Создание уникального пользователя и сохранение токенов")
    public void testCreateUniqueUser() {
        Response response = UserSteps.registerUser(email, password, name);
        System.out.println("🔹 Ответ сервера на создание: " + response.asString());

        response.then().statusCode(200);
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        Response loginResponse = UserSteps.loginUser(email, password);
        System.out.println("🔹 Ответ сервера на логин: " + loginResponse.asString());

        accessToken = loginResponse.jsonPath().getString("accessToken");
        refreshToken = loginResponse.jsonPath().getString("refreshToken");

        System.out.println("✅ Логин успешен, accessToken: " + accessToken);
    }

    @Test
    @Description("Попытка создания уже зарегистрированного пользователя")
    public void testCreateDuplicateUser() {
        // Сначала создаём нового пользователя с уникальным email
        Response response = UserSteps.registerUser(email, password, name);
        System.out.println("🔹 Ответ сервера на создание уникального пользователя: " + response.asString());

        response.then().statusCode(200);
        Assert.assertTrue(response.jsonPath().getBoolean("success"));

        // Пытаемся создать пользователя с тем же email
        Response duplicateResponse = UserSteps.registerUser(email, password, name);
        System.out.println("🔹 Ответ сервера на попытку создать пользователя с дублирующим email: " + duplicateResponse.asString());

        duplicateResponse.then().statusCode(403); // Ожидаем ошибку 403, т.к. пользователь уже существует
        Assert.assertEquals(duplicateResponse.jsonPath().getString("message"), "User already exists");
    }

    @After
    @Description("Удаление пользователя после теста")
    public void cleanup() {
        System.out.println("🔹 Перед удалением пользователя accessToken: " + accessToken);

        if (accessToken != null && !accessToken.isEmpty()) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            System.out.println("🔹 Ответ сервера на удаление: " + deleteResponse.asString());
            deleteResponse.then().statusCode(202);

            // После удаления разлогиниваться НЕ нужно
            refreshToken = null;
        } else {
            System.out.println("⚠ Ошибка: accessToken отсутствует, удаление невозможно!");
        }

        if (refreshToken != null && !refreshToken.isEmpty()) {
            Response logoutResponse = UserSteps.logoutUser(refreshToken);
            System.out.println("🔹 Ответ сервера на разлогин: " + logoutResponse.asString());
            logoutResponse.then().statusCode(200);
        } else {
            System.out.println("⚠ Ошибка: refreshToken отсутствует или уже невалиден, разлогин невозможен!");
        }
    }
}