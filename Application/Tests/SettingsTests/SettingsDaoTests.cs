﻿using Domain.DTOs;
using Grpc;
using Grpc.DAOs;
using Moq;

namespace Tests.SettingsTests;

public class SettingsDaoTests
{
    private readonly Mock<IGrpcClient> grpcClient;
    private readonly SettingsDao settingDao;

    public SettingsDaoTests()
    {
        grpcClient = new Mock<IGrpcClient>();
        settingDao = new SettingsDao(grpcClient.Object);
    }

    [Fact]
    public async void changing_the_base_rate()
    {
        var accountBaseRate = new AccountNewBaseRateDTO()
        {
            UserID = 1,
            BaseRate = 3.7
        };
        grpcClient.Setup(client => client.ChangeBaseRate(accountBaseRate));

        await settingDao.ChangeBaseRate(accountBaseRate);

        grpcClient.Verify(client => client.ChangeBaseRate(accountBaseRate), Times.Once());
    }

    [Fact]
    public async Task changing_the_base_rate_throws_exception()
    {
        var accountBaseRate = new AccountNewBaseRateDTO()
        {
            UserID = 1,
            BaseRate = 3.7
        };

        grpcClient.Setup(client => client.ChangeBaseRate(accountBaseRate)).Throws<Exception>();


        await Assert.ThrowsAsync<Exception>(() => settingDao.ChangeBaseRate(accountBaseRate));
    }

    [Fact]
    public async Task changing_user_details()
    {
        var userDetails = new UserNewDetailsRequestDTO()
        {
            OldEmail = "oldEmail@gmail.com",
            NewEmail = "newEmail@gmail.com",
            Password = "testPassword",
            Plan = "Premium"
        };


        grpcClient.Setup(client => client.ChangeUserDetails(userDetails));

        settingDao.ChangeUserDetails(userDetails);
        
        grpcClient.Verify(client => client.ChangeUserDetails(userDetails), Times.Once);

    }

    [Fact]
    public async Task changing_user_details_throws_exception()
    {
        var userDetails = new UserNewDetailsRequestDTO()
        {
            OldEmail = "oldEmail@gmail.com",
            NewEmail = "newEmail@gmail.com",
            Password = "testPassword",
            Plan = "Premium"
        };
        
        
        grpcClient.Setup(client => client.ChangeUserDetails(userDetails)).Throws<Exception>();

        await Assert.ThrowsAsync<Exception>(() => settingDao.ChangeUserDetails(userDetails));
    }
}