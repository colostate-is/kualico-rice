alter table "EN_DOC_HDR_T" drop column "DOC_OVRD_IND"
/
alter table "EN_DOC_HDR_T" drop column "DOC_LOCK_CD"
/
alter table "EN_RTE_NODE_T" drop column "CONTENT_FRAGMENT"
/
alter table "EN_DOC_HDR_T" drop column "DTYPE"
/
alter table "EN_ACTN_ITM_T" drop column "DTYPE"
/
alter table "EN_USR_T" drop column "DTYPE"
/
alter table "EN_DOC_TYP_T" drop column "CSTM_ACTN_LIST_ATTRIB_CLS_NM"
/
alter table "EN_DOC_TYP_T" drop column "CSTM_ACTN_EMAIL_ATTRIB_CLS_NM"
/
alter table "KRICE"."EN_DOC_TYP_T" drop column "CSTM_DOC_NTE_ATTRIB_CLS_NM" 
/

-- Convert KEW group ids from numbers to strings /

CREATE TABLE KRTMP_DOC_TYP_T (
    DOC_TYP_ID NUMBER(19) NOT NULL,
    GRP_ID VARCHAR2(40),
    BLNKT_APPR_GRP_ID VARCHAR2(40),
    RPT_GRP_ID VARCHAR2(40))
/
INSERT INTO KRTMP_DOC_TYP_T
SELECT DOC_TYP_ID, GRP_ID, BLNKT_APPR_GRP_ID, RPT_GRP_ID
FROM KREW_DOC_TYP_T
/

ALTER TABLE KREW_DOC_TYP_T DROP COLUMN BLNKT_APPR_GRP_ID
/
ALTER TABLE KREW_DOC_TYP_T ADD BLNKT_APPR_GRP_ID VARCHAR2(40)
/

ALTER TABLE KREW_DOC_TYP_T DROP COLUMN RPT_GRP_ID
/
ALTER TABLE KREW_DOC_TYP_T ADD RPT_GRP_ID VARCHAR2(40)
/

ALTER TABLE KREW_DOC_TYP_T DROP COLUMN GRP_ID
/
ALTER TABLE KREW_DOC_TYP_T ADD GRP_ID VARCHAR2(40)
/

UPDATE KREW_DOC_TYP_T a SET a.BLNKT_APPR_GRP_ID = (select b.BLNKT_APPR_GRP_ID from KRTMP_DOC_TYP_T b where b.DOC_TYP_ID=a.DOC_TYP_ID)
/
UPDATE KREW_DOC_TYP_T a SET a.RPT_GRP_ID = (select b.RPT_GRP_ID from KRTMP_DOC_TYP_T b where b.DOC_TYP_ID=a.DOC_TYP_ID)
/
UPDATE KREW_DOC_TYP_T a SET a.GRP_ID = (select b.GRP_ID from KRTMP_DOC_TYP_T b where b.DOC_TYP_ID=a.DOC_TYP_ID)
/

DROP TABLE KRTMP_DOC_TYP_T
/

CREATE TABLE KRTMP_RTE_NODE_T (
    RTE_NODE_ID NUMBER(19) NOT NULL,
    GRP_ID VARCHAR2(40))
/

INSERT INTO KRTMP_RTE_NODE_T
SELECT RTE_NODE_ID, GRP_ID
FROM KREW_RTE_NODE_T
/

ALTER TABLE KREW_RTE_NODE_T DROP COLUMN GRP_ID
/
ALTER TABLE KREW_RTE_NODE_T ADD GRP_ID VARCHAR2(40)
/

UPDATE KREW_RTE_NODE_T a SET a.GRP_ID = (select b.GRP_ID from KRTMP_RTE_NODE_T b where b.RTE_NODE_ID=a.RTE_NODE_ID)
/

DROP TABLE KRTMP_RTE_NODE_T
/

-- New System Parameters /

INSERT INTO KRNS_NMSPC_T ("NMSPC_CD","VER_NBR","NM","ACTV_IND") VALUES ('KR-WRKFLW', 0,'Workflow',1)
/

INSERT INTO KRNS_PARM_T ("NMSPC_CD","PARM_DTL_TYP_CD","PARM_NM","TXT","CONS_CD","PARM_DESC_TXT","PARM_TYP_CD","GRP_NM") VALUES ('KR-WRKFLW','All','KIM_PRIORITY_ON_DOC_TYP_PERMS_IND','N','A','Flag for enabling/disabling document type permissions checks to use KIM Permissions as priority over Document Type policies.','CONFG','WorkflowAdmin')
/