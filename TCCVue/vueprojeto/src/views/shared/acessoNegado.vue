    <template>
        <div class="tela_inicial">
            <div class="imagem">
                <img src="@/assets/a951c0cc-0659-417c-bbbe-aa8f6cb892e4.png" alt="ilustracao">
            </div>

            <div class="texto">
                <h1>Acesso Negado</h1>
                <h3>Ops, o nosso sistema não reconheceu este dispositivo. Por
                    razões de segurança, este IP está bloqueado no nosso sistema</h3>
                <img src="@/assets/t.webp" alt="ilustracao">
            </div>
        </div>
    </template>

    <script>

    </script>

    <style>
    .tela_inicial {
        display: flex;
        height: 100vh;
        width: 100%;
    }

    .imagem {
        flex: 1;
        background: #fff;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .imagem img {
        max-width: 70%;
        height: auto;
    }

    .texto {
        flex: 1;
        background: #111;
        color: #fff;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        text-align: center;
        padding: 2rem;
    }

    .texto h1 {
        font-size: 2.5rem;
        font-weight: bold;
        margin-bottom: 1rem;
    }

    .texto h3 {
        font-size: 1rem;
        font-weight: 400;
        margin-bottom: 2rem;
        line-height: 1.5rem;
        max-width: 80%;
    }

    .texto img {
        max-width: 120px;
        margin-top: 1rem;
    }
    </style>