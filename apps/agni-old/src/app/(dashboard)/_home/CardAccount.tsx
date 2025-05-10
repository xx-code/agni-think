/* eslint-disable @typescript-eslint/ban-ts-comment */
import { primary, light, gray_light, primary_dark, status_error, status_error_light, status_ok, status_ok_light} from '@/app/color';
import styled from '@emotion/styled';

import { library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Link from 'next/link';
import { CSSProperties } from 'react';

// @ts-ignore
library.add(fas)

const CardAccountContainer = styled.div`
    border: 1px solid ${gray_light};
    padding: 1.2rem 1rem;
    border-radius: 24px;
    width: 300px;
`
const CardTitle = styled.div`
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 1.3rem;
`

const CardAmount = styled.div`
    color: ${primary_dark};
    font-size: 1.8rem;
    margin-top: 30px;
`

const StickGreenPercent = styled.div`
    display: flex;
    align-items: center;
    background-color: ${status_ok_light};
    color: ${status_ok};
    border-radius: 15px;
    padding: 4px 8px;
    font-size: 12px;
`

const StickRedPercent = styled.div`
    display: flex;
    align-items: center;
    background-color: ${status_error_light};
    color: ${status_error};
    border-radius: 15px;
    padding: 4px 8px;
    font-size: 12px;
`

const BottomCard = styled.div`
    display: flex;
    margin-top: 15px;
    align-items: center;
`

const SubTitleCard = styled.p`
    margin: 0;
    margin-left: 5px;
    color: ${gray_light}
`

type Props = {
    title: string
    amount: number
    symbole: string
    diffPercent: number
    period: string
    isPositif: boolean
    style: CSSProperties
}


export default function Render({title, amount, symbole, diffPercent, period, isPositif, style}: Props) {
    return (
        <CardAccountContainer style={style}>
            <CardTitle>
                <p style={{margin: 0}}>{title}</p>
                <Link style={{color: primary_dark}} href="/wallets">
                    <FontAwesomeIcon style={{fontSize: '25px'}} icon={['fas', 'up-right-from-square']} />
                </Link>
            </CardTitle>
            <CardAmount>
                {symbole}{amount}
            </CardAmount>
            <BottomCard>
                {
                    isPositif ?
                    <StickGreenPercent>
                        <FontAwesomeIcon style={{fontSize: '12px'}} icon={['fas', 'arrow-up']} />
                        {diffPercent}%
                    </StickGreenPercent>
                    :
                    <StickRedPercent>
                        <FontAwesomeIcon style={{fontSize: '12px'}} icon={['fas', 'arrow-up']} />
                        {diffPercent}%
                    </StickRedPercent>
                }
                <SubTitleCard>vs precedent {period}</SubTitleCard>
            </BottomCard>
        </CardAccountContainer>
    )
}