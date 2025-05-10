import { styled } from '@mui/material/styles'
import { primary, light, primary_dark, gray_light } from '@/app/color';
import { FormControl, InputLabel, NativeSelect } from '@mui/material';
import InputBase from '@mui/material/InputBase';

const BootstrapInputPrimary  = styled(InputBase)(() => ({
    'label + &': {
        marginTop: 3
    },
    '& .MuiInputBase-input': {
        padding: '3px 12px',
        fontWeight: '100',
        fontSize: '12px',
        borderRadius:'24px',
        backgroundColor: `${primary}`,
        color: `${light}`,
        margin: '2px'
    },
    '&:focus': {
        borderRadius: '24px',
        borderColor: '#80bdff',
        boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
    },
}))

const BootstrapInputSecondary = styled(InputBase)(() => ({
    'label + &': {
       
    },
    '& .MuiInputBase-input': {
        padding: '3px 12px',
        fontWeight: '100',
        fontSize: '12px',
        textAlign: 'center',
        borderRadius:'24px',
        border: `1px solid ${gray_light}`,
        color: `${primary_dark}`,
        margin: '2px'
    },
    '&:focus': {
        borderRadius: '24px',
        borderColor: '#80bdff',
        boxShadow: '0 0 0 0.2rem rgba(0,123,255,.25)',
    },
}))

type Props = {
    title: string
    type: 'primary' | 'secondary'
    items: {value: string, label: string}[]
    value: string
    handleChange: (ev: {target: {value: string}}) => void
}

export default function StickSelect({ type, items, title, value, handleChange }: Props) {
    if (type === 'primary')
        return (
            <FormControl>
                <InputLabel htmlFor="demo-customized-textbox">{title}</InputLabel>
                <NativeSelect
                    id="stick-select-secondary"
                    value={value}
                    onChange={handleChange}
                    input={<BootstrapInputPrimary/>}
                >
                    {
                        items.map((item, index) => (
                            <option key={index} value={item.value} >{item.label}</option>
                        ))
                    }
                </NativeSelect>
            </FormControl>
        )

    if (type === 'secondary')
        return (
            <FormControl >
                <InputLabel htmlFor="demo-customized-textbox">{title}</InputLabel>
                <NativeSelect
                    id="stick-select-secondary"
                    value={value}
                    onChange={handleChange}
                    input={<BootstrapInputSecondary/>}
                >
                    {
                        items.map((item, index) => (
                            <option key={index} value={item.value} >{item.label}</option>
                        ))
                    }
                </NativeSelect>
            </FormControl>
        )
}