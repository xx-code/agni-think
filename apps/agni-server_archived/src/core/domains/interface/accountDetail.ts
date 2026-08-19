import { Account } from "../constants";
import { IObjectEquality } from "./equality";

export interface IAccountDetail extends IObjectEquality {
    getType(): Account    
    getJson(): any
}