/**
 * Mule Anypoint Template
 * Copyright (c) MuleSoft, Inc.
 * All rights reserved.  http://www.mulesoft.com
 */

package org.mule.templates.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.MessageExchangePattern;
import org.mule.api.MuleException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.processor.chain.InterceptingChainLifecycleWrapper;
import org.mule.processor.chain.SubflowInterceptingChainLifecycleWrapper;
import org.mule.templates.AbstractTemplatesTestCase;
import org.mule.transport.NullPayload;

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is validating the correct behavior of the flows
 * for this Mule Anypoint Template
 * 
 */
@SuppressWarnings("unchecked")
public class BidirectionalUserSyncFromAtoBTestIT extends AbstractTemplatesTestCase {

	private static final String PATH_TO_TEST_PROPERTIES = "./src/test/resources/mule.test.properties";	
	private static final String ANYPOINT_TEMPLATE_NAME = "user-bidirectional-sync";
	private static final String A_INBOUND_FLOW_NAME = "triggerSyncFromAFlow";
	private static final String B_INBOUND_FLOW_NAME = "triggerSyncFromBFlow";
	private static final int TIMEOUT_MILLIS = 60;

	// TODO - Replace this constant with an email that belongs to some user in the configured sfdc organization
	private static final String USER_TO_UPDATE_EMAIL = "noreply@chatter.salesforce.com";

	private SubflowInterceptingChainLifecycleWrapper updateUserInAFlow;
	private SubflowInterceptingChainLifecycleWrapper updateUserInBFlow;
	private InterceptingChainLifecycleWrapper queryUserFromAFlow;
	private InterceptingChainLifecycleWrapper queryUserFromBFlow;
	private BatchTestHelper batchTestHelper;
	private List<Map<String, Object>> userList; 
	private static String TEST_USER_EMAIL;
	
	@BeforeClass
	public static void beforeTestClass() {
		System.setProperty("page.size", "1000");

		// Set polling frequency to 10 seconds
		System.setProperty("polling.frequency", "10000");

		// Set default water-mark expression to current time
		System.clearProperty("watermark.default.expression");
		DateTime now = new DateTime(DateTimeZone.UTC);
		DateTimeFormatter dateFormat = DateTimeFormat
				.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		System.setProperty("watermark.default.expression",
				now.toString(dateFormat));
	}

	@Before
	public void setUp() throws Exception {
		Properties props = new Properties();
		try {			
			props.load(new FileInputStream(PATH_TO_TEST_PROPERTIES));			
		} catch (Exception e) {
			throw new IllegalStateException(
					"Could not find the test properties file.");
		}		
		TEST_USER_EMAIL  = props.getProperty("test.sfdc.a.user.email");
		stopAutomaticPollTriggering();
		getAndInitializeFlows();
		
		batchTestHelper = new BatchTestHelper(muleContext);
		
		updateTestEntities();
	}

	private void updateTestEntities() throws MuleException, Exception {
		Map<String, Object> userToRetrieveMail = new HashMap<String, Object>();
		userToRetrieveMail.put("Email", TEST_USER_EMAIL);

		Map<String, Object> userToUpdate = (Map<String, Object>) queryUser(userToRetrieveMail, queryUserFromAFlow);
		userList = new ArrayList<Map<String, Object>>();
		userToUpdate.put("isActive", false);
		userList.add(userToUpdate);			
		updateUserInAFlow.process(getTestEvent(userList));		
		
		userToRetrieveMail = new HashMap<String, Object>();
		userToRetrieveMail.put("Email", USER_TO_UPDATE_EMAIL);

		Map<String, Object> userToUpdate1 = (Map<String, Object>) queryUser(userToRetrieveMail, queryUserFromAFlow);
		
		userToUpdate1.remove("type");
		userToUpdate1.remove("Username");
		userToUpdate1.remove("ProfileId");
		userToUpdate1.put("FirstName", ANYPOINT_TEMPLATE_NAME + "-" + System.currentTimeMillis());
		userToUpdate1.put("Title", "Doctor");
		
		userList = new ArrayList<Map<String, Object>>();
		userList.add(userToUpdate1);
		updateUserInAFlow.process(getTestEvent(userList, MessageExchangePattern.REQUEST_RESPONSE));
		userList.add(userToUpdate);
	}

	private void stopAutomaticPollTriggering() throws MuleException {
		stopFlowSchedulers(A_INBOUND_FLOW_NAME);
		stopFlowSchedulers(B_INBOUND_FLOW_NAME);
	}

	private void getAndInitializeFlows() throws InitialisationException {
		// Flow for updating a user in A instance
		updateUserInAFlow = getSubFlow("updateUserInAFlow");
		updateUserInAFlow.initialise();

		// Flow for updating a user in B instance
		updateUserInBFlow = getSubFlow("updateUserInBFlow");
		updateUserInBFlow.initialise();

		// Flow for querying the user in A instance
		queryUserFromAFlow = getSubFlow("queryUserFromAFlow");
		queryUserFromAFlow.initialise();

		// Flow for querying the user in B instance
		queryUserFromBFlow = getSubFlow("queryUserFromBFlow");
		queryUserFromBFlow.initialise();
	}

	@Test
	public void whenUpdatingAnUserInInstanceATheBelongingUserGetsUpdatedInInstanceB()
			throws MuleException, Exception {		
		
		// Execution
		executeWaitAndAssertBatchJob(A_INBOUND_FLOW_NAME);
		Thread.sleep(3000);
		// Assertions
		Map<String, Object> payload = (Map<String, Object>) queryUser(userList.get(0), queryUserFromBFlow);

		assertEquals("The user should have been sync and new name must match", userList.get(0).get("FirstName"), payload.get("FirstName"));
		assertEquals("The user should have been sync and new title must match", userList.get(0).get("Title"), payload.get("Title"));
		
		Object response = queryUser(userList.get(1), queryUserFromBFlow);
		assertTrue("The inactive user should have not been synced", response instanceof NullPayload);

	}

	private Object queryUser(Map<String, Object> user,
			InterceptingChainLifecycleWrapper queryUserFlow)
			throws MuleException, Exception {
		return queryUserFlow
				.process(
						getTestEvent(user,
								MessageExchangePattern.REQUEST_RESPONSE))
				.getMessage().getPayload();
	}

	private void executeWaitAndAssertBatchJob(String flowConstructName)
			throws Exception {

		// Execute synchronization
		runSchedulersOnce(flowConstructName);

		// Wait for the batch job execution to finish
		batchTestHelper.awaitJobTermination(TIMEOUT_MILLIS * 1000, 500);
		batchTestHelper.assertJobWasSuccessful();
	}

}
