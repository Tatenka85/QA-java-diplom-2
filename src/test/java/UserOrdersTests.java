import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public class UserOrdersTests extends Base {

    @Test
    @Description("Получение заказов авторизованного пользователя")
    public void testGetOrdersWithAuth() {
        List<String> ingredients = OrderSteps.getIngredients();
        OrderSteps.createOrder(accessToken, ingredients.subList(0, 2));

        Response response = OrderSteps.getUserOrders(accessToken);
        response.then().statusCode(200);
        assertTrue(response.jsonPath().getBoolean("success"));
        assertFalse(response.jsonPath().getList("orders").isEmpty());
    }

    @Test
    @Description("Попытка получить заказы без авторизации")
    public void testGetOrdersWithoutAuth() {
        Response response = OrderSteps.getUserOrders("");
        response.then().statusCode(401);
        assertEquals("You should be authorised", response.jsonPath().getString("message"));
    }
}