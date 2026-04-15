package com.projectbible.shop.maven.jpa.postgresql.review.application;

import com.projectbible.shop.maven.jpa.postgresql.common.exception.AppException;
import com.projectbible.shop.maven.jpa.postgresql.common.security.CurrentActor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    private final JdbcTemplate jdbcTemplate;

    public ReviewService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> list(long productId, Map<String, String> query) {
        int page = page(query.get("page"));
        int limit = limit(query.get("limit"));
        int offset = (page - 1) * limit;
        Integer totalCount = jdbcTemplate.queryForObject("select count(*) from reviews where product_id = ? and status = 'ACTIVE' and deleted_at is null", Integer.class, productId);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
            """
            select r.id, r.order_item_id as "orderItemId", r.product_id as "productId", r.user_id as "userId", r.rating, r.content, r.status,
                   r.created_at as "createdAt", json_build_object('id', u.id, 'name', u.name) as author
            from reviews r
            join users u on u.id = r.user_id
            where r.product_id = ? and r.status = 'ACTIVE' and r.deleted_at is null
            order by """ + sort(query.get("sort")) + """
            limit ? offset ?
            """,
            productId, limit, offset
        );
        return Map.of("items", items, "meta", meta(page, limit, totalCount == null ? 0 : totalCount));
    }

    public Map<String, Object> one(long reviewId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            """
            select r.id, r.order_item_id as "orderItemId", r.product_id as "productId", r.user_id as "userId", r.rating, r.content, r.status,
                   r.created_at as "createdAt", r.updated_at as "updatedAt", json_build_object('id', u.id, 'name', u.name) as author
            from reviews r
            join users u on u.id = r.user_id
            where r.id = ?
            """,
            reviewId
        );
        if (rows.isEmpty()) {
            throw new AppException("REVIEW_NOT_FOUND", "Review not found", HttpStatus.NOT_FOUND);
        }
        return rows.get(0);
    }

    public Map<String, Object> create(CurrentActor actor, long orderItemId, Map<String, Object> body) {
        requireUser(actor);
        List<Map<String, Object>> orderItemRows = jdbcTemplate.queryForList(
            """
            select oi.id, oi.product_id as "productId", o.user_id as "userId"
            from order_items oi
            join orders o on o.id = oi.order_id
            where oi.id = ?
            """,
            orderItemId
        );
        if (orderItemRows.isEmpty()) {
            throw new AppException("ORDER_NOT_FOUND", "Order item not found", HttpStatus.NOT_FOUND);
        }
        Map<String, Object> orderItem = orderItemRows.get(0);
        if (((Number) orderItem.get("userId")).longValue() != actor.id()) {
            throw new AppException("FORBIDDEN", "You cannot review this order item", HttpStatus.FORBIDDEN);
        }
        Integer duplicate = jdbcTemplate.queryForObject("select count(*) from reviews where order_item_id = ?", Integer.class, orderItemId);
        if (duplicate != null && duplicate > 0) {
            throw new AppException("REVIEW_ALREADY_EXISTS", "Review already exists", HttpStatus.CONFLICT);
        }
        return jdbcTemplate.queryForMap(
            """
            insert into reviews (order_item_id, product_id, user_id, rating, content, status)
            values (?, ?, ?, ?, ?, 'ACTIVE')
            returning id, order_item_id as "orderItemId", product_id as "productId", user_id as "userId", rating, content, status, created_at as "createdAt"
            """,
            orderItemId, orderItem.get("productId"), actor.id(), number(body.get("rating"), "rating"), body.get("content") == null ? null : String.valueOf(body.get("content")).trim()
        );
    }

    public Map<String, Object> update(CurrentActor actor, long reviewId, Map<String, Object> body) {
        requireUser(actor);
        Map<String, Object> current = one(reviewId);
        if (((Number) current.get("userId")).longValue() != actor.id()) {
            throw new AppException("FORBIDDEN", "Only the author can update this review", HttpStatus.FORBIDDEN);
        }
        return jdbcTemplate.queryForMap(
            """
            update reviews
            set rating = ?, content = ?, updated_at = now()
            where id = ?
            returning id, order_item_id as "orderItemId", product_id as "productId", user_id as "userId", rating, content, status, updated_at as "updatedAt"
            """,
            body.containsKey("rating") ? number(body.get("rating"), "rating") : ((Number) current.get("rating")).intValue(),
            body.containsKey("content") ? (body.get("content") == null ? null : String.valueOf(body.get("content")).trim()) : current.get("content"),
            reviewId
        );
    }

    public Map<String, Object> remove(CurrentActor actor, long reviewId) {
        requireUser(actor);
        Map<String, Object> current = one(reviewId);
        if (((Number) current.get("userId")).longValue() != actor.id()) {
            throw new AppException("FORBIDDEN", "Only the author can delete this review", HttpStatus.FORBIDDEN);
        }
        jdbcTemplate.update("update reviews set status = 'DELETED', deleted_at = now(), updated_at = now() where id = ?", reviewId);
        return Map.of("message", "Review deleted successfully");
    }

    public Map<String, Object> adminList(Map<String, String> query) {
        int page = page(query.get("page"));
        int limit = limit(query.get("limit"));
        int offset = (page - 1) * limit;
        List<Object> params = new ArrayList<>();
        List<String> filters = new ArrayList<>(List.of("1=1"));
        if (query.get("search") != null && !query.get("search").isBlank()) {
            String pattern = "%" + query.get("search").trim() + "%";
            params.add(pattern);
            params.add(pattern);
            filters.add("(coalesce(r.content,'') ilike ? or u.name ilike ?)");
        }
        if (query.get("status") != null && !query.get("status").isBlank()) {
            params.add(query.get("status").trim().toUpperCase());
            filters.add("r.status = ?");
        }
        String where = " where " + String.join(" and ", filters);
        Integer totalCount = jdbcTemplate.queryForObject("select count(*) from reviews r join users u on u.id = r.user_id" + where, Integer.class, params.toArray());
        List<Object> listParams = new ArrayList<>(params);
        listParams.add(limit);
        listParams.add(offset);
        List<Map<String, Object>> items = jdbcTemplate.queryForList(
            """
            select r.id, r.product_id as "productId", r.rating, r.status, r.created_at as "createdAt",
                   json_build_object('id', u.id, 'name', u.name) as author
            from reviews r
            join users u on u.id = r.user_id
            """ + where + """
            order by r.created_at desc
            limit ? offset ?
            """,
            listParams.toArray()
        );
        return Map.of("items", items, "meta", meta(page, limit, totalCount == null ? 0 : totalCount));
    }

    public Map<String, Object> adminRemove(long reviewId) {
        one(reviewId);
        jdbcTemplate.update("update reviews set status = 'DELETED', deleted_at = now(), updated_at = now() where id = ?", reviewId);
        return Map.of("message", "Review deleted successfully");
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }

    private int page(String value) { return value == null || value.isBlank() ? 1 : Math.max(Integer.parseInt(value), 1); }
    private int limit(String value) { return value == null || value.isBlank() ? 20 : Math.max(Integer.parseInt(value), 1); }
    private int number(Object value, String key) {
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (Exception ex) { throw new AppException("VALIDATION_ERROR", key + " must be a number", HttpStatus.BAD_REQUEST); }
    }
    private Map<String, Object> meta(int page, int limit, int totalCount) {
        int totalPages = (int) Math.ceil(totalCount / (double) limit);
        return Map.of("page", page, "limit", limit, "totalCount", totalCount, "totalPages", totalPages);
    }
    private String sort(String value) {
        if (value == null) return "r.created_at desc, r.id desc";
        return switch (value.toLowerCase()) {
            case "rating_desc" -> "r.rating desc, r.id desc";
            case "rating_asc" -> "r.rating asc, r.id desc";
            default -> "r.created_at desc, r.id desc";
        };
    }
}
