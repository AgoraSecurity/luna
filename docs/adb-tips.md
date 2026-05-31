# Comandos Útiles de ADB

Guía rápida para configurar ADB y comandos esenciales para probar el sistema de notificaciones de Luna.

## Configuración del PATH (macOS/Linux)

Si el comando `adb` no funciona en tu terminal, puedes agregarlo temporalmente con estos pasos:

```bash
# 1. Define la ruta de tu SDK (ajusta si es necesario)
export ANDROID_HOME=/Users/manuel/Library/Android/sdk

# 2. Agrega platform-tools al PATH
export PATH=$PATH:$ANDROID_HOME/platform-tools/

# 3. Verifica la instalación
adb version
```

---

## Comandos para Alertas y Notificaciones

### 1. Ver alarmas programadas
Este comando muestra todas las alarmas en el sistema. Filtra por el nombre del paquete para ver cuándo se dispararán las notificaciones de Luna (tips a las 9 AM, alertas de fase a las 10 AM).
```bash
adb shell dumpsys alarm | grep com.tarmiga.luna
```

### 2. Forzar el envío de una notificación (Test)
Si quieres probar visualmente cómo se ve una alerta sin esperar a que pase el tiempo, usa este comando. 

**Ejemplo: Alerta de fase lútea en 2 días**
```bash
adb shell am broadcast -a com.tarmiga.luna.TEST_NOTIF \
  --es notification_type PHASE_WARNING \
  --es phase_type LUTEAL \
  -p com.tarmiga.luna
```

**Ejemplo: Tip diario de fase folicular (índice 0-4)**
```bash
adb shell am broadcast -a com.tarmiga.luna.TEST_NOTIF \
  --es notification_type TIP \
  --es phase_type FOLLICULAR \
  --ei tip_index 0 \
  -p com.tarmiga.luna
```

### 3. Simular un Reinicio (Boot Completed)
Por seguridad de Android, enviar `BOOT_COMPLETED` manualmente suele dar `SecurityException` en dispositivos no rooteados. 

Si tienes **root**, puedes usar:
```bash
adb shell su root am broadcast -a android.intent.action.BOOT_COMPLETED -p com.tarmiga.luna
```

Si **no tienes root**, la mejor forma de probar la persistencia es:
1. Abrir la app y loguear el inicio del periodo (esto crea las alarmas).
2. Forzar el cierre de la app.
3. Reiniciar el celular físicamente.
4. Esperar un minuto y revisar `adb logcat -s NotificationReceiver` para ver si se disparó el re-agendamiento.

---

## Otros Comandos Esenciales

### Ver Logs en tiempo real
Filtra por los tags que usamos en la app para ver qué está pasando internamente.
```bash
adb logcat -s NotificationHelper NotificationReceiver LunaBridge MainActivity
```

### Instalar la APK manualmente
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
