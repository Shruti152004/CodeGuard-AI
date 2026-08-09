FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["csharp-analyzer.csproj", "."]
RUN dotnet restore "csharp-analyzer.csproj"
COPY . .
RUN dotnet build "csharp-analyzer.csproj" -c Release -o /app/build
RUN dotnet publish "csharp-analyzer.csproj" -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS final
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 5001
ENV ASPNETCORE_URLS=http://+:5001
ENTRYPOINT ["dotnet", "csharp-analyzer.dll"]
