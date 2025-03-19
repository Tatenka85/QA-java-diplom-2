import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;
import java.util.List;

public class OrderCreationTests extends Base {

    private List<String> ingredients;
    private final Gson gson = new Gson();
    private final Faker faker = new Faker();// Инициализация Gson

    @Before
    public void setUp() {
            // Генерация уникальных данных для пользователя
            String email = faker.internet().emailAddress();
            String password = faker.internet().password(8, 16, true, true, true);
            String name = faker.name().firstName();

            // Регистрация пользователя
            Response registerResponse = UserSteps.registerUser(email, password, name);
            registerResponse.then().statusCode(200);

            // Логин пользователя и получение токена
            Response loginResponse = UserSteps.loginUser(email, password);
            loginResponse.then().statusCode(200);
            accessToken = loginResponse.jsonPath().getString("accessToken");
        // Получаем список доступных ингредиентов перед каждым тестом
        ingredients = OrderSteps.getIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());
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

    @Test
    @Description("Создание заказа с авторизацией и ингредиентами")
    public void testCreateOrderWithAuthAndIngredients() {
        // Создаем заказ с авторизацией
        Response response = OrderSteps.createOrder(accessToken, ingredients.subList(0, 2));
        System.out.println("Ответ сервера: " + response.asString());

        // Ожидаем статус 200 и успешное создание заказа
        response.then().statusCode(200);
        JsonObject responseJson = gson.fromJson(response.asString(), JsonObject.class); // Парсинг JSON
        assertTrue(responseJson.get("success").getAsBoolean());
        assertNotNull(responseJson.getAsJsonObject("order").get("number").getAsString());
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
        response.then().statusCode(200);
        JsonObject responseJson = gson.fromJson(response.asString(), JsonObject.class); // Парсинг JSON
        assertTrue(responseJson.get("success").getAsBoolean());
        assertNotNull(responseJson.getAsJsonObject("order").get("number").getAsString());
    }

    @Test
    @Description("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        // Создаем заказ без ингредиентов
        Response response = OrderSteps.createOrder(accessToken, List.of());
        System.out.println("Ответ сервера: " + response.asString());

        // Ожидаем статус 400 и сообщение об ошибке
        response.then().statusCode(400);
        JsonObject responseJson = gson.fromJson(response.asString(), JsonObject.class); // Парсинг JSON
        assertEquals("Ingredient ids must be provided", responseJson.get("message").getAsString());
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
}