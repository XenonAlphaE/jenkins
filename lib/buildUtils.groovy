def build(repo, envConf, idx) {
    if (repo.buildType == "nextjs") {
        buildNextjs(repo, envConf, idx)
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

// Check if domain exists in MISSING_CERTS string
def isMissingCert(String domain) {
    if (!missingCerts || missingCerts.isEmpty()) {
        return false
    }
    def domainList = missingCerts.collect { it?.trim()?.toLowerCase() }
                             .findAll { it } // filter out null/empty
    return domainList.contains(domain.toLowerCase())
}


private def buildNextjs(repo, envConf, idx) {
    echo "📦 Building Next.js project: ${repo.folder} for env ${envConf.name}"
    def domain = extractDomain(envConf.MAIN_DOMAIN)

    if (isMissingCert(domain)) {
        echo "⏭️ Skipping build for ${envConf.name} (${domain}) due to missing cert"
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
