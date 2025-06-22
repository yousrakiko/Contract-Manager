package contract.management.bea.CONTRACT_MANAGER.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import contract.management.bea.CONTRACT_MANAGER.dto.SimpleActionDto;
import contract.management.bea.CONTRACT_MANAGER.model.Contracts;
import contract.management.bea.CONTRACT_MANAGER.repository.CommentRepository;
import contract.management.bea.CONTRACT_MANAGER.repository.ContractsRepository;
import contract.management.bea.CONTRACT_MANAGER.service.ActionLogService;
import contract.management.bea.CONTRACT_MANAGER.service.ContractsService;
@RestController
public class AnalyseController {
    @Autowired
    private ContractsRepository contractsRepository;
    @Autowired
    private CommentRepository commentRepository;
     @Autowired
    private ContractsService contractsService;
    @Autowired
    private ActionLogService actionLogService;

    


    @GetMapping("/analytics/financial-summary")
    public Map<String, Object> getFinancialSummary() {
        Map<String, Object> response = new HashMap<>();

        // Fetch contracts with non-null guarantee
        List<Contracts> all = contractsRepository.findContractsWithGuarantee();

        // Average budget
        double avgBudget = all.stream()
            .filter(c -> c.getBudgetAmount() != null)
            .mapToDouble(Contracts::getBudgetAmount)
            .average().orElse(0.0);

        // Average guarantee rate
        double avgGuaranteeRate = all.stream()
            .filter(c -> c.getBudgetAmount() != null && c.getBudgetAmount() > 0 && c.getGuaranteeAmount() != null)
            .mapToDouble(c -> c.getGuaranteeAmount() / c.getBudgetAmount())
            .average().orElse(0.0);

        // Top 5 contracts by guarantee rate
        List<Map<String, Object>> topGuarantees = contractsRepository
            .findTopByGuaranteeRate(PageRequest.of(0, 5))
            .stream()
            .map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("title", c.getContractTitle());
                map.put("contractor", c.getContractorName());
                map.put("guaranteeAmount", c.getGuaranteeAmount());
                map.put("budgetAmount", c.getBudgetAmount());
                map.put("rate", c.getGuaranteeAmount() / c.getBudgetAmount());
                return map;
            }).collect(Collectors.toList());

        response.put("averageBudget", avgBudget);
        response.put("averageGuaranteeRate", avgGuaranteeRate);
        response.put("topGuarantees", topGuarantees);

        return response;
    }

    @GetMapping("/analytics/contracts-per-month")
public Map<String, Object> getContractsPerMonth() {
    Map<String, Object> result = new HashMap<>();

    // Signed contracts per month
    List<Object[]> signed = contractsRepository.countApprovedContractsPerMonth();
    Map<Integer, Long> signedMap = signed.stream()
        .collect(Collectors.toMap(
            row -> (Integer) row[0],
            row -> (Long) row[1]
        ));

    // Canceled contracts per month
    List<Object[]> canceled = contractsRepository.countDeclinedContractsPerMonth();
    Map<Integer, Long> canceledMap = canceled.stream()
        .collect(Collectors.toMap(
            row -> (Integer) row[0],
            row -> (Long) row[1]
        ));

    result.put("signed", signedMap);
    result.put("canceled", canceledMap);
    return result;
}
@GetMapping("/analytics/contract-type-distribution")
public Map<String, Long> getContractTypeDistribution() {
    List<Object[]> results = contractsRepository.getContractTypeCounts();

   
    for (Object[] row : results) {
        System.out.println("Type: " + row[0] + " | Count: " + row[1]);
    }

    return results.stream()
        .filter(row -> row[0] != null)
        .collect(Collectors.toMap(
            row -> row[0].toString(),
            row -> (Long) row[1]
        ));
}

