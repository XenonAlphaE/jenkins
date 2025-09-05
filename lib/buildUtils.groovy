def build(repo, envConf, idx, state) {
    if (repo.buildType == "nextjs") {
        buildNextjs(repo, envConf, idx, state)
    // } else if (repo.buildType == "docker") {
    //     buildDocker(repo, envConf, idx)
    } else {
        error "❌ Unknown repo.buildType '${repo.buildType}' for ${repo.name}"
    }
}

// --- Helper functions ---
def extractDomain(String url) {
    return url
        .replaceAll(/^https?:\/\//, '')  // remove http(s)
        .replaceAll(/\/$/, '')           // remove trailing slash
        .replaceAll(/^www\./, '')        // strip leading www
}

private def buildNextjs(repo, envConf, idx) {
    echo "📦 Building Next.js project: ${repo.folder} for env ${envConf.name}"
    def domain = extractDomain(envConf.MAIN_DOMAIN)

    if (domain && state.hasMissingCert(domain)) {
        echo "⏭️ Skipping ${envConf.name}, missing cert for ${domain}"
        return
    }

    echo "=== Building ${repo.folder} branch >>${repo.branch}<< for environment: ${envConf.name} ==="

    withEnv(envConf.collect { k,v -> "${k.toUpperCase()}=${v}" } ) {
        if (idx == 0) {
            // 👉 First env: full CI build
            sh '''
                if [ -f package.json ]; then
                    export CI=true
                    npm ci
                    npx next build && npx next-sitemap

                    if [ -d .next ]; then
                        rm -rf .next/cache || true
                        rm -rf .next/server || true
                        rm -rf .next/**/*.nft.json || true
                    fi
                else
                    echo "No package.json found, skipping build."
                fi
            '''
        } else {
            sh '''
                if [ -f package.json ]; then
                    npx next build && npx next-sitemap

                    if [ -d .next ]; then
                        rm -rf .next/cache || true
                        rm -rf .next/server || true
                        rm -rf .next/**/*.nft.json || true
                    fi
                else
                    echo "No package.json found, skipping build."
                fi
            '''                                        
        }

        def envOut = "outs/${envConf.name}"
        sh """
            mkdir -p outs
            rm -rf ${envOut} || true
            cp -r out ${envOut} || echo "⚠️ Warning: 'out' folder missing, copy skipped"
        """

        sh """
            if [ -d ${envOut} ] && [ "\$(ls -A ${envOut})" ]; then
                echo "✅ Build output exists for ${repo.folder}/${envConf.name}"
            else
                echo "❌ ERROR: ${envOut} missing or empty for ${repo.folder}"
                exit 1
            fi
        """
    }
}

private def buildDocker(repo, envConf, idx) {
    echo "🐳 Building Docker project: ${repo.folder} for env ${envConf.name}"
    sh """
        docker build -t ${repo.folder}:${envConf.name} .
    """
}


return [ build: this.&build ]   // only export `build`
