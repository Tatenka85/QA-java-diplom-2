import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class OrderModel {
    // Геттер и сеттер
    private List<String> ingredients;

    public OrderModel(List<String> ingredients) {
        this.ingredients = ingredients;
    }

}