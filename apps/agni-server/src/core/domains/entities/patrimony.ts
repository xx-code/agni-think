import { PatrimonyType } from "../constants";
import { ValueObjectCollection } from "../valueObjects/collection";
import { PatrimonyAccount } from "../valueObjects/patrimonyAccount";
import Entity, { TrackableProperty } from "./entity";

export class Patrimony extends Entity {
    private title: TrackableProperty<string>
    private accounts: ValueObjectCollection<PatrimonyAccount>
    private type: TrackableProperty<PatrimonyType> 

    constructor(id: string, title: string, type: PatrimonyType, accounts: PatrimonyAccount[]) {
        super(id) 
        this.title = new TrackableProperty(title, this.markHasChange.bind(this))
        this.accounts = new ValueObjectCollection(accounts, this.markHasChange.bind(this))
        this.type = new TrackableProperty(type, this.markHasChange.bind(this))
    }
 
    setTitle(title: string) {
        this.title.set(title)
    }

    getTitle(): string {
        return this.title.get()
    }

    setType(type: PatrimonyType) {
        this.type.set(type)
    }

    getType(): PatrimonyType {
        return this.type.get()
    }

    addAccountId(account: PatrimonyAccount) {
        this.accounts.add(account)
    }

    deleteAccountId(account: PatrimonyAccount) {
        this.accounts.delete(account)
    }

    setAccounts(accounts: PatrimonyAccount[]) {
        this.accounts.set(accounts)
    }

    getAccounts(): PatrimonyAccount[] {
        return this.accounts.get()
    }
    
    getAccountCollections(): ValueObjectCollection<PatrimonyAccount> {
        return this.accounts
    }
}