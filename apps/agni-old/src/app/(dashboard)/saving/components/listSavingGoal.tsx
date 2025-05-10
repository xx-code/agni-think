'use client'

import { SaveGoalModel } from "@/api/models/save-goal"
import Button from "@/app/components/button"
import CardSaving from "./cardSaving"

import './listSavingGoal.css'

type Props = {
    saveGoals: SaveGoalModel[]
    onUpdateSaveGoals: (id: string) => void 
    onIncreaseSaveGoal: (id: string) => void 
    onDeleteSaveGoal: (id: string, accountId: string) => void
    onTransfertSaveGoal: (id: string) => void 
}

export default function ListSavingGoal({ saveGoals, onDeleteSaveGoal, onIncreaseSaveGoal, onTransfertSaveGoal, onUpdateSaveGoals } : Props) {
    return (
        <div>
            <div className="list-saving-goal" style={{marginTop:  '3rem'}}>
                {
                    saveGoals.map((val, index) => {
                        return (
                            <CardSaving
                                key={index}
                                title={val.title}
                                description={val.description}
                                targetPrice={val.target}
                                currentPrice={val.balance}
                                onIncrease={() => onIncreaseSaveGoal(val.id)}
                                onTransfert={() => onTransfertSaveGoal(val.id)}
                                onUpdate={() => onUpdateSaveGoals(val.id)}
                                onDelete={() => onDeleteSaveGoal(val.id, val.id)} 
                                items={val.items.map(item => ({title: item.title, price: item.price, link: item.link, priceUpdate: 0}))}                            
                            />
                            // <CardSaving 
                            //     key={index} 
                            //     savingGoald={val} 
                            //     onIncrease={() => onIncreaseSaveGoal(val.id)} 
                            //     onTransfert={() => onTransfertSaveGoal(val.id)} 
                            //     onUpdate={() => onUpdateSaveGoals(val.id)} 
                            //     onDelete={() => onDeleteSaveGoal(val.id)} 
                            // />
                        )
                    })
                }
            </div>
        </div>
    )
} 