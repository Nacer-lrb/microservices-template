package com.template.auth.entity;

/**
 * Defines the set of roles available in the system.
 * Roles are stored as strings in the database via @ElementCollection.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MANAGER
}
