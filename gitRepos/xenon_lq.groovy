// repos/liquidchain.groovy
return [
    [
        url: 'git@github.com:herocoinhunter2/saler-service.git',
        branch: 'LiquidChain',
        folder: 'xenon_liquidchain',
        credId: 'id_ed25519_herocoinhunter2',
        buildType: 'nextjs',
        vpsRef : 'xenon_vps2',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        kwList: 'LIQUIDCHAIN Token, LIQUIDCHAIN Token Presale, LIQUIDCHAIN Token Official Website',
        envs: [
            [ 
                MAIN_DOMAIN:'https://liquidchainlink.com/',
                CANONICAL_DOMAIN:'https://liquidchainlink.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/liquidchain/token.png',
                MAIN_GTAG:'G-12MCW744MW',
                MAIN_SITENAME:'liquidchain',
                MAIN_TWITTER:'@liquidchain',
                OG_PATH:'/img/liquidchain/OG.jpeg',
                configMaps: [
                    [
                        sourceFile: "xenon.eth.json",
                        targetFile: "config.eth.main.json"
                    ],
                    [
                        sourceFile: "ico.solana.json",
                        targetFile: "config.solana.main.json"
                    ],
                    [
                        sourceFile: "liquid.site.json",
                        targetFile: "config.site.json"
                    ]
                ]
            ],
            [ 
                MAIN_DOMAIN:'https://lqtychain.com/',
                CANONICAL_DOMAIN:'https://liquidchainlink.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/liquidchain/token.png',
                MAIN_GTAG:'G-12MCW744MW',
                MAIN_SITENAME:'liquidchain',
                MAIN_TWITTER:'@liquidchain',
                OG_PATH:'/img/liquidchain/OG.jpeg',
                configMaps: [
                    [
                        sourceFile: "xenon.eth.json",
                        targetFile: "config.eth.main.json"
                    ],
                    [
                        sourceFile: "ico.solana.json",
                        targetFile: "config.solana.main.json"
                    ],
                    [
                        sourceFile: "liquid.site.json",
                        targetFile: "config.site.json"
                    ]
                ]
            ],

        ]

    ]
]
