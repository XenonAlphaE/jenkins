return [
    [
        url: 'git@github.com:herocoinhunter2/saler-service.git',
        branch: 'nxsdefi',
        folder: 'nxsdefi',
        credId: 'id_ed25519_herocoinhunter2',
        buildType: 'nextjs',
        vpsRef : 'ico_vps',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        kwList: 'NexusDeFi, NexusDeFi Token is the first true operating system for the decentralized economy.',
        envs: [
            [ 
                MAIN_DOMAIN:'https://nxsdefi.com/',
                CANONICAL_DOMAIN:'https://nxsdefi.com/',
                LOGO_PATH:'/img/nxsdefi/token.svg',
                MAIN_GTAG:'G-XQ7H5FEYQF',
                MAIN_SITENAME:'NexusDeFi',
                MAIN_TWITTER:'@NexusDeFi',
                OG_PATH:'/img/nxsdefi/OG.jpeg',
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
                        sourceFile: "nexusdefi.site.json",
                        targetFile: "config.site.json"
                    ]
                ]
            ],
            [ 
                MAIN_DOMAIN:'https://nexusdefi.finance/',
                CANONICAL_DOMAIN:'https://nexusdefi.finance/',
                LOGO_PATH:'/img/nxsdefi/token.svg',
                MAIN_GTAG:'G-XQ7H5FEYQF',
                MAIN_SITENAME:'NexusDeFi',
                MAIN_TWITTER:'@NexusDeFi',
                OG_PATH:'/img/nxsdefi/OG.jpeg',
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
                        sourceFile: "nexusdefi.site.json",
                        targetFile: "config.site.json"
                    ]
                ]
            ]
        ]

    ]
]
