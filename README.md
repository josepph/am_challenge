# The Asset Management Digital Challenge

##Summary:

* add functionality for a transfer of money between accounts

##Available functionality:

1) create sample accounts:

    [POST] localhost:18080/v1/accounts

    {"accountId":"Id-123","balance":1000}
    {"accountId":"Id-234","balance":3000}

2) check details of a given account:

    [GET] localhost:18080/v1/accounts/Id-123

3) create sample transfer:

    [POST] localhost:18080/v1/transfers

    {"transferId":"transf-a","accountFromId":"Id-123","accountToId":"Id-234","amount":400}

4) check details of a given transfer:

    [GET] localhost:18080/v1/transfers/transf-a


##Considerations:

1) TransferId is required to be manually created by the user
2) Accounts involved in a transfer need to exist before the transfer is created

