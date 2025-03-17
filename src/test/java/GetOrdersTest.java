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
        registerResponse.then().assertThat().statusCode(200);

        // Авторизация пользователя
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().assertThat().statusCode(200);
        accessToken = loginResponse.then().extract().body().path("accessToken");

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
        response.then().assertThat().statusCode(200);

        boolean success = response.then().extract().body().path("success");
        List<?> orders = response.then().extract().body().path("orders");

        Assert.assertTrue("Ответ должен содержать успех", success);
        Assert.assertFalse("Список заказов не должен быть пустым", orders.isEmpty());
    }

    @Test
    @Description("Попытка получить заказы без авторизации")
    public void testGetOrdersWithoutAuth() {
        Response response = OrderSteps.getUserOrders("");
        response.then().assertThat().statusCode(401);

        String message = response.then().extract().body().path("message");
        Assert.assertEquals("Сообщение об ошибке должно быть корректным", "You should be authorised", message);
    }

    @After
    @Description("Удаление пользователя после тестов")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            Response deleteResponse = UserSteps.deleteUser(accessToken);
            deleteResponse.then().assertThat().statusCode(202);
        }
    }
}