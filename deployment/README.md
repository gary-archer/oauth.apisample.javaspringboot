# Deployment Resources

Resources to manage builds and deployment for this component.

## Environments

A number of configuration files exist for various development setups:

| Environment | Description |
| ----------- | ----------- |
| dev | Local development of the API component |
| test | Used by API tests, which use Wiremock as a mock Authorization Server |
| docker-local | Used to test Docker deployment for the API |
| kubernetes-local | An end-to-end deployment of SPA, API and token handler components that runs in a KIND cluster |
| kubernetes-aws | An end-to-end deployment of SPA, API and token handler components that runs in an AWS cluster |

## Shared

The shared resources include the Dockerfile and are used in multiple deployment scenarios.

## Docker Local

Scripts for local standalone Docker deployment of this API component:

```bash
cd deployment/docker-local
./build.sh
./deploy.sh
./teardown.sh
```

## Kubernetes Local

Scripts invoked using parent scripts from the [Cloud Native Local](https://github.com/gary-archer/oauth.cloudnative.local) project.\
This runs an end-to-end SPA and API setup in a local Kubernetes in Docker (KIND) cluster.

## Kubernetes AWS

Scripts invoked using parent scripts from the [Cloud Native AWS](https://github.com/gary-archer/oauth.cloudnative.aws) project.\
This runs an end-to-end SPA and API setup in a cloud EKS cluster.