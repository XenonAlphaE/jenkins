def repos = []
repos += load "${env.WORKSPACE}/gitRepos/pepenode.groovy"
repos += load "${env.WORKSPACE}/gitRepos/hyper.groovy"
repos += load "${env.WORKSPACE}/gitRepos/priceSignature.groovy"


// Extract only MAIN_DOMAIN + vpsRef
def repoDomains = repos.collectMany { repo ->
    repo.envs.collect { env ->
        [
            domain : env.MAIN_DOMAIN,
            vpsRef : repo.vpsRef,
            name: env.MAIN_SITENAME

        ]
    }
}

return repoDomains
