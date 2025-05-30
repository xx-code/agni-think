import { TransactionRepository, TransactionFilter, SortBy } from "../../core/repositories/transactionRepository";
import { Transaction, TransactionType } from "@/core/domains/entities/transaction";
import { TransactionPaginationResponse } from "@/core/domains/metaData/transaction";
import { SqlLiteRepository } from "./sql_lite_connector";
import { TransactionDto, TransactionMapper, TransactionPaginationMapper } from "@/core/mappers/transaction";
import { isEmpty } from "@/core/domains/helpers";
import { SAVING_CATEGORY_ID, TRANSFERT_CATEGORY_ID } from "@/core/domains/constants";

export class SqlLiteTransaction extends SqlLiteRepository implements TransactionRepository {
    private async getAllTag(id_transaction: string): Promise<string[]> {
        return new Promise(async (resolve, reject) => {
            let result_tag = await this.db.all(`
                SELECT * 
                FROM 
                    transactions_tags
                WHERE transactions_tags.id_transaction = ?
                `,
                id_transaction
            );

            let tags = []
            for (let tag of result_tag) {
                tags.push(tag['id_tag']);
            }

            resolve(tags);
        });
    }

    save(request: Transaction): Promise<boolean> {
        return new Promise(async (resolve, reject) => {
            // TODO: Make it as transaction
            let dto = TransactionMapper.to_persistence(request)

            let result = await this.db.run(`
                INSERT INTO transactions (id, id_account, id_category, id_record) VALUES (?, ?, ?, ?)`,
                dto.id, dto.account_ref, dto.category_ref, dto.record
            );

            let saved_tag = true;
            if (dto.tags.length > 0) {
                for (let tag of dto.tags) {
                    let result = await this.db.run(`
                        INSERT INTO transactions_tags (id_transaction, id_tag) VALUES (?, ?)`,
                        request.id, tag
                    );
                    saved_tag = result != undefined;
                }
            }

            if (result != undefined && saved_tag) {
                resolve(true);
            } else {
                // TODO: Delete row when saving tags fault;
                resolve(false);
            } 
        })
    }

    get(id: string): Promise<Transaction | null> {
        return new Promise(async (resolve, reject) => {
            let result = await this.db.get(`
                SELECT 
                    id,
                    id_account,
                    id_category,
                    id_record
                FROM 
                transactions
                WHERE transactions.id = ?`,
                id
            );

            if(result != undefined) {
                let tags = await this.getAllTag(id);

                let transaction_dto: TransactionDto = {
                    id: result['id'],
                    record: result['id_record'],
                    account_ref: result['id_account'],
                    tags: tags,
                    category_ref: result['id_category']
                }

                resolve(TransactionMapper.to_domain(transaction_dto));
            } else {
                resolve(null);
            }
        })
    }

