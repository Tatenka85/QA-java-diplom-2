import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class CreateOrderTest {
    private String accessToken;
    private List<String> availableIngredients;

    @Before
    @Description("Регистрация и авторизация пользователя перед тестом, получение списка ингредиентов")
    public void setUp() {
        // Регистрация нового пользователя
        String email = "test" + System.currentTimeMillis() + "@example.com";
        String password = "TestPassword123!";
        String name = "Tata";

        Response registerResponse = UserSteps.registerUser(email, password, name);
        registerResponse.then().statusCode(200);

        // Авторизация пользователя
        Response loginResponse = UserSteps.loginUser(email, password);
        accessToken = loginResponse.jsonPath().getString("accessToken");

        // Получение списка доступных ингредиентов
        availableIngredients = OrderSteps.getIngredients();
        Assert.assertFalse("Список ингредиентов не должен быть пустым", availableIngredients.isEmpty());
    }

    @Test
    @Description("Создание заказа с авторизацией и ингредиентами")
    public void testCreateOrderWithAuthAndIngredients() {
        Response response = OrderSteps.createOrder(accessToken, availableIngredients.subList(0, 2));
        response.then().statusCode(200);

        Assert.assertTrue("Ответ должен содержать успех", response.jsonPath().getBoolean("success"));
        response.jsonPath().getInt("order.number");
    }

    @Test
    @Description("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        Response response = OrderSteps.createOrder("", availableIngredients.subList(0, 2));
        response.then().statusCode(200);

        Assert.assertTrue("Ответ должен содержать успех", response.jsonPath().getBoolean("success"));
        response.jsonPath().getInt("order.number");
    }

    @Test
    @Description("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        Response response = OrderSteps.createOrder(accessToken, List.of());
        response.then().statusCode(400);

        Assert.assertEquals("Неверное сообщение об ошибке", "Ingredient ids must be provided",
                response.jsonPath().getString("message"));
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void testCreateOrderWithInvalidIngredient() {
        String invalidIngredient = "9999888";
        Response response = OrderSteps.createOrder(accessToken, List.of(invalidIngredient));
        response.then().statusCode(500);
    }

    @After
    @Description("Удаление пользователя после теста")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            UserSteps.deleteUser(accessToken).then().statusCode(202);
        }
    }
}
