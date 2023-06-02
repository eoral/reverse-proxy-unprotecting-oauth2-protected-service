## About

**Warning:** It is not recommended to use this application on production, it was created for development purposes.

Let's assume that:
* We have an OAuth2 protected backend service. 
* To be able to call the backend service, we should obtain a Bearer token using client credentials flow and send the token in Authorization header. 
* For development purposes, we want to delegate token handling to a reverse proxy. So, the clients won't deal with token handling.

## Configuration

* Go to **Config** class and change values according to your own environment.

## Requirements

* Java 1.8 or above
