'use client'

import styled from '@emotion/styled';
import useResponsiveWidth from '../hooks/useResponsiveWidth';
import { primary, primary_light, primary_dark, light } from '@/app/color';

import { IconProp, library } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import Icon from '../Icon';

// @ts-ignore
library.add(fas)

const NavContainer = styled.div`
    background-color: ${primary_light};
    width: 225px;
`

const NavContainerResponsive = styled.div`
    background-color: ${primary_light};
    width: 73px;
`

const NavContent = styled.div`
    padding: 1rem;
`

const TopNav = styled.div`
    margin-bottom: 75px;
    margin-left: 1em;
    margin-right: 1em;
`

const MidNav = styled.div`

`

const Title = styled.h3`
    color: ${primary_dark};
    font-size: 2em;
    font-weight: bold;
`

const NavBarButtonActive = styled.div`
    background-color: ${primary};
    color: ${light};
    font-size: 0.90rem;
    border-radius: 15px;
    display: flex;
    align-items: center;
    padding-left: 20px;
    &:hover {
        background: ${primary_light};
    }
`

const NavBarButtonActiveResponsive = styled.div`
    background-color: ${primary};
    color: ${light};
    font-size: 1rem;
    border-radius: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 1rem;
    &:hover {
        background: ${primary_light}
    }
`

const NavBarButton = styled.div`
    color: ${primary_dark};
    font-size: 0.90rem;
    border-radius: 15px;
    display: flex;
    align-items: center;
    padding-left: 20px;
    &:hover {
        background: ${primary_light};
    }
`

const NavBarButtonResponsive = styled.div`
    color: ${primary_dark};
    border-radius: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 1rem;
    &:hover {
        background: ${primary_light}
    }
`

function LogoTitle({ title }: {title: string}) {
    return (
        <div>
            <Title>{title}</Title>
        </div>
    )
}

function NavBarButtonActiveRender({ icon, link, title, isActive}: {icon: IconProp, link: string, title: string, isActive: boolean}) {
    return (
        <Link href={link} style={{textDecoration: 'none'}}>
            {
                isActive ? 
                <NavBarButtonActive>
                    <FontAwesomeIcon style={{fontSize: '15px', marginRight: '5px'}} icon={icon} />
                    <p>{title}</p>
                </NavBarButtonActive>
                :
                <NavBarButton>
                    <FontAwesomeIcon style={{fontSize: '15px', marginRight: '5px'}} icon={icon} />
                    <p>{title}</p>
                </NavBarButton>
            }
        </Link>
    )
}

function NavbarButonActiveRenderResponsive({ icon, link,  isActive }: {icon: IconProp, link: string, isActive: boolean}) {
    return (
        <Link href={link} style={{textDecoration: 'none'}}>
            {
                isActive ?
                <NavBarButtonActiveResponsive>
                    <Icon style={{fontSize: '1.15rem'}} icon={icon} />
                </NavBarButtonActiveResponsive>
                :
                <NavBarButtonResponsive>
                    <Icon style={{fontSize: '1.15rem'}} icon={icon}/>
                </NavBarButtonResponsive>
            }
        </Link>
    )
}


export function NavBarResponsive({pathname}: {pathname: string}) {

    return (
        <NavContainerResponsive>
            <NavContent>
                <TopNav>
                    <LogoTitle title='A.' />
                </TopNav>
                <MidNav>
                    <NavbarButonActiveRenderResponsive icon={["fas", "house"]} link='/' isActive={pathname === '/'} />
                    <NavbarButonActiveRenderResponsive icon={["fas", "money-bill-transfer"]} link='/transactions' isActive={pathname === '/transactions'} />
                    <NavbarButonActiveRenderResponsive icon={["fas", "wallet"]} link='/wallets' isActive={pathname === '/wallets'} />
                    <NavbarButonActiveRenderResponsive icon={["fas", "bullseye"]} link='/goals' isActive={pathname === '/goals'} />
                    <NavbarButonActiveRenderResponsive icon={["fas", "coins"]} link='/budgets' isActive={pathname === '/budgets'} />
                    <NavbarButonActiveRenderResponsive icon={["fas", "chart-line"]} link='/analystics'  isActive={pathname === '/analystics'} />
                    <NavbarButonActiveRenderResponsive icon={["fas", "gear"]} link='/settings' isActive={pathname === '/settings'} />
                </MidNav>
            </NavContent>
        </NavContainerResponsive>
    )
}

export default function NavBar() {
    const screenWidth = useResponsiveWidth()
    const pathname = usePathname()

    return (
        screenWidth > 975 ?
            <NavContainer>
                <NavContent>
                    <TopNav>
                        <LogoTitle title="Agni." />
                    </TopNav>
                    <MidNav>
                        <NavBarButtonActiveRender icon={["fas", "house"]} link='/' title='Dashboard' isActive={pathname === '/'} />
                        <NavBarButtonActiveRender icon={["fas", "money-bill-transfer"]} link='/transactions' title='Transactions' isActive={pathname === '/transactions'} />
                        <NavBarButtonActiveRender icon={["fas", "wallet"]} link='/wallets' title='Wallets' isActive={pathname === '/wallets'} />
                        <NavBarButtonActiveRender icon={["fas", "bullseye"]} link='/goals' title='Goals' isActive={pathname === '/goals'} />
                        <NavBarButtonActiveRender icon={["fas", "coins"]} link='/budgets' title='Budgets' isActive={pathname === '/budgets'} />
                        <NavBarButtonActiveRender icon={["fas", "chart-line"]} link='/analystics' title='Analystics' isActive={pathname === '/analystics'} />
                        <NavBarButtonActiveRender icon={["fas", "gear"]} link='/settings' title='Settings' isActive={pathname === '/settings'} />
                    </MidNav>
                </NavContent>
            </NavContainer>
        :
        <NavBarResponsive pathname={pathname} />
    )
}