    getPaginations(page: number, size: number, sort_by: SortBy | null, filter_by: TransactionFilter): Promise<TransactionPaginationResponse> {
        return new Promise(async (resolve, reject) => {
            let index_start_element_in_page = (page - 1) * size;

            let filter_id_account: string[] = [];
            for (let account of filter_by.accounts) {
                filter_id_account.push(`'${account}'`);
            }

            let where_id_account = `id_account in (${filter_id_account})`;

            let filter_id_cat: string[] = [];
            for (let category of filter_by.categories) {
                filter_id_cat.push(`'${category}'`);
            }

            let where_id_catogry = `categories.id in (${filter_id_cat})`;

            let filter_id_tag: string[] = [];
            for (let tag of filter_by.tags) {
                filter_id_tag.push(`'${tag}'`);
            }

            let result_filter_by_tag = await this.db.all(`
                SELECT id_transaction, id_tag
                FROM 
                    transactions_tags
                WHERE transactions_tags.id_tag in (${filter_id_tag})
                `
            ); 

            let filter_id_transaction_tag: string[] = [];
            for(let result of result_filter_by_tag) {
                filter_id_transaction_tag.push(`"${result['id_transaction']}"` );
            }
            
            let where_id_transaction_tag = `transactions.id in (${filter_id_transaction_tag})`;

            let where = '';
            let value_where = [];
            
            if (filter_id_transaction_tag.length > 0) {
                value_where.push(where_id_transaction_tag);
            }

            if (filter_id_account.length > 0) {
                value_where.push(where_id_account);
            }

            if (filter_id_cat.length > 0) {
                value_where.push(where_id_catogry);
            }

            if (!isEmpty(filter_by.start_date)) {
                value_where.push(`date >= '${filter_by.start_date!.toString()}'`)
            }

            if (!isEmpty(filter_by.end_date)) {
                value_where.push(`date <= '${filter_by.end_date!.toString()}'`)
            }

            if (filter_by.type !== null && filter_by.type !== undefined) {
                value_where.push(`LOWER(type) = LOWER('${filter_by.type}') `)
            }

            if (value_where.length > 0) {
                where = 'WHERE ' + value_where.join(' AND ');
            }


            let results = await this.db.all(`
                SELECT transactions.id, id_account, id_category, id_record
                FROM 
                    transactions 
                JOIN accounts
                    ON accounts.id = transactions.id_account
                JOIN records
                    ON records.id = transactions.id_record
                JOIN categories
                    ON categories.id = transactions.id_category
                ${where}
                ORDER BY 
                    date(records.date) ${sort_by !== null ? (sort_by!.asc ? 'ASC':'DESC') : 'DESC'}
                ${page > 0 ? `LIMIT ${size} OFFSET ${index_start_element_in_page}` : ""}
                `
            );

            let count = await this.db.get(`
                SELECT 
                    COUNT(*) 
                FROM 
                    transactions 
                JOIN accounts
                    ON accounts.id = transactions.id_account
                JOIN records
                    ON records.id = transactions.id_record
                JOIN categories
                    ON categories.id = transactions.id_category
                ${where}`);

            let max_page = Math.ceil(count['COUNT(*)']/size);

            let transactions = [];

            for (let result of results) {
                let id_transaction = result['id'];
                let tags = await this.getAllTag(id_transaction);
                let transaction_dto: TransactionDto = {
                    id: result['id'],
                    record: result['id_record'],
                    account_ref: result['id_account'],
                    tags: tags,
                    category_ref: result['id_category']
                }

                transactions.push(transaction_dto);
            }

            let trans_result = TransactionPaginationMapper.to_domain({
                transactions: transactions,
                max_page: max_page,
                num_items: count
            })

            resolve(trans_result)
        })
    }
    getAccountBalance(id: string): Promise<number> {
        return new Promise(async (resolve, reject) => {
            let result_credit = await this.db.get(`
                SELECT id_account, SUM(records.amount) as total_price 
                FROM transactions
                JOIN records
                    ON records.id = transactions.id_record
                WHERE id_account = ? AND LOWER(type) = 'credit'
                `, 
            id);

            let credit = result_credit['total_price'];

            let result_debit = await this.db.get(`
                    SELECT id_account, SUM(records.amount) as total_price 
                    FROM transactions
                    JOIN records
                        ON records.id = transactions.id_record
                    WHERE id_account = ? AND LOWER(type) = 'debit'
                `, 
            id);

            let debit = result_debit['total_price'];

            let balance_account = credit - debit;

            resolve(balance_account);
        })
    }
    getBalance(filter_by: TransactionFilter): Promise<number> {
        return new Promise(async (resolve, reject) => {
            let filter_id_account: string[] = [];
            for (let account of filter_by.accounts) {
                filter_id_account.push(`'${account}'`);
            }

            let where_id_account = `id_account in (${filter_id_account})`;

            let filter_id_cat: string[] = [];
            for (let category of filter_by.categories) {
                filter_id_cat.push(`'${category}'`);
            }

            let exclude_cat = [TRANSFERT_CATEGORY_ID, SAVING_CATEGORY_ID]

            let where_id_catogry = `categories.id in (${filter_id_cat})`;

            let filter_id_tag: string[] = [];
            for (let tag of filter_by.tags) {
                filter_id_tag.push(`'${tag}'`);
            }

            let result_filter_by_tag = await this.db.all(`
                SELECT id_transaction, id_tag
                FROM 
                    transactions_tags
                WHERE transactions_tags.id_tag in (${filter_id_tag})
                `
            ); 

            let filter_id_transaction_tag: string[] = [];
            for(let result of result_filter_by_tag) {
                filter_id_transaction_tag.push(`"${result['id_transaction']}"` );
            }
            
            let where_id_transaction_tag = `transactions.id in (${filter_id_transaction_tag})`;

            let where_excluse = `categories.id NOT IN (${exclude_cat.map(cat => `'${cat}'`)})`

            let where_debit = '';
            let where_credit = '';
            let value_where = [];
            
            if (filter_id_transaction_tag.length > 0) {
                value_where.push(where_id_transaction_tag);
            }

            if (filter_id_account.length > 0) {
                value_where.push(where_id_account);
            }

            if (filter_id_cat.length > 0) {
                value_where.push(where_id_catogry);
            }

            if (!isEmpty(filter_by.start_date)) {
                value_where.push(`date >= '${filter_by.start_date!.toString()}'`)
            }

            if (!isEmpty(filter_by.end_date)) {
                value_where.push(`date <= '${filter_by.end_date!.toString()}'`)
            }

            if (filter_by.type === null || filter_by.type === undefined) {
                value_where.unshift("LOWER(type) = 'credit'");
                where_credit = "WHERE " + value_where.join(' AND ');
                value_where[0] = "LOWER(type) = 'debit' "
                where_debit = "WHERE "  + value_where.join(' AND ');
            } else {
                if (filter_by.type === TransactionType.CREDIT) {
                    value_where.unshift("LOWER(type) = 'credit'");
                    where_credit = "WHERE " + value_where.join(' AND ');
                }

                if (filter_by.type === TransactionType.DEBIT) {
                    value_where.unshift("LOWER(type) = 'debit'");
                    where_debit = "WHERE "  + value_where.join(' AND ');  
                }
            }

            // TODO: Refactor transfert in compute balance

            let results_credit:any = null;
            let result_total_credit_transfert:any = null;


            if (!isEmpty(where_credit)) {
                results_credit = await this.db.get(`
                    SELECT 
                        transactions.id, id_account, id_record, SUM(records.amount) as total_price 
                    FROM 
                        transactions 
                    JOIN accounts
                        ON accounts.id = transactions.id_account
                    JOIN records
                        ON records.id = transactions.id_record
                    JOIN categories
                        ON categories.id = transactions.id_category
                    ${where_credit}
                    `
                );
            }


            let credit =  results_credit !== null ? results_credit['total_price'] : 0;

            let results_debit:any = null;
            let result_total_debit_transfert:any = null;

            if (!isEmpty(where_debit)) {             
                results_debit = await this.db.get(`
                    SELECT 
                        transactions.id, id_account, id_record, SUM(records.amount) as total_price 
                    FROM 
                        transactions 
                    JOIN accounts
                        ON accounts.id = transactions.id_account
                    JOIN records
                        ON records.id = transactions.id_record
                    JOIN categories
                        ON categories.id = transactions.id_category
                    ${where_debit}
                    `
                );
            }

            //let transfert_debit =  result_total_debit_transfert !== null ? result_total_debit_transfert['total_price'] : 0;
            let debit = results_debit !== null ? results_debit['total_price'] : 0;

            credit = credit > 0 ? credit /*- transfert_credit*/ : 0;
            debit = debit > 0 ? debit /*- transfert_debit*/ : 0;
 
            let balance_account = credit - debit;

            resolve(balance_account);
        })
    }
    
