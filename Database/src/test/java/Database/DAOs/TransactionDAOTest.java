package Database.DAOs;
import Database.DAOs.Interfaces.TransactionDaoInterface;
import Database.DTOs.*;
import Database.DataAccess.SQLConnection;
import Database.DataAccess.SQLConnectionInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionDAOTest {

    @InjectMocks
    private TransactionDaoInterface dao;
    @Mock
    private SQLConnectionInterface connection;
    private UpdatedBalancesForTransferDTO transferRequestDTO;
    private CheckAccountDTO checkAccountDTO;
    private DepositRequestDTO depositRequestDTO;
    private UserInfoAccNumDTO userInfoAccNumDTO;
    private CreditInterestDTO creditInterestDTO;
    private LoanRequestDTO loanRequestDTO;
    private UserInfoEmailDTO userInfoEmailDTO;
    private FlagUserDTO flagUserDTO;

    @BeforeEach
    void setup()
    {
        dao = new TransactionDao();
        connection = Mockito.spy(SQLConnection.class);
        transferRequestDTO = Mockito.mock(UpdatedBalancesForTransferDTO.class);
        checkAccountDTO = Mockito.mock(CheckAccountDTO.class);
        depositRequestDTO = Mockito.mock(DepositRequestDTO.class);
        userInfoAccNumDTO = Mockito.mock(UserInfoAccNumDTO.class);
        loanRequestDTO = Mockito.mock(LoanRequestDTO.class);
        userInfoEmailDTO = Mockito.mock(UserInfoEmailDTO.class);
        flagUserDTO = Mockito.mock(FlagUserDTO.class);
        creditInterestDTO = Mockito.mock(CreditInterestDTO.class);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void makeTransfer_connection_called() throws SQLException {
        dao.makeTransfer(transferRequestDTO);
        Mockito.verify(connection).transfer(transferRequestDTO);
    }
    @Test
    void checkAccountId_connection_called() throws SQLException {
        dao.checkAccountId(checkAccountDTO);
        Mockito.verify(connection).checkAccountId(checkAccountDTO);
    }

    @Test
    void checkAccountId_throws_SQLException() throws SQLException {
        Mockito.when(connection.checkAccountId(checkAccountDTO)).thenThrow(SQLException.class);
        assertThrows(SQLException.class, () -> dao.checkAccountId(checkAccountDTO));
    }
    @Test
    void checkBalance_connection_called() throws SQLException {
        dao.checkBalance(checkAccountDTO);
        Mockito.verify(connection).checkBalance(checkAccountDTO);
    }
    @Test
    void checkBalance_throws_SQLException() throws SQLException
    {
        Mockito.when(connection.checkBalance(checkAccountDTO)).thenThrow(SQLException.class);
        assertThrows(SQLException.class, ()->dao.checkBalance(checkAccountDTO));
    }
    @Test
    void checkInterestRate_connection_called() throws SQLException
    {
        dao.checkInterestRate(checkAccountDTO);
        Mockito.verify(connection).checkInterestRate(checkAccountDTO);
    }
    @Test
    void dailyCheck_connection_called() throws SQLException {
        dao.dailyCheck(checkAccountDTO);
        Mockito.verify(connection).dailyCheck(checkAccountDTO);
    }
    @Test
    void dailyCheck_throws_SQLException() throws SQLException
    {
        Mockito.when(connection.dailyCheck(checkAccountDTO)).thenThrow(SQLException.class);
        assertThrows(SQLException.class, ()->dao.dailyCheck(checkAccountDTO));
    }
    @Test
    void makeDeposit_connection_called() throws SQLException {
        dao.makeDeposit(depositRequestDTO);
        Mockito.verify(connection).deposit(depositRequestDTO);
    }

    @Test
    void creditInterest_connection_called() throws SQLException {
        dao.creditInterest(creditInterestDTO);
        Mockito.verify(connection).creditInterest(creditInterestDTO);
    }

    @Test
    void creditInterest_throws_SQLException() throws SQLException {
        Mockito.when(connection.creditInterest(creditInterestDTO)).thenThrow(SQLException.class);
        assertThrows(SQLException.class, ()->dao.creditInterest(creditInterestDTO));
    }

    @Test
    void lastInterest_connection_called() throws SQLException {
        dao.lastInterest(userInfoAccNumDTO);
        Mockito.verify(connection).lastInterest(userInfoAccNumDTO);
    }

    @Test
    void lastInterest_throws_SQLException() throws SQLException {
        Mockito.when(connection.lastInterest(userInfoAccNumDTO)).thenThrow(SQLException.class);
        assertThrows(SQLException.class, ()->dao.lastInterest(userInfoAccNumDTO));
    }

    @Test
    void logLoan_connection_called() throws SQLException {
        dao.logLoan(loanRequestDTO);
        Mockito.verify(connection).logLoan(loanRequestDTO);
    }

    @Test
    void logLoan_throws_SQLException() throws SQLException {
        Mockito.doThrow(SQLException.class).when(connection).logLoan(loanRequestDTO);
        assertThrows(SQLException.class, () -> dao.logLoan(loanRequestDTO));
    }

    @Test
    void getAllTransactions_connection_called() throws SQLException {
        dao.getAllTransactions(userInfoEmailDTO);
        Mockito.verify(connection).getAllTransactions(userInfoEmailDTO);
    }
    @Test
    void getAllTransactionsForEmployee_connection_called() throws SQLException {
        dao.getAllTransactionsForEmployee();
        Mockito.verify(connection).getAllTransactionsForEmployee();
    }
    @Test
    void flagUser_connection_called()
    {
        dao.flagUser(flagUserDTO);
        Mockito.verify(connection).flagUser(flagUserDTO);
    }
    @Test
    void getAllSubscriptions_connection_called() throws SQLException {
        dao.getAllSubscriptions(userInfoEmailDTO);
        Mockito.verify(connection).getAllSubscriptions(userInfoEmailDTO);
    }
}
