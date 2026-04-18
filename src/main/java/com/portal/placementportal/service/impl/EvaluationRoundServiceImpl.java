package com.portal.placementportal.service.impl;

import com.portal.placementportal.dto.EvaluationRoundDtos.AuditEntryDto;
import com.portal.placementportal.dto.EvaluationRoundDtos.CompanyRoundProgressResponse;
import com.portal.placementportal.dto.EvaluationRoundDtos.RoundStateDto;
import com.portal.placementportal.dto.EvaluationRoundDtos.StudentRoundProgress;
import com.portal.placementportal.entity.AdminUser;
import com.portal.placementportal.entity.Company;
import com.portal.placementportal.entity.EvaluationRound;
import com.portal.placementportal.entity.EvaluationRoundAudit;
import com.portal.placementportal.entity.EvaluationRoundType;
import com.portal.placementportal.entity.Registration;
import com.portal.placementportal.entity.RoundStatus;
import com.portal.placementportal.entity.RoundStatusValue;
import com.portal.placementportal.entity.Student;
import com.portal.placementportal.exception.CrossCollegeAccessException;
import com.portal.placementportal.exception.ForbiddenOperationException;
import com.portal.placementportal.exception.InvalidRequestException;
import com.portal.placementportal.exception.ResourceNotFoundException;
import com.portal.placementportal.repository.CompanyRepository;
import com.portal.placementportal.repository.EvaluationRoundAuditRepository;
import com.portal.placementportal.repository.EvaluationRoundRepository;
import com.portal.placementportal.repository.RegistrationRepository;
import com.portal.placementportal.repository.StudentRepository;
import com.portal.placementportal.service.AdminUserService;
import com.portal.placementportal.service.EvaluationRoundService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Concurrency model:
 *   - No pessimistic row locks. {@code @Version} on EvaluationRound
 *     provides optimistic concurrency control; the unique composite PK
 *     protects the first-create race at the DB level.
 *   - Write methods do not carry {@code @Transactional} at the bean
 *     boundary. Instead they run the transactional work inside a
 *     {@link TransactionTemplate}, which lets us catch conflict
 *     exceptions after the transaction has rolled back and start a
 *     fresh one — something a single {@code @Transactional} method
 *     cannot do, because its session is poisoned once the flush fails.
 *   - Parsing and admin/company/student look-ups that don't strictly
 *     need the write transaction are kept outside it, so the locked
 *     window (such as it is) stays as narrow as possible.
 */
@Service
@RequiredArgsConstructor
public class EvaluationRoundServiceImpl implements EvaluationRoundService {

    /** Total attempts including the first try. 3 is enough for typical contention. */
    private static final int MAX_WRITE_ATTEMPTS = 3;

    private final EvaluationRoundRepository evaluationRoundRepository;
    private final EvaluationRoundAuditRepository auditRepository;
    private final RegistrationRepository registrationRepository;
    private final CompanyRepository companyRepository;
    private final StudentRepository studentRepository;
    private final AdminUserService adminUserService;
    private final PlatformTransactionManager txManager;

    private TransactionTemplate txWrite;

    @PostConstruct
    void initTxTemplates() {
        this.txWrite = new TransactionTemplate(txManager);
    }

    // ---- read ----

    @Override
    @Transactional(readOnly = true)
    public CompanyRoundProgressResponse listForCompany(Long companyId, Long adminCollegeId,
                                                       Pageable pageable) {
        Company company = requireCompanyInCollege(companyId, adminCollegeId);

        Page<Registration> page = registrationRepository
                .findByCompany_CompanyId(companyId, pageable);

        List<Registration> rows = page.getContent();
        if (rows.isEmpty()) {
            return new CompanyRoundProgressResponse(
                    company.getCompanyId(), company.getName(),
                    page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages(), page.isLast(),
                    List.of());
        }

        // Student ids for the current page — the key to batching everything else.
        Set<Long> studentIds = rows.stream()
                .map(r -> r.getStudent().getStudentId())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        // One query for all tracking aggregates on this page, with statuses join-fetched.
        Map<Long, EvaluationRound> trackingByStudent = new HashMap<>(studentIds.size());
        for (EvaluationRound er : evaluationRoundRepository
                .findWithStatusesByCompanyAndStudents(companyId, studentIds)) {
            trackingByStudent.put(er.getStudent().getStudentId(), er);
        }

        // One query for all audit history on this page, pre-sorted by changed_at.
        Map<Long, List<EvaluationRoundAudit>> auditsByStudent = new HashMap<>(studentIds.size());
        for (EvaluationRoundAudit a : auditRepository
                .findByCompany_CompanyIdAndStudent_StudentIdInOrderByChangedAtAsc(
                        companyId, studentIds)) {
            auditsByStudent
                    .computeIfAbsent(a.getStudent().getStudentId(), k -> new ArrayList<>())
                    .add(a);
        }

        // Map the page contents — preallocated list, no per-row DB trips.
        List<StudentRoundProgress> mapped = new ArrayList<>(rows.size());
        for (Registration reg : rows) {
            Student s = reg.getStudent();
            EvaluationRound er = trackingByStudent.get(s.getStudentId());
            List<EvaluationRoundAudit> history = auditsByStudent
                    .getOrDefault(s.getStudentId(), Collections.emptyList());
            mapped.add(toProgress(s, company, er, history));
        }

        return new CompanyRoundProgressResponse(
                company.getCompanyId(), company.getName(),
                page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast(),
                mapped);
    }