    delete(id: string): Promise<boolean> {
        return new Promise(async (resolve, reject) => {
            let result = await this.db.run(`DELETE FROM transactions WHERE id = ?`, id);
            let result_2 = await this.db.run(`DELETE FROM transactions_tags WHERE id_transaction = ?`, id);

            if (result['changes'] == 0) {
                resolve(false);
            } else {
                resolve(true)
            }
        })
    }

    update(request: Transaction): Promise<boolean> {
        return new Promise(async (resolve, reject) => {
            await this.db.run(`
                UPDATE transactions SET id_account = ?, id_category = ? WHERE id = ? 
            `, request.account_ref, request.category_ref, request.id)

            if (request.__delete_event_tag.length > 0) {
                await this.db.run(`DELETE FROM transactions_tags WHERE id_tag in (?)`, request.__delete_event_tag.toString())
            }

            if (request.__add_event_tag.length > 0) {
                for (let tag of request.__add_event_tag) {
                    await this.db.run(`
                        INSERT INTO transactions_tags (id_transaction, id_tag) VALUES (?, ?)`,
                        request.id, tag
                    )
                }
            }

            resolve(true);
        })
    }

}

// export class SqlTransactionRepository implements TransactionRepository {
//     private db: any;
//     public table_name: string;
//     private table_account_name: string = '';
//     private table_category_name: string = '';
//     private table_record_name: string = '';
//     private table_tag_name: string = '';

//     constructor(table_name: string) {
//         this.table_name = table_name;
//     }

//     async init(db_file_name: string, table_account_name: string, table_category_name: string, table_tag_name: string, table_record_name: string): Promise<void> {
//         this.db = await open_database(db_file_name);
//         await this.db.exec(`
//             CREATE TABLE IF NOT EXISTS ${this.table_name} (
//                 id TEXT PRIMARY KEY,
//                 id_account TEXT NOT NULL,
//                 id_category TEXT NOT NULL,
//                 id_record TEXT NOT NULL,
//                 FOREIGN KEY (id_account) REFERENCES ${table_account_name}(id),
//                 FOREIGN KEY (id_category) REFERENCES ${table_category_name}(id),
//                 FOREIGN KEY (id_record) REFERENCES ${table_record_name}(id)
//                     ON DELETE CASCADE
//             )
//         `);

