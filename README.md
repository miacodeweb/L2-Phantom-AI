# L2 Phantom AI Manager

<p align="center">
  <strong>Autonomous phantom players for L2J Mobius Essence RoseVain</strong><br>
  Realistic AI-driven players that spawn, level, fight, recover, use gear, move through real datapack hunting zones, and can be managed from an in-game GM panel.
</p>

<p align="center">
  <a href="#portugues-pt-br">Portugues</a> |
  <a href="#english-en">English</a> |
  <a href="#espanol-es">Espanol</a>
</p>

---

<p align="center"> 🌟 ¿Te ha servido este proyecto? / Did this project help you? / Este projeto te ajudou?

Si este repositorio te ha sido de utilidad, ¡considera apoyarlo! Las **estrellas (Stars)** ayudan a que más desarrolladores encuentren esta herramienta y motivan a seguir mejorando el sistema de *Phantom Players*.</p>

<p align="center">
ES:** Si te gusta el proyecto, por favor regálame una ⭐.
EN:** If you find this project useful, please consider giving it a ⭐.
PT:** Se este projeto for útil para você, por favor considere deixar uma ⭐.

¡Gracias por tu apoyo! / Thanks for your support! / Obrigado pelo seu apoio!</p>

---

## Portugues (PT-BR)

### Visao Geral

**L2 Phantom AI Manager** e um sistema modular para criar jogadores phantom autonomos em **L2J Mobius Essence RoseVain**. Os phantoms sao personagens reais do banco de dados que podem nascer em suas cidades de origem, viajar para spots de level, upar, lutar, usar skills, descansar, fazer PvP, virar PK e ser controlados por um painel de GM.

### Principais Recursos

- **Arquitetura modular:** o sistema foi separado em arquivos menores como `PhantomAI`, `PhantomEngine`, `PhantomFactory`, `PhantomEquipment`, `PhantomHuntingSpots`, `PhantomGeo`, `PhantomMenu` e outros.
- **Criacao automatica:** crie 10 ou 50 phantoms pelo menu ou por comando.
- **Persistencia em XML:** phantoms criados automaticamente sao adicionados ao `PhantomPlayers.xml`, entao continuam carregando apos reiniciar o servidor.
- **Inicio por lotes:** `.pstart` e o botao `Iniciar 10` carregam apenas 10 phantoms por vez, evitando spawn massivo de uma vez.
- **Desconexao por lote:** o menu permite desconectar 10 phantoms ativos sem desligar tudo.
- **Cidades de origem:** novos phantoms nascem usando `PlayerTemplate.getCreationPoint()`, respeitando raca e classe.
- **Spawn seguro com geodata:** os pontos usam validacao estilo NPC para evitar personagens abaixo do chao ou presos no mapa.
- **Spots reais da datapack:** o sistema le `data/stats/npcs` e `data/spawns` para montar spots por level.
- **Filtro de alvos ruins:** ignora `Training Dummy`, objetos de tutorial, baus e alvos que nao servem para farm real.
- **Recolocacao inteligente:** se o phantom nao encontra mobs uteis, ele nao fica andando sem sentido; procura outro spot do mesmo level.
- **Metas de level:** cada phantom recebe metas de subir alguns niveis, volta para cidade, recupera recursos e depois retorna ao farm.
- **Magos com descanso de MP:** magos entram em modo repouso ate recuperar MP; em PvP tentam escapar se estiverem sem mana.
- **Skills e buffs:** phantoms tentam usar skills ofensivas e buffs proprios como Might, Shield, Focus, Haste, Empower, Acumen, Wind Walk, Chant, Song e Dance.
- **Equipamentos variados:** packs de equipamentos por grade para magos e fighters, com variantes para evitar clones visuais.
- **Shots automaticos:** recarga automatica e aleatoria de Soulshots e Spiritshots conforme classe e grade.
- **Inventario limpo:** itens desnecessarios sao removidos automaticamente, preservando Adena, shots e equipamentos.
- **PvP e PK:** alguns phantoms sao agressivos, podem virar PK, e phantoms atacados por outros phantoms tentam se defender.
- **Chat opcional com IA:** suporte opcional para Google Gemini em respostas sociais.
- **Logs TXT:** o menu mostra se o log esta ativo e permite ativar/desativar.

### Menu GM

Use `.pmenu` para abrir o painel.

Opcoes atuais:

- `Iniciar 10`
- `Desconectar 10`
- `Criar 10`
- `Criar 50`
- `Recargar XML`
- `Detener Todo`
- `Activar Log` / `Desactivar Log`

