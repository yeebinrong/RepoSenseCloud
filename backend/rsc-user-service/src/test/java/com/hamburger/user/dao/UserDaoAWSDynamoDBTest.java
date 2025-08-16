package com.hamburger.user.dao;

import com.hamburger.user.dao.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserDaoAWSDynamoDBTest {
    @Mock
    private DynamoDbClient dynamoDbClient;
    @Mock
    private DynamoDbEnhancedClient enhancedDynamoDbClient;
    @Mock
    private DynamoDbTable<User> userTable;

    @InjectMocks
    private UserDao.UserDaoAWSDynamoDB userDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(enhancedDynamoDbClient.table(anyString(), any(TableSchema.class))).thenReturn(userTable);
        userDao = new UserDao.UserDaoAWSDynamoDB(dynamoDbClient, enhancedDynamoDbClient);
    }

    @Test
    void testSave() {
        User user = User.builder().userName("name").email("name@email.com").hashedPassword("hashedPassword").build();
        userDao.save(user);
        verify(userTable).putItem(any(User.class));
        assertNotNull(user.getId());
    }

    @Test
    void testDeleteSuccess() {
        User user = User.builder().id("user").userName("name").email("name@email.com").hashedPassword("hashedPassword").build();
        UserDao.UserDaoAWSDynamoDB spyDao = spy(userDao);
        doReturn(user).when(spyDao).findById("user");
        spyDao.delete("user");
        verify(userTable).deleteItem(user);
    }

    @Test
    void testDeleteError() {
        UserDao.UserDaoAWSDynamoDB spyDao = spy(userDao);
        doReturn(null).when(spyDao).findById("user2");
        spyDao.delete("user2");
        verify(userTable, never()).deleteItem(any(User.class));
    }

    @Test
    void testFindByUserNameSuccess() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s("user").build());
        item.put("userName", AttributeValue.builder().s("name").build());
        item.put("email", AttributeValue.builder().s("name@email.com").build());
        item.put("hashedPassword", AttributeValue.builder().s("hashedPassword").build());
        QueryResponse response = QueryResponse.builder().items(Collections.singletonList(item)).build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);
        User userFound = userDao.findByUserName("name");
        assertNotNull(userFound);
        assertEquals("name", userFound.getUserName());
    }

    @Test
    void testFindByUserNameError() {
        QueryResponse emptyResponse = QueryResponse.builder().items(Collections.emptyList()).build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(emptyResponse);
        User userFound = userDao.findByUserName("name2");
        assertNull(userFound);
    }

    @Test
    void testFindByEmailSuccess() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s("user").build());
        item.put("userName", AttributeValue.builder().s("name").build());
        item.put("email", AttributeValue.builder().s("name@email.com").build());
        item.put("hashedPassword", AttributeValue.builder().s("hashedPassword").build());
        QueryResponse response = QueryResponse.builder().items(Collections.singletonList(item)).build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(response);
        User userFound = userDao.findByEmail("name@email.com");
        assertNotNull(userFound);
        assertEquals("name@email.com", userFound.getEmail());
    }

    @Test
    void testFindByEmailError() {
        QueryResponse emptyResponse = QueryResponse.builder().items(Collections.emptyList()).build();
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(emptyResponse);
        User userFound = userDao.findByEmail("name2@email.com");
        assertNull(userFound);
    }

    @Test
    void testUpdateSuccess() {
        User user = User.builder().id("user").userName("name").email("name@email.com").hashedPassword("Password!1").build();
        UserDao.UserDaoAWSDynamoDB spyDao = spy(userDao);
        doReturn(user).when(spyDao).findByEmail("name@email.com");
        spyDao.update("name@email.com", "newHashedPassword");
        assertEquals("newHashedPassword", user.getHashedPassword());
    }

    @Test
    void testUpdateError() {
        UserDao.UserDaoAWSDynamoDB spyDao = spy(userDao);
        doReturn(null).when(spyDao).findByEmail("name2@email.com");
        spyDao.update("name2@email.com", "newHashedPassword");
        verify(userTable, never()).putItem(any(User.class));
    }
}
