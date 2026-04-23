import { AdminCategoryPanel } from "../../widgets/admin-category-panel/AdminCategoryPanel";
import { PageShell } from "../../widgets/app-shell/PageShell";

export function AdminCategoriesPage() {
  return (
    <PageShell title="Admin Categories" description="카테고리 CRUD입니다.">
      <AdminCategoryPanel />
    </PageShell>
  );
}