@GetMapping("/analytics/status-trend")
public Map<String, Map<Integer, Long>> getStatusTrend() {
    List<Contracts> all = contractsRepository.findAll();
    Map<String, Map<Integer, Long>> trendMap = new HashMap<>();

    for (Contracts c : all) {
      
        if (c.getSignedAt() != null) {
            Integer month = c.getSignedAt().toLocalDateTime().getMonthValue();
            trendMap
                .computeIfAbsent("signed", k -> new HashMap<>())
                .merge(month, 1L, Long::sum);
        }
      
        if (c.getCancelledAt() != null) {
            Integer month = c.getCancelledAt().toLocalDateTime().getMonthValue();
            trendMap
                .computeIfAbsent("rejected", k -> new HashMap<>())
                .merge(month, 1L, Long::sum);
        }
        
        if (c.getCreatedAt() != null) {
            Integer month = c.getCreatedAt().toLocalDateTime().getMonthValue();
            trendMap
                .computeIfAbsent("pending", k -> new HashMap<>())
                .merge(month, 1L, Long::sum);
        }
    }

    return trendMap;
}



 @GetMapping("/analytics/guarantee-vs-budget")
    public Map<String, Object> getGuaranteeVsBudget(@RequestParam List<Integer> months) {
        Map<String, Object> response = new HashMap<>();

        int currentYear = LocalDate.now().getYear();
        int minMonth = Collections.min(months);
        int maxMonth = Collections.max(months);

        List<Contracts> contracts = contractsRepository.findValidContractsForMonthRange(minMonth, maxMonth, currentYear);

       Map<Integer, List<Contracts>> groupedByMonth = contracts.stream()
    .collect(Collectors.groupingBy(c -> (Integer) c.getSignedAt().toLocalDateTime().getMonthValue()));


         List<String> labels = new ArrayList<>();
    List<Double> budgetData = new ArrayList<>();
    List<Double> guaranteeData = new ArrayList<>();

      

for (Contracts c : contracts) {
    if (c.getBudgetAmount() == null || c.getGuaranteeAmount() == null) continue;

    Timestamp signedAt = c.getSignedAt();

String monthAbbr = "";
if (signedAt != null) {
    LocalDateTime signedDateTime = signedAt.toLocalDateTime();
    monthAbbr = signedDateTime.getMonth()
                             .getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
}

labels.add(c.getContractTitle());

    budgetData.add(c.getBudgetAmount());
    guaranteeData.add(c.getGuaranteeAmount());
}

        response.put("labels", labels);
        response.put("budgetData", budgetData);
        response.put("guaranteeData", guaranteeData);
        

        return response;
    }
@GetMapping("/analytics/expiring-contracts")
public List<Map<String, Object>> getExpiringContracts() {
    LocalDate dateLimit = LocalDate.now().plusDays(30);
    List<Contracts> expiringContracts = contractsRepository.findContractsExpiringSoon(dateLimit);

    return expiringContracts.stream()
    .map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getContractId());
            map.put("title", c.getContractTitle());
            map.put("contractor", c.getContractorName());

            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), c.getEndDate());

            map.put("daysLeft", daysLeft);

            return map;
        })
        .collect(Collectors.toList());
}

@GetMapping("/api/analytics/awaiting-modification")
public ResponseEntity<Long> getAwaitingModificationCount() {
    long count = commentRepository.countContractsWithComments();
    return ResponseEntity.ok(count);
}
@GetMapping("/api/analytics/waiting-review")
public ResponseEntity<Long> getWaitingReviewCount() {
    long count = contractsRepository.countWaitingReviewContracts();
    return ResponseEntity.ok(count);
}
@GetMapping("/api/analytics/recent-signed")
public ResponseEntity<Long> getRecentlySignedContracts() {
   LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);
    long count = contractsRepository.countRecentlySignedContracts(twoMonthsAgo);
    return ResponseEntity.ok(count);
}
    @GetMapping("/api/analytics/performance-snapshot")
    public ResponseEntity<Map<String, Long>> getPerformanceSnapshot() {
        return ResponseEntity.ok(contractsService.getPerformanceSnapshot());
    }

@GetMapping("/api/analytics/signature-delay")
public ResponseEntity<List<Map<String, Object>>> getAvgSignatureDelay() {
    LocalDateTime oneYearAgo = LocalDate.now().minusMonths(11).withDayOfMonth(1).atStartOfDay();
    List<Contracts> signedContracts = contractsRepository.findSignedContractsSince(oneYearAgo);

    Map<YearMonth, List<Long>> delayMap = new TreeMap<>();

    for (Contracts contract : signedContracts) {
        LocalDate createdAt = contract.getCreatedAt().toLocalDateTime().toLocalDate();
        LocalDate signedAt = contract.getSignedAt().toLocalDateTime().toLocalDate();

        if (createdAt != null && signedAt != null) {
            long delay = ChronoUnit.DAYS.between(createdAt, signedAt);
            YearMonth month = YearMonth.from(signedAt);
            delayMap.computeIfAbsent(month, k -> new ArrayList<>()).add(delay);
        }
    }

    YearMonth current = YearMonth.now().minusMonths(11);
    for (int i = 0; i < 12; i++) {
        delayMap.putIfAbsent(current, new ArrayList<>());
        current = current.plusMonths(1);
    }

    List<Map<String, Object>> result = new ArrayList<>();
    for (Map.Entry<YearMonth, List<Long>> entry : delayMap.entrySet()) {
        String month = entry.getKey().format(DateTimeFormatter.ofPattern("MMM yyyy"));
        List<Long> delays = entry.getValue();
        double average = delays.isEmpty() ? 0 : delays.stream().mapToLong(d -> d).average().orElse(0);

        Map<String, Object> dataPoint = new HashMap<>();
        dataPoint.put("month", month);
        dataPoint.put("avg_delay", Math.round(average * 10.0) / 10.0);
        result.add(dataPoint);
    }

    return ResponseEntity.ok(result);
}
   @GetMapping("/api/analytics/latest-actions")
   public ResponseEntity<List<SimpleActionDto>> getLatestSimpleActions() {
        List<SimpleActionDto> actions = actionLogService.getLatestSimpleActions();
        return ResponseEntity.ok(actions);
    }

}



    

