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
* **Progressão Realista:** Os phantoms começam em zonas iniciais e progridem organicamente, trocando de equipamento e de classe à medida que sobem de nível.
* **Inteligência de Combate & Movimentação:** Nascem nas cidades, correm para os spots, escolhem alvos inteligentemente (evitando Kill Steal) e mantêm distanciamento social de outros bots.
* **Sistema de Geodata Failsafe:** Implementação de "Sky Drop" seguro e sistema de teletransporte automático anti-travamento para navegar por terrenos complexos sem afundar no mapa.
* **Chat IA Dinâmico:** Integração nativa com Google Gemini 1.5. Os phantoms conversam no chat geral e respondem a mensagens privadas (`.pm`) usando gírias gamers.
* **Gerenciamento de Inventário:** Auto-limpeza de itens inúteis e auto-abastecimento de Soulshots/Spiritshots.

### 📥 Installation (Instalação)
1. Copie o arquivo `PhantomManager.java` para o diretório de scripts customizados do seu servidor (`data/scripts/custom/PhantomManager/`).
2. Coloque o arquivo `PhantomPlayers.xml` na pasta `config/Custom/`.
3. (Opcional) Edite o `PhantomManager.java` e insira sua API Key do Google Gemini para habilitar o Chat IA.
4. Reinicie o servidor ou dê reload nos scripts.

### 💻 Commands (Comandos)
* **`.pstart`** / **`.pstop`** - Inicia ou para o sistema e os phantoms.
* **`.pmenu`** - Abre o Painel de Controle HTML do Admin no jogo.
* **`.pm [Nome] [Mensagem]`** - Envia uma mensagem privada para um phantom para testar a IA.
* **`.pgo [Nome]` / `.pbring [Nome]` / `.pkill [Nome]`** - Comandos rápidos de controle de GM.

### 🛠️ Adaptações Sob Medida & Contato
Este núcleo gratuito foi desenvolvido especificamente para **L2J Mobius Essence (RoseVain)**. No entanto, o código foi desenhado para ser universal. 
Trabalho de maneira particular adaptando e configurando este sistema para **qualquer outro servidor, emulador ou crônica** (L2jFrozen, aCis, Lucera, High Five, Interlude, etc.).

