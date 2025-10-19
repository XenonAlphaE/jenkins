return [
    [
        url: 'git@github.com:XenonAlphaE/oracle-price-signature.git',
        branch: 'master',
        folder: 'pricesignature',
        credId: 'id_ed25519_xenonalphae',
        imageName: 'pricesignatureapi',
        buildType: 'proxy',

        vpsRef : 'xenon_vps',   // 👈 just reference which VPS to use
        envs: [
            [ 
                MAIN_DOMAIN:'https://btcsymbol.net/',
                MAIN_SITENAME:'btcsymbol',
                proxyPort: '7001',

            ],
            
        ]

    ]
]