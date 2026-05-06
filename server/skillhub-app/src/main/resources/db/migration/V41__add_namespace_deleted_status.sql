-- V41__add_namespace_deleted_status.sql
-- Add DELETED status to namespace lifecycle

-- Note: PostgreSQL enum types cannot be altered directly in a transaction-safe way.
-- The namespace.status column uses VARCHAR(32), so no schema change is needed.
-- This migration serves as documentation that DELETED is now a valid status value.

-- Add comment to document the new status
COMMENT ON COLUMN namespace.status IS 'Namespace lifecycle status: ACTIVE, FROZEN, ARCHIVED, DELETED';