O menu tambem mostra:

- quantidade de phantoms ativos;
- quantidade de IDs carregados no XML;
- estado atual dos logs TXT.

### Comandos

- `.pmenu` - abre o painel de controle.
- `.pstart` - inicia 10 phantoms do XML.
- `.pstop` - remove todos os phantoms ativos.
- `.pstop10` - desconecta 10 phantoms ativos.
- `.pload` - recarrega `PhantomPlayers.xml`.
- `.pcreate 10` - cria, inicia e salva 10 phantoms no XML.
- `.pcreate 50` - cria, inicia e salva 50 phantoms no XML.
- `.pm Nome Mensagem` - envia mensagem privada para um phantom.
- `.pdebug` - ativa/desativa logs TXT.

### Logs

Os logs ficam na pasta `log/` do GameServer:

- `log/PhantomManager.txt` - log historico geral.
- `log/PhantomManager-yyyyMMdd-HHmmss.txt` - log separado por sessao.

Exemplos de mensagens uteis:

- nova meta de level;
- viagem para spot;
- descanso em cidade;
- ataque a mob;
- defesa PvP;
- recarga de shots;
- limpeza de inventario;
- erros de IA com stacktrace.

### Arquivos Principais

- `PhantomManager.java` - comandos, logs e bootstrap.
- `PhantomEngine.java` - inicio, stop, spawn, respawn, lotes e ciclo da IA.
- `PhantomAI.java` - decisoes de farm, PvP, PK, descanso, alvo e movimentacao.
- `PhantomConfig.java` - rotas, equipamentos, shots e persistencia XML.
- `PhantomFactory.java` - criacao automatica de personagens.
- `PhantomEquipment.java` - buffs, progresso, shots e limpeza de inventario.
- `PhantomHuntingSpots.java` - leitura de NPCs/spawns reais da datapack.
- `PhantomGeo.java` - coordenadas seguras com geodata.
- `PhantomState.java` - memoria temporaria dos phantoms.
- `PhantomMenu.java` - painel HTML de GM.
- `PhantomBypass.java` - botoes do menu.
- `PhantomChat.java` - mensagens e integracao opcional com Gemini.

### Instalacao

1. Copie `custom/PhantomManager` para:
   `dist/game/data/scripts/custom/PhantomManager/`
2. Coloque `PhantomPlayers.xml` em:
   `game/config/Custom/PhantomPlayers.xml`
3. Compile os scripts ou reinicie o GameServer.
4. Use `.pmenu` para criar, iniciar e controlar phantoms.

---

## English (EN)

### Overview

**L2 Phantom AI Manager** is a modular autonomous phantom player system for **L2J Mobius Essence RoseVain**. Phantoms are real database characters that can spawn in origin towns, travel to level-appropriate hunting spots, level up, fight, use skills, rest, engage in PvP, become PK, and be managed from an in-game GM panel.

### Main Features

- **Modular architecture:** the system is split into focused files such as `PhantomAI`, `PhantomEngine`, `PhantomFactory`, `PhantomEquipment`, `PhantomHuntingSpots`, `PhantomGeo`, `PhantomMenu`, and more.
- **Automatic creation:** create 10 or 50 phantoms from the menu or commands.
- **XML persistence:** automatically created phantoms are saved into `PhantomPlayers.xml`, so they keep loading after server restarts.
- **Batch startup:** `.pstart` and `Start 10` load only 10 phantoms at a time.
- **Batch disconnect:** disconnect 10 active phantoms without shutting down the whole system.
- **Origin towns:** new phantoms spawn using `PlayerTemplate.getCreationPoint()`, respecting race and class.
- **Geodata-safe spawning:** spawn points use NPC-like coordinate validation to avoid under-map or floating characters.
- **Real datapack spots:** reads `data/stats/npcs` and `data/spawns` to build level-based hunting locations.
- **Bad target filtering:** ignores `Training Dummy`, tutorial objects, chests, and fake farm targets.
- **Smart relocation:** if a phantom cannot find useful mobs, it relocates to another spot for its level.
- **Level goals:** phantoms receive leveling goals, return to town, recover resources, and go back to farming.
- **Mage MP rest mode:** mages rest until MP is recovered; in PvP they try to escape when out of mana.
- **Skills and buffs:** phantoms try to use offensive skills and self buffs such as Might, Shield, Focus, Haste, Empower, Acumen, Wind Walk, Chant, Song, and Dance.
- **Varied gear packs:** gear packs by grade for mages and fighters, with multiple variants to avoid visual clones.
- **Automatic shots:** randomized Soulshot and Spiritshot restocking by class and grade.
- **Inventory cleanup:** unnecessary items are removed automatically while preserving Adena, shots, and equipped gear.
- **PvP and PK behavior:** some phantoms are aggressive, may become PK, and attacked phantoms try to defend themselves.
- **Optional AI chat:** optional Google Gemini integration for short social replies.
- **TXT logs:** the menu displays log state and can enable/disable logging.

