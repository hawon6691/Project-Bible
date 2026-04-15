INSERT INTO admins (id, email, password_hash, name, status) VALUES
  (1, 'admin-post-1@example.com', '$2a$10$14w0lOj3Jm7WF2ZXQ6qvGuYqHaJu7.R3CB/ZOwznKOS77jUSSOvca', 'Post Admin 1', 'ACTIVE'),
  (2, 'admin-post-2@example.com', '$2a$10$14w0lOj3Jm7WF2ZXQ6qvGuYqHaJu7.R3CB/ZOwznKOS77jUSSOvca', 'Post Admin 2', 'ACTIVE');

INSERT INTO users (id, email, password_hash, nickname, status) VALUES
  (1, 'post-user-1@example.com', '$2a$10$oxn/00RSwIT5Opg4SLhpG.p9huJkLMthMGsre/vlkx5GpOT.hy/oe', 'writer-one', 'ACTIVE'),
  (2, 'post-user-2@example.com', '$2a$10$oxn/00RSwIT5Opg4SLhpG.p9huJkLMthMGsre/vlkx5GpOT.hy/oe', 'writer-two', 'ACTIVE'),
  (3, 'post-user-3@example.com', '$2a$10$oxn/00RSwIT5Opg4SLhpG.p9huJkLMthMGsre/vlkx5GpOT.hy/oe', 'reader-three', 'ACTIVE');

INSERT INTO boards (id, created_by_admin_id, name, description, display_order, status) VALUES
  (1, 1, 'free', '자유 게시판', 1, 'ACTIVE'),
  (2, 1, 'qna', '질문 게시판', 2, 'ACTIVE'),
  (3, 2, 'notice-like', '운영 참고 게시판', 3, 'HIDDEN');

INSERT INTO posts (id, board_id, user_id, title, content, view_count, like_count, comment_count, status) VALUES
  (1, 1, 1, '첫 번째 자유글', '자유 게시판 샘플 본문입니다.', 12, 2, 2, 'ACTIVE'),
  (2, 2, 2, '질문 있습니다', 'ERD 기준 댓글과 좋아요가 연결됩니다.', 7, 1, 1, 'ACTIVE'),
  (3, 1, 2, '숨김 처리 대상 예시', '관리자 운영 상태 샘플입니다.', 3, 0, 0, 'HIDDEN');

INSERT INTO comments (id, post_id, user_id, content, status) VALUES
  (1, 1, 2, '첫 댓글입니다.', 'ACTIVE'),
  (2, 1, 3, '두 번째 댓글입니다.', 'ACTIVE'),
  (3, 2, 1, '질문에 대한 답변 예시입니다.', 'ACTIVE');

INSERT INTO post_likes (id, post_id, user_id) VALUES
  (1, 1, 2),
  (2, 1, 3),
  (3, 2, 1);

SELECT setval(pg_get_serial_sequence('admins', 'id'), COALESCE((SELECT MAX(id) FROM admins), 1), true);
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('boards', 'id'), COALESCE((SELECT MAX(id) FROM boards), 1), true);
SELECT setval(pg_get_serial_sequence('posts', 'id'), COALESCE((SELECT MAX(id) FROM posts), 1), true);
SELECT setval(pg_get_serial_sequence('comments', 'id'), COALESCE((SELECT MAX(id) FROM comments), 1), true);
SELECT setval(pg_get_serial_sequence('post_likes', 'id'), COALESCE((SELECT MAX(id) FROM post_likes), 1), true);
