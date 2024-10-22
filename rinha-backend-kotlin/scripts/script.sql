CREATE  UNLOGGED  TABLE IF NOT EXISTS CLIENT
(
    CLIENT_ID    INT NOT NULL PRIMARY KEY,
    BALANCE      INT,
    CREDIT_LIMIT INT
);

CREATE UNLOGGED TABLE IF NOT EXISTS TRANSACTIONS
(
    ID          SERIAL PRIMARY KEY,
    AMOUNT      INT       NOT NULL,
    CREATED_AT  TIMESTAMP(6) WITH TIME ZONE,
    DESCRIPTION VARCHAR(10) NOT NULL,
    TYPE        VARCHAR(1),
    CLIENT_ID   INT       NOT NULL
);

DO
$$
    BEGIN
        INSERT INTO CLIENT (CLIENT_ID, CREDIT_LIMIT, BALANCE)
        VALUES (1, 1000 * 100, 0),
               (2, 800 * 100, 0),
               (3,  10000 * 100, 0),
               (4,  100000 * 100, 0),
               (5,  5000 * 100, 0);
    END;
$$;

CREATE INDEX idx_transacao_id_cliente ON TRANSACTIONS (client_id);
CREATE INDEX idx_transacao_id_cliente_realizada_em ON TRANSACTIONS (client_id, CREATED_AT DESC);

CREATE OR REPLACE FUNCTION register_transaction(
    clientIdParam INTEGER,
    typeParam character varying,
    amountParam BIGINT,
    descriptionParam character varying
)
    RETURNS TABLE(
                     saldoRetorno int,
                     limiteRetorno int
                 )
AS $$
DECLARE
    client client%ROWTYPE;
    newBalance int;
    affectedRows int;
BEGIN
    -- Bloquear a linha do cliente para atualização
    PERFORM * FROM client WHERE client_id = clientIdParam FOR UPDATE;

    -- Determinar se a transação é débito ou crédito
    IF typeParam = 'd' THEN
        newBalance := -amountParam; -- Débito subtrai do saldo
    ELSE
        newBalance := amountParam; -- Crédito adiciona ao saldo
    END IF;

    -- Atualizar o saldo do cliente
    UPDATE client
    SET balance = balance + newBalance
    WHERE client_id = clientIdParam
      AND (newBalance > 0 OR balance + newBalance >= credit_limit * -1) -- Verifica limite no caso de débito
    RETURNING * INTO client;

    -- Verificar se a atualização afetou alguma linha
    GET DIAGNOSTICS affectedRows = ROW_COUNT;

    IF affectedRows = 0 THEN
        RAISE EXCEPTION 'Cliente nao possui limite';
    END IF;

    -- Inserir registro da transação
    INSERT INTO transactions (client_id, amount, type, description, created_at)
    VALUES (clientIdParam, amountParam, typeParam, descriptionParam, current_timestamp);

    -- Retornar o novo saldo e limite
    RETURN QUERY SELECT client.balance, client.credit_limit;
END;
$$ LANGUAGE plpgsql;