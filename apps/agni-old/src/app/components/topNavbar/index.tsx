'use client'

import styled from '@emotion/styled';

import { primary_light, dark, gray} from '@/app/color';

import { usePathname } from 'next/navigation';
import Icon from '../Icon';


const TopNavContainer = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 1.05rem;
`

const Title = styled.h1`
    color: ${dark};
    margin: 0;
`
const SubTitle = styled.p`
    color: ${gray};
    margin: 0;
`

const IconNotification = styled.div`
    background-color: ${primary_light};
    border-radius: 12px;
    padding: 1rem;
`

const AlertIcon = styled.div`
    height: 10px;
    width: 10px;
    border-radius: 100%;
    background-color: #DC4C4C;
    position: absolute;
    bottom: -5px;
    right: -5px;
`

function IconNotificationRender() {
    return(
        <IconNotification >
            <div style={{position: 'relative'}}>
                <Icon  style={{width: "20px", height: "100%", color: ""}} icon={["fas", "bell"]} />
                <AlertIcon />
            </div>
        </IconNotification>
    )
}

type TitleData = {
    title: string
    subTitle: string
}

const pathMapper = new Map<string, TitleData>()
pathMapper.set('/', {title: 'Bienvenu, Auguste!', subTitle: 'Il est temps de jeter un coup d\'oeil au portefeuil'})
pathMapper.set('/transactions', {title: 'Transactions', subTitle: 'Un angle de vue global sur votre flux d\'argent'})
pathMapper.set('/wallets', {title: 'Vos comptes', subTitle: 'Gestion de vos different comptes'})
export default function TopNav() {
    const pathname = usePathname()

    return (
        <TopNavContainer>
            <div>
                <Title>{pathMapper.get(pathname)?.title}</Title>
                <SubTitle>{pathMapper.get(pathname)?.subTitle}</SubTitle>
            </div>
            <div>
                <IconNotificationRender />
            </div>
        </TopNavContainer>
    )
}