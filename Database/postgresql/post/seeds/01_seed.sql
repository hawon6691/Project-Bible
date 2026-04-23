INSERT INTO admins (id, email, password_hash, name, status) VALUES
  (1, 'admin-post-1@example.com', '$2a$10$14w0lOj3Jm7WF2ZXQ6qvGuYqHaJu7.R3CB/ZOwznKOS77jUSSOvca', 'Post Admin 1', 'ACTIVE'),
  (2, 'admin-post-2@example.com', '$2a$10$14w0lOj3Jm7WF2ZXQ6qvGuYqHaJu7.R3CB/ZOwznKOS77jUSSOvca', 'Post Admin 2', 'ACTIVE');

INSERT INTO users (id, email, password_hash, nickname, status) VALUES
  (1, 'post-user-1@example.com', '$2a$10$oxn/00RSwIT5Opg4SLhpG.p9huJkLMthMGsre/vlkx5GpOT.hy/oe', 'writer-one', 'ACTIVE'),
  (2, 'post-user-2@example.com', '$2a$10$oxn/00RSwIT5Opg4SLhpG.p9huJkLMthMGsre/vlkx5GpOT.hy/oe', 'writer-two', 'ACTIVE'),
  (3, 'post-user-3@example.com', '$2a$10$oxn/00RSwIT5Opg4SLhpG.p9huJkLMthMGsre/vlkx5GpOT.hy/oe', 'reader-three', 'ACTIVE');

INSERT INTO boards (id, created_by_admin_id, name, description, display_order, status) VALUES
  (1, 1, 'free', 'Free board', 1, 'ACTIVE'),
  (2, 1, 'qna', 'Question board', 2, 'ACTIVE'),
  (3, 2, 'notice-like', 'Operation sample board', 3, 'HIDDEN');

INSERT INTO posts (id, board_id, user_id, title, content, view_count, like_count, comment_count, status) VALUES
  (1, 1, 1, 'First free post', 'Sample content for free board.', 12, 2, 2, 'ACTIVE'),
  (2, 2, 2, 'Question post', 'Sample content linked with comments and likes.', 7, 1, 1, 'ACTIVE'),
  (3, 1, 2, 'Hidden sample post', 'Sample content for moderation.', 3, 0, 0, 'HIDDEN');

INSERT INTO comments (id, post_id, user_id, content, status) VALUES
  (1, 1, 2, 'First comment.', 'ACTIVE'),
  (2, 1, 3, 'Second comment.', 'ACTIVE'),
  (3, 2, 1, 'Sample answer comment.', 'ACTIVE');

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
