CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  actor_user_id BIGINT NULL,
  action VARCHAR(80) NOT NULL,
  entity_type VARCHAR(80) NOT NULL,
  entity_id VARCHAR(100) NULL,
  outcome ENUM('SUCCESS','FAILURE') NOT NULL,
  occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_audit_actor_time (actor_user_id, occurred_at),
  INDEX idx_audit_entity (entity_type, entity_id),
  CONSTRAINT fk_audit_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);
