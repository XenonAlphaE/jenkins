def repos = []
repos += load "${env.WORKSPACE}/gitRepos/solanaSignature.groovy"


// Extract only MAIN_DOMAIN + vpsRef
def repoDomains = repos.collectMany { repo ->
    repo.envs.collect { env ->
        [
            domain : env.MAIN_DOMAIN,
            vpsRef : repo.vpsRef
        ]
    }
}

return repoDomains
