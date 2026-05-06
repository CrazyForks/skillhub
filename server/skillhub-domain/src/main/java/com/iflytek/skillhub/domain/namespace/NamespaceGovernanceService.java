package com.iflytek.skillhub.domain.namespace;

import com.iflytek.skillhub.domain.audit.AuditLogService;
import com.iflytek.skillhub.domain.shared.exception.DomainBadRequestException;
import com.iflytek.skillhub.domain.shared.exception.DomainForbiddenException;
import com.iflytek.skillhub.domain.skill.SkillRepository;
import com.iflytek.skillhub.domain.skill.SkillStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Applies namespace lifecycle transitions such as freeze, unfreeze, archive,
 * restore, and delete while recording audit history.
 */
@Service
public class NamespaceGovernanceService {

    private final NamespaceRepository namespaceRepository;
    private final NamespaceMemberRepository namespaceMemberRepository;
    private final NamespaceAccessPolicy namespaceAccessPolicy;
    private final AuditLogService auditLogService;
    private final SkillRepository skillRepository;

    public NamespaceGovernanceService(NamespaceRepository namespaceRepository,
                                      NamespaceMemberRepository namespaceMemberRepository,
                                      NamespaceAccessPolicy namespaceAccessPolicy,
                                      AuditLogService auditLogService,
                                      SkillRepository skillRepository) {
        this.namespaceRepository = namespaceRepository;
        this.namespaceMemberRepository = namespaceMemberRepository;
        this.namespaceAccessPolicy = namespaceAccessPolicy;
        this.auditLogService = auditLogService;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public Namespace freezeNamespace(String slug,
                                     String actorUserId,
                                     String reason,
                                     String requestId,
                                     String clientIp,
                                     String userAgent) {
        Namespace namespace = loadNamespaceBySlug(slug);
        NamespaceRole role = requireRole(namespace.getId(), actorUserId);
        if (namespace.getStatus() != NamespaceStatus.ACTIVE) {
            throw new DomainBadRequestException("error.namespace.state.transition.invalid", namespace.getSlug());
        }
        if (!namespaceAccessPolicy.canFreeze(namespace, role)) {
            throw new DomainForbiddenException("error.namespace.lifecycle.forbidden", namespace.getSlug());
        }
        namespace.setStatus(NamespaceStatus.FROZEN);
        Namespace updated = namespaceRepository.save(namespace);
        record("FREEZE_NAMESPACE", actorUserId, updated.getId(), requestId, clientIp, userAgent, reason);
        return updated;
    }

    @Transactional
    public Namespace unfreezeNamespace(String slug,
                                       String actorUserId,
                                       String requestId,
                                       String clientIp,
                                       String userAgent) {
        Namespace namespace = loadNamespaceBySlug(slug);
        NamespaceRole role = requireRole(namespace.getId(), actorUserId);
        if (namespace.getStatus() != NamespaceStatus.FROZEN) {
            throw new DomainBadRequestException("error.namespace.state.transition.invalid", namespace.getSlug());
        }
        if (!namespaceAccessPolicy.canUnfreeze(namespace, role)) {
            throw new DomainForbiddenException("error.namespace.lifecycle.forbidden", namespace.getSlug());
        }
        namespace.setStatus(NamespaceStatus.ACTIVE);
        Namespace updated = namespaceRepository.save(namespace);
        record("UNFREEZE_NAMESPACE", actorUserId, updated.getId(), requestId, clientIp, userAgent, null);
        return updated;
    }

    @Transactional
    public Namespace archiveNamespace(String slug,
                                      String actorUserId,
                                      String reason,
                                      String requestId,
                                      String clientIp,
                                      String userAgent) {
        Namespace namespace = loadNamespaceBySlug(slug);
        NamespaceRole role = requireRole(namespace.getId(), actorUserId);
        if (namespace.getStatus() == NamespaceStatus.ARCHIVED) {
            throw new DomainBadRequestException("error.namespace.state.transition.invalid", namespace.getSlug());
        }
        if (!namespaceAccessPolicy.canArchive(namespace, role)) {
            throw new DomainForbiddenException("error.namespace.lifecycle.forbidden", namespace.getSlug());
        }
        namespace.setStatus(NamespaceStatus.ARCHIVED);
        Namespace updated = namespaceRepository.save(namespace);
        record("ARCHIVE_NAMESPACE", actorUserId, updated.getId(), requestId, clientIp, userAgent, reason);
        return updated;
    }

    @Transactional
    public Namespace restoreNamespace(String slug,
                                      String actorUserId,
                                      String requestId,
                                      String clientIp,
                                      String userAgent) {
        Namespace namespace = loadNamespaceBySlug(slug);
        NamespaceRole role = requireRole(namespace.getId(), actorUserId);
        if (namespace.getStatus() != NamespaceStatus.ARCHIVED) {
            throw new DomainBadRequestException("error.namespace.state.transition.invalid", namespace.getSlug());
        }
        if (!namespaceAccessPolicy.canRestore(namespace, role)) {
            throw new DomainForbiddenException("error.namespace.lifecycle.forbidden", namespace.getSlug());
        }
        namespace.setStatus(NamespaceStatus.ACTIVE);
        Namespace updated = namespaceRepository.save(namespace);
        record("RESTORE_NAMESPACE", actorUserId, updated.getId(), requestId, clientIp, userAgent, null);
        return updated;
    }

    @Transactional
    public void deleteNamespace(String slug,
                                String actorUserId,
                                String reason,
                                String requestId,
                                String clientIp,
                                String userAgent) {
        Namespace namespace = loadNamespaceBySlug(slug);
        NamespaceRole role = requireRole(namespace.getId(), actorUserId);

        if (namespace.getStatus() != NamespaceStatus.ARCHIVED) {
            throw new DomainBadRequestException("error.namespace.delete.mustBeArchived", namespace.getSlug());
        }

        if (!namespaceAccessPolicy.canDelete(namespace, role)) {
            throw new DomainForbiddenException("error.namespace.lifecycle.forbidden", namespace.getSlug());
        }

        // Check if namespace has any active skills
        long activeSkillCount = skillRepository.findByNamespaceIdAndStatus(namespace.getId(), SkillStatus.ACTIVE).size();
        if (activeSkillCount > 0) {
            throw new DomainBadRequestException("error.namespace.delete.hasActiveSkills", namespace.getSlug());
        }

        namespace.setStatus(NamespaceStatus.DELETED);
        namespaceRepository.save(namespace);
        record("DELETE_NAMESPACE", actorUserId, namespace.getId(), requestId, clientIp, userAgent, reason);
    }

    private Namespace loadNamespaceBySlug(String slug) {
        Namespace namespace = namespaceRepository.findBySlug(slug)
                .orElseThrow(() -> new DomainBadRequestException("error.namespace.slug.notFound", slug));
        if (namespaceAccessPolicy.isImmutable(namespace)) {
            throw new DomainBadRequestException("error.namespace.system.immutable", slug);
        }
        return namespace;
    }

    private NamespaceRole requireRole(Long namespaceId, String userId) {
        return namespaceMemberRepository.findByNamespaceIdAndUserId(namespaceId, userId)
                .map(NamespaceMember::getRole)
                .orElseThrow(() -> new DomainForbiddenException("error.namespace.membership.required"));
    }

    private void record(String action,
                        String actorUserId,
                        Long namespaceId,
                        String requestId,
                        String clientIp,
                        String userAgent,
                        String reason) {
        auditLogService.record(
                actorUserId,
                action,
                "NAMESPACE",
                namespaceId,
                requestId,
                clientIp,
                userAgent,
                reason == null || reason.isBlank() ? null : "{\"reason\":\"" + reason.replace("\"", "\\\"") + "\"}"
        );
    }
}
