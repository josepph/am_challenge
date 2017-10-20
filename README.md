# The Asset Management Digital Challenge

## Summary:

* add functionality for a transfer of money between accounts


## Available functionality:

1) create sample accounts:

    [POST] localhost:18080/v1/accounts

    Sample JSON data to create accounts:
    {"accountId":"Id-123","balance":1000}
    {"accountId":"Id-234","balance":3000}

2) check details of a given account:

    [GET] localhost:18080/v1/accounts/Id-123

3) create sample transfer:

    [POST] localhost:18080/v1/transfers

    Sample JSON data to create a transfer:
    {"accountFromId":"Id-123","accountToId":"Id-234","amount":400}

4) check details of a given transfer:

    [GET] localhost:18080/v1/transfers/{UUID key}


## Considerations:

1) UUID TransferId is automatically created by the service
2) Accounts involved in a transfer need to exist before the transfer is created


## Pending tasks to be production ready:

1) Add persistant database, to keep all data in case the application stops running
2) Security should be added:
    Authentication to ensure user is who he says he is
    Authorization to ensure once a user is logged in the system can access to the operations
    allowed to him
3) Auditory to keep a log of all activities that could need to be traced
4) DevOps to integrate the application in a continuous integration and delivery system.
    Continuous integration to push bits of new development quickly to the main baseline
    Continuous delivery to ensure the code passes tests, quality checks and is ready for deployment


## Deployment:

  This application could be deployed into one of the microservices cloud infrastructures currently available, either a
  private one using spring-cloud or a public one, like AWS or Google Cloud.
  Also, if there is a requirement to hide into which cloud the application is deployed, CloudFoundry could be used.
