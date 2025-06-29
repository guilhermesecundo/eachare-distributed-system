
# EACHare 

Este projeto é um EP da disciplina de Desenvolvimento de Sistemas Distribuídos. O EACHare é um sistema de compartilhamento de arquivos peer-to-peer simplificado que permite a transferência de arquivos entre múltiplos peers. O sistema utiliza transferência de arquivos baseada em *chunks*.

## Funcionalidades Principais

- Conexão peer-to-peer  
- Transferência de arquivos baseada em chunks  
- Busca de arquivos na rede  
- Download paralelizado de múltiplos peers  
- Estatísticas de desempenho de download  
- Gerenciamento dinâmico de tamanho de chunks  

## Compilação e Execução

### Pré-requisitos

- Java JDK 15 ou superior

### Compilação

Na pasta do projeto, execute os seguintes comandos:

```bash
javac -d bin src/main/Main.java src/network/*.java src/models/*.java
jar cfm eachare.jar src/manifest.txt -C bin .
```

Isso criará um arquivo executável `eachare.jar`.

### Execução

Para executar o programa, use o seguinte comando:

```bash
java -jar eachare.jar <endereco>:<porta> <vizinhos.txt> <diretorio>
```

Certifique-se de que:

* O arquivo `vizinhos.txt` exista no diretório atual
* O diretório de compartilhamento (`<diretorio>`) exista


## Arquivo de Vizinhos

O arquivo `vizinhos.txt` deve conter a lista de peers iniciais, um por linha, no formato:

```
<endereco_ip>:<porta>
```

## Diretório de Compartilhamento

O diretório especificado na execução será usado para:

* Listar os arquivos disponíveis para compartilhamento
* Salvar os arquivos baixados