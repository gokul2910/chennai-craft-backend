package chennai_craft_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerApplyRequest {

    @NotBlank
    @Size(max = 100)
    private String shopName;

    @NotBlank
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone must be a valid 10-digit mobile number")
    private String phone;

    @NotBlank
    @Size(max = 100)
    private String area;

    @NotBlank
    @Pattern(regexp = "^600\\d{3}$", message = "Pincode must be a Chennai pincode (600xxx)")
    private String pincode;
}