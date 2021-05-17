package co.anteia.anteiasdk.provider

class DataProviderSingleton {
    private object HOLDER {
        val INSTANCE = DataProvider()
    }

    companion object {
        val instance: DataProvider by lazy { HOLDER.INSTANCE }
    }
}