package com.hamburger.user.dao;

import com.hamburger.user.dao.entity.ResetPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ResetPasswordDaoAWSDynamoDBTest {
    @Mock
    private DynamoDbEnhancedClient enhancedDynamoDbClient;
    @Mock
    private DynamoDbTable<ResetPassword> tokenTable;

    @InjectMocks
    private ResetPasswordDao.ResetPasswordDaoAWSDynamoDB resetPasswordDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(enhancedDynamoDbClient.table(anyString(), any(TableSchema.class))).thenReturn(tokenTable);
        resetPasswordDao = new ResetPasswordDao.ResetPasswordDaoAWSDynamoDB(enhancedDynamoDbClient);
    }

    @Test
    void testSave() {
        ResetPassword resetPassword = new ResetPassword("name@email.com", "valid_token", 123456789L);
        resetPasswordDao.save(resetPassword);
        verify(tokenTable, times(1)).putItem(resetPassword);
    }

    @Test
    void testDeleteEmailSuccess() {
        ResetPassword resetPassword = new ResetPassword("name@email.com", "valid_token", 123456789L);
        ResetPasswordDao.ResetPasswordDaoAWSDynamoDB spyDao = spy(resetPasswordDao);
        doReturn(resetPassword).when(spyDao).findByEmail("name@email.com");
        spyDao.delete("name@email.com");
        verify(tokenTable, times(1)).deleteItem(eq(resetPassword));
    }

    @Test
    void testDeleteEmailError() {
        ResetPasswordDao.ResetPasswordDaoAWSDynamoDB spyDao = spy(resetPasswordDao);
        doReturn(null).when(spyDao).findByEmail("name2@email.com");
        spyDao.delete("name2@email.com");
        verify(tokenTable, never()).deleteItem(any(ResetPassword.class));
    }

    @Test
    void testFindByEmailError() {
        when(tokenTable.getItem(any(Key.class))).thenReturn(null);
        ResetPassword found = resetPasswordDao.findByEmail("name@email.com");
        assertNull(found);
    }
}
