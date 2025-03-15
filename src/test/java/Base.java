import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;

public class Base {
    protected static String email = "TBelanova@logycom.kz";
    protected static String password = "123456";

    protected static String accessToken;
    protected static String refreshToken;

    @Before
    public void setup() {
        Response loginResponse = loginUser(email, password);
        accessToken = loginResponse.jsonPath().getString("accessToken");
        refreshToken = loginResponse.jsonPath().getString("refreshToken");
    }

    protected Response loginUser(String email, String password) {
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().statusCode(200);
        return loginResponse;
    }

    @After
    public void cleanup() {
        if (accessToken != null) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            deleteResponse.then().statusCode(202);
        }
        if (refreshToken != null) {
            Response logoutResponse = UserSteps.logoutUser(refreshToken);
            logoutResponse.then().statusCode(200);
        }
    }
}
