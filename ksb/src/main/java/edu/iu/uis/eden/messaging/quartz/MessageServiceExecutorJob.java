/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package edu.iu.uis.eden.messaging.quartz;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.kuali.bus.services.KSBServiceLocator;
import org.kuali.rice.RiceConstants;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import edu.iu.uis.eden.messaging.MessageServiceInvoker;
import edu.iu.uis.eden.messaging.PersistedMessage;
import edu.iu.uis.eden.messaging.threadpool.KSBThreadPool;

/**
 * Job saves a {@link PersistedMessage} to the message queue in the state of 'R' and then puts into a
 * {@link MessageServiceInvoker} for execution in {@link KSBThreadPool}.
 * 
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 * 
 */
public class MessageServiceExecutorJob implements Job, Serializable {

    private static final Logger LOG = Logger.getLogger(MessageServiceExecutorJob.class);

    private static final long serialVersionUID = 6702139047380618522L;

    public static final String MESSAGE_KEY = "message";

    public void execute(JobExecutionContext jec) throws JobExecutionException {
	try {
	    PersistedMessage message = (PersistedMessage) jec.getJobDetail().getJobDataMap().get(MESSAGE_KEY);
	    message.setQueueStatus(RiceConstants.ROUTE_QUEUE_ROUTING);
	    KSBServiceLocator.getRouteQueueService().save(message);
	    KSBServiceLocator.getThreadPool().execute(new MessageServiceInvoker(message));
	} catch (Throwable t) {
	    LOG.error("Caught throwable attempting to process message in exception messaging queue.", t);
	    throw new JobExecutionException(new Exception(t));
	}
    }
}