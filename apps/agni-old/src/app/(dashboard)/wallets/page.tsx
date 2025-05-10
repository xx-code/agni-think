'use client'
import styled from '@emotion/styled';
import WalletActionCard from './WalletActionCard';
import WalletCard from './WalletCard';
import Button from '@/app/components/button';
import { Grid, Stack } from '@mui/material';
import { gray_light } from '@/app/color';

const CardContainer = styled.div`
    display: flex;
    flex-direction: row;
` 
const WalletContenair = styled.div`
    display: flex;
    align-self: flex-end;
    flex-direction: column;
    width: 100%;
    height: max-content;
    margin-left: 10px;
`
const ListWalletCard = styled.div`
    display: flex;
    flex-direction: row;
    over-flow-x: auto;
    justify-content: space-around;
`
const GridItem = styled.div`
    display: flex;
    flex-direction: column;
    border: 1px solid ${gray_light};
    border-radius: 24px;
    height: 225px;
    flex: 1;
`

export default function Wallets() {
    return (
        <>
            <CardContainer>
                <WalletActionCard  />
                <WalletContenair>
                    <div style={{display: 'flex', flexDirection: 'row-reverse', marginBottom: '1.15rem'}}>
                        <Button title='Ajouter un Compte' icon={['fas', 'plus']} onClick={() => {}} type='primary' />
                    </div>
                    <ListWalletCard>
                        <WalletCard title='Princal' amount={15282} symbole='$' id='DF' onClickDelete={() => {}} onClickUpdate={() => {}}/>
                        <WalletCard title='Princal' amount={15282} symbole='$' id='DF' onClickDelete={() => {}} onClickUpdate={() => {}}/>
                        <WalletCard title='Princal' amount={15282} symbole='$' id='DF' onClickDelete={() => {}} onClickUpdate={() => {}}/> 
                        <WalletCard title='Princal' amount={15282} symbole='$' id='DF' onClickDelete={() => {}} onClickUpdate={() => {}}/>
                    </ListWalletCard>
                </WalletContenair> 
            </CardContainer>
            <Grid  marginTop={'1.15rem'}  spacing={1} container>
                <Grid size={{xs:12, md: 8}}>
                    <Grid container spacing={1}>
                        <Grid size={12}>
                            <GridItem>
                            </GridItem>
                        </Grid>
                        <Grid size={12}>
                            <GridItem >
                            </GridItem>
                        </Grid>  
                    </Grid>
                </Grid> 
                <Grid size={{xs: 12, md:4}} >
                    <GridItem style={{height: '100%'}}>

                    </GridItem>
                </Grid>
            </Grid>
        </>   
    )
}