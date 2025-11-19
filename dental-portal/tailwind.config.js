/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        'traslasierra-primary': '#1976d2',
      },
    },
  },
  plugins: [
    function({ addUtilities }) {
      const newUtilities = {
        '.scrollbar-thin': {
          'scrollbar-width': 'thin',
        },
        '.scrollbar-thin::-webkit-scrollbar': {
          width: '8px',
        },
        '.scrollbar-track-slate-100::-webkit-scrollbar-track': {
          backgroundColor: '#f1f5f9',
          borderRadius: '10px',
        },
        '.scrollbar-thumb-slate-300::-webkit-scrollbar-thumb': {
          backgroundColor: '#cbd5e1',
          borderRadius: '10px',
        },
        '.hover\\:scrollbar-thumb-slate-400:hover::-webkit-scrollbar-thumb': {
          backgroundColor: '#94a3b8',
        },
      }
      addUtilities(newUtilities, ['responsive', 'hover'])
    }
  ],
}