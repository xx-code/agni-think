import type { Config } from 'tailwindcss'

export default <Partial<Config>>{
  theme: {
    extend: {
      colors: {
        custom: {
          DEFAULT: '#6755D7',
          light: '#6755D7',
          dark: '#6755D7'
        }
      }
    }
  }
}