package com.example.mirai.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * MediaHelper - Utilidad para grabación de audio
 *
 * Funciones:
 * - startRecording() → Inicia grabación de audio
 * - stopRecording() → Detiene grabación de audio
 *
 * Formato de audio: AAC (.m4a)
 * Ubicación: /files/audio/AUD_timestamp.m4a
 */
object MediaHelper {

    /**
     * Inicia la grabación de audio
     *
     * @param context Contexto de la aplicación
     * @return Par de (MediaRecorder, File) si tiene éxito, null si falla
     *
     * Ejemplo de uso:
     * ```
     * MediaHelper.startRecording(context)?.let { (recorder, file) ->
     *     // Grabación iniciada
     *     mediaRecorder = recorder
     *     audioFile = file
     * }
     * ```
     */
    fun startRecording(context: Context): Pair<MediaRecorder, File>? {
        return try {
            // Crear directorio de audio si no existe
            val audioDir = File(context.filesDir, "audio")
            audioDir.mkdirs()

            // Crear archivo de audio con timestamp único
            val timestamp = System.currentTimeMillis()
            val audioFile = File(audioDir, "AUD_$timestamp.m4a")

            // Configurar MediaRecorder según versión de Android
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+)
                MediaRecorder(context)
            } else {
                // Android 11 y anteriores
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            // Configurar el recorder
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            // Retornar el recorder y el archivo
            Pair(recorder, audioFile)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Detiene la grabación de audio
     *
     * @param recorder MediaRecorder a detener
     * @return true si se detuvo correctamente, false si hubo error
     *
     * Ejemplo de uso:
     * ```
     * if (MediaHelper.stopRecording(mediaRecorder)) {
     *     // Grabación detenida exitosamente
     * }
     * ```
     */
    fun stopRecording(recorder: MediaRecorder?): Boolean {
        return try {
            recorder?.apply {
                stop()
                release()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}