# azure-tools

## Sentimental Analyis of Tweets in Realtime under Azure environment

### Create Event Hub

Follow the steps given below:

1. If you don't already have one, create a new 'Resource Group' under 'Resource groups'.
2. Under 'Create a resource', click on 'Internet of Things' and then on 'Event Hubs'.
3. It will first ask you to 'Create Namespace'. Use 'Standard' under 'Pricing Tier'. Use the same 'Location' as your 'Resource Group'.
4. Click on 'Create' & wait till the Namespace is created.
5. In the 'Dashboard' click on the newly created Namespace.
6. On the top menu bar click on the 'plus' sign next to 'Event Hub'.
7. Under 'Create Event Hub' enter 'Name'. Leave default values for rest, click on 'Create' & wait for event hub to be created.


### Get URL & Key for Sentimental Analysis API

Follow the steps given below:

1. Click on this link: https://tinyurl.com/y7azvm2y
2. Click on 'Save' at the bottom of the window & then on 'Run'.
3. It takes about a minute to finish 'Running'. Wait till it finishes running.
4. Click on 'Deploy Web Service'.
5. Click on 'Test'. Enter 'I love my life' & click on 'Check mark'. It will show the result in the bottom.
6. Under 'REQUEST/RESPONSE' click on 'Excel 2010 or earlier workbook' to save the workbook locally. You will need these values to create a 'Function' in Stream Analytics Job.


### Create Stream Analytics Job

Follow the steps given below:

1. If you don't already have one, create a new 'Cosmos DB Account'. Under 'Create a resource', click on 'Cosmos DB'. Enter unique 'Account Name'. Use 'SQL' under 'API'. Use same 'Location' as your 'Resource Group'.
2. Under 'Create a resource', click on 'Analytics' & then on 'Stream Analytics Job'.
3. Create a new job under same 'Resource Group' & 'Location' & wait for it to get created.
4. In the 'Dashboard' click on the newly created 'Stream Analytics Job'.
5. Click on 'Inputs' under 'Job topology' & then on 'Add stream input'.
6. Select 'Event Hub' in the drop-down box.
7. Give a name to the Input under 'Input alias'.
8. It will pre-populate rest of the values. Confirm them & click on 'Save'.
9. It will test connection & create the input.
10. Click on 'Outputs'under 'Job Topology' & then on 'Add'.
11. In the drop-down box select 'Cosmos DB'.
12. Give a name to the Output under 'Output alias'.
13. Select your 'Account id' if it's not selected. Under 'Database' select 'Create new' & enter name of database.
14. Enter value under 'Collection name pattern'
15. ***IMPORTANT***: Under 'Document id', enter 'id'. This is 'name of the field in output events used to specify document id'. In our case, every tweet has a field called 'id' which is a unique identifier.
16. Click on 'Save' to create the output.
17. Click on 'Functions' & then on 'Add'.
18. In the drop-down, select 'Azure ML'.
19. Enter 'sentiment' as 'Function alias'. Select 'Provide Azure ML manually'.
20. From the workbook saved in step #6 in the previous step, copy 'Web Service URL' value and enter it here under 'URL'.
21. From the same workbook, copy 'Access Key' value and enter it under 'Key'.
22. Click on 'Save'.
23. Click on 'Query' and replace the default query with:  
    ```
    WITH sentiment AS (  
    SELECT id, "user".screenName, text, sentiment(text) as result, retweetedStatus.retweetCount
    FROM <Your Input Alias>
    )  

    SELECT id, screenName, text, result.[Sentiment], result.[Score], retweetCount 
    INTO <Your Output Alias>
    FROM sentiment
    ```
24. Click on 'Save' to save the query.

### Send tweets to the Event Hub

Follow the steps given below:

1. Create an App at: https://apps.twitter.com/
2. Twitter will then provide you values for the following 4 properties which you can use to get a 'Sample' of Tweets in real time:
```
authConsumerKey
authConsumerSecret
authAccessToken
authAccessTokenSecret
```
3. Clone this directory on your machine:
```
git clone https://github.com/ajaychitre/azure-tools.git
```
4. Open [application.properties](./azure-tools/src/main/resources/application.properties & set the following properties:
```
authConsumerKey=<from step 2 above>
authConsumerSecret=<from step 2 above>
authAccessToken=<from step 2 above>
authAccessTokenSecret=<from step 2 above>
namespace=<name of namespace created above>
eventHubName=<name of event hub>
sasKeyName=RootManageSharedAccessKey
sasKey=<primary key of the Namespace under Shared Access Policy>
```
5. Compile & Run Restserver
```
cd azure-tools
mvn clean package
java -jar ./target/azure-tools-1.0-SNAPSHOT.jar
```
This will start sending tweets to the Event Hub. Keep this job running.


### Start Stream Analytics Job

Follow the steps given below:

1. On the 'Dashboard' click on the newly created Stream Analytics Job.
2. Under 'Overview', click on 'Start'. Leave the 'Job output start time' to 'Now' & click on 'Start' button at the bottom.
3. It takes about a minute to start the job.


### Check output in Cosmos Db.

Follow the steps given below:

1. Wait till the Stream Analytics Job is in 'Running' state.
2. Click on 'Azure Cosmos DB' in left menu bar.
3. Click on the 'Account' you created above.
4. Click on the 'Collection' created as 'Output' in your Stream Processing Job.
5. Click on 'New SQL Query' tab. You can now enter queries such as this:
```
SELECT * FROM c where c.sentiment = 'negative' and c.retweetcount > 1000
```

### Cleanup

Follow the steps given below:

1. ***IMPORTANT***: Remember that you get charged by compute resources used so stop & release the resources that you no longer need.
2. Stop the job that sends tweets to the Event Hub.
3. Stop the Stream Analytics Job.
4. If you're simply experimenting, you can 'Delete' the resource group which will delete all resources under it.




