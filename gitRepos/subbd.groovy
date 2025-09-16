// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:XenonAlphaE/xenon-sale-web.git',
        branch: 'xenon_subbd',
        folder: 'xenon_subbd',
        credId: 'id_ed25519_xenonalphae',
        buildType: 'nextjs',
        vpsRef : 'xenon_vps',   // 👈 just reference which VPS to use

        envs: [

            [ 
                MAIN_DOMAIN:'https://subbd.biz/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/subbd/token.png',
                MAIN_GTAG:'G-LVNYLPX8Y1',
                MAIN_SITENAME:'subbd',
                MAIN_TWITTER:'@subbd',
                OG_PATH:'/img/subbd/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://subbdcoin.com/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/subbd/token.png',
                MAIN_GTAG:'G-LVNYLPX8Y1',
                MAIN_SITENAME:'subbd',
                MAIN_TWITTER:'@subbd',
                OG_PATH:'/img/subbd/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://subbd.nl/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/subbd/token.png',
                MAIN_GTAG:'G-LVNYLPX8Y1',
                MAIN_SITENAME:'subbd',
                MAIN_TWITTER:'@subbd',
                OG_PATH:'/img/subbd/OG.jpeg'
            ],
            [ 
                MAIN_DOMAIN:'https://subbdtoken.net/',
                BACKLINKS_URL:'https://flockez.netlify.app/js/backlinks.json',
                LOGO_PATH:'/img/subbd/token.png',
                MAIN_GTAG:'G-LVNYLPX8Y1',
                MAIN_SITENAME:'subbd',
                MAIN_TWITTER:'@subbd',
                OG_PATH:'/img/subbd/OG.jpeg'
            ],
           
        ]

    ]
]