import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.List;

import static io.restassured.RestAssured.given;

public class OrderSteps {

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
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", accessToken)
                .body("{\"ingredients\": " + formatIngredients(ingredientIds) + "}")
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

    private static String formatIngredients(List<String> ingredientIds) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return "[]";
        }
        StringBuilder formatted = new StringBuilder("[");
        for (String id : ingredientIds) {
            formatted.append("\"").append(id).append("\",");
        }
        formatted.deleteCharAt(formatted.length() - 1).append("]");
        return formatted.toString();
    }
}
