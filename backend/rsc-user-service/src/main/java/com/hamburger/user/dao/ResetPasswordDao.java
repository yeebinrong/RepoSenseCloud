package com.hamburger.user.dao;

import com.hamburger.user.dao.entity.ResetPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public interface ResetPasswordDao {
    void save(ResetPassword email);
    void delete(String email);
    ResetPassword findByEmail(String email);

    @Repository
    public class ResetPasswordDaoAWSDynamoDB implements ResetPasswordDao {
        private final DynamoDbTable<ResetPassword> tokenTable;

        @Autowired
        public ResetPasswordDaoAWSDynamoDB(DynamoDbEnhancedClient enhancedDynamoDbClient) {
            this.tokenTable = enhancedDynamoDbClient.table("rsc-" + System.getenv("STAGE") +"-reset-password-data", TableSchema.fromBean(ResetPassword.class));
        }

        @Override
        public void save(ResetPassword email) {
            tokenTable.putItem(email);
        }

        @Override
        public void delete(String userEmail) {
            ResetPassword email = findByEmail(userEmail);
            if (email != null) {
                tokenTable.deleteItem(email);
            }
        }

        @Override
        public ResetPassword findByEmail(String email) {
            return tokenTable.getItem(r -> r.key(k -> k.partitionValue(email)));
        }
    }
}
