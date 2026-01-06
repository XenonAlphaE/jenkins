def repos = []
repos += load "${env.WORKSPACE}/gitRepos/liquidchain.groovy"
repos += load "${env.WORKSPACE}/gitRepos/supepe.groovy"
repos += load "${env.WORKSPACE}/gitRepos/nxsdefi.groovy"

// repos += load "${env.WORKSPACE}/gitRepos/lilpepe.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/pepenode.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/hyper.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/priceSignature.groovy"
repos += load "${env.WORKSPACE}/gitRepos/subbd.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/bull.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/wepe.groovy"
repos += load "${env.WORKSPACE}/gitRepos/btcsymbol.groovy"

// Extract only MAIN_DOMAIN + vpsRef
def repoDomains = repos.collectMany { repo ->
    (repo.envs ?: []).collect { env ->
        def formatedDomain = env.MAIN_DOMAIN
            .replaceAll(/^https?:\/\//, '')   // remove protocol
            .replaceAll(/\/$/, '')            // remove trailing slash
            .replaceAll(/[^a-zA-Z0-9]/, '_')  // replace special chars with underscore
            .replaceAll(/_+/, '_')            // collapse consecutive underscores
            .toLowerCase()                    // optional: normalize case
        return [
            "MAIN_DOMAIN" : env.MAIN_DOMAIN,
            "vpsRef" : repo.vpsRef,
            "name": formatedDomain,
            "ssl": repo.ssl
        ]
    }
}

return repoDomains