//         await this.db.exec(`
//             CREATE TABLE IF NOT EXISTS ${this.table_name}_tags (
//                 id_transaction TEXT NOT NULL,
//                 id_tag TEXT NOT NULL,
//                 FOREIGN KEY (id_transaction) REFERENCES ${this.table_name}(id)
//                     ON DELETE CASCADE
//                 FOREIGN KEY (id_tag) REFERENCES ${table_tag_name}(title) 
//             )
//         `);
        
//         this.table_account_name = table_account_name;
//         this.table_category_name = table_category_name;
//         this.table_record_name = table_record_name;
//         this.table_tag_name = table_tag_name;
//     }


//     private async get_all_tags(id_transaction: string): Promise<Tag[]> {
//         return new Promise(async (resolve, reject) => {
//             let result_tag = await this.db.all(`
//                 SELECT * 
//                 FROM 
//                     ${this.table_name}_tags
//                 JOIN ${this.table_tag_name}
//                     ON ${this.table_tag_name}.title = ${this.table_name}_tags.id_tag
//                 WHERE ${this.table_name}_tags.id_transaction = ?
//                 `,
//                 id_transaction
//             );

//             let tags = []
//             for (let tag of result_tag) {
//                 tags.push(tag['title']);
//             }

//             resolve(tags);
//         });
//     }

//     private create_transaction(id:string, result_db: any, tags: Tag[]): Transaction {
//         let account: Account = {
//             id: result_db['id_account'],
//             title: result_db['account_title'],
//             is_saving: result_db['is_saving']
//         }

//         let category: Category = {
//             id: result_db['id'],
//             title: result_db['category_title'],
//             icon: result_db['icon']
//         }


//         let record: Record = {
//             id: result_db['record_id'],
//             price: result_db['price'],
//             date: DateParser.from_string(result_db['date']),
//             description: result_db['description'],
//             type: result_db['type']
//         } 
        
//         return {
//             id: id,
//             account: account,
//             record: record,
//             category: category,
//             tags: tags
//         }
//     }

//     save(request: dbTransaction): Promise<boolean> {
//         return new Promise(async (resolve, reject) => {
//             let result = await this.db.run(`
//                 INSERT INTO ${this.table_name} (id, id_account, id_category, id_record) VALUES (?, ?, ?, ?)`,
//                 request.id, request.account_ref, request.category_ref, request.record_ref,
//             );

//             let saved_tag = true;
//             if (request.tag_ref.length > 0) {
//                 for (let tag of request.tag_ref) {
//                     let result = await this.db.run(`
//                         INSERT INTO ${this.table_name}_tags (id_transaction, id_tag) VALUES (?, ?)`,
//                         request.id, tag
//                     );
//                     saved_tag = result != undefined;
//                 }
//             }

//             if (result != undefined && saved_tag) {
//                 resolve(true);
//             } else {
//                 // TODO: Delete row when saving tags fault;
//                 resolve(false);
//             } 
//         });
//     }
//     get(id: string): Promise<Transaction | null> {
//         return new Promise(async (resolve, reject) => {
//             let result = await this.db.get(`
//                 SELECT 
//                     ${this.table_name}.id, 
//                     ${this.table_account_name}.id as id_account,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.is_saving, 
//                     ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                     ${this.table_category_name}.title as category_title, ${this.table_category_name}.icon
//                 FROM 
//                     ${this.table_name} 
//                 JOIN ${this.table_account_name}
//                     ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                 JOIN ${this.table_record_name}
//                     ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                 JOIN ${this.table_category_name}
//                     ON ${this.table_category_name}.id = ${this.table_name}.id_category
//                 WHERE ${this.table_name}.id = ?`,
//                 id
//             );

//             if(result != undefined) {
//                 let tags = await this.get_all_tags(id);

//                 let transaction = this.create_transaction(id, result, tags);