**Faço orçamentos totalmente gratuitos!** Se você precisa deste sistema adaptado para o seu servidor, ou de configurações e mods exclusivos:
* ✉️ **Email:** [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
* 🌐 **Chat ao vivo:** Fale comigo diretamente pelo chat em nosso site [miacodeweb.com](https://miacodeweb.com)

### ☕ Suporte e Doações
Este é um projeto mantido pela MiaCodeWeb. Se este projeto ajudou o seu servidor, considere fazer uma doação para manter o desenvolvimento ativo!

https://paypal.me/miacodeweb

---

<h2 id="english-en">🇺🇸 English (EN)</h2>

An advanced system to create **Artificial Intelligence (AI) phantom players** that scale and evolve within the game just like real human players. This system goes beyond visual "bots", providing dynamic entities that farm, level up, manage gear, and interact via chat using the Google Gemini API.

### 🌟 Key Features
* **Realistic Progression:** Phantoms start in beginner zones and progress organically, changing gear and classes as they level up.
* **Combat & Movement AI:** They spawn in towns, run to farming spots, pick targets smartly (Anti-KS), and maintain social distancing from other bots.
* **Geodata Failsafe System:** Safe "Sky Drop" implementation and automatic anti-stuck teleportation to navigate complex terrains safely.
* **Dynamic AI Chat:** Native integration with Google Gemini 1.5. Phantoms chat publicly and reply to private messages (`.pm`) using natural gamer slang.
* **Inventory Management:** Auto-cleans garbage drops to prevent weight limits and auto-generates Soulshots/Spiritshots.

### 📥 Installation
1. Drop `PhantomManager.java` into your custom scripts folder (`data/scripts/custom/PhantomManager/`).
2. Place `PhantomPlayers.xml` inside `config/Custom/`.
3. (Optional) Edit `PhantomManager.java` and insert your Google Gemini API Key for the AI Chat engine.
4. Restart the server or reload scripts.

### 💻 Commands
* **`.pstart`** / **`.pstop`** - Starts or stops the system and phantoms.
* **`.pmenu`** - Opens the In-Game Admin HTML Control Panel.
* **`.pm [Name] [Message]`** - Send a private message to a phantom to test the AI.
* **`.pgo [Name]` / `.pbring [Name]` / `.pkill [Name]`** - Quick GM control commands.

### 🛠️ Custom Adaptations & Contact
This free core was built specifically for **L2J Mobius Essence (RoseVain)**. However, the logic is universal.
I work privately to adapt and configure this system for **any other server pack or chronicle** (L2jFrozen, aCis, Lucera, High Five, Interlude, etc.).

**Get a free quote today!** If you need this system adapted to your server, or if you need private configurations and exclusive mods:
* ✉️ **Email:** [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
* 🌐 **Live Chat:** Reach out directly via the live chat on our website [miacodeweb.com](https://miacodeweb.com)

### ☕ Support & Donations
This project is managed by MiaCodeWeb. If this project helped your server, please consider donating to keep the development active!

https://paypal.me/miacodeweb

---

<h2 id="español-es">🇪🇸 Español (ES)</h2>

Un sistema avanzado para crear **jugadores phantom con Inteligencia Artificial (IA)** que escalan y evolucionan dentro del juego como si fuesen jugadores reales. El sistema no crea "bots de adorno", sino entidades dinámicas que farmean, suben de nivel, equipan armaduras y conversan a través del chat usando la API de Google Gemini.

### 🌟 Key Features (Características Principales)
* **Progresión Realista:** Los phantoms comienzan en zonas iniciales y progresan orgánicamente, cambiando de equipo y clase a medida que suben de nivel.
* **IA de Combate y Movimiento:** Nacen en las ciudades, corren hacia los spots, eligen objetivos inteligentemente (evitando el Kill Steal) y mantienen distanciamiento social de otros bots.
* **Sistema de Geodata Failsafe:** Implementación segura de "Sky Drop" y sistema de teletransporte automático anti-atascos para navegar terrenos complejos.
* **Chat IA Dinámico:** Integración nativa con Google Gemini 1.5. Los phantoms hablan por chat general y responden mensajes privados (`.pm`) usando jerga gamer.
* **Gestión de Inventario:** Auto-limpieza de basura del inventario y auto-abastecimiento de Soulshots/Spiritshots.

### 📥 Installation (Instalación)
1. Copia el archivo `PhantomManager.java` a la carpeta de scripts custom de tu servidor (`data/scripts/custom/PhantomManager/`).
2. Coloca el archivo `PhantomPlayers.xml` dentro de `config/Custom/`.
3. (Opcional) Edita el archivo `PhantomManager.java` y coloca tu API Key de Google Gemini para habilitar el Chat IA.
4. Reinicia el servidor o recarga los scripts.

### 💻 Commands (Comandos)
* **`.pstart`** / **`.pstop`** - Inicia o detiene el sistema de phantoms.
* **`.pmenu`** - Abre el Panel de Control HTML de Administrador dentro del juego.
* **`.pm [Nombre] [Mensaje]`** - Envía un mensaje privado a un phantom para poner a prueba la IA.
* **`.pgo [Nombre]` / `.pbring [Nombre]` / `.pkill [Nombre]`** - Comandos rápidos de control de GM.

### 🛠️ Adaptaciones a Medida y Contacto
Este núcleo gratuito fue desarrollado específicamente para **L2J Mobius Essence (RoseVain)**. Sin embargo, el código fue diseñado de forma universal.
Trabajo de forma particular adaptando y configurando este sistema para **cualquier otro emulador, versión o crónica de servidor** (L2jFrozen, aCis, Lucera, High Five, Interlude, etc.).

**¡Pide tu presupuesto sin cargo!** Si necesitas este sistema adaptado a tu servidor, o configuraciones y mods exclusivos, contáctame:
* ✉️ **Correo:** [contacto@miacodeweb.com](mailto:contacto@miacodeweb.com)
* 🌐 **Chat en vivo:** Comunícate directamente por el chat en nuestro sitio web [miacodeweb.com](https://miacodeweb.com)

### ☕ Soporte y Donaciones
Este proyecto es mantenido por MiaCodeWeb. Si este trabajo te fue de utilidad, ¡considera hacer una donación para mantener el desarrollo activo!

https://paypal.me/miacodeweb
