package com.fund_transfer.backend.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Fine-grained permission checks used from @PreAuthorize annotations.
 *
 * Architecture (per the RBAC spec §4): Keycloak owns coarse roles only;
 * this service owns the role -> fine-grained-permission mapping. Phase 1
 * is config-based (this hardcoded map) — moving it to a database is an
 * explicit later phase, not done here.
 *
 * Role source: KeycloakJwtAuthenticationConverter (see SecurityConfig)
 * puts each Keycloak realm role directly onto the Authentication as a
 * GrantedAuthority, using the raw role name with NO "ROLE_" prefix (e.g.
 * "RETAIL_CUSTOMER", not "ROLE_RETAIL_CUSTOMER"). That decision was made
 * here because no Spring Security config existed anywhere in this project
 * before this task — there was no existing prefix convention to match, so
 * one had to be chosen and is documented rather than assumed. If a
 * different part of the system already relies on a ROLE_-prefixed
 * convention, reconcile that before deploying this.
 *
 * Scope note (§4.2, "Flag this rather than silently building it"): the
 * permission set below only covers what §4.1 defined — RETAIL_CUSTOMER's
 * create/view permissions per resource, and BANK_ADMIN/BANK_SUPER_ADMIN's
 * read-only transfer:view. It does NOT define separate permissions for
 * "delete beneficiary" / "cancel schedule" / "pause schedule" etc. —
 * the spec's §4.1 permission list didn't include them. Controllers map
 * those mutating-but-not-creating actions onto the closest existing
 * "*:create" permission (see TransferController / BeneficiaryController /
 * ScheduleController) rather than inventing new permission names
 * unilaterally. Revisit if finer granularity (e.g. beneficiary:delete) is
 * actually wanted.
 */
@Component("permissionService")
public class PermissionService {

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
            "RETAIL_CUSTOMER", Set.of(
                    "transfer:create", "transfer:view",
                    "beneficiary:create", "beneficiary:view",
                    "schedule:create", "schedule:view"
            ),
            "BANK_ADMIN", Set.of("transfer:view"),
            "BANK_SUPER_ADMIN", Set.of("transfer:view")
    );

    public boolean hasPermission(Authentication authentication, String permission) {
        if (authentication == null || permission == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            Set<String> permissions = ROLE_PERMISSIONS.get(authority.getAuthority());
            if (permissions != null && permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
}

