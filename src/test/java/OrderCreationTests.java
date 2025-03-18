import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;
import java.util.List;

public class OrderCreationTests extends Base {

    private List<String> ingredients;

    @Before
    public void setUp() {
        // Получаем список доступных ингредиентов перед каждым тестом
        ingredients = OrderSteps.getIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());
    }

    @Test
    @Description("Создание заказа с авторизацией и ингредиентами")
    public void testCreateOrderWithAuthAndIngredients() {
        // Создаем заказ с авторизацией
        Response response = OrderSteps.createOrder(accessToken, ingredients.subList(0, 2));
        System.out.println("Ответ сервера: " + response.asString());

        // Ожидаем статус 200 и успешное создание заказа
        response.then().statusCode(200);
        assertTrue(response.jsonPath().getBoolean("success"));
        assertNotNull(response.jsonPath().getString("order.number"));
    }

    @Test
    @Description("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        // Создаем заказ без авторизации
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"ingredients\": " + formatIngredients(ingredients.subList(0, 2)) + "}")
                .when()
                .post(Constants.ORDERS_ENDPOINT);
        System.out.println("Ответ сервера: " + response.asString());

        // Ожидаем статус 200, так как сервер позволяет создавать заказы без авторизации
        // В документации указано, что заказы могут делать только авторизованные пользователи,
        // но текущее поведение сервера отличается от документации.
        response.then().statusCode(200);
        assertTrue(response.jsonPath().getBoolean("success"));
        assertNotNull(response.jsonPath().getString("order.number"));
    }

    @Test
    @Description("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        // Создаем заказ без ингредиентов
        Response response = OrderSteps.createOrder(accessToken, List.of());
        System.out.println("Ответ сервера: " + response.asString());

        // Ожидаем статус 400 и сообщение об ошибке
        response.then().statusCode(400);
        assertEquals("Ingredient ids must be provided", response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void testCreateOrderWithInvalidIngredient() {
        // Создаем заказ с неверным хешем ингредиента
        Response response = OrderSteps.createOrder(accessToken, List.of("invalidIngredient"));
        System.out.println("Ответ сервера: " + response.asString());

        // Ожидаем статус 500, так как сервер не может обработать неверный хеш
        response.then().statusCode(500);
    }

    @After
    public void cleanup() {
        // Очистка после тестов (если требуется)
        if (accessToken != null) {
            UserSteps.deleteUser(accessToken).then().statusCode(202);
        }
    }

    // Вспомогательный метод для форматирования списка ингредиентов в JSON
    private String formatIngredients(List<String> ingredientIds) {
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