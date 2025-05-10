import CardTransaction, { CardTranscationValue } from "@/app/components/cardTransaction"


type Props = {
    transactions: CardTranscationValue[]
    onClickTransaction: (id: string) => void
}

export default function ListCardTransaction({transactions, onClickTransaction}: Props) {
    return (
        <>
            {
                transactions.map((trans, index) => (
                    <CardTransaction key={index} style={{marginTop: 10}} transaction={trans} onClick={onClickTransaction} />
                ))
            }
        </>
    )
} 