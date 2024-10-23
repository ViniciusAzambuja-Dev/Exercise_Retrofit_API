package com.example.calculadorainvest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val codigoAcao = "VALE3"
        setContent {
            DadosAcoesScreen(::obterDadosAcao, codigoAcao)
        }
    }

    fun obterDadosAcao(codigoAcao: String, callback: (String?, List<String>?, String?) -> Unit) {
        val call = RetrofitObject.instance.getAcao(codigoAcao)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val html = response.body()?.string()
                    if (html != null) {
                        try {
                            val document = Jsoup.parse(html)

                            val cotacaoElement = document.selectFirst("span.value")
                            val cotacao = cotacaoElement?.text() ?: "Cotação não foi encontrada"

                            val dividendosElements = document.select("div.content.no_datatable.indicator-history table tbody tr")
                            val dividendos = dividendosElements.map { row ->
                                val tipo = row.select("td").get(0).text()
                                val dataCom = row.select("td").get(1).text()
                                val pagamento = row.select("td").get(2).text()
                                val valor = row.select("td").get(3).text()
                                "Tipo: $tipo, Data Com: $dataCom, Pagamento: $pagamento, Valor: R$ $valor"
                            }

                            callback(cotacao, dividendos, null)
                        } catch (e: Exception) {
                            callback(null, null, "Erro no parse.")
                        }
                    } else {
                        callback(null, null, "HTML retornou vazio na resposta.")
                    }
                } else {
                    callback(null, null, "Erro na resposta da API: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback(null, null, "Falha na requisição: ${t.message}")
            }
        })
    }
}

@Composable
fun DadosAcoesScreen(
    obterDadosAcao: (String, (String?, List<String>?, String?) -> Unit) -> Unit,
    codigoAcao: String
) {
    var cotacaoAtual by remember { mutableStateOf<String>("Espere..") }
    var dividendos by remember { mutableStateOf<List<String>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(codigoAcao) {
        obterDadosAcao(codigoAcao) { cotacao, dividendosLista, erro ->
            if (erro != null) {
                errorMessage = erro
            } else {
                cotacaoAtual = cotacao ?: "Cotação não disponível"
                dividendos = dividendosLista
            }
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Cotação atual: $cotacaoAtual", style = MaterialTheme.typography.headlineMedium)

        dividendos?.let {
            Text(text = "Dividendos:", style = MaterialTheme.typography.headlineSmall)
            it.forEach { dividendo ->
                Text(text = dividendo)
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
    }
}