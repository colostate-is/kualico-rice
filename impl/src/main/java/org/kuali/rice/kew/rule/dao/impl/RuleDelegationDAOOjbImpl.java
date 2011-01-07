/*
 * Copyright 2005-2007 The Kuali Foundation
 *
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.rule.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.rice.core.exception.RiceRuntimeException;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.RuleDelegation;
import org.kuali.rice.kew.rule.RuleExtension;
import org.kuali.rice.kew.rule.RuleResponsibility;
import org.kuali.rice.kew.rule.dao.RuleDelegationDAO;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kim.bo.entity.KimPrincipal;
import org.kuali.rice.kim.service.KIMServiceLocatorInternal;
import org.springmodules.orm.ojb.support.PersistenceBrokerDaoSupport;

import java.util.*;


public class RuleDelegationDAOOjbImpl extends PersistenceBrokerDaoSupport implements RuleDelegationDAO {

    public List findByDelegateRuleId(Long ruleId) {
        Criteria crit = new Criteria();
        crit.addEqualTo("delegateRuleId", ruleId);
        return (List) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(RuleDelegation.class, crit));
    }

    public void save(RuleDelegation ruleDelegation) {
    	this.getPersistenceBrokerTemplate().store(ruleDelegation);
    }
    public List findAllCurrentRuleDelegations(){
        Criteria crit = new Criteria();
        crit.addEqualTo("delegationRuleBaseValues.currentInd", true);
        return (List) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(RuleDelegation.class, crit));
    }

    public RuleDelegation findByRuleDelegationId(Long ruleDelegationId){
        Criteria crit = new Criteria();
        crit.addEqualTo("ruleDelegationId", ruleDelegationId);
        return (RuleDelegation) this.getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(RuleDelegation.class, crit));

    }
    public void delete(Long ruleDelegationId){
    	this.getPersistenceBrokerTemplate().delete(findByRuleDelegationId(ruleDelegationId));
    }

    public List<RuleDelegation> findByResponsibilityIdWithCurrentRule(Long responsibilityId) {
    	Criteria crit = new Criteria();
    	crit.addEqualTo("responsibilityId", responsibilityId);
    	crit.addEqualTo("delegationRuleBaseValues.currentInd", true);
    	Collection delegations = getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(RuleDelegation.class, crit));
    	return new ArrayList<RuleDelegation>(delegations);
    }

    /**
     * This overridden method ...
     *
     * @see org.kuali.rice.kew.rule.dao.RuleDelegationDAO#search(java.lang.String, java.lang.Long, java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.util.Map, java.lang.String)
     */
    public List<RuleDelegation> search(String parentRuleBaseVaueId, String parentResponsibilityId, String docTypeName, Long ruleId,
            Long ruleTemplateId, String ruleDescription, String workgroupId,
            String principalId, String delegationType, Boolean activeInd,
            Map extensionValues, String workflowIdDirective) {
        Criteria crit = new Criteria(); //getSearchCriteria(docTypeName, ruleTemplateId, ruleDescription, delegationType, activeInd, extensionValues);

        if (StringUtils.isNotBlank(delegationType) && !delegationType.equals(KEWConstants.DELEGATION_BOTH)) {
        	crit.addEqualTo("delegationType", delegationType);
        }
        
        if (StringUtils.isNotBlank(parentRuleBaseVaueId) && StringUtils.isNumeric(parentRuleBaseVaueId)) {
            crit.addIn("responsibilityId", this.getRuleResponsibilitySubQuery(new Long(parentRuleBaseVaueId)));
        }

        if (StringUtils.isNotBlank(parentResponsibilityId) && StringUtils.isNumeric(parentResponsibilityId)) {
            crit.addEqualTo("responsibilityId", parentResponsibilityId);
        }

        crit.addIn("delegateRuleId", getRuleBaseValuesSubQuery(docTypeName, ruleId,
                                                               ruleTemplateId, ruleDescription, workgroupId,
                                                               principalId, activeInd,
                                                               extensionValues, workflowIdDirective));

        return (List<RuleDelegation>) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(RuleDelegation.class, crit, true));
    }

    /**
     * This overridden method ...
     *
     * @see org.kuali.rice.kew.rule.dao.RuleDelegationDAO#search(java.lang.String, java.lang.Long, java.lang.String, java.util.Collection, java.lang.String, java.lang.String, java.lang.Boolean, java.util.Map, java.util.Collection)
     */
    public List<RuleDelegation> search(String parentRuleBaseVaueId, String parentResponsibilityId, String docTypeName, Long ruleTemplateId,
            String ruleDescription, Collection<String> workgroupIds,
            String workflowId, String delegationType, Boolean activeInd,
            Map extensionValues, Collection actionRequestCodes) {
        Criteria crit = new Criteria();
        
        if (StringUtils.isNotBlank(delegationType) && !delegationType.equals(KEWConstants.DELEGATION_BOTH)) {
        	crit.addEqualTo("delegationType", delegationType);
        }
        
        if (StringUtils.isNotBlank(parentRuleBaseVaueId) && StringUtils.isNumeric(parentRuleBaseVaueId)) {
            crit.addIn("responsibilityId", this.getRuleResponsibilitySubQuery(new Long(parentRuleBaseVaueId)));
        }

        if (StringUtils.isNotBlank(parentResponsibilityId) && StringUtils.isNumeric(parentResponsibilityId)) {
            crit.addEqualTo("responsibilityId", parentResponsibilityId);
        }

        crit.addIn("delegateRuleId", getRuleBaseValuesSubQuery(docTypeName, ruleTemplateId,
                                                               ruleDescription, workgroupIds,
                                                               workflowId, activeInd,
                                                               extensionValues, actionRequestCodes));
       return (List) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(RuleDelegation.class, crit, true));
    }

    private ReportQueryByCriteria getResponsibilitySubQuery(String ruleResponsibilityName) {
        Criteria responsibilityCrit = new Criteria();
        responsibilityCrit.addLike("ruleResponsibilityName", ruleResponsibilityName);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(RuleResponsibility.class, responsibilityCrit);
        query.setAttributes(new String[] { "ruleBaseValuesId" });
        return query;
    }

    private ReportQueryByCriteria getWorkgroupResponsibilitySubQuery(Set<Long> workgroupIds) {
            Set<String> workgroupIdStrings = new HashSet<String>();
            for (Long workgroupId : workgroupIds) {
                workgroupIdStrings.add(workgroupId.toString());
            }
        Criteria responsibilityCrit = new Criteria();
        responsibilityCrit.addIn("ruleResponsibilityName", workgroupIds);
        responsibilityCrit.addEqualTo("ruleResponsibilityType", KEWConstants.RULE_RESPONSIBILITY_GROUP_ID);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(RuleResponsibility.class, responsibilityCrit);
        query.setAttributes(new String[] { "ruleBaseValuesId" });
        return query;
    }

    private ReportQueryByCriteria getRuleBaseValuesSubQuery(String docTypeName, Long ruleTemplateId,
            String ruleDescription, Collection<String> workgroupIds,
            String workflowId, Boolean activeInd,
            Map extensionValues, Collection actionRequestCodes) {
        Criteria crit = getSearchCriteria(docTypeName, ruleTemplateId, ruleDescription, activeInd, extensionValues);
        crit.addIn("responsibilities.ruleBaseValuesId", getResponsibilitySubQuery(workgroupIds, workflowId, actionRequestCodes, (workflowId != null), ((workgroupIds != null) && !workgroupIds.isEmpty())));
        crit.addEqualTo("delegateRule", 1);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(RuleBaseValues.class, crit);
        query.setAttributes(new String[] { "ruleBaseValuesId" });
        return query;
    }

    private ReportQueryByCriteria getRuleBaseValuesSubQuery(String docTypeName, Long ruleId,
            Long ruleTemplateId, String ruleDescription, String workgroupId,
            String principalId, Boolean activeInd,
            Map extensionValues, String workflowIdDirective) {
        Criteria crit = getSearchCriteria(docTypeName, ruleTemplateId, ruleDescription, activeInd, extensionValues);
        if (ruleId != null) {
            crit.addEqualTo("ruleBaseValuesId", ruleId);
        }
        if (workgroupId != null) {
            crit.addIn("responsibilities.ruleBaseValuesId", getResponsibilitySubQuery(workgroupId));
        }
        List<String> workgroupIds = new ArrayList<String>();
        Boolean searchUser = Boolean.FALSE;
        Boolean searchUserInWorkgroups = Boolean.FALSE;
        if (workflowIdDirective != null) {/** IU patch EN-1552 */
            if ("group".equals(workflowIdDirective)) {
                searchUserInWorkgroups = Boolean.TRUE;
            } else if ("".equals(workflowIdDirective)) {
                searchUser = Boolean.TRUE;
                searchUserInWorkgroups = Boolean.TRUE;
            } else {
                searchUser = Boolean.TRUE;
            }
        }
        if (!org.apache.commons.lang.StringUtils.isEmpty(principalId) && searchUserInWorkgroups)
        {
            KimPrincipal principal = KIMServiceLocatorInternal.getIdentityManagementService().getPrincipal(principalId);

            if (principal == null)
            {
                throw new RiceRuntimeException("Failed to locate user for the given workflow id: " + principalId);
            }
            workgroupIds = KIMServiceLocatorInternal.getIdentityManagementService().getGroupIdsForPrincipal(principalId);
        }
        crit.addIn("responsibilities.ruleBaseValuesId", getResponsibilitySubQuery(workgroupIds, principalId, searchUser, searchUserInWorkgroups));
        crit.addEqualTo("delegateRule", 1);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(RuleBaseValues.class, crit);
        query.setAttributes(new String[] { "ruleBaseValuesId" });
        return query;
        //return (List<RuleDelegation>) this.getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(RuleDelegation.class, crit, true));
    }

    private ReportQueryByCriteria getRuleResponsibilitySubQuery(Long ruleBaseValuesId) {
        Criteria crit = new Criteria();
        crit.addEqualTo("ruleBaseValuesId", ruleBaseValuesId);
        ReportQueryByCriteria query = QueryFactory.newReportQuery(RuleResponsibility.class, crit);
        query.setAttributes(new String[] { "responsibilityId" });
        return query;
        //return getResponsibilitySubQuery(workgroupIdStrings,workflowId,new ArrayList<String>(), searchUser, searchUserInWorkgroups);
    }

    private ReportQueryByCriteria getResponsibilitySubQuery(List<String> workgroupIds, String workflowId, Boolean searchUser, Boolean searchUserInWorkgroups) {
        Collection<String> workgroupIdStrings = new ArrayList<String>();
        for (String workgroupId : workgroupIds) {
            workgroupIdStrings.add(workgroupId);
        }
        return getResponsibilitySubQuery(workgroupIdStrings,workflowId,new ArrayList<String>(), searchUser, searchUserInWorkgroups);
    }

    private ReportQueryByCriteria getResponsibilitySubQuery(Collection<String> workgroupIds, String workflowId, Collection actionRequestCodes, Boolean searchUser, Boolean searchUserInWorkgroups) {
        Criteria responsibilityCrit = new Criteria();
        if ( (actionRequestCodes != null) && (!actionRequestCodes.isEmpty()) ) {
            responsibilityCrit.addIn("actionRequestedCd", actionRequestCodes);
        }

        Criteria ruleResponsibilityNameCrit = null;
        if (!org.apache.commons.lang.StringUtils.isEmpty(workflowId)) {
            // workflow user id exists
            if (searchUser != null && searchUser) {
                // searching user wishes to search for rules specific to user
                ruleResponsibilityNameCrit = new Criteria();
                ruleResponsibilityNameCrit.addLike("ruleResponsibilityName", workflowId);
                ruleResponsibilityNameCrit.addEqualTo("ruleResponsibilityType", KEWConstants.RULE_RESPONSIBILITY_WORKFLOW_ID);
            }
            if ( (searchUserInWorkgroups != null && searchUserInWorkgroups) && (workgroupIds != null) && (!workgroupIds.isEmpty()) ) {
                // at least one workgroup id exists and user wishes to search on workgroups
                if (ruleResponsibilityNameCrit == null) {
                    ruleResponsibilityNameCrit = new Criteria();
                }
                Criteria workgroupCrit = new Criteria();
                workgroupCrit.addIn("ruleResponsibilityName", workgroupIds);
                workgroupCrit.addEqualTo("ruleResponsibilityType", KEWConstants.RULE_RESPONSIBILITY_GROUP_ID);
                ruleResponsibilityNameCrit.addOrCriteria(workgroupCrit);
            }
        } else if ( (workgroupIds != null) && (workgroupIds.size() == 1) ) {
            // no user and one workgroup id
            ruleResponsibilityNameCrit = new Criteria();
            ruleResponsibilityNameCrit.addLike("ruleResponsibilityName", workgroupIds.iterator().next());
            ruleResponsibilityNameCrit.addEqualTo("ruleResponsibilityType", KEWConstants.RULE_RESPONSIBILITY_GROUP_ID);
        } else if ( (workgroupIds != null) && (workgroupIds.size() > 1) ) {
            // no user and more than one workgroup id
            ruleResponsibilityNameCrit = new Criteria();
            ruleResponsibilityNameCrit.addIn("ruleResponsibilityName", workgroupIds);
            ruleResponsibilityNameCrit.addEqualTo("ruleResponsibilityType", KEWConstants.RULE_RESPONSIBILITY_GROUP_ID);
        }
        if (ruleResponsibilityNameCrit != null) {
            responsibilityCrit.addAndCriteria(ruleResponsibilityNameCrit);
        }

        ReportQueryByCriteria query = QueryFactory.newReportQuery(RuleResponsibility.class, responsibilityCrit);
        query.setAttributes(new String[] { "ruleBaseValuesId" });
        return query;
    }


    private Criteria getSearchCriteria(String docTypeName, Long ruleTemplateId, String ruleDescription, Boolean activeInd, Map extensionValues) {
        Criteria crit = new Criteria();
        crit.addEqualTo("currentInd", Boolean.TRUE);
        crit.addEqualTo("templateRuleInd", Boolean.FALSE);
        if (activeInd != null) {
            crit.addEqualTo("activeInd", activeInd);
        }
        if (docTypeName != null) {
            crit.addLike("UPPER(docTypeName)", docTypeName.toUpperCase());
        }
        if (ruleDescription != null && !ruleDescription.trim().equals("")) {
            crit.addLike("UPPER(description)", ruleDescription.toUpperCase());
        }
        if (ruleTemplateId != null) {
            crit.addEqualTo("ruleTemplateId", ruleTemplateId);
        }
        if (extensionValues != null && !extensionValues.isEmpty()) {
            for (Iterator iter2 = extensionValues.entrySet().iterator(); iter2.hasNext();) {
                Map.Entry entry = (Map.Entry) iter2.next();
                if (!org.apache.commons.lang.StringUtils.isEmpty((String) entry.getValue())) {
                    // Criteria extensionCrit = new Criteria();
                    // extensionCrit.addEqualTo("extensionValues.key",
                    // entry.getKey());
                    // extensionCrit.addLike("extensionValues.value",
                    // "%"+(String) entry.getValue()+"%");

                    Criteria extensionCrit2 = new Criteria();
                    extensionCrit2.addEqualTo("extensionValues.key", entry.getKey());
                    extensionCrit2.addLike("UPPER(extensionValues.value)", ("%" + (String) entry.getValue() + "%").toUpperCase());

                    // Criteria extensionCrit3 = new Criteria();
                    // extensionCrit3.addEqualTo("extensionValues.key",
                    // entry.getKey());
                    // extensionCrit3.addLike("extensionValues.value",
                    // ("%"+(String) entry.getValue()+"%").toLowerCase());

                    // extensionCrit.addOrCriteria(extensionCrit2);
                    // extensionCrit.addOrCriteria(extensionCrit3);
                    ReportQueryByCriteria query = QueryFactory.newReportQuery(RuleExtension.class, extensionCrit2);
                    query.setAttributes(new String[] { "ruleBaseValuesId" });
                    crit.addIn("ruleExtensions.ruleBaseValuesId", query);
                }
            }
        }
        return crit;
    }
}
