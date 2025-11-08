// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:XenonAlphaE/xenon-sale-web.git',
        branch: 'xenon_hyper',
        folder: 'xenon_hyper',
        credId: 'id_ed25519_xenonalphae',
        buildType: 'nextjs',
        vpsRef : 'xenon_vps',   // 👈 just reference which VPS to use

        envs: [

            [ 
                MAIN_DOMAIN:'https://btchyper.nl/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/btchyper/token.svg',
                MAIN_GTAG:'G-EV4VMQB5HZ',
                MAIN_SITENAME:'btchyper',
                MAIN_TWITTER:'@btchyper',
                OG_PATH:'/img/btchyper/OG.jpeg'
            ],
           
        ]

    ]
]