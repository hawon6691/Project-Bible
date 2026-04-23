export interface PostSummary {
  id: number;
  boardId: number;
  title: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  status: string;
}

export interface PostDetail extends PostSummary {
  content: string;
  author?: { id: number; email: string; nickname: string };
}
