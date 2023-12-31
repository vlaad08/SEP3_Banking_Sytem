using System.Text;
using Application.DaoInterfaces;
using Application.Logic;
using Application.LogicInterfaces;
using DataAccess.DAOs;
using Domain.Auth;
using Grpc;
using Grpc.DAOs;
using WebAPI.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.Extensions.DependencyInjection.Extensions;
using Microsoft.IdentityModel.Tokens;


var builder = WebApplication.CreateBuilder(args);

// Add services to the container.

builder.Services.AddControllers();
// Learn more about configuring Swagger/OpenAPI at https://aka.ms/aspnetcore/swashbuckle
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddScoped<ITransferLogic, TransferLogic>();
builder.Services.AddScoped<ITransferDAO, TransferDAO>();
builder.Services.AddScoped<IDepositLogic, DepositLogic>();
builder.Services.AddScoped<IDepositDAO, DepositDao>();
builder.Services.AddScoped<IGrpcClient, ProtoClient>();
builder.Services.AddScoped<IAuthLogic, AuthLogic>();
builder.Services.AddScoped<IUserLoginDao, UserLoginDao>();
builder.Services.AddScoped<IInterestDAO, InterestDao>();
builder.Services.AddScoped<ILoanLogic, LoanLogic>();
builder.Services.AddScoped<ILoanDAO, LoanDao>();
builder.Services.AddScoped<IUserRegisterDAO, UserRegisterDao>();
builder.Services.AddScoped<ISettingsDAO, SettingsDao>();
builder.Services.AddScoped<ISettingsLogic, SettingsLogic>(); builder.Services.AddScoped<IIssueLogic, IssueLogic>();
builder.Services.AddScoped<IIssueDAO, IssueDao>();

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme).AddJwtBearer(options =>
{
    options.RequireHttpsMetadata = false;
    options.SaveToken = true;
    options.TokenValidationParameters = new TokenValidationParameters()
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidAudience = builder.Configuration["Jwt:Audience"],
        ValidIssuer = builder.Configuration["Jwt:Issuer"],
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(builder.Configuration["Jwt:Key"]))
    };
});

AuthorizationPolicies.AddPolicies(builder.Services);

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors(x => x
    .AllowAnyMethod()
    .AllowAnyHeader()
    .SetIsOriginAllowed(origin => true) // allow any origin
    .AllowCredentials());


app.UseHttpsRedirection();

app.UseAuthorization();

app.MapControllers();

app.Run();