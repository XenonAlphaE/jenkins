// repos/maxidoge.groovy
return [
    [
        // url: 'git@github.com:XenonAlphaE/xenon-sale-web.git',
        // branch: 'xenon_supepe',
        // folder: 'xenon_supepe',
        credId: 'id_ed25519_xenonalphae',
        buildType: 'proxy',
        proxyPort: 3000,
        vpsRef : 'xenon_vps',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        envs: [
            [ 
                MAIN_DOMAIN:'https://btcsymbol.net/',
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