INSERT INTO bank.currencies (code, rus_title, title, country, mark) VALUES ('RUB', 'Рубль', 'Russian Ruble', 'Россия', '₽') ON CONFLICT DO NOTHING;
INSERT INTO bank.currencies (code, rus_title, title, country, mark) VALUES ('USD', 'Доллар', 'US Dollar', 'США', '$') ON CONFLICT DO NOTHING;
INSERT INTO bank.currencies (code, rus_title, title, country, mark) VALUES ('EUR', 'Евро', 'Euro', 'Евро союз', '€') ON CONFLICT DO NOTHING;
INSERT INTO bank.currencies (code, rus_title, title, country, mark) VALUES ('CNY', 'Юань', 'Yuan Renminbi', 'Китай', '¥') ON CONFLICT DO NOTHING;
INSERT INTO bank.currencies (code, rus_title, title, country, mark) VALUES ('GBP', 'Фунт стерлингов', 'Pound Sterling', 'Великобритания', '£') ON CONFLICT DO NOTHING;
