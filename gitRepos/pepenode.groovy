// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:XenonAlphaE/xenon-sale-web.git',
        branch: 'pepenode',
        folder: 'pepenode',
        credId: 'id_ed25519_xenonalphae',

        vpsRef : 'xenon_vps1',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://pepenodetoken.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/pepenode/token.svg',
                MAIN_GTAG:'G-DT4375LBL3',
                MAIN_SITENAME:'pepenode',
                MAIN_TWITTER:'@pepenode',
                OG_PATH:'/img/pepenode/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://pepenodecoin.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/pepenode/token.svg',
                MAIN_GTAG:'G-DT4375LBL3',
                MAIN_SITENAME:'pepenode',
                MAIN_TWITTER:'@pepenode',
                OG_PATH:'/img/pepenode/OG.jpeg'
            ],
        ]

    ]
]