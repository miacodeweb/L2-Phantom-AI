# L2 Phantom AI Manager

<p align="center">
  <strong>Autonomous phantom players for L2J Mobius Essence RoseVain</strong><br>
  Realistic AI-driven players that spawn, level, fight, chat, recover MP, and move through real datapack hunting zones.
</p>

<p align="center">
  <a href="#português-pt-br">Português</a> ·
  <a href="#english-en">English</a> ·
  <a href="#español-es">Español</a>
</p>

---

## Português (PT-BR)

### Visão Geral

**L2 Phantom AI Manager** é um sistema modular para criar jogadores phantom autônomos em **L2J Mobius Essence RoseVain**. Os phantoms se comportam como jogadores reais: nascem em suas cidades de origem, se equipam, evoluem, procuram spots adequados ao level, fazem PvP, podem se tornar PK e interagem por chat.

### Recursos

- **Criação automática de phantoms:** crie grupos de 10 ou 50 phantoms pelo painel ou por comandos.
- **Cidades de origem reais:** novos personagens usam `PlayerTemplate.getCreationPoint()`, respeitando classe e raça.
- **Geodata segura:** todo spawn consulta `GeoEngine.getSpawnHeight()` e valida região do mundo antes de aparecer.
- **Spots reais da datapack:** o sistema lê `data/stats/npcs` e `data/spawns` para montar spots de caça por level.
- **Distribuição natural pelo mapa:** phantoms do mesmo level podem ir para spots diferentes, evitando concentração artificial.
- **Progressão inteligente:** troca de classe, troca de equipamento por grade e reposição automática de shots.
- **Combate com regras realistas:** evita mobs muito altos ou muito baixos e reduz disputa por alvo entre phantoms.
- **Magos com modo repouso:** quando ficam sem MP, sentam e recuperam até 100%; em PvP, tentam fugir.
- **PvP e PK:** alguns phantoms podem agir de forma agressiva e assumir comportamento PK.
- **Chat com IA opcional:** integração com Google Gemini para respostas curtas e sociais.
- **Logs de debug:** `.pdebug` grava ações em `log/PhantomManager.txt`.

### Arquivos Principais

- `PhantomManager.java` - comandos e bootstrap do sistema.
- `PhantomEngine.java` - ciclo de vida, spawn, respawn e execução da IA.
- `PhantomAI.java` - comportamento de farm, PvP, movimentação e recuperação de MP.
- `PhantomConfig.java` - rotas base, equipamentos, shots e XML de phantoms.
- `PhantomHuntingSpots.java` - leitura automática de spots reais da datapack.
- `PhantomGeo.java` - validação de geodata e coordenadas seguras.
- `PhantomFactory.java` - criação automática de personagens.
- `PhantomChat.java` - mensagens e integração opcional com Gemini.
- `PhantomMenu.java` / `PhantomHtml.java` - painel HTML de GM.

### Instalação

1. Copie a pasta `custom/PhantomManager` para:
   `dist/game/data/scripts/custom/PhantomManager/`
2. Coloque `PhantomPlayers.xml` em:
   `dist/game/config/Custom/PhantomPlayers.xml`
3. Compile os scripts ou reinicie o GameServer.
4. Se quiser chat com IA, edite `PhantomChat.java` e configure sua chave do Google Gemini.

### Comandos

- `.pmenu` - abre o painel de controle.
- `.pstart` - carrega phantoms salvos no XML.
- `.pstop` - remove os phantoms ativos.
- `.pload` - recarrega `PhantomPlayers.xml`.
- `.pcreate 10` - cria e inicia 10 phantoms.
- `.pcreate 50` - cria e inicia 50 phantoms.
- `.pm Nome Mensagem` - envia mensagem privada para um phantom.
- `.pdebug` - ativa/desativa logs.

### Observações

Ao criar novos phantoms, eles aparecem primeiro em sua cidade de origem e aguardam brevemente antes de viajar para um spot de level. Isso ajuda a simular jogadores reais entrando no mundo.

---

## English (EN)

### Overview

**L2 Phantom AI Manager** is a modular system for autonomous phantom players on **L2J Mobius Essence RoseVain**. Phantoms behave like real players: they spawn in their race/class origin towns, gear up, level, choose suitable hunting spots, fight, recover MP, engage in PvP, may become PK, and interact through chat.

### Features

- **Automatic phantom creation:** create groups of 10 or 50 phantoms from the panel or commands.
- **Real origin towns:** new characters use `PlayerTemplate.getCreationPoint()`.
- **Safe geodata spawning:** every spawn validates world regions and uses `GeoEngine.getSpawnHeight()`.
- **Real datapack hunting spots:** the system reads `data/stats/npcs` and `data/spawns` to build level-based hunting locations.
- **Natural map distribution:** phantoms of the same level can pick different valid spots.
- **Smart progression:** class changes, gear grade upgrades, and automatic shot restocking.
- **Realistic combat rules:** avoids mobs that are too high or too low and reduces target stealing.
- **Mage rest mode:** mages sit to recover MP to 100%; during PvP, they try to run.
- **PvP and PK behavior:** some phantoms can behave aggressively and become PK.
- **Optional AI chat:** Google Gemini support for short, social replies.
- **Debug logs:** `.pdebug` writes AI actions to `log/PhantomManager.txt`.

