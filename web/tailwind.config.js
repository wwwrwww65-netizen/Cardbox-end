/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef6ff',
          100: '#d9ebff',
          200: '#bce0ff',
          300: '#8eccff',
          400: '#58adff',
          500: '#2f8eff',
          600: '#0f4c81',
          700: '#145cb5',
          800: '#154e95',
          900: '#174278',
          950: '#0c274d',
        },
        cardbox: {
          primary: '#0F4C81',
          accent: '#00A86B',
          warning: '#F59E0B',
          danger: '#EF4444',
          dark: '#0F172A',
          card: '#1E293B'
        }
      },
      fontFamily: {
        sans: ['Cairo', 'Noto Sans Arabic', 'system-ui', 'sans-serif'],
      }
    },
  },
  plugins: [],
}
