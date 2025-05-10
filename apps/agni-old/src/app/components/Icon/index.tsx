/* eslint-disable @typescript-eslint/ban-ts-comment */
import { isEmpty } from '@/_utils/isEmpty';
import { IconProp, library, SizeProp } from '@fortawesome/fontawesome-svg-core'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import Link from 'next/link';
import { CSSProperties } from 'react';

// @ts-ignore
library.add(fas)

type Props = {
    icon: IconProp|string,
    href?: string,
    size?: SizeProp,
    style?: CSSProperties
}

export default function Icon({icon, href, size, style }: Props) {
    
    if (isEmpty(href))
        return (
            // @ts-ignore
            <FontAwesomeIcon icon={icon} size={size} style={style}  />           
        )
    else 
        return (
            <Link href={href!}  style={{...style, textDecoration: 'none'}}>
                {/*@ts-ignore*/}
                <FontAwesomeIcon icon={icon} size={size} style={style}  />   
            </Link>
        )

}