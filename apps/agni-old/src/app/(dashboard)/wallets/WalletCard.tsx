import { gray_light } from '@/app/color';
import styled from '@emotion/styled';
import Icon from '@/app/components/Icon';

const CardContainer = styled.div`
    border-radius: 24px;
    border: 1px solid ${gray_light};
    padding: 1rem;
    width: 156px;
`

const TopCard = styled.div`
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    align-items: center;
`
const CardTitle = styled.h3`
    font-weight: 500;
    margin: 0;
`
const CardAction = styled.div`
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: space-arround;
`

const CardAmount = styled.h3`
    margin: 0;
    margin-top: 1.25rem;
    font-size: 1.85rem; 
    font-weight: 500;
`

type Props = {
    id: string
    symbole: string
    amount: number
    title: string
    onClickUpdate: (id: string) => void
    onClickDelete: (id: string) => void
}

export default function WalletCard({id, symbole, amount, title, onClickUpdate, onClickDelete}: Props) {
    return (
        <CardContainer>
            <TopCard>
                <CardTitle>{title}</CardTitle>
                <CardAction>
                    <Icon icon={['fas', 'edit']}  style={{fontSize: '14px'}} />
                    <Icon icon={['fas', 'trash']} style={{fontSize: '14px'}} />
                </CardAction>
            </TopCard>
            <CardAmount>{symbole}{amount}</CardAmount>
        </CardContainer>
    )
}