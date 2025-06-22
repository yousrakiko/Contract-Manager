package contract.management.bea.CONTRACT_MANAGER.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import contract.management.bea.CONTRACT_MANAGER.model.Contracts;

@Repository
public interface ContractsRepository extends JpaRepository<Contracts, Integer> {

    long countByStatus(String status);

    @Query("SELECT FUNCTION('MONTH', c.signedAt) AS month, COUNT(c) " +
           "FROM Contracts c " +
           "WHERE c.status = 'signed' AND c.signedAt IS NOT NULL " +
           "GROUP BY FUNCTION('MONTH', c.signedAt)")
    List<Object[]> countApprovedContractsPerMonth();

    @Query("SELECT FUNCTION('MONTH', c.cancelledAt) AS month, COUNT(c) " +
           "FROM Contracts c " +
           "WHERE c.status = 'rejected' AND c.cancelledAt IS NOT NULL " +
           "GROUP BY FUNCTION('MONTH', c.cancelledAt)")
    List<Object[]> countDeclinedContractsPerMonth();

    @Query("SELECT c FROM Contracts c WHERE c.budgetAmount IS NOT NULL")
    List<Contracts> findContractsWithBudget();

    @Query("SELECT c FROM Contracts c WHERE c.budgetAmount IS NOT NULL AND c.guaranteeAmount IS NOT NULL")
    List<Contracts> findContractsWithGuarantee();

    @Query("SELECT c FROM Contracts c WHERE c.budgetAmount IS NOT NULL AND c.guaranteeAmount IS NOT NULL ORDER BY (c.guaranteeAmount / c.budgetAmount) DESC")
    List<Contracts> findTopByGuaranteeRate(org.springframework.data.domain.Pageable pageable);
     
    @Query("SELECT FUNCTION('MONTH', c.createdAt), SUM(c.budgetAmount), SUM(c.guaranteeAmount) " +
       "FROM Contracts c " +
       "WHERE c.budgetAmount IS NOT NULL AND c.guaranteeAmount IS NOT NULL " +
       "GROUP BY FUNCTION('MONTH', c.createdAt)")
    List<Object[]> getMonthlyBudgetAndGuarantee();

    @Query("SELECT c.type, COUNT(c) FROM Contracts c GROUP BY c.type")
List<Object[]> getContractTypeCounts();

@Query("SELECT FUNCTION('MONTH', c.createdAt), c.status, COUNT(c) " +
       "FROM Contracts c " +
       "GROUP BY FUNCTION('MONTH', c.createdAt), c.status")
List<Object[]> getMonthlyStatusTrends();

@Query("SELECT c FROM Contracts c WHERE c.status = 'active' AND c.endDate IS NOT NULL AND c.endDate <= :dateLimit")
List<Contracts> findContractsExpiringSoon(@Param("dateLimit") LocalDate dateLimit);

@Query("SELECT c.status, COUNT(c) FROM Contracts c GROUP BY c.status")
List<Object[]> getStatusCounts();

@Query("SELECT AVG(DATEDIFF(c.signedAt, c.createdAt)) FROM Contracts c WHERE c.signedAt IS NOT NULL AND c.createdAt IS NOT NULL")
Double getAverageSignatureDelay();

@Query("SELECT c FROM Contracts c WHERE c.status = 'pending' AND DATEDIFF(CURRENT_DATE, c.createdAt) > 14")
List<Contracts> getStalePendingContracts();
List<Contracts> findByApprovalStageNot(String stage);

List<Contracts> findByApprovalStageNotAndStatusNotAndEndDateAfter(
    String approvalStage, String status, LocalDate date
);

    @Query("SELECT c FROM Contracts c " +
           "WHERE c.status IN ('signed', 'approved', 'active') " +
           "AND c.signedAt IS NOT NULL " +
           "AND c.endDate IS NOT NULL " +
           "AND FUNCTION('MONTH', c.signedAt) <= :month " +
           "AND FUNCTION('YEAR', c.signedAt) = :year " +
           "AND FUNCTION('MONTH', c.endDate) >= :month " +
           "AND FUNCTION('YEAR', c.endDate) = :year " +
           "ORDER BY c.signedAt DESC")
    List<Contracts> findValidContractsForMonth(@Param("month") int month, @Param("year") int year);

    @Query("SELECT c FROM Contracts c " +
           "WHERE c.status IN ('signed', 'approved', 'active') " +
           "AND c.signedAt IS NOT NULL " +
           "AND c.endDate IS NOT NULL " +
           "AND FUNCTION('MONTH', c.signedAt) <= :maxMonth " +
           "AND FUNCTION('YEAR', c.signedAt) = :year " +
           "AND FUNCTION('MONTH', c.endDate) >= :minMonth " +
           "AND FUNCTION('YEAR', c.endDate) = :year " +
           "ORDER BY c.signedAt ASC")
    List<Contracts> findValidContractsForMonthRange(
        @Param("minMonth") int minMonth,
        @Param("maxMonth") int maxMonth,
        @Param("year") int year);

 @Query("SELECT c FROM Contracts c WHERE c.status = 'expired' OR (c.endDate IS NOT NULL AND c.endDate < :dateLimit)")
       List<Contracts> findExpiringContracts(@Param("dateLimit") LocalDate dateLimit);
@Query("SELECT COUNT(c) FROM Contracts c " +
       "WHERE (LOWER(c.status) = 'pending' AND " +
       "       c.contractId NOT IN (SELECT cm.contract.contractId FROM Comment cm)) " +
       "   OR LOWER(c.status) = 'modified'")
long countWaitingReviewContracts();

@Query("SELECT COUNT(c) FROM Contracts c " +
       "WHERE c.status IN ('signed','active','approved') " +
       "AND c.signedAt IS NOT NULL " +
       "AND c.signedAt >= :dateLimit")
long countRecentlySignedContracts(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT COUNT(c) FROM Contracts c WHERE c.createdAt >= :fiveMonthsAgo")
    long countCreatedInLast5Months(@Param("fiveMonthsAgo") LocalDateTime fiveMonthsAgo);

    @Query("SELECT COUNT(c) FROM Contracts c WHERE c.signedAt >= :fiveMonthsAgo")
    long countSignedInLast5Months(@Param("fiveMonthsAgo") LocalDateTime fiveMonthsAgo);

    @Query("SELECT COUNT(c) FROM Contracts c WHERE c.startsAt >= :fiveMonthsAgo")
    long countActiveInLast5Months(@Param("fiveMonthsAgo") LocalDateTime fiveMonthsAgo);

@Query("SELECT c FROM Contracts c WHERE c.signedAt IS NOT NULL AND c.signedAt >= :startDate")
List<Contracts> findSignedContractsSince(@Param("startDate") LocalDateTime startDate);

List<Contracts> findByStatus(String status);

}              









