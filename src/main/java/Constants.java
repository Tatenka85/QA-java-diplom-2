public class Constants {
    // Базовый URL для всех запросов
    public static final String BASE_URL = "https://stellarburgers.nomoreparties.site/api";

    // Эндпоинты для работы с ингредиентами
    public static final String INGREDIENTS_ENDPOINT = BASE_URL + "/ingredients";

    // Эндпоинты для работы с заказами
    public static final String ORDERS_ENDPOINT = BASE_URL + "/orders";

    // Эндпоинты для управления пользователями
    public static final String AUTH_REGISTER_ENDPOINT = BASE_URL + "/auth/register";
    public static final String AUTH_LOGIN_ENDPOINT = BASE_URL + "/auth/login";
    public static final String AUTH_LOGOUT_ENDPOINT = BASE_URL + "/auth/logout";
    public static final String AUTH_TOKEN_ENDPOINT = BASE_URL + "/auth/token";
    public static final String AUTH_USER_ENDPOINT = BASE_URL + "/auth/user";

}
