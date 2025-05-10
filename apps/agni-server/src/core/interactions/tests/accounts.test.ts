import { ResourceAlreadyExist } from "../../errors/resourceAlreadyExistError";
import { Account } from "../../domains/entities/account";
import { CreationAccountUseCase, ICreationAccountUseCaseResponse, RequestCreationAccountUseCase } from "../account/creationAccountUseCase";
import { AccountRepository } from "../../repositories/accountRepository";
import { DeleteAccountUseCase, IDeleteAccountUseCaseResponse } from "../account/deleteAccountUseCase";
import { TransactionRepository } from "../../repositories/transactionRepository";
import { GetAccountUseCase, IGetAccountUseCaseResponse } from "../account/getAccountUseCase";
import { Money } from "../../domains/entities/money";
import { GetAllAccountUseCase, IGetAllAccountUseCaseResponse } from "../account/getAllAccountUseCase";
import { ResourceNotFoundError } from "../../errors/resournceNotFoundError";
import { IUpdateAccountUseCaseResponse, RequestUpdateAccountUseCase, UpdateAccountUseCase } from "../account/updateAccountUseCase";
import { AccountType } from "@core/domains/constants";

describe("create account usecase Class", () => {

    let accountRepo: AccountRepository
    let presenter: ICreationAccountUseCaseResponse

    beforeEach(() => {
        accountRepo = {
            get: jest.fn(() => new Promise<Account>(() => null)),
            getAll: jest.fn(() => new Promise<Account[]>(() => [])),
            isExistById: jest.fn(() => new Promise<boolean>(() => false)),
            isExistByIds: jest.fn(() => new Promise<boolean>(() => false)),
            isExistByName: jest.fn(() => new Promise<boolean>(() => false)),
            delete: jest.fn(() => new Promise<void>(() => {})),
            save: jest.fn(() => new Promise<void>(() => {})),
            update: jest.fn(() => new Promise<void>(() => {})),
        }
        presenter = {
            success: jest.fn(() => {}),
            fail: jest.fn(() => {})
        }
    });

    test("should create the creation of account", async () => {
        accountRepo.isExistByName = jest.fn(() => new Promise<boolean>((resolve, reject) => resolve(false)))
        accountRepo.save = jest.fn(() => new Promise<void>((resolve, reject) => resolve()))
        let creationAccountUseCase = new CreationAccountUseCase(accountRepo, presenter)
        let request: RequestCreationAccountUseCase = {
            title: "New account",
            type: "Checking"
        }
    
        await creationAccountUseCase.execute(request)

        expect(accountRepo.save).toHaveBeenCalled()
        expect(presenter.success).toHaveBeenCalledWith(true)
    })

    test("should not create duplicate of account", async () => {
        accountRepo.isExistByName = jest.fn(() => new Promise<boolean>((resolve, reject) => resolve(true)))
        let creationAcountUseCase = new CreationAccountUseCase(accountRepo, presenter)
        let request: RequestCreationAccountUseCase = {
            title: "New account",
            type: "Checking"
        }
        await creationAcountUseCase.execute(request)
        
        expect(presenter.fail).toHaveBeenCalledWith(expect.any(ResourceAlreadyExist))
    })
})

