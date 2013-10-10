/**
 * Copyright 2005-2013 The Kuali Foundation
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
package org.kuali.rice.kew.doctype.dao.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.rice.core.api.criteria.QueryResults;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.dao.DocumentTypeDAO;
import org.kuali.rice.krad.data.DataObjectService;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;

/**
 * JPA implementation of DocumentTypeDAo
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DocumentTypeDAOJpa implements DocumentTypeDAO {

	public static final Logger LOG = Logger.getLogger(DocumentTypeDAOJpa.class);

	@PersistenceContext(unitName="kew")
	private EntityManager entityManager;
    private DataObjectService dataObjectService;


	/**
	 * @return the entityManager
	 */
	public EntityManager getEntityManager() {
		return this.entityManager;
	}

	/**
	 * @param entityManager the entityManager to set
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public DocumentType findByName(String name){
		return findByName(name, true); // by default find by name is case sensitive
	}

    public DocumentType findByName(String name, boolean caseSensitive) {
        org.kuali.rice.core.api.criteria.QueryByCriteria.Builder builder =
                org.kuali.rice.core.api.criteria.QueryByCriteria.Builder.create();
        if (caseSensitive) {
            if (StringUtils.contains(name, "*")) {
                name = StringUtils.replace(name, "*", "%");
                builder.setPredicates(likeIgnoreCase("name", name.trim()));
            } else {
                builder.setPredicates(equal("name", name));
            }

        } else {
            if (name.contains("*") || name.contains("%")) {
                name.replace("*", "%");
                builder.setPredicates(likeIgnoreCase("name", name));
            } else {
                builder.setPredicates(equalIgnoreCase("name", name));
            }
        }
        QueryResults<DocumentType> results = getDataObjectService().findMatching(DocumentType.class, builder.build());
        if (results != null && !results.getResults().isEmpty()) {
            return results.getResults().get(0);
        }

        return null;
    }

	public Integer getMaxVersionNumber(String docTypeName) {
        TypedQuery<Integer> query = getEntityManager().
                createNamedQuery("DocumentType.GetMaxVersionNumber", Integer.class);
        query.setParameter("docTypeName", docTypeName);
        return query.getSingleResult();
	}

	public List<String> getChildDocumentTypeIds(String parentDocumentTypeId) {
		try {
            getEntityManager().flush();
            Query query = getEntityManager().createNamedQuery("DocumentType.GetChildDocumentTypeIds");
            query.setParameter("parentDocumentTypeId", parentDocumentTypeId);
            return query.getResultList();
		} catch (Exception e) {
			LOG.error("Error occured fetching children document type ids for document type " + parentDocumentTypeId, e);
			throw new RuntimeException(e);
		}
	}

	public Collection<DocumentType> find(DocumentType documentType, DocumentType docTypeParent, boolean climbHierarchy) {
		LOG.debug("documentType: "+ documentType);
		LOG.debug("docTypeParent: "+ docTypeParent);
		LOG.debug("climbHierarchy: " + climbHierarchy);
        org.kuali.rice.core.api.criteria.QueryByCriteria.Builder builder = org.kuali.rice.core.api.criteria
                .QueryByCriteria.Builder.create();
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (documentType != null && !org.apache.commons.lang.StringUtils.isEmpty(documentType.getLabel())) {
            predicates.add(likeIgnoreCase("label",documentType.getLabel().trim()));
		}
		if (documentType != null && !org.apache.commons.lang.StringUtils.isEmpty(documentType.getName())) {
			String docTypeName = documentType.getName();
            predicates.add(likeIgnoreCase("name","%" + docTypeName.trim() + "%"));
		}
		if (documentType != null && documentType.getActive() != null) {
            predicates.add(equal("active", documentType.getActive()));
		}
		if (documentType != null && documentType.getDocumentTypeId() != null) {
            predicates.add(equal("documentTypeId",documentType.getDocumentTypeId()));
		}
		if (documentType != null && documentType.getActualApplicationId() != null){
            predicates.add(equal("actualApplicationId", documentType.getActualApplicationId()));
		}
		if (docTypeParent != null) {
			if (StringUtils.isNotBlank(docTypeParent.getName())) {
                List<Predicate> parentCriteria = new ArrayList<Predicate>();
				List<Predicate> childCriteria = new ArrayList<Predicate>();
				//Criteria parentCrit = new Criteria(DocumentType.class.getName());
				addParentIdOrCriteria(docTypeParent.getDocumentTypeId(), parentCriteria);

				if (climbHierarchy) {
					assembleChildrenCriteria(docTypeParent.getChildrenDocTypes(), childCriteria);
				}
                parentCriteria.add(equal("currentInd", Boolean.TRUE));
                predicates.add(and((Predicate[]) parentCriteria.toArray(new Predicate[parentCriteria.size()])));
                if(!childCriteria.isEmpty()){
                    predicates.add(or((Predicate[])childCriteria.toArray(new Predicate[childCriteria.size()])));
                }
			}
		} else {
			if (documentType != null && StringUtils.isNotBlank(documentType.getName())) {
				DocumentType searchDocumentType = findByName(documentType.getName());
				if ((searchDocumentType != null) && climbHierarchy) {
					LOG.debug("searchDocumentType: "+ searchDocumentType);
                    List<Predicate> parentPredicates = new ArrayList<Predicate>();
                    addParentIdOrCriteria(searchDocumentType.getDocumentTypeId(), parentPredicates);
                    assembleChildrenCriteria(searchDocumentType.getChildrenDocTypes(), parentPredicates);
					parentPredicates.add(equal("currentInd", Boolean.TRUE));
					predicates.add(or(parentPredicates.toArray(new Predicate[parentPredicates.size()])));
				}
			}
		}
		predicates.add(equal("currentInd", Boolean.TRUE));
        Predicate[] preds = predicates.toArray(new Predicate[predicates.size()]);
        builder.setPredicates(preds);
        QueryResults<DocumentType> results = getDataObjectService().findMatching(DocumentType.class, builder.build());
        return results.getResults();
	}

    private void addParentIdOrCriteria(String parentId, List<Predicate> parentPredicates) {
        parentPredicates.add(equal("docTypeParentId", parentId));
    }

	private void assembleChildrenCriteria(Collection childrenDocTypes, List<Predicate> parentPredicates) {
		if (childrenDocTypes != null) {
			Iterator childrenDocTypesIter = childrenDocTypes.iterator();
			while (childrenDocTypesIter.hasNext()) {
				DocumentType child = (DocumentType) childrenDocTypesIter.next();
				addParentIdOrCriteria(child.getDocumentTypeId(), parentPredicates);
				assembleChildrenCriteria(child.getChildrenDocTypes(), parentPredicates);
			}
		}
	}

    public List findAllCurrent() {
        org.kuali.rice.core.api.criteria.QueryByCriteria.Builder builder = org.kuali
            .rice.core.api.criteria.QueryByCriteria.Builder.create();
        builder.setPredicates(
                equal("currentInd", Boolean.TRUE)
        );
        QueryResults<DocumentType> results = getDataObjectService().findMatching(DocumentType.class, builder.build());
        return results.getResults();
    }

    public List findAllCurrentByName(String name) {
        org.kuali.rice.core.api.criteria.QueryByCriteria.Builder builder = org.kuali
                .rice.core.api.criteria.QueryByCriteria.Builder.create();
        builder.setPredicates(equal("name", name), equal("currentInd", Boolean.TRUE));
        QueryResults<DocumentType> results = getDataObjectService().findMatching(DocumentType.class, builder.build());
        return results.getResults();
    }

    public String findDocumentTypeIdByName(String documentTypeName) {
        TypedQuery<String> query = getEntityManager().
                createNamedQuery("DocumentType.GetIdByName", String.class);
        query.setParameter("docTypeName", documentTypeName);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public String findDocumentTypeNameById(String documentTypeId) {
        TypedQuery<String> query = getEntityManager().
                createNamedQuery("DocumentType.FindDocumentTypeNameById", String.class);
        query.setParameter("documentTypeId",documentTypeId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public DocumentType findDocumentTypeByDocumentId(String documentId){
        TypedQuery<DocumentType> query =
                getEntityManager().createNamedQuery("DocumentType.GetDocumentTypeByDocumentId", DocumentType.class);
        query.setParameter("documentId", documentId);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }


    public DataObjectService getDataObjectService() {
        return dataObjectService;
    }

    @Required
    public void setDataObjectService(DataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }


}