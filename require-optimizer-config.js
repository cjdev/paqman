({
    mainConfigFile:"${basedir}/src/main/resources/content/require-config.js",
    appDir: "${basedir}/src/main/resources/content",
    baseUrl: "./",
    dir: "${basedir}/target/classes/content",
    optimize:"uglify",
    keepBuildDir:true,
    modules: [
        {
            name: "sarge"
        },
        {
            name:"qual"
        }
    ]
})
