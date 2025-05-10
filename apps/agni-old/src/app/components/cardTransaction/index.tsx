import Tag from '../tag';
import styled from '@emotion/styled';
import matchSystemIcon from '@/_utils/matchSystemIcon';
import { TransactionType } from '@/models/transaction';
import Icon from '../Icon';
import { gray, primary_dark, primary_light } from '@/app/color';
import { CSSProperties } from 'react';

export type CardTranscationValue = {
    transactionId: string
    description: string 
    type: TransactionType
    amount: number
    category: {category_id: string, title: string, icon: string, color: string|null}
    tags: {tagId: string, value: string, color: string|null}[]
    date: string 
}

const CardTransactionStyle = styled.div`
    display: flex;
    flexDirection: row;
    padding: 0.25rem 1rem;
    justify-content: space-between;
    align-items: center;
    background-color: ${primary_light};
    border-radius: 20px;
`
const CardTransactionDetail = styled.div`
    display: flex;
    flexDirection: row;
    align-items: center;
`

const IconStyle = styled.div`
    width: 25px;
    height: 25px;
    background-color: white;
    border-radius: 0.6em;

    display: flex;
    justify-content: center;
    align-items: center;
`

const CardDate = styled.p`
    margin: 0;
    font-weight: 200;
    color: ${gray};
    font-size: 0.9rem
`

const CardTitle = styled.p`
    margin: 0;
    margin-left: 5px;
    font-weight: bold;
    color: ${primary_dark};
    font-size: 1rem;
`

const CardListTag = styled.div`
    display: flex;
    flex-direction: row;
    align-items: center;
    margin-left: 5px;
`

const CardDescription = styled.p`
    font-size: 0.92rem;
    color: ${gray};
    margin: 0;
    margin-left: 5px;
`

const CardAmount = styled.p`
    margin: 0;
    font-size: 1rem;
    font-weight: 500;
    color: ${primary_dark}
`

const CardRightDetail = styled.div`
    display: flex;
    flex-direction: column;
    align-items: flex-end;
`

type Props = {
    transaction: CardTranscationValue
    style?: CSSProperties
    onClick: (id: string) => void 
}

export default function CardTransaction({transaction, style, onClick} : Props) {

    return (
        <CardTransactionStyle style={style} onClick={() => onClick(transaction.transactionId)}>
            <CardTransactionDetail>
                <IconStyle>
                    <Icon style={{width: '16px', height: '25px', color: primary_dark}} icon={matchSystemIcon(transaction.category.icon)} />
                </IconStyle>
                <CardTitle>{transaction.category.title}</CardTitle>
                <CardDescription>{transaction.description}</CardDescription>
                <CardListTag >
                    {
                        transaction.tags.map((tag, index) => (
                            <Tag key={index} id={tag.tagId} title={tag.value} color={tag.color!} />
                        ))
                    }
                </CardListTag>
            </CardTransactionDetail>
            <CardRightDetail>
                <CardAmount>
                    {
                        transaction.type === TransactionType.CREDIT ?
                            "$" + transaction.amount.toString()
                        :
                        "-" + "$" + transaction.amount.toString()
                    }
                </CardAmount>
                <CardDate>{transaction.date}</CardDate>
            </CardRightDetail>
        </CardTransactionStyle>
    )
}