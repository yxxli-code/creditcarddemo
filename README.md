# Credit Card Service

A demo of solution for credit card system.

## Background

Build a backend APls for a credit card application system that handles comprehensive evaluations of applicants to determine their eligibility for credit. 
The system should interact with other existing evaluation services to finish the scoring adjustments.
These are the existing services: "Employment Verification Service", "Compliance Check Service", "Risk Evaluation Service" and "Behavior Analysis Service".

## Functional Requirements
 - a set of endpoints to maintain credit card application records.
 - a set of endpoints to upload statement files.
 - a set of endpoints to submit credit card applications for evaluations.
 - a set of endpoints to review evaluation scores.

## Non-functional Requirements
 - avoid unauthenticated access to those protected endpoints
 - avoid cascading failures when down-streaming services are unresponsive
 - ensure data consistency (idempotent check, transaction lock)
 
## Tech Stack
 - Java(SpringBoot, MyBatis)
 - Redis
 - MySQL

## Database Design
   - one emirate can only have one active application request
   - table for application request
   - table for score board
   - table for submission history (ignore, no requirement for this yet)
   - table for scoring history (ignore, no requirement for this yet)

## Design of endpoints 
 - an endpoint to query application request info (/protected/credit-card/info)
 - an endpoint to submit basic info (/protected/credit-card/request)
   - receive primitive fields
   - only allow to update when status=DRAFT or status=REJECTED
   - validate basic input
   - save the application request record to database with status=DRAFT(create if not yet) and create a new score record(if not yet)

 - an endpoint to submit file (/protected/credit-card/bank-statement-file)
   - receive file 
   - only allow to update when status=DRAFT or status=REJECTED
   - suggest save to cloud file system like private AWS S3 bucket for service decoupling

 - an endpoint to get score details (/protected/submission-score)

 - an endpoint to submit the application (/protected/credit-card/submit)
   - begin a transaction
   - update the application record to status=SUBMITTED
   - call "account service" to check "Identity Verification"
   - if the call fails due to network or service unavailable, set status=DRAFT and return to the caller with message.
   - if the call succeeds, 
      - check result, if no, reject the request immediately (set application record status=REJECTED and return to the caller)
      - check result, if yes. go to next process.
   - submit requests to "employment service",
      - if unresponsive or service unavailable, reset the application record to status=DRAFT and return to the caller with message.
      - idempotentId is passed to "employment service" and "employment service" should do idempotent check.
   - submit request to "compliance service", 
      - if unresponsive or service unavailable, reset the application record to status=DRAFT and return to the caller with message.
      - idempotentId is passed to "compliance service" and "compliance service" should do idempotent check.
   - submit request to "risk service" 
      - if unresponsive or service unavailable, reset the application record to status=DRAFT and return to the caller with message.
      - idempotentId is passed to "risk service" and "risk service" should do idempotent check.
   - submit request to "behavior service"
      - if unresponsive or service unavailable, reset the application record to status=DRAFT and return to the caller with message.
      - idempotentId is passed to "behavior service" and "behavior service" should do idempotent check.
   - commit or rollback the transaction accordingly

## Flowcharts
### flowchart of calculating total score
  - read the score board
  - calculate each weighted part
  - sum up the total
  - check the total
     - if >=90 then update db table application with status=STP and publish broadcast event
     - if >=75 and <90 then update db table application with status=NEAR_STP and publish broadcast event
     - if <75 and >=50 then update db table application with status=MANUAL_REVIEW and publish broadcast event
     - if <50 then update db table application with status=REJECTED and publish broadcast event                

### flowchart of webhook endpoint for "employment service" (/webhook/employment-check/result)
   - receive the request
   - if total score is there, ignore the update as idempotent check
   - populate evaluation results and save into score board
   - if all evaluation results are populated, trigger the total score calculation

### flowchart of webhook endpoint for "compliance service" (/webhook/compliance-check/result)
   - receive the request
   - if total score is there, ignore the update as idempotent check
   - populate evaluation results and save into score board
   - if all evaluation results are populated, trigger the total score calculation

### flowchart of webhook endpoint for "risk service" (/webhook/risk-evaluation/result)
   - receive the request
   - if total score is there, ignore the update as idempotent check
   - populate evaluation results and save into score board
   - if all evaluation results are populated, trigger the total score calculation

### flowchart of webhook endpoint "behavior service" (/webhook/behavior-analysis/result)
   - receive the request
   - if total score is there, ignore the update as idempotent check
   - populate evaluation results and save into score board
   - if all evaluation results are populated, trigger the total score calculation


## Design Considerations
  - Assumptions: user authentication and evaluation services are ready, API gateway is ready and responsible for API authentication, URL routing and rate limits.
  - Design a uniform response format of API endpoint(ApiResult)
  - Design an authentication using token to protect the API endpoints so that each call is authenticated
  - Design a set of mocking endpoints and objects since we need to set assumptions that user authentication and evaluation services are ready in blackbox.
  - Use SpringBoot to facilitate the implementation of RESTful endpoints
  - Use MySQL to store the business data
  - Use Redis to support mocking functions and global object locks.
  - No messaging queue is used, since we assume the communications between services are using webhooks. The evaluation services may be provided by internet providers.
  
## Lifecycle status of application request
	DRAFT,
	SUBMITTED,
	STP,
	NEAR_STP,
	MANUAL_REVIEW,
	REJECTED  

## Builds and Tests 
   1. Download project source code
   2. Setup Redis and MySQL
   3. Initialize MySQL with scripts/init.sql
   4. Edit application.yml with proper Redis and MySQL connection settings.
   5. Run the project or Run DemoTest.

## Deliverables
   - Modify .env with Redis and MySQL connection information
   - Run the following command lines in project home.
    -- mvn clean package -Dmaven.test.skip=true
    -- cp ./target/classes/Dockerfile ./target/
    -- cp ./target/classes/.env ./target/
    -- cd  ./target/
    -- docker build -t credit-card-service:latest -f Dockerfile .
    -- docker run -p 8081:8081 --env-file ./.env --name CreditCardService -d credit-card-service:latest
