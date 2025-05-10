import CardAccount from './CardAccount';
import styled from '@emotion/styled';

const ListCard = styled.div`
    margin-top: 1.12rem;
    width: 100%;
    overflow-x: auto;
`

export default function Render() {
    const accounts = [
        {
            title: 'Total balance',
            amount: 15700.00,
            symbole: '$',
            diffPercent: 12.7,
            period: 'mois',
            isPositif: true
        },
        {
            title: 'Principal',
            amount: 1500.00,
            symbole: '$',
            diffPercent: 6.7,
            period: 'mois',
            isPositif: true
        },
        {
            title: 'Principal',
            amount: 7500.00,
            symbole: '$',
            diffPercent: 11.0,
            period: 'mois',
            isPositif: false
        },
        {
            title: 'Principal',
            amount: 352.00,
            symbole: '$',
            diffPercent: 10.4,
            period: 'mois',
            isPositif: true
        }
    ]


    return (
        <ListCard>
            <div style={{display: 'flex', minHeight: 'min-content'}}>
                {
                    accounts.map((account, index) => (
                        <CardAccount key={index} style={{marginRight: accounts.length - 1 === index ? '' : '13px'}}  title={account.title} amount={account.amount} symbole={account.symbole} diffPercent={account.diffPercent} period={account.period} isPositif={account.isPositif} />
                    ))
                }
            </div>
        </ListCard>
    )
}