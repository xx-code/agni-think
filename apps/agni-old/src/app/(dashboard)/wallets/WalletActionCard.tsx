import { gray_light } from '@/app/color';
import Button from '@/app/components/button';
import StickSelect from '@/app/components/stickSelect';
import styled from '@emotion/styled';

const WalletCardContainer = styled.div`
    border-radius: 24px;
    border: 1px solid ${gray_light};
    padding: 1rem;
    margin-top: 1rem;
`
const TopWalletCard = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
`
const CardTitle = styled.h3`
    margin: 0;
    font-weight: 500;
`
const CardAmount = styled.h3`
    font-size: 1.85rem;
    font-weight: 500;
    margin-top: 1.25rem;
`
const ActionButtonContainer = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-arround;
`
export default function WalletActionCard() {
    return (
        <WalletCardContainer>
            <TopWalletCard>
                <CardTitle>Total Balance </CardTitle>                
                <StickSelect title="" type={'secondary'} items={[{value: 'all', label: 'Total balance'}]} value={'all'} handleChange={() => {}} />
            </TopWalletCard>
           <CardAmount>$157000</CardAmount> 
           <div style={{height: '15px', width: '100%'}}></div>
           <ActionButtonContainer>
                <Button title={'Transfert'} type={'primary'} icon={['fas', 'money-bill-transfer']} onClick={() => {}} />
                <Button title={'Freeze'} type={'primary'} onClick={() => {}} icon={['fas', 'snowflake']} />
                <Button title="" type='secondary' icon={['fas', 'ellipsis']} onClick={() => {}} />
           </ActionButtonContainer>
        </WalletCardContainer>
    )
}