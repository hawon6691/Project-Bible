package com.projectbible.shop.maven.jpa.postgresql.admin.application;
import java.util.Map; import org.springframework.stereotype.Service;
@Service public class AdminService { public Map<String,Object> me() { return Map.of("id",1,"email","admin@project-bible.dev","name","Project Bible Admin","role","ADMIN","domain","shop"); } public Map<String,Object> dashboard() { return Map.of("domain","shop","metrics",Map.of("users",3,"admins",1,"items",4,"active",3)); } }
