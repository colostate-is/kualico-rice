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
package org.kuali.rice.kew.docsearch.xml;

import org.kuali.rice.kew.docsearch.DocumentSearchResultProcessor;
import org.kuali.rice.kew.rule.RuleAttribute;


/**
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public interface DocumentSearchXMLResultProcessor extends DocumentSearchResultProcessor {
	
	public static final boolean DEFAULT_SHOW_ALL_STANDARD_FIELDS_VALUE = false;
	public static final boolean DEFAULT_OVERRIDE_SEARCHABLE_ATTRIBUTES_VALUE = true;

	public void setRuleAttribute(RuleAttribute ruleAttribute);

}