//                 resolve(transaction);
//             } else {
//                 resolve(null);
//             }
//         });
//     }
//     get_transactions_by_categories(category_ref: string[], start_date: DateParser, end_date: DateParser): Promise<Transaction[]> {
//         return new Promise(async (resolve, reject) => {
//             let results = await this.db.all(`
//                 SELECT 
//                     ${this.table_name}.id, 
//                     ${this.table_account_name}.id as id_account,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.credit_value, ${this.table_account_name}.credit_limit, 
//                     ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                     ${this.table_category_name}.title  as category_title, ${this.table_category_name}.icon
//                 FROM 
//                     ${this.table_name} 
//                 JOIN ${this.table_account_name}
//                     ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                 JOIN ${this.table_record_name}
//                     ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                 JOIN ${this.table_category_name}
//                     ON ${this.table_category_name}.title = ${this.table_name}.id_category
//                 WHERE category_title in (?) AND date >= '${start_date.toString()}' AND  date <= '${end_date.toString()}'
//                 `,
//                 category_ref
//             );
//             //AND date >= datetime(${start_date.toDateString()}) AND date <= datetime(${end_date.toDateString()})

//             let transactions = [];

//             for (let result of results) {
//                 let id_transaction = result['id'];
//                 let tags = await this.get_all_tags(id_transaction);
//                 let transaction = this.create_transaction(id_transaction, result, tags);

//                 transactions.push(transaction);
//             }

//             resolve(transactions);
//         });

//     }
//     get_transactions_by_tags(tags_ref: string[], start_date: DateParser, end_date: DateParser): Promise<Transaction[]> {
//         return new Promise(async (resolve, reject) => {
//             let result_filter_by_tag = await this.db.all(`
//                 SELECT id_transaction
//                 FROM 
//                     ${this.table_name}_tags
//                 JOIN ${this.table_tag_name}
//                     ON ${this.table_tag_name}.title = ${this.table_name}_tags.id_tag
//                 WHERE ${this.table_name}_tags.id_tag in (?)
//                 `,
//                 tags_ref.toString()
//             ); 

//             let filter_id_transaction_tag: string[] = [];
//             for(let result of result_filter_by_tag) {
//                 filter_id_transaction_tag.push(result['id_transaction']);
//             }

//             let results = await this.db.all(`
//                 SELECT 
//                     ${this.table_name}.id, 
//                     ${this.table_account_name}.id as id_account,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.is_saving, 
//                     ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                     ${this.table_category_name}.title  as category_title, ${this.table_category_name}.icon
//                 FROM 
//                     ${this.table_name} 
//                 JOIN ${this.table_account_name}
//                     ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                 JOIN ${this.table_record_name}
//                     ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                 JOIN ${this.table_category_name}
//                     ON ${this.table_category_name}.title = ${this.table_name}.id_category
//                 WHERE ${this.table_name}.id in (${filter_id_transaction_tag}) AND date >= '${start_date.toString()}' AND  date <= '${end_date.toString()}'
//                 `,
//             );

//             let transactions = [];

//             for (let result of results) {
//                 let id_transaction = result['id'];
//                 let tags = await this.get_all_tags(id_transaction);
//                 let transaction = this.create_transaction(id_transaction, result, tags);

//                 transactions.push(transaction);
//             }

//             resolve(transactions);
//         });
//     }
//     get_paginations(page: number, size: number, sort_by: dbSortBy | null, filter_by: dbFilter): Promise<dbTransactionPaginationResponse> {
//         return new Promise(async (resolve, reject) => {
//             let index_start_element_in_page = (page - 1) * size;

//             let filter_id_account: string[] = [];
//             for (let account of filter_by.accounts) {
//                 filter_id_account.push(`'${account}'`);
//             }

//             let where_id_account = `id_account in (${filter_id_account})`;

//             let filter_id_cat: string[] = [];
//             for (let category of filter_by.categories) {
//                 filter_id_cat.push(`'${category}'`);
//             }

//             let where_id_catogry = `${this.table_category_name}.id in (${filter_id_cat})`;

//             let filter_id_tag: string[] = [];
//             for (let tag of filter_by.tags) {
//                 filter_id_tag.push(`'${tag}'`);
//             }

//             let result_filter_by_tag = await this.db.all(`
//                 SELECT id_transaction, id_tag
//                 FROM 
//                     ${this.table_name}_tags
//                 JOIN ${this.table_tag_name}
//                     ON ${this.table_tag_name}.title = ${this.table_name}_tags.id_tag
//                 WHERE ${this.table_name}_tags.id_tag in (${filter_id_tag})
//                 `
//             ); 

//             let filter_id_transaction_tag: string[] = [];
//             for(let result of result_filter_by_tag) {
//                 filter_id_transaction_tag.push(`"${result['id_transaction']}"` );
//             }
            
//             let where_id_transaction_tag = `${this.table_name}.id in (${filter_id_transaction_tag})`;

//             let where = '';
//             let value_where = [];
            
//             if (filter_id_transaction_tag.length > 0) {
//                 value_where.push(where_id_transaction_tag);
//             }

