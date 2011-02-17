-- Tabela, w ktorej przechowywane sa dostepne pokoje.
create table if not exists rooms (
  name varchar(255) not null,
  password varchar(255) not null,
  primary key (name)
);

