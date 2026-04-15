package com.projectbible.shop.maven.jpa.postgresql.address.application;

import com.projectbible.shop.maven.jpa.postgresql.common.exception.AppException;
import com.projectbible.shop.maven.jpa.postgresql.common.security.CurrentActor;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    private final JdbcTemplate jdbcTemplate;

    public AddressService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> list(CurrentActor actor) {
        requireUser(actor);
        return jdbcTemplate.queryForList(
            """
            select id, recipient_name as "recipientName", phone, zip_code as "zipCode", address1, address2, is_default as "isDefault",
                   created_at as "createdAt", updated_at as "updatedAt"
            from addresses
            where user_id = ?
            order by is_default desc, id asc
            """,
            actor.id()
        );
    }

    public Map<String, Object> one(CurrentActor actor, long addressId) {
        requireUser(actor);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            """
            select id, recipient_name as "recipientName", phone, zip_code as "zipCode", address1, address2, is_default as "isDefault",
                   created_at as "createdAt", updated_at as "updatedAt"
            from addresses
            where id = ? and user_id = ?
            """,
            addressId, actor.id()
        );
        if (rows.isEmpty()) {
            throw new AppException("ADDRESS_NOT_FOUND", "Address not found", HttpStatus.NOT_FOUND);
        }
        return rows.get(0);
    }

    public Map<String, Object> create(CurrentActor actor, Map<String, Object> body) {
        requireUser(actor);
        boolean isDefault = Boolean.parseBoolean(String.valueOf(body.getOrDefault("isDefault", false)));
        if (isDefault) {
            jdbcTemplate.update("update addresses set is_default = false, updated_at = now() where user_id = ?", actor.id());
        }
        return jdbcTemplate.queryForMap(
            """
            insert into addresses (user_id, recipient_name, phone, zip_code, address1, address2, is_default)
            values (?, ?, ?, ?, ?, ?, ?)
            returning id, recipient_name as "recipientName", phone, zip_code as "zipCode", address1, address2, is_default as "isDefault",
                     created_at as "createdAt", updated_at as "updatedAt"
            """,
            actor.id(),
            required(body, "recipientName"),
            required(body, "phone"),
            required(body, "zipCode"),
            required(body, "address1"),
            body.get("address2") == null ? null : String.valueOf(body.get("address2")).trim(),
            isDefault
        );
    }

    public Map<String, Object> update(CurrentActor actor, long addressId, Map<String, Object> body) {
        Map<String, Object> current = one(actor, addressId);
        boolean isDefault = body.containsKey("isDefault") ? Boolean.parseBoolean(String.valueOf(body.get("isDefault"))) : Boolean.parseBoolean(String.valueOf(current.get("isDefault")));
        if (isDefault) {
            jdbcTemplate.update("update addresses set is_default = false, updated_at = now() where user_id = ?", actor.id());
        }
        return jdbcTemplate.queryForMap(
            """
            update addresses
            set recipient_name = ?, phone = ?, zip_code = ?, address1 = ?, address2 = ?, is_default = ?, updated_at = now()
            where id = ? and user_id = ?
            returning id, recipient_name as "recipientName", phone, zip_code as "zipCode", address1, address2, is_default as "isDefault",
                     created_at as "createdAt", updated_at as "updatedAt"
            """,
            body.get("recipientName") != null && !String.valueOf(body.get("recipientName")).trim().isEmpty() ? String.valueOf(body.get("recipientName")).trim() : String.valueOf(current.get("recipientName")),
            body.get("phone") != null && !String.valueOf(body.get("phone")).trim().isEmpty() ? String.valueOf(body.get("phone")).trim() : String.valueOf(current.get("phone")),
            body.get("zipCode") != null && !String.valueOf(body.get("zipCode")).trim().isEmpty() ? String.valueOf(body.get("zipCode")).trim() : String.valueOf(current.get("zipCode")),
            body.get("address1") != null && !String.valueOf(body.get("address1")).trim().isEmpty() ? String.valueOf(body.get("address1")).trim() : String.valueOf(current.get("address1")),
            body.containsKey("address2") ? (body.get("address2") == null ? null : String.valueOf(body.get("address2")).trim()) : current.get("address2"),
            isDefault,
            addressId,
            actor.id()
        );
    }

    public Map<String, Object> remove(CurrentActor actor, long addressId) {
        one(actor, addressId);
        jdbcTemplate.update("delete from addresses where id = ? and user_id = ?", addressId, actor.id());
        return Map.of("message", "Address deleted successfully");
    }

    private void requireUser(CurrentActor actor) {
        if (!actor.isUser()) {
            throw new AppException("FORBIDDEN", "User access required", HttpStatus.FORBIDDEN);
        }
    }

    private String required(Map<String, Object> body, String key) {
        Object value = body.get(key);
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            throw new AppException("VALIDATION_ERROR", key + " is required", HttpStatus.BAD_REQUEST);
        }
        return String.valueOf(value).trim();
    }
}
