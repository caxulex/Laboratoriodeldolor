package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.laboratoriodeldolor.ui.AppScaffold

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    AppScaffold { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            color = androidx.compose.ui.graphics.Color.Transparent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Política de Privacidad y No Tratamiento de Datos Personales de CeroDolorApp",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Desarrollador: Laboratorio del Dolor\n" +
                        "Correo electrónico de contacto: jessik.cantillo12@gmail.com\n" +
                        "Fecha de entrada en vigor: 11 de septiembre de 2025",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 1. Declaración Fundamental de Privacidad
                Text(
                    text = "1. Declaración Fundamental de Privacidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "En el Laboratorio del Dolor, consideramos la privacidad y la seguridad de nuestros usuarios como el pilar fundamental de la aplicación \"CeroDolorApp\". Por esta razón, hemos diseñado la aplicación con un enfoque de \"Privacidad por Diseño y por Defecto\" en su máxima expresión.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Declaramos de manera explícita y categórica que \"CeroDolorApp\" NO RECOPILA, NO PROCESA, NO ALMACENA, NO COMPARTE, y NO TIENE ACCESO a ningún tipo de dato personal o información que pueda identificar directa o indirectamente a un usuario.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = "Debido a esta característica esencial de diseño, las disposiciones de la Ley Estatutaria 1581 de 2012 y sus decretos reglamentarios sobre el tratamiento de datos personales no son aplicables a las operaciones de la aplicación, ya que no existe un \"Tratamiento de Datos Personales\" según la definición de dicha ley. Sin embargo, nos adherimos plenamente a los principios de privacidad, confidencialidad y seguridad que inspiran la legislación colombiana de protección de datos.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 2. Datos que NO Recopilamos
                Text(
                    text = "2. Datos que NO Recopilamos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Para ofrecer total transparencia, a continuación, se detalla una lista no exhaustiva de los tipos de datos que la aplicación NO recopila intencionadamente:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text("• Información de Identificación Personal (IIP): Nombres, apellidos, números de cédula, direcciones de correo electrónico, números de teléfono, fechas de nacimiento, género, etc.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Text("• Datos de Salud: Cualquier información relacionada con su condición física o mental, historial médico, tratamientos o cualquier dato que usted ingrese o genere en las herramientas interactivas de la aplicación.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Text("• Datos de Uso de la Aplicación: No rastreamos cómo interactúa con la aplicación, qué secciones visita, cuánto tiempo pasa en ellas, ni ninguna otra métrica de comportamiento.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Text("• Datos del Dispositivo: No recopilamos identificadores únicos del dispositivo (IDFA, ID de Android), tipo de dispositivo, sistema operativo, dirección IP, información de la red móvil, etc.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Text("• Datos de Ubicación: La aplicación no solicita, accede ni almacena información sobre su ubicación geográfica.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                Text("• Contactos, Fotos o Archivos: La aplicación no solicita acceso a sus contactos, galería de fotos, micrófono, cámara o almacenamiento de archivos en su dispositivo.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))

                // 3. Ausencia de Cookies y Tecnologías de Rastreo
                Text(
                    text = "3. Ausencia de Cookies y Tecnologías de Rastreo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "\"CeroDolorApp\" no utiliza cookies, balizas web, píxeles de seguimiento, kits de desarrollo de software (SDK) de rastreo, ni ninguna otra tecnología similar destinada a recopilar datos o monitorear la actividad del usuario dentro o fuera de la aplicación.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 4. Ausencia de Integración con Terceros
                Text(
                    text = "4. Ausencia de Integración con Terceros",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "La aplicación ha sido desarrollada para ser completamente autónoma. No integramos servicios, API, SDK de análisis, plataformas publicitarias o redes sociales de terceros que pudieran recopilar datos de nuestros usuarios. Toda la funcionalidad y el contenido están contenidos dentro de la propia aplicación.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 5. Funcionamiento Offline
                Text(
                    text = "5. Funcionamiento Offline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Una vez que \"CeroDolorApp\" y su contenido educativo han sido descargados en su dispositivo, la aplicación no requiere una conexión a internet para funcionar. No se establece ninguna comunicación con servidores externos, lo que garantiza que no hay transferencia de datos.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 6. Derechos de los Usuarios
                Text(
                    text = "6. Derechos de los Usuarios",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Dado que no se recopila, almacena ni procesa ningún dato personal, los derechos de los titulares establecidos en el Artículo 8 de la Ley 1581 de 2012 (conocer, actualizar, rectificar, suprimir, etc.) se garantizan por defecto. Su privacidad es inherente al diseño de la aplicación. Usted tiene el control total, ya que ninguna información sale de su dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 7. Cambios a esta Política
                Text(
                    text = "7. Cambios a esta Política",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "En el improbable caso de que futuras actualizaciones de la aplicación modifiquen este enfoque fundamental de no recopilación de datos, actualizaremos esta Política de Privacidad de manera significativa y solicitaremos su consentimiento explícito antes de que dichos cambios entren en vigor. Las actualizaciones se notificarán a través de la tienda de aplicaciones correspondiente.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // 8. Contacto
                Text(
                    text = "8. Contacto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Si tiene alguna pregunta, duda o comentario sobre nuestra política de privacidad y nuestro compromiso con la no recolección de datos, puede contactarnos en la siguiente dirección de correo electrónico: jessik.cantillo12@gmail.com.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                    com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(
                        text = stringResource(id = R.string.back_button),
                        onClick = onBack,
                        modifier = Modifier.padding(top = 24.dp)
                    )
            }
        }
    }
}
