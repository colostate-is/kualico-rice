/*
 * Copyright 2005-2006 The Kuali Foundation.
 *
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.docsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.exception.RiceRuntimeException;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
//import org.kuali.rice.kns.web.ui.Column;
import org.kuali.rice.kew.docsearch.DocumentSearchColumn;
import org.kuali.rice.kns.web.ui.Field;
import org.kuali.rice.kew.docsearch.DocumentSearchField;
//import org.kuali.rice.kns.web.ui.Row;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kew.user.WorkflowUser;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kew.util.KEWPropertyConstants;
import org.kuali.rice.kew.util.Utilities;
import org.kuali.rice.kew.web.KeyValueSort;
import org.kuali.rice.kew.web.UrlResolver;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.util.KNSConstants;


/**
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class StandardDocumentSearchResultProcessor implements DocumentSearchResultProcessor {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(StandardDocumentSearchResultProcessor.class);

    private Map<String,Boolean> sortableByKey = new HashMap<String,Boolean>();
    private Map<String,String> labelsByKey = new HashMap<String,String>();
    private DocSearchCriteriaDTO searchCriteria;
    private String searchingUser;

    /**
     * @return the searchCriteria
     */
    public DocSearchCriteriaDTO getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * @param searchCriteria the searchCriteria to set
     */
    public void setSearchCriteria(DocSearchCriteriaDTO searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    /**
     * @return the searchingUser
     */
    public String getSearchingUser() {
        return searchingUser;
    }

    /**
     * @param searchingUser the searchingUser to set
     */
    public void setSearchingUser(String searchingUser) {
        this.searchingUser = searchingUser;
    }

    public List<DocumentSearchColumn> getCustomDisplayColumns() {
		return new ArrayList<DocumentSearchColumn>();
	}

    private List<DocumentSearchColumn> getAndSetUpCustomDisplayColumns(DocSearchCriteriaDTO criteria) {
        List<DocumentSearchColumn> columns = getCustomDisplayColumns();
        for (DocumentSearchColumn column : columns) {
            if (column instanceof DocumentSearchColumn) {
                DocumentSearchColumn dsColumn = (DocumentSearchColumn)column;
                for (org.kuali.rice.kns.web.ui.Field field : getFields(criteria)) {
                    if (field instanceof DocumentSearchField) {
                        DocumentSearchField dsField = (DocumentSearchField)field;
                        if ( (dsField.getSavablePropertyName().equals(dsColumn.getKey())) && (dsColumn.getDisplayParameters().isEmpty()) ) {
                            dsColumn.setDisplayParameters(dsField.getDisplayParameters());
                        }
                    } else {
                        throw new RiceRuntimeException("field must be of type org.kuali.rice.kew.docsearch.DocumentSearchField");
                    }
                }
            } else {
                throw new RiceRuntimeException("column must be of type org.kuali.rice.kew.docsearch.DocumentSearchColumn");
            }
        }
        return columns;
    }

	public boolean getShowAllStandardFields() {
		return true;
	}

	public boolean getOverrideSearchableAttributes() {
		return false;
	}

    /**
     * Convenience method to find a specific searchable attribute
     *
     * @param name  - name of search attribute savable property name
     * @return the SearchAttributeCriteriaComponent object related to the given key name or null if component is not found
     */
    protected SearchAttributeCriteriaComponent getSearchableAttributeByFieldName(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Attempted to find Searchable Attribute with blank Field name '" + name + "'");
        }
        for (Iterator iter = getSearchCriteria().getSearchableAttributes().iterator(); iter.hasNext();) {
            SearchAttributeCriteriaComponent critComponent = (SearchAttributeCriteriaComponent) iter.next();
            if (name.equals(critComponent.getFormKey())) {
                return critComponent;
            }
        }
        return null;
    }

	/* (non-Javadoc)
	 * @see org.kuali.rice.kew.docsearch.DocumentSearchResultProcessor#processIntoFinalResults(java.util.List, org.kuali.rice.kew.docsearch.DocSearchCriteriaDTO, org.kuali.rice.kew.user.WorkflowUser)
	 */
	public DocumentSearchResultComponents processIntoFinalResults(List<DocSearchDTO> docSearchResultRows, DocSearchCriteriaDTO criteria, String principalId) {
        this.setSearchCriteria(criteria);
        this.setSearchingUser(principalId);
		List columns = constructColumnList(criteria);

		List<DocumentSearchResult> documentSearchResults = new ArrayList<DocumentSearchResult>();
		for (Iterator iter = docSearchResultRows.iterator(); iter.hasNext();) {
			DocSearchDTO docCriteriaDTO = (DocSearchDTO) iter.next();
			DocumentSearchResult docSearchResult = this.generateSearchResult(docCriteriaDTO,columns);
			if (docSearchResult != null) {
				documentSearchResults.add(docSearchResult);
			}
		}
		return new DocumentSearchResultComponents(columns,documentSearchResults);
	}

	/**
	 * Method to construct a list of columns in order of how they should appear in the search results
	 *
	 * @return a list of columns in an ordered list that will be used to generate the final search results
	 */
	public List<DocumentSearchColumn> constructColumnList(DocSearchCriteriaDTO criteria) {
		List<DocumentSearchColumn> tempColumns = new ArrayList<DocumentSearchColumn>();
		List<DocumentSearchColumn> customDisplayColumnNames = getAndSetUpCustomDisplayColumns(criteria);
        if ((!getShowAllStandardFields()) && (getOverrideSearchableAttributes())) {
			// use only what is contained in displayColumns
			this.addAllCustomColumns(tempColumns, criteria, customDisplayColumnNames);
		} else if (getShowAllStandardFields() && (getOverrideSearchableAttributes())) {
			// do standard fields and use displayColumns for searchable attributes
			this.addStandardSearchColumns(tempColumns);
			this.addAllCustomColumns(tempColumns, criteria, customDisplayColumnNames);
		} else if ((!getShowAllStandardFields()) && (!getOverrideSearchableAttributes())) {
			// do displayColumns and then do standard searchable attributes
			this.addCustomStandardCriteriaColumns(tempColumns, criteria, customDisplayColumnNames);
			this.addSearchableAttributeColumnsNoOverrides(tempColumns,criteria);
		}
		if (tempColumns.isEmpty()) {
			// do default
			this.addStandardSearchColumns(tempColumns);
			this.addSearchableAttributeColumnsNoOverrides(tempColumns,criteria);
		}

		List<DocumentSearchColumn> columns = new ArrayList<DocumentSearchColumn>();
		this.addRouteHeaderIdColumn(columns);
		columns.addAll(tempColumns);
		this.addRouteLogColumn(columns);
		return columns;
	}

	public void addStandardSearchColumns(List<DocumentSearchColumn> columns) {
		this.addColumnUsingKey(columns, KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOC_TYPE_LABEL);
		this.addColumnUsingKey(columns, KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOCUMENT_TITLE);
		this.addColumnUsingKey(columns, KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_STATUS_DESC);
		this.addColumnUsingKey(columns, KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_INITIATOR);
		this.addColumnUsingKey(columns, KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DATE_CREATED);
	}

	public void addRouteHeaderIdColumn(List<DocumentSearchColumn> columns) {
		this.addColumnUsingKey(columns, KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_HEADER_ID);
	}

	public void addRouteLogColumn(List<DocumentSearchColumn> columns) {
		this.addColumnUsingKey(columns, KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_LOG);
	}

	public void addSearchableAttributeColumnsNoOverrides(List<DocumentSearchColumn> columns,DocSearchCriteriaDTO criteria) {
        this.addSearchableAttributeColumnsBasedOnFields(columns, criteria, null);
	}

    protected void addSearchableAttributeColumnsBasedOnFields(List<DocumentSearchColumn> columns,DocSearchCriteriaDTO criteria,List<String> searchAttributeFieldNames) {
        Set<String> alreadyProcessedFieldKeys = new HashSet<String>();
        List<Field> fields = this.getFields(criteria, searchAttributeFieldNames);
        for (Field field : fields) {
            if (field instanceof DocumentSearchField) {
                DocumentSearchField dsField = (DocumentSearchField)field;
                if ( (dsField.getSavablePropertyName() == null) || (!alreadyProcessedFieldKeys.contains(dsField.getSavablePropertyName())) ) {
                    if (dsField.isColumnVisible()) {
                        if (DocumentSearchField.SEARCH_RESULT_DISPLAYABLE_FIELD_TYPES.contains(dsField.getFieldType())) {
                            String resultFieldLabel = dsField.getFieldLabel();
                            if (dsField.isMemberOfRange()) {
                                resultFieldLabel = dsField.getMainFieldLabel();
                            }
                            this.addSearchableAttributeColumnUsingKey(columns, dsField.getDisplayParameters(), dsField.getSavablePropertyName(), resultFieldLabel, getSortableByKey().get(dsField.getSavablePropertyName()), Boolean.TRUE);
                            if (dsField.getSavablePropertyName() != null) {
                                alreadyProcessedFieldKeys.add(dsField.getSavablePropertyName());
                            }
                        }
                    }
                }
            } else {
                throw new RiceRuntimeException("Fields must be of type org.kuali.rice.kew.docsearch.DocumentSearchField");
            }
        }
    }

	public void addAllCustomColumns(List<DocumentSearchColumn> columns,DocSearchCriteriaDTO criteria,List<DocumentSearchColumn> customDisplayColumns) {
		for (DocumentSearchColumn customColumn : customDisplayColumns) {
			this.addCustomColumn(columns,customColumn);
		}
	}

	public void addCustomStandardCriteriaColumns(List<DocumentSearchColumn> columns,DocSearchCriteriaDTO criteria,List<DocumentSearchColumn> customDisplayColumns) {
		for (DocumentSearchColumn customColumn : customDisplayColumns) {
			if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_SET.contains(customColumn.getKey())) {
				this.addCustomColumn(columns,customColumn);
			}
		}
	}

	public void addCustomColumn(List<DocumentSearchColumn> columns,DocumentSearchColumn customColumn) {
		Boolean sortable = null;
		if ( (customColumn.getSortable() != null) && (DocumentSearchColumn.COLUMN_IS_SORTABLE_VALUE.equals(customColumn.getSortable())) ) {
			sortable =  Boolean.TRUE;
		} else if ( (customColumn.getSortable() != null) && (DocumentSearchColumn.COLUMN_NOT_SORTABLE_VALUE.equals(customColumn.getSortable())) ) {
			sortable = Boolean.FALSE;
		}
		addColumnUsingKey(columns, customColumn.getDisplayParameters(), customColumn.getKey(), customColumn.getColumnTitle(), sortable);
	}

	private List<Field> getFields(DocSearchCriteriaDTO criteria) {
	    return getFields(criteria, null);
	}

    private DocumentType getDocumentType(String documentTypeName) {
	DocumentType documentType = null;
	if (StringUtils.isNotBlank(documentTypeName)) {
	    documentType = ((DocumentTypeService) KEWServiceLocator.getService(KEWServiceLocator.DOCUMENT_TYPE_SERVICE)).findByName(documentTypeName);
	}
	return documentType;
    }

    private List<Field> getFields(DocSearchCriteriaDTO criteria, List<String> searchAttributeFieldNames) {
		List<Field> returnFields = new ArrayList<Field>();
		DocumentType documentType = getDocumentType(criteria.getDocTypeFullName());
		if (documentType != null) {
            List<Field> allFields = new ArrayList<Field>();
            for (SearchableAttribute searchableAttribute : documentType.getSearchableAttributes()) {
                List<DocumentSearchRow> searchRows = searchableAttribute.getSearchingRows(DocSearchUtils.getDocumentSearchContext("", documentType.getName(), ""));
                if (searchRows == null) {
                    continue;
                }
                for (DocumentSearchRow row : searchRows) {
                    allFields.addAll(row.getFields());
                }
            }
            if (searchAttributeFieldNames == null) {
                returnFields = allFields;
            } else {
                for (String searchAttributeName : searchAttributeFieldNames) {
                    for (Field field : allFields) {
                        DocumentSearchField dsField = (DocumentSearchField)field;
                        if (field instanceof DocumentSearchField) {
                            if (searchAttributeName.equals(dsField.getSavablePropertyName())) {
                                returnFields.add(field);
                            }
                        } else {
                            throw new RiceRuntimeException("Fields must be of type org.kuali.rice.kew.docsearch.DocumentSearchField");
                        }
                    }
                }
            }
		}
		return returnFields;
	}

	public DocumentSearchResult generateSearchResult(DocSearchDTO docCriteriaDTO, List<DocumentSearchColumn> columns) {
		Map<String,Object> alternateSortValues = getSortValuesMap(docCriteriaDTO);
		DocumentSearchResult docSearchResult = null;
		for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
		    DocumentSearchColumn currentColumn = (DocumentSearchColumn) iterator.next();
			KeyValueSort kvs = generateSearchResult(docCriteriaDTO,currentColumn,alternateSortValues);
			if (kvs != null) {
				if (docSearchResult == null) {
					docSearchResult = new DocumentSearchResult();
				}
				docSearchResult.addResultContainer(kvs);
			}
		}
		return docSearchResult;
	}

	protected class DisplayValues {
		public String htmlValue;
		public String userDisplayValue;
	}

	public KeyValueSort generateSearchResult(DocSearchDTO docCriteriaDTO, DocumentSearchColumn column, Map<String,Object> sortValuesByColumnKey) {
		KeyValueSort returnValue = null;
		DisplayValues fieldValue = null;
		Object sortFieldValue = null;
		String columnKeyName = column.getKey();
		SearchableAttributeValue attributeValue = null;

		if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_HEADER_ID.equals(columnKeyName)) {
			fieldValue = this.getRouteHeaderIdFieldDisplayValue(docCriteriaDTO.getRouteHeaderId().toString(), docCriteriaDTO.isUsingSuperUserSearch(), docCriteriaDTO.getDocTypeName());
			sortFieldValue = sortValuesByColumnKey.get(columnKeyName);
		} else if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_LOG.equals(columnKeyName)) {
			fieldValue = this.getRouteLogFieldDisplayValue(docCriteriaDTO.getRouteHeaderId().toString());
			sortFieldValue = sortValuesByColumnKey.get(columnKeyName);
		} else if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DATE_CREATED.equals(columnKeyName)) {
			fieldValue = new DisplayValues();
			fieldValue.htmlValue = DocSearchUtils.getDisplayValueWithDateTime(docCriteriaDTO.getDateCreated());
			sortFieldValue = sortValuesByColumnKey.get(columnKeyName);
		} else if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOC_TYPE_LABEL.equals(columnKeyName)) {
			fieldValue = new DisplayValues();
			fieldValue.htmlValue = docCriteriaDTO.getDocTypeLabel();
			sortFieldValue = sortValuesByColumnKey.get(columnKeyName);
		} else if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOCUMENT_TITLE.equals(columnKeyName)) {
			fieldValue = new DisplayValues();
			fieldValue.htmlValue = docCriteriaDTO.getDocumentTitle();
			sortFieldValue = sortValuesByColumnKey.get(columnKeyName);
		} else if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_INITIATOR.equals(columnKeyName)) {
			fieldValue = this.getInitiatorFieldDisplayValue(docCriteriaDTO.getInitiatorTransposedName(), docCriteriaDTO.getInitiatorWorkflowId());
			sortFieldValue = sortValuesByColumnKey.get(columnKeyName);
		} else if (KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_STATUS_DESC.equals(columnKeyName)) {
			fieldValue = new DisplayValues();
			fieldValue.htmlValue = docCriteriaDTO.getDocRouteStatusCodeDesc();
			sortFieldValue = sortValuesByColumnKey.get(columnKeyName);
		} else {
			// check searchable attributes
			for (Iterator iter = docCriteriaDTO.getSearchableAttributes().iterator(); iter.hasNext();) {
				KeyValueSort searchAttribute = (KeyValueSort) iter.next();
				if (searchAttribute.getKey().equals(columnKeyName)) {
					Object sortValue = sortValuesByColumnKey.get(columnKeyName);
					sortFieldValue = (sortValue != null) ? sortValue : searchAttribute.getSortValue();
					attributeValue = searchAttribute.getSearchableAttributeValue();
					if ( (column.getDisplayParameters() != null) && (!column.getDisplayParameters().isEmpty()) ) {
					    fieldValue = new DisplayValues();
						fieldValue.htmlValue = searchAttribute.getSearchableAttributeValue().getSearchableAttributeDisplayValue(column.getDisplayParameters());
					}
					else {
					    fieldValue = new DisplayValues();
						fieldValue.htmlValue = searchAttribute.getValue();
					}
					break;
				}
			}
		}
		if (fieldValue != null) {
			String userDisplaySortValue = fieldValue.userDisplayValue;
			if (StringUtils.isBlank(userDisplaySortValue)) {
				userDisplaySortValue = fieldValue.htmlValue;
			}
		    returnValue = new KeyValueSort(columnKeyName,fieldValue.htmlValue,fieldValue.userDisplayValue,(sortFieldValue != null) ? sortFieldValue : userDisplaySortValue, attributeValue);
		}
		return returnValue;
	}

	/*
	 * Convenience Methods to get field values for certain Workflow Standard
	 * Search Result columns
	 */

	protected DisplayValues getRouteLogFieldDisplayValue(String routeHeaderId) {
		DisplayValues dv = new DisplayValues();
		String linkPopup = "";
		if (this.isRouteLogPopup()) {
			linkPopup = " target=\"_new\"";
		}
		String imageSource = "<img alt=\"Route Log for Document\" src=\"images/my_route_log.gif\"/>";
		dv.htmlValue = "<a href=\"RouteLog.do?routeHeaderId=" + routeHeaderId + "\"" + linkPopup + ">" + imageSource + "</a>";
		dv.userDisplayValue = imageSource;
		return dv;
	}

	protected DisplayValues getRouteHeaderIdFieldDisplayValue(String routeHeaderId,boolean isSuperUserSearch, String documentTypeName) {
		return this.getValueEncodedWithDocHandlerUrl(routeHeaderId, routeHeaderId, isSuperUserSearch, documentTypeName);
	}

	protected DisplayValues getInitiatorFieldDisplayValue(String fieldLinkTextValue, String initiatorWorkflowId) {
		UrlResolver urlResolver = new UrlResolver();
		DisplayValues dv = new DisplayValues();
		dv.htmlValue = "<a href=\"" + urlResolver.getUserReportUrl() +  "?showEdit=no&methodToCall=report&workflowId=" + initiatorWorkflowId + "\" target=\"_blank\">" + fieldLinkTextValue + "</a>";
		dv.userDisplayValue = fieldLinkTextValue;
		return dv;
	}

	/**
	 * Convenience method to allow child classes to use a custom value string and wrap
	 * that string in the document handler URL
	 *
	 * @param value - the value that will show on screen as the clickable link
	 * @param routeHeaderId - the string value of the route header id the doc handler should point to
	 * @param isSuperUserSearch - boolean indicating whether this search is a super user search or not
	 *        see {@link org.kuali.rice.kew.docsearch.DocSearchDTO#isUsingSuperUserSearch()}
	 * @return the fully encoded html for a link using the text from the input parameter 'value'
	 */
	protected DisplayValues getValueEncodedWithDocHandlerUrl(String value, String routeHeaderId, boolean isSuperUserSearch, String documentTypeName) {
		DisplayValues dv = new DisplayValues();
		dv.htmlValue = getDocHandlerUrlPrefix(routeHeaderId,isSuperUserSearch,documentTypeName) + value + getDocHandlerUrlSuffix(isSuperUserSearch);
		dv.userDisplayValue = value;
		return dv;
	}

	private Map<String,Object> getSortValuesMap(DocSearchDTO docCriteriaDTO) {
		Map<String, Object> alternateSort = new HashMap<String, Object>();
		alternateSort.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_HEADER_ID, docCriteriaDTO.getRouteHeaderId());
		alternateSort.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_INITIATOR, docCriteriaDTO.getInitiatorTransposedName());
		alternateSort.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DATE_CREATED, docCriteriaDTO.getDateCreated());
		return alternateSort;
	}

	public Map<String,Boolean> getSortableByKey() {
		if (sortableByKey.isEmpty()) {
			sortableByKey = constructSortableByKey();
		}
		return sortableByKey;
	}

	protected Map<String,Boolean> constructSortableColumnByKey() {
		Map<String,Boolean> sortable = new HashMap<String,Boolean>();
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_HEADER_ID, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOC_TYPE_LABEL, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOCUMENT_TITLE, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_STATUS_DESC, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_INITIATOR, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DATE_CREATED, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_LOG, Boolean.FALSE);
		return sortable;
	}

	public Map<String,Boolean> getSortableColumnByKey() {
		if (sortableByKey.isEmpty()) {
			sortableByKey = constructSortableByKey();
		}
		return sortableByKey;
	}

	protected Map<String,Boolean> constructSortableByKey() {
		Map<String,Boolean> sortable = new HashMap<String,Boolean>();
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_HEADER_ID, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOC_TYPE_LABEL, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DOCUMENT_TITLE, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_STATUS_DESC, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_INITIATOR, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_DATE_CREATED, Boolean.TRUE);
		sortable.put(KEWPropertyConstants.DOC_SEARCH_RESULT_PROPERTY_NAME_ROUTE_LOG, Boolean.FALSE);
		return sortable;
	}

	public Map<String,String> getLabelsByKey() {
		if (labelsByKey.isEmpty()) {
			labelsByKey = constructLabelsByKey();
		}
		return labelsByKey;
	}

	protected Map<String,String> constructLabelsByKey() {
		return new HashMap<String,String>();
	}

	/*
	 * Below columns are for convenience for overriding classes
	 *
	 */

	protected void addColumnUsingKey(List<DocumentSearchColumn> columns,String key) {
		this.addColumnUsingKey(columns, new HashMap<String,String>(), key, null, null);
	}

	protected void addColumnUsingKey(List<DocumentSearchColumn> columns,Map<String,String> displayParameters,String key,String label) {
		this.addColumnUsingKey(columns, displayParameters, key, label, null);
	}

	protected void addColumnUsingKey(List<DocumentSearchColumn> columns,Map<String,String> displayParameters,String key,Boolean sortable) {
		this.addColumnUsingKey(columns, displayParameters, key, null, sortable);
	}

	protected void addColumnUsingKey(List<DocumentSearchColumn> columns,Map<String,String> displayParameters,String key,String label,Boolean sortable) {
		columns.add(this.constructColumnUsingKey(displayParameters, key, label, sortable));
	}

	protected void addSearchableAttributeColumnUsingKey(List<DocumentSearchColumn> columns,String key,String label,Boolean sortableOverride, Boolean defaultSortable) {
	    addSearchableAttributeColumnUsingKey(columns, new HashMap<String,String>(), key, label, sortableOverride, defaultSortable);
	}

	protected void addSearchableAttributeColumnUsingKey(List<DocumentSearchColumn> columns,Map<String,String> displayParameters,String key,String label,Boolean sortableOverride, Boolean defaultSortable) {
	    columns.add(this.constructColumnUsingKey(displayParameters, key, label, (sortableOverride != null) ? sortableOverride : defaultSortable));
	}


	/*
	 * Below methods should probably not be overriden by overriding classes but could be if desired
	 */

	protected DocumentSearchColumn constructColumnUsingKey(Map<String,String> displayParameters, String key,String label,Boolean sortable) {
		if (sortable == null) {
			sortable = getSortableByKey().get(key);
		}
		if (label == null) {
			label = getLabelsByKey().get(key);
		}
		DocumentSearchColumn c = new DocumentSearchColumn(label,((sortable != null) && (sortable.booleanValue())) ? DocumentSearchColumn.COLUMN_IS_SORTABLE_VALUE : DocumentSearchColumn.COLUMN_NOT_SORTABLE_VALUE,"resultContainer(" +key + ").value","resultContainer(" +key + ").sortValue",key,displayParameters);
		return c;
	}

	private boolean isDocumentHandlerPopup() {
		String parameterValue = Utilities.getKNSParameterValue(KEWConstants.DEFAULT_KIM_NAMESPACE, KNSConstants.DetailTypes.DOCUMENT_SEARCH_DETAIL_TYPE, KEWConstants.DOCUMENT_SEARCH_DOCUMENT_POPUP_IND).trim();
		return (KEWConstants.DOCUMENT_SEARCH_DOCUMENT_POPUP_VALUE.equals(parameterValue));
	}

	private boolean isRouteLogPopup() {
		String parameterValue = Utilities.getKNSParameterValue(KEWConstants.DEFAULT_KIM_NAMESPACE, KNSConstants.DetailTypes.DOCUMENT_SEARCH_DETAIL_TYPE, KEWConstants.DOCUMENT_SEARCH_ROUTE_LOG_POPUP_IND).trim();
		return (KEWConstants.DOCUMENT_SEARCH_ROUTE_LOG_POPUP_VALUE.equals(parameterValue));
	}

	private String getDocHandlerUrlPrefix(String routeHeaderId,boolean superUserSearch,String documentTypeName) {
		String linkPopup = "";
		if (this.isDocumentHandlerPopup()) {
			linkPopup = " target=\"_blank\"";
		}
		if (superUserSearch) {
		    String url = "<a href=\"SuperUser.do?methodToCall=displaySuperUserDocument&routeHeaderId=" + routeHeaderId + "\"" + linkPopup + " >";
		    if (!getDocumentType(documentTypeName).getUseWorkflowSuperUserDocHandlerUrl().getPolicyValue().booleanValue()) {
			url = "<a href=\"" + KEWConstants.DOC_HANDLER_REDIRECT_PAGE + "?" + KEWConstants.COMMAND_PARAMETER + "=" + KEWConstants.SUPERUSER_COMMAND + "&" + KEWConstants.ROUTEHEADER_ID_PARAMETER + "=" + routeHeaderId + "\"" + linkPopup + ">";
		    }
		    return url;
		} else {
			return "<a href=\"" + KEWConstants.DOC_HANDLER_REDIRECT_PAGE + "?" + KEWConstants.COMMAND_PARAMETER + "=" + KEWConstants.DOCSEARCH_COMMAND + "&" + KEWConstants.ROUTEHEADER_ID_PARAMETER + "=" + routeHeaderId + "\"" + linkPopup + ">";
		}
	}

	private String getDocHandlerUrlSuffix(boolean superUserSearch) {
		if (superUserSearch) {
			return "</a>";
		} else {
			return "</a>";
		}
	}
}
