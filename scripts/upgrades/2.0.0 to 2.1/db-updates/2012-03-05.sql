UPDATE KREW_DOC_TYP_T SET LBL = 'Undefined' WHERE LBL is null
/
ALTER TABLE KREW_DOC_TYP_T MODIFY (LBL NOT NULL)
/