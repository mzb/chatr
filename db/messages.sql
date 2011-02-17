-- Tabela, w ktorej przechowywane sa przeslane wiadomosci
-- (log rozmow)
create table if not exists messages (
  body text not null,
  room varchar(255) not null,
  sender varchar(255) not null,
  timestamp integer not null
);

