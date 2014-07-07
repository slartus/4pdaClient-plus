drop view if exists TopicsHistoryView;

CREATE VIEW TopicsHistoryView AS select t._id, t.Title,t.Description,ForumId,DateTime,URL from TopicsHistory h
left join Topics t on t._id=h.Topic_Id
order by DateTime Desc;

drop table if exists Topics;
CREATE TABLE Topics ("_id" integer NOT NULL PRIMARY KEY,
Title text,
ForumId integer,
Description text,
LastMessageDate date,
LastMessageAuthor text,
HasNew boolean DEFAULT false);

drop table if exists TabTopics;
CREATE TABLE TabTopics (Number integer PRIMARY KEY AUTOINCREMENT UNIQUE,
"_id" integer NOT NULL,
Template text NOT NULL)