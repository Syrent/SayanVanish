stickynote {
    registerModule(core {
        database
        configuration {
            kotlinx
        }
        messaging {
            redis
            websocket
        }
    })
}

dependencies {
    compileOnlyApi(libs.discordsrv)
    compileOnlyApi(libs.luckperms.api)
    compileOnlyApi(libs.advancedserverlist)
    compileOnlyApi(libs.tab.api)
}
