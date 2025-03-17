import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;

public class Base {
    protected static String email = "TBelanova@logycom.kz";
    protected static String password = "123456";

    protected static String accessToken;
    protected static String refreshToken;

    @Before
    public void setUp() {
        // Настройка RestAssured: отключаем логирование, кроме случаев ошибок
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(); // Логирование только при ошибках
        RestAssured.config = RestAssured.config()
                .logConfig(io.restassured.config.LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)); // Логировать всё при ошибках
        Response loginResponse = loginUser(email, password);
        loginResponse.then().assertThat().statusCode(200);

        accessToken = loginResponse.then().extract().body().path("accessToken");
        refreshToken = loginResponse.then().extract().body().path("refreshToken");
    }

    protected Response loginUser(String email, String password) {
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().assertThat().statusCode(200);
        return loginResponse;
    }

    @After
    public void cleanup() {
        if (accessToken != null) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            deleteResponse.then().assertThat().statusCode(202);
        }
        if (refreshToken != null) {
            Response logoutResponse = UserSteps.logoutUser(refreshToken);
            logoutResponse.then().assertThat().statusCode(200);
        }
    }
}