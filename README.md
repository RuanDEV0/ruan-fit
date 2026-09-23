# Ruan Fit

Aplicativo Android nativo para fichas e execução de treinos. UI em Jetpack Compose/Material 3, estado em ViewModel + Flow, persistência local em Room, injeção com Hilt.

## Requisitos e build

- Android SDK 36, JDK 17 e acesso aos repositórios Google Maven/Maven Central na primeira compilação.
- `./gradlew assembleDebug` gera `app/build/outputs/apk/debug/app-debug.apk`.
- `./gradlew testDebugUnitTest` executa os testes de lógica.
- Instale com `adb install -r app/build/outputs/apk/debug/app-debug.apk`.
- minSdk 26; targetSdk 35. Nenhuma chave de release é gerada.

## Uso

1. Cadastre exercícios na aba **Exercícios**, incluindo grupo muscular, instruções e link opcional para demonstração.
2. Crie uma ficha em **Fichas**, abra-a e adicione exercícios, séries, repetições e carga planejada.
3. Inicie a ficha e registre cada série com carga, repetições e RPE opcional. Séries concluídas são salvas no Room imediatamente.
4. Inicie o descanso com o tempo desejado. A notificação mantém o cronômetro ativo em segundo plano; no fim, o app vibra, toca um alerta e reduz/restaura o volume de mídia.
5. Veja sessões, duração e séries em **Histórico**; o gráfico e os recordes de carga aparecem em **Progresso**.

## Permissões

- **Notificações (Android 13+)**: antes de solicitar `POST_NOTIFICATIONS`, o app explica que ela permite mostrar o timer em segundo plano. A permissão é solicitada quando o usuário inicia um descanso.
- **Acesso às notificações**: opcional. O botão **Ativar controle de mídia** abre uma explicação antes de `Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS`. `NotificationListenerService` habilita `MediaSessionManager.getActiveSessions`, e o mini player usa `MediaController` para play/pause, faixa anterior e próxima. O serviço não lê o conteúdo de outras notificações. Consulte [PRIVACY.md](PRIVACY.md).
- **Serviço em primeiro plano**: o timer usa `specialUse` porque descansos configuráveis podem exceder os três minutos permitidos para `shortService`. O subtipo é declarado no manifesto. A distribuição via Google Play pode exigir revisão da justificativa desse tipo de serviço.

## Estrutura

- `data/`: entidades, DAO, Room e repositório com I/O em `Dispatchers.IO`.
- `domain/`: cálculo de recordes e sugestão de progressão (a sugestão fica preparada para uso após o MVP).
- `timer/`: serviço em primeiro plano e contagem por `elapsedRealtime`.
- `media/`: monitor reativo de sessões de mídia, controles e volume.
- `ui/`: telas Compose e ViewModel.

## Limitações conhecidas

- Integrações específicas de Spotify/YouTube não fazem parte do MVP; o controle depende de uma sessão de mídia ativa exposta pelo outro app e do acesso opcional às notificações. Alguns players podem não oferecer todos os comandos.
- O histórico guarda cópias dos nomes de ficha e exercício por série. A exclusão de uma ficha ou exercício remove suas prescrições, mas não apaga os registros concluídos.
- Gráficos usam a maior carga registrada por exercício em cada sessão, sem cálculo de 1RM.
- Medidas corporais, fotos, streaks e badges ficam para fases posteriores. O modelo de ficha já reserva um campo opcional para playlist.

Referências Android: [sessões de mídia](https://developer.android.com/reference/android/media/session/MediaSessionManager) e [tipos de serviço em primeiro plano](https://developer.android.com/develop/background-work/services/fgs/service-types).
