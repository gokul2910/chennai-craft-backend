package chennai_craft_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PlaceOrderRequest {

    @NotBlank
    @Size(max = 500)
    private String address;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit mobile number")
    private String phone;

    @NotBlank
    @Pattern(regexp = "^600\\d{3}$", message = "Delivery is available only in Chennai (pincode 600xxx)")
    private String pincode;

    @NotEmpty
    @Valid
    private List<OrderLineRequest> items;
}