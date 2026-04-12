package com.projectbible.post.maven.jdbc.mysql.admin.application;
import java.util.Map; import org.springframework.stereotype.Service;
@Service public class AdminService { public Map<String,Object> me() { return Map.of("id",1,"email","admin@project-bible.dev","name","Project Bible Admin","role","ADMIN","domain","post"); } public Map<String,Object> dashboard() { return Map.of("domain","post","metrics",Map.of("users",3,"admins",1,"items",4,"active",3)); } }
