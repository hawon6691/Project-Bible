package com.projectbible.post.maven.jpa.postgresql.board.controller;

import com.projectbible.post.maven.jpa.postgresql.board.dto.BoardDtos.BoardResponseDto;
import com.projectbible.post.maven.jpa.postgresql.board.service.BoardQueryService;
import com.projectbible.post.maven.jpa.postgresql.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@Tag(name = "boards")
@RestController
@RequestMapping("/api/v1/boards")
public class BoardController {
    private final BoardQueryService service;

    public BoardController(BoardQueryService service) {
        this.service = service;
    }

    @Operation(summary = "List boards")
    @GetMapping
    public ApiResponse<List<BoardResponseDto>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(service.list(status));
    }

    @Operation(summary = "Get board detail")
    @GetMapping("/{boardId}")
    public ApiResponse<BoardResponseDto> one(@PathVariable long boardId) {
        return ApiResponse.success(service.one(boardId));
    }
}
