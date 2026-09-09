CREATE TABLE account (
   account_no    VARCHAR(32)  NOT NULL,
   cif_id        VARCHAR(20)  NOT NULL,
   customer_name VARCHAR(255) NOT NULL,
   type          VARCHAR(40)  NOT NULL,
   branch        VARCHAR(60),
   ifsc          VARCHAR(15),
   balance       DECIMAL(19,2),
   status        VARCHAR(20)  NOT NULL,
   opened_date   DATE,
   PRIMARY KEY (account_no)
);

CREATE INDEX idx_account_cif ON account (cif_id);