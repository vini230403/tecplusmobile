# TecPlusMobile - Sistema de Abertura e Gestão de Chamados

## Descrição
TecPlusMobile é um aplicativo Android para registro e gerenciamento de chamados de suporte técnico por colaboradores. Permite abertura, edição, exclusão de chamados, além de cadastro e login de usuários com validação e feedback visual.

O app utiliza armazenamento local via SharedPreferences e está preparado para futura integração com backend e inteligência artificial para automação de processamento e encaminhamento dos chamados.

## Funcionalidades

- Cadastro e login de usuários com validação de senha segura.
- Manutenção do login salvo via checkbox.
- Diálogo para abertura de chamados com seleção categorica e descrição opcional.
- Lista de chamados do usuário logado, com opção de edição e exclusão.
- Diálogo para edição de chamados com campos pré-preenchidos e validação.
- Perfil de usuário com opções para suporte, trocar senha e logout.
- Customização visual dos componentes UI (spinners, botões, toasts).
- Inclusão de ícones nos botões para melhor usabilidade.

## Tecnologias Utilizadas

- Kotlin
- Android Studio (AndroidX, Material Components)
- SharedPreferences para persistência local
- Layouts customizados XML para diálogos e listas
- Toasts customizados para feedback rápido

## Como usar

1. Clone o repositório.
2. Abra no Android Studio.
3. Instale os recursos gráficos e layouts (ícones, arquivos XML).
4. Compile e execute o app em um dispositivo ou emulador Android.
5. Cadastre um usuário, faça login e comece a abrir e gerenciar chamados.

## Melhorias futuras

- Integração com backend para persistência real dos dados.
- Implementação de IA para triagem automática e sugestões de soluções.
- Sincronização em tempo real e notificações push.
- Layouts responsivos e suporte à múltiplas resoluções de tela.

## Licença

Projeto destinado a uso educacional e de aprendizado, sem licença formal.
