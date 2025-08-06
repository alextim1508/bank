INSERT INTO bank.accounts (login,
                           password,
                           first_name,
                           last_name,
                           birth_date,
                           blocked,
                           roles,
                           created_at)
VALUES ('Aleksey',
        '$2a$10$dOSxKBc1hzzCNkCkdznZ7uvSqaD5LVbCMH6n4NpjfU2JFfjxTa4Xu', -- хеш от "pass"
        'Aleksey',
        'Timofeev',
        '1988-08-15',
        false,
        '["USER"]',
        '2025-07-28 10:00:00')
ON CONFLICT DO NOTHING;

INSERT INTO bank.balances (currency_code,
                           amount,
                           frozen_amount,
                           locked,
                           locking_time,
                           opened,
                           account_id)
VALUES ('RUB', 800.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Aleksey')),
       ('USD', 500.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Aleksey')),
       ('EUR', 300.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Aleksey')),
       ('CNY', 350.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Aleksey')),
       ('GBP', 120.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Aleksey'))
ON CONFLICT DO NOTHING;

INSERT INTO bank.contacts (type,
                           value,
                           account_id)
VALUES ('EMAIL', 'atimofeev1508@yandex.ru', (SELECT id FROM bank.accounts WHERE login = 'Aleksey')),
       ('TELEGRAM', 'alextim1508', (SELECT id FROM bank.accounts WHERE login = 'Aleksey')),
       ('PHONE', '+79602574201', (SELECT id FROM bank.accounts WHERE login = 'Aleksey'))
ON CONFLICT DO NOTHING;


INSERT INTO bank.accounts (login,
                           password,
                           first_name,
                           last_name,
                           birth_date,
                           blocked,
                           roles,
                           created_at)
VALUES ('Ivan',
        '$2a$10$dOSxKBc1hzzCNkCkdznZ7uvSqaD5LVbCMH6n4NpjfU2JFfjxTa4Xu', -- хеш от "pass"
        'Ivan',
        'Ivan',
        '2002-01-20',
        false,
        '["USER"]',
        '2025-07-28 10:00:00')
ON CONFLICT DO NOTHING;

INSERT INTO bank.balances (currency_code,
                           amount,
                           frozen_amount,
                           locked,
                           locking_time,
                           opened,
                           account_id)
VALUES ('RUB', 900.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Ivan')),
       ('USD', 600.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Ivan')),
       ('EUR', 310.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Ivan')),
       ('CNY', 320.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Ivan')),
       ('GBP', 140.00, 0.00, false, NULL, true, (SELECT id FROM bank.accounts WHERE login = 'Ivan'))
ON CONFLICT DO NOTHING;

INSERT INTO bank.contacts (type,
                           value,
                           account_id)
VALUES ('EMAIL', 'ivan0120@yandex.ru', (SELECT id FROM bank.accounts WHERE login = 'Ivan')),
       ('TELEGRAM', 'ivan0120', (SELECT id FROM bank.accounts WHERE login = 'Ivan')),
       ('PHONE', '+79602574202', (SELECT id FROM bank.accounts WHERE login = 'Ivan'))
ON CONFLICT DO NOTHING;