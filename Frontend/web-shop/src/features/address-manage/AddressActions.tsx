import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createAddress, deleteAddress, updateAddress } from "../../entities/address/api";

export function CreateSampleAddressButton({ onCreated }: { onCreated?: (addressId: number) => void }) {
  const queryClient = useQueryClient();
  const create = useMutation({
    mutationFn: () =>
      createAddress({
        recipientName: "Frontend Buyer",
        phone: "01012345678",
        zipCode: "12345",
        address1: "Seoul simple road",
        address2: "1F",
        isDefault: true,
      }),
    onSuccess: (address) => {
      onCreated?.(address.id);
      queryClient.invalidateQueries({ queryKey: ["addresses"] });
    },
  });

  return (
    <button className="secondary" type="button" onClick={() => create.mutate()}>
      Create sample address
    </button>
  );
}

export function AddressRowActions({ addressId }: { addressId: number }) {
  const queryClient = useQueryClient();
  const reload = () => queryClient.invalidateQueries({ queryKey: ["addresses"] });
  return (
    <>
      <button type="button" onClick={() => updateAddress(addressId, { address2: "Updated" }).then(reload)}>
        Quick update
      </button>
      <button className="secondary" type="button" onClick={() => deleteAddress(addressId).then(reload)}>
        Delete
      </button>
    </>
  );
}
