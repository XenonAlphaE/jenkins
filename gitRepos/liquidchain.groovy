// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:herocoinhunter2/saler-service.git',
        branch: 'LiquidChain',
        folder: 'liquidchain',
        credId: 'id_ed25519_herocoinhunter2',
        buildType: 'nextjs',
        vpsRef : 'ico_vps',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        envs: [
            [ 
                MAIN_DOMAIN:'https://liquidchaintoken.com/',
                // BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                // LOGO_PATH:'/img/supepe/logo-7PppWrxZ.webp',
                // MAIN_GTAG:'G-RKQYQZCCT3',
                // MAIN_SITENAME:'supepe',
                // MAIN_TWITTER:'@supepe',
                // OG_PATH:'/img/supepe/OG.jpeg'
            ],
        ]

    ]
]