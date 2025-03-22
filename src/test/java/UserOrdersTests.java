import com.github.javafaker.Faker;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import java.util.List;

public class UserOrdersTests extends Base {

    private final Faker faker = new Faker();
    private String accessToken;

    @Before
    public void setUp() {
        // Генерация уникальных данных для пользователя
        UserModel user = new UserModel(
                faker.internet().emailAddress(),
                faker.internet().password(8, 16, true, true, true),
                faker.name().firstName()
        );

        // Регистрация пользователя
        Response registerResponse = UserSteps.registerUser(user);
        registerResponse.then().statusCode(200);

        // Логин пользователя и получение токена
        Response loginResponse = UserSteps.loginUser(user);
        loginResponse.then().statusCode(200);
        accessToken = loginResponse.jsonPath().getString("accessToken");
    }

    @Test
    @Description("Получение заказов авторизованного пользователя")
    public void testGetOrdersWithAuth() {
        // Создание заказа
        List<String> ingredients = OrderSteps.getIngredients();
        assertFalse("Список ингредиентов не должен быть пустым", ingredients.isEmpty());

        // Создаем заказ
        Response createOrderResponse = OrderSteps.createOrder(accessToken, ingredients.subList(0, 2));
        System.out.println("Create Order Response: " + createOrderResponse.asString());
        createOrderResponse.then().statusCode(200);
        assertTrue(createOrderResponse.jsonPath().getBoolean("success"));
        assertNotNull(createOrderResponse.jsonPath().getString("order.number"));

        // Получение заказов пользователя
        Response getOrdersResponse = OrderSteps.getUserOrders(accessToken);
        System.out.println("Get Orders Response: " + getOrdersResponse.asString());
        getOrdersResponse.then().statusCode(200);
        assertTrue(getOrdersResponse.jsonPath().getBoolean("success"));
        assertFalse(getOrdersResponse.jsonPath().getList("orders").isEmpty());
    }

    @Test
    @Description("Получение заказов неавторизованного пользователя")
    public void testGetOrdersWithoutAuth() {
        // Попытка получить заказы без авторизации
        Response getOrdersResponse = OrderSteps.getUserOrders("");
        System.out.println("Get Orders Response: " + getOrdersResponse.asString());
        getOrdersResponse.then().statusCode(401);
        assertEquals("You should be authorised", getOrdersResponse.jsonPath().getString("message"));
    }
}