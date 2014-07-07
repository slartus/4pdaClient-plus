CREATE TABLE Topics ("_id" integer NOT NULL PRIMARY KEY UNIQUE,Title text,ForumId integer,Description text);
CREATE TABLE TopicsHistory ("Topic_id" integer NOT NULL PRIMARY KEY UNIQUE,DateTime date NOT NULL,Url text);
CREATE VIEW TopicsHistoryView AS select t._id, t.Title,t.Description,ForumId, f.Title ForumTitle, DateTime,URL from TopicsHistory h
left join Topics t on t._id=h.Topic_Id 
left join Forums f on f._id=ForumId
order by DateTime Desc;