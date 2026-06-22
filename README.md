# Sistema de Gestao de Inventario

Aplicacao Java de consola para gestao de lojas, produtos, stock, vendas, utilizadores e relatorios. O projeto foi criado no NetBeans e usa ficheiros locais na pasta `data/` para persistir os dados.

## Requisitos

- JDK instalado. O projeto esta configurado em `nbproject/project.properties` com `javac.source=24` e `javac.target=24`.
- Apache Ant, para compilar/executar pelo terminal usando o `build.xml` gerado pelo NetBeans.
- NetBeans, opcional, para abrir e executar o projeto pela IDE.

## Estrutura do Projeto

```text
Inventory_Management/
├── src/                         Codigo-fonte Java
│   └── sistemadegestaodeinventario/
│       ├── modelo/              Entidades do sistema
│       ├── negocio/             Regras de negocio e managers
│       ├── persistencia/        Leitura/escrita dos ficheiros
│       └── ui/                  Menus de consola
├── data/                        Dados persistidos do sistema
├── nbproject/                   Configuracoes do projeto NetBeans
├── build.xml                    Script Ant do projeto
├── manifest.mf                  Manifesto do JAR
├── sources.txt                  Lista de fontes Java
└── dist/                        JAR gerado apos build
```

## Execucao pelo Terminal

### 1. Entrar na pasta do projeto

```bash
cd /caminho/para/Inventory_Management
```

### 2. Compilar o projeto

```bash
ant clean
ant jar
```

Ou, para compilar e executar diretamente:

```bash
ant run
```

### 3. Executar o JAR gerado

Depois do build, o ficheiro principal fica em `dist/SistemaDeGestaoDeInventario.jar`.

```bash
java -jar dist/SistemaDeGestaoDeInventario.jar
```

Tambem pode entrar na pasta `dist` e executar:

```bash
cd dist
java -jar SistemaDeGestaoDeInventario.jar
```

## Execucao no NetBeans

1. Abrir o NetBeans.
2. Escolher `File > Open Project`.
3. Selecionar a pasta `Inventory_Management`.
4. Confirmar que o projeto aparece como `SistemaDeGestaoDeInventario`.
5. Clicar com o botao direito no projeto e escolher `Clean and Build`.
6. Para executar, clicar em `Run Project` ou pressionar `F6`.

A classe principal configurada e:

```text
sistemadegestaodeinventario.SistemaDeGestaoDeInventario
```

## Primeiro Acesso

Se nao existir nenhum administrador, o sistema cria automaticamente uma conta admin inicial:

```text
Login: admin@system
Senha: Admin1234
```

A senha e guardada de forma criptografada no ficheiro `data/usuarios.txt` depois do carregamento/salvamento dos dados.

## Recursos do Programa

### Autenticacao

- Login de vendedor.
- Login de gestor de stock.
- Login de administrador.
- Validacao separada de login e senha.
- Repeticao do login quando a credencial esta incorreta.
- Repeticao apenas da senha quando a senha esta incorreta.
- Senhas persistidas com hash PBKDF2 e sal.

### Gestao de Lojas

- Adicionar nova loja.
- Listar todas as lojas.
- Selecionar loja ativa.
- Ver detalhes da loja ativa.
- Deletar loja.
- Importar loja e produtos via ficheiro CSV.

### Gestao de Produtos

- Adicionar produto a loja selecionada.
- Listar produtos da loja ativa.
- Consultar stock de um produto.
- Aumentar stock de produto.
- Listar produtos com stock baixo.
- Deletar produto.

### Vendas

- Registar nova venda.
- Validar existencia dos produtos vendidos.
- Validar stock suficiente antes da venda.
- Diminuir stock automaticamente apos venda.
- Guardar historico de vendas por loja.
- Ver historico de vendas da loja ativa.
- Consultar valor total vendido.

### Relatorios e Consultas

- Relatorio da loja atual.
- Inventario completo de todas as lojas.
- Relatorio geral do sistema.
- Total de lojas, produtos e vendas.

### Configuracao

- Informacoes do sistema.
- Criacao de backup basico dos dados.
- Insercao de dados de teste.
- Gestao de utilizadores.
- Criacao e listagem de utilizadores.
- Associacao obrigatoria de vendedores a uma loja existente.

## Ficheiros de Dados

Os dados sao guardados na pasta `data/`.

### Utilizadores

```text
data/usuarios.txt
```

Formato:

```text
credencial|senha_criptografada|PERFIL|idLoja
```

Exemplo de perfil:

```text
ADMIN
GESTOR_STOCK
VENDEDOR
```

O campo `idLoja` e usado para vendedores. Administradores e gestores podem ficar com este campo vazio.

### Lojas

```text
data/lojas.txt
```

Formato:

```text
idLoja|nome|morada|telefone
```

### Produtos por Loja

Cada loja tem um ficheiro proprio de produtos:

```text
data/loja_<ID_DA_LOJA>_produtos.txt
```

Formato:

```text
idProduto|nome|descricao|preco|quantidadeEmStock|quantidadeMinima
```

### Vendas por Loja

Cada loja tem um ficheiro proprio de vendas:

```text
data/loja_<ID_DA_LOJA>_vendas.csv
```

Formato CSV:

```text
ID_VENDA,DATA_VENDA,ID_LOJA,ID_PRODUTO,NOME_PRODUTO,QUANTIDADE,PRECO_UNITARIO,SUBTOTAL
```

## Importacao CSV de Loja

A importacao espera um ficheiro CSV onde a primeira linha contem os dados da loja:

```text
id,nome,morada,telefone
```

As linhas seguintes devem conter os produtos:

```text
idProduto,nome,descricao,preco,quantidade,quantidadeMinima
```

Exemplo:

```csv
L003,Loja Sul,Rua Comercial 25,912345678
P010,Feijao 1kg,Feijao seco,2.50,30,5
P011,Massa 500g,Massa alimentar,1.20,50,10
```

## Perfis de Utilizador

### ADMIN

Pode aceder a todos os menus:

- Gestao de lojas.
- Gestao de produtos.
- Registo de vendas.
- Relatorios.
- Configuracao do sistema.
- Gestao de utilizadores.

### GESTOR_STOCK

Pode aceder aos recursos operacionais do sistema, mas nao pode criar ou gerir utilizadores:

- Gestao de lojas.
- Gestao de produtos.
- Registo de vendas.
- Relatorios.
- Configuracao sem gestao de utilizadores.

### VENDEDOR

Deve estar associado a uma loja existente. Pode apenas:

- Consultar stock da sua loja.
- Registar vendas da sua loja.

## Observacoes

- A aplicacao e executada no terminal/console.
- Os ficheiros em `data/` devem permanecer junto da raiz do projeto quando executar pelo NetBeans ou pelo terminal a partir da raiz.
- Ao executar o JAR dentro de `dist/`, a pasta `data/` sera procurada relativamente a pasta atual. Para usar os dados existentes, execute o JAR a partir da raiz do projeto ou copie a pasta `data/` para o diretorio de execucao.
- O projeto nao depende de bibliotecas externas para executar a logica principal.
