export interface ApiEnvelope<T> { success: boolean; data: T | null; meta?: Record<string, unknown> | null; error?: { code: string; message: string; details?: unknown } | null; }
export interface HealthPayload { status: string; service: string; domain: string; }
export interface BoardSummary { id: number; name: string; description: string; displayOrder: number; status: string; }
export interface PostSummary { id: number; boardId: number; title: string; viewCount: number; likeCount: number; commentCount: number; status: string; }
export interface PostDetail extends PostSummary { content: string; }
export interface CategorySummary { id: number; name: string; displayOrder: number; status: string; }
export interface ProductSummary { id: number; categoryId: number; name: string; price: number; stock: number; status: string; }
export interface ProductDetail extends ProductSummary { description: string; }
