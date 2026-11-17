package chennai_craft_backend.dto;

import chennai_craft_backend.model.Role;

public record UserResponse(Long id, String name, String email, Role role) {
}