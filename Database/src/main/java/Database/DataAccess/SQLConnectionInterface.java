package Database.DataAccess;

import java.sql.SQLException;

public interface SQLConnectionInterface {
    void transfer(String id_1, String id_2, double amount, String message);
    double checkBalance(String account_id) throws SQLException;
    String checkAccountId(String account_id) throws SQLException;
    double dailyCheck(String account_id)throws SQLException;
    void deposit(String account_id, double amount) throws SQLException;
}
