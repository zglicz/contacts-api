# contacts-api
## Simple contacts and skills management

Allows contacts (users) to create their contact information and manage their own skills.

Authentication uses Basis Authentication (username + password).

Authorization allows all GET requests - so anyone can browse all of the resources. However, all mutations require an authenticated user.
Also, we enforce more granular mutation access to only allow for the authenticated user to mutate their resources.

Included are basic validations like:
- All skills need a unique name
- A contact cannot have multiple skills of the same type
- Firstname, lastname size needs to be within a certain range

## Getting Started

### Tests
`./mvnw test`

### Running locally from Intellij
`./mvnw spring-boot:run`

### Functionality

To see exposed endpoints see: (http://localhost:8080/swagger-ui/index.html#/)

System consists of 3 main categories:
- `/contacts/` - endpoint to load, create, delete contacts
- `/skills/` - endpoint to add skills
- `/contacts/{id}/skills` - endpoint to mutate a single contacts skill set