describe("DeleteAccountUseCase", () => {
    let accountRepo: AccountRepository;
    let presenter: IDeleteAccountUseCaseResponse;

    beforeEach(() => {
        accountRepo = {
            get: jest.fn(),
            getAll: jest.fn(),
            isExistById: jest.fn(() => Promise.resolve(false)), // Par défaut, l'ID n'existe pas
            isExistByIds: jest.fn(),
            isExistByName: jest.fn(),
            delete: jest.fn(() => Promise.resolve()), // Suppression réussie par défaut
            save: jest.fn(),
            update: jest.fn(),
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn()
        };
    });

    test("should delete account if it exists", async () => {
        const accountId = "valid-id";

        // Simule que l'ID existe
        accountRepo.isExistById = jest.fn(() => Promise.resolve(true));

        const deleteAccountUseCase = new DeleteAccountUseCase(accountRepo, presenter);

        await deleteAccountUseCase.execute(accountId);

        expect(accountRepo.isExistById).toHaveBeenCalledWith(accountId);
        expect(accountRepo.delete).toHaveBeenCalledWith(accountId);
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should fail if account does not exist", async () => {
        const accountId = "non-existent-id";

        // Simule que l'ID n'existe pas (déjà par défaut)
        const deleteAccountUseCase = new DeleteAccountUseCase(accountRepo, presenter);

        await deleteAccountUseCase.execute(accountId);

        expect(accountRepo.isExistById).toHaveBeenCalledWith(accountId);
        expect(accountRepo.delete).not.toHaveBeenCalled(); // Ne doit pas appeler delete
        expect(presenter.fail).toHaveBeenCalledWith(expect.any(ResourceNotFoundError));
    });

    test("should handle unexpected errors", async () => {
        const accountId = "valid-id";

        // Simule que l'ID existe
        accountRepo.isExistById = jest.fn(() => Promise.resolve(true));
        // Simule une erreur lors de la suppression
        accountRepo.delete = jest.fn(() => Promise.reject(new Error("Database error")));

        const deleteAccountUseCase = new DeleteAccountUseCase(accountRepo, presenter);

        await deleteAccountUseCase.execute(accountId);

        expect(accountRepo.delete).toHaveBeenCalledWith(accountId);
        expect(presenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});

describe("GetAccountUseCase", () => {
    let accountRepo: AccountRepository;
    let transactionRepo: TransactionRepository;
    let presenter: IGetAccountUseCaseResponse;

    beforeEach(() => {
        accountRepo = {
            get: jest.fn(),
            getAll: jest.fn(),
            isExistById: jest.fn(),
            isExistByIds: jest.fn(),
            isExistByName: jest.fn(),
            delete: jest.fn(),
            save: jest.fn(),
            update: jest.fn(),
        };

        transactionRepo = {
            delete: jest.fn(),
            get: jest.fn(),
            getTransactions: jest.fn(),
            getPaginations: jest.fn(),
            isTransactionExistById: jest.fn(),
            save: jest.fn(),
            update: jest.fn()
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn()
        };
    });

    test("should return account details with balance", async () => {
        const accountId = "valid-id";
        const mockAccount = new Account(accountId, "Personal Account", AccountType.CHECKING);
        const mockBalance = new Money(1000);

        (accountRepo.get as jest.Mock).mockResolvedValue(mockAccount);
        (transactionRepo.get as jest.Mock).mockResolvedValue(mockBalance);

        const getAccountUseCase = new GetAccountUseCase(accountRepo, transactionRepo, presenter);

        await getAccountUseCase.execute(accountId);

        expect(accountRepo.get).toHaveBeenCalledWith(accountId);
        expect(transactionRepo.getAccountBalance).toHaveBeenCalledWith(accountId);
        expect(presenter.success).toHaveBeenCalledWith({
            accountId: accountId,
            title: "Personal Account",
            balance: 1000
        });
    });

    test("should fail if account does not exist", async () => {
        const accountId = "invalid-id";
        (accountRepo.get as jest.Mock).mockRejectedValue(new Error("Account not found"));

        const getAccountUseCase = new GetAccountUseCase(accountRepo, transactionRepo, presenter);

        await getAccountUseCase.execute(accountId);

        expect(accountRepo.get).toHaveBeenCalledWith(accountId);
        expect(transactionRepo.getAccountBalance).not.toHaveBeenCalled();
        expect(presenter.fail).toHaveBeenCalledWith(new Error("Account not found"));
    });

    test("should fail if balance retrieval fails", async () => {
        const accountId = "valid-id";
        const mockAccount = new Account(accountId, "Personal Account");

        (accountRepo.get as jest.Mock).mockResolvedValue(mockAccount);
        (transactionRepo.getAccountBalance as jest.Mock).mockRejectedValue(new Error("Balance fetch error"));

        const getAccountUseCase = new GetAccountUseCase(accountRepo, transactionRepo, presenter);

        await getAccountUseCase.execute(accountId);

        expect(accountRepo.get).toHaveBeenCalledWith(accountId);
        expect(transactionRepo.getAccountBalance).toHaveBeenCalledWith(accountId);
        expect(presenter.fail).toHaveBeenCalledWith(new Error("Balance fetch error"));
    });
});

describe("GetAllAccountUseCase", () => {
    let accountRepo: AccountRepository;
    let transactionRepo: TransactionRepository;
    let presenter: IGetAllAccountUseCaseResponse;

    beforeEach(() => {
        accountRepo = {
            get: jest.fn(),
            getAll: jest.fn(),
            isExistById: jest.fn(),
            isExistByIds: jest.fn(),
            isExistByName: jest.fn(),
            delete: jest.fn(),
            save: jest.fn(),
            update: jest.fn(),
        };

        transactionRepo = {
            getAccountBalance: jest.fn(),
            delete: jest.fn(),
            get: jest.fn(),
            getBalance: jest.fn(),
            getPaginations: jest.fn(),
            isTransactionExistById: jest.fn(),
            save: jest.fn(),
            update: jest.fn()
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn()
        };
    });

    test("should return all accounts with balance", async () => {
        const mockAccounts = [
            new Account("id-1", "Checking Account"),
            new Account("id-2", "Savings Account")
        ];
        const mockBalances = {
            "id-1": new Money(500),
            "id-2": new Money(1000)
        };

        const getAllAccountUseCase = new GetAllAccountUseCase(accountRepo, transactionRepo, presenter);

        await getAllAccountUseCase.execute();

        expect(accountRepo.getAll).toHaveBeenCalled();
        expect(transactionRepo.getAccountBalance).toHaveBeenCalledTimes(1);

    });

    test("should filter accounts based on isSaving flag", async () => {
        const mockAccounts = [
            new Account("id-1", "Checking Account"),
            new Account("id-2", "Savings Account")
        ];
        const mockBalances = {
            "id-2": new Money(1000)
        };

        (accountRepo.getAll as jest.Mock).mockResolvedValue(mockAccounts);
        (transactionRepo.getAccountBalance as jest.Mock).mockImplementation(async (id) => mockBalances["id-2"]);

        const getAllAccountUseCase = new GetAllAccountUseCase(accountRepo, transactionRepo, presenter);

        await getAllAccountUseCase.execute(true);

        expect(accountRepo.getAll).toHaveBeenCalled();
        expect(transactionRepo.getAccountBalance).toHaveBeenCalledTimes(1);
        expect(presenter.success).toHaveBeenCalledWith([
            { accountId: "id-2", title: "Savings Account", balance: 1000 }
        ]);
    });

    test("should fail if an error occurs", async () => {
        (accountRepo.getAll as jest.Mock).mockRejectedValue(new Error("Database error"));

        const getAllAccountUseCase = new GetAllAccountUseCase(accountRepo, transactionRepo, presenter);

        await getAllAccountUseCase.execute();

        expect(accountRepo.getAll).toHaveBeenCalled();
        expect(presenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});

describe("UpdateAccountUseCase", () => {
    let accountRepo: AccountRepository;
    let presenter: IUpdateAccountUseCaseResponse;

    beforeEach(() => {
        accountRepo = {
            get: jest.fn(),
            getAll: jest.fn(),
            isExistById: jest.fn(),
            isExistByIds: jest.fn(),
            isExistByName: jest.fn(),
            delete: jest.fn(),
            save: jest.fn(),
            update: jest.fn(),
        };

        presenter = {
            success: jest.fn(),
            fail: jest.fn(),
        };
    });

    test("should update account title successfully", async () => {
        const existingAccount = new Account("id-1", "Old Title");
        existingAccount.setTitle = jest.fn();
        existingAccount.hasChange = jest.fn(() => true);

        (accountRepo.get as jest.Mock).mockResolvedValue(existingAccount);
        (accountRepo.isExistByName as jest.Mock).mockResolvedValue(false);
        (accountRepo.update as jest.Mock).mockResolvedValue(undefined);

        const updateAccountUseCase = new UpdateAccountUseCase(accountRepo, presenter);
        const request: RequestUpdateAccountUseCase = { id: "id-1", title: "New Title" };

        await updateAccountUseCase.execute(request);

        expect(accountRepo.get).toHaveBeenCalledWith("id-1");
        expect(accountRepo.isExistByName).toHaveBeenCalledWith("New Title");
        expect(existingAccount.setTitle).toHaveBeenCalledWith("New Title");
        expect(accountRepo.update).toHaveBeenCalledWith(existingAccount);
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should not update if the title is unchanged", async () => {
        const existingAccount = new Account("id-1", "Same Title");
        existingAccount.setTitle = jest.fn();
        existingAccount.hasChange = jest.fn(() => false);

        (accountRepo.get as jest.Mock).mockResolvedValue(existingAccount);
        (accountRepo.isExistByName as jest.Mock).mockResolvedValue(false);

        const updateAccountUseCase = new UpdateAccountUseCase(accountRepo, presenter);
        const request: RequestUpdateAccountUseCase = { id: "id-1", title: "Same Title" };

        await updateAccountUseCase.execute(request);

        expect(accountRepo.update).not.toHaveBeenCalled();
        expect(presenter.success).toHaveBeenCalledWith(true);
    });

    test("should fail if account title already exists", async () => {
        const existingAccount = new Account("id-1", "Old Title");

        (accountRepo.get as jest.Mock).mockResolvedValue(existingAccount);
        (accountRepo.isExistByName as jest.Mock).mockResolvedValue(true);

        const updateAccountUseCase = new UpdateAccountUseCase(accountRepo, presenter);
        const request: RequestUpdateAccountUseCase = { id: "id-1", title: "Existing Title" };

        await updateAccountUseCase.execute(request);

        expect(accountRepo.get).toHaveBeenCalledWith("id-1");
        expect(accountRepo.isExistByName).toHaveBeenCalledWith("Existing Title");
        expect(presenter.success).toHaveBeenCalledWith(false);
        expect(presenter.fail).toHaveBeenCalledWith(new ResourceAlreadyExist("Account name already exist"));
    });

    test("should fail if repository throws an error", async () => {
        (accountRepo.get as jest.Mock).mockRejectedValue(new Error("Database error"));

        const updateAccountUseCase = new UpdateAccountUseCase(accountRepo, presenter);
        const request: RequestUpdateAccountUseCase = { id: "id-1", title: "New Title" };

        await updateAccountUseCase.execute(request);

        expect(presenter.success).toHaveBeenCalledWith(false);
        expect(presenter.fail).toHaveBeenCalledWith(new Error("Database error"));
    });
});