    // ---- writes ----

    @Override
    public StudentRoundProgress addRound(Long studentId, Long companyId,
                                         String round, Long adminId) {
        // Parse outside the transaction. Invalid input should never open one.
        EvaluationRoundType target = parseRound(round);
        return withRetry(() -> txWrite.execute(status ->
                applyTransition(studentId, companyId, adminId,
                        (er, now, sink) -> cascadeClear(er, target, now, sink))));
    }

    @Override
    public StudentRoundProgress removeRound(Long studentId, Long companyId,
                                            String round, Long adminId) {
        EvaluationRoundType target = parseRound(round);
        return withRetry(() -> txWrite.execute(status ->
                applyTransition(studentId, companyId, adminId,
                        (er, now, sink) -> cascadeFail(er, target, now, sink))));
    }

    /**
     * Run {@code op} against a fresh transaction; on a transient conflict
     * (stale {@code @Version}, or a lost first-create race), roll back and
     * retry up to {@link #MAX_WRITE_ATTEMPTS} times.
     *
     * The loop must sit outside the transaction: you cannot catch an
     * OptimisticLockingFailureException inside the same transaction it was
     * thrown from — that session is already marked for rollback.
     */
    private <T> T withRetry(Supplier<T> op) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= MAX_WRITE_ATTEMPTS; attempt++) {
            try {
                return op.get();
            } catch (OptimisticLockingFailureException | DataIntegrityViolationException e) {
                last = e;
                // brief fall-through; no sleep — contention on a single aggregate
                // is low-volume and backoff would just add latency.
            }
        }
        throw last;
    }

    /**
     * Runs inside a write transaction. Keeps its body short: load-or-init,
     * apply the state change, write audits, read back history. Scope checks
     * happen here too — they must see the same transaction as the write so
     * a read-write-race admin promotion cannot slip through.
     */
    private StudentRoundProgress applyTransition(Long studentId, Long companyId,
                                                 Long adminId, TransitionOp op) {
        AdminUser admin = adminUserService.getById(adminId);
        if (admin.getCollege() == null) {
            throw new ForbiddenOperationException("Admin is not attached to a college");
        }
        Long adminCollegeId = admin.getCollege().getCollegeId();

        Company company = requireCompanyInCollege(companyId, adminCollegeId);
        Student student = requireStudentInCollege(studentId, adminCollegeId);

        Instant now = Instant.now();
        EvaluationRound er = loadOrInit(student, company, now);

        List<AuditDelta> deltas = new ArrayList<>();
        op.apply(er, now, deltas);

        evaluationRoundRepository.save(er);
        persistAudits(student, company, admin, now, deltas);

        List<EvaluationRoundAudit> history = auditRepository
                .findByStudent_StudentIdAndCompany_CompanyIdOrderByChangedAtAsc(
                        studentId, companyId);
        return toProgress(student, company, er, history);
    }

    // ---- transition primitives ----

    @FunctionalInterface
    private interface TransitionOp {
        void apply(EvaluationRound er, Instant now, List<AuditDelta> auditSink);
    }

    private record AuditDelta(EvaluationRoundType round,
                              RoundStatus oldStatus,
                              RoundStatus newStatus) {}

    /**
     * Mark {@code target} and every earlier round CLEARED. Upstream rounds
     * must have been cleared to reach {@code target}, so those are
     * implied-CLEARED. Later rounds are untouched.
     */
    private void cascadeClear(EvaluationRound er, EvaluationRoundType target,
                              Instant now, List<AuditDelta> sink) {
        EvaluationRoundType[] all = EvaluationRoundType.values();
        int targetIdx = target.ordinal();
        for (int i = 0; i <= targetIdx; i++) {
            EvaluationRoundType t = all[i];
            RoundStatus before = er.statusOf(t);
            if (er.transition(t, RoundStatus.CLEARED, now)) {
                sink.add(new AuditDelta(t, before, RoundStatus.CLEARED));
            }
        }
    }

    /**
     * Mark {@code target} FAILED and reset every later round to PENDING;
     * the student never reached later rounds, so any prior status on them
     * is stale.
     */
    private void cascadeFail(EvaluationRound er, EvaluationRoundType target,
                             Instant now, List<AuditDelta> sink) {
        EvaluationRoundType[] all = EvaluationRoundType.values();
        int targetIdx = target.ordinal();
        // target itself -> FAILED
        {
            RoundStatus before = er.statusOf(target);
            if (er.transition(target, RoundStatus.FAILED, now)) {
                sink.add(new AuditDelta(target, before, RoundStatus.FAILED));
            }
        }
        // strictly later rounds -> PENDING
        for (int i = targetIdx + 1; i < all.length; i++) {
            EvaluationRoundType t = all[i];
            RoundStatus before = er.statusOf(t);
            if (er.transition(t, RoundStatus.PENDING, now)) {
                sink.add(new AuditDelta(t, before, RoundStatus.PENDING));
            }
        }
    }

    // ---- load-or-init ----

    /**
     * Resolve the aggregate for (student, company), creating it on first use.
     * No explicit lock — @Version on updates and the composite unique PK on
     * inserts together cover both concurrency cases, and the enclosing
     * {@link #withRetry} loop turns a lost race into at most one retry.
     */
    private EvaluationRound loadOrInit(Student student, Company company, Instant now) {
        Long sid = student.getStudentId();
        Long cid = company.getCompanyId();

        Optional<EvaluationRound> existing =
                evaluationRoundRepository.findByStudent_StudentIdAndCompany_CompanyId(sid, cid);
        if (existing.isPresent()) {
            return existing.get();
        }

        EvaluationRound fresh = EvaluationRound.builder()
                .student(student)
                .company(company)
                .createdAt(now)
                .build();
        fresh.seedPending(now);
        // A DataIntegrityViolationException from a concurrent insert bubbles
        // out of this transaction; withRetry restarts and the re-read above
        // will find the winning row on the next attempt.
        return evaluationRoundRepository.saveAndFlush(fresh);
    }

    // ---- scope checks ----

    private Company requireCompanyInCollege(Long companyId, Long adminCollegeId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
        if (!company.getCollege().getCollegeId().equals(adminCollegeId)) {
            throw new CrossCollegeAccessException("Company belongs to a different college");
        }
        return company;
    }

    private Student requireStudentInCollege(Long studentId, Long adminCollegeId) {
        Student s = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        if (!s.getCollege().getCollegeId().equals(adminCollegeId)) {
            throw new CrossCollegeAccessException("Student belongs to a different college");
        }
        return s;
    }

    // ---- parsing ----

    private EvaluationRoundType parseRound(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidRequestException("round is required");
        }
        try {
            return EvaluationRoundType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("Invalid round: " + raw
                    + ". Expected one of " + listValidRounds());
        }
    }

    private String listValidRounds() {
        EvaluationRoundType[] vals = EvaluationRoundType.values();
        StringBuilder sb = new StringBuilder(vals.length * 16).append("[");
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(vals[i].name());
        }
        return sb.append("]").toString();
    }

    // ---- audit + DTO mapping ----

    private void persistAudits(Student student, Company company, AdminUser admin,
                               Instant now, List<AuditDelta> deltas) {
        if (deltas.isEmpty()) return;
        List<EvaluationRoundAudit> rows = new ArrayList<>(deltas.size());
        for (AuditDelta d : deltas) {
            rows.add(EvaluationRoundAudit.builder()
                    .student(student)
                    .company(company)
                    .roundType(d.round())
                    .oldStatus(d.oldStatus())
                    .newStatus(d.newStatus())
                    .changedAt(now)
                    .changedBy(admin)
                    .changedByUsername(admin.getUsername())
                    .build());
        }
        auditRepository.saveAll(rows);
    }

    /**
     * Build the progress DTO. Preallocates both collections, iterates over
     * the enum array exactly once, and never triggers a DB query because
     * both {@code er.rounds} (join-fetched or batch-fetched upstream) and
     * {@code history} (already queried) are fully in memory.
     */
    private StudentRoundProgress toProgress(Student student, Company company,
                                            EvaluationRound er,
                                            List<EvaluationRoundAudit> history) {
        EvaluationRoundType[] roundTypes = EvaluationRoundType.values();
        List<RoundStateDto> rounds = new ArrayList<>(roundTypes.length);
        Map<EvaluationRoundType, RoundStatusValue> statuses =
                er == null ? Collections.emptyMap() : er.getRounds();
        for (EvaluationRoundType t : roundTypes) {
            RoundStatusValue v = statuses.get(t);
            rounds.add(v == null
                    ? new RoundStateDto(t, RoundStatus.PENDING, null)
                    : new RoundStateDto(t, v.getStatus(), v.getUpdatedAt()));
        }

        List<AuditEntryDto> historyDtos = new ArrayList<>(history.size());
        for (EvaluationRoundAudit a : history) {
            historyDtos.add(new AuditEntryDto(
                    a.getAuditId(),
                    a.getRoundType(),
                    a.getOldStatus(),
                    a.getNewStatus(),
                    a.getChangedAt(),
                    a.getChangedBy() != null ? a.getChangedBy().getAdminId() : null,
                    a.getChangedByUsername()
            ));
        }

        return new StudentRoundProgress(
                student.getStudentId(),
                student.getUsn(),
                student.getFullName(),
                student.getEmail(),
                company.getCompanyId(),
                company.getName(),
                rounds,
                historyDtos
        );
    }
}
