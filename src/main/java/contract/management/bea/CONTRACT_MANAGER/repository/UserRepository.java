package contract.management.bea.CONTRACT_MANAGER.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import contract.management.bea.CONTRACT_MANAGER.model.User;


@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByMatricule(String matricule);
    User findByEmail(String email);

    public boolean existsByEmail(String email);
    boolean existsByMatricule(String matricule);

    List<User> findByRoleRoleName(String roleName);
}











