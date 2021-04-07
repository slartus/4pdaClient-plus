<?php
header('Content-Type: application/json');
$PASSWORD = "MY_PASSWORD";// для безопасности замените пароль на свой

class Repository
{
    private $db;

    function __construct()
    {
        $this->db = new SQLite3('Notes.sqlite');
        $this->checkDb();
    }

    function checkDb()
    {
        $query = "SELECT name FROM sqlite_master WHERE type='table' AND name='Notes'";

        $result = $this->db->query($query);
        if ($result->fetchArray() === false) {
            $query = 'CREATE TABLE IF NOT EXISTS  android_metadata (locale TEXT);';
            $this->db->exec($query);
            $query = '
CREATE TABLE IF NOT EXISTS Notes ("_id" integer PRIMARY KEY AUTOINCREMENT,
Title text,
Body text,
Url text,
TopicId text,
PostId text,
UserId text,
User text,
Topic text,
"Date" date NOT NULL);';
            $this->db->exec($query);
        }
    }

    function getNotes()
    {
        $results = $this->db->query("select * from Notes");

        $data = array();
        while ($res = $results->fetchArray(1)) {
            $data[] = $res;
        }
        return $data;

    }

    function insertRow($title, $body, $url, $topicId, $topic,
                       $postId, $userId, $user, $date)
    {
        $query = "INSERT INTO 'Notes' (Title, Body, Url, TopicId, PostId, UserId, 'User', Topic, 'Date') VALUES (:Title, :Body, :Url, :TopicId, :PostId, :UserId, :User, :Topic, :date)";

        $stmt = $this->db->prepare($query);
        $stmt->bindValue(':Title', $title);
        $stmt->bindValue(':Body', $body);
        $stmt->bindValue(':Url', $url);
        $stmt->bindValue(':TopicId', $topicId);
        $stmt->bindValue(':PostId', $postId);
        $stmt->bindValue(':UserId', $userId);
        $stmt->bindValue(':User', $user);
        $stmt->bindValue(':Topic', $topic);
        $stmt->bindValue(':date', $date);
        $result = $stmt->execute();
        if ($result == FALSE) {
            throw new Exception($this->db->lastErrorMsg());
        }
    }

    function delete($id)
    {
        $query = "delete from 'Notes' where _id=:id";

        $stmt = $this->db->prepare($query);
        $stmt->bindValue(':id', $id);
        $result = $stmt->execute();
        if ($result == FALSE) {
            throw new Exception($this->db->lastErrorMsg());
        }
    }
}

$password = isset($_REQUEST["password"]) ? $_REQUEST["password"] : null;
if ($password != $PASSWORD) {
    return;
}


$action = isset($_REQUEST["action"]) ? $_REQUEST["action"] : null;

try {
    switch ($action) {
        case "get":
            $repository = new Repository();
            $notes = $repository->getNotes();
            echo json_encode($notes);
            break;
        case "ins":
            $data = json_decode(file_get_contents('php://input'), true);
            $repository = new Repository();
            $repository->insertRow($data['Title'], $data["Body"], $data["Url"],
                $data["TopicId"], $data["Topic"], $data["PostId"], $data["UserId"], $data["User"], $data["Date"]);
            $notes = $repository->getNotes();
            echo json_encode($notes);
            break;
        case "del":
            $id = isset($_REQUEST["id"]) ? $_REQUEST["id"] : null;
            $repository = new Repository();
            $repository->delete($id);
            $notes = $repository->getNotes();
            echo json_encode($notes);
            break;
    }
} catch (Exception $ex) {
    $error = array();
    $error["error"] = $ex->getMessage();
    echo json_encode($error);
}