//             if (filter_id_account.length > 0) {
//                 value_where.push(where_id_account);
//             }

//             if (filter_id_cat.length > 0) {
//                 value_where.push(where_id_catogry);
//             }

//             const is_start_date_empty = filter_by.start_date === null || filter_by.start_date === undefined;
//             const is_end_date_empty = filter_by.end_date === null || filter_by.end_date === undefined;

//             if (!is_start_date_empty) {
//                 value_where.push(`date >= '${filter_by.start_date!.toString()}'`)
//             }

//             if (!is_end_date_empty) {
//                 value_where.push(`date <= '${filter_by.end_date!.toString()}'`)
//             }

//             if (filter_by.type !== null && filter_by.type !== undefined) {
//                 value_where.push(`LOWER(type) = LOWER('${filter_by.type}') `)
//             }

//             if (value_where.length > 0) {
//                 where = 'WHERE ' + value_where.join(' AND ');
//             }


//             let results = await this.db.all(`
//                 SELECT 
//                     ${this.table_name}.id, 
//                     ${this.table_account_name}.id as id_account,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.is_saving, 
//                     ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                     ${this.table_category_name}.title as category_title, ${this.table_category_name}.icon
//                 FROM 
//                     ${this.table_name} 
//                 JOIN ${this.table_account_name}
//                     ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                 JOIN ${this.table_record_name}
//                     ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                 JOIN ${this.table_category_name}
//                     ON ${this.table_category_name}.id = ${this.table_name}.id_category
//                 ${where}
//                 ORDER BY 
//                     date(${this.table_record_name}.date) ${sort_by !== null ? (sort_by!.asc ? 'ASC':'DESC') : 'ASC'}
//                 ${page > 0 ? `LIMIT ${size} OFFSET ${index_start_element_in_page}` : ""}
//                 `
//             );

//             let count = await this.db.get(`
//                 SELECT 
//                     COUNT(*) 
//                 FROM 
//                     ${this.table_name} 
//                 JOIN ${this.table_account_name}
//                     ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                 JOIN ${this.table_record_name}
//                     ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                 JOIN ${this.table_category_name}
//                     ON ${this.table_category_name}.id = ${this.table_name}.id_category
//                 ${where}`);

//             let max_page = Math.ceil(count['COUNT(*)']/size);

//             let transactions = [];

//             for (let result of results) {
//                 let id_transaction = result['id'];
//                 let tags = await this.get_all_tags(id_transaction);
//                 let transaction = this.create_transaction(id_transaction, result, tags);

//                 transactions.push(transaction);
//             }

//             resolve({
//                 current_page: page,
//                 max_page: max_page,
//                 transactions: transactions
//             });
//         });
//     }
//     get_account_balance(id: string): Promise<number> {
//         return new Promise(async (resolve, reject) => {
//             let result_credit = await this.db.get(`
//                     SELECT id_account, SUM(${this.table_record_name}.price) as total_price 
//                     FROM ${this.table_name}
//                     JOIN ${this.table_record_name}
//                         ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                     WHERE id_account = ? AND LOWER(type) = 'credit'
//                 `, 
//             id);

//             let credit = result_credit['total_price'];

//             let result_debit = await this.db.get(`
//                     SELECT id_account, SUM(${this.table_record_name}.price) as total_price 
//                     FROM ${this.table_name}
//                     JOIN ${this.table_record_name}
//                         ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                     WHERE id_account = ? AND LOWER(type) = 'debit'
//                 `, 
//             id);

//             let debit = result_debit['total_price'];

//             let balance_account = credit - debit;

//             resolve(balance_account);
//         });
//     }
//     get_balance(filter_by: dbFilter): Promise<number> {
//         return new Promise(async (resolve, reject) => {

//             let filter_id_account: string[] = [];
//             for (let account of filter_by.accounts) {
//                 filter_id_account.push(`'${account}'`);
//             }

//             let where_id_account = `id_account in (${filter_id_account})`;

//             let filter_id_cat: string[] = [];
//             for (let category of filter_by.categories) {
//                 filter_id_cat.push(`'${category}'`);
//             }

//             let where_id_catogry = `${this.table_category_name}.id in (${filter_id_cat})`;

//             let filter_id_tag: string[] = [];
//             for (let tag of filter_by.tags) {
//                 filter_id_tag.push(`'${tag}'`);
//             }

