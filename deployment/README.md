# Deployment Resources

## environments

A number of configuration files exist for various development setups:

| Environment | Description |
| ----------- | ----------- |
| dev | Local development of the API component |
| test | Used by API tests, which use Wiremock as a mock Authorization Server |
| docker-local | Used to test Docker deployment for the API |
| kubernetes-local | An end-to-end deployment of SPA, API and token handler components that runs in a KIND cluster |

## docker

The main API Dockerfile used in all Docker and Kubernetes deployment scenarios.

## docker-local

Scripts for local standalone Docker deployment of this API component:

```bash
cd deployment/docker-local
./build.sh
./deploy.sh
./teardown.sh
```

## kubernetes-local

Scripts invoked using parent scripts from the [Cloud Native Deployment](https://github.com/gary-archer/oauth.cloudnative.deployment) project.
