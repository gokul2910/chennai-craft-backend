package chennai_craft_backend.dto;

import chennai_craft_backend.model.OrderItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull
    private OrderItemStatus status;
}