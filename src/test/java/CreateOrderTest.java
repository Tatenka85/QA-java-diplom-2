import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
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
        registerResponse.then().assertThat().statusCode(200);

        // Авторизация пользователя
        Response loginResponse = UserSteps.loginUser(email, password);
        loginResponse.then().assertThat().statusCode(200);

        // Извлечение accessToken
        accessToken = loginResponse.then().extract().body().path("accessToken");

        // Получение списка доступных ингредиентов
        availableIngredients = OrderSteps.getIngredients();
        Assert.assertFalse("Список ингредиентов не должен быть пустым", availableIngredients.isEmpty());
    }

    @Test
    @Description("Создание заказа с авторизацией и ингредиентами")
    public void testCreateOrderWithAuthAndIngredients() {
        Response response = OrderSteps.createOrder(accessToken, availableIngredients.subList(0, 2));
        response.then().assertThat().statusCode(200);

        boolean success = response.then().extract().body().path("success");

        Assert.assertTrue("Ответ должен содержать успех", success);
    }

    @Test
    @Description("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        Response response = OrderSteps.createOrder("", availableIngredients.subList(0, 2));
        response.then().assertThat().statusCode(200);

        boolean success = response.then().extract().body().path("success");

        Assert.assertTrue("Ответ должен содержать успех", success);

    }

    @Test
    @Description("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        Response response = OrderSteps.createOrder(accessToken, new ArrayList<>());
        response.then().assertThat().statusCode(400);

        String message = response.then().extract().body().path("message");
        Assert.assertEquals("Неверное сообщение об ошибке", "Ingredient ids must be provided", message);
    }

    @Test
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void testCreateOrderWithInvalidIngredient() {
        String invalidIngredient = "9999888";
        List<String> ingredients = new ArrayList<>();
        ingredients.add(invalidIngredient);
        Response response = OrderSteps.createOrder(accessToken, ingredients);
        response.then().assertThat().statusCode(500);
    }

    @After
    @Description("Удаление пользователя после теста")
    public void tearDown() {
        if (accessToken != null && !accessToken.isEmpty()) {
            UserSteps.deleteUser(accessToken).then().assertThat().statusCode(202);
        }
    }
}