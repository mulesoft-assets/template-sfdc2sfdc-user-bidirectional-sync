
# Anypoint Template: Salesforce to Salesforce User Bidirectional Synchronization	

<!-- Header (start) -->

<!-- Header (end) -->

# License Agreement
This template is subject to the conditions of the <a href="https://s3.amazonaws.com/templates-examples/AnypointTemplateLicense.pdf">MuleSoft License Agreement</a>. Review the terms of the license before downloading and using this template. You can use this template for free with the Mule Enterprise Edition, CloudHub, or as a trial in Anypoint Studio. 
# Use Case
<!-- Use Case (start) -->
This template serves as a foundation for setting an online bi-directional sync of Users between two Salesforce instances with ability to specify filtering criteria.
The main behavior of this template is fetching data for changes (new or modified Users) using scheduler or http component that have occurred in any of the Salesforce instances during a certain defined period of time. For those Users that both have not been updated yet the integration triggers an upsert (update or create depending the case) taking the last modification as the one that should be applied.
<!-- Use Case (end) -->

# Considerations
<!-- Default Considerations (start) -->

<!-- Default Considerations (end) -->

<!-- Considerations (start) -->
There are a couple of things you should take into account before running this kick:

1. **Users cannot be deleted in SalesForce:** For now, the only thing to do regarding users removal is disabling/deactivating them, but this won't make the username available for a new user.
2. **Each user needs to be associated to a Profile:** SalesForce's profiles are what define the permissions the user will have for manipulating data and other users. Each SalesForce account has its own profiles. Check out the next section to define a map between Profile Ids (from the source account to the ones in the target account and the other way around).
3. **Working with sandboxes for the same account**: Although each sandbox should be a completely different environment, Usernames cannot be repeated in different sandboxes, i.e. if you have a user with username *bob.dylan* in *sandbox A*, you will not be able to create another user with username *bob.dylan* in *sandbox B*. If you are indeed working with Sandboxes for the same SalesForce account you will need to map the source username to a different one in the target sandbox, for this purpose, please refer to the processor labeled *assign ProfileId and Username to the User*.
<!-- Considerations (end) -->



## Salesforce Considerations

Here's what you need to know about Salesforce to get this template to work:

- Where can I check that the field configuration for my Salesforce instance is the right one? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=checking_field_accessibility_for_a_particular_field.htm&language=en_US">Salesforce: Checking Field Accessibility for a Particular Field</a>.
- Can I modify the Field Access Settings? How? See: <a href="https://help.salesforce.com/HTViewHelpDoc?id=modifying_field_access_settings.htm&language=en_US">Salesforce: Modifying Field Access Settings</a>.

### As a Data Source

If the user who configured the template for the source system does not have at least *read only* permissions for the fields that are fetched, then an *InvalidFieldFault* API fault displays.

```
java.lang.RuntimeException: [InvalidFieldFault [ApiQueryFault 
[ApiFault  exceptionCode='INVALID_FIELD'
exceptionMessage='Account.Phone, Account.Rating, Account.RecordTypeId, 
Account.ShippingCity
^
ERROR at Row:1:Column:486
No such column 'RecordTypeId' on entity 'Account'. If you are attempting to 
use a custom field, be sure to append the '__c' after the custom field name. 
Reference your WSDL or the describe call for the appropriate names.'
]
row='1'
column='486'
]
]
```

### As a Data Destination

There are no considerations with using Salesforce as a data destination.









# Run it!
Simple steps to get this template running.
<!-- Run it (start) -->

<!-- Run it (end) -->

## Running On Premises
In this section we help you run this template on your computer.
<!-- Running on premise (start) -->

<!-- Running on premise (end) -->

### Where to Download Anypoint Studio and the Mule Runtime
If you are new to Mule, download this software:

