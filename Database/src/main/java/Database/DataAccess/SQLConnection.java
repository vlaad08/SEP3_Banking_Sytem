package Database.DataAccess;

import Database.AccountsInfo;
import Database.DTOs.LoanRequestDTO;
import Database.DTOs.UserInfoAccNumDTO;
import Database.DTOs.UserInfoEmailDTO;
import Database.Transactions;
import Database.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SQLConnection implements SQLConnectionInterface{
    private static SQLConnection instance;
    protected SQLConnection() throws SQLException {
        DriverManager.registerDriver(new org.postgresql.Driver());
    }
    public static SQLConnection getInstance() throws SQLException
    {
        if (instance ==null) {
            instance = new SQLConnection();
        }
        return instance;
    }
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres?currentSchema=banking_system","postgres","password");
    }
    /** Database manipulator method, to make the transfer in the database with the given details**/
    @Override
    public void transfer(String id_1, String id_2, double amount, String message) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement updateStatement1 = connection.prepareStatement(
                    "UPDATE account SET balance = balance + ? WHERE account_id = ?")) {

                updateStatement1.setDouble(1, amount);
                updateStatement1.setString(2, id_2);
                updateStatement1.executeUpdate();

                try (PreparedStatement updateStatement2 = connection.prepareStatement(
                        "UPDATE account SET balance = balance - ? WHERE account_id = ?")) {

                    updateStatement2.setDouble(1, amount);
                    updateStatement2.setString(2, id_1);
                    updateStatement2.executeUpdate();

                    try (PreparedStatement insertStatement = connection.prepareStatement(
                            "INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) " +
                                    "VALUES (?, ?, ?, ?, ?)")) {

                        insertStatement.setTimestamp(1, now);
                        insertStatement.setDouble(2, amount);
                        insertStatement.setString(3, message);
                        insertStatement.setString(4, id_1);
                        insertStatement.setString(5, id_2);
                        insertStatement.executeUpdate();

                        connection.commit();
                    } catch (SQLException e) {
                        connection.rollback();
                        throw new RuntimeException("Error executing insert statement", e);
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    throw new RuntimeException("Error executing second update statement", e);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error executing first update statement", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error opening/closing connection", e);
        }
    }

    /* Database query method, to make the query for the available balance for the given account_id*/
    @Override
    public double checkBalance(String account_id) throws SQLException {
        double balance=0;
        try (Connection connection= getConnection())
        {
            PreparedStatement statement=connection.prepareStatement("SELECT balance FROM account WHERE account_id = ?;");
            statement.setString(1,account_id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                balance = result.getDouble("balance");
            }
        }
        return balance;
    }

    @Override
    public String checkAccountId(String account_id) throws SQLException {
        String recipientAccount_id = "-";
        try(Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT account_id FROM account WHERE account_id = ?;");
            statement.setString(1, account_id);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                recipientAccount_id = result.getString("account_id");
            }
        }
        return recipientAccount_id;
    }

    @Override
    public double dailyCheck(String account_id) throws SQLException {
        double amount = 0;
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT SUM(amount)\n" +
                    "FROM transactions\n" +
                    "WHERE senderAccount_id = ?\n" +
                    "  AND DATE_TRUNC('day', dateTime) = CURRENT_DATE;");
            statement.setString(1,account_id);
            ResultSet result = statement.executeQuery();
            while (result.next())
            {
                amount = result.getDouble("sum");
            }
        }
        return amount;
    }

    @Override
    public void deposit(String account_id, double amount) throws SQLException {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE account SET balance = balance + ? WHERE account_id = ?")) {

                updateStatement.setDouble(1, amount);
                updateStatement.setString(2, account_id);
                updateStatement.executeUpdate();

                try (PreparedStatement insertStatement = connection.prepareStatement(
                        "INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) " +
                                "VALUES (?, ?, ?, ?, ?)")) {

                    insertStatement.setTimestamp(1, now);
                    insertStatement.setDouble(2, amount);
                    //insertStatement.setNull(3, Types.VARCHAR); //message will be null
                    insertStatement.setString(3,"deposit");//actully no this s better
                    insertStatement.setString(4, account_id);
                    insertStatement.setString(5, account_id); // deposit to himself?
                    insertStatement.executeUpdate();

                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<User> getUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try(Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement("SELECT *\n" +
                    "FROM \"user\"");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next())
            {
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");
                String firstName = resultSet.getString("firstname");
                String middleName = resultSet.getString("middlename");
                String lastName = resultSet.getString("lastname");
                String role = resultSet.getString("role");

                User user = User.newBuilder().setEmail(email).setPassword(password).setFirstName(firstName).setMiddleName(middleName).
                setLastName(lastName).setRole(role).build();
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public List<AccountsInfo> getAccountsInfo() throws SQLException {
        List<AccountsInfo> accountsInfoList = new ArrayList<>();
        try (Connection connection = getConnection()) {
            String query = "SELECT a.account_id, u.firstname, u.lastname, a.balance, a.account_type " +
                    "FROM account a JOIN \"user\" u ON a.user_id = u.user_id;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next())
                {
                    String accountNumber = resultSet.getString("account_id");
                    String ownerName = resultSet.getString("firstname") + " " + resultSet.getString("lastname");
                    double accountBalance = resultSet.getDouble("balance");
                    String accountType = resultSet.getString("account_type");

                    AccountsInfo accountsInfo = AccountsInfo.newBuilder().setAccountNumber(accountNumber).setOwnerName(ownerName).setAccountBalance(accountBalance).setAccountType(accountType).build();
                    accountsInfoList.add(accountsInfo);
                }
            }
        }
        return accountsInfoList;
    }

    public List<AccountsInfo> getUserAccountInfos(UserInfoEmailDTO userInfoDTO) throws SQLException {

        List<AccountsInfo> accountsInfoList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            String query = "SELECT a.account_id, u.firstname, u.lastname, a.balance, a.account_type " +
                    "FROM account a JOIN \"user\" u ON a.user_id = u.user_id " +
                    "WHERE u.email = ?;";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userInfoDTO.getEmail());
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String accountNumber = resultSet.getString("account_id");
                    String ownerName = resultSet.getString("firstname") + " " + resultSet.getString("lastname");
                    double accountBalance = resultSet.getDouble("balance");
                    String accountType = resultSet.getString("account_type");

                    AccountsInfo accountsInfo = AccountsInfo.newBuilder().setAccountNumber(accountNumber).setOwnerName(ownerName).setAccountBalance(accountBalance).setAccountType(accountType).build();
                    accountsInfoList.add(accountsInfo);
                }
            }
        }
        return accountsInfoList;
    }

    @Override
    //gpt enhanced code not tested
    public boolean creditInterest(UserInfoAccNumDTO userInfoAccNumDTO) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try {
                PreparedStatement selectStatement = connection.prepareStatement(
                        "SELECT a.balance, a.interest_rate FROM account a WHERE a.account_id = ?");

                selectStatement.setString(1, userInfoAccNumDTO.getAccNum());
                ResultSet resultSet = selectStatement.executeQuery();

                if (resultSet.next()) {
                    double balance = resultSet.getDouble("balance");
                    double interestRate = resultSet.getDouble("interest_rate");

                    double interest = balance * interestRate;

                    PreparedStatement updateStatement = connection.prepareStatement(
                            "UPDATE account SET balance = balance + ? WHERE account_id = ?");

                    updateStatement.setDouble(1, interest);
                    updateStatement.setString(2, userInfoAccNumDTO.getAccNum());
                    int updatedRows = updateStatement.executeUpdate();

                    if (updatedRows > 0) {  // Check if the update was successful
                        PreparedStatement insertStatement = connection.prepareStatement(
                                "INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) " +
                                        "VALUES (?, ?, ?, ?, ?)");

                        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                        insertStatement.setTimestamp(1, now);
                        insertStatement.setDouble(2, interest);
                        insertStatement.setString(3, "Interest");
                        insertStatement.setString(4, userInfoAccNumDTO.getAccNum());
                        insertStatement.setString(5, userInfoAccNumDTO.getAccNum());
                        insertStatement.executeUpdate();

                        connection.commit();
                        return true;  // Return true indicating success
                    } else {
                        throw new RuntimeException("Update failed. No rows affected.");
                    }
                } else {
                    throw new RuntimeException("Account not found");
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error executing statements", e);
            }
        } catch (SQLException e) {
            return false;  // Return false in case of an exception while opening/closing connection
        }
    }

    @Override
    public Timestamp lastInterest(UserInfoAccNumDTO userInfoAccNumDTO) throws SQLException {
        Timestamp lastInterestTimestamp = null;

        try (Connection connection = getConnection()) {
            String query = "SELECT t.dateTime " +
                    "FROM transactions t " +
                    "WHERE t.senderAccount_id = ? AND t.message = 'Interest' " +
                    "ORDER BY t.dateTime DESC LIMIT 1;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userInfoAccNumDTO.getAccNum());
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    lastInterestTimestamp = resultSet.getTimestamp("dateTime");
                }
            }
        }

        return lastInterestTimestamp;
    }

    public void logLoan(LoanRequestDTO loanRequestDTO) throws SQLException {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try {
                PreparedStatement insertAccountStatement = connection.prepareStatement(
                        "INSERT INTO loan(account_id, remaining_amount, interest_rate, monthly_payment, end_date, loan_amount) " +
                                "VALUES (?, ?, ?, ?, ?, ?)");
                java.sql.Timestamp sqlEndDate = new java.sql.Timestamp(loanRequestDTO.getEndDate().getSeconds() * 1000);
                insertAccountStatement.setString(1, loanRequestDTO.getAccountId());
                insertAccountStatement.setDouble(2, loanRequestDTO.getRemainingAmount());
                insertAccountStatement.setDouble(3, loanRequestDTO.getInterestRate());
                insertAccountStatement.setDouble(4, loanRequestDTO.getMonthlyPayment());
                insertAccountStatement.setTimestamp(5, sqlEndDate);
                insertAccountStatement.setDouble(6, loanRequestDTO.getLoanAmount());

                insertAccountStatement.executeUpdate();

                PreparedStatement insertTransactionStatement = connection.prepareStatement(
                        "INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) " +
                                "VALUES (?, ?, ?, ?, ?)");

                Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                insertTransactionStatement.setTimestamp(1, now);
                insertTransactionStatement.setDouble(2, loanRequestDTO.getLoanAmount());
                insertTransactionStatement.setString(3, "Loan");
                insertTransactionStatement.setString(4, loanRequestDTO.getAccountId());
                insertTransactionStatement.setString(5, loanRequestDTO.getAccountId());

                insertTransactionStatement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Error executing statements", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error opening/closing connection", e);
        }
    }

    @Override
    public List<Transactions> getAllTransactions(UserInfoEmailDTO userInfoEmailDTO) {
        List<Transactions> transactionsList = new ArrayList<>();

        try (Connection connection = getConnection()) {
            String query = "SELECT t.senderAccount_id, t.recipientAccount_id, t.amount, t.message, t.dateTime, u1.firstName AS senderFirstName, u1.lastName AS senderLastName, u2.firstName AS receiverFirstName, u2.lastName AS receiverLastName " +
                    "FROM transactions t " +
                    "JOIN account a1 ON t.senderAccount_id = a1.account_id " +
                    "JOIN account a2 ON t.recipientAccount_id = a2.account_id " +
                    "JOIN \"user\" u1 ON a1.user_id = u1.user_id " +
                    "JOIN \"user\" u2 ON a2.user_id = u2.user_id " +
                    "WHERE u1.email = ? OR u2.email = ? " +
                    "ORDER BY t.dateTime DESC;";

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, userInfoEmailDTO.getEmail());
                statement.setString(2, userInfoEmailDTO.getEmail());

                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    String senderAccountNumber = resultSet.getString("senderAccount_id");
                    String recipientAccountNumber = resultSet.getString("recipientAccount_id");
                    double amount = resultSet.getDouble("amount");
                    String message = resultSet.getString("message");
                    java.sql.Timestamp sqlTimestamp = resultSet.getTimestamp("dateTime");
                    String senderFirstName = resultSet.getString("senderFirstName");
                    String senderLastName = resultSet.getString("senderLastName");
                    String receiverFirstName = resultSet.getString("receiverFirstName");
                    String receiverLastName = resultSet.getString("receiverLastName");
                    com.google.protobuf.Timestamp date = com.google.protobuf.Timestamp.newBuilder()
                            .setSeconds(sqlTimestamp.getTime() / 1000)
                            .setNanos((int) ((sqlTimestamp.getTime() % 1000) * 1_000_000))
                            .build();

                    Transactions transaction = Transactions.newBuilder()
                            .setSenderAccountNumber(senderAccountNumber)
                            .setRecipientAccountNumber(recipientAccountNumber)
                            .setAmount(amount)
                            .setMessage(message)
                            .setDate(date)
                            .setSenderName(senderFirstName + " " + senderLastName)
                            .setReceiverName(receiverFirstName + " " + receiverLastName)
                            .build();

                    transactionsList.add(transaction);
                }
            }
        } catch (SQLException e) {
            // Handle SQLException
            throw new RuntimeException("Error executing statements", e);
        }

        return transactionsList;
    }






}
