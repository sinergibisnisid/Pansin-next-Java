package com.bjb.pansin.common.enums;

/**
 * Lifecycle states for an IoT device, from the moment it's manufactured
 * until it's decommissioned. Only INSTALLED devices are allowed to
 * communicate with the production backend.
 */
public enum DeviceLifecycleState {
    MANUFACTURED,
    PROVISIONED,
    IN_TRANSIT,
    RECEIVED,
    ALLOCATED,
    INSTALLED,
    MAINTENANCE,
    DECOMMISSIONED,
    LOST_OR_STOLEN,
    REVOKED
}
