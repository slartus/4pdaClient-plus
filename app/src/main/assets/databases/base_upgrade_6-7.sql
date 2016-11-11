drop table if exists TabTopics;
CREATE TABLE TabTopics (Number integer PRIMARY KEY AUTOINCREMENT UNIQUE,
"_id" integer NOT NULL,
Template text NOT NULL,
MaxCount integer)

drop table if exists Topics;
CREATE TABLE Topics ("_id" text NOT NULL PRIMARY KEY,
Title text,
ForumId integer,
Description text,
LastMessageDate date,
LastMessageAuthor text,
HasNew boolean DEFAULT false)