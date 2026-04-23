import { apiClient, unwrap } from "../../shared/api/client";
import { authHeader } from "../session/storage";
import type { Address } from "./model";

export const fetchAddresses = () => unwrap<Address[]>(apiClient.get("/api/v1/addresses", { headers: authHeader("user") }));
export const createAddress = (payload: Omit<Address, "id">) =>
  unwrap<Address>(apiClient.post("/api/v1/addresses", payload, { headers: authHeader("user") }));
export const updateAddress = (addressId: number, payload: Partial<Omit<Address, "id">>) =>
  unwrap<Address>(apiClient.patch(`/api/v1/addresses/${addressId}`, payload, { headers: authHeader("user") }));
export const deleteAddress = (addressId: number) =>
  unwrap<{ message: string }>(apiClient.delete(`/api/v1/addresses/${addressId}`, { headers: authHeader("user") }));
