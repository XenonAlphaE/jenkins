return [
    [
        url: 'git@github.com:XenonAlphaE/xenon-sale-web.git',
        branch: 'xenon_btcbull',
        folder: 'xenon_btcbull',
        credId: 'id_ed25519_xenonalphae',
        buildType: 'nextjs',

        vpsRef : 'xenon_vps',   // 👈 just reference which VPS to use
        envs: [
            [ 
                MAIN_DOMAIN:'https://btcbull.nl/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks3.json',
                LOGO_PATH:'/img/btcbull/logo.png',
                MAIN_GTAG:'G-W3VRRLZ0C5',
                MAIN_SITENAME:'btcbull',
                MAIN_TWITTER:'@btcbull',
                OG_PATH:'img/btcbull/OG.jpeg'
            ],
           
        ]

    ],
]