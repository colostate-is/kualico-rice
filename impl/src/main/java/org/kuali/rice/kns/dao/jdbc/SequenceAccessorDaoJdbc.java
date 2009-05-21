/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.rice.kns.dao.jdbc;

import javax.persistence.EntityManager;

import org.apache.ojb.broker.PersistenceBroker;
import org.kuali.rice.kns.dao.SequenceAccessorDao;

/**
 * This class uses the KualiDBPlatform to get the next number from a given sequence.
 */
public class SequenceAccessorDaoJdbc extends PlatformAwareDaoBaseJdbc implements SequenceAccessorDao {
	private EntityManager entityManager;
	private PersistenceBroker broker;
	
    /**
     * @see org.kuali.rice.kns.dao.SequenceAccessorDao#getNextAvailableSequenceNumber(java.lang.String)
     */
    public Long getNextAvailableSequenceNumber(String sequenceName) {
    	if ( broker != null )
    		return getDbPlatform().getNextValSQL(sequenceName, broker);
    	else if ( entityManager != null ) 
    		return getDbPlatform().getNextValSQL(sequenceName, entityManager);
    	else
    		throw new RuntimeException("Either broker or entityManager must be set");
    }

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public void setPersistenceBroker(PersistenceBroker broker) {
		this.broker = broker;
	}
}