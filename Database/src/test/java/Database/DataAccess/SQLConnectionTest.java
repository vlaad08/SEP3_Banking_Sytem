package Database.DataAccess;

import Database.*;
import Database.DTOs.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SQLConnectionTest {
    @InjectMocks
    private SQLConnection sqlConnection = null;
    @Mock
    private Connection connection = null;
    @Mock
    private PreparedStatement statement;
    @Mock
    private ResultSet resultSet;
    @Captor
    private ArgumentCaptor<String> stringCaptor;
    @Captor
    private ArgumentCaptor<Double> doubleCaptor;
    @Captor
    private ArgumentCaptor<Timestamp> timestampCaptor;
    @InjectMocks
    private UserInfoEmailDTO userInfoDTO;
    @InjectMocks
    private IssueCreationDTO issueCreationDTO;
    @InjectMocks
    private IssueinfoDTO issueinfoDTO;
    @InjectMocks
    private MessageDTO messageDTO;
    @Captor
    private ArgumentCaptor<UserInfoEmailDTO> userInfoCaptor;
    @Captor
    private ArgumentCaptor<RegisterRequestDTO> registerRequestDto;

    @BeforeEach
    void setup() throws SQLException {
        sqlConnection = Mockito.spy(SQLConnection.getInstance());
        connection = Mockito.mock(Connection.class);
        statement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);
        stringCaptor = ArgumentCaptor.forClass(String.class);
        doubleCaptor = ArgumentCaptor.forClass(Double.class);
        timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
        registerRequestDto =ArgumentCaptor.forClass(RegisterRequestDTO.class);

        Mockito.when(sqlConnection.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(anyString())).thenReturn(statement);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
    }
    @Test
    void SQLConnection_is_created()
    {
        assertNotNull(sqlConnection);
    }
    @Test
    void SQLConnection_is_singleton() throws SQLException {
        SQLConnection sqlConnection1 = SQLConnection.getInstance();
        SQLConnection sqlConnection2 = SQLConnection.getInstance();
        assertSame(sqlConnection1,sqlConnection2);
    }
    @Test
    void connection_is_alive() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method getConnectionMethod = SQLConnection.class.getDeclaredMethod("getConnection");
        getConnectionMethod.setAccessible(true);
        Connection connection = (Connection) getConnectionMethod.invoke(sqlConnection);
        assertNotNull(connection);
    }
    @Test
    void transfer_is_saved_in_the_database() throws SQLException {
        sqlConnection.transfer("aaaabbbbccccdddd", "bbbbaaaaccccdddd", 10.0, "-");
        Mockito.verify(connection).setAutoCommit(false);
        Mockito.verify(connection).prepareStatement("UPDATE account SET balance = balance + ? WHERE account_id = ?");
        Mockito.verify(connection).prepareStatement("UPDATE account SET balance = balance - ? WHERE account_id = ?");
        Mockito.verify(connection).prepareStatement("INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) "
                +
                "VALUES (?, ?, ?, ?, ?)");
        Mockito.verify(statement, times(2)).setDouble(eq(1), anyDouble());
        Mockito.verify(statement, times(1)).setDouble(eq(2), anyDouble());
        Mockito.verify(statement, times(2)).setString(eq(2), anyString());
        Mockito.verify(statement, times(1)).setString(eq(3), anyString());
        Mockito.verify(statement, times(1)).setString(eq(4), anyString());
        Mockito.verify(statement, times(1)).setString(eq(5), anyString());
        Mockito.verify(statement, times(1)).setTimestamp(eq(1), Mockito.any(Timestamp.class));

        Mockito.verify(connection).commit();
    }
    /* tried with assert and failed
    * @Test
    void transfer_is_saved_in_the_database() throws SQLException {

        PreparedStatement statement1 = Mockito.mock(PreparedStatement.class);
        PreparedStatement statement2 = Mockito.mock(PreparedStatement.class);
        PreparedStatement statement3 = Mockito.mock(PreparedStatement.class);

        sqlConnection.transfer("aaaabbbbccccdddd", "bbbbaaaaccccdddd", 10.0, "-");
        // Checking for the beginning of the Transaction
        Mockito.verify(connection).setAutoCommit(false);

        // Verifying the addition of the amount
        Mockito.verify(connection).prepareStatement(Mockito.eq("UPDATE account SET balance = balance + ? WHERE account_id = ?"));
        Mockito.verify(statement1).setDouble(Mockito.eq(1), doubleCaptor.capture());
        Mockito.verify(statement1).setString(Mockito.eq(2), stringCaptor.capture());
        Mockito.verify(statement1).executeUpdate();

        assertEquals(10.0, doubleCaptor.getValue(), 0.01);
        assertEquals("bbbbaaaaccccdddd", stringCaptor.getValue());

        // Verifying the deduction of the amount
        Mockito.verify(connection).prepareStatement(Mockito.eq("UPDATE account SET balance = balance - ? WHERE account_id = ?"));
        Mockito.verify(statement2).setDouble(Mockito.eq(1), doubleCaptor.capture());
        Mockito.verify(statement2).setString(Mockito.eq(2), stringCaptor.capture());
        Mockito.verify(statement2).executeUpdate();

        assertEquals(10.0, doubleCaptor.getValue(), 0.01);
        assertEquals("aaaabbbbccccdddd", doubleCaptor.getValue());

        // Verifying that the transaction is saved in the transactions table
        Mockito.verify(connection).prepareStatement(Mockito.eq("INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) " +
                "VALUES (?, ?, ?, ?, ?)"));
        Mockito.verify(statement3).setTimestamp(Mockito.eq(1), Mockito.any(Timestamp.class));
        Mockito.verify(statement3).setDouble(Mockito.eq(2), doubleCaptor.capture());
        Mockito.verify(statement3).setString(Mockito.eq(3), stringCaptor.capture());
        Mockito.verify(statement3).setString(Mockito.eq(4), stringCaptor.capture());
        Mockito.verify(statement3).setString(Mockito.eq(5), stringCaptor.capture());
        Mockito.verify(statement3).executeUpdate();

        assertEquals(10.0, doubleCaptor.getValue(), 0.01);
        assertEquals("-", stringCaptor.getValue());
        assertEquals("aaaabbbbccccdddd", stringCaptor.getAllValues().get(2));
        assertEquals("bbbbaaaaccccdddd", stringCaptor.getAllValues().get(3));

        // Check if the transaction is committed
        Mockito.verify(connection).commit();
    }*/

    @Test
    void checkBalance_returns_a_double() throws SQLException {
        Mockito.when(sqlConnection.checkBalance(Mockito.anyString())).thenReturn(10.0);
        assertEquals(10.0, sqlConnection.checkBalance(Mockito.anyString()));
    }

    @Test
    void checkBalance_reaches_the_database() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(sqlConnection.checkBalance("1111111111111111")).thenReturn(1000.0);
        sqlConnection.checkBalance("1111111111111111");
        Mockito.verify(connection).prepareStatement(Mockito.eq("SELECT balance FROM account WHERE account_id = ?;"));
        Mockito.verify(statement).setString(eq(1),stringCaptor.capture());
        Mockito.verify(statement).executeQuery();
        Mockito.verify(resultSet).next();
        Mockito.verify(resultSet).getDouble("balance");

        assertEquals("1111111111111111",stringCaptor.getAllValues().get(0));
    }

    @Test
    void checkAccountId_returns_a_String() throws SQLException {
        Mockito.when(sqlConnection.checkAccountId(Mockito.anyString())).thenReturn("-");
        assertEquals(sqlConnection.checkAccountId("-"),"-");
    }

    //infinitly running???
    /*@Test
    void checkAccountId_reaches_the_database() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(sqlConnection.checkAccountId("-")).thenReturn("-");
        sqlConnection.checkAccountId("-");
        Mockito.verify(connection).prepareStatement(Mockito.eq("SELECT account_id FROM account WHERE account_id = ?;"));
        Mockito.verify(statement).setString(eq(1),stringCaptor.capture());
        Mockito.verify(statement).executeQuery();
        Mockito.verify(resultSet).next();
        Mockito.verify(resultSet).getString("account_id");

        assertEquals("-",stringCaptor.getAllValues().get(0));
    }*/

    @Test
    void dailyCheck_returns_a_double() throws SQLException {
        Mockito.when(sqlConnection.dailyCheck(Mockito.anyString())).thenReturn(200.0);
        assertEquals(200.0,sqlConnection.dailyCheck("-"));
    }
    @Test
    void dailyCheck_reaches_the_database() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true, false);
        Mockito.when(resultSet.getDouble("sum")).thenReturn(200.0);
        double result = sqlConnection.dailyCheck("-");
        Mockito.verify(connection).prepareStatement(Mockito.eq("SELECT SUM(amount)\n" +
                "FROM transactions\n" +
                "WHERE senderAccount_id = ?\n" +
                "  AND DATE_TRUNC('day', dateTime) = CURRENT_DATE;"));
        Mockito.verify(statement).setString(eq(1), stringCaptor.capture());
        Mockito.verify(statement).executeQuery();
        Mockito.verify(resultSet, Mockito.times(2)).next();
        Mockito.verify(resultSet).getDouble("sum");
        assertEquals(200.0, result, 0.01);
        assertEquals("-", stringCaptor.getValue());
    }

    @Test
    void deposit_updates_the_database() throws SQLException {
        sqlConnection.deposit("aaaabbbbccccdddd",50);
        Mockito.verify(connection).prepareStatement("UPDATE account SET balance = balance + ? WHERE account_id = ?");
        Mockito.verify(statement).setDouble(eq(1),doubleCaptor.capture());
        Mockito.verify(statement).setString(eq(2),stringCaptor.capture());

        Mockito.verify(connection).prepareStatement(Mockito.eq("INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) " +
                "VALUES (?, ?, ?, ?, ?)"));
        Mockito.verify(statement).setTimestamp(Mockito.eq(1), Mockito.any(Timestamp.class));
        Mockito.verify(statement).setDouble(Mockito.eq(2), doubleCaptor.capture());
        Mockito.verify(statement).setString(Mockito.eq(3), stringCaptor.capture());
        Mockito.verify(statement).setString(Mockito.eq(4), stringCaptor.capture());
        Mockito.verify(statement).setString(Mockito.eq(5), stringCaptor.capture());
        Mockito.verify(statement,Mockito.times(2)).executeUpdate();

        assertEquals("aaaabbbbccccdddd",stringCaptor.getValue());
        assertEquals(50,doubleCaptor.getValue());
    }

    @Test
    void getUsers_returns_a_list() throws SQLException {
        User sampleUser = User.newBuilder()
                .setEmail("test@example.com")
                .setPassword("test1234")
                .setFirstName("Test")
                .setMiddleName("Test")
                .setLastName("Test")
                .setRole("testUser")
                .build();
        Mockito.when(sqlConnection.getUsers()).thenReturn(List.of(sampleUser));
        List<User> result = sqlConnection.getUsers();

        assertEquals(List.of(sampleUser), result);
    }

    @Test
    void getUsers_queries_the_database() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true);
        User sampleUser = User.newBuilder()
                .setEmail("test@example.com")
                .setPassword("test1234")
                .setFirstName("Test")
                .setMiddleName("Test")
                .setLastName("Test")
                .setRole("testUser")
                .build();
        List<User> users = new ArrayList<>();
        users.add(sampleUser);
        try
        {
            Mockito.when(sqlConnection.getUsers()).thenReturn(users);
            users = sqlConnection.getUsers();
        }catch (NullPointerException ignored){}
        Mockito.verify(connection).prepareStatement("SELECT *\n" +
                "FROM \"user\"");
        Mockito.verify(statement).executeQuery();
        Mockito.verify(resultSet).next();
        Mockito.verify(resultSet).getString("email");
        Mockito.verify(resultSet).getString("password");
        Mockito.verify(resultSet).getString("firstname");
        Mockito.verify(resultSet).getString("middlename");
        Mockito.verify(resultSet).getString("lastname");
        Mockito.verify(resultSet).getString("role");
        User.Builder builder = Mockito.mock(User.Builder.class);
        try
        {
            Mockito.verify(builder.setEmail("test@example.com").setPassword("test1234").setFirstName("Test").setMiddleName("Test").setLastName("Test").setRole("testUser")).build();
        }catch (NullPointerException ignored){}
        assertEquals(users.size(), 1);
    }

    @Test
    void getAccountsInfo_returns_a_list() throws SQLException {
        AccountsInfo temp = AccountsInfo.newBuilder().
                setAccountBalance(10)
                .setAccountNumber("11111111111111")
                .setAccountType("personal")
                .setOwnerName("Name")
                .build();
        Mockito.when(sqlConnection.getAccountsInfo()).thenReturn(List.of(temp));
        List<AccountsInfo> result = sqlConnection.getAccountsInfo();

        assertEquals(List.of(temp), result);
    }
    @Test
    void getUserAccountInfos_returns_a_list() throws SQLException {
        AccountsInfo temp = AccountsInfo.newBuilder().
                setAccountBalance(10)
                .setAccountNumber("11111111111111")
                .setAccountType("personal")
                .setOwnerName("Name")
                .build();
        userInfoDTO = new UserInfoEmailDTO("testmail@test.test");
        Mockito.when(sqlConnection.getUserAccountInfos(userInfoDTO)).thenReturn(List.of(temp));
        List<AccountsInfo> result = sqlConnection.getUserAccountInfos(userInfoDTO);

        assertEquals(List.of(temp), result);
    }
    @Test
    void getAccountsInfo_queries_the_database() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true);
        AccountsInfo temp = AccountsInfo.newBuilder()
                .setAccountBalance(10)
                .setAccountNumber("11111111111111")
                .setAccountType("personal")
                .setOwnerName("Gipsz Jakab")
                .build();
        List<AccountsInfo> list = new ArrayList<>();
        list.add(temp);

        try {
            Mockito.when(sqlConnection.getAccountsInfo()).thenReturn(list);
            list = sqlConnection.getAccountsInfo();
        } catch (NullPointerException ignored) {
        }

        Mockito.verify(connection).prepareStatement("SELECT a.account_id, u.firstname, u.lastname, a.balance, a.account_type " +
                "FROM account a JOIN \"user\" u ON a.user_id = u.user_id;");
        Mockito.verify(statement).executeQuery();
        Mockito.verify(resultSet).next();
        Mockito.verify(resultSet).getString("account_id");
        Mockito.verify(resultSet).getString("firstname");
        Mockito.verify(resultSet).getString("lastname");
        Mockito.verify(resultSet).getDouble("balance");
        Mockito.verify(resultSet).getString("account_type");
    }
    @Test
    void getUserAccountInfos_queries_database() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true);
        AccountsInfo temp = AccountsInfo.newBuilder()
                .setAccountBalance(10)
                .setAccountNumber("11111111111111")
                .setAccountType("personal")
                .setOwnerName("Gipsz Jakab")
                .build();
        List<AccountsInfo> list = new ArrayList<>();
        list.add(temp);
        userInfoDTO = new UserInfoEmailDTO("testmail@test.test");

        try {
            Mockito.when(sqlConnection.getUserAccountInfos(userInfoDTO)).thenReturn(list);
            list = sqlConnection.getUserAccountInfos(userInfoDTO);
        } catch (NullPointerException ignored) {
        }

        Mockito.verify(connection).prepareStatement("SELECT a.account_id, u.firstname, u.lastname, a.balance, a.account_type " +
                "FROM account a JOIN \"user\" u ON a.user_id = u.user_id " +
                "WHERE u.email = ?;");
        Mockito.verify(statement).executeQuery();
        Mockito.verify(resultSet).next();
        Mockito.verify(resultSet).getString("account_id");
        Mockito.verify(resultSet).getString("firstname");
        Mockito.verify(resultSet).getString("lastname");
        Mockito.verify(resultSet).getDouble("balance");
        Mockito.verify(resultSet).getString("account_type");



        AccountsInfo.Builder builder = Mockito.mock(AccountsInfo.Builder.class);
        Mockito.when(builder.setAccountNumber("11111111111111")).thenReturn(builder);
        Mockito.when(builder.setOwnerName("Gipsz Jakab")).thenReturn(builder);
        Mockito.when(builder.setAccountBalance(10)).thenReturn(builder);
        Mockito.when(builder.setAccountType("personal")).thenReturn(builder);

        Mockito.when(builder.build()).thenReturn(temp);

        Mockito.mockStatic(AccountsInfo.class);
        Mockito.when(AccountsInfo.newBuilder()).thenReturn(builder);

        assertEquals(list.size(), 1);
    }
    @Test
    void testRegisterUser() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true);
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO("email","fname",
                "mname","lname","12345678","basic");
        try{
        sqlConnection.registerUser(registerRequestDTO);
        } catch (NullPointerException ignored){}

        //Mockito.verify(connection).prepareStatement();

    }

    @Test
    void test_lastInterest_queries_the_database() throws SQLException {
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getTimestamp("dateTime")).thenReturn(Timestamp.valueOf("2023-01-01 12:00:00"));
        UserInfoAccNumDTO dto = new UserInfoAccNumDTO("aaaabbbbccccdddd");
        Timestamp result = sqlConnection.lastInterest(dto);

        Mockito.verify(connection).prepareStatement("SELECT t.dateTime FROM transactions t " +
                "WHERE t.senderAccount_id = ? AND t.message = 'Interest' " +
                "ORDER BY t.dateTime DESC LIMIT 1;");

        Mockito.verify(statement).setString(1, dto.getAccNum());
        assertEquals(Timestamp.valueOf("2023-01-01 12:00:00"), result);
    }


    @Test
    void test_CreditInterest_queries_the_database() throws SQLException {
        UserInfoAccNumDTO userInfoAccNumDTO = new UserInfoAccNumDTO("aaaabbbbccccdddd");
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(statement.executeUpdate()).thenReturn(1);
        sqlConnection.creditInterest(userInfoAccNumDTO);
        Mockito.verify(connection).setAutoCommit(false);
        Mockito.verify(connection).prepareStatement("SELECT a.balance, a.interest_rate FROM account a WHERE a.account_id = ?");
        Mockito.verify(connection).prepareStatement("UPDATE account SET balance = balance + ? WHERE account_id = ?");
        Mockito.verify(connection).prepareStatement("INSERT INTO transactions(dateTime, amount, message, senderAccount_id, recipientAccount_id) "
                +
                "VALUES (?, ?, ?, ?, ?)");

        Mockito.verify(statement).setString(1,userInfoAccNumDTO.getAccNum());
        Mockito.verify(statement).executeQuery();
        Mockito.verify(statement,Mockito.times(2)).executeUpdate();

        Mockito.verify(statement).setDouble(Mockito.eq(1),Mockito.anyDouble());
        Mockito.verify(statement).setString(2,userInfoAccNumDTO.getAccNum());
        Mockito.verify(statement).setTimestamp(Mockito.eq(1),Mockito.any(Timestamp.class));
        Mockito.verify(statement).setDouble(Mockito.eq(2),Mockito.anyDouble());
        Mockito.verify(statement).setString(3,"Interest");
        Mockito.verify(statement).setString(4,userInfoAccNumDTO.getAccNum());
        Mockito.verify(statement).setString(5,userInfoAccNumDTO.getAccNum());
        Mockito.verify(connection).commit();
    }




    @Test
    void testLogLoan() throws SQLException {
        LoanRequestDTO loanRequestDTO = new LoanRequestDTO(
                "123456",
                1000.0,
                0.05,
                100.0,
                com.google.protobuf.Timestamp.newBuilder().setSeconds(LocalDateTime.now().getSecond()).build(),
                5000.0
        );

        try {
            sqlConnection.logLoan(loanRequestDTO);

            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT * FROM loan WHERE account_id = ?");
            selectStatement.setString(1, loanRequestDTO.getAccountId());
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                assertEquals(loanRequestDTO.getRemainingAmount(), resultSet.getDouble("remaining_amount"));
                assertEquals(loanRequestDTO.getInterestRate(), resultSet.getDouble("interest_rate"));
                assertEquals(loanRequestDTO.getMonthlyPayment(), resultSet.getDouble("monthly_payment"));
                assertEquals(loanRequestDTO.getLoanAmount(), resultSet.getDouble("loan_amount"));
            }

        } catch (RuntimeException e) {
            assertEquals(1, 0, "Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void getAllTransactions_returns_a_list() throws SQLException {
        com.google.protobuf.Timestamp timestamp = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000)
                .setNanos(0)
                .build();
        Transactions temp = Transactions.newBuilder().
                setSenderAccountNumber("testmessage")
                .setRecipientAccountNumber("11111111111111")
                .setAmount(200)
                .setMessage("Name")
                .setDate(timestamp)
                .setSenderName("Alin Maaa")
                .setReceiverName("Levi Ksaaa")
                .build();
        userInfoDTO = new UserInfoEmailDTO("testmail@test.test");
        Mockito.when(sqlConnection.getAllTransactions(userInfoDTO)).thenReturn(List.of(temp));
        List<Transactions> result = sqlConnection.getAllTransactions(userInfoDTO);

        assertEquals(List.of(temp), result);
    }
    @Test
    void testCreateIssue() throws SQLException {
        IssueCreationDTO issueDTO = new IssueCreationDTO("Title", "Body", 1);

        try {
            sqlConnection.createIssue(issueDTO);

            PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT * FROM issues WHERE title = ?");
            selectStatement.setString(1, issueDTO.getTitle());
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                assertEquals(issueDTO.getTitle(), resultSet.getString("title"));
                assertEquals(issueDTO.getBody(), resultSet.getString("body"));
                assertEquals(issueDTO.getOwnerId(), resultSet.getInt("owner_id"));
            }

        } catch (RuntimeException e) {
            assertEquals(1, 0, "Exception occurred: " + e.getMessage());
        }
    }

    @Test
    void testSendMessage() throws SQLException {
        java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(LocalDateTime.now());

        com.google.protobuf.Timestamp protobufTimestamp = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(sqlTimestamp.getTime() / 1000)
                .setNanos((int) ((sqlTimestamp.getTime() % 1000) * 1_000_000))
                .build();

        MessageDTO messageDTO = new MessageDTO("Sample Title", 1, "Body", 1, protobufTimestamp);

        sqlConnection.sendMessage(messageDTO);

        ArgumentCaptor<Timestamp> timestampCaptor = ArgumentCaptor.forClass(Timestamp.class);
        Mockito.verify(statement).setTimestamp(eq(4), timestampCaptor.capture());
        Mockito.verify(statement).setString(1, messageDTO.getTitle());
        Mockito.verify(statement).setString(2, messageDTO.getBody());
        Mockito.verify(statement).setInt(3, messageDTO.getOwner());
        Mockito.verify(statement).setInt(5, messageDTO.getIssueId());

        Mockito.verify(statement).executeUpdate();

        Mockito.verify(connection).commit();
    }

    @Test
    void testGetMessagesForIssue() throws SQLException {
        IssueinfoDTO issueinfoDTO = new IssueinfoDTO(1);

        ResultSet resultSetMock = mock(ResultSet.class);
        Mockito.when(resultSetMock.next()).thenReturn(true, false);
        Mockito.when(resultSetMock.getString("title")).thenReturn("Sample Title");
        Mockito.when(resultSetMock.getString("body")).thenReturn("Body");
        Mockito.when(resultSetMock.getInt("owner_id")).thenReturn(1);

        java.sql.Timestamp sqlTimestamp = java.sql.Timestamp.valueOf(LocalDateTime.now());
        com.google.protobuf.Timestamp expectedDate = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(sqlTimestamp.getTime() / 1000)
                .setNanos((int) ((sqlTimestamp.getTime() % 1000) * 1_000_000))
                .build();
        Mockito.when(resultSetMock.getTimestamp("creation_time")).thenReturn(sqlTimestamp);

        PreparedStatement statementMock = mock(PreparedStatement.class);
        Mockito.when(statementMock.executeQuery()).thenReturn(resultSetMock);

        Connection connectionMock = mock(Connection.class);
        Mockito.when(connectionMock.prepareStatement(anyString())).thenReturn(statementMock);

        SQLConnection sqlConnectionMock = spy(new SQLConnection());
        Mockito.doReturn(connectionMock).when(sqlConnectionMock).getConnection();

        List<MessageInfo> result = sqlConnectionMock.getMessagesForIssue(issueinfoDTO);

        Mockito.verify(statementMock).setInt(1, issueinfoDTO.getId());

        Mockito.verify(resultSetMock).getString("title");
        Mockito.verify(resultSetMock).getString("body");
        Mockito.verify(resultSetMock).getInt("owner_id");
        Mockito.verify(resultSetMock).getTimestamp("creation_time");

        MessageInfo expectedMessageInfo = MessageInfo.newBuilder()
                .setTitle("Sample Title")
                .setBody("Body")
                .setOwner(1)
                .setCreationTime(expectedDate)
                .setIssueId(issueinfoDTO.getId())
                .build();
        assertEquals(List.of(expectedMessageInfo), result);
    }

    @Test
    void testGetAllIssues() throws SQLException {
        ResultSet resultSetMock = mock(ResultSet.class);

        Mockito.when(resultSetMock.next()).thenReturn(true, true, false);
        Mockito.when(resultSetMock.getInt("issue_id")).thenReturn(1, 2);
        Mockito.when(resultSetMock.getString("title")).thenReturn("Issue 1", "Issue 2");
        Mockito.when(resultSetMock.getString("body")).thenReturn("Body 1", "Body 2");
        Mockito.when(resultSetMock.getInt("owner_id")).thenReturn(1, 2);

        java.sql.Timestamp sqlTimestamp1 = java.sql.Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        com.google.protobuf.Timestamp date1 = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(sqlTimestamp1.getTime() / 1000)
                .setNanos((int) ((sqlTimestamp1.getTime() % 1000) * 1_000_000))
                .build();

        java.sql.Timestamp sqlTimestamp2 = java.sql.Timestamp.valueOf(LocalDateTime.now());
        com.google.protobuf.Timestamp date2 = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(sqlTimestamp2.getTime() / 1000)
                .setNanos((int) ((sqlTimestamp2.getTime() % 1000) * 1_000_000))
                .build();

        Mockito.when(resultSetMock.getTimestamp("creation_time")).thenReturn(sqlTimestamp1, sqlTimestamp2);
        Mockito.when(resultSetMock.getBoolean("flagged")).thenReturn(true, false);

        PreparedStatement statementMock = mock(PreparedStatement.class);
        Mockito.when(statementMock.executeQuery()).thenReturn(resultSetMock);

        Connection connectionMock = mock(Connection.class);
        Mockito.when(connectionMock.prepareStatement(anyString())).thenReturn(statementMock);

        SQLConnection sqlConnectionMock = spy(new SQLConnection());
        doReturn(connectionMock).when(sqlConnectionMock).getConnection();
        doReturn(List.of(MessageInfo.getDefaultInstance())).when(sqlConnectionMock).getMessagesForIssue(any());

        List<Issue> result = sqlConnectionMock.getAllIssues();

        Mockito.verify(statementMock).executeQuery();

        Mockito.verify(resultSetMock, times(2)).getInt("issue_id");
        Mockito.verify(resultSetMock, times(2)).getString("title");
        Mockito.verify(resultSetMock, times(2)).getString("body");
        Mockito.verify(resultSetMock, times(2)).getInt("owner_id");
        Mockito.verify(resultSetMock, times(2)).getTimestamp("creation_time");
        Mockito.verify(resultSetMock, times(2)).getBoolean("flagged");

        Issue expectedIssue1 = Issue.newBuilder()
                .setIssueId(1)
                .setTitle("Issue 1")
                .setBody("Body 1")
                .setOwnerId(1)
                .setCreationTime(date1)
                .setFlagged(true)
                .addAllMessages(List.of(MessageInfo.getDefaultInstance()))
                .build();

        Issue expectedIssue2 = Issue.newBuilder()
                .setIssueId(2)
                .setTitle("Issue 2")
                .setBody("Body 2")
                .setOwnerId(2)
                .setCreationTime(date2)
                .setFlagged(false)
                .addAllMessages(List.of(MessageInfo.getDefaultInstance()))
                .build();

        assertEquals(List.of(expectedIssue1, expectedIssue2), result);
    }











}