### Main Files

- `PhantomManager.java` - commands and system bootstrap.
- `PhantomEngine.java` - lifecycle, spawn, respawn, and AI scheduling.
- `PhantomAI.java` - farm, PvP, movement, and MP recovery behavior.
- `PhantomConfig.java` - base routes, gear, shots, and XML config.
- `PhantomHuntingSpots.java` - automatic datapack spot loader.
- `PhantomGeo.java` - geodata-safe coordinate validation.
- `PhantomFactory.java` - automatic character creation.
- `PhantomChat.java` - messages and optional Gemini integration.
- `PhantomMenu.java` / `PhantomHtml.java` - GM HTML panel.

### Installation

1. Copy `custom/PhantomManager` into:
   `dist/game/data/scripts/custom/PhantomManager/`
2. Place `PhantomPlayers.xml` in:
   `dist/game/config/Custom/PhantomPlayers.xml`
3. Compile scripts or restart the GameServer.
4. Optional: edit `PhantomChat.java` and set your Google Gemini API key.

### Commands

- `.pmenu` - opens the control panel.
- `.pstart` - loads phantoms listed in XML.
- `.pstop` - removes active phantoms.
- `.pload` - reloads `PhantomPlayers.xml`.
- `.pcreate 10` - creates and starts 10 phantoms.
- `.pcreate 50` - creates and starts 50 phantoms.
- `.pm Name Message` - sends a private message to a phantom.
- `.pdebug` - toggles debug logging.

### Notes

Newly created phantoms appear in their origin town first, then wait briefly before moving to an appropriate hunting spot. This makes them look closer to real players entering the world.

---

## Español (ES)

### Descripción

**L2 Phantom AI Manager** es un sistema modular para crear jugadores phantom autónomos en **L2J Mobius Essence RoseVain**. Los phantoms actúan como jugadores reales: nacen en su ciudad de origen, se equipan, suben de nivel, buscan spots adecuados, hacen PvP, pueden volverse PK, recuperan MP y conversan por chat.

### Características

- **Creación automática de phantoms:** crea grupos de 10 o 50 desde el panel o por comando.
- **Ciudades de origen reales:** los nuevos personajes usan `PlayerTemplate.getCreationPoint()`.
- **Spawn seguro con geodata:** cada aparición valida región del mundo y usa `GeoEngine.getSpawnHeight()`.
- **Spots reales de la datapack:** lee `data/stats/npcs` y `data/spawns` para construir zonas de leveo por nivel.
- **Distribución natural en el mapa:** phantoms del mismo nivel pueden ir a spots distintos.
- **Progresión inteligente:** cambio de clase, cambio de equipo por grado y recarga automática de shots.
- **Combate realista:** evita mobs demasiado altos o demasiado bajos y reduce el kill steal entre phantoms.
- **Magos con modo reposo:** cuando se quedan sin MP, se sientan hasta recuperar 100%; si están en PvP, intentan escapar.
- **PvP y PK:** algunos phantoms pueden ser agresivos y convertirse en PK.
- **Chat IA opcional:** integración con Google Gemini para respuestas cortas y sociales.
- **Logs de debug:** `.pdebug` registra acciones en `log/PhantomManager.txt`.

### Archivos Principales

- `PhantomManager.java` - comandos e inicio del sistema.
- `PhantomEngine.java` - ciclo de vida, spawn, respawn y ejecución de IA.
- `PhantomAI.java` - farm, PvP, movimiento y recuperación de MP.
- `PhantomConfig.java` - rutas base, equipos, shots y configuración XML.
- `PhantomHuntingSpots.java` - cargador automático de spots desde la datapack.
- `PhantomGeo.java` - validación segura de coordenadas con geodata.
- `PhantomFactory.java` - creación automática de personajes.
- `PhantomChat.java` - mensajes e integración opcional con Gemini.
- `PhantomMenu.java` / `PhantomHtml.java` - panel HTML para GM.

### Instalación

1. Copia `custom/PhantomManager` en:
   `dist/game/data/scripts/custom/PhantomManager/`
2. Coloca `PhantomPlayers.xml` en:
   `dist/game/config/Custom/PhantomPlayers.xml`
3. Compila los scripts o reinicia el GameServer.
4. Opcional: edita `PhantomChat.java` y coloca tu API key de Google Gemini.

### Comandos

- `.pmenu` - abre el panel de control.
- `.pstart` - carga los phantoms del XML.
- `.pstop` - remueve los phantoms activos.
- `.pload` - recarga `PhantomPlayers.xml`.
- `.pcreate 10` - crea e inicia 10 phantoms.
- `.pcreate 50` - crea e inicia 50 phantoms.
- `.pm Nombre Mensaje` - envía un mensaje privado a un phantom.
- `.pdebug` - activa/desactiva logs.

### Notas

Los phantoms recién creados aparecen primero en su ciudad de origen y esperan brevemente antes de viajar a un spot de leveo acorde a su nivel. Esto hace que parezcan jugadores reales entrando al mundo.

---

## Contact

Custom adaptations, private configurations, and ports to other chronicles are available through MiaCodeWeb.

- Email: [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
- Website: [miacodeweb.com](https://miacodeweb.com)

[![Donate via PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://paypal.me/miacodeweb)
