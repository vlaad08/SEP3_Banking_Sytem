package Database.Server;

import Database.*;
import Database.AccountCheckRequest;
import Database.AccountCheckResponse;
import Database.AccountsInfo;
import Database.AllAccountsInfoRequest;
import Database.AllAccountsInfoResponse;
import Database.BalanceCheckRequest;
import Database.BalanceCheckResponse;
import Database.CreditInterestRequest;
import Database.CreditInterestResponse;
import Database.DAOs.CredentialChangerDao;
import Database.DAOs.ChatDao;
import Database.DAOs.Interfaces.ChatDaoInterface;
import Database.DAOs.Interfaces.LoginDaoInterface;
import Database.DAOs.Interfaces.RegisterDaoInterface;
import Database.DAOs.Interfaces.TransactionDaoInterface;
import Database.DAOs.LoginDao;
import Database.DAOs.RegisterDao;
import Database.DAOs.TransactionDao;
import Database.DTOs.*;
import io.grpc.stub.StreamObserver;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class GRPCServerImp extends DatabaseServiceGrpc.DatabaseServiceImplBase {
    TransactionDaoInterface transactionDao = new TransactionDao();
    LoginDaoInterface loginDao = new LoginDao();
    RegisterDaoInterface registerDao = new RegisterDao();
    CredentialChangerDao credentialChangerDao = new CredentialChangerDao();
    ChatDaoInterface chatDao = new ChatDao();

    @Override
    public void transfer(TransferRequest request, StreamObserver<TransferResponse> responseObserver) {
        UpdatedBalancesForTransferDTO updatedBalancesForTransferDTO = new UpdatedBalancesForTransferDTO(request.getSenderNewBalance(),request.getReceiverNewBalance(),request.getMessage(),request.getSenderId(),request.getReceiverId(),request.getAmount());
        try {
            transactionDao.makeTransfer(updatedBalancesForTransferDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        String resp = "Transfer happened";
        TransferResponse response = TransferResponse.newBuilder().setResp(resp).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void checkAccount(AccountCheckRequest request, StreamObserver<AccountCheckResponse> responseObserver) {
        CheckAccountDTO checkAccountDTO = new CheckAccountDTO(request.getRecipientAccountId());
        String recipientAccount_id;
        try {
            recipientAccount_id = transactionDao.checkAccountId(checkAccountDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        AccountCheckResponse response = AccountCheckResponse.newBuilder().setRecipientAccountId(recipientAccount_id)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void checkBalance(BalanceCheckRequest request, StreamObserver<BalanceCheckResponse> responseObserver) {
        CheckAccountDTO checkBalanceDTO = new CheckAccountDTO(request.getAccountId());
        double balance;
        try {
            balance = transactionDao.checkBalance(checkBalanceDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        BalanceCheckResponse response = BalanceCheckResponse.newBuilder().setBalance(balance).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void checkInterestRate(InterestRateCheckRequest request, StreamObserver<InterestRateCheckResponse> responseObserver) {
        CheckAccountDTO checkBalanceDTO = new CheckAccountDTO(request.getAccountId());
        double interestRate;
        try {
            interestRate = transactionDao.checkInterestRate(checkBalanceDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        InterestRateCheckResponse response = InterestRateCheckResponse.newBuilder().setInterestRate(interestRate).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void dailyCheckTransactions(DailyCheckRequest request, StreamObserver<DailyCheckResponse> responseObserver) {
        CheckAccountDTO checkAccountDTO = new CheckAccountDTO(request.getAccountId());
        double amount = 0;
        try {
            amount = transactionDao.dailyCheck(checkAccountDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        DailyCheckResponse response = DailyCheckResponse.newBuilder().setAmount(amount).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deposit(DepositRequest request, StreamObserver<DepositResponse> responseObserver) {
        try {
            DepositRequestDTO depositRequestDTO = new DepositRequestDTO(request.getAccountId(), request.getAmount(), request.getNewBalance());
            transactionDao.makeDeposit(depositRequestDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        DepositResponse response = DepositResponse.newBuilder().setResp("Deposit happened").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void loginValidation(LoginValidationRequest request,
            StreamObserver<LoginValidationResponse> responseObserver) {
        try {
            List<User> users = loginDao.getUsers();
            LoginValidationResponse response = LoginValidationResponse.newBuilder().addAllUsers(users).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void allAccountsInfo(AllAccountsInfoRequest request,
            StreamObserver<AllAccountsInfoResponse> responseStreamObserver) {
        try {
            List<AccountsInfo> accInfo = loginDao.getAccountsInfo();
            AllAccountsInfoResponse response = AllAccountsInfoResponse.newBuilder().addAllAccountInfo(accInfo).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void userAccountsInfo(UserAccountInfoRequest request,
            StreamObserver<UserAccountInfoResponse> responseStreamObserver) {
        try {
            UserInfoEmailDTO userInfoDTO = new UserInfoEmailDTO(request.getEmail());
            List<AccountsInfo> accInfo = loginDao.getUserAccountInfos(userInfoDTO);
            UserAccountInfoResponse response = UserAccountInfoResponse.newBuilder().addAllAccountInfo(accInfo).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void creditInterest(CreditInterestRequest request,
            StreamObserver<CreditInterestResponse> responseStreamObserver) {
        try {
            CreditInterestDTO creditInterestDTO = new CreditInterestDTO(request.getAccountNumber(),request.getAmount(),request.getBalance());
            boolean happened = transactionDao.creditInterest(creditInterestDTO);
            CreditInterestResponse response = CreditInterestResponse.newBuilder().setHappened(happened).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void lastInterest(LastInterestRequest request, StreamObserver<LastInterestResponse> responseStreamObserver) {
        try {
            UserInfoAccNumDTO userInfoDTO = new UserInfoAccNumDTO(request.getAccountNumber());
            Timestamp date = transactionDao.lastInterest(userInfoDTO);
            LastInterestResponse response;
            if (date != null) {
                com.google.protobuf.Timestamp timestampProto = com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(date.getTime() / 1000)
                        .setNanos((int) ((date.getTime() % 1000) * 1000000))
                        .build();
                response = LastInterestResponse.newBuilder().setDate(timestampProto).build();
            } else {
                response = null;
            }
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logLoan(LogLoanRequest request, StreamObserver<LogLoanResponse> responseStreamObserver) {
        try {
            LoanRequestDTO loanRequestDTO = new LoanRequestDTO(request.getAccountId(), request.getRemainingAmount(),
                    request.getInterestRate(), request.getMonthlyPayment(), request.getEndDate(),
                    request.getLoanAmount());
            transactionDao.logLoan(loanRequestDTO);
            LogLoanResponse response = LogLoanResponse.newBuilder().setResponse("Loan granted").build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getTransactions(GetTransactionsRequest request,
            StreamObserver<GetTransactionsResponse> responseStreamObserver) {
        try {
            UserInfoEmailDTO userInfoEmailDTO = new UserInfoEmailDTO(request.getEmail());
            List<Transactions> transactions = transactionDao.getAllTransactions(userInfoEmailDTO);
            GetTransactionsResponse response = GetTransactionsResponse.newBuilder().addAllTransactions(transactions)
                    .build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getTransactionsForEmployee(GetTransactionsForEmployeeRequest request,
            StreamObserver<GetTransactionsForEmployeeResponse> responseObserver) {
        try {
            List<Transactions> transactions = transactionDao.getAllTransactionsForEmployee();
            GetTransactionsForEmployeeResponse response = GetTransactionsForEmployeeResponse.newBuilder()
                    .addAllTransactions(transactions).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getSubscriptions(GetTransactionsRequest request,
            StreamObserver<GetTransactionsResponse> responseStreamObserver) {
        try {
            UserInfoEmailDTO userInfoEmailDTO = new UserInfoEmailDTO(request.getEmail());
            List<Transactions> subscriptions = transactionDao.getAllSubscriptions(userInfoEmailDTO);
            GetTransactionsResponse response = GetTransactionsResponse.newBuilder().addAllTransactions(subscriptions)
                    .build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flagUser(FlagUserRequest request, StreamObserver<FlagUserResponse> responseObserver) {
        FlagUserDTO dto = new FlagUserDTO(request.getSenderId());
        transactionDao.flagUser(dto);
        FlagUserResponse response = FlagUserResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registerUser(RegisterRequest request, StreamObserver<RegisterResponse> responseStreamObserver) {
        try {
            RegisterRequestDTO registerUserDTO = new RegisterRequestDTO(
                    request.getEmail(), request.getFirstname(),
                    request.getMiddlename(), request.getLastname(),
                    request.getPassword(), request.getPlan());
            registerDao.registerUser(registerUserDTO);
            RegisterResponse response = RegisterResponse.newBuilder().build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getUserByEmail(UserEmailRequest request, StreamObserver<UserEmailResponse> responseStreamObserver) {
        try {
            UserAccountRequestDTO userAccountRequestDTO = new UserAccountRequestDTO(request.getEmail());
            String email = registerDao.getUserEmail(userAccountRequestDTO);
            UserEmailResponse response = UserEmailResponse.newBuilder().setEmail(email).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getUserId(UserAccountRequest request, StreamObserver<UserAccountResponse> responseStreamObserver) {
        try {
            UserAccountRequestDTO userAccountRequestDTO = new UserAccountRequestDTO(request.getEmail());
            int user_id = registerDao.getUserID(userAccountRequestDTO);
            UserAccountResponse response = UserAccountResponse.newBuilder().setUserId(
                    user_id).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createUserAccountNumber(AccountCreateRequest request,
            StreamObserver<AccountCreateResponse> responseStreamObserver) {
        try {
            UserAccountDTO userAccountDTO = new UserAccountDTO(
                    request.getUserId(), request.getUserAccountNumber(), request.getAccountType(),
                    request.getInterestRate());
            registerDao.generateAccountNumber(userAccountDTO);
            AccountCreateResponse response = AccountCreateResponse.newBuilder().build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeUserDetails(UserNewDetailsRequest request,
            StreamObserver<UserNewDetailsResponse> responseStreamObserver) {
        try {
            UserNewDetailsRequestDTO userNewDetailsRequestDTO = new UserNewDetailsRequestDTO(
                    request.getNewEmail(), request.getOldEmail(), request.getPassword(), request.getPlan());
            credentialChangerDao.UpdateUserInformation(userNewDetailsRequestDTO);
            UserNewDetailsResponse response = UserNewDetailsResponse.newBuilder().build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void changeBaseRate(AccountNewBaseRateRequest request,
            StreamObserver<AccountNewBaseRateResponse> responseStreamObserver) {
        try {
            AccountNewBaseRateDTO accountNewBaseRateDTO = new AccountNewBaseRateDTO(
                    request.getUserId(), request.getBaseRate());
            credentialChangerDao.UpdateBaseRate(accountNewBaseRateDTO);
            AccountNewBaseRateResponse response = AccountNewBaseRateResponse.newBuilder().build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createIssue(CreateIssueRequest request, StreamObserver<CreateIssueResponse> responseStreamObserver) {
        try {
            IssueCreationDTO issueDTO = new IssueCreationDTO(request.getTitle(), request.getBody(), request.getOwner());
            chatDao.createIssue(issueDTO);
            CreateIssueResponse response = CreateIssueResponse.newBuilder().build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateIssue(UpdateIssueRequest request, StreamObserver<UpdateIssueResponse> responseStreamObserver) {
        try {
            IssueUpdateDTO issueDTO = new IssueUpdateDTO(request.getId());
            chatDao.updateIssue(issueDTO);
            UpdateIssueResponse response = UpdateIssueResponse.newBuilder().build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseStreamObserver) {
        try {
            MessageDTO messageDTO = new MessageDTO(request.getTitle(), request.getOwner(), request.getBody(),
                    request.getIssueId(), null);// WATCH OUT FOR THIS CHECK IF ALR
            chatDao.sendMessage(messageDTO);
            SendMessageResponse response = SendMessageResponse.newBuilder().build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getMessagesForIssue(GetMessagesForIssueRequest request,
            StreamObserver<GetMessagesForIssueResponse> responseStreamObserver) {
        try {
            IssueinfoDTO issueinfoDTO = new IssueinfoDTO(request.getIssueId());
            List<MessageInfo> messageInfos = chatDao.getMessagesForIssue(issueinfoDTO);
            GetMessagesForIssueResponse response = GetMessagesForIssueResponse.newBuilder()
                    .addAllMessageInfo(messageInfos).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getAllIssues(GetAllIssuesRequest request, StreamObserver<GetAllIssuesResponse> responseStreamObserver) {
        try {
            List<Issue> issues = chatDao.getAllIssues();
            GetAllIssuesResponse response = GetAllIssuesResponse.newBuilder().addAllIssues(issues).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
            for (Issue issue : issues) {
                System.out.println(issue.getOwnerId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getMessagesByIssueId(GetMessagesByIssueIdRequest request,
            StreamObserver<GetMessagesByIssueIdResponse> responseStreamObserver) {
        try {
            IssueinfoDTO issueInfoDTO = new IssueinfoDTO(request.getIssueId());
            List<MessageInfo> messageInfos = chatDao.getMessagesByIssueId(issueInfoDTO);
            GetMessagesByIssueIdResponse response = GetMessagesByIssueIdResponse.newBuilder()
                    .addAllMessageInfo(messageInfos).build();
            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
            for (MessageInfo info : messageInfos) {
                System.out.println(info.getTitle());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEmail(UserNewEmailRequest request, StreamObserver<UserNewEmailResponse> responseStreamObserver)

    {
        UserNewEmailDTO userNewEmailDTO = new UserNewEmailDTO(
                request.getUserId(), request.getEmail());
        try {
            credentialChangerDao.UpdateEmail(userNewEmailDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        UserNewEmailResponse response = UserNewEmailResponse.newBuilder().build();
        responseStreamObserver.onNext(response);
        responseStreamObserver.onCompleted();
    }

    @Override
    public void updatePassword(UserNewPasswordRequest request,
            StreamObserver<UserNewPasswordResponse> responseStreamObserver)

    {
        UserNewPasswordDTO userNewPasswordDTO = new UserNewPasswordDTO(
                request.getUserId(), request.getPassword());
        try {
            credentialChangerDao.UpdatePassword(userNewPasswordDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        UserNewPasswordResponse response = UserNewPasswordResponse.newBuilder().build();
        responseStreamObserver.onNext(response);
        responseStreamObserver.onCompleted();
    }

    @Override
    public void updatePlan(UserNewPlanRequest request, StreamObserver<UserNewPlanResponse> responseStreamObserver)

    {
        UserNewPlanDTO userNewPlanDTO = new UserNewPlanDTO(
                request.getUserId(), request.getPlan());
        try {
            credentialChangerDao.UpdatePlan(userNewPlanDTO);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        UserNewPlanResponse response = UserNewPlanResponse.newBuilder().build();
        responseStreamObserver.onNext(response);
        responseStreamObserver.onCompleted();
    }

}
