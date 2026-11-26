
# Registro de Uso de IA (IA_LOG) - Anderson

## Entrada 1 - 24/11/2026 (Planejamento Inicial)
* **Integrante:** Anderson (Banco de Dados)
* **Ferramenta:** Gemini
* **Objetivo:** Entender o cronograma de entregas, definir os primeiros passos no GitHub e estruturar as entidades iniciais do banco de dados para o tema *Breaking Bad / Better Call Saul* (Guerra de Territórios).
* **Resultado:** Definição da estrutura inicial do README e levantamento das tabelas principais (`gangue`, `jogador`, `territorio`, `inimigo`).

## Registro de Uso de IA - Anderson

### 1. Estruturação Base das Classes Java (POO)
* **Por que utilizei:** Para acelerar a criação da estrutura inicial do RPG (herança, encapsulamento e relacionamento entre as classes `Personagem`, `Jogador`, `Inimigo`, `Gangue`, `Territorio`, `Item` e o fluxo principal na `Main`), garantindo uma base sólida de Programação Orientada a Objetos para depois adaptar às regras específicas do nosso tema.

### 2. Configuração e Sincronização do Git no IntelliJ
* **Por que utilizei:** Para entender e resolver conflitos de histórico inicial (`--allow-unrelated-histories`), renomeação de branch (`master` para `main`) e configuração do `.gitignore`/arquivos locais, evitando perda de código ou sobrescrita do trabalho dos outros integrantes no repositório do grupo.


## Etapa: Persistência de Dados com MySQL, Padrão DAO e Versionamento (Git/GitHub) - Anderson

### Objetivo do Uso da IA
Auxiliar na transição do jogo `rpg-os-salamancas` de armazenamento temporário em memória (`ArrayList`) para persistência real em banco de dados relacional (MySQL) via JDBC, além de orientar o fluxo de versionamento (Commit e Push) no IntelliJ IDEA para o GitHub.

### Principais Implementações e Aprendizados
* **Conexão JDBC (`ConexaoDB.java`):** Configuração da ponte de comunicação entre o Java e o banco local `rpg_salamancas` utilizando `DriverManager` e o driver `mysql-connector-j`.
* **Padrão DAO (`JogadorDAO.java`):** Separação da lógica de acesso a dados (`SELECT`, `INSERT` e `UPDATE`) das regras de negócio, permitindo carregar gangues, territórios e inimigos do banco, além de salvar o progresso do jogador, conquistas de território e itens coletados no inventário.
* **Refatoração da `Main.java`:** Substituição das listas estáticas iniciais por chamadas diretas ao `JogadorDAO`, garantindo que cada ação relevante do jogador seja persistida no MySQL em tempo real.
* **Versionamento no GitHub:** Resolução de conflito de nomenclatura de classe (`JogaDAO` -> `JogadorDAO`), autenticação da IDE com o GitHub e envio das novas classes para o repositório remoto via Commit e Push.
