
def repos = []
repos += load "${env.WORKSPACE}/gitRepos/ico_sonami.groovy"
repos += load "${env.WORKSPACE}/gitRepos/xenon_lq.groovy"
repos += load "${env.WORKSPACE}/gitRepos/xenon_nxs.groovy"

repos += load "${env.WORKSPACE}/gitRepos/nxsdefi.groovy"
repos += load "${env.WORKSPACE}/gitRepos/liquidchain.groovy"
repos += load "${env.WORKSPACE}/gitRepos/supepe.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/hyper.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/pepenode.groovy"
repos += load "${env.WORKSPACE}/gitRepos/subbd.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/bull.groovy"
// repos += load "${env.WORKSPACE}/gitRepos/wepe.groovy"


return repos.collect { repo ->
    repo.envs = repo.envs.collect { env ->
        def domain = env.MAIN_DOMAIN
            .replaceAll(/^https?:\/\//, '')   // remove protocol
            .replaceAll(/\/$/, '')            // remove trailing slash
            .replaceAll(/[^a-zA-Z0-9]/, '_')  // replace special chars with underscore
            .replaceAll(/_+/, '_')            // collapse consecutive underscores
            .toLowerCase()                    // optional: normalize case

        env.KEYWORD_LIST = repo.kwList
        env.name = domain
        return env
    }
    return repo
}


