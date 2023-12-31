using System.Runtime.InteropServices.JavaScript;
using Application.DaoInterfaces;
using Application.Logic;
using Application.LogicInterfaces;
using Domain.DTOs;
using Domain.Models;
using Grpc.DAOs;
using Shared.DTOs;

namespace WebAPI.Services;

public class AuthLogic : IAuthLogic
{
    private readonly IUserLoginDao userLoginDao;
    private readonly IInterestDAO interestDao;
    private readonly IUserRegisterDAO registerDao;
    public AuthLogic(IUserLoginDao userLoginDao, IInterestDAO interestDao, IUserRegisterDAO registerDao)
    {
        this.userLoginDao = userLoginDao;
        this.interestDao = interestDao;
        this.registerDao = registerDao;
    }
    private async Task<User> ValidateUser(UserLoginRequestDto userLoginRequestDto)
    {
        List<User> users = await userLoginDao.GetAllUserDataForValidation();
        
        User existingUser = null;
        foreach (var u in users)
        {
            if (u.Email!.Equals(userLoginRequestDto.Email) && u.Password!.Equals(userLoginRequestDto.Password))
            {
                existingUser = new User()
                {
                    Id = u.Id,
                    Email = u.Email,
                    FirstName = u.FirstName,
                    LastName = u.LastName,
                    MiddleName = u.MiddleName,
                    Role = u.Role
                };
                break;
            }
        }
        return existingUser!;
    }
    public async Task<User> Login(UserLoginRequestDto userLoginRequestDto)
    {
        return await ValidateUser(userLoginRequestDto);
    }

    public async Task<List<AccountsInfo>> GetAccounts()
    {
        return await userLoginDao.GetAccounts();
    }

    public async Task<List<AccountsInfo>> GetUserAccounts(UserLoginRequestDto userLoginRequestDto)
    {
        List<AccountsInfo> accounts = await userLoginDao.GetUserAccounts(userLoginRequestDto);

        foreach (var a in accounts)
        {
            InterestCheckDTO dto = new InterestCheckDTO { AccountID = a.AccountNumber };
            DateTime? interestTimestamp = await interestDao.CheckInterest(dto);
            DateTime datePart;
            DateTime today;
            if (interestTimestamp != null)
            {
                datePart = interestTimestamp.Value.Date;
                today = DateTime.Now.Date;
                if ((interestTimestamp != null && !datePart.Equals(today) && DateTime.Now.Day == 1) || (interestTimestamp==null&&DateTime.Now.Day == 1))
                {
                    double oldBalance = await interestDao.GetBalanceByAccountNumber(dto);
                    double interestRate = await interestDao.GetInterestRateByAccountNumber(dto);
                    double newBalance = oldBalance + (oldBalance*(interestRate/100));
                    CreditInterestDTO creditInterestDto = new CreditInterestDTO
                    {
                        AccountID = dto.AccountID,
                        Balance = newBalance,
                        Amount = oldBalance*interestRate
                    };
                    await interestDao.CreditInterest(creditInterestDto);
                }
            }
        }
        return accounts;
    }

    public async Task<bool> VerifyUser(UserRegisterDto userRegisterDto)
    {
        UserEmailDTO user = new UserEmailDTO()
        {
            Email = userRegisterDto.Email
        };

        
        string email = await registerDao.VerifyUser(user);

        
        if (email.Equals(userRegisterDto.Email))
        {
            return true;
        }
        return false;

    }

    public async Task RegisterUser(UserRegisterDto userRegisterDto)
    {
        await registerDao.RegisterUser(userRegisterDto);
        
        var userEmail = new UserEmailDTO()
        {
            Email = userRegisterDto.Email
        };
        int newUserID =  await GetUserId(userEmail);

        string accountNumber = GenerateAccountNumber();


        TransferRequestDTO transferRequestDto = new TransferRequestDTO()
        {
            RecipientAccountNumber = accountNumber
        };
             
        while (await VerifyAccountNumber(transferRequestDto) == false)
        {
            accountNumber = GenerateAccountNumber();
            transferRequestDto = new TransferRequestDTO()
            {
                RecipientAccountNumber = accountNumber
            };
        }

        double baseInterestRate = 1.7;
        if (userRegisterDto.Plan == "Premium")
        {
            baseInterestRate = 3.7;
        }

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto()
        {
            User_id = newUserID,
            AccountType = "personal",
            UserAccountNumber = accountNumber,
            InterestRate = baseInterestRate
        };

        await CreateUserAccountNumber(accountCreateRequestDto);
    }

    public async Task<int> GetUserId(UserEmailDTO userEmailDto)
    {
       return await registerDao.GetUserId(userEmailDto);
    }

    public async Task<bool> VerifyAccountNumber(TransferRequestDTO transferRequestDto)
    {
        string accountNumber = await registerDao.VerifyAccountNumber(transferRequestDto);

        if (accountNumber == "" || accountNumber == null)
        {
            return false;
        }

        return true;
    }

    public async Task CreateUserAccountNumber(AccountCreateRequestDto accountCreateRequestDto)
    {
        await registerDao.CreateUserAccountNumber(accountCreateRequestDto);
    }
    
    static string GenerateAccountNumber()
    {
        Random random = new Random();
        string accountNumber = "";

        for (int i = 0; i < 14; i++)
        {
            accountNumber += random.Next(0, 10).ToString();
        }

        return accountNumber;
    }
    
}