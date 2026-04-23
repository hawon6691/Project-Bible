package com.projectbible.post.board.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectbible.post.admin.entity.AdminEntity;
import com.projectbible.post.admin.repository.AdminRepository;
import com.projectbible.post.board.dto.BoardDtos.BoardResponseDto;
import com.projectbible.post.board.dto.BoardDtos.UpsertBoardDto;
import com.projectbible.post.board.entity.BoardEntity;
import com.projectbible.post.board.repository.BoardRepository;
import com.projectbible.post.common.api.MessageResponse;
import com.projectbible.post.common.exception.AppException;

@Service
@Transactional
public class BoardQueryService {
    private final BoardRepository boardRepository;
    private final AdminRepository adminRepository;

    public BoardQueryService(BoardRepository boardRepository, AdminRepository adminRepository) {
        this.boardRepository = boardRepository;
        this.adminRepository = adminRepository;
    }

    @Transactional(readOnly = true)
    public List<BoardResponseDto> list(String status) {
        List<BoardEntity> boards = status == null || status.isBlank()
            ? boardRepository.findAllVisible()
            : boardRepository.findByStatus(status.trim().toUpperCase());
        return boards.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BoardResponseDto one(long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new AppException("BOARD_NOT_FOUND", "Board not found", HttpStatus.NOT_FOUND));
        return toResponse(board);
    }

    public BoardResponseDto create(UpsertBoardDto body, long adminId) {
        AdminEntity admin = adminRepository.findById(adminId)
            .orElseThrow(() -> new AppException("ADMIN_NOT_FOUND", "Admin not found", HttpStatus.NOT_FOUND));
        BoardEntity board = new BoardEntity(admin, body.name().trim(), trimToNull(body.description()), body.displayOrder() == null ? 0 : body.displayOrder());
        boardRepository.save(board);
        return toResponse(board);
    }

    public BoardResponseDto update(long boardId, UpsertBoardDto body) {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new AppException("BOARD_NOT_FOUND", "Board not found", HttpStatus.NOT_FOUND));
        board.update(
            body.name() == null || body.name().isBlank() ? board.getName() : body.name().trim(),
            body.description() == null ? board.getDescription() : trimToNull(body.description()),
            body.displayOrder() == null ? board.getDisplayOrder() : body.displayOrder(),
            body.status() == null || body.status().isBlank() ? board.getStatus() : body.status().trim().toUpperCase()
        );
        boardRepository.save(board);
        return toResponse(board);
    }

    public MessageResponse remove(long boardId) {
        BoardEntity board = boardRepository.findById(boardId)
            .orElseThrow(() -> new AppException("BOARD_NOT_FOUND", "Board not found", HttpStatus.NOT_FOUND));
        board.markDeleted();
        boardRepository.save(board);
        return new MessageResponse("Board deleted successfully");
    }

    private BoardResponseDto toResponse(BoardEntity board) {
        return new BoardResponseDto(
            board.getId(),
            board.getName(),
            board.getDescription(),
            board.getDisplayOrder(),
            board.getStatus(),
            board.getCreatedAt(),
            board.getUpdatedAt()
        );
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
