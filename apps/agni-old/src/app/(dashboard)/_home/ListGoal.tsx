import TrackBar from "@/app/components/trackBar"

export type ListGoalValue = {
    title: string
    targetAmount: number
    amount: number
}

type Props = {
    goals: ListGoalValue[]
}

export default function ListGoals({goals} : Props) {
    return (
        <>
            {
                goals.map((goal, index) => (
                    <TrackBar key={index} style={{marginTop: 15}} title={goal.title} targetAmount={goal.targetAmount} amount={goal.amount} />
                ))
            }
        </>
    )
}