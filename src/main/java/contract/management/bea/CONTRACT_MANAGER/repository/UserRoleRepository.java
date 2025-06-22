package contract.management.bea.CONTRACT_MANAGER.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import contract.management.bea.CONTRACT_MANAGER.model.UserRole;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    UserRole findByRoleId(int roleId);
    UserRole findByRoleName(String roleName);
}