// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:XenonAlphaE/xenon-sale-web.git',
        branch: 'pepenode',
        folder: 'pepenode',
        credId: 'id_ed25519_xenonalphae',
        buildType: 'nextjs',
        vpsRef : 'xenon_vps',   // 👈 just reference which VPS to use

        envs: [
            [ 
                MAIN_DOMAIN:'https://pepenode.nl/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/pepenode/token.svg',
                MAIN_GTAG:'G-DT4375LBL3',
                MAIN_SITENAME:'pepenode',
                MAIN_TWITTER:'@pepenode',
                OG_PATH:'/img/pepenode/OG.jpeg'
            ],
        ]

    ]
]