+ [Download Anypoint Studio](https://www.mulesoft.com/platform/studio)
+ [Download Mule runtime](https://www.mulesoft.com/lp/dl/mule-esb-enterprise)

**Note:** Anypoint Studio requires JDK 8.
<!-- Where to download (start) -->

<!-- Where to download (end) -->

### Importing a Template into Studio
In Studio, click the Exchange X icon in the upper left of the taskbar, log in with your Anypoint Platform credentials, search for the template, and click Open.
<!-- Importing into Studio (start) -->

<!-- Importing into Studio (end) -->

### Running on Studio
After you import your template into Anypoint Studio, follow these steps to run it:

+ Locate the properties file `mule.dev.properties`, in src/main/resources.
+ Complete all the properties required as per the examples in the "Properties to Configure" section.
+ Right click the template project folder.
+ Hover your mouse over `Run as`.
+ Click `Mule Application (configure)`.
+ Inside the dialog, select Environment and set the variable `mule.env` to the value `dev`.
+ Click `Run`.
<!-- Running on Studio (start) -->

<!-- Running on Studio (end) -->

### Running on Mule Standalone
Update the properties in one of the property files, for example in mule.prod.properties, and run your app with a corresponding environment variable. In this example, use `mule.env=prod`. 


## Running on CloudHub
When creating your application in CloudHub, go to Runtime Manager > Manage Application > Properties to set the environment variables listed in "Properties to Configure" as well as the mule.env value.
<!-- Running on Cloudhub (start) -->

<!-- Running on Cloudhub (end) -->

### Deploying a Template in CloudHub
In Studio, right click your project name in Package Explorer and select Anypoint Platform > Deploy on CloudHub.
<!-- Deploying on Cloudhub (start) -->

<!-- Deploying on Cloudhub (end) -->

## Properties to Configure
To use this template, configure properties such as credentials, configurations, etc.) in the properties file or in CloudHub from Runtime Manager > Manage Application > Properties. The sections that follow list example values.
### Application Configuration
<!-- Application Configuration (start) -->
#### Application configuration

+ scheduler.frequency `10000`  
This are the milliseconds that will run between two different checks for updates in either Salesforce instance

+ scheduler.startDelay `0`

+ watermark.default.expression `2018-02-25T11:00:00.000Z`  
This property is an important one, as it configures what should be the start point of the synchronization. If the use case includes synchronization of every 
user created from the begining of the times, you should use a date previous to any user creation (perhaphs `1900-01-01T08:00:00.000Z` is a good choice). 
If you want to synchronize the users created from now on, then you should use a default value according to that requirement (for example, 
if today is April 21st of 2018 and it's eleven o'clock in London, then you could use the following value `2018-04-21T11:00:00.000Z`).

+ page.size `1000`


#### SalesForce Connector configuration for company A

+ sfdc.a.username `jorge.drexler@mail.com`
+ sfdc.a.password `Noctiluca123`
+ sfdc.a.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.a.integration.user.id `A0ed000BO9T`

	**Note:** To find out the correct *sfdc.a.integration.user.id* value, please, refer to example project **Salesforce Data Retrieval** in [Anypoint Exchange](http://www.mulesoft.org/documentation/display/current/Anypoint+Exchange).

#### SalesForce Connector configuration for company B

+ sfdc.b.username `mariano.cozzi@mail.com`
+ sfdc.b.password `LaRanitaDeLaBicicleta456`
+ sfdc.b.securityToken `ces56arl7apQs56XTddf34X`
+ sfdc.b.integration.user.id `B0ed000BO9T`

	**Note:** To find out the correct *sfdc.b.integration.user.id* value, please, refer to example project **Salesforce Data Retrieval** in [Anypoint Exchange](http://www.mulesoft.org/documentation/display/current/Anypoint+Exchange).

SalesForce's profiles are what define the permissions the user will have for manipulating data and other users.
Each SalesForce account has its own profiles.

This should be a map that associates a profile in sfdc A with a profile in sfdc B
for example: ['SOME_PROFILE_IN_A': 'SOME_PROFILE_IN_B', 'ANOTHER_PROFILE_IN_A': 'ANOTHER_PROFILE_IN_B']

+ sfdc.a.profile.id  `['00e200000015oKF': '00e20000001UzDx']`

This should be a map that associates a profile in sfdc B with a profile in sfdc A
for example: ['SOME_PROFILE_IN_B': 'SOME_PROFILE_IN_A', 'ANOTHER_PROFILE_IN_B': 'ANOTHER_PROFILE_IN_A']

+ sfdc.b.profile.id `['00e200000015oKF': '00e20000001UzDx']`

The meaning of the properties above is defined in the second consideration on [the previous section](#afewconsiderations)

#### Test configuration
email.a.b 'test@test.com'
<!-- Application Configuration (end) -->

# API Calls
<!-- API Calls (start) -->
Not relevant for this use case.
<!-- API Calls (end) -->

# Customize It!
This brief guide provides a high level understanding of how this template is built and how you can change it according to your needs. As Mule applications are based on XML files, this page describes the XML files used with this template. More files are available such as test classes and Mule application files, but to keep it simple, we focus on these XML files:

* config.xml
* businessLogic.xml
* endpoints.xml
* errorHandling.xml<!-- Customize it (start) -->

<!-- Customize it (end) -->

## config.xml
<!-- Default Config XML (start) -->
This file provides the configuration for connectors and configuration properties. Only change this file to make core changes to the connector processing logic. Otherwise, all parameters that can be modified should instead be in a properties file, which is the recommended place to make changes.<!-- Default Config XML (end) -->

<!-- Config XML (start) -->

<!-- Config XML (end) -->

## businessLogic.xml
<!-- Default Business Logic XML (start) -->
This file holds the functional aspect of the template. Its main component is a batch job, and it includes steps for both executing the synchronization from Salesforce A to Salesforce B, and the other way around.<!-- Default Business Logic XML (end) -->

<!-- Business Logic XML (start) -->

<!-- Business Logic XML (end) -->

## endpoints.xml
<!-- Default Endpoints XML (start) -->
This file should contain every inbound and outbound endpoint of your integration app. 
In this particular template, this file contains a scheduler endpoint that query Salesforce A and Salesforce B for updates using watermark and http endpoint for push operation.<!-- Default Endpoints XML (end) -->

<!-- Endpoints XML (start) -->

<!-- Endpoints XML (end) -->

## errorHandling.xml
<!-- Default Error Handling XML (start) -->
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Error Handling](http://www.mulesoft.org/documentation/display/current/Error+Handling) that is referenced by the scheduler flow in the endpoints xml file.<!-- Default Error Handling XML (end) -->

<!-- Error Handling XML (start) -->

<!-- Error Handling XML (end) -->

<!-- Extras (start) -->

<!-- Extras (end) -->
