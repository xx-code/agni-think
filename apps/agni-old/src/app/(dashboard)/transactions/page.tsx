'use client'

import { gray_light, primary } from '@/app/color';
import Button from '@/app/components/button';
import Select from '@/app/components/select';
import styled from '@emotion/styled';
import ListTransaction from './ListTransaction';
import { TransactionType } from '@/models/transaction';
import { Pagination } from '@mui/material';

const TopActionBar = styled.div`
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
    margin-top: 1rem;
`
const FilterBar = styled.div`
    display: flex;
    flex-direction: row;
    align-items: center;
`

const LeftContentActionBar = styled.div`
    display: flex;
    align-items: center;
`
const RigthContentActionBar = styled.div`
    display: flex;
    flex-direction: row-reverse;
`

const ContainerTransaction = styled.div`
    padding: 1.15rem;
    border-radius:24px;
    margin-top: 1rem;
    border: 1px solid ${gray_light};
    flex: 1;
`

export default function Transactions() {
    return (
        <>
            <TopActionBar>
                <LeftContentActionBar>
                    <Select title={''} type={'secondary'} items={[{value: 'all', label: 'Tout les comptes'}]} value={'all'} handleChange={() => {}} />
                    <Button title={`Balance: $ ${17500.00}`} type={'secondary'} onClick={() =>{}} />
                    <Button title='' icon={['fas', 'sliders']} type='secondary' onClick={() => {}}/>
                </LeftContentActionBar>
                <RigthContentActionBar>
                    <Button title='Ajouter transaction' icon={['fas', 'plus']} type='primary' onClick={() => {}}/>
                </RigthContentActionBar>
            </TopActionBar>
            <FilterBar>

            </FilterBar>
            <div style={{display: 'flex', flexDirection: 'column', height: '100%', marginBottom: '1.15rem'}}>
              <ContainerTransaction>
                <ListTransaction transactions={[
                        {
                                transactionId:'dsf',
                                amount: 150, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.CREDIT,
                                tags: []
                            },
                            {
                                transactionId:'dsf',
                                amount: 250, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.CREDIT,
                                tags: [{tagId: 'df', color: '', value: 'tag'}]
                            },
                            {
                                transactionId:'dsf',
                                amount: 150, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.DEBIT,
                                tags: []
                            },
                            {
                                transactionId:'dsf',
                                amount: 50, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.DEBIT,
                                tags: []
                            },
                             {
                                transactionId:'dsf',
                                amount: 150, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.CREDIT,
                                tags: []
                            },
                            {
                                transactionId:'dsf',
                                amount: 250, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.CREDIT,
                                tags: [{tagId: 'df', color: '', value: 'tag'}]
                            },
                            {
                                transactionId:'dsf',
                                amount: 150, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.DEBIT,
                                tags: []
                            },
                            {
                                transactionId:'dsf',
                                amount: 50, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.DEBIT,
                                tags: []
                            },
                             {
                                transactionId:'dsf',
                                amount: 150, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.CREDIT,
                                tags: []
                            },
                            {
                                transactionId:'dsf',
                                amount: 250, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.CREDIT,
                                tags: [{tagId: 'df', color: '', value: 'tag'}]
                            },
                            {
                                transactionId:'dsf',
                                amount: 150, 
                                category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                date: "15 Auguste 2025", 
                                description:"test description", 
                                type: TransactionType.DEBIT,
                                tags: []
                            },
                            
                    ]} onClickTransaction={() => {}} />
                </ContainerTransaction>  
                <Pagination count={10} style={{marginTop: '1rem'}} />
            </div>
            
        </>
    )
}