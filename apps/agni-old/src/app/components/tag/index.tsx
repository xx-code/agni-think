import { isEmpty } from '@/_utils/isEmpty';
import { gray } from '@/app/color';
import './index.css'

type Props = {
    id: string
    title: string
    color?: string
    onDelete?: (id: string) => void
}

export default function Tag({id, title, onDelete, color} : Props) {
    return (
        <div className='tag' style={{backgroundColor: isEmpty(color) ? gray : color!}}>
            <p>{title}</p>
            {
                onDelete !== undefined ?<span onClick={() => onDelete(id)}>x</span> : <></>
            }
        </div>
    )
}