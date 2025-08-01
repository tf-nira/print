-- DROP TABLE IF EXISTS print.card_detail CASCADE;
CREATE TABLE print.card_detail (
    transaction_id character varying PRIMARY KEY,
    reg_id character varying,
    nin character varying,
    given_name character varying,
    surname character varying,
    other_name character varying,
    nationality character varying,
    sex character varying,
    date_of_birth character varying,
    primary_finger character varying,
    secondary_finger character varying,
    date_of_issue character varying,
    date_of_expiry character varying,
	event_data character varying,
	is_ready_to_push BOOLEAN DEFAULT FALSE,
	is_pushed BOOLEAN DEFAULT FALSE,
    cr_by character varying(255) NOT NULL,
    cr_dtimes TIMESTAMP NOT NULL,
    upd_by character varying(255),
    upd_dtimes TIMESTAMP,
    is_deleted BOOLEAN,
    del_dtimes TIMESTAMP,
	is_failed BOOLEAN DEFAULT FALSE,
	remark character varying
);

CREATE INDEX IF NOT EXISTS idx_card_detail_push_status ON print.card_detail (is_ready_to_push, is_pushed, upd_dtimes);
CREATE INDEX IF NOT EXISTS INDEX idx_card_detail_nin ON print.card_detail(nin);