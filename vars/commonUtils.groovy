def extractDomain(String url) {
    return url
        .replaceAll(/^https?:\/\//, '')
        .replaceAll(/\/$/, '')
        .replaceAll(/^www\./, '')
}

return this
