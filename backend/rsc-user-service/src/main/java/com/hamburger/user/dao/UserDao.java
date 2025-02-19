package com.hamburger.user.dao;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hamburger.user.dao.entity.User;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public interface UserDao {
    void save(User user);
    void delete(String id);
    User findById(String id);
    User findByUserName(String userName);

    @Repository
    public class UserDaoAWSDynamoDB implements UserDao {

        private final DynamoDbTable<User> userTable;
        private final DynamoDbClient dynamoDbClient;

        @Autowired
        public UserDaoAWSDynamoDB(DynamoDbClient dynamoDbClient, DynamoDbEnhancedClient enhancedDynamoDbClient) {
            this.userTable = enhancedDynamoDbClient.table("rsc-localhost-user-data", TableSchema.fromBean(User.class));
            this.dynamoDbClient = dynamoDbClient;
        }

        @Override
        public void save(User user) {
            user.setId(UUID.randomUUID().toString());
            userTable.putItem(user);
        }

        @Override
        public void delete(String id) {
            User user = findById(id);
            if (user != null) {
                userTable.deleteItem(user);
            }
        }

        @Override
        public User findById(String id) {
            return userTable.getItem(r -> r.key(k -> k.partitionValue(id)));
        }

        @Override
        public User findByUserName(String userName) {
            String indexName = "UserNameIndex";
            String tableName = "rsc-localhost-user-data";
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":userName", AttributeValue.builder().s(userName).build());

            QueryRequest queryRequest = QueryRequest.builder()
                .tableName(tableName)
                .indexName(indexName)
                .keyConditionExpression("userName = :userName")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
            if (queryResponse.items().isEmpty()) {
                return null;
            }
            System.out.println(queryResponse.items().get(0));
            Map<String, AttributeValue> resp = queryResponse.items().get(0);
            return User.builder()
                .id(resp.get("id").s())
                .userName(resp.get("userName").s())
                .email(resp.get("email").s())
                .hashedPassword(resp.get("hashedPassword").s())
                .build();
        }
    }
}