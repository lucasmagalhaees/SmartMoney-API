CREATE TABLE categoria(
 codigo BIGINT(28) PRIMARY KEY AUTO_INCREMENT,
 nome VARCHAR(50) NOT NULL
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;


insert into categoria(nome) values('Lazer');
insert into categoria(nome) values('Alimentação');
insert into categoria(nome) values('Supermercado');
insert into categoria(nome) values('Farmácia');
insert into categoria(nome) values('Outros');