using Application.Logic;
using Domain.DTOs;
using Grpc;
using Grpc.DAOs;
using Moq;

namespace Tests.LoanTests;

public class LoanDaoTest
{
    [Fact]
    public async Task RequestLoan_Calls_For_grpc()
    {
        var dto = new LoanRequestDTO 
        {
            AccountNumber = "1111222233334444",
            RemainingAmount = 4500,
            Amount = 3000,
            Duration = 16,
            InterestRate = 24,
            MonthlyPayment = 450,
            EndDate = DateTime.Today 
        };
        var grpcClient = new Mock<IGrpcClient>();
        var loanDao = new LoanDao(grpcClient.Object);
        await loanDao.RequestLoan(dto);
        grpcClient.Verify(g=>g.RequestLoan(dto));
    }

    [Fact]
    public async Task GetBalanceByAccountNumber_Calls_For_Grpc()
    {
        DepositRequestDTO dto = new DepositRequestDTO
        {
            Amount = 1000,
            ToppedUpAccountNumber = "-"
        };
        var grpcClient = new Mock<IGrpcClient>();
        var loanDao = new LoanDao(grpcClient.Object);
        await loanDao.GetBalanceByAccountNumber(dto);
        grpcClient.Verify(g=>g.GetBalanceByAccountNumber(It.IsAny<GetBalanceDTO>()));
    }
}