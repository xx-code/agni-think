import { SavingRepository } from "@core/repositories/savingRepository";
import { KnexConnector } from "./postgreSqlConnector";
import { SaveGoal, SaveGoalItem } from "@core/domains/entities/saveGoal";
import { Knex } from "knex";
import { Money } from "@core/domains/entities/money";
import { ResourceNotFoundError } from "@core/errors/resournceNotFoundError";

export class PostgreSqlSavingRepository extends KnexConnector implements SavingRepository {

    constructor(connector: Knex) {
        super(connector);
    }
    
    async initialisation(): Promise<void> {
        let isExist = await this.connector.schema.hasTable('save_goals') 
        if (!isExist) {
            await this.connector.schema.createTable('save_goals', (table) => {
                table.uuid('save_goal_id').primary()
                table.string('title')
                table.float('target')
                table.float('balance')
                table.string('description')
            })
        }

        isExist = await this.connector.schema.hasTable('save_goal_items') 
        if (!isExist) {
            await this.connector.schema.createTable('save_goal_items', (table) => {
                table.uuid('save_goal_item_id').primary()
                table.uuid('save_goal_id').index().references('save_goal_id').inTable('save_goals').onDelete('CASCADE')
                table.string('title')
                table.string('link')
                table.float('price')
                table.string('html_to_track')
            })
        }
        
    }

    async create(saveGoal: SaveGoal): Promise<void> {
        await this.connector('save_goals').insert({
            save_goal_id: saveGoal.getId(),
            title: saveGoal.getTitle(),
            target: saveGoal.getTarget().getAmount(),
            balance: saveGoal.getBalance().getAmount(),
            description: saveGoal.getDescription()
        });
        
        if (saveGoal.getItems().length > 0)
            await this.connector('save_goal_items').insert(saveGoal.getItems().map(item => ({
                save_goal_item_id: item.id,
                save_goal_id: saveGoal.getId(),
                title: item.title,
                link: item.link,
                price: item.price,
                html_to_track: item.htmlToTrack
            })))
    }

    async get(saveGoalId: string): Promise<SaveGoal> {
        let result = await this.connector('save_goals').where('save_goal_id', saveGoalId).first();
        if (!result) {
            throw new ResourceNotFoundError(`SaveGoal with Id: ${saveGoalId} not found`);
        }
        return new SaveGoal(
            result.save_goal_id,
            result.title,
            new Money(result.target),
            new Money(result.balance),
            await this.getItems(saveGoalId),
            result.description
        );
    }

    async getAll(): Promise<SaveGoal[]> {
        let results = await this.connector('save_goals').select('*');
        return Promise.all(results.map(async result => new SaveGoal(
            result.save_goal_id,
            result.title,
            new Money(result.target),
            new Money(result.balance),
            await this.getItems(result.save_goal_id),
            result.description
        )));
    }

    async update(saveGoal: SaveGoal): Promise<void> {
        await this.connector('save_goals').where('save_goal_id', saveGoal.getId()).update({
            title: saveGoal.getTitle(),
            target: saveGoal.getTarget().getAmount(),
            balance: saveGoal.getBalance().getAmount(),
            description: saveGoal.getDescription()
        });

        await this.connector('save_goal_items').whereIn('save_goal_item_id', saveGoal.__del_event_item).del()
        if (saveGoal.__add_event_item.length > 0)
            await this.connector('save_goal_items').insert(saveGoal.__add_event_item.map(item => ({
                save_goal_item_id: item.id,
                save_goal_id: saveGoal.getId(),
                title: item.title,
                link: item.link,
                price: item.price,
                html_to_track: item.htmlToTrack
            })))
    }

    async delete(saveGoalId: string): Promise<void> {
        await this.connector('save_goals').where('save_goal_id', saveGoalId).del();
    }

    async getItems(saveGoalId: string): Promise<SaveGoalItem[]> {
        let results = await this.connector('save_goal_items').where('save_goal_id', saveGoalId).select('*');
        return results.map(result => ({
            id: result.save_goal_item_id,
            title: result.title,
            link: result.link,
            price: new Money(result.price),
            htmlToTrack: result.html_to_track
        }));
    }

}