package css.Producers;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import css.Model.Order;

/**
 * Responsible for loading in the orders from a json file
 */
public class LoadOrders {

    private List<Order> orders;
    private final String filePath;

    public LoadOrders(String filePath) {
        this.filePath = filePath;
        this.orders = loadOrdersFromJson();
    }

    public String getFilePath() {
        return filePath;
    }

    public List<Order> getOrders() {
        return orders;
    }

    private List<Order> loadOrdersFromJson() {
        orders = new ArrayList<>();
        try (FileReader file = new FileReader(filePath)) {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(file, JsonArray.class);

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();

                String id = jsonObject.get("id").getAsString();
                String foodItem = jsonObject.get("name").getAsString();
                int prepTime = jsonObject.get("prepTime").getAsInt();

                Order newOrder = new Order(id, foodItem, prepTime);
                orders.add(newOrder);
            }

        } catch (IOException e) {
            System.out.println("File not found.");
            e.printStackTrace();
        }

        return orders;
    }

}
