
module.exports = {
    theme: {
        extend: {
            typography: {
                DEFAULT: {
                    css: {
                        color: '#000000',
                        code: {
                            backgroundColor: '#bfbfbf',
                            padding: '2px 6px 2px 6px',
                            borderRadius: '6px'
                        },
                        'code::before': {
                            display: 'none',
                        },
                        'code::after': {
                            display: 'none',
                        },
                        pre: {
                            backgroundColor: '#1E1E1E',
                        }
                    },

                },
            }
        }
    },
    plugins: [
        require('@tailwindcss/typography'),
    ],
}