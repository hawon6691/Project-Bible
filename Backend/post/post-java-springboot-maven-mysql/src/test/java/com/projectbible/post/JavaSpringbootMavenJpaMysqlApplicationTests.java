package com.projectbible.post;

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
class JavaSpringbootMavenJpaMysqlApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void healthSmoke() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.domain").value("post"));
    }

    @Test
    void authFlowAndAuthorizationBoundary() throws Exception {
        String suffix = uniqueSuffix();
        String email = "post-auth-" + suffix + "@example.com";
        String nickname = "post-auth-" + suffix;

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", email,
                    "password", "Password1!",
                    "nickname", nickname
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value(email));

        LoginTokens userLogin = userLogin(email, "Password1!");
        assertThat(userLogin.accessToken()).isNotBlank();
        assertThat(userLogin.refreshToken()).isNotBlank();

        mockMvc.perform(get("/api/v1/users/me").header("Authorization", bearer(userLogin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value(email))
            .andExpect(jsonPath("$.data.nickname").value(nickname));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", userLogin.refreshToken()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", bearer(userLogin.accessToken())))
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

        LoginTokens adminLogin = adminLogin("admin-post-1@example.com", "AdminPassword1!");
        mockMvc.perform(get("/api/v1/admin/me").header("Authorization", bearer(adminLogin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("admin-post-1@example.com"));

        mockMvc.perform(get("/api/v1/admin/me").header("Authorization", bearer(userLogin.accessToken())))
            .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("refreshToken", adminLogin.refreshToken()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty());

        mockMvc.perform(post("/api/v1/admin/auth/logout")
                .header("Authorization", bearer(adminLogin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void boardPostCommentLikeAndAdminModerationFlow() throws Exception {
        String suffix = uniqueSuffix();
        LoginTokens admin = adminLogin("admin-post-1@example.com", "AdminPassword1!");

        long boardId = readLong(mockMvc.perform(post("/api/v1/admin/boards")
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "board-" + suffix,
                    "description", "board-" + suffix,
                    "displayOrder", 99,
                    "status", "ACTIVE"
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.name").value("board-" + suffix))
            .andReturn(), "$.data.id");

        mockMvc.perform(patch("/api/v1/admin/boards/{boardId}", boardId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "name", "board-updated-" + suffix,
                    "description", "board-updated-" + suffix,
                    "displayOrder", 100,
                    "status", "ACTIVE"
                ))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("board-updated-" + suffix));

        mockMvc.perform(get("/api/v1/boards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/v1/boards/{boardId}", boardId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(boardId));

        String authorEmail = "post-author-" + suffix + "@example.com";
        String reviewerEmail = "post-reviewer-" + suffix + "@example.com";
        signupUser(authorEmail, "post-author-" + suffix);
        signupUser(reviewerEmail, "post-reviewer-" + suffix);
        LoginTokens author = userLogin(authorEmail, "Password1!");
        LoginTokens reviewer = userLogin(reviewerEmail, "Password1!");

        long postId = readLong(mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", bearer(author.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "boardId", boardId,
                    "title", "post-" + suffix,
                    "content", "content-" + suffix
                ))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.title").value("post-" + suffix))
            .andReturn(), "$.data.id");

        mockMvc.perform(get("/api/v1/posts").param("page", "1").param("limit", "10").param("search", "post-" + suffix))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.page").value(1))
            .andExpect(jsonPath("$.meta.limit").value(10))
            .andExpect(jsonPath("$.data[0].title").value("post-" + suffix))
            .andExpect(jsonPath("$.data[0].viewCount").exists())
            .andExpect(jsonPath("$.data[0].likeCount").exists())
            .andExpect(jsonPath("$.data[0].commentCount").exists());

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(postId))
            .andExpect(jsonPath("$.data.author.nickname").value("post-author-" + suffix))
            .andExpect(jsonPath("$.data.viewCount").exists())
            .andExpect(jsonPath("$.data.likeCount").exists())
            .andExpect(jsonPath("$.data.commentCount").exists());

        mockMvc.perform(patch("/api/v1/posts/{postId}", postId)
                .header("Authorization", bearer(author.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "post-updated-" + suffix, "content", "content-updated-" + suffix))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("post-updated-" + suffix));

        mockMvc.perform(patch("/api/v1/posts/{postId}", postId)
                .header("Authorization", bearer(reviewer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("title", "bad-update", "content", "bad-update"))))
            .andExpect(status().isForbidden());

        long disposablePostId = readLong(mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", bearer(author.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "boardId", boardId,
                    "title", "post-delete-" + suffix,
                    "content", "delete-me"
                ))))
            .andExpect(status().is2xxSuccessful())
            .andReturn(), "$.data.id");

        mockMvc.perform(delete("/api/v1/posts/{postId}", disposablePostId)
                .header("Authorization", bearer(author.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.message").exists());

        long commentId = readLong(mockMvc.perform(post("/api/v1/posts/{postId}/comments", postId)
                .header("Authorization", bearer(reviewer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("content", "comment-" + suffix))))
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.data.content").value("comment-" + suffix))
            .andReturn(), "$.data.id");

        mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId).param("page", "1").param("limit", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.page").value(1))
            .andExpect(jsonPath("$.data[0].id").exists());

        mockMvc.perform(patch("/api/v1/comments/{commentId}", commentId)
                .header("Authorization", bearer(author.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("content", "forbidden-update"))))
            .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/comments/{commentId}", commentId)
                .header("Authorization", bearer(reviewer.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("content", "comment-updated-" + suffix))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").value("comment-updated-" + suffix));

        mockMvc.perform(post("/api/v1/posts/{postId}/likes", postId)
                .header("Authorization", bearer(author.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.liked").value(true));

        mockMvc.perform(post("/api/v1/posts/{postId}/likes", postId)
                .header("Authorization", bearer(author.accessToken())))
            .andExpect(status().isConflict());

        mockMvc.perform(delete("/api/v1/posts/{postId}/likes", postId)
                .header("Authorization", bearer(author.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.liked").value(false));

        mockMvc.perform(get("/api/v1/admin/posts")
                .header("Authorization", bearer(admin.accessToken()))
                .param("page", "1")
                .param("limit", "20")
                .param("search", "post-updated-" + suffix))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.page").value(1))
            .andExpect(jsonPath("$.data[0].id").value(postId));

        mockMvc.perform(get("/api/v1/admin/posts/{postId}", postId)
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(postId));

        mockMvc.perform(patch("/api/v1/admin/posts/{postId}/status", postId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "HIDDEN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("HIDDEN"));

        mockMvc.perform(get("/api/v1/admin/comments")
                .header("Authorization", bearer(admin.accessToken()))
                .param("page", "1")
                .param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.meta.page").value(1));

        mockMvc.perform(get("/api/v1/admin/comments/{commentId}", commentId)
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(commentId));

        mockMvc.perform(patch("/api/v1/admin/comments/{commentId}/status", commentId)
                .header("Authorization", bearer(admin.accessToken()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", "HIDDEN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("HIDDEN"));

        mockMvc.perform(get("/api/v1/admin/dashboard")
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.boardCount").exists())
            .andExpect(jsonPath("$.data.postCount").exists())
            .andExpect(jsonPath("$.data.commentCount").exists());

        mockMvc.perform(delete("/api/v1/admin/boards/{boardId}", boardId)
                .header("Authorization", bearer(admin.accessToken())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.message").exists());
    }

    private void signupUser(String email, String nickname) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "email", email,
                    "password", "Password1!",
                    "nickname", nickname
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

    private long readLong(MvcResult result, String jsonPointer) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String pointer = jsonPointer.replace("$", "").replace(".", "/").replace("[", "/").replace("]", "");
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
