// repos/maxidoge.groovy
return [
    [
        url: 'git@github.com:XenonAlphaE/xenon-sale-web.git',
        branch: 'xenon_lilpepe',
        folder: 'xenon_lilpepe',
        credId: 'id_ed25519_xenonalphae',
        buildType: 'nextjs',
        vpsRef : 'xenon_vps',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',

        envs: [
            [ 
                MAIN_DOMAIN:'https://lilpepetoken.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/lilpepe/favlogo.png',
                MAIN_GTAG:'G-WG56YMTFKX',
                MAIN_SITENAME:'lilpepe',
                MAIN_TWITTER:'@lilpepe',
                OG_PATH:'/img/lilpepe/OG.jpeg'
            ],
        ]

    ]
]