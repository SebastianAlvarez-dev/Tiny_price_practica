# TinyPrice

Aplicación Android en Kotlin y Jetpack Compose para buscar productos y abrir sus fuentes web. Incluye registro e inicio de sesión con Supabase Auth y una Edge Function protegida que consulta TinyFish Search.

## Flujo

1. La persona crea una cuenta o inicia sesión con Supabase Auth.
2. La app envía la búsqueda y el token de sesión a `search-products`.
3. La Edge Function valida la sesión, limita las solicitudes y consulta TinyFish desde el servidor.
4. TinyPrice muestra título, fragmento y enlace de cada resultado para abrir la tienda.

La app no extrae ni verifica precios estructurados. Por eso muestra **Ver en fuente**; confirma el precio, variante y disponibilidad directamente en la tienda. Los resultados pueden no coincidir exactamente con el producto buscado.

## Configuración local

Guarda estos valores en `%USERPROFILE%\.gradle\gradle.properties` (no en el repositorio):

```properties
supabaseUrl=https://<project-ref>.supabase.co
edgeRuntimeBaseUrl=https://<project-ref>.supabase.co/functions/v1
supabasePublishableKey=<publishable-key>
useRemoteDeals=true
```

En Supabase, configura el secreto `TINYFISH_API_KEY` para la Edge Function `search-products`. No incluyas claves privadas, `service_role`, secretos de TinyFish ni archivos `.env` en Git. La clave publishable puede estar en la app; los secretos de proveedor deben permanecer en Supabase.

## Compilar y probar

Requiere Android Studio/JDK compatible con Gradle y un SDK Android instalado.

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Para ejecutar las pruebas de interfaz en un dispositivo/emulador conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

El modo de demostración usa datos de ejemplo y se selecciona con `-PuseRemoteDeals=false`; para búsqueda en vivo usa `-PuseRemoteDeals=true` y la configuración local anterior.
