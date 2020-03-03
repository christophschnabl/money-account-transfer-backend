# Money Transfers

## Requirements (as defined in the task sheet)
1. Kotlin is used
2. For information about design decisions see [Design Decisions](#Design Decisions)
3. The API can be invoked by multiply users concurrently
4. Used Frameworks
    * Spark Microframework
    * Guice 
    * Testing
        * Mockito Kotlin
        * Junit 
        * OkHttp as HttpClient (any client or none at all would work)
5. A ConcurrentHashMap is used in combination with simple OptimisticLocking - see [Concurrency](#Concurrency)
6. Executable as a fat jar:
    * Build:
     ```mvn package```
     * Run:
     `` java -jar target/money-account-transfer-backend-1.0-SNAPSHOT-jar-with-dependencies.jar``
7. 
    * Unit Tests follow a behavior driven style (given, when, then) without the additional complexity of a framework such as cucumber.
    * Integration Testing is down by spinning up the embedded spark server and validating against expected json data.
   

## Design Decisions 

### Constraints
* In this example money is viewed as an imaginary unit. No currency conversion is provided for the scope of the backend test. 
However one unit of money can easily be interpreted as one cent, pence, ... . This avoids many problems in comparison to depicting money as floating point numbers.
* No top-ups/withdraw from ATM events are implemented as this example just focuses on money transfer. However, accounts can be initialized with a starting balance. 
* Logging is done on a request level, but not further down the application. Detailed Logging is necessary in a real world scenario but would only add more unreasonable time effort in this task. 

### Packaging
On a first hierarchy a package by feature is used as I see is little benefit here in depending on different layers.
### Event Sourcing & CQRS

Event sourcing is used to model changes of state, a simplified (no separate services, same datastore) Command Query Responsibility Separation for sending commands and querying aggregates. 
API Calls send Commands that trigger Events. Events are then applied on an aggregate to reconstruct current balances. 

### Concurrency

A simple ConcurrentHashMap is used in combination with optimistic locking as an in memory store to not depend on another external library. This is a very simple version of Optimistic Locking, so I would suggest to use a Pessimistic version when running transfers in production e.g. paired with a database such as H2. 

### Event Order

Event Order is guaranteed by using versioning (and optimistic locking), as timestamps are unreliable and lamport vector clocks to complex for this purpose. 

## API

Example Requests in curl are defined here. For usage with postman the collection is attached below:

One example scenario (happy path) could be as followed:
1. [Create](#Create Account) Account A with a balance of 42 and Account B with a balance of 0
3. [Transfer](#Transfer Money from Account A to Account B) 23 from A to B
4. [Query](#Get Account by Uuid) Account A and Account B and check their balances
5. [List](#Get Transfers for one Account) Transfers for Account A or B

### Create Account
```curl -X POST -H "Content-Type: application-json" -d "{'balance': 100}" localhost:4567/accounts```

### Get Account by Uuid

```curl localhost:4567/accounts/<uuid>```

### Transfer Money from Account A to Account B
```curl -X POST -H "Content-Type: application-json" -d "{"from": <A-uuid>,"to": <B-uuid>,"amount":100}" localhost:4567/transfers```

### Get Transfers for one Account
```curl localhost:4567/accounts/<uuid>/transfers```

### Exceptions

Exceptions are serialized and mapped to http status code e.g. `UserNotFoundException` -> `404`

### Postman Collection

Save as `money-account-transfer-api.postman_collection.json` and import it in postman
```
{
	"info": {
		"_postman_id": "1efb6e5a-6bfe-4aff-88ba-b020ef736427",
		"name": "money-account-transfer-api",
		"schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
	},
	"item": [
		{
			"name": "Get Account",
			"request": {
				"method": "GET",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": ""
				},
				"url": {
					"raw": "localhost:4567/accounts/:uuid",
					"host": [
						"localhost"
					],
					"port": "4567",
					"path": [
						"accounts",
						":uuid"
					],
					"variable": [
						{
							"key": "uuid",
							"value": "17d1a2c2-20a6-4490-902a-bb9653a31443"
						}
					]
				}
			},
			"response": []
		},
		{
			"name": "Create Account",
			"request": {
				"method": "POST",
				"header": [
					{
						"key": "Content-Type",
						"value": "application/json",
						"type": "text"
					}
				],
				"body": {
					"mode": "raw",
					"raw": "{\"balance\": 42}"
				},
				"url": {
					"raw": "localhost:4567/accounts",
					"host": [
						"localhost"
					],
					"port": "4567",
					"path": [
						"accounts"
					]
				}
			},
			"response": []
		},
		{
			"name": "Create Transfer",
			"request": {
				"method": "POST",
				"header": [
					{
						"key": "Content-Type",
						"value": "application/json",
						"type": "text"
					}
				],
				"body": {
					"mode": "raw",
					"raw": "{\"from\": \"17d1a2c2-20a6-4490-902a-bb9653a31443\",\"to\": \"55588086-8349-4626-99b8-88e476beddf6\", \"amount\": 1}"
				},
				"url": {
					"raw": "localhost:4567/transfers",
					"host": [
						"localhost"
					],
					"port": "4567",
					"path": [
						"transfers"
					]
				}
			},
			"response": []
		},
		{
			"name": "Get Account Transfers",
			"request": {
				"method": "GET",
				"header": [],
				"body": {
					"mode": "raw",
					"raw": ""
				},
				"url": {
					"raw": "localhost:4567/accounts/:id/transfers",
					"host": [
						"localhost"
					],
					"port": "4567",
					"path": [
						"accounts",
						":id",
						"transfers"
					],
					"variable": [
						{
							"key": "id",
							"value": "55588086-8349-4626-99b8-88e476beddf6"
						}
					]
				}
			},
			"response": []
		}
	],
	"event": [
		{
			"listen": "prerequest",
			"script": {
				"id": "f53ebd76-ecd9-49d5-9695-519094f3a221",
				"type": "text/javascript",
				"exec": [
					""
				]
			}
		},
		{
			"listen": "test",
			"script": {
				"id": "f7222dd2-bcf9-414c-8f36-3626a5cd67e5",
				"type": "text/javascript",
				"exec": [
					""
				]
			}
		}
	]
}
```
