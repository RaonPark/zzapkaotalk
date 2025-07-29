create table verification_code(
    id BIGINT NOT NULL,
    email VARCHAR(100) NOT NULL,
    verificationCode VARCHAR(6) NOT NULL,
);

CREATE INDEX idx_verification_code_email ON verification_code(email);