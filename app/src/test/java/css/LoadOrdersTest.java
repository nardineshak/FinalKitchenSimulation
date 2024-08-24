package css;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import css.Model.Order;
import css.Producers.LoadOrders;

public class LoadOrdersTest {

    private String testFilePath;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary JSON file for testing
        String jsonContent = "["
                + "{\"id\": \"1\", \"name\": \"Order 1\", \"prepTime\": 10},"
                + "{\"id\": \"2\", \"name\": \"Order 2\", \"prepTime\": 15}"
                + "]";

        File tempFile = File.createTempFile("orders", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(jsonContent);
        }
        testFilePath = tempFile.getAbsolutePath();
    }

    @Test
    void testLoadOrdersFromJsonWithValidFile() {
        LoadOrders loadOrders = new LoadOrders(testFilePath);
        List<Order> orders = loadOrders.getOrders();

        assertEquals(2, orders.size());
        assertEquals("1", orders.get(0).getId());
        assertEquals("Order 1", orders.get(0).getFoodItem());
        assertEquals(10, orders.get(0).getPrepTime());

        assertEquals("2", orders.get(1).getId());
        assertEquals("Order 2", orders.get(1).getFoodItem());
        assertEquals(15, orders.get(1).getPrepTime());
    }

    @Test
    void testLoadOrdersFromJsonWithInvalidFilePath() {
        String invalidFilePath = "invalid/path/to/orders.json";
        LoadOrders loadOrders = new LoadOrders(invalidFilePath);
        List<Order> orders = loadOrders.getOrders();

        assertTrue(orders.isEmpty());
    }

    @Test
    void testGetFilePathReturnsCorrectPath() {
        LoadOrders loadOrders = new LoadOrders(testFilePath);
        assertEquals(testFilePath, loadOrders.getFilePath());
    }

    @Test
    void testingInputFile() {
        String filePath = "src/test/resources/small_orders.json";
        LoadOrders loadOrders = new LoadOrders(filePath);
        List<Order> orders = loadOrders.getOrders();

        // Verify that the number of orders loaded matches the expected size
        assertEquals(4, orders.size(), "The number of orders loaded is incorrect.");

        try (FileReader file = new FileReader(filePath)) {
            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(file, JsonArray.class);

            int index = 0;
            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                String id = jsonObject.get("id").getAsString();
                String foodItem = jsonObject.get("name").getAsString();
                int prepTime = jsonObject.get("prepTime").getAsInt();

                Order order = orders.get(index++);
                assertEquals(new Order(id, foodItem, prepTime), order, 
                             "Order at index " + (index - 1) + " does not match.");
            }
        } catch (FileNotFoundException ex) {
            fail("Test failed because the file was not found: " + ex.getMessage());
        } catch (IOException ex) {
            fail("Test failed due to an IO error: " + ex.getMessage());
        }
    }
}
