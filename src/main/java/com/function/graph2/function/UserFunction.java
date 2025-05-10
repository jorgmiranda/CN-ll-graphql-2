package com.function.graph2.function;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import com.function.graph2.resolver.UserDataFetcher;
import com.microsoft.azure.functions.*;

import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;

public class UserFunction {

    @FunctionName("graphqlHandler")
    public HttpResponseMessage run(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.POST },
            route = "graphql",
            authLevel = AuthorizationLevel.ANONYMOUS
        ) HttpRequestMessage<Optional<Map<String, Object>>> request,
        final ExecutionContext context) {

        try {
            Map<String, Object> body = request.getBody()
                .orElseThrow(() -> new IllegalArgumentException("Body is required"));

            String query = (String) body.get("query");

            if (query == null) {
                throw new IllegalArgumentException("Missing 'query' field in request body");
            }

            GraphQLSchema schema = buildSchema();
            GraphQL build = GraphQL.newGraphQL(schema).build();

            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .build();

            ExecutionResult executionResult = build.execute(executionInput);

            return request.createResponseBuilder(HttpStatus.OK)
                .body(executionResult.toSpecification())
                .build();

        } catch (Exception e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .body("Error: " + e.getMessage())
                .build();
        }
    }

    private GraphQLSchema buildSchema() throws IOException {
        InputStream schemaStream = getClass()
            .getClassLoader()
            .getResourceAsStream("graphql/schema.graphqls");

        if (schemaStream == null) {
            throw new FileNotFoundException("No se encontró el archivo schema.graphqls en resources/graphql");
        }

        String schema = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schema);

        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
            .type("Query", builder -> builder
                // .dataFetcher("getAllUsers", new UserDataFetcher().getAllUsersFetcher())
                .dataFetcher("getUsersByEmail", new UserDataFetcher().getUsersByEmailFetcher())
                .dataFetcher("getUserById", new UserDataFetcher().getUserByIdFetcher()))
            .type("Mutation", builder -> builder
                // .dataFetcher("createUser", new UserDataFetcher().createUserFetcher())
                .dataFetcher("updateUser", new UserDataFetcher().updateUserFetcher())
                .dataFetcher("deleteUser", new UserDataFetcher().deleteUserFetcher())
                )
            .build();

        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, wiring);
    }
}
