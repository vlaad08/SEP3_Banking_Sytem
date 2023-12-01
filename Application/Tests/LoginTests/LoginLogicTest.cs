using Application.DaoInterfaces;
using Domain.Models;
using Moq;
using Shared.DTOs;
using WebAPI.Services;

namespace Tests;

public class LoginLogicTest
{
    [Fact]
    public async Task GetAccounts_Calls_LoginDao()
    {
        var loginDao = new Mock<IUserLoginDao>();
        var interestDao = new Mock<IInterestDAO>();
        var auth = new AuthLogic(loginDao.Object,interestDao.Object);
        await auth.GetAccounts();
        loginDao.Verify(d => d.GetAccounts());
    }

    [Fact]
    public async Task GetUserAccounts_Calls_LoginDao()
    {
        var loginDao = new Mock<IUserLoginDao>();
        var interestDao = new Mock<IInterestDAO>();
        var auth = new AuthLogic(loginDao.Object,interestDao.Object);
        await auth.GetUserAccounts("test@gmail.com");
        loginDao.Verify(d =>d.GetUserAccounts("test@gmail.com"));
    }

    [Fact]
    public async Task Login_Calls_For_Validation()
    {
        UserLoginRequestDto dto = new UserLoginRequestDto
        {
            Email = "user1@gmail.com",
            Password = "password1"
        };
        List<User> userList = new List<User>
        {
            new User { Email = "user1@gmail.com", Password = "password1" },
            new User { Email = "user2@gmail.com", Password = "password2" }
        };
        var loginDao = new Mock<IUserLoginDao>();
        loginDao.Setup(l => l.GetAllUserDataForValidation())
            .ReturnsAsync(userList);
        var interestDao = new Mock<IInterestDAO>();
        var auth = new AuthLogic(loginDao.Object, interestDao.Object);

        User existing = await auth.Login(dto);
        
        loginDao.Verify(d => d.GetAllUserDataForValidation());
        Assert.NotNull(existing);
    }

    [Fact]
    public async Task Login_Returns_Null_User_Upon_Denied_UserName()
    {
        UserLoginRequestDto dto = new UserLoginRequestDto
        {
            Email = "test@gmail.com",
            Password = "password1"
        };
        List<User> userList = new List<User>
        {
            new User { Email = "user1@gmail.com", Password = "password1" },
            new User { Email = "user2@gmail.com", Password = "password2" }
        };
        var loginDao = new Mock<IUserLoginDao>();
        loginDao.Setup(l => l.GetAllUserDataForValidation())
            .ReturnsAsync(userList);
        var interestDao = new Mock<IInterestDAO>();
        var auth = new AuthLogic(loginDao.Object, interestDao.Object);

        User nonExistent = await auth.Login(dto);
        
        loginDao.Verify(d => d.GetAllUserDataForValidation());
        Assert.Null(nonExistent);
    }
    
    [Fact]
    public async Task Login_Returns_Null_User_Upon_Denied_Password()
    {
        UserLoginRequestDto dto = new UserLoginRequestDto
        {
            Email = "user1@gmail.com",
            Password = "12345678"
        };
        List<User> userList = new List<User>
        {
            new User { Email = "user1@gmail.com", Password = "password1" },
            new User { Email = "user2@gmail.com", Password = "password2" }
        };
        var loginDao = new Mock<IUserLoginDao>();
        loginDao.Setup(l => l.GetAllUserDataForValidation())
            .ReturnsAsync(userList);
        var interestDao = new Mock<IInterestDAO>();
        var auth = new AuthLogic(loginDao.Object, interestDao.Object);

        User nonExistent = await auth.Login(dto);
        
        loginDao.Verify(d => d.GetAllUserDataForValidation());
        Assert.Null(nonExistent);
    }
}