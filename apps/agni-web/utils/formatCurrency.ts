export default function formatCurrency(value: number) {
    return new Intl.NumberFormat('en-US', {style: 'currency',currency: 'CAD'}).format(value)
}