package com.example.laboratoriodeldolor

import android.content.Context
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope

object DatabaseSeeder {

    /**
     * Create a RoomDatabase.Callback that will wait for the built AppDatabase instance
     * via the provided CompletableDeferred and then perform DAO-based seeding.
     */
    fun createCallback(dbDeferred: CompletableDeferred<AppDatabase>, appContext: Context): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // Run seeding off the main thread; onCreate may be called on a background thread
                val scope = CoroutineScope(Dispatchers.IO)
                scope.launch {
                    try {
                        val database = dbDeferred.await()

                        val prefs = com.example.laboratoriodeldolor.data.UserPreferencesRepository(appContext)
                        val already = prefs.hasSeededContentFlow.first()
                        if (already) return@launch

                        // Insert techniques first
                        val techDao = database.techniqueDao()
                        val techniqueMap = mutableMapOf<String, Long>()

                        val techniques = listOf(
                            Technique(title = "Dedo índice sobre dedo corazón", description = "Poner el dedo corazón encima del dedo índice para generar más presión en el punto de dolor, realizar el masaje en círculos."),
                            // Keep singular title used in routines but update description to final wording
                            Technique(title = "Amasamiento", description = "Dar pellizcos en la zona de dolor con la mano completa."),
                            Technique(title = "Fricción", description = "Con la parte que une el dedo índice con el dedo pulgar realiza deslizamientos rápidos."),
                            Technique(title = "Garrita", description = "Cerrar la mano en forma de puño y usar los “nudillos” o falanges medias y las proximales para masajear."),
                            // Add techniques requested by content list
                            Technique(title = "Percusiones", description = "Palmadas rápidas y constantes para atraer la circulación."),
                            Technique(title = "Bombeo", description = "Usar la palma de la mano para hacer movimientos de presión hacia abajo."),
                            // Update existing combined entry to include the video URL for 'hueso a hueso' technique
                            Technique(title = "Dedo a dedo / Hueso a hueso", description = "Esta técnica se realiza usando el dedo pulgar de forma lateral, donde se encuentra el hueso y no la yema del dedo, para ejercer más presión sobre la zona a trabajar.", videoUrl = "https://drive.google.com/drive/u/0/folders/1A0IOUnAagu4hpcx2TD89K4m-p_GcZZpv"),
                            Technique(title = "Técnica Espejo", description = "En caso de dolor crónico o cirugía que no permite masajear. Por ejemplo: en la lesión del hombro poner el Smärtgel y masajear el hombro sano.")
                        )

                        for (t in techniques) {
                            val id = techDao.insert(t)
                            techniqueMap[t.title.lowercase()] = id
                        }

                        // Helper to get technique id by simple key
                        fun techniqueIdByName(name: String?): Long? {
                            if (name == null) return null
                            return techniqueMap[name.lowercase()]
                        }

                        // Insert routines and their steps
                        val routineDao = database.routineDao()
                        val stepDao = database.routineStepDao()

                        data class RoutineSeed(val title: String, val bodyRegion: String, val summary: String?, val steps: List<Pair<String, String?>>)

                        val seeds = listOf(
                            RoutineSeed(
                                title = "Rostro y Cabeza",
                                bodyRegion = "face_head",
                                summary = "Movimientos y masajes para rostro y cráneo",
                                steps = listOf(
                                    Pair("Masaje Facial: Con la técnica de dedo índice sobre dedo corazón, haz un masaje circular a lo largo de 6 líneas horizontales en el rostro.", null),
                                    Pair("Línea 1: Pegada al cuero cabelludo, desde el centro hacia las sienes.", null),
                                    Pair("Línea 2: En la mitad de la frente, desde el centro hacia las sienes.", null),
                                    Pair("Línea 3: Encima de las cejas, desde el entrecejo hacia las sienes.", null),
                                    Pair("Línea 4: Sobre la comisura de la nariz, hacia la mandíbula.", null),
                                    Pair("Línea 5: Desde la comisura de los labios, pasando por los músculos de la mandíbula.", null),
                                    Pair("Línea 6: Desde el mentón, pasando por el hueso mandibular hasta la cabeza.", null),
                                    Pair("Repite tres veces cada línea y refuerza el trabajo en los puntos de dolor que encuentres.", null),
                                    Pair("Masaje Craneal: Con la misma técnica de dedo índice sobre dedo corazón, realiza un masaje circular desde el inicio del cabello hasta la base del cráneo.", null),
                                    Pair("Línea 1: Desde el inicio de las cejas (entrecejo) hacia atrás.", null),
                                    Pair("Línea 2: Desde la mitad de ambas cejas hacia atrás.", null),
                                    Pair("Línea 3: Desde el final de las cejas hacia atrás.", null),
                                    Pair("Línea 4: Desde el rabillo de los ojos, pasando por encima de las orejas, hasta la base del cráneo.", null),
                                    Pair("Repite tres veces cada línea y refuerza el trabajo en los puntos de dolor.", null)
                                )
                            ),
                            RoutineSeed(
                                title = "Cuello y Hombros",
                                bodyRegion = "neck_shoulders",
                                summary = "Estiramientos y movilizaciones para cuello y hombros",
                                steps = listOf(
                                    Pair("Preparación: Antes de iniciar, aplica Smärtgel sobre la base del cráneo, el cuello y los hombros.", null),
                                    Pair("Masaje Lateral: Con la técnica dedo corazón sobre dedo índice, masajea desde la base del cráneo, bajando por el lado lateral del cuello y continuando hacia el extremo de ambos hombros. Repite 6 veces por lado o hasta que el dolor se reduzca.", null),
                                    Pair("Movilidad de Cabeza: Con los brazos alzados, mira al frente. Baja y sube la cabeza (hacia adelante y atrás). Repite 6 veces. Luego, dibuja una \"W\" con la cabeza. Repite 3 veces.", null),
                                    Pair("Masaje Completo: Con la técnica dedo corazón sobre dedo índice, masajea desde la frente (sobre la línea del cuero cabelludo), pasando por las sienes y por encima de las orejas hasta el extremo de los hombros. Repite 6 veces por lado.", null),
                                    Pair("Amasamiento de Hombros: Realiza un amasamiento en cada hombro, desde la base del cuello hasta el extremo del hombro. Repite 3 veces por lado.", "Amasamiento"),
                                    Pair("Amasamiento Axilar: Pasa una mano por debajo de la axila opuesta para hacer amasamiento en la zona entre la cara lateral y la espalda con las yemas de los dedos. Repite 6 veces por lado.", "Amasamiento"),
                                    Pair("Estiramientos de Cuello: Mueve la cabeza hacia abajo, presionando suavemente con ambas manos entrelazadas sobre ella. Gira la cabeza hacia un lado y luego al otro, poniendo una mano sobre la cabeza para generar una leve presión. Mantén cada estiramiento por 10 segundos y repite 3 veces por lado.", "Estiramiento suave"),
                                    Pair("Punto de Dolor Referido (Vesícula): Masajea con la técnica dedo corazón sobre dedo índice en la zona media del estómago, desde la línea media hasta el lateral de las costillas (zona de la vesícula biliar). Repite 6 veces.", null),
                                    Pair("Técnica Espejo (Hombro): Si no puedes masajear el hombro adolorido, aplica Smärtgel en él, pero realiza el masaje (técnica dedo corazón sobre dedo índice y amasamiento) en el hombro sano, el pecho y la zona axilar de ese mismo lado.", "Técnica Espejo"),
                                    Pair("Masaje Flexores del Cuello: Con la técnica garrita, masajea los músculos de la parte anterior del cuello, trabajando los puntos de dolor. Finaliza aplicando Smärtgel.", "Garrita")
                                )
                            ),
                            RoutineSeed(
                                title = "Espalda Alta y Pecho",
                                bodyRegion = "upper_back",
                                summary = "Liberación y estiramientos para la parte superior de la espalda y pectorales",
                                steps = listOf(
                                    Pair("Preparación: Aplica Smärtgel y realiza primero la rutina completa de cuello y hombros.", null),
                                    Pair("Masaje Pectoral Superior: Con la técnica dedo corazón sobre dedo índice, masajea por debajo de la clavícula, desde la línea media del pecho hasta el extremo de los hombros.", null),
                                    Pair("Masaje Pectoral Medio: Baja la medida de un dedo y repite el masaje en una segunda línea, continuando así hasta llegar a un dedo por encima del pezón, siempre desde la línea media del pecho hasta debajo de las axilas. Repite 6 veces cada línea en cada lado.", null),
                                    Pair("Masaje Serrato Anterior: Con la misma técnica, masajea desde debajo de la línea del pezón hasta la axila y la espalda (músculo serrato anterior), hasta donde alcancen tus dedos. Repite 6 veces en cada lado.", null),
                                    Pair("Masaje Esternón: Realiza un masaje con la técnica dedo corazón sobre dedo índice sobre la línea media del esternón (entre ambos pectorales). Repite hasta que se reduzca el dolor.", null)
                                )
                            ),
                            RoutineSeed(
                                title = "Espalda Baja, Caderas y Pelvis",
                                bodyRegion = "lower_back",
                                summary = "Ejercicios y masajes para la zona lumbar, caderas y pelvis",
                                steps = listOf(
                                    Pair("Preparación y Percusión: Aplica Smärtgel en la zona lumbar y pélvica (anterior y posterior). Realiza percusiones (palmadas con las manos ahuecadas) sobre la espalda baja y la pelvis hasta sentir calor en la zona (mínimo 10 segundos). Repite 3 veces.", null),
                                    Pair("Estiramiento de Cuádriceps: Aplica Smärtgel en la zona lumbar. Apoyándote en una superficie firme, coge el tobillo de una pierna y dóblala hacia atrás para estirar el músculo cuádriceps (parte anterior del muslo). Mantén por 10 segundos. Repite 3 veces al día con cada pierna. Si el cuádriceps duele, aplica Smärtgel directamente sobre él.", null),
                                    Pair("Ejercicio del Nadador: Aplica Smärtgel en la zona lumbar. Apoya tus manos en una superficie firme y realiza 10 patadas suaves hacia atrás con cada pierna, como si nadaras. Repite 3 veces al día por 10 segundos.", null),
                                    Pair("Masaje de Cadera: Aplica Smärtgel en la cadera (anterior, posterior y lateral). Realiza masajes de amasamientos, fricción y garrita encima y debajo del músculo de la cadera. Repite 3 veces en cada lado.", "Amasamiento"),
                                    Pair("Técnica Espejo (Intestino): Si hay dolor intestinal, aplica Smärtgel en la zona y masajea con la técnica dedo corazón sobre el dedo índice en el lado contrario del dolor.", "Técnica Espejo")
                                )
                            ),
                            RoutineSeed(
                                title = "Manos, Muñecas y Antebrazos",
                                bodyRegion = "arms_hands",
                                summary = "Cuidado y movilización de manos, muñecas y antebrazos",
                                steps = listOf(
                                    Pair("Dedos: Aplica Smärtgel debajo de la uña (falange distal) y realiza fricciones con la técnica hueso a hueso en cada falange de todos los dedos. Repite 6 veces por dedo.", "Dedo a dedo / Hueso a hueso"),
                                    Pair("Aplica Smärtgel en todas las coyunturas y falanges y realiza fricciones. Repite 6 veces por dedo.", null),
                                    Pair("Apoya la mano en una superficie firme, aplica Smärtgel y con la técnica dedo corazón sobre el dedo índice haz círculos sobre cada falange. Repite 3 veces por dedo.", null),
                                    Pair("Aplica Smärtgel y realiza estiramientos de cada dedo, desde la base hacia arriba. Repite 3 veces por dedo.", null),
                                    Pair("Muñeca y Antebrazo: Aplica Smärtgel sobre la muñeca y realiza fricciones sobre toda la articulación (posterior, anterior y lateral), continuando hasta el codo por el antebrazo. Repite 3 veces por brazo.", null),
                                    Pair("Aplica Smärtgel y haz movimientos circulares con cada dedo y con la muñeca. Repite 3 veces por cada uno.", null),
                                    Pair("Codo: Aplica Smärtgel en toda la articulación del codo (parte posterior y anterior) y haz fricciones arriba y debajo de la articulación.", null),
                                    Pair("Con la técnica dedo corazón sobre el dedo índice, haz un masaje en círculos sobre la línea de la comisura interna y externa que se forma en el codo al doblarlo.", null),
                                    Pair("Brazo Completo: Masajea con amasamientos y con la técnica dedo corazón sobre el dedo índice desde la muñeca hasta los hombros, trazando líneas imaginarias desde cada dedo y buscando puntos dolorosos en todo el brazo.", "Amasamiento"),
                                    Pair("Realiza un estiramiento entrelazando los dedos de ambas manos y estirando los brazos hacia adelante. Mantén por 10 segundos y haz 3 repeticiones.", null)
                                )
                            ),
                            RoutineSeed(
                                title = "Tobillos, Pies y Dedos",
                                bodyRegion = "legs_feet",
                                summary = "Rutina para pies, tobillos y piernas",
                                steps = listOf(
                                    Pair("Preparación de Pies: Antes de dormir, sumerge los pies en agua tibia con una cucharada de bórax o sales de Epson. Seca y aplica Smärtgel en la zona de dolor.", null),
                                    Pair("Pies (Reflexología): Aplica 3 gotas de Smärtgel en aceite de oliva y realiza un masaje de reflexología podal en los puntos de dolor (dedos, tobillos, metatarsos, talón).", null),
                                    Pair("Pies (Masaje General): Con la técnica dedo corazón sobre dedo índice, masajea desde las falanges de los dedos, pasando por el empeine hasta llegar al tobillo.", null),
                                    Pair("Aplica Smärtgel con aceite vegetal sobre la almohadilla plantar y repite la misma maniobra. Repite 6 veces por pie.", null),
                                    Pair("Con la técnica garrita y aceite de oliva, masajea la almohadilla plantar, pasando por el arco y deslizando hasta el talón. Aplica Smärtgel en los puntos de dolor.", "Garrita"),
                                    Pair("Tobillos: Realiza masajes de amasamientos, dedo corazón sobre el dedo índice y garrita en el tobillo (maléolo interno y externo) y sobre el talón (hueso calcáneo).", null),
                                    Pair("Piernas (Pantorrillas): Realiza amasamientos en los músculos gemelos y la tibia con aceite de oliva. Al terminar, aplica Smärtgel sobre los puntos de dolor.", "Amasamiento"),
                                    Pair("Piernas (Muslos): Realiza el estiramiento de cuádriceps: apoyándote en una superficie firme, coge el tobillo de una pierna y dóblala hacia atrás. Mantén por 10 segundos. Repite 3 veces al día con cada pierna.", null)
                                )
                            ),
                            RoutineSeed(
                                title = "Espalda Media y Abdomen",
                                bodyRegion = "abdomen_pelvis",
                                summary = "Trabajo sobre estómago, intestinos y pelvis",
                                steps = listOf(
                                    Pair("Estómago y Costillas: Aplica Smärtgel desde la línea media del estómago hacia los laterales. Realiza un masaje en garrita (con los nudillos) desde la línea media hasta los laterales de las costillas. Repite 6 veces por lado.", "Garrita"),
                                    Pair("Con la técnica dedo corazón sobre el dedo índice, masajea la zona media del estómago y haz presión de 3 segundos sobre la zona de la vesícula biliar (lado derecho). Repite 6 veces.", null),
                                    Pair("Intestinos: Aplica Smärtgel sobre la zona media del estómago e intestinos después de comer.", null),
                                    Pair("Aplica Smärtgel y haz presión con la técnica dedo corazón sobre el dedo índice a un dedo de distancia al lado del ombligo. Repite 3 veces por lado.", null),
                                    Pair("Realiza percusiones sobre el recorrido del intestino grueso (desde la izquierda hacia la derecha).", null),
                                    Pair("Realiza un masaje con la técnica garrita sobre el mismo recorrido del intestino grueso.", "Garrita"),
                                    Pair("Pelvis e Ingle: Realiza un bombeo (5 presiones con la palma de la mano) sobre la zona de la ingle izquierda y derecha.", null),
                                    Pair("Aplica Smärtgel en la zona anterior de la pelvis (arriba y sobre el hueso púbico).", null),
                                    Pair("Masajea la zona del hueso púbico con la técnica dedo índice y dedo corazón, buscando puntos de dolor. Repite 6 veces en cada lado.", null)
                                )
                            ),
                            RoutineSeed(
                                title = "Principios Generales para todo el Cuerpo",
                                bodyRegion = "full_body",
                                summary = "Principios y recomendaciones generales",
                                steps = listOf(
                                    Pair("Principio de la Región Completa: Cuando trabajes un punto de dolor, no te limites solo a esa zona. Trabaja toda la región muscular involucrada. Por ejemplo, si te duele la rodilla, masajea también los músculos del muslo y la pantorrilla.", null),
                                    Pair("Preparación con Smärtgel: Antes de iniciar cualquier ejercicio o técnica manual, aplica Smärtgel en toda la región de dolor. Esto ayuda a relajar los músculos, aumentar la flexibilidad y evitar lesiones. El uso recomendado es 3 veces al día.", null),
                                    Pair("Uso de Aceite: Para facilitar las maniobras de masaje, puedes agregar unas gotas de aceite vegetal (oliva, coco, almendras) después de la primera aplicación de Smärtgel. Al terminar, vuelve a aplicar el Smärtgel solo en los puntos de dolor que identificaste.", null),
                                    Pair("La Importancia de la Respiración: Al encontrar un punto de dolor durante un masaje, es crucial que inhales y exhales lentamente. Esto evita que tenses el cuerpo y los músculos, permitiendo que la técnica sea más efectiva.", null),
                                    Pair("Trabajo sobre Nudos: Si identificas un nudo o un endurecimiento en el músculo, trabaja en ese punto con una presión suficiente pero sin hacerte daño, hasta que sientas que se reduce. El movimiento debe ser lento y rítmico, coordinado con tu respiración.", null),
                                    Pair("Constancia: Una sola sesión te ayudará a reducir la tensión, pero es la constancia en el trabajo manual de los puntos de dolor lo que te ayudará a reparar la zona y a evitar que el dolor regrese. Sigue las técnicas recomendadas por al menos una semana.", null),
                                    Pair("Técnica Espejo: Para casos de dolor crónico, cirugía o lesiones que impiden masajear directamente la zona afectada, utiliza la técnica espejo. Consiste en aplicar el Smärtgel en la zona de dolor, pero realizar el masaje y las maniobras en la parte simétrica del cuerpo que está sana.", "Técnica Espejo")
                                )
                            )
                        )

                        for (seed in seeds) {
                            val rid = routineDao.insert(Routine(title = seed.title, bodyRegion = seed.bodyRegion, summary = seed.summary))
                            var order = 1
                            for (step in seed.steps) {
                                val desc = step.first
                                val techName = step.second
                                val tid = techniqueIdByName(techName)
                                stepDao.insert(RoutineStep(routineId = rid, stepOrder = order, description = desc, techniqueId = tid))
                                order++
                            }
                        }

                        // Mark preferences as seeded
                        prefs.setHasSeededContent(true)
                        
                        // Initialize rehabilitation data
                        try {
                            val rehabRepository = com.example.laboratoriodeldolor.data.rehabilitation.RehabilitationRepository(database.rehabilitationDao())
                            rehabRepository.initializeDefaultCategories()
                            rehabRepository.initializeDefaultExercises()
                        } catch (e: Exception) {
                            // Log but don't fail the entire seeding process
                            e.printStackTrace()
                        }

                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }
        }
    }
}
