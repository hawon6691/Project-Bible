package com.projectbible.post.maven.jdbc.postgresql.board.application;
import java.util.List; import java.util.Map; import org.springframework.stereotype.Service;
@Service public class BoardQueryService { public List<Map<String,Object>> list() { return List.of(Map.of("id",1,"name","free","description","Free board","displayOrder",1,"status","ACTIVE"),Map.of("id",2,"name","notice","description","Notice board","displayOrder",2,"status","ACTIVE")); } }
