/**
 * Copyright 2005-2019 The Kuali Foundation
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
package org.kuali.rice.core.framework.persistence.jdbc.dao;

import org.kuali.rice.core.framework.persistence.dao.PlatformAwareDao;
import org.kuali.rice.core.framework.persistence.platform.DatabasePlatform;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

public abstract class PlatformAwareDaoBaseJdbc extends JdbcDaoSupport implements PlatformAwareDao {
    private DatabasePlatform dbPlatform;
    
    public DatabasePlatform getDbPlatform(){
        return dbPlatform;
    }
    
    public void setDbPlatform(DatabasePlatform dbPlatform) {
        this.dbPlatform = dbPlatform;
    }

}
