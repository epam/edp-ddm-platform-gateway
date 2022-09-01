# Platform-gateway

This service provides api gateway functionality for no-trembita requests to third-party registries

### Requests mapping example:
Request to platform gateway GET http://host/registry-target/endpoint with be mapped to GET http://registry-target.registry-rest-api/endpoint

### Local development:
###### Prerequisites:
* data-factory.registry-rest-api is up and running locally
* Vault is up and running locally

###### Configuration:
* Check `src/main/resources/application-local.yaml` and replace if needed all required by Spring Cloud Gateway and Vault values

###### Steps:
1. (Optional) Package application into jar file with `mvn clean package`
2. Add `--spring.profiles.active=local` to application run arguments
3. Run application with your favourite IDE or via `java -jar ...` with jar file, created above

Application starts by default on port 8092

### License
platform-gateway is Open Source software released under the Apache 2.0 license.
