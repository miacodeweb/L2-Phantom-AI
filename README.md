# 🤖 L2 Phantom AI Manager by MiaCodeWeb

<p align="center">
  <a href="#português-pt-br">Português (PT-BR)</a> • 
  <a href="#english-en">English (EN)</a> • 
  <a href="#español-es">Español (ES)</a>
</p>

---

<h2 id="português-pt-br">🇧🇷 Português (PT-BR)</h2>

Um sistema avançado para criar **jogadores phantom com Inteligência Artificial (IA)** que evoluem e escalam dentro do jogo como se fossem jogadores reais. O sistema não apenas cria "bots de enfeite", mas entidades dinâmicas que farmam, sobem de nível, equipam itens e interagem através do chat usando a API do Google Gemini.

### 🌟 Key Features (Principais Recursos)
* **Arquitetura Modular:** Código limpo e dividido em 4 arquivos independentes (`Manager`, `AI`, `Config`, `Chat`) para fácil manutenção e escalabilidade sem quebrar o núcleo.
* **Geodata Inteligente & Ancoragem de NPCs:** Os phantoms "aprendem" o mapa lendo a memória do GeoEngine e clonando as coordenadas exatas dos NPCs reais. Isso garante aterragens 100% legais (Eixo Z perfeito) e evita que afundem no mapa.
* **Progressão Realista:** Começam em zonas iniciais e progridem organicamente, trocando de equipamento (D, C, B, A, S) e de classe à medida que sobem de nível.
* **Inteligência de Combate:** Escolhem alvos de forma inteligente (evitando Kill Steal), mantêm distanciamento social e recarregam Soulshots automaticamente.
* **Chat IA Dinâmico & Eventos Sociais:** Integração nativa com Google Gemini 1.5. Os phantoms conversam no chat geral, saúdam jogadores próximos e respondem a mensagens privadas (`.pm`).
* **Sistema de Logs TXT:** Inclui um modo `.pdebug` que registra os pensamentos e ações da IA em um arquivo `PhantomManager.txt` silencioso, sem inundar o console do GameServer.

### 📥 Installation (Instalação)
1. Copie os 4 arquivos Java (`PhantomManager.java`, `PhantomAI.java`, `PhantomConfig.java`, `PhantomChat.java`) para o diretório de scripts customizados do seu servidor (`data/scripts/custom/PhantomManager/`).
2. Coloque o arquivo `PhantomPlayers.xml` na pasta `config/Custom/`.
3. (Opcional) Edite o `PhantomChat.java` e insira sua API Key do Google Gemini para habilitar o Chat IA.
4. Reinicie o servidor ou compile os scripts.

### 💻 Commands (Comandos)
* **`.pstart`** / **`.pstop`** - Inicia ou para o sistema e os phantoms.
* **`.pmenu`** - Abre o Painel de Controle HTML do Admin no jogo (com bypass nativo anti-bloqueio).
* **`.pm [Nome] [Mensagem]`** - Envia uma mensagem privada para um phantom para testar a IA.
* **`.pgo [Nome]` / `.pbring [Nome]` / `.pkill [Nome]`** - Comandos rápidos de controle de GM.
* **`.pdebug`** - Liga/Desliga a gravação de ações no log de texto.

### 🛠️ Adaptações Sob Medida & Contato
Este núcleo gratuito foi desenvolvido especificamente para **L2J Mobius Essence (RoseVain)**. No entanto, o código foi desenhado para ser universal. 
Trabalho de maneira particular adaptando e configurando este sistema para **qualquer outro servidor, emulador ou crônica** (L2jFrozen, aCis, Lucera, High Five, Interlude, etc.).

