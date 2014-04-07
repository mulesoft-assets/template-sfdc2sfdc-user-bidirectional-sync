# Anypoint Template: Salesforce to Salesforce bi-directional user sync

+ [Use case](#usecase)
+ [Template overview](#templateoverview)
+ [Run it!](#runit)
    * [Properties to be configured](#propertiestobeconfigured)
    * [Running on CloudHub](#runoncloudhub)
    * [Running on premise](#runonopremise)
+ [Customize It!](#customizeit)
    * [config.xml](#configxml)
    * [endpoints.xml](#endpointsxml)
    * [businessLogic.xml](#businesslogicxml)
    * [errorHandling.xml](#errorhandlingxml)

## Use case <a name="usecase"/>

As a Salesforce admin, I want to have my users synchronized between two different Salesforce organizations

## Template overview <a name="templateoverview"/>

Let's say we want to keep Salesforce instance *A* synchronized with Salesforce instance *B*. Then, the integration behavior can be summarized just with the following steps:

1. Ask Salesforce *A*:
> *Which changes have there been since the last time I got in touch with you?*

2. For each of the updates fetched in the previous step (1.), ask Salesforce *B*:
> *Does the update received from A should be applied?*

3. If Salesforce answer for the previous question (2.) is *Yes*, then *upsert* (create or update depending each particular case) B with the belonging change

4. Repeat previous steps (1. to 3.) the other way around (using *B* as source instance and *A* as the target one)

 Repeat *ad infinitum*:

5. Ask Salesforce *A*:
> *Which changes have there been since the question I've made in the step 1.?*

And so on...
  
  
The question for recent changes since a certain moment in nothing but a [poll inbound][1] with a [watermark][2] defined.


# Run it! <a name="runit"/>

In order to have the template up and running just complete the two following steps:

 1. [Configure the application properties](#propertiestobeconfigured)
 2. Run it! ([on premise](#runonopremise) or [in Cloudhub](#runoncloudhub))


## Properties to be configured<a name="propertiestobeconfigured"/>

### Application configuration
+ polling.frequency `10000`  
This are the miliseconds (also different time units can be used) that will run between two different checks for updates in Salesforce

+ watermark.default.expression `2014-02-25T11:00:00.000Z`  
This property is an important one, as it configures what should be the start point of the synchronization.The date format accepted in SFDC Query Language is either *YYYY-MM-DDThh:mm:ss+hh:mm* or you can use Constants. [More information about Dates in SFDC][3]

### SalesForce Connector configuration for company A
+ sfdc.a.username `jorge.drexler@mail.com`
+ sfdc.a.password `Noctiluca123`
+ sfdc.a.securityToken `avsfwCUl7apQs56Xq2AKi3X`
+ sfdc.a.url `https://login.salesforce.com/services/Soap/u/28.0`

### SalesForce Connector configuration for company B
+ sfdc.b.username `mariano.cozzi@mail.com`
+ sfdc.b.password `LaRanitaDeLaBicicleta456`
+ sfdc.b.securityToken `ces56arl7apQs56XTddf34X`
+ sfdc.b.url `https://login.salesforce.com/services/Soap/u/28.0`



## Running on CloudHub <a name="runoncloudhub"/>

Running the template on CloudHub is as simple as follow the 4 steps detailed on the following documetation page: 
  
> [http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub](http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub)

## Running on premise <a name="runonopremise"/>
Once all properties are filled in one of the template property files (for example in [mule.prod.properties][4]) the template can be run by just choosing an enviromet and follow the steps detailed in the link placed below:

> [http://www.mulesoft.org/documentation/display/current/Hello+World+on+CloudHub](http://www.mulesoft.org/documentation/display/current/Deploying+Mule+Applications)

# Customize It!<a name="customizeit"/>
This brief guide intends to give a high level idea of how this template is built and how you can change it according to your needs.
As mule applications are based on XML files, the idea is describing each XML included in the template.
Of course more files will be found such as Test Classes and [Mule Application Files][5], but to keep it simple we will focus on the XMLs.

Here is a list of the main XML files you'll find in this application:

* [config.xml](#configxml)
* [endpoints.xml](#endpointsxml)
* [businessLogic.xml](#businesslogicxml)
* [errorHandling.xml](#errorhandlingxml)


## config.xml<a name="configxml"/>
This file holds the configuration for Connectors and [Properties Place Holders][6]. 
Although you can update the configuration properties here, we highly recommend to keep them parametrized and modified them in the belonging property files.

For this particular template, what you will find in the config file is
* the configuration for the two Salesforce instances that are being synced
* the property place holder configuration

In order to find the mentioned configuration, you should check out the [*Global Element* tab][7].


## endpoints.xml<a name="endpointsxml"/> 
This file should contain every inbound and outbound endpoint of your integration app. It is intented to contain the application API.
In this particular template, this file contains a couple of poll inbound endpoints that query salesforce for updates using watermark as mentioned before.

## businessLogic.xml<a name="businesslogicxml"/>
This file holds the functional aspect of the template (points 2. to 4. described in the [template overview](#templateoverview)). Its main component is a [*Batch job*][8], and it includes *steps* for both executing the synchronization from Salesforce instance A to Salesforce instance B, and the other way around.


## errorHandling.xml<a name="errorhandlingxml"/>
This is the right place to handle how your integration will react depending on the different exceptions. 
This file holds a [Choice Exception Strategy][9] that should be referenced by any flow included in the business logic.


  [1]: http://www.mulesoft.org/documentation/display/current/Poll+Reference
  [2]: http://blogs.mulesoft.org/data-synchronizing-made-easy-with-mule-watermarks/
  [3]: http://www.salesforce.com/us/developer/docs/officetoolkit/Content/sforce_api_calls_soql_select_dateformats.htm
  [4]: https://github.com/mulesoft/template-sfdc2sfdc-user-bidirectional-sync/blob/master/src/main/resources/mule.prod.properties
  [5]: http://www.mulesoft.org/documentation/display/current/Application+Format
  [6]: http://www.mulesoft.org/documentation/display/current/Configuring+Properties
  [7]: http://www.mulesoft.org/documentation/display/current/Global+Elements
  [8]: http://www.mulesoft.org/documentation/display/current/Batch+Processing
  [9]: http://www.mulesoft.org/documentation/display/current/Choice+Exception+Strategy
  
  