### GM Menu

Use `.pmenu` to open the control panel.

Current options:

- `Start 10`
- `Disconnect 10`
- `Create 10`
- `Create 50`
- `Reload XML`
- `Stop All`
- `Enable Log` / `Disable Log`

The menu also shows:

- active phantom count;
- XML ID count;
- current TXT log state.

### Commands

- `.pmenu` - opens the control panel.
- `.pstart` - starts 10 phantoms from XML.
- `.pstop` - removes all active phantoms.
- `.pstop10` - disconnects 10 active phantoms.
- `.pload` - reloads `PhantomPlayers.xml`.
- `.pcreate 10` - creates, starts, and saves 10 phantoms to XML.
- `.pcreate 50` - creates, starts, and saves 50 phantoms to XML.
- `.pm Name Message` - sends a private message to a phantom.
- `.pdebug` - toggles TXT logging.

### Logs

Logs are created in the GameServer `log/` folder:

- `log/PhantomManager.txt` - global historical log.
- `log/PhantomManager-yyyyMMdd-HHmmss.txt` - separate log for each session.

Useful logged events include:

- new level goals;
- travel to hunting spots;
- town rest;
- mob attacks;
- PvP defense;
- shot restocking;
- inventory cleanup;
- AI exceptions with stacktrace.

### Main Files

- `PhantomManager.java` - commands, logs, and bootstrap.
- `PhantomEngine.java` - start, stop, spawn, respawn, batches, and AI loop.
- `PhantomAI.java` - farm, PvP, PK, rest, targeting, and movement decisions.
- `PhantomConfig.java` - routes, gear, shots, and XML persistence.
- `PhantomFactory.java` - automatic character creation.
- `PhantomEquipment.java` - buffs, progression, shots, and inventory cleanup.
- `PhantomHuntingSpots.java` - real datapack NPC/spawn loader.
- `PhantomGeo.java` - geodata-safe coordinate handling.
- `PhantomState.java` - temporary phantom memory.
- `PhantomMenu.java` - GM HTML panel.
- `PhantomBypass.java` - menu button handling.
- `PhantomChat.java` - messages and optional Gemini integration.

### Installation

1. Copy `custom/PhantomManager` into:
   `dist/game/data/scripts/custom/PhantomManager/`
2. Place `PhantomPlayers.xml` in:
   `game/config/Custom/PhantomPlayers.xml`
3. Compile scripts or restart the GameServer.
4. Use `.pmenu` to create, start, and manage phantoms.

---

## Espanol (ES)

### Descripcion

**L2 Phantom AI Manager** es un sistema modular para crear jugadores phantom autonomos en **L2J Mobius Essence RoseVain**. Los phantoms son personajes reales de la base de datos que pueden nacer en su ciudad de origen, viajar a spots de leveo, subir de nivel, pelear, usar skills, descansar, hacer PvP, convertirse en PK y ser administrados desde un panel GM dentro del juego.

### Caracteristicas Principales