**Faço orçamentos totalmente gratuitos!** Se você precisa deste sistema adaptado para o seu servidor, ou de configurações e mods exclusivos:
* ✉️ **Email:** [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
* 🌐 **Chat ao vivo:** Fale comigo diretamente pelo chat em nosso site [miacodeweb.com](https://miacodeweb.com)

### ☕ Suporte e Doações
Este é um projeto mantido pela MiaCodeWeb. Se este projeto ajudou o seu servidor, considere fazer uma doação para manter o desenvolvimento ativo!

[![Donate via PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://paypal.me/miacodeweb)

---

<h2 id="english-en">🇺🇸 English (EN)</h2>

An advanced system to create **Artificial Intelligence (AI) phantom players** that scale and evolve within the game just like real human players. This system goes beyond visual "bots", providing dynamic entities that farm, level up, manage gear, and interact via chat using the Google Gemini API.

### 🌟 Key Features
* **Modular Architecture:** Clean codebase split into 4 independent files (`Manager`, `AI`, `Config`, `Chat`) for easy maintenance and scaling without breaking the core.
* **Smart Geodata & NPC Anchoring:** Phantoms "learn" the map by reading the GeoEngine memory and cloning the exact coordinates of real NPCs. This guarantees 100% legal Z-axis landings and prevents them from falling through the floor.
* **Realistic Progression:** Phantoms start in beginner zones and progress organically, changing gear grades (D, C, B, A, S) and classes as they level up.
* **Combat Intelligence:** They pick targets smartly (Anti-KS), maintain social distancing, and auto-restock Soulshots.
* **Dynamic AI Chat & Social Events:** Native integration with Google Gemini 1.5. Phantoms chat publicly, greet nearby players randomly, and reply to private messages (`.pm`).
* **TXT Logging System:** Includes a `.pdebug` mode that tracks the AI's thoughts and actions into a silent `PhantomManager.txt` file, preventing GameServer console flood.

### 📥 Installation
1. Drop the 4 Java files (`PhantomManager.java`, `PhantomAI.java`, `PhantomConfig.java`, `PhantomChat.java`) into your custom scripts folder (`data/scripts/custom/PhantomManager/`).
2. Place `PhantomPlayers.xml` inside `config/Custom/`.
3. (Optional) Edit `PhantomChat.java` and insert your Google Gemini API Key for the AI engine.
4. Restart the server or compile scripts.

### 💻 Commands
* **`.pstart`** / **`.pstop`** - Starts or stops the system and phantoms.
* **`.pmenu`** - Opens the In-Game Admin HTML Control Panel (using native anti-block bypasses).
* **`.pm [Name] [Message]`** - Send a private message to a phantom to test the AI.
* **`.pgo [Name]` / `.pbring [Name]` / `.pkill [Name]`** - Quick GM control commands.
* **`.pdebug`** - Toggles the TXT log recording on/off.

### 🛠️ Custom Adaptations & Contact
This free core was built specifically for **L2J Mobius Essence (RoseVain)**. However, the logic is universal.
I work privately to adapt and configure this system for **any other server pack or chronicle** (L2jFrozen, aCis, Lucera, High Five, Interlude, etc.).

**Get a free quote today!** If you need this system adapted to your server, or if you need private configurations and exclusive mods:
* ✉️ **Email:** [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
* 🌐 **Live Chat:** Reach out directly via the live chat on our website [miacodeweb.com](https://miacodeweb.com)

### ☕ Support & Donations
This project is managed by MiaCodeWeb. If this project helped your server, please consider donating to keep the development active!

[![Donate via PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://paypal.me/miacodeweb)

---

<h2 id="español-es">🇪🇸 Español (ES)</h2>

Un sistema avanzado para crear **jugadores phantom con Inteligencia Artificial (IA)** que escalan y evolucionan dentro del juego como si fuesen jugadores reales. El sistema no crea "bots de adorno", sino entidades dinámicas que farmean, suben de nivel, equipan armaduras y conversan a través del chat usando la API de Google Gemini.

### 🌟 Key Features (Características Principales)
* **Arquitectura Modular:** Código limpio y dividido en 4 archivos independientes (`Manager`, `AI`, `Config`, `Chat`) para facilitar su mantenimiento y escalar funciones sin romper el núcleo.
* **Geodata Inteligente y Anclaje a NPCs:** Los phantoms "aprenden" el mapa leyendo la memoria del motor GeoEngine y clonando las coordenadas exactas de los NPCs reales. Esto garantiza aterrizajes 100% legales (Eje Z perfecto) y evita que caigan al vacío.
* **Progresión Realista:** Comienzan en zonas iniciales y progresan orgánicamente, cambiando de equipo (Grados D, C, B, A, S) y clase a medida que suben de nivel.
* **Inteligencia de Combate:** Eligen objetivos inteligentemente (Anti-KS), mantienen distanciamiento social y recargan sus Soulshots automáticamente.
* **Chat IA Dinámico y Eventos Sociales:** Integración nativa con Google Gemini 1.5. Los phantoms hablan por chat general, saludan aleatoriamente a los jugadores cercanos y responden mensajes privados (`.pm`).
* **Sistema de Logs TXT:** Incluye un modo `.pdebug` que registra los pensamientos y acciones de la IA en un archivo `PhantomManager.txt` silencioso, evitando inundar la consola del GameServer.

### 📥 Installation (Instalación)
1. Copia los 4 archivos Java (`PhantomManager.java`, `PhantomAI.java`, `PhantomConfig.java`, `PhantomChat.java`) a la carpeta de scripts custom de tu servidor (`data/scripts/custom/PhantomManager/`).
2. Coloca el archivo `PhantomPlayers.xml` dentro de `config/Custom/`.
3. (Opcional) Edita el archivo `PhantomChat.java` y coloca tu API Key de Google Gemini para habilitar el Chat IA.
4. Reinicia el servidor o compila los scripts.

### 💻 Commands (Comandos)
* **`.pstart`** / **`.pstop`** - Inicia o detiene el sistema de phantoms.
* **`.pmenu`** - Abre el Panel de Control HTML de Administrador (con bypasses nativos anti-bloqueo).
* **`.pm [Nombre] [Mensaje]`** - Envía un mensaje privado a un phantom para poner a prueba la IA.
* **`.pgo [Nombre]` / `.pbring [Nombre]` / `.pkill [Nombre]`** - Comandos rápidos de control de GM.
* **`.pdebug`** - Activa o desactiva la generación del archivo de Logs TXT.

### 🛠️ Adaptaciones a Medida y Contacto
Este núcleo gratuito fue desarrollado específicamente para **L2J Mobius Essence (RoseVain)**. Sin embargo, el código fue diseñado de forma universal.
Trabajo de forma particular adaptando y configurando este sistema para **cualquier otro emulador, versión o crónica de servidor** (L2jFrozen, aCis, Lucera, High Five, Interlude, etc.).

**¡Pide tu presupuesto sin cargo!** Si necesitas este sistema adaptado a tu servidor, o configuraciones y mods exclusivos, contáctame:
* ✉️ **Correo:** [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
* 🌐 **Chat en vivo:** Comunícate directamente por el chat en nuestro sitio web [miacodeweb.com](https://miacodeweb.com)

### ☕ Soporte y Donaciones
Este proyecto es mantenido por MiaCodeWeb. Si este trabajo te fue de utilidad, ¡considera hacer una donación para mantener el desarrollo activo!

[![Donate via PayPal](https://img.shields.io/badge/Donate-PayPal-blue.svg?style=for-the-badge&logo=paypal)](https://paypal.me/miacodeweb)