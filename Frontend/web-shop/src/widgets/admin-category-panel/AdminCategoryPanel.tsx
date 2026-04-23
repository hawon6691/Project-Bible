import { useQuery } from "@tanstack/react-query";
import { fetchCategories } from "../../entities/category/api";
import { AdminCategoryManager } from "../../features/admin-category-manage/AdminCategoryManager";

export function AdminCategoryPanel() {
  const categories = useQuery({ queryKey: ["categories", "admin"], queryFn: fetchCategories });
  return <AdminCategoryManager categories={categories.data ?? []} />;
}
