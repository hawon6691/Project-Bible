package com.projectbible.post.gradle.jpa.mysql.user.application;
import java.util.Map; import org.springframework.stereotype.Service;
@Service public class UserService { public Map<String,Object> me() { return Map.of("id",1,"email","user@project-bible.dev","name","Project Bible User","role","USER","domain","post"); } }
