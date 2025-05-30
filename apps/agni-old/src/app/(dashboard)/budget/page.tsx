'use client';

import ListBudget from "./listBudget";
import './page.css';
import { useEffect} from "react";
import axios from "axios";
import TopNav from "../topNav";
import { useBudgetsFetching } from "../hooks/budgets";
import { useRouter } from "next/navigation";
import Button from "@/app/components/button";

export default function Budget() {
    const { budgets, errorBudget, loading, fetchBudgets } = useBudgetsFetching()
    const route = useRouter()

    async function deleteBudget(id: string) {
        try {
            let isOk = confirm('Voulez vous vraiment le supprimer');

            if (isOk) {
                await axios.delete(`/api/budget/${id}`);
            }
            await fetchBudgets()
        } catch(err) {
            console.log(err);
        }
    }

    useEffect(() => {
       fetchBudgets()
    }, [])

    return (
        <>
            <TopNav title='Les budgets'/>
            <div className="budgets">
                <div>
                    <div>                        
                        <Button backgroundColor="white" colorText="#6755D7" title="Ajouter Budget" onClick={() => route.push(`/budget/new-budget`)}/>
                    </div>
                </div>
                <div className="list">
                    <ListBudget budgets={budgets} onDelete={deleteBudget} onUpdate={(id) => route.push(`/budget/${id}`)} />
                </div>
            </div>
        </>
    )
}