import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class GetOrdersTest {
    private String accessToken;

    @Before
    @Description("Регистрация и авторизация пользователя перед тестом, получение списка ингредиентов и создание заказа")
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
        List<String> availableIngredients = OrderSteps.getIngredients();
        Assert.assertFalse("Список ингредиентов не должен быть пустым", availableIngredients.isEmpty());

        // Создаём заказ, используя первые доступные ингредиенты
        OrderSteps.createOrder(accessToken, availableIngredients.subList(0, 1));
    }

    @Test
    @Description("Получение заказов авторизованного пользователя")
    public void testGetOrdersWithAuth() {
        Response response = OrderSteps.getUserOrders(accessToken);
        response.then().statusCode(200);

        Assert.assertTrue("Ответ должен содержать успех", response.jsonPath().getBoolean("success"));
        Assert.assertFalse("Список заказов не должен быть пустым", response.jsonPath().getList("orders").isEmpty());
    }

    @Test
    @Description("Попытка получить заказы без авторизации")
    public void testGetOrdersWithoutAuth() {
        Response response = OrderSteps.getUserOrders("");
        response.then().statusCode(401);

        Assert.assertEquals("Сообщение об ошибке должно быть корректным",
                "You should be authorised",
                response.jsonPath().getString("message"));
    }

    @After
    @Description("Удаление пользователя после тестов")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            UserSteps.deleteUser(accessToken).then().statusCode(202);
        }
    }
}
