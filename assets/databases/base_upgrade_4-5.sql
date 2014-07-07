drop table if exists Notes;
CREATE TABLE Notes ("_id" integer PRIMARY KEY AUTOINCREMENT,
Title text,
Body text,
Url text,
TopicId text,
PostId text,
UserId text,
User text,
Topic text,
"Date" date NOT NULL);