package com.projectbible.shop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JavaSpringbootGradleJpaPostgresqlApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void healthSmoke() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.domain").value("shop"));
    }

    @Test
    void authFlowAndAuthorizationBoundary() throws Exception {
        String suffix = uniqueSuffix();
        String email = "shop-auth-" + suffix + "@example.com";

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", email,
                    "password", "Password1!",
                    "name", "shop-auth-" + suffix,
                    "phone", "010-9000-" + suffix.substring(Math.max(0, suffix.length() - 4))
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value(email));

        LoginTokens userLogin = userLogin(email, "Password1!");
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(userLogin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(email));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", userLogin.refreshToken()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", bearer(userLogin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", userLogin.refreshToken()))))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", "WrongPassword1!"))))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users/me"))
            .andExpect(status().isUnauthorized());

        LoginTokens adminLogin = adminLogin("admin-shop-1@example.com", "AdminPassword1!");
        mockMvc.perform(get("/api/v1/admin/me").header("Authorization", bearer(adminLogin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("admin-shop-1@example.com"));

        mockMvc.perform(get("/api/v1/admin/me").header("Authorization", bearer(userLogin.accessToken())))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", adminLogin.refreshToken()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/v1/admin/auth/logout").header("Authorization", bearer(adminLogin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void categoryProductCartOrderPaymentReviewAndAdminFlow() throws Exception {
        String suffix = uniqueSuffix();
        LoginTokens admin = adminLogin("admin-shop-1@example.com", "AdminPassword1!");

        long categoryId = readLong(mockMvc.perform(post("/api/v1/admin/categories")
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "category-" + suffix,
                    "displayOrder", 50,
                    "status", "ACTIVE"
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.name").value("category-" + suffix))
            .andReturn(), "/data/id");

        mockMvc.perform(patch("/api/v1/admin/categories/{categoryId}", categoryId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "category-updated-" + suffix,
                    "displayOrder", 51,
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("category-updated-" + suffix));

        long productId = readLong(mockMvc.perform(post("/api/v1/admin/products")
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "categoryId", categoryId,
                    "name", "product-" + suffix,
                    "description", "product-" + suffix,
                    "price", 199000,
                    "stock", 20,
                    "status", "ACTIVE"
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.name").value("product-" + suffix))
            .andReturn(), "/data/id");

        mockMvc.perform(patch("/api/v1/admin/products/{productId}", productId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "categoryId", categoryId,
                    "name", "product-updated-" + suffix,
                    "description", "product-updated-" + suffix,
                    "price", 209000,
                    "stock", 18,
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("product-updated-" + suffix));

        long optionId = readLong(mockMvc.perform(post("/api/v1/admin/products/{productId}/options", productId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "switch",
                    "value", "silent-" + suffix,
                    "additionalPrice", 3000,
                    "stock", 10
                ))))
            .andExpect(status().is2xxSuccessful())
            .andReturn(), "/data/id");

        mockMvc.perform(patch("/api/v1/admin/product-options/{optionId}", optionId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "switch",
                    "value", "silent-updated-" + suffix,
                    "additionalPrice", 5000,
                    "stock", 9
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.value").value("silent-updated-" + suffix));

        long disposableOptionId = readLong(mockMvc.perform(post("/api/v1/admin/products/{productId}/options", productId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "switch",
                    "value", "linear-" + suffix,
                    "additionalPrice", 1000,
                    "stock", 8
                ))))
            .andExpect(status().is2xxSuccessful())
            .andReturn(), "/data/id");

        long imageId = readLong(mockMvc.perform(post("/api/v1/admin/products/{productId}/images", productId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "imageUrl", "https://example.com/" + suffix + ".jpg",
                    "isPrimary", true,
                    "displayOrder", 1
                ))))
            .andExpect(status().is2xxSuccessful())
            .andReturn(), "/data/id");

        mockMvc.perform(patch("/api/v1/admin/product-images/{imageId}", imageId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "imageUrl", "https://example.com/" + suffix + "-updated.jpg",
                    "isPrimary", false,
                    "displayOrder", 2
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/" + suffix + "-updated.jpg"));

        mockMvc.perform(get("/api/v1/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(categoryId));

        mockMvc.perform(get("/api/v1/products").param("page", "1").param("limit", "10").param("search", "product-updated-" + suffix))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.page").value(1))
            .andExpect(jsonPath("$.data[0].id").value(productId));

        mockMvc.perform(get("/api/v1/products/{productId}", productId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(productId))
            .andExpect(jsonPath("$.data.options[0].id").exists())
            .andExpect(jsonPath("$.data.images[0].id").exists());

        String buyerEmail = "shop-buyer-" + suffix + "@example.com";
        String otherEmail = "shop-other-" + suffix + "@example.com";
        signupUser(buyerEmail, "buyer-" + suffix, "010-1000-" + suffix.substring(Math.max(0, suffix.length() - 4)));
        signupUser(otherEmail, "other-" + suffix, "010-2000-" + suffix.substring(Math.max(0, suffix.length() - 4)));
        LoginTokens buyer = userLogin(buyerEmail, "Password1!");
        LoginTokens other = userLogin(otherEmail, "Password1!");

        long addressId = readLong(mockMvc.perform(post("/api/v1/addresses")
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "recipientName", "Buyer " + suffix,
                    "phone", "010-1000-0000",
                    "zipCode", "06234",
                    "address1", "Seoul Street " + suffix,
                    "address2", "101-101",
                    "isDefault", true
                ))))
            .andExpect(status().is2xxSuccessful())
            .andReturn(), "/data/id");

        mockMvc.perform(patch("/api/v1/addresses/{addressId}", addressId)
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "recipientName", "Buyer Updated " + suffix,
                    "phone", "010-1000-1111",
                    "zipCode", "06234",
                    "address1", "Seoul Street Updated " + suffix,
                    "address2", "202-202",
                    "isDefault", true
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.recipientName").value("Buyer Updated " + suffix));

        long cartItemId = readLong(mockMvc.perform(post("/api/v1/cart-items")
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "productId", productId,
                    "productOptionId", optionId,
                    "quantity", 1
                ))))
            .andExpect(status().is2xxSuccessful())
            .andReturn(), "/data/id");

        long disposableCartItemId = readLong(mockMvc.perform(post("/api/v1/cart-items")
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "productId", productId,
                    "productOptionId", disposableOptionId,
                    "quantity", 1
                ))))
            .andExpect(status().is2xxSuccessful())
            .andReturn(), "/data/id");

        mockMvc.perform(get("/api/v1/cart-items").header("Authorization", bearer(buyer.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(patch("/api/v1/cart-items/{cartItemId}", cartItemId)
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "productId", productId,
                    "productOptionId", optionId,
                    "quantity", 2
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.quantity").value(2));

        mockMvc.perform(delete("/api/v1/cart-items/{cartItemId}", disposableCartItemId)
                .header("Authorization", bearer(buyer.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.message").exists());

        MvcResult orderCreation = mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "cartItemIds", new long[]{cartItemId},
                    "addressId", addressId
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.order.id").exists())
            .andExpect(jsonPath("$.data.orderAddress.recipientName").value("Buyer Updated " + suffix))
            .andExpect(jsonPath("$.data.orderItems[0].id").exists())
            .andReturn();

        long orderId = readLong(orderCreation, "/data/order/id");
        long orderItemId = readLong(orderCreation, "/data/orderItems/0/id");

        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                .header("Authorization", bearer(buyer.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.order.id").value(orderId))
            .andExpect(jsonPath("$.data.orderItems[0].id").value(orderItemId));

        long paymentId = readLong(mockMvc.perform(post("/api/v1/payments")
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "orderId", orderId,
                    "paymentMethod", "mock-card"
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.orderId").value(orderId))
            .andReturn(), "/data/id");

        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId)
                .header("Authorization", bearer(buyer.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(paymentId));

        long reviewId = readLong(mockMvc.perform(post("/api/v1/order-items/{orderItemId}/reviews", orderItemId)
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "rating", 5,
                    "content", "review-" + suffix
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.orderItemId").value(orderItemId))
            .andReturn(), "/data/id");

        mockMvc.perform(post("/api/v1/order-items/{orderItemId}/reviews", orderItemId)
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "rating", 4,
                    "content", "duplicate-review"
                ))))
            .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId)
                .header("Authorization", bearer(buyer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "rating", 4,
                    "content", "review-updated-" + suffix
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("review-updated-" + suffix));

        mockMvc.perform(patch("/api/v1/reviews/{reviewId}", reviewId)
                .header("Authorization", bearer(other.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "rating", 3,
                    "content", "forbidden"
                ))))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/orders")
                .header("Authorization", bearer(admin.accessToken()))
                .param("page", "1")
                .param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.page").value(1));

        mockMvc.perform(get("/api/v1/admin/orders/{orderId}", orderId)
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.order.id").value(orderId));

        mockMvc.perform(patch("/api/v1/admin/orders/{orderId}/status", orderId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("orderStatus", "PREPARING"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderStatus").value("PREPARING"));

        mockMvc.perform(get("/api/v1/admin/reviews")
                .header("Authorization", bearer(admin.accessToken()))
                .param("page", "1")
                .param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.page").value(1));

        mockMvc.perform(get("/api/v1/admin/reviews/{reviewId}", reviewId)
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(reviewId));

        mockMvc.perform(delete("/api/v1/admin/reviews/{reviewId}", reviewId)
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.message").exists());

        mockMvc.perform(post("/api/v1/payments/{paymentId}/refund", paymentId)
                .header("Authorization", bearer(buyer.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.paymentStatus").value("REFUNDED"));

        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.categoryCount").exists())
            .andExpect(jsonPath("$.data.productCount").exists())
            .andExpect(jsonPath("$.data.pendingOrderCount").exists());
    }

    private void signupUser(String email, String name, String phone) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", email,
                    "password", "Password1!",
                    "name", name,
                    "phone", phone
                ))))
            .andExpect(status().is2xxSuccessful());
    }

    private LoginTokens userLogin(String email, String password) throws Exception {
        return parseTokens(mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", password))))
            .andExpect(status().isOk())
            .andReturn());
    }

    private LoginTokens adminLogin(String email, String password) throws Exception {
        return parseTokens(mockMvc.perform(post("/api/v1/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("email", email, "password", password))))
            .andExpect(status().isOk())
            .andReturn());
    }

    private LoginTokens parseTokens(MvcResult result) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return new LoginTokens(
            jsonNode.path("data").path("accessToken").asText(),
            jsonNode.path("data").path("refreshToken").asText()
        );
    }

    private long readLong(MvcResult result, String pointer) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        JsonNode valueNode = jsonNode.at(pointer);
        assertThat(valueNode.isMissingNode()).isFalse();
        return valueNode.asLong();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueSuffix() {
        return Long.toString(System.nanoTime());
    }

    private record LoginTokens(String accessToken, String refreshToken) {}
}