- **Arquitectura modular:** el sistema fue dividido en archivos mas pequenos como `PhantomAI`, `PhantomEngine`, `PhantomFactory`, `PhantomEquipment`, `PhantomHuntingSpots`, `PhantomGeo`, `PhantomMenu` y otros.
- **Creacion automatica:** crea 10 o 50 phantoms desde el menu o por comandos.
- **Persistencia en XML:** los phantoms creados automaticamente se guardan en `PhantomPlayers.xml`, asi siguen cargando despues de reiniciar el servidor.
- **Inicio por lotes:** `.pstart` y el boton `Iniciar 10` cargan solo 10 phantoms por vez.
- **Desconexion por lote:** permite desconectar 10 phantoms activos sin apagar todo el sistema.
- **Ciudades de origen:** los nuevos phantoms nacen usando `PlayerTemplate.getCreationPoint()`, respetando raza y clase.
- **Spawn seguro con geodata:** las coordenadas usan validacion estilo NPC para evitar phantoms bajo tierra o flotando.
- **Spots reales de la datapack:** lee `data/stats/npcs` y `data/spawns` para construir spots por nivel.
- **Filtro de objetivos malos:** ignora `Training Dummy`, objetos de tutorial, cofres y objetivos falsos de farm.
- **Reubicacion inteligente:** si un phantom no encuentra mobs utiles, busca otro spot acorde a su nivel.
- **Metas de leveo:** cada phantom recibe metas de subir algunos niveles, vuelve a ciudad, recupera recursos y regresa al farm.
- **Magos con descanso de MP:** los magos descansan hasta recuperar MP; en PvP intentan escapar si estan sin mana.
- **Skills y buffs:** los phantoms intentan usar skills ofensivas y buffs propios como Might, Shield, Focus, Haste, Empower, Acumen, Wind Walk, Chant, Song y Dance.
- **Equipamiento variado:** packs por grado para magos y fighters, con variantes para evitar que todos se vean iguales.
- **Shots automaticos:** recarga aleatoria de Soulshots y Spiritshots segun clase y grado.
- **Limpieza de inventario:** elimina items innecesarios automaticamente, conservando Adena, shots y equipo equipado.
- **PvP y PK:** algunos phantoms son agresivos, pueden convertirse en PK, y los phantoms atacados intentan defenderse.
- **Chat IA opcional:** integracion opcional con Google Gemini para respuestas sociales cortas.
- **Logs TXT:** el menu muestra si el log esta activo y permite activarlo/desactivarlo.

### Menu GM

Usa `.pmenu` para abrir el panel.

Opciones actuales:

- `Iniciar 10`
- `Desconectar 10`
- `Crear 10`
- `Crear 50`
- `Recargar XML`
- `Detener Todo`
- `Activar Log` / `Desactivar Log`

El menu tambien muestra:

- cantidad de phantoms activos;
- cantidad de IDs cargados desde XML;
- estado actual de los logs TXT.

### Comandos

- `.pmenu` - abre el panel de control.
- `.pstart` - inicia 10 phantoms desde el XML.
- `.pstop` - remueve todos los phantoms activos.
- `.pstop10` - desconecta 10 phantoms activos.
- `.pload` - recarga `PhantomPlayers.xml`.
- `.pcreate 10` - crea, inicia y guarda 10 phantoms en el XML.
- `.pcreate 50` - crea, inicia y guarda 50 phantoms en el XML.
- `.pm Nombre Mensaje` - envia un mensaje privado a un phantom.
- `.pdebug` - activa/desactiva los logs TXT.

### Logs

Los logs se guardan en la carpeta `log/` del GameServer:

- `log/PhantomManager.txt` - log historico general.
- `log/PhantomManager-yyyyMMdd-HHmmss.txt` - log separado por cada sesion.

Eventos utiles que quedan registrados:

- nueva meta de leveo;
- viaje a spot;
- descanso en ciudad;
- ataque a mobs;
- defensa PvP;
- recarga de shots;
- limpieza de inventario;
- errores de IA con stacktrace.

### Archivos Principales

- `PhantomManager.java` - comandos, logs e inicio del sistema.
- `PhantomEngine.java` - inicio, stop, spawn, respawn, lotes y ciclo de IA.
- `PhantomAI.java` - decisiones de farm, PvP, PK, descanso, target y movimiento.
- `PhantomConfig.java` - rutas, equipos, shots y persistencia XML.
- `PhantomFactory.java` - creacion automatica de personajes.
- `PhantomEquipment.java` - buffs, progreso, shots y limpieza de inventario.
- `PhantomHuntingSpots.java` - lectura de NPCs/spawns reales de la datapack.
- `PhantomGeo.java` - manejo seguro de coordenadas con geodata.
- `PhantomState.java` - memoria temporal de los phantoms.
- `PhantomMenu.java` - panel HTML para GM.
- `PhantomBypass.java` - botones del menu.
- `PhantomChat.java` - mensajes e integracion opcional con Gemini.

### Instalacion

1. Copia `custom/PhantomManager` en:
   `dist/game/data/scripts/custom/PhantomManager/`
2. Coloca `PhantomPlayers.xml` en:
   `game/config/Custom/PhantomPlayers.xml`
3. Compila los scripts o reinicia el GameServer.
4. Usa `.pmenu` para crear, iniciar y administrar phantoms.

---

## Contact

Custom adaptations, private configurations, and ports to other chronicles are available through MiaCodeWeb.

- Email: [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
- Website: [miacodeweb.com](https://miacodeweb.com)

[![Donate via PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://paypal.me/miacodeweb)
