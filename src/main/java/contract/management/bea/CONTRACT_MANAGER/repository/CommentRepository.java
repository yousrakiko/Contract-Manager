package contract.management.bea.CONTRACT_MANAGER.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import contract.management.bea.CONTRACT_MANAGER.model.Comment;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByContract(Contracts contract);
    

    @Query("SELECT COUNT(DISTINCT c.contract.contractId) FROM Comment c")
    long countContractsWithComments();

    @Query("SELECT COUNT(c) > 0 FROM Comment c WHERE c.contract.contractId = :contractId")
    boolean existsByContractId(@Param("contractId") Integer contractId);

    @Query("SELECT c FROM Comment c " +
           "WHERE c.contract.status IN ('pending', 'modified', 'pending with comment') " +
           "AND c.createdAt >= :cutoffDate " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsForUser(@Param("cutoffDate") LocalDateTime cutoffDate);
}
