package co.anteia.anteiasdk.provider

class ApiSingleton private constructor() {

    private object HOLDER {
        val INSTANCE = ApiConnection()
    }

    companion object {
        val instance: IApiConnection by lazy { HOLDER.INSTANCE }
    }
}