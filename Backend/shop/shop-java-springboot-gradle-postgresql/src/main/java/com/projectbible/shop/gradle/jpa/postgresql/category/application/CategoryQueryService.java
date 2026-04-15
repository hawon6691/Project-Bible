package com.projectbible.shop.gradle.jpa.postgresql.category.application;
import java.util.List; import java.util.Map; import org.springframework.stereotype.Service;
@Service public class CategoryQueryService { public List<Map<String,Object>> list() { return List.of(Map.of("id",1,"name","books","displayOrder",1,"status","ACTIVE"),Map.of("id",2,"name","goods","displayOrder",2,"status","ACTIVE")); } }
