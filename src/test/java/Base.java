import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;

public class Base {
    protected static String email;
    protected static String password = "TestPassword123!";
    protected static String name = "Tata";

    protected static String accessToken;
    protected static String refreshToken;

    @Before
    public void setup() {
        email = "test" + System.currentTimeMillis() + "@example.com";
        Response registerResponse = UserSteps.registerUser(email, password, name);
        registerResponse.then().statusCode(200);

        Response loginResponse = UserSteps.loginUser(email, password);
        accessToken = loginResponse.jsonPath().getString("accessToken");
        refreshToken = loginResponse.jsonPath().getString("refreshToken");
    }

    @After
    public void cleanup() {

        System.out.println("🔹 Перед разлогином refreshToken: " + refreshToken);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            Response logoutResponse = UserSteps.logoutUser(refreshToken);
            System.out.println("🔹 Ответ сервера на разлогин: " + logoutResponse.asString());
            logoutResponse.then().statusCode(200);
        } else {
            System.out.println("⚠ Ошибка: refreshToken отсутствует или уже невалиден, разлогин невозможен!");
        }
        System.out.println("🔹 Перед удалением пользователя accessToken: " + accessToken);
        if (accessToken != null && !accessToken.isEmpty()) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            System.out.println("🔹 Ответ сервера на удаление: " + deleteResponse.asString());
            deleteResponse.then().statusCode(202);
        } else {
            System.out.println("⚠ Ошибка: accessToken отсутствует, удаление невозможно!");
        }
    }
}