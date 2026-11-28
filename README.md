# RPG: Os Salamancas (Guerra de Territórios)

Jogo de RPG de texto desenvolvido em **Java** com persistência de dados em **MySQL (JDBC)**, inspirado no universo de *Breaking Bad* e *Better Call Saul*. O jogador escolhe uma facção, gerencia dinheiro e respeito, utiliza itens de cura ou aumento de dano e batalha para dominar territórios.

---

## Integrantes do Grupo

* **Anderson Vanderlei** (`@andersonvanderleis-sys`): (Banco de Dados MySQL, Programação Java / POO, Integração JDBC e Versionamento Git)
* **Antônio Brasil** (`@antoniobrasil-arte`): (Idealização do tema, papel inicial: Java/Programação e documentação)
* **Dário Moreira** (`@dariomoreira-png`): (Idealização do tema, papel inicial: Git e documentação)

---

## Tecnologias Utilizadas

* **Linguagem:** Java (Orientação a Objetos)
* **Banco de Dados:** MySQL 8.0
* **Conexão:** JDBC (`mysql-connector-j`)
* **Versionamento:** Git & GitHub

---

## Como Executar o Projeto

### 1. Configurar o Banco de Dados
1. Abra o **MySQL Workbench** e conecte-se à sua instância local (`localhost:3306`).
2. Abra o arquivo `banco-de-dados-SQL` disponível na raiz deste repositório, copie todo o script SQL e execute-o na aba de consulta (`Query`) para criar o banco `rpg_salamancas`, as tabelas e os dados iniciais.

### 2. Configurar a Conexão no Código
1. Abra o projeto no **IntelliJ IDEA** (ou na sua IDE Java de preferência).
2. Verifique se o usuário e a senha do seu MySQL local correspondem aos configurados na classe de conexão JDBC (`root` / sua senha).

### 3. Iniciar o Jogo
1. Execute o método `main` da classe principal (`Main.java`).
2. Utilize o menu no console para criar ou carregar o seu personagem, explorar territórios, lutar contra inimigos e salvar o progresso antes de sair.
