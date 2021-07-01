package org.example.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.example.model.Order;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Random;

import static io.restassured.RestAssured.given;

public class HomeTaskApiTest {
    @BeforeClass
    public void prepare() {

        System.getProperties().put("api.key", "");
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://petstore.swagger.io/v2/store/") // задаём базовый адрес каждого ресурса
                .addHeader("api_key", System.getProperty("api.key"))
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .log(LogDetail.ALL)
                .build();
        RestAssured.filters(new ResponseLoggingFilter());
    }

    /**
     * Простейшая проверка: создаём объект, сохраняем на сервере и проверяем, что при запросе возвращается
     * "тот же" объект
     */
    @Test
    public void checkObjectSave() {
        Order order = new Order();
        int id = new Random().nextInt(500000);
        order.setId(id);

        given()
                .body(order)
                .post("/order")
                .then()
                .statusCode(200);

        Order actual =
                given()
                        .pathParam("id", id)
                        .when()
                        .get("/order/{id}")
                        .then()
                        .statusCode(200)
                        .extract().body()
                        .as(Order.class);
        Assert.assertEquals(actual.getId(), order.getId());

    }

    @Test
    public void orderDelete() throws IOException {
        Order order = new Order();
        int id = new Random().nextInt(500000);
        order.setId(id);

        //создаем заказ для дальнейшего удаления

        given()
                .body(order)
                .post("/order")
                .then()
                .statusCode(200);


        //удаляем его же

        given()
                .pathParam("id", id)
                .when()
                .delete("/order/{id}")
                .then()
                .statusCode(200);

        //ищем удаленный заказ

        given()
                .pathParam("id", id)
                .when()
                .get("/order/{id}")
                .then()
                .statusCode(404);
    }
}
