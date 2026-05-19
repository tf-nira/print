-- Table to store reg_ids that should be excluded from perso processing
-- Records in this table will be skipped by the print service when fetching cards to send to perso

CREATE TABLE IF NOT EXISTS print.card_exclusion (
    reg_id      VARCHAR(64)  NOT NULL,
    cr_dtimes   TIMESTAMP    NOT NULL DEFAULT NOW(),
    cr_by       VARCHAR(64),
    CONSTRAINT pk_card_exclusion PRIMARY KEY (reg_id)
);

CREATE INDEX IF NOT EXISTS idx_card_exclusion_reg_id ON print.card_exclusion (reg_id);

COMMENT ON TABLE  print.card_exclusion        IS 'Blocklist of registration IDs that should never be sent to perso';
COMMENT ON COLUMN print.card_exclusion.reg_id IS 'Registration ID to exclude';

