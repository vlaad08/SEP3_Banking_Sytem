using System.Collections;
using System.Security.AccessControl;
using System.Threading.Channels;
using Application.DaoInterfaces;
using Application.LogicInterfaces;
using Domain.DTOs;
using Domain.Models;

namespace Application.Logic;

public class TransferLogic : ITransferLogic
{
    private readonly ITransferDAO transferDao;

    public TransferLogic(ITransferDAO transferDAO)
    {
        this.transferDao = transferDAO;
    }

    private async Task ValidateTransfer(TransferRequestDTO transferRequestDto)
    {
        if (await transferDao.GetAccountNumberByAccountNumber(transferRequestDto) !=
            transferRequestDto.RecipientAccountNumber)
        {
            throw new Exception("The account number does not exist!");
        }
        if (await transferDao.GetBalanceByAccountNumber(transferRequestDto) < transferRequestDto.Amount)
        {
            throw new Exception("There is not sufficient balance to make the transaction!");
        }
        if ((await transferDao.GetTransferAmountsByDayForUser(transferRequestDto) + transferRequestDto.Amount) >=
            200000)
        {
            throw new Exception("You have reached your daily limit!");
        }
        if (await transferDao.GetAccountNumberByAccountNumber(transferRequestDto) == transferRequestDto.SenderAccountNumber)
        {
            throw new Exception("Cannot transfer money to the same account!");
        }
    }

    public async Task TransferMoney(TransferRequestDTO transferRequest)
    {
        await ValidateTransfer(transferRequest);
        double oldSenderBalance = await transferDao.GetBalanceByAccountNumber(transferRequest);
        TransferRequestDTO temp = new TransferRequestDTO()
        {
            Amount = transferRequest.Amount,
            Message = transferRequest.Message,
            RecipientAccountNumber = transferRequest.SenderAccountNumber,
            SenderAccountNumber = transferRequest.RecipientAccountNumber
        };
        double oldRecipientBalance = await transferDao.GetBalanceByAccountNumber(transferRequest);
        double newSenderBalance = oldSenderBalance - transferRequest.Amount;
        double newRecipientBalance = oldRecipientBalance + transferRequest.Amount;
        UpdatedBalancesForTransferDTO dto = new UpdatedBalancesForTransferDTO()
        {
            newReceiverBalance = newRecipientBalance,
            newSenderBalance = newSenderBalance,
            Message = transferRequest.Message,
            senderId = transferRequest.SenderAccountNumber,
            receiverId = transferRequest.RecipientAccountNumber,
            amount = transferRequest.Amount
        };
        await transferDao.TransferMoney(dto);
    }

    public async Task<IEnumerable<Transaction>> GetTransactions(GetTransactionsDTO getTransactionsDto)
    {
        return await transferDao.GetTransactions(getTransactionsDto);
    }

    public async Task<IEnumerable<Transaction>> GetTransactions()
    {
        return await transferDao.GetTransactions();
    }

    public async Task FlagUser(FlagUserDTO dto)
    {
        await transferDao.FlagUser(dto);

    }

    public async Task<Dictionary<string, Subscription>> GetSubscriptions(GetTransactionsDTO getTransactionsDto)
    {
        IEnumerable<Transaction> listFromDataBase = await transferDao.GetSubscriptions(getTransactionsDto); // Takes O(n)

        List<Transaction> list = listFromDataBase.ToList(); //Takes O(n)

        Dictionary<string, Subscription> dictionary = new Dictionary<string, Subscription>(); //Takes 1
        
        foreach (var l in list) //Takes: (1+1+1+1+1+2)*n = O(n)
        {
            if (!dictionary.ContainsKey(l.RecipientAccountNumber)) //Takes 1
            {
                DateTime dateTime = l.Date.AddMonths(1); //Takes 1
                var subs = new Subscription()
                {
                    ServiceName = l.RecipientName, // Takes 1
                    Amount = l.Amount, //Takes 1
                    Date = dateTime
                }; //Takes 1
                dictionary.TryAdd(l.RecipientAccountNumber, subs); //Takes 2
            }
        }
        return dictionary; // Takes 1
        //The time complexity of the method: (After the elimination of constants)
        // T(n) = O(n) + O(n) + 1 + O(n) + 1 = O(n)
    }
}