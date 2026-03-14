// repos/liquidchain.groovy
return [
    [
        url: 'git@github.com:herocoinhunter2/saler-service.git',
        branch: 'pepeto',
        folder: 'pepeto',
        credId: 'id_ed25519_herocoinhunter2',
        buildType: 'nextjs',
        vpsRef : 'ico_vps2',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        kwList: 'Join PEPETO, PEPETO Coin, $PEPETO , PEPETO Token',
        envs: [
            [ 
                MAIN_DOMAIN:'https://pepetotoken.com/',
                CANONICAL_DOMAIN:'https://pepetotoken.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/pepeto/token.png',
                MAIN_GTAG:'G-KR7PWTC9TS',
                MAIN_SITENAME:'pepeto',
                MAIN_TWITTER:'@pepeto',
                OG_PATH:'/img/pepeto/OG.jpeg',
                configMaps: [
                    [
                        sourceFile: "ico.eth.json",
                        targetFile: "config.eth.main.json"
                    ],
                    [
                        sourceFile: "ico.solana.json",
                        targetFile: "config.solana.main.json"
                    ],
                    [
                        sourceFile: "pepeto.site.json",
                        targetFile: "config.site.json"
                    ]
                ]
            ],
       

        ]

    ]
]
