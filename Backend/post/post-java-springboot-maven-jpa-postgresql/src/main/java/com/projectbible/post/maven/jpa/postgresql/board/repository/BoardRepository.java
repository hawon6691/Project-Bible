package com.projectbible.post.maven.jpa.postgresql.board.repository;

import com.projectbible.post.maven.jpa.postgresql.board.entity.BoardEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class BoardRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public List<BoardEntity> findAllVisible() {
        return entityManager.createQuery(
                "select b from BoardEntity b where b.status <> 'DELETED' order by b.displayOrder asc, b.id asc",
                BoardEntity.class
            )
            .getResultList();
    }

    public List<BoardEntity> findByStatus(String status) {
        return entityManager.createQuery(
                "select b from BoardEntity b where b.status = :status order by b.displayOrder asc, b.id asc",
                BoardEntity.class
            )
            .setParameter("status", status)
            .getResultList();
    }

    public Optional<BoardEntity> findById(Long boardId) {
        return Optional.ofNullable(entityManager.find(BoardEntity.class, boardId));
    }

    public Optional<BoardEntity> findActiveById(Long boardId) {
        List<BoardEntity> rows = entityManager.createQuery(
                "select b from BoardEntity b where b.id = :boardId and b.status = 'ACTIVE'",
                BoardEntity.class
            )
            .setParameter("boardId", boardId)
            .getResultList();
        return rows.stream().findFirst();
    }

    public BoardEntity save(BoardEntity board) {
        if (board.getId() == null) {
            entityManager.persist(board);
            return board;
        }
        return entityManager.merge(board);
    }
}
