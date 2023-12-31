using DataAccess.DAOs;
using Domain.DTOs;
using Grpc;
using Moq;

namespace Tests.Transfer.Tests;

public class TransferDaoTest
{
    [Fact]
    public async Task TransferMoney_ValidTransfer_ShouldCallGrpcClient()
    {
        // Arrange
        TransferRequestDTO transferRequestDto = new TransferRequestDTO
        {
            SenderAccountNumber = "1111111111111111",
            RecipientAccountNumber = "2222222222222222",
            Amount = 69420.0
        };

        var grpcClientMock = new Mock<IGrpcClient>();
        var transferDao = new TransferDAO(grpcClientMock.Object);

        // Act
        await transferDao.TransferMoney(It.IsAny<UpdatedBalancesForTransferDTO>());

        // Assert
        //grpcClientMock.Verify(c => c.MakeTransfer(transferRequestDto), Times.Once);
    }
    
    [Fact]
    public async Task GetBalanceByAccountNumber_ValidAccountNumber_ShouldCallGrpcClient()
    {
        // Arrange
        TransferRequestDTO transferRequestDto = new TransferRequestDTO
        {
            SenderAccountNumber = "1111111111111111",
            RecipientAccountNumber = "2222222222222222",
            Amount = 69420.0
        };
        GetBalanceDTO getBalanceDto = new GetBalanceDTO
        {
            AccountId = transferRequestDto.SenderAccountNumber
        };
        var accountNumber = "1111111111111111"; 
        var expectedBalance = 69420; 

        var grpcClientMock = new Mock<IGrpcClient>();
        grpcClientMock.Setup(c => c.GetBalanceByAccountNumber(It.IsAny<GetBalanceDTO>()))
            .ReturnsAsync(expectedBalance);

        var transferDao = new TransferDAO(grpcClientMock.Object);

        // Act
        var result = await transferDao.GetBalanceByAccountNumber(transferRequestDto);

        // Assert
        Assert.IsType<double>(result);
        Assert.Equal(expectedBalance, result);
        grpcClientMock.Verify(c => c.GetBalanceByAccountNumber(It.IsAny<GetBalanceDTO>()), Times.Once);
    }

    [Fact]
    public async Task GetAccountNumberByAccountNumber_ValidAccountNumber_ShouldCallGrpcClientAndReturnString()
    {
        // Arrange
        TransferRequestDTO transferRequestDto = new TransferRequestDTO
        {
            SenderAccountNumber = "1111111111111111",
            RecipientAccountNumber = "2222222222222222",
            Amount = 69420.0
        };
        var accountNumber = "1111111111111111"; 
        var expectedAccount = "1111111111111111"; 

        var grpcClientMock = new Mock<IGrpcClient>();
        grpcClientMock.Setup(c => c.GetAccountNumberByAccountNumber(transferRequestDto))
            .ReturnsAsync(expectedAccount);

        var transferDao = new TransferDAO(grpcClientMock.Object);

        // Act
        var result = await transferDao.GetAccountNumberByAccountNumber(transferRequestDto);

        // Assert
        Assert.IsType<string>(result);
        Assert.Equal(expectedAccount, result);
        grpcClientMock.Verify(c => c.GetAccountNumberByAccountNumber(transferRequestDto), Times.Once);
    }
    [Fact]
    public async Task GetTransferAmountsByDayForUser_ValidAccountNumber_ShouldCallGrpcClientAndReturnString()
    {
        // Arrange
        TransferRequestDTO transferRequestDto = new TransferRequestDTO
        {
            SenderAccountNumber = "1111111111111111",
            RecipientAccountNumber = "2222222222222222",
            Amount = 69420.0
        };
        var accountNumber = "1111111111111111"; 
        var expectedAmount = 2222; 

        var grpcClientMock = new Mock<IGrpcClient>();
        grpcClientMock.Setup(c => c.DailyCheck(transferRequestDto))
            .ReturnsAsync(expectedAmount);

        var transferDao = new TransferDAO(grpcClientMock.Object);

        // Act
        var result = await transferDao.GetTransferAmountsByDayForUser(transferRequestDto);

        // Assert
        Assert.IsType<double>(result);
        Assert.Equal(expectedAmount, result);
        grpcClientMock.Verify(c => c.DailyCheck(transferRequestDto), Times.Once);
    }

    [Fact]
    public async Task GetTransactions_Calls_Grpc()
    {
        var dto = new GetTransactionsDTO
        {
            Email = "test@gmail.com"
        };
        var grpcClientMock = new Mock<IGrpcClient>();
        var transferDao = new TransferDAO(grpcClientMock.Object);
        await transferDao.GetTransactions(dto);
        grpcClientMock.Verify(g=>g.GetTransactions(dto));
    }
    [Fact]
    public async Task GetTransactionsForEmployee_Calls_Grpc()
    {
        var grpcClientMock = new Mock<IGrpcClient>();
        var transferDao = new TransferDAO(grpcClientMock.Object);
        await transferDao.GetTransactions();
        grpcClientMock.Verify(g=>g.GetTransactions());
    }
    [Fact]
    public async Task FlagUser_Calls_Grpc()
    {
        var grpcClientMock = new Mock<IGrpcClient>();
        var transferDao = new TransferDAO(grpcClientMock.Object);
        await transferDao.FlagUser(It.IsAny<FlagUserDTO>());
        grpcClientMock.Verify(g=>g.FlagUser(It.IsAny<FlagUserDTO>()));
    }
    [Fact]
    public async Task GetSubscriptions_Calls_Grpc()
    {
        var grpcClientMock = new Mock<IGrpcClient>();
        var transferDao = new TransferDAO(grpcClientMock.Object);
        await transferDao.GetSubscriptions(It.IsAny<GetTransactionsDTO>());
        grpcClientMock.Verify(g=>g.GetSubscriptions(It.IsAny<GetTransactionsDTO>()));
    }
}