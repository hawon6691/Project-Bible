import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { fetchAddresses } from "../../entities/address/api";
import { fetchCart } from "../../entities/cart/api";
import { CreateSampleAddressButton } from "../../features/address-manage/AddressActions";
import { CreateOrderForm } from "../../features/checkout-create-order/CreateOrderForm";

export function CheckoutPanel() {
  const cart = useQuery({ queryKey: ["cart", "checkout"], queryFn: fetchCart, retry: false });
  const addresses = useQuery({ queryKey: ["addresses"], queryFn: fetchAddresses, retry: false });
  const [addressId, setAddressId] = useState(1);

  return (
    <>
      <div className="grid">
        <div className="card">
          <h2>Cart item IDs</h2>
          <p>{(cart.data ?? []).map((item) => item.id).join(", ") || "No cart items"}</p>
        </div>
        <div className="card">
          <h2>Addresses</h2>
          {(addresses.data ?? []).map((address) => (
            <p key={address.id}>#{address.id} {address.address1}</p>
          ))}
          <CreateSampleAddressButton onCreated={setAddressId} />
        </div>
      </div>
      <CreateOrderForm cartItems={cart.data ?? []} addressId={addressId} onAddressChange={setAddressId} />
    </>
  );
}
