import styled from '@emotion/styled'
import { IconProp } from '@fortawesome/fontawesome-svg-core';
import { primary, light, gray_light, primary_dark, primary_light } from '@/app/color';
import Icon from '../Icon';

const ButtonPrimary = styled.div`
    padding: 3px 12px;
    font-weight: 500;
    font-weight: 100;
    font-size: 12px;
    border-radius: 24px;
    background-color: ${primary};
    color: ${light};
    cursor: pointer;
    margin: 2px;
    &:hover {
        background-color: ${primary_dark};
    }
`

const ButtonSecondary = styled.div`
    padding: 3px 12px;
    font-weight: 100;
    font-size: 12px;
    border-radius:24px;
    border: 1px solid ${gray_light};
    color: ${primary_dark};
    cursor: pointer;
    margin: 2px;
    &:hover {
        background-color: ${primary_light};
    }
`

type Props = {
    title: string
    leftIcon?: IconProp
    rightIcon?: IconProp
    type: 'primary' | 'secondary'
    onClick: () => void
}

export default function StickButton({title, leftIcon, rightIcon, type, onClick}: Props) {
    
    if (type === 'primary')
        return (
            <ButtonPrimary onClick={onClick}>
                {
                    leftIcon ? <Icon icon={leftIcon} style={{fontSize: '15px'}} /> : ''
                }
                {title}
                {
                    rightIcon ? <Icon icon={rightIcon} style={{fontSize: '15px'}} /> : ''
                }
            </ButtonPrimary>
        )

    if (type === 'secondary')
        return (
            <ButtonSecondary onClick={onClick}>
                {
                    leftIcon ? <Icon icon={leftIcon} style={{fontSize: '15px'}} /> : ''
                }
                {title}
                {
                    rightIcon ? <Icon icon={rightIcon} style={{fontSize: '15px'}} /> : ''
                }
            </ButtonSecondary>
        )

    return <></>
}