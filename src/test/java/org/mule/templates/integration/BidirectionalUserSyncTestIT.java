package org.mule.templates.integration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.mulesoft.module.batch.BatchTestHelper;

/**
 * The objective of this class is validating the correct behavior of the flows
 * for this Mule Anypoint Template
 * 
 */
@SuppressWarnings("unchecked")
public class BidirectionalUserSyncTestIT extends AbstractTemplatesTestCase {

	private static final String ANYPOINT_TEMPLATE_NAME = "user-bidirectional-sync";
	private static final String A_INBOUND_FLOW_NAME = "triggerSyncFromAFlow";
	private static final String B_INBOUND_FLOW_NAME = "triggerSyncFromBFlow";
	private static final int TIMEOUT_MILLIS = 60;

	// TODO - Replace this constant with an email that belongs to some user in the configured sfdc organization
	private static final String USER_TO_UPDATE_EMAIL = "noreply@chatter.salesforce.com";

	private Map<String, Object> userToUpdate;
	private SubflowInterceptingChainLifecycleWrapper updateUserInAFlow;
	private SubflowInterceptingChainLifecycleWrapper updateUserInBFlow;
	private InterceptingChainLifecycleWrapper queryUserFromAFlow;
	private InterceptingChainLifecycleWrapper queryUserFromBFlow;
	private BatchTestHelper batchTestHelper;

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
	public void setUp() throws MuleException {
		stopAutomaticPollTriggering();
		getAndInitializeFlows();
		
		batchTestHelper = new BatchTestHelper(muleContext);
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
	public void whenUpdatingAnUserInInstanceBTheBelongingUserGetsUpdatedInInstanceA()
			throws MuleException, Exception {

		Map<String, Object> userToRetrieveMail = new HashMap<String, Object>();
		userToRetrieveMail.put("Email", USER_TO_UPDATE_EMAIL);

		userToUpdate = (Map<String, Object>) queryUser(userToRetrieveMail, queryUserFromBFlow);
		
		userToUpdate.remove("type");
		userToUpdate.remove("Username");
		userToUpdate.put("FirstName", ANYPOINT_TEMPLATE_NAME + "-" + System.currentTimeMillis());
		userToUpdate.put("Title", "Doctor");
		
		List<Map<String, Object>> userList = new ArrayList<Map<String, Object>>();
		userList.add(userToUpdate);

		updateUserInBFlow.process(getTestEvent(userList, MessageExchangePattern.REQUEST_RESPONSE));
		
		// Execution
		executeWaitAndAssertBatchJob(B_INBOUND_FLOW_NAME);
		
		// Assertions
		Map<String, Object> payload = (Map<String, Object>) queryUser(userToRetrieveMail, queryUserFromBFlow);

		assertEquals("The user should have been sync and new name must match", userToUpdate.get("FirstName"), payload.get("FirstName"));
		assertEquals("The user should have been sync and new title must match", userToUpdate.get("Title"), payload.get("Title"));
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
