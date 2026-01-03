-- Migration: Create user biometric enrollment table
-- Version: 5
-- Description: Stores biometric face tokens for users

CREATE TABLE IF NOT EXISTS user_biometric_enrollment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    face_token VARCHAR(255) NOT NULL,
    biometric_type VARCHAR(20) NOT NULL DEFAULT 'FACE',
    device_id VARCHAR(255),
    enrolled_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_verified_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_biometric_user_id ON user_biometric_enrollment(user_id);
CREATE INDEX IF NOT EXISTS idx_user_biometric_active ON user_biometric_enrollment(user_id, is_active);

COMMENT ON TABLE user_biometric_enrollment IS 'Stores biometric enrollment data for users';
COMMENT ON COLUMN user_biometric_enrollment.id IS 'Unique identifier for the enrollment';
COMMENT ON COLUMN user_biometric_enrollment.user_id IS 'ID of the user who enrolled';
COMMENT ON COLUMN user_biometric_enrollment.face_token IS 'Face token from Face++ API';
COMMENT ON COLUMN user_biometric_enrollment.biometric_type IS 'Type of biometric: FACE, FINGERPRINT, VOICE, IRIS';
COMMENT ON COLUMN user_biometric_enrollment.device_id IS 'ID of the device used for enrollment';
COMMENT ON COLUMN user_biometric_enrollment.enrolled_at IS 'Timestamp when user enrolled';
COMMENT ON COLUMN user_biometric_enrollment.last_verified_at IS 'Timestamp of last successful verification';
COMMENT ON COLUMN user_biometric_enrollment.is_active IS 'Whether this enrollment is active';

