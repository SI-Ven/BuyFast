package com.example.buyfast.config;

import com.example.buyfast.modules.auth.model.Role;
import com.example.buyfast.modules.auth.repository.RoleRepo;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepo roleRepo;
    private final UuidService uuidService;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
    }

    private void seedRoles() {
        // List of roles to ensure exist
        List<RoleData> rolesToSeed = Arrays.asList(
                new RoleData("buyer", "platform", "Default role for new users"),
                new RoleData("seller", "platform", "Verified seller"),
                new RoleData("admin_company", "company", "Company Owner"),
                new RoleData("seller_company", "company", "Seller belonging to a company")
        );

        for (RoleData data : rolesToSeed) {
            if (roleRepo.findByRoleName(data.name).isEmpty()) {
                Role role = new Role();
                // --- Generates UUID from Spring Boot (Java) ---
                role.setRoleUuid(uuidService.generateUuid());
                role.setRoleName(data.name);
                role.setScope(data.scope);
                role.setDescription(data.description);

                roleRepo.save(role);
                System.out.println("Seeded Role: " + data.name);
            }
        }
    }

    // Simple helper class for the loop
    record RoleData(String name, String scope, String description) {}
}