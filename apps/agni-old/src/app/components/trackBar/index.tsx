import { light, primary, primary_dark, primary_light } from '@/app/color';
import styled from '@emotion/styled';
import { CSSProperties, ReactElement } from 'react';

const TopTrackBar = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
`

const TitleTrackBar = styled.p`
    font-size: 14px;
    font-weight: bold;
    color: ${primary_dark};
    margin: 0;
`

const AmountBar = styled.p`
    font-size: 14px;
    color: ${primary};
    font-weight: 100;
    margin: 0;
`

const TrackBarContainer = styled.div`
    background-color: ${primary_light};
    position: relative;
    height: 25px;
    width: 100%;
    border-radius: 20px;
`

const BarStyle = styled.div`
    left: 0;
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: ${primary};
    height: 25px;
    border-radius: 20px;
`

const Bar = ({children, width}: {width: number, children: ReactElement}) => {
    console.log(width)
    return (
        <BarStyle style={{width: `${width}%`}}>
            {children}
        </BarStyle>
    )
}

const ValueBar = styled.p`
    margin: 0;
    color: ${light};
    font-weight: 100;
    font-size: 10px;
`

type Props = {
    title: string
    targetAmount: number
    amount: number
    style?: CSSProperties
}

export default function TrackBar({title, targetAmount, amount, style}: Props) {
    const percentAmout = ((amount * 100) / targetAmount).toFixed(2)
    return (
        <div style={style}>
            <TopTrackBar>
                <TitleTrackBar>{title}</TitleTrackBar>
                <AmountBar>${targetAmount}</AmountBar>
            </TopTrackBar>
            <TrackBarContainer>
                <Bar width={Number(percentAmout)}>
                    <ValueBar>{percentAmout}%</ValueBar>
                </Bar>
            </TrackBarContainer>
        </div>
    )
}