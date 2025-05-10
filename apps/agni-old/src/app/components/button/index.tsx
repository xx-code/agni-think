import { isEmpty } from '@/_utils/isEmpty';
import { primary, light, gray_light, primary_dark, primary_light} from '@/app/color';
import styled from '@emotion/styled';

import { IconProp } from '@fortawesome/fontawesome-svg-core'
import Icon from '../Icon';

const ButtonPrimary = styled.div`
    padding: 1rem 1.9rem;
    font-weight: 500;
    font-size: 0.8rem;
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
    padding: 1rem 1.9rem;
    font-weight: 500;
    font-size: 0.8rem;
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
    title: string, 
    icon?: IconProp,
    type: 'primary' | 'secondary'
    onClick: () => void
}

export default function Button({title, onClick, type, icon}: Props) {
    if(type === 'primary')
        return (
            <ButtonPrimary  onClick={onClick} >
                {
                    icon ? <Icon style={{fontSize: isEmpty(title) ? '20px' : '15px', marginRight: isEmpty(title) ? '0' : '10px'}} icon={icon} /> : ''
                }
                {title}
            </ButtonPrimary>
        )

    if (type === 'secondary')
        return (
            <ButtonSecondary>
                {
                    icon ? <Icon style={{fontSize: isEmpty(title) ? '20px' : '15px', marginRight: isEmpty(title) ? '0' : '10px'}} icon={icon} /> : ''
                }
                {title}
            </ButtonSecondary>
        )
}