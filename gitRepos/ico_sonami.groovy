// repos/liquidchain.groovy
return [
    [
        url: 'git@github.com:herocoinhunter2/saler-service.git',
        branch: 'sonami_token',
        folder: 'sonami_token',
        credId: 'id_ed25519_herocoinhunter2',
        buildType: 'nextjs',
        vpsRef : 'ico_vps2',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        kwList: 'Sonami Token | Sonami Presale |High-Performance Solana Layer 2 Network',
        envs: [
            [ 
                MAIN_DOMAIN:'https://sonamitoken.com/',
                CANONICAL_DOMAIN:'https://sonamitoken.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/sonami/token.svg',
                MAIN_GTAG:'G-NTQFF6CGDC',
                MAIN_SITENAME:'sonamitoken',
                MAIN_TWITTER:'@sonamitoken',
                OG_PATH:'/img/sonami/OG.jpeg',
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
                        sourceFile: "sonami.site.json",
                        targetFile: "config.site.json"
                    ]
                ]
            ],
       

        ]

    ]
]
