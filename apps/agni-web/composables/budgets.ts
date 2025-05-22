
export type BudgetType = {
    id: string
    title: string
    target: number
    amount: number
}

export const useListBudget = (): Ref<BudgetType[]> => {

    const budgets: BudgetType[] = [
        {
            id: 'bud-1',
            title: 'Budget 1',
            target: 150,
            amount: 83.56
        },
        {
            id: 'bud-2',
            title: 'Budget 2',
            target: 85,
            amount: 56
        },
        {
            id: 'bud-3',
            title: 'Budget 3',
            target:  50,
            amount: 50
        },
        {
            id: 'bud-4',
            title: 'Budget 4',
            target:  100,
            amount: 50
        }
    ]

    return ref(budgets)
}

function generateFlatRGBColor(alpha: number) {
  // Define a list of flat UI colors (based on Flat UI palette)
  const flatColors = [
    [26, 188, 156],   // Turquoise
    [46, 204, 113],   // Emerald
    [52, 152, 219],   // Peter River
    [155, 89, 182],   // Amethyst
    [241, 196, 15],   // Sun Flower
    [230, 126, 34],   // Carrot
    [231, 76, 60],    // Alizarin
    [149, 165, 166],  // Concrete
    [52, 73, 94],     // Wet Asphalt
    [22, 160, 133],   // Green Sea
    [39, 174, 96],    // Nephritis
    [41, 128, 185],   // Belize Hole
    [142, 68, 173],   // Wisteria
    [243, 156, 18],   // Orange
    [211, 84, 0],     // Pumpkin
    [192, 57, 43],    // Pomegranate
    [127, 140, 141],  // Asbestos
    [44, 62, 80]      // Midnight Blue
  ];

  const color = flatColors[Math.floor(Math.random() * flatColors.length)];
  return `rgba(${color[0]}, ${color[1]}, ${color[2]}, ${alpha})`;
}


export const formatBudgetDataForChart = (budgets: BudgetType[]) => {
const labels = budgets.map(budget => budget.title + " - " + ((budget.amount/budget.target)* 100).toFixed(1) + "%")
    const data: number[]= []
    const reactiveColor: string[] = []
    budgets.forEach(budget => {
        data.push(budget.target)
        const alpha = budget.amount/budget.target
        reactiveColor.push(`rgba(102,85,215, ${alpha})`)
    })

    return {
        labels: labels,
        datasets: [{
            label: 'Budgets',
            data: data,
            backgroundColor: reactiveColor,
        }]
    }
}