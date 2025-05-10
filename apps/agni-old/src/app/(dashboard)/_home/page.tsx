'use client'

import styled from '@emotion/styled';
import Button from '@/app/components/button';
import ListCardAccount from './ListCardAccount';
import { Grid} from '@mui/material';
import { gray_light, primary, primary_dark, primary_light} from '@/app/color';
import Icon from '@/app/components/Icon';
import StickButton from '@/app/components/stickButton';
import StickSelect from '@/app/components/stickSelect';
import { BarChart } from '@mui/x-charts/BarChart';
import { PieChart } from '@mui/x-charts';
import ListCardTransaction from './ListCardTransaction';
import { TransactionType } from '@/models/transaction';
import ListGoals from './ListGoal';

const OptionDisplayContainer = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
`

const GridItem = styled.div`
    display: flex;
    flex-direction: column;
    border: 1px solid ${gray_light};
    border-radius: 24px;
    flex: 1;
`
const GridItemContent = styled.div`
    padding: 1.2rem 1rem;
`
const TitleGridItem = styled.h3`
    font-size: 24;
    color: ${primary_dark};
    margin: 0
`
const TopGridItem = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
`

export default function Home() {
    return (
        <>
            <OptionDisplayContainer style={{marginTop: '1rem'}}>
                <OptionDisplayContainer>
                    <Button  title={''} icon={["fas", "calendar-days"]} type={'secondary'} onClick={() => {}} />
                    <Button title={'Ajouter Carte'} icon={undefined} type={'secondary'} onClick={() => {}} />
                </OptionDisplayContainer>
                <Button title={'Ajouter Carte'} icon={["fas", "plus"]} type={'primary'} onClick={() => {}} />
            </OptionDisplayContainer>
            <ListCardAccount />
            <Grid   marginTop={'1.15rem'} marginBottom={'1rem'} container spacing={1}>
                <Grid display={'flex'} size={{xs: 12, md: 8}}>
                    <GridItem>
                        <GridItemContent>
                            <TopGridItem>
                                <TitleGridItem>Money flow</TitleGridItem>
                            </TopGridItem>
                            <div style={{flex: 1}}>
                                <BarChart 
                                    grid={{horizontal: true}}
                                    series={[
                                        { data: [35, 44, 63, 28, 45, 36], label: 'Income', color: primary},
                                        { data: [51, 6, 85, 41, 25, 55], label: 'Expense', color: primary_light},
                                    ]}
                                    height={180}
                                    borderRadius={35}
                                    xAxis={[{ data: ['Jan', 'Fev', 'Mar', 'Avr', 'Mai', 'Jun'], scaleType: 'band', dataKey: 'month' }]}
                                />
                            </div>
                        </GridItemContent> 
                    </GridItem>
                </Grid>
                <Grid display={'flex'} size={{xs: 12, md: 4}}>
                    <GridItem>
                        <GridItemContent>
                            <TopGridItem>
                                <TitleGridItem>Budgets</TitleGridItem>
                                <Icon href='/budgets' icon={['fas', 'up-right-from-square']} style={{fontSize: '25px', color: primary_dark}}  />
                            </TopGridItem>
                            <div style={{ flex: 1}}>
                                <PieChart 
                                    height={180}
                                    series={[
                                        {
                                            data: [{value: 56, color: primary_light, label: 'car'}, {value: 44, color: primary, label: 'enterterment'}],
                                            innerRadius: 40,
                                            outerRadius: 50,
                                            paddingAngle: 5,
                                            cornerRadius: 20,
                                            startAngle: -39,
                                            endAngle: 225,
                                            cx: 100,
                                            cy: 100,
                                        }
                                    ]}

                                />
                            </div>
                        </GridItemContent> 
                    </GridItem>
                </Grid>
                <Grid display={'flex'} size={{xs: 12, md: 8}}>
                    <GridItem>
                        <GridItemContent>
                            <TopGridItem>
                                <TitleGridItem>Transaction Recente</TitleGridItem>
                                <div style={{display: 'flex', flexDirection: 'row', alignItems: 'center'}}>
                                    <StickSelect title='' handleChange={() => {}} items={[{value: 'all', label: 'Tous les comptes'}]} type='secondary' value='all' />
                                    <StickButton title='Voir tous' type={'secondary'} onClick={() => {}} />
                                </div>
                            </TopGridItem>
                            <ListCardTransaction 
                            transactions={[
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
                                // {
                                //     transactionId:'dsf',
                                //     amount: 50, 
                                //     category: {category_id: 'df', color: primary, icon: 'fa-solid fa-basket-shopping', title: 'Shop'}, 
                                //     date: "15 Auguste 2025", 
                                //     description:"test description", 
                                //     type: TransactionType.DEBIT,
                                //     tags: []
                                // },
                            ]} 
                                onClickTransaction={(id: string) => {}} 
                            />
                        </GridItemContent>
                    </GridItem>
                </Grid>
                <Grid display={'flex'} size={{xs: 12, md: 4}}>
                    <GridItem>
                        <GridItemContent>
                            <TopGridItem>
                                <TitleGridItem>But d&apos;epargne</TitleGridItem>
                                <Icon href='/goals' icon={['fas', 'up-right-from-square']} style={{fontSize: '25px', color: primary_dark}}  />
                            </TopGridItem>
                            <ListGoals goals={[
                                {
                                    title: 'Macbook Pro',
                                    targetAmount: 1650.50,
                                    amount: 450
                                },
                                {
                                    title: 'New car donw',
                                    targetAmount: 10000.30,
                                    amount: 4256
                                },
                                {
                                    title: 'Clavier pro',
                                    targetAmount: 175.50,
                                    amount: 20
                                }
                            ]} />
                        </GridItemContent> 
                    </GridItem>
                </Grid>
            </Grid>
        </>
    )
}