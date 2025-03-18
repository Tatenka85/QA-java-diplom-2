import com.google.gson.Gson;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class OrderSteps {

    private static final Gson gson = new Gson();

    @Step("Получение списка доступных ингредиентов")
    public static List<String> getIngredients() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(Constants.INGREDIENTS_ENDPOINT);

        response.then().statusCode(200);
        return response.jsonPath().getList("data._id", String.class);
    }

    @Step("Создание заказа с ингредиентами")
    public static Response createOrder(String accessToken, List<String> ingredientIds) {
        Map<String, Object> requestBody = Map.of("ingredients", ingredientIds);
        String jsonBody = gson.toJson(requestBody);

        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body(jsonBody)
                .when()
                .post(Constants.ORDERS_ENDPOINT);
    }

    @Step("Получение заказов пользователя")
    public static Response getUserOrders(String accessToken) {
        return given()
                .header("Authorization", accessToken)
                .when()
                .get(Constants.ORDERS_ENDPOINT);
    }
}