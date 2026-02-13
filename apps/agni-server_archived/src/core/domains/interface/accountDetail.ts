import { AccountType } from "../constants";
import { IObjectEquality } from "./equality";

export interface IAccountDetail extends IObjectEquality {
    getType(): AccountType    
    getJson(): any
}