//             let result_filter_by_tag = await this.db.all(`
//                 SELECT id_transaction, id_tag
//                 FROM 
//                     ${this.table_name}_tags
//                 JOIN ${this.table_tag_name}
//                     ON ${this.table_tag_name}.title = ${this.table_name}_tags.id_tag
//                 WHERE ${this.table_name}_tags.id_tag in (${filter_id_tag})
//                 `
//             ); 

//             console.log(result_filter_by_tag)

//             let filter_id_transaction_tag: string[] = [];
//             for(let result of result_filter_by_tag) {
//                 filter_id_transaction_tag.push(`"${result['id_transaction']}"` );
//             }
            
//             let where_id_transaction_tag = `${this.table_name}.id in (${filter_id_transaction_tag})`;

            

//             let where_debit = '';
//             let where_credit = '';
//             let value_where = [];
            
//             if (filter_id_transaction_tag.length > 0) {
//                 value_where.push(where_id_transaction_tag);
//             }

//             if (filter_id_account.length > 0) {
//                 value_where.push(where_id_account);
//             }

//             if (filter_id_cat.length > 0) {
//                 value_where.push(where_id_catogry);
//             }

//             const is_start_date_empty = filter_by.start_date === null || filter_by.start_date === undefined;
//             const is_end_date_empty = filter_by.end_date === null || filter_by.end_date === undefined;

//             if (!is_start_date_empty) {
//                 value_where.push(`date >= '${filter_by.start_date!.toString()}'`)
//             }

//             if (!is_end_date_empty) {
//                 value_where.push(`date <= '${filter_by.end_date!.toString()}'`)
//             }

//             if (filter_by.type === null || filter_by.type === undefined) {
//                 value_where.unshift("LOWER(type) = 'credit'");
//                 where_credit = "WHERE " + value_where.join(' AND ');
//                 value_where[0] = "LOWER(type) = 'debit' "
//                 where_debit = "WHERE "  + value_where.join(' AND ');
//             } else {
//                 if (filter_by.type === TransactionType.Credit) {
//                     value_where.unshift("LOWER(type) = 'credit'");
//                     where_credit = "WHERE " + value_where.join(' AND ');
//                 }

//                 if (filter_by.type === TransactionType.Debit) {
//                     value_where.unshift("LOWER(type) = 'debit'");
//                     where_debit = "WHERE "  + value_where.join(' AND ');  
//                 }
//             }

//             console.log(where_credit)

//             // TODO: Refactor transfert in compute balance

//             let results_credit:any = null;
//             let result_total_credit_transfert:any = null;


//             if (!is_empty(where_credit)) {
//                 /*if (where_id_catogry.includes(TRANSFERT_CATEGORY_ID) || filter_id_cat.length === 0) { 
//                     result_total_credit_transfert = await this.db.get(`
//                         SELECT 
//                             ${this.table_name}.id, 
//                             ${this.table_account_name}.id as account_id,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.credit_value, ${this.table_account_name}.credit_limit, 
//                             ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                             ${this.table_category_name}.title as category_title, ${this.table_category_name}.icon,
//                             SUM(${this.table_record_name}.price) as total_price 
//                         FROM 
//                             ${this.table_name} 
//                         JOIN ${this.table_account_name}
//                             ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                         JOIN ${this.table_record_name}
//                             ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                         JOIN ${this.table_category_name}
//                             ON ${this.table_category_name}.id = ${this.table_name}.id_category
//                         WHERE LOWER(type) = 'credit' AND ${this.table_category_name}.id = '${TRANSFERT_CATEGORY_ID}'
//                     `)
        
//                 }*/

            
//                 results_credit = await this.db.get(`
//                     SELECT 
//                         ${this.table_name}.id, 
//                         ${this.table_account_name}.id as id_account,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.is_saving, 
//                         ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                         ${this.table_category_name}.title as category_title, ${this.table_category_name}.icon,
//                         SUM(${this.table_record_name}.price) as total_price 
//                     FROM 
//                         ${this.table_name} 
//                     JOIN ${this.table_account_name}
//                         ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                     JOIN ${this.table_record_name}
//                         ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                     JOIN ${this.table_category_name}
//                         ON ${this.table_category_name}.id = ${this.table_name}.id_category 
//                     ${where_credit}
//                     `
//                 );
//             }

//             // let transfert_credit =  result_total_credit_transfert !== null ? result_total_credit_transfert['total_price'] : 0;
//             let credit =  results_credit !== null ? results_credit['total_price'] : 0;

//             let results_debit:any = null;
//             let result_total_debit_transfert:any = null;

//             if (!is_empty(where_debit)) { 
//                 /*if (where_id_catogry.includes(TRANSFERT_CATEGORY_ID) || filter_id_cat.length === 0) {
//                     result_total_debit_transfert = await this.db.get(`
//                         SELECT 
//                             ${this.table_name}.id, 
//                             ${this.table_account_name}.id as account_id,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.credit_value, ${this.table_account_name}.credit_limit, 
//                             ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                             ${this.table_category_name}.title  as category_title, ${this.table_category_name}.icon,
//                             SUM(${this.table_record_name}.price) as total_price 
//                         FROM 
//                             ${this.table_name} 
//                         JOIN ${this.table_account_name}
//                             ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                         JOIN ${this.table_record_name}
//                             ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                         JOIN ${this.table_category_name}
//                             ON ${this.table_category_name}.id = ${this.table_name}.id_category
//                         WHERE LOWER(type) = 'debit' AND ${this.table_category_name}.id = '${TRANSFERT_CATEGORY_ID}'
//                     `)
//                 }*/
                
                
//                 results_debit = await this.db.get(`
//                     SELECT 
//                         ${this.table_name}.id, 
//                         ${this.table_account_name}.id as id_account,  ${this.table_account_name}.title as account_title, ${this.table_account_name}.is_saving, 
//                         ${this.table_record_name}.id  as record_id, ${this.table_record_name}.price, ${this.table_record_name}.date, ${this.table_record_name}.description, ${this.table_record_name}.type,
//                         ${this.table_category_name}.title  as category_title, ${this.table_category_name}.icon,
//                         SUM(${this.table_record_name}.price) as total_price 
//                     FROM 
//                         ${this.table_name} 
//                     JOIN ${this.table_account_name}
//                         ON ${this.table_account_name}.id = ${this.table_name}.id_account
//                     JOIN ${this.table_record_name}
//                         ON ${this.table_record_name}.id = ${this.table_name}.id_record
//                     JOIN ${this.table_category_name}
//                         ON ${this.table_category_name}.id = ${this.table_name}.id_category
//                     ${where_debit}
//                     `
//                 );
//             }

//             //let transfert_debit =  result_total_debit_transfert !== null ? result_total_debit_transfert['total_price'] : 0;
//             let debit = results_debit !== null ? results_debit['total_price'] : 0;

//             credit = credit > 0 ? credit /*- transfert_credit*/ : 0;
//             debit = debit > 0 ? debit /*- transfert_debit*/ : 0;
 
//             let balance_account = credit - debit;

//             resolve(balance_account);
//         });
//     }
//     delete(id: string): Promise<boolean> {
//         return new Promise(async (resolve, reject) => {
//             let result = await this.db.run(`DELETE FROM ${this.table_name} WHERE id = ?`, id);
//             let result_2 = await this.db.run(`DELETE FROM ${this.table_name}_tags WHERE id_transaction = ?`, id);

//             if (result['changes'] == 0) {
//                 resolve(false);
//             } else {
//                 resolve(true)
//             }
//         });
//     }
//     update(request: dbTransaction): Promise<Transaction> {
//         return new Promise(async (resolve, reject) => {
//             await this.db.run(`
//                 UPDATE ${this.table_name} SET id_account = ?, id_category = ? WHERE id = ? 
//             `, request.account_ref, request.category_ref, request.id);

//             let result_filter_by_tag = await this.db.all(`
//                 SELECT id_transaction, id_tag
//                 FROM 
//                     ${this.table_name}_tags
//                 JOIN ${this.table_tag_name}
//                     ON ${this.table_tag_name}.title = ${this.table_name}_tags.id_tag
//                 WHERE id_transaction = ?
//                 `,
//                 request.id
//             ); 

//             let tag_to_remove = [];
//             let tag_to_add = [];

//             let tags = [];
//             for(let result of result_filter_by_tag) {
//                 if (!request.tag_ref.includes(result['id_tag'])) {
//                     tag_to_remove.push(result['id_tag']);
//                 }
//                 tags.push(result['id_tag']);
//             }

//             for (let tag of request.tag_ref) {
//                 if (!tags.includes(tag)) {
//                     tag_to_add.push(tag);
//                 }
//             }

//             if (tag_to_remove.length > 0) {
//                 await this.db.run(`DELETE FROM ${this.table_name}_tags WHERE id_tag in (?)`, tag_to_remove.toString());
//             }

//             if (tag_to_add.length > 0) {
//                 for (let tag of tag_to_add) {
//                     await this.db.run(`
//                         INSERT INTO ${this.table_name}_tags (id_transaction, id_tag) VALUES (?, ?)`,
//                         request.id, tag
//                     );
//                 }
//             }

//             let transaction = await this.get(request.id);

//             resolve(transaction!);
//         });
//     }
// }