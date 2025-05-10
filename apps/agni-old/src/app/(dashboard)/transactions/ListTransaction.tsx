import CardTransaction, { CardTranscationValue } from "@/app/components/cardTransaction"

type Props = {
    transactions: CardTranscationValue[]
    onClickTransaction: (id: string) => void
}

export default function ListTransaction({ transactions, onClickTransaction }: Props) {
    return (
        <>
            {
                transactions.map((transaction, index) => (
                    <CardTransaction key={index} transaction={transaction} onClick={() => onClickTransaction(transaction.transactionId)} 
                    style={{marginBottom: transactions.length - 1 === index ? '0': '11px' }} />
                ))
            } 
        </>
    )
}