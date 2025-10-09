delete config.output.libraryTarget; // Kotlin Multiplatformが勝手に設定するので消す
config.experiments = {
    ...(config.experiments || {}),
    outputModule: true,
};
config.output = {
    ...config.output,
    module: true,
    library: {
        type: "module",
    },
};
