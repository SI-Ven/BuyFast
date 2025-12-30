package com.example.buyfast.config;

import com.example.buyfast.modules.auth.model.Permission;
import com.example.buyfast.modules.auth.model.Role;
import com.example.buyfast.modules.auth.repository.PermissionRepo;
import com.example.buyfast.modules.auth.repository.RoleRepo;
import com.example.buyfast.modules.product.model.Brand;
import com.example.buyfast.modules.product.repository.BrandRepo;
import com.example.buyfast.util.UuidService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepo roleRepo;
    private final PermissionRepo permissionRepo; // <-- Inject this
    private final UuidService uuidService;
    private final BrandRepo brandRepo;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedPermissions();
        assignPermissionsToRoles();
        seedBrands();
    }

    private void seedRoles() {
        List<RoleData> rolesToSeed = Arrays.asList(
                new RoleData("buyer", "platform", "Default role for new users"),
                new RoleData("seller", "platform", "Verified seller"),
                new RoleData("admin_company", "company", "Company Owner"),
                new RoleData("seller_company", "company", "Seller belonging to a company"),
                new RoleData("super_admin", "platform", "Platform Super Admin")
        );

        for (RoleData data : rolesToSeed) {
            if (roleRepo.findByRoleName(data.name).isEmpty()) {
                Role role = new Role();
                role.setRoleUuid(uuidService.generateUuid());
                role.setRoleName(data.name);
                role.setScope(data.scope);
                role.setDescription(data.description);
                roleRepo.save(role);
                System.out.println("Seeded Role: " + data.name);
            }
        }
    }

    private void seedPermissions() {
        List<PermissionData> perms = Arrays.asList(
                // Admin
                new PermissionData("VIEW_ADMIN_DASHBOARD", "admin", "View platform stats"),
                new PermissionData("APPROVE_VERIFICATION", "verify", "Approve sellers/companies"),
                new PermissionData("MANAGE_USERS", "user", "Manage platform users"),

                // Company / Product
                new PermissionData("MANAGE_COMPANY_SELLERS", "company", "Manage company staff"),
                new PermissionData("CREATE_PRODUCT", "product", "Create new products"),
                new PermissionData("UPDATE_PRODUCT", "product", "Update existing products"),
                new PermissionData("DELETE_PRODUCT", "product", "Delete products")
        );

        for (PermissionData data : perms) {
            if (permissionRepo.findByName(data.name).isEmpty()) {
                Permission p = new Permission();
                p.setPermissionUuid(uuidService.generateUuid());
                p.setPermissionName(data.name);
                p.setResourceGroup(data.group);
                p.setDescription(data.desc);
                permissionRepo.save(p);
                System.out.println("Seeded Permission: " + data.name);
            }
        }
    }

    private void assignPermissionsToRoles() {
        // Define which Role gets which Permissions
        Map<String, List<String>> rolePermissions = Map.of(
                "super_admin", Arrays.asList(
                        "VIEW_ADMIN_DASHBOARD", "APPROVE_VERIFICATION", "MANAGE_USERS",
                        "MANAGE_COMPANY_SELLERS", "CREATE_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT"
                ),
                "admin_company", Arrays.asList(
                        "MANAGE_COMPANY_SELLERS", "CREATE_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT"
                ),
                "seller", Arrays.asList(
                        "CREATE_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT"
                ),
                "seller_company", Arrays.asList(
                        "CREATE_PRODUCT", "UPDATE_PRODUCT", "DELETE_PRODUCT"
                )
        );

        // Loop through map and assign
        rolePermissions.forEach((roleName, permNames) -> {
            Optional<Role> roleOpt = roleRepo.findByRoleName(roleName);
            if (roleOpt.isPresent()) {
                Role role = roleOpt.get();
                for (String permName : permNames) {
                    Optional<Permission> permOpt = permissionRepo.findByName(permName);
                    if (permOpt.isPresent()) {
                        // Link them in the DB
                        roleRepo.addPermissionToRole(role.getId(), permOpt.get().getId());
                    }
                }
                System.out.println("Assigned permissions to: " + roleName);
            }
        });
    }

    private void seedBrands() {
        List<BrandData> brands = Arrays.asList(
                new BrandData("Nike", "Leading sports brand"),
                new BrandData("Adidas", "Sportswear and accessories"),
                new BrandData("Apple", "Premium electronics"),
                new BrandData("Samsung", "Electronics and appliances"),
                new BrandData("Generic", "No specific brand")
        );

        for (BrandData data : brands) {
            // Check if brand exists by name to avoid duplicates
            if (brandRepo.findByName(data.name).isEmpty()) {
                Brand brand = new Brand();
                // Generate UUID using Spring Boot service
                brand.setBrandUuid(uuidService.generateUuid());
                brand.setBrandName(data.name);
                brand.setDescription(data.desc);
                // brand.setLogoUrl("..."); // Optional: Set a default logo URL if you have one

                brandRepo.save(brand);
                System.out.println("Seeded Brand: " + data.name);
            }
        }
    }

    // Helper records
    record RoleData(String name, String scope, String description) {}
    record PermissionData(String name, String group, String desc) {}
    record BrandData(String name, String desc) {}
}