// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:XenonAlphaE/oracle-price-signature.git',
        branch: 'master',
        folder: 'web3predict',
        credId: 'id_ed25519_xenonalphae',
        buildType: 'web_and_api',
        fePort: '8000',
        apiPort: '8001',
        vpsRef : 'xenon_vps2',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        envs: [
            [ 
                MAIN_DOMAIN:'https://web3predict.io/',
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