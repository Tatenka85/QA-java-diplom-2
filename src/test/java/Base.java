import com.github.javafaker.Faker;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;

public class Base {
    protected static String email;
    protected static String password;
    protected static String name;

    protected static String accessToken; // Токен для авторизации
    protected static String refreshToken;

    private static final Faker faker = new Faker();

    @Before
    public void setup() {
        // Генерация случайных данных
        email = faker.internet().emailAddress();
        password = faker.internet().password(8, 16, true, true, true);
        name = faker.name().firstName();

        // Регистрация пользователя
        Response registerResponse = UserSteps.registerUser(email, password, name);
        registerResponse.then().statusCode(200);

        // Логин пользователя и получение токенов
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().statusCode(200);
        accessToken = loginResponse.jsonPath().getString("accessToken");
        refreshToken = loginResponse.jsonPath().getString("refreshToken");
    }

    @After
    public void cleanup() {
        // Разлогин пользователя
        System.out.println("🔹 Перед разлогином refreshToken: " + refreshToken);
        if (refreshToken != null && !refreshToken.isEmpty()) {
            Response logoutResponse = UserSteps.logoutUser(refreshToken);
            System.out.println("🔹 Ответ сервера на разлогин: " + logoutResponse.asString());
            logoutResponse.then().statusCode(200); // Убедимся, что разлогин прошел успешно
        } else {
            System.out.println("⚠ Ошибка: refreshToken отсутствует или уже невалиден, разлогин невозможен!");
        }

        // Удаление пользователя
        System.out.println("🔹 Перед удалением пользователя accessToken: " + accessToken);
        if (accessToken != null && !accessToken.isEmpty()) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            System.out.println("🔹 Ответ сервера на удаление: " + deleteResponse.asString());
            deleteResponse.then().statusCode(202); // Убедимся, что удаление прошло успешно
        } else {
            System.out.println("⚠ Ошибка: accessToken отсутствует, удаление невозможно!");
        }
    }
}