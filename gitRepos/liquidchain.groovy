// repos/liquidchain.groovy
return [
    [
        url: 'git@github.com:herocoinhunter2/saler-service.git',
        branch: 'LiquidChain',
        folder: 'liquidchain',
        credId: 'id_ed25519_herocoinhunter2',
        buildType: 'nextjs',
        vpsRef : 'ico_vps',   // 👈 just reference which VPS to use
        ssl : 'cloudflare',
        kwList: 'LIQUIDCHAIN Token, LIQUIDCHAIN Token Presale, LIQUIDCHAIN Token Official Website',
        envs: [
            [ 
                MAIN_DOMAIN:'https://liquidchaintoken.com/',
                CANONICAL_DOMAIN:'https://liquidchaintoken.com/',
                BACKLINKS_URL:'https://btcsymbol.net/public/js/backlinks.json',
                LOGO_PATH:'/img/liquidchain/token.png',
                MAIN_GTAG:'G-12MCW744MW',
                MAIN_SITENAME:'liquidchain',
                MAIN_TWITTER:'@liquidchain',
                OG_PATH:'/img/liquidchain/OG.jpeg',
                configMaps: [
                    [
                        sourceFile: "abc.conf",
                        targetFile: "aaa.conf"
                    ],
                    [
                        sourceFile: "bbb.conf",
                        targetFile: "ccc.conf"
                    ],
                    [
                        sourceFile: "ddd.conf",
                        targetFile: "eee.conf"
                    ]
                ]

            ],
        ]

    ]
]

[{
    sourceFile: 'abc.conf',
    targetFile: 'aaa.conf'
},
{
    sourceFile: 'abc.conf',
    targetFile: 'aaa.conf'
},
{
    sourceFile: 'abc.conf',
    targetFile: 'aaa.conf